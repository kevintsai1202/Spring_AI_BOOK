package com.example.enterprise.exception;

/**
 * 資源未找到異常
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super(404, String.format("%s (ID: %s) 不存在", resource, id));
    }

    public ResourceNotFoundException(String message) {
        super(404, message);
    }
}
