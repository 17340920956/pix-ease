package com.pixease.common.config;

import com.pixease.filter.RateLimitFilter;
import com.pixease.filter.SignAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Filter;

/**
 * 过滤器注册配置类
 * 按顺序注册IP限流过滤器和签名验证过滤器
 */
@Configuration
public class FilterConfig {

    /**
     * 注册IP限流过滤器（优先级最高，Order=1）
     */
    @Bean
    public FilterRegistrationBean<Filter> rateLimitFilterRegistration(RateLimitFilter rateLimitFilter) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("rateLimitFilter");
        return registration;
    }

    /**
     * 注册签名验证过滤器（Order=2，在限流过滤器之后执行）
     */
    @Bean
    public FilterRegistrationBean<Filter> signAuthFilterRegistration(SignAuthFilter signAuthFilter) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(signAuthFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        registration.setName("signAuthFilter");
        return registration;
    }
}
