package com.pixease.common.utils;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP地址工具类，从请求中获取客户端真实IP
 */
public class IpUtils {

    private IpUtils() {}

    private static final String UNKNOWN = "unknown";

    /**
     * 获取客户端真实IP地址，支持代理环境
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (StringUtils.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
