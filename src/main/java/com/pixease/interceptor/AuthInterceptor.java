package com.pixease.interceptor;

import com.alibaba.fastjson2.JSON;
import com.pixease.common.constant.CommonConstant;
import com.pixease.common.enums.ResultCode;
import com.pixease.common.result.Result;
import com.pixease.common.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Token鉴权拦截器
 * 校验请求头中的JWT Token，验证用户身份
 * 登录和注册接口不需要Token，其他接口均需携带有效Token
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    /** Token签名密钥 */
    @Value("${pixease.token.secret}")
    private String tokenSecret;

    public AuthInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 前置拦截逻辑：
     * 1. 放行OPTIONS预检请求
     * 2. 从请求头获取Token
     * 3. 验证Token格式和签名
     * 4. 验证Redis中Token是否存在且一致
     * 5. 将userId和role注入请求属性供Controller使用
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String tokenHeader = request.getHeader(CommonConstant.TOKEN_HEADER);
        if (StringUtils.isBlank(tokenHeader) || !tokenHeader.startsWith(CommonConstant.TOKEN_PREFIX)) {
            writeErrorResponse(response, ResultCode.UNAUTHORIZED.getCode(), "未提供有效的Token");
            return false;
        }

        String token = tokenHeader.substring(CommonConstant.TOKEN_PREFIX.length());

        if (!TokenUtils.validateToken(token, tokenSecret)) {
            writeErrorResponse(response, ResultCode.TOKEN_INVALID.getCode(),
                    ResultCode.TOKEN_INVALID.getMessage());
            return false;
        }

        Long userId = TokenUtils.getUserId(token, tokenSecret);
        String redisToken = redisTemplate.opsForValue().get(CommonConstant.REDIS_TOKEN_PREFIX + userId);

        if (StringUtils.isBlank(redisToken) || !redisToken.equals(token)) {
            writeErrorResponse(response, ResultCode.TOKEN_EXPIRED.getCode(),
                    ResultCode.TOKEN_EXPIRED.getMessage());
            return false;
        }

        Integer role = TokenUtils.getRole(token, tokenSecret);

        request.setAttribute("userId", userId);
        request.setAttribute("role", role);
        return true;
    }

    /**
     * 写入鉴权失败响应
     */
    private void writeErrorResponse(HttpServletResponse response, int code, String message) throws Exception {
        log.warn("鉴权失败: code={}, message={}", code, message);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Result<Void> result = Result.fail(code, message);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
