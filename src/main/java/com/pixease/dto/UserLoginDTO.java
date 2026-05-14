package com.pixease.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求参数
 */
@Data
public class UserLoginDTO {

    /** 账号 */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
