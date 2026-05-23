package com.pixease.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pixease.common.constant.CommonConstant;
import com.pixease.common.enums.ResultCode;
import com.pixease.common.enums.UserRole;
import com.pixease.common.exception.BusinessException;
import com.pixease.common.utils.PasswordUtils;
import com.pixease.common.utils.TokenUtils;
import com.pixease.dto.AdminLoginDTO;
import com.pixease.dto.ResetPasswordDTO;
import com.pixease.dto.UserLoginDTO;
import com.pixease.dto.UserPageDTO;
import com.pixease.dto.UserRegisterDTO;
import com.pixease.dto.UserUpdateDTO;
import com.pixease.entity.PeUser;
import com.pixease.mapper.PeUserMapper;
import com.pixease.service.EmailService;
import com.pixease.service.PeUserService;
import com.pixease.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class PeUserServiceImpl extends ServiceImpl<PeUserMapper, PeUser> implements PeUserService {

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    /** Token签名密钥 */
    @Value("${pixease.token.secret}")
    private String tokenSecret;

    /** Token过期时间（分钟） */
    @Value("${pixease.token.expire-minutes}")
    private long tokenExpireMinutes;

    /** 账号前缀 */
    private static final String ACCOUNT_PREFIX = "pe";

    /** 账号随机数字位数 */
    private static final int ACCOUNT_DIGIT_COUNT = 6;

    /** 账号生成最大重试次数 */
    private static final int ACCOUNT_GENERATE_MAX_RETRY = 10;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public PeUserServiceImpl(StringRedisTemplate redisTemplate, EmailService emailService) {
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
    }

    /**
     * 生成系统账号，格式：pe + 6位随机数字（如 pe829374）
     * 若生成重复则重试，最多重试10次
     */
    private String generateAccount() {
        for (int i = 0; i < ACCOUNT_GENERATE_MAX_RETRY; i++) {
            StringBuilder sb = new StringBuilder(ACCOUNT_PREFIX);
            for (int j = 0; j < ACCOUNT_DIGIT_COUNT; j++) {
                sb.append(SECURE_RANDOM.nextInt(10));
            }
            String account = sb.toString();

            LambdaQueryWrapper<PeUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PeUser::getAccount, account);
            if (count(wrapper) == 0) {
                return account;
            }
            log.warn("账号生成碰撞，重试: account={}, 第{}次", account, i + 1);
        }
        throw new BusinessException("账号生成失败，请稍后再试");
    }

    /**
     * 用户注册
     * 1. 校验邮箱验证码
     * 2. 校验邮箱是否已被注册
     * 3. 系统自动生成账号（pe+6位随机数字）
     * 4. BCrypt加密密码后保存，默认角色为普通用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PeUser register(UserRegisterDTO dto) {
        boolean codeValid = emailService.verifyCode(dto.getEmail(), dto.getCode());
        if (!codeValid) {
            throw new BusinessException(ResultCode.CODE_ERROR);
        }

        LambdaQueryWrapper<PeUser> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(PeUser::getEmail, dto.getEmail());
        long emailCount = count(emailWrapper);
        if (emailCount > 0) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        PeUser user = new PeUser();
        user.setUserName(dto.getUserName());
        user.setAccount(generateAccount());
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole(UserRole.USER.getValue());

        save(user);

        user.setPassword(null);
        return user;
    }

    /**
     * 用户注册（含IP限制校验）
     * 同一IP每天最多注册5次
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PeUser register(UserRegisterDTO dto, String ip) {
        String ipDailyKey = CommonConstant.REDIS_REGISTER_IP_DAILY_PREFIX + ip;
        String ipCountStr = redisTemplate.opsForValue().get(ipDailyKey);
        int ipCount = ipCountStr != null ? Integer.parseInt(ipCountStr) : 0;
        if (ipCount >= CommonConstant.REGISTER_IP_DAILY_MAX) {
            throw new BusinessException(ResultCode.REGISTER_IP_LIMIT);
        }

        PeUser user = register(dto);

        if (ipCount == 0) {
            redisTemplate.opsForValue().set(ipDailyKey, "1", 1, TimeUnit.DAYS);
        } else {
            redisTemplate.opsForValue().increment(ipDailyKey);
        }

        return user;
    }

    /**
     * 用户登录
     * 1. 检查账号是否被锁定
     * 2. 根据账号查询用户
     * 3. BCrypt验证密码（失败则累计失败次数，达到上限锁定账号）
     * 4. 登录成功清除失败计数
     * 5. 生成JWT Token（含角色信息）并存入Redis
     */
    @Override
    public Map<String, Object> login(UserLoginDTO dto) {
        checkAccountLock(dto.getAccount());

        LambdaQueryWrapper<PeUser> wrapper = new LambdaQueryWrapper<>();
        if (dto.getAccount().contains("@")) {
            wrapper.eq(PeUser::getEmail, dto.getAccount());
        } else {
            wrapper.eq(PeUser::getAccount, dto.getAccount());
        }
        PeUser user = getOne(wrapper);

        if (user == null) {
            recordLoginFail(dto.getAccount());
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }

        if (!PasswordUtils.matches(dto.getPassword(), user.getPassword())) {
            recordLoginFail(dto.getAccount());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        clearLoginFail(dto.getAccount());

        String token = generateAndStoreToken(user);

        user.setPassword(null);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    /**
     * 管理员登录
     * 1. 检查账号是否被锁定
     * 2. 根据账号查询用户
     * 3. BCrypt验证密码（失败则累计失败次数）
     * 4. 校验用户是否为管理员角色
     * 5. 登录成功清除失败计数
     * 6. 生成JWT Token（含角色信息）并存入Redis
     */
    @Override
    public String adminLogin(AdminLoginDTO dto) {
        checkAccountLock(dto.getAccount());

        LambdaQueryWrapper<PeUser> wrapper = new LambdaQueryWrapper<>();
        if (dto.getAccount().contains("@")) {
            wrapper.eq(PeUser::getEmail, dto.getAccount());
        } else {
            wrapper.eq(PeUser::getAccount, dto.getAccount());
        }
        PeUser user = getOne(wrapper);

        if (user == null) {
            recordLoginFail(dto.getAccount());
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }

        if (!PasswordUtils.matches(dto.getPassword(), user.getPassword())) {
            recordLoginFail(dto.getAccount());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        if (!UserRole.isAdmin(user.getRole())) {
            throw new BusinessException(ResultCode.ROLE_FORBIDDEN);
        }

        clearLoginFail(dto.getAccount());

        return generateAndStoreToken(user);
    }

    /**
     * 检查账号是否被锁定
     */
    private void checkAccountLock(String account) {
        String lockKey = CommonConstant.REDIS_ACCOUNT_LOCK_PREFIX + account;
        String lockValue = redisTemplate.opsForValue().get(lockKey);
        if (lockValue != null) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }
    }

    /**
     * 记录登录失败次数，达到上限则锁定账号
     */
    private void recordLoginFail(String account) {
        String failKey = CommonConstant.REDIS_LOGIN_FAIL_PREFIX + account;
        String failCountStr = redisTemplate.opsForValue().get(failKey);
        int failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
        failCount++;

        if (failCount >= CommonConstant.LOGIN_FAIL_MAX) {
            String lockKey = CommonConstant.REDIS_ACCOUNT_LOCK_PREFIX + account;
            redisTemplate.opsForValue().set(lockKey, "1", CommonConstant.ACCOUNT_LOCK_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(failKey);
            log.warn("账号已被锁定: account={}, 连续失败{}次", account, failCount);
        } else {
            redisTemplate.opsForValue().set(failKey, String.valueOf(failCount), CommonConstant.ACCOUNT_LOCK_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * 登录成功，清除失败计数
     */
    private void clearLoginFail(String account) {
        String failKey = CommonConstant.REDIS_LOGIN_FAIL_PREFIX + account;
        redisTemplate.delete(failKey);
    }

    /**
     * 生成Token并存储到Redis
     */
    private String generateAndStoreToken(PeUser user) {
        long expireMillis = tokenExpireMinutes * 60 * 1000;
        String token = TokenUtils.generateToken(user.getId(), user.getAccount(), user.getRole(), tokenSecret, expireMillis);

        redisTemplate.opsForValue().set(
                CommonConstant.REDIS_TOKEN_PREFIX + user.getId(),
                token,
                tokenExpireMinutes,
                TimeUnit.MINUTES
        );

        return token;
    }

    /**
     * 更新用户信息
     * 1. 更新邮箱时校验邮箱唯一性（排除自身）
     * 2. 更新密码时重新BCrypt加密
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PeUser update(Long userId, UserUpdateDTO dto) {
        PeUser user = super.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<PeUser> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(PeUser::getEmail, dto.getEmail());
            emailWrapper.ne(PeUser::getId, userId);
            long emailCount = count(emailWrapper);
            if (emailCount > 0) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getUserName() != null) {
            user.setUserName(dto.getUserName());
        }

        if (dto.getPassword() != null) {
            user.setPassword(PasswordUtils.encode(dto.getPassword()));
        }

        updateById(user);

        user.setPassword(null);
        return user;
    }

    /**
     * 注销用户（逻辑删除）
     * 同时清除Redis中的Token
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId) {
        PeUser user = super.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }

        removeById(userId);

        redisTemplate.delete(CommonConstant.REDIS_TOKEN_PREFIX + userId);
    }

    /**
     * 找回密码
     * 1. 校验邮箱验证码
     * 2. 根据邮箱查询用户
     * 3. 重置密码（BCrypt加密）
     * 4. 清除该账号的登录锁定状态和失败计数
     * 5. 清除Redis中已有的Token（强制重新登录）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO dto) {
        boolean codeValid = emailService.verifyCode(dto.getEmail(), dto.getCode());
        if (!codeValid) {
            throw new BusinessException(ResultCode.CODE_ERROR);
        }

        LambdaQueryWrapper<PeUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PeUser::getEmail, dto.getEmail());
        PeUser user = getOne(wrapper);

        if (user == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }

        user.setPassword(PasswordUtils.encode(dto.getNewPassword()));
        updateById(user);

        String failKey = CommonConstant.REDIS_LOGIN_FAIL_PREFIX + user.getAccount();
        String lockKey = CommonConstant.REDIS_ACCOUNT_LOCK_PREFIX + user.getAccount();
        redisTemplate.delete(failKey);
        redisTemplate.delete(lockKey);

        redisTemplate.delete(CommonConstant.REDIS_TOKEN_PREFIX + user.getId());
    }

    /**
     * 根据ID查询用户信息，返回时脱敏密码字段
     */
    @Override
    public PeUser getById(Long userId) {
        PeUser user = super.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    /**
     * 分页查询用户列表
     * 支持按用户名称、账号、邮箱模糊查询
     * 返回UserVO不包含密码和删除标识字段
     */
    @Override
    public IPage<UserVO> page(UserPageDTO dto) {
        Page<PeUser> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<PeUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(dto.getUserName()), PeUser::getUserName, dto.getUserName());
        wrapper.like(StringUtils.isNotBlank(dto.getAccount()), PeUser::getAccount, dto.getAccount());
        wrapper.like(StringUtils.isNotBlank(dto.getEmail()), PeUser::getEmail, dto.getEmail());
        wrapper.orderByDesc(PeUser::getCreateTime);

        IPage<PeUser> userPage = super.page(page, wrapper);

        IPage<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }
}
