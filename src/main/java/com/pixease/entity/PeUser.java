package com.pixease.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类，对应pe_user表
 */
@Data
@TableName("pe_user")
public class PeUser {

    /** 自增ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标识 0-未删除 1-已删除 */
    @TableLogic
    private Integer isDel;

    /** 用户名称 */
    private String userName;

    /** 账号 */
    private String account;

    /** 密码（BCrypt加密存储） */
    private String password;

    /** 邮箱号 */
    private String email;

    /** 角色标识 0-普通用户 1-管理员 */
    private Integer role;
}
