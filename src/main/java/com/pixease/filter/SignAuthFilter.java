package com.pixease.filter;

import com.alibaba.fastjson2.JSON;
import com.pixease.common.constant.CommonConstant;
import com.pixease.common.enums.ResultCode;
import com.pixease.common.result.Result;
import com.pixease.common.utils.SignUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 签名验证过滤器
 * 验证请求中的签名参数，防止参数篡改和重放攻击
 *
 * 请求头要求：
 * - X-Sign: 签名值（参数+时间戳+随机字符串+密钥 → MD5）
 * - X-Timestamp: 毫秒级时间戳
 * - X-Nonce: 随机字符串（防重放）
 */
@Slf4j
@Component
public class SignAuthFilter implements Filter {

    private final StringRedisTemplate redisTemplate;

    /** 签名密钥 */
    @Value("${pixease.sign.secret}")
    private String signSecret;

    /** 签名有效期（秒） */
    @Value("${pixease.sign.expire-seconds}")
    private long signExpireSeconds;

    public SignAuthFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 过滤逻辑：
     * 1. 校验签名参数是否齐全
     * 2. 校验时间戳是否在有效期内
     * 3. 校验Nonce是否重复（防重放攻击）
     * 4. 验证签名是否正确
     * 5. 验证通过后将Nonce存入Redis
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String sign = httpRequest.getHeader(CommonConstant.SIGN_HEADER);
        String timestamp = httpRequest.getHeader(CommonConstant.TIMESTAMP_HEADER);
        String nonce = httpRequest.getHeader(CommonConstant.NONCE_HEADER);

        if (StringUtils.isBlank(sign) || StringUtils.isBlank(timestamp) || StringUtils.isBlank(nonce)) {
            writeErrorResponse(httpResponse, ResultCode.SIGN_ERROR.getCode(), "缺少签名参数");
            return;
        }

        long requestTime;
        try {
            requestTime = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            writeErrorResponse(httpResponse, ResultCode.SIGN_ERROR.getCode(), "时间戳格式错误");
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - requestTime) > signExpireSeconds * 1000) {
            writeErrorResponse(httpResponse, ResultCode.SIGN_EXPIRED.getCode(),
                    ResultCode.SIGN_EXPIRED.getMessage());
            return;
        }

        String nonceKey = CommonConstant.REDIS_NONCE_PREFIX + nonce;
        Boolean nonceExists = redisTemplate.hasKey(nonceKey);
        if (nonceExists != null && nonceExists) {
            writeErrorResponse(httpResponse, ResultCode.SIGN_ERROR.getCode(), "重复的请求");
            return;
        }

        Map<String, String> params = new HashMap<>();
        httpRequest.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        ServletRequest requestToUse = request;
        String contentType = httpRequest.getContentType();
        String method = httpRequest.getMethod();
        if (contentType != null && contentType.contains("application/json")
                && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method))) {
            try {
                CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);
                String body = cachedRequest.getCachedBodyString();
                if (StringUtils.isNotBlank(body)) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> jsonParams = JSON.parseObject(body, Map.class);
                        if (jsonParams != null) {
                            jsonParams.forEach((key, value) -> {
                                if (value != null) {
                                    params.put(key, value.toString());
                                }
                            });
                        }
                    } catch (Exception e) {
                        log.warn("解析 JSON 请求体失败: {}", e.getMessage());
                    }
                }
                requestToUse = cachedRequest;
            } catch (IOException e) {
                log.warn("读取请求体失败: {}", e.getMessage());
            }
        }

        boolean verified = SignUtils.verifySign(params, timestamp, nonce, signSecret, sign);
        if (!verified) {
            writeErrorResponse(httpResponse, ResultCode.SIGN_ERROR.getCode(),
                    ResultCode.SIGN_ERROR.getMessage());
            return;
        }

        redisTemplate.opsForValue().set(nonceKey, "1", signExpireSeconds, TimeUnit.SECONDS);

        chain.doFilter(requestToUse, response);
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        log.warn("签名验证失败: code={}, message={}", code, message);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Result<Void> result = Result.fail(code, message);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
