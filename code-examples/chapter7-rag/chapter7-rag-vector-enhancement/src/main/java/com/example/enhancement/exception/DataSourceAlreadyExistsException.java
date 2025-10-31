package com.example.enhancement.exception;

/**
 * 資料來源已存在異常
 *
 * 當嘗試註冊已存在的資料來源時拋出
 */
public class DataSourceAlreadyExistsException extends DataSourceException {

    public DataSourceAlreadyExistsException(String message) {
        super(message);
    }

    public DataSourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
