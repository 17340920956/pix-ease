package com.pixease.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求参数
 * 账号由系统自动生成（pe+6位随机数字），无需用户输入
 */
@Data
public class UserRegisterDTO {

    /** 用户名称 */
    @NotBlank(message = "用户名称不能为空")
    @Size(max = 64, message = "用户名称最长64个字符")
    private String userName;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度6-64个字符")
    private String password;

    /** 邮箱号 */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 邮箱验证码 */
    @NotBlank(message = "验证码不能为空")
    private String code;
}
