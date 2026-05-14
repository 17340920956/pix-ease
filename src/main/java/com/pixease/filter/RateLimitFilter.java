package com.pixease.filter;

import com.alibaba.fastjson2.JSON;
import com.pixease.common.constant.CommonConstant;
import com.pixease.common.enums.ResultCode;
import com.pixease.common.result.Result;
import com.pixease.common.utils.IpUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * IP限流过滤器
 * 基于Redis实现滑动窗口限流，防止同一IP在短时间内频繁请求
 * 默认配置：60秒内最多60次请求
 */
@Slf4j
@Component
public class RateLimitFilter implements Filter {

    private final StringRedisTemplate redisTemplate;

    /** 窗口内最大请求数 */
    @Value("${pixease.rate-limit.max-requests}")
    private long maxRequests;

    /** 限流窗口时间（秒） */
    @Value("${pixease.rate-limit.window-seconds}")
    private long windowSeconds;

    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 过滤逻辑：
     * 1. 获取客户端IP
     * 2. 从Redis获取该IP在窗口内的请求计数
     * 3. 超过限制则拒绝请求，否则计数+1并放行
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ip = IpUtils.getIpAddr(httpRequest);
        String key = CommonConstant.REDIS_RATE_LIMIT_PREFIX + ip;

        String countStr = redisTemplate.opsForValue().get(key);
        long count = countStr != null ? Long.parseLong(countStr) : 0;

        if (count >= maxRequests) {
            log.warn("IP限流触发: ip={}, count={}", ip, count);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.setStatus(429);
            Result<Void> result = Result.fail(ResultCode.RATE_LIMIT_EXCEEDED.getCode(),
                    ResultCode.RATE_LIMIT_EXCEEDED.getMessage());
            httpResponse.getWriter().write(JSON.toJSONString(result));
            return;
        }

        if (count == 0) {
            redisTemplate.opsForValue().set(key, "1", windowSeconds, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().increment(key);
        }

        chain.doFilter(request, response);
    }
}
