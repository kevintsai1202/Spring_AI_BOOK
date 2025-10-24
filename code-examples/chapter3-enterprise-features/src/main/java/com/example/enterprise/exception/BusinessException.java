package com.example.enterprise.exception;

import lombok.Getter;

/**
 * 業務異常基類
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }
}
