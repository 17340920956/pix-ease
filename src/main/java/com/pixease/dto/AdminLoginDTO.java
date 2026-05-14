package com.pixease.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录请求参数
 */
@Data
public class AdminLoginDTO {

    /** 账号 */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
