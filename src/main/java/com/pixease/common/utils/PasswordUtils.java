package com.pixease.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类，基于BCrypt算法进行密码加密和验证
 * BCrypt输出固定60字符，包含算法标识、成本因子、盐和哈希值
 * 成本因子设为8，兼顾安全性与性能
 */
public class PasswordUtils {

    private PasswordUtils() {}

    /** 成本因子，值越大计算越慢越安全，8约需40ms，10约需100ms */
    private static final int STRENGTH = 8;

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(STRENGTH);

    /**
     * 对原始密码进行BCrypt加密
     * 输出格式：$2a$08${22字符盐}{31字符哈希}，固定60字符
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码（60字符）
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证原始密码与加密密码是否匹配
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
