package com.pixease.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    private Result() {}

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 失败响应（指定状态码和信息）
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应（默认500状态码）
     */
    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
}
