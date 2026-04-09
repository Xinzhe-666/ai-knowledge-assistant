package com.xinzhe.aiassistant.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 * 用于抛出业务逻辑中的错误，比如"用户名已存在""密码错误"
 */
@Getter
public class BusinessException extends RuntimeException {
    // 错误状态码
    private final Integer code;

    /**
     * 只传错误信息，默认状态码500
     */
    public BusinessException(String msg) {
        super(msg);
        this.code = 500;
    }

    /**
     * 自定义状态码+错误信息
     */
    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}