package com.pixease.service.impl;

import com.pixease.common.constant.CommonConstant;
import com.pixease.common.enums.ResultCode;
import com.pixease.common.exception.BusinessException;
import com.pixease.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务实现类
 * 基于JavaMailSender发送验证码邮件，验证码存储在Redis中
 * 包含发送频率限制、每日限额、验证码校验失败次数限制
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    /** 发件人邮箱地址 */
    @Value("${spring.mail.username}")
    private String fromEmail;

    /** 验证码过期时间（分钟） */
    @Value("${pixease.email.code-expire-minutes}")
    private long codeExpireMinutes;

    /** 验证码长度 */
    @Value("${pixease.email.code-length}")
    private int codeLength;

    /** 验证码发送间隔（秒） */
    @Value("${pixease.email.send-interval-seconds}")
    private long sendIntervalSeconds;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public EmailServiceImpl(JavaMailSender mailSender, StringRedisTemplate redisTemplate) {
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 发送邮箱验证码
     * 1. 检查发送频率限制（60秒间隔）
     * 2. 检查每日发送限额（同一邮箱每天最多10次）
     * 3. 生成随机验证码
     * 4. 发送邮件
     * 5. 将验证码存入Redis
     */
    @Override
    public void sendVerifyCode(String email) {
        String intervalKey = CommonConstant.REDIS_EMAIL_INTERVAL_PREFIX + email;
        String intervalValue = redisTemplate.opsForValue().get(intervalKey);
        if (intervalValue != null) {
            throw new BusinessException(ResultCode.CODE_SEND_TOO_FREQUENT);
        }

        String dailyKey = CommonConstant.REDIS_EMAIL_DAILY_PREFIX + email;
        String dailyCountStr = redisTemplate.opsForValue().get(dailyKey);
        int dailyCount = dailyCountStr != null ? Integer.parseInt(dailyCountStr) : 0;
        if (dailyCount >= CommonConstant.EMAIL_DAILY_MAX) {
            throw new BusinessException(ResultCode.CODE_DAILY_LIMIT);
        }

        String code = generateCode();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("PixEase - 邮箱验证码");
        message.setText("您的验证码为：" + code + "，有效期" + codeExpireMinutes + "分钟，请勿泄露给他人。");

        try {
            mailSender.send(message);
            log.info("验证码邮件发送成功: email={}", email);
        } catch (Exception e) {
            log.error("验证码邮件发送失败: email={}", email, e);
            throw new BusinessException("邮件发送失败，请稍后再试");
        }

        String codeKey = CommonConstant.REDIS_EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey, code, codeExpireMinutes, TimeUnit.MINUTES);

        redisTemplate.opsForValue().set(intervalKey, "1", sendIntervalSeconds, TimeUnit.SECONDS);

        if (dailyCount == 0) {
            redisTemplate.opsForValue().set(dailyKey, "1", 1, TimeUnit.DAYS);
        } else {
            redisTemplate.opsForValue().increment(dailyKey);
        }
    }

    /**
     * 校验邮箱验证码
     * 1. 从Redis获取验证码进行比对
     * 2. 校验失败累计失败次数，达到上限后作废验证码
     * 3. 校验成功后删除验证码和失败计数（一次性使用）
     */
    @Override
    public boolean verifyCode(String email, String code) {
        String codeKey = CommonConstant.REDIS_EMAIL_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (StringUtils.isBlank(storedCode)) {
            throw new BusinessException(ResultCode.CODE_EXPIRED);
        }

        if (!storedCode.equals(code)) {
            String failKey = CommonConstant.REDIS_CODE_FAIL_PREFIX + email;
            String failCountStr = redisTemplate.opsForValue().get(failKey);
            int failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
            failCount++;

            if (failCount >= CommonConstant.CODE_FAIL_MAX) {
                redisTemplate.delete(codeKey);
                redisTemplate.delete(failKey);
                throw new BusinessException(ResultCode.CODE_FAIL_TOO_MANY);
            }

            redisTemplate.opsForValue().set(failKey, String.valueOf(failCount), codeExpireMinutes, TimeUnit.MINUTES);
            return false;
        }

        redisTemplate.delete(codeKey);

        String failKey = CommonConstant.REDIS_CODE_FAIL_PREFIX + email;
        redisTemplate.delete(failKey);

        return true;
    }

    /**
     * 生成指定长度的数字验证码
     */
    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
