package com.example.enhancement.exception;

/**
 * 資料來源相關異常基礎類
 */
public class DataSourceException extends EnhancementException {

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
