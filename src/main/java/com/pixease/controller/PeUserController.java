package com.pixease.controller;

import com.pixease.common.result.Result;
import com.pixease.common.utils.IpUtils;
import com.pixease.dto.EmailCodeDTO;
import com.pixease.dto.ResetPasswordDTO;
import com.pixease.dto.UserLoginDTO;
import com.pixease.dto.UserRegisterDTO;
import com.pixease.dto.UserUpdateDTO;
import com.pixease.entity.PeUser;
import com.pixease.service.EmailService;
import com.pixease.service.PeUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器，提供用户注册、登录、更新、注销和查询接口
 */
@RestController
@RequestMapping("/user")
public class PeUserController {

    private final PeUserService peUserService;
    private final EmailService emailService;

    public PeUserController(PeUserService peUserService, EmailService emailService) {
        this.peUserService = peUserService;
        this.emailService = emailService;
    }

    /**
     * 发送邮箱验证码（注册前调用）
     * 受IP限流和签名验证保护，同一邮箱60秒间隔、每天最多10次
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody EmailCodeDTO dto) {
        emailService.sendVerifyCode(dto.getEmail());
        return Result.success();
    }

    /**
     * 用户注册（需先获取邮箱验证码）
     * 受IP限流和签名验证保护，同一IP每天最多注册5次
     */
    @PostMapping("/register")
    public Result<PeUser> register(@Valid @RequestBody UserRegisterDTO dto,
                                   HttpServletRequest request) {
        String ip = IpUtils.getIpAddr(request);
        PeUser user = peUserService.register(dto, ip);
        return Result.success(user);
    }

    /**
     * 用户登录，返回JWT Token
     * 受IP限流和签名验证保护，连续5次密码错误锁定账号30分钟
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody UserLoginDTO dto) {
        Map<String, Object> result = peUserService.login(dto);
        return Result.success(result);
    }

    /**
     * 找回密码，通过邮箱验证码重置密码
     * 无需Token鉴权，受IP限流和签名验证保护
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        peUserService.resetPassword(dto);
        return Result.success();
    }

    /**
     * 更新用户信息（需Token鉴权）
     */
    @PutMapping("/update")
    public Result<PeUser> update(@RequestAttribute("userId") Long userId,
                                 @Valid @RequestBody UserUpdateDTO dto) {
        PeUser user = peUserService.update(userId, dto);
        return Result.success(user);
    }

    /**
     * 注销用户，逻辑删除（需Token鉴权）
     */
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestAttribute("userId") Long userId) {
        peUserService.delete(userId);
        return Result.success();
    }

    /**
     * 查询当前用户信息（需Token鉴权）
     */
    @GetMapping("/info")
    public Result<PeUser> info(@RequestAttribute("userId") Long userId) {
        PeUser user = peUserService.getById(userId);
        return Result.success(user);
    }
}
