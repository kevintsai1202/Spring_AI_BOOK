package com.example.enhancement.exception;

/**
 * RAG 向量增強基礎異常類
 *
 * 所有專案特定異常的父類
 */
public class EnhancementException extends RuntimeException {

    public EnhancementException(String message) {
        super(message);
    }

    public EnhancementException(String message, Throwable cause) {
        super(message, cause);
    }
}
