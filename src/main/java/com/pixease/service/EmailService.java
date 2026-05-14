package com.pixease.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送邮箱验证码
     * 验证码存入Redis并设置过期时间，同时限制发送频率
     *
     * @param email 目标邮箱地址
     */
    void sendVerifyCode(String email);

    /**
     * 校验邮箱验证码
     *
     * @param email 目标邮箱地址
     * @param code  用户输入的验证码
     * @return 验证码是否正确
     */
    boolean verifyCode(String email, String code);
}
