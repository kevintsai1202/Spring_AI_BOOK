package com.example.enterprise.exception;

import com.example.enterprise.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 全域例外處理器
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 處理資料驗證例外
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("資料驗證失敗：{}", errors);

        return ApiResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("資料驗證失敗")
                .errors(errors)
                .build();
    }

    /**
     * 處理資源不存在例外
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {

        log.warn("資源不存在：{}", ex.getMessage());

        return ApiResponse.<Void>builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .build();
    }

    /**
     * 處理檔案儲存例外
     */
    @ExceptionHandler(FileStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleFileStorageException(
            FileStorageException ex,
            WebRequest request) {

        log.error("檔案儲存失敗：{}", ex.getMessage(), ex);

        return ApiResponse.<Void>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .build();
    }

    /**
     * 處理業務邏輯例外
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(
            BusinessException ex,
            WebRequest request) {

        log.warn("業務邏輯例外：{}", ex.getMessage());

        return ApiResponse.<Void>builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();
    }

    /**
     * 處理其他未預期的例外
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGlobalException(
            Exception ex,
            WebRequest request) {

        log.error("未預期的例外：{}", ex.getMessage(), ex);

        return ApiResponse.<Void>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("系統錯誤，請稍後再試")
                .build();
    }
}
