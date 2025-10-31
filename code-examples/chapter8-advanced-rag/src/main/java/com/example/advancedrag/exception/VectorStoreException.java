package com.example.advancedrag.exception;

/**
 * 向量存儲異常
 *
 * 向量數據庫操作過程中的異常
 */
public class VectorStoreException extends RuntimeException {

    private String errorCode;
    private String operation;

    public VectorStoreException(String message) {
        super(message);
    }

    public VectorStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public VectorStoreException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public VectorStoreException(String errorCode, String message, String operation) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getOperation() {
        return operation;
    }
}
