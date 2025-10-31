package com.example.enhancement.exception;

/**
 * 資料來源連接異常
 *
 * 當無法連接到資料來源時拋出
 */
public class DataSourceConnectionException extends DataSourceException {

    public DataSourceConnectionException(String message) {
        super(message);
    }

    public DataSourceConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
