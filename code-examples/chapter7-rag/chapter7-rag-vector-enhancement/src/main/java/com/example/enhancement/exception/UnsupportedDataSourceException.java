package com.example.enhancement.exception;

/**
 * 不支援的資料來源類型異常
 *
 * 當使用不支援的資料來源類型時拋出
 */
public class UnsupportedDataSourceException extends DataSourceException {

    public UnsupportedDataSourceException(String message) {
        super(message);
    }

    public UnsupportedDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
