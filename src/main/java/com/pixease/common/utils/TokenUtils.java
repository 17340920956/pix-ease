package com.pixease.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token工具类，负责Token的生成、解析和验证
 */
public class TokenUtils {

    private TokenUtils() {}

    /**
     * 生成JWT Token
     *
     * @param userId      用户ID
     * @param account     用户账号
     * @param role        用户角色
     * @param secret      签名密钥
     * @param expireMillis 过期时间（毫秒）
     * @return JWT Token字符串
     */
    public static String generateToken(Long userId, String account, Integer role, String secret, long expireMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("account", account);
        claims.put("role", role);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims)
                .subject(account)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMillis))
                .signWith(key)
                .compact();
    }

    /**
     * 解析JWT Token，返回Claims
     *
     * @param token  JWT Token字符串
     * @param secret 签名密钥
     * @return Claims对象
     */
    public static Claims parseToken(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token  JWT Token字符串
     * @param secret 签名密钥
     * @return 用户ID
     */
    public static Long getUserId(String token, String secret) {
        Claims claims = parseToken(token, secret);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从Token中获取用户角色
     *
     * @param token  JWT Token字符串
     * @param secret 签名密钥
     * @return 用户角色值
     */
    public static Integer getRole(String token, String secret) {
        Claims claims = parseToken(token, secret);
        Object role = claims.get("role");
        if (role instanceof Integer) {
            return (Integer) role;
        }
        return 0;
    }

    /**
     * 验证Token是否有效
     *
     * @param token  JWT Token字符串
     * @param secret 签名密钥
     * @return Token是否有效
     */
    public static boolean validateToken(String token, String secret) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        try {
            parseToken(token, secret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
