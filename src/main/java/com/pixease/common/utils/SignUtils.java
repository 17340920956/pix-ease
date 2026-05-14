package com.pixease.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;

/**
 * 签名工具类，用于接口签名生成与验证
 * 签名算法：将请求参数按key字典序排列，拼接时间戳、随机字符串和密钥后做MD5加密
 */
public class SignUtils {

    private SignUtils() {}

    /**
     * 生成签名
     * 算法步骤：
     * 1. 将请求参数按key字典序排列
     * 2. 拼接为 key1=value1&key2=value2&timestamp=xxx&nonce=xxx&secret=xxx
     * 3. 对拼接字符串做MD5加密
     *
     * @param params   请求参数Map
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @param secret    签名密钥
     * @return MD5签名字符串
     */
    public static String generateSign(Map<String, String> params, String timestamp, String nonce, String secret) {
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (StringUtils.isNotBlank(value)) {
                sb.append(key).append("=").append(value).append("&");
            }
        }

        sb.append("timestamp=").append(timestamp).append("&");
        sb.append("nonce=").append(nonce).append("&");
        sb.append("secret=").append(secret);

        return md5(sb.toString());
    }

    /**
     * 验证签名是否匹配
     *
     * @param params   请求参数Map
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @param secret    签名密钥
     * @param sign      待验证的签名
     * @return 签名是否有效
     */
    public static boolean verifySign(Map<String, String> params, String timestamp, String nonce, String secret, String sign) {
        String calculated = generateSign(params, timestamp, nonce, secret);
        return calculated.equalsIgnoreCase(sign);
    }

    /**
     * MD5加密
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}
