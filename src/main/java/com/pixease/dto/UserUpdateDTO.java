package com.pixease.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 用户更新请求参数（所有字段可选）
 */
@Data
public class UserUpdateDTO {

    /** 用户名称 */
    @Size(max = 64, message = "用户名称最长64个字符")
    private String userName;

    /** 密码 */
    @Size(min = 6, max = 64, message = "密码长度6-64个字符")
    private String password;

    /** 邮箱号 */
    @Email(message = "邮箱格式不正确")
    private String email;
}
