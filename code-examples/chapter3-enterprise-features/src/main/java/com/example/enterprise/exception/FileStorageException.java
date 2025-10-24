package com.example.enterprise.exception;

/**
 * 檔案儲存異常
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
public class FileStorageException extends BusinessException {

    public FileStorageException(String message) {
        super(500, message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(500, message);
        initCause(cause);
    }
}
