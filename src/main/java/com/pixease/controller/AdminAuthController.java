package com.pixease.controller;

import com.pixease.common.result.Result;
import com.pixease.dto.AdminLoginDTO;
import com.pixease.service.PeUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员认证控制器，提供管理员登录接口
 * 与普通用户登录接口分离，路径前缀为/admin/auth
 */
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final PeUserService peUserService;

    public AdminAuthController(PeUserService peUserService) {
        this.peUserService = peUserService;
    }

    /**
     * 管理员登录，校验账号密码及管理员角色，返回JWT Token
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginDTO dto) {
        String token = peUserService.adminLogin(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }
}
