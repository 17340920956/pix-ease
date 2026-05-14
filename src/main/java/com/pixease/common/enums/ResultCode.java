package com.pixease.common.enums;

import lombok.Getter;

/**
 * 业务结果码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    /** 签名相关错误码 */
    SIGN_ERROR(1001, "签名验证失败"),
    SIGN_EXPIRED(1002, "签名已过期"),

    /** Token相关错误码 */
    TOKEN_INVALID(1003, "Token无效"),
    TOKEN_EXPIRED(1004, "Token已过期"),

    /** 权限相关错误码 */
    ROLE_FORBIDDEN(1101, "权限不足，仅管理员可操作"),

    /** 用户业务错误码 */
    ACCOUNT_EXISTS(2001, "账号已存在"),
    EMAIL_EXISTS(2002, "邮箱已被注册"),
    ACCOUNT_NOT_FOUND(2003, "账号不存在"),
    PASSWORD_ERROR(2004, "密码错误"),
    ACCOUNT_LOCKED(2005, "账号已被锁定，请30分钟后再试"),

    /** 验证码相关错误码 */
    CODE_EXPIRED(2101, "验证码已过期"),
    CODE_ERROR(2102, "验证码错误"),
    CODE_SEND_TOO_FREQUENT(2103, "验证码发送过于频繁，请稍后再试"),
    CODE_DAILY_LIMIT(2104, "验证码每日发送次数已达上限"),
    CODE_FAIL_TOO_MANY(2105, "验证码错误次数过多，请重新获取"),

    /** 注册限流错误码 */
    REGISTER_IP_LIMIT(2201, "该IP今日注册次数已达上限"),

    /** 限流错误码 */
    RATE_LIMIT_EXCEEDED(3001, "请求过于频繁，请稍后再试");

    /** 状态码 */
    private final int code;

    /** 提示信息 */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
