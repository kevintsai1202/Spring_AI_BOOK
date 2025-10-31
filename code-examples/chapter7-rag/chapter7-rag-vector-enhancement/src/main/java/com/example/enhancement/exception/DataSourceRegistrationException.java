package com.example.enhancement.exception;

/**
 * 資料來源註冊異常
 *
 * 當資料來源註冊失敗時拋出
 */
public class DataSourceRegistrationException extends DataSourceException {

    public DataSourceRegistrationException(String message) {
        super(message);
    }

    public DataSourceRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
