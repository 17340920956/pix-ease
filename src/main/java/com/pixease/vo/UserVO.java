package com.pixease.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象，用于返回给前端的数据（不包含密码和删除标识）
 */
@Data
public class UserVO {

    /** 自增ID */
    private Long id;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 用户名称 */
    private String userName;

    /** 账号 */
    private String account;

    /** 邮箱号 */
    private String email;

    /** 角色标识 0-普通用户 1-管理员 */
    private Integer role;
}
