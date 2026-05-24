package com.pixease.common.constant;

/**
 * 公共常量定义
 */
public class CommonConstant {

    private CommonConstant() {}

    /** Token请求头名称 */
    public static final String TOKEN_HEADER = "Authorization";

    /** Token前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 签名请求头名称 */
    public static final String SIGN_HEADER = "X-Sign";

    /** 时间戳请求头名称 */
    public static final String TIMESTAMP_HEADER = "X-Timestamp";

    /** 随机字符串请求头名称 */
    public static final String NONCE_HEADER = "X-Nonce";

    /** Redis中Token存储前缀 */
    public static final String REDIS_TOKEN_PREFIX = "pixease:token:";

    /** Redis中IP限流计数前缀 */
    public static final String REDIS_RATE_LIMIT_PREFIX = "pixease:rate_limit:";

    /** Redis中Nonce防重放前缀 */
    public static final String REDIS_NONCE_PREFIX = "pixease:nonce:";

    /** Redis中邮箱验证码存储前缀 */
    public static final String REDIS_EMAIL_CODE_PREFIX = "pixease:email_code:";

    /** Redis中邮箱发送间隔限制前缀 */
    public static final String REDIS_EMAIL_INTERVAL_PREFIX = "pixease:email_interval:";

    /** Redis中登录失败次数计数前缀 */
    public static final String REDIS_LOGIN_FAIL_PREFIX = "pixease:login_fail:";

    /** Redis中账号锁定前缀 */
    public static final String REDIS_ACCOUNT_LOCK_PREFIX = "pixease:account_lock:";

    /** Redis中邮箱每日发送次数计数前缀 */
    public static final String REDIS_EMAIL_DAILY_PREFIX = "pixease:email_daily:";

    /** Redis中注册IP每日次数计数前缀 */
    public static final String REDIS_REGISTER_IP_DAILY_PREFIX = "pixease:register_ip_daily:";

    /** Redis中验证码校验失败次数计数前缀 */
    public static final String REDIS_CODE_FAIL_PREFIX = "pixease:code_fail:";

    /** Redis中注册防重锁前缀 */
    public static final String REDIS_REGISTER_LOCK_PREFIX = "pixease:lock:register:";

    /** Redis中更新防重锁前缀 */
    public static final String REDIS_UPDATE_LOCK_PREFIX = "pixease:lock:update:";

    /** 登录失败最大次数 */
    public static final int LOGIN_FAIL_MAX = 5;

    /** 账号锁定时间（分钟） */
    public static final int ACCOUNT_LOCK_MINUTES = 30;

    /** 邮箱每日发送验证码最大次数 */
    public static final int EMAIL_DAILY_MAX = 10;

    /** 同一IP每日注册最大次数 */
    public static final int REGISTER_IP_DAILY_MAX = 5;

    /** 验证码校验失败最大次数 */
    public static final int CODE_FAIL_MAX = 5;
}
