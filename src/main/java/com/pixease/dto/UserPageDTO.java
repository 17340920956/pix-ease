package com.pixease.dto;

import lombok.Data;

/**
 * 用户分页查询请求参数
 */
@Data
public class UserPageDTO {

    /** 当前页码，默认1 */
    private Integer pageNum = 1;

    /** 每页条数，默认10 */
    private Integer pageSize = 10;

    /** 用户名称（模糊查询） */
    private String userName;

    /** 账号（模糊查询） */
    private String account;

    /** 邮箱（模糊查询） */
    private String email;
}
