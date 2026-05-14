package com.pixease.common.exception;

import com.pixease.common.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常，用于在业务逻辑中主动抛出可预期的异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码 */
    private final int code;

    /**
     * 通过状态码和信息构造
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 通过ResultCode枚举构造
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 通过信息构造（默认500状态码）
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}
