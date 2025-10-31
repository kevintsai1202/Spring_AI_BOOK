package com.example.advancedrag.exception;

/**
 * Advanced RAG 系統異常
 *
 * RAG 查詢處理過程中的通用異常
 */
public class AdvancedRAGException extends RuntimeException {

    private String errorCode;

    public AdvancedRAGException(String message) {
        super(message);
    }

    public AdvancedRAGException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdvancedRAGException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AdvancedRAGException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
