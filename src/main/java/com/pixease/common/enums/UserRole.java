package com.pixease.common.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {

    /** 普通用户 */
    USER(0, "普通用户"),

    /** 管理员 */
    ADMIN(1, "管理员");

    /** 角色值 */
    private final int value;

    /** 角色描述 */
    private final String description;

    UserRole(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 判断是否为管理员
     */
    public static boolean isAdmin(Integer roleValue) {
        return roleValue != null && roleValue == ADMIN.value;
    }
}
