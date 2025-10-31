package com.example.enhancement.exception;

/**
 * 資料來源未找到異常
 *
 * 當請求的資料來源不存在時拋出
 */
public class DataSourceNotFoundException extends DataSourceException {

    public DataSourceNotFoundException(String message) {
        super(message);
    }

    public DataSourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
