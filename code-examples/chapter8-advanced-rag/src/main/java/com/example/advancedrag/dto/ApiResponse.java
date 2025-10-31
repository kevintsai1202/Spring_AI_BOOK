package com.example.advancedrag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * API 通用響應包裝器
 *
 * 用於統一 API 響應格式
 *
 * @param <T> 響應數據類型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 狀態碼（200: 成功, 400: 客戶端錯誤, 500: 服務器錯誤）
     */
    private Integer code;

    /**
     * 響應消息
     */
    private String message;

    /**
     * 響應數據
     */
    private T data;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 時間戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 請求追蹤 ID
     */
    private String traceId;

    /**
     * 額外的元數據
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 創建成功響應（無數據）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建成功響應（帶數據）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建成功響應（帶消息和數據）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建失敗響應
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .code(500)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建失敗響應（帶錯誤碼）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建失敗響應（帶錯誤碼和數據）
     */
    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建客戶端錯誤響應（400）
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
                .code(400)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建未授權錯誤響應（401）
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .code(401)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建禁止訪問錯誤響應（403）
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
                .code(403)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建未找到錯誤響應（404）
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .code(404)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建內容審核失敗響應（403）
     */
    public static <T> ApiResponse<T> moderationFailed(String message) {
        return ApiResponse.<T>builder()
                .code(403)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建內容審核失敗響應（帶數據）
     */
    public static <T> ApiResponse<T> moderationFailed(String message, T data) {
        return ApiResponse.<T>builder()
                .code(403)
                .message(message)
                .data(data)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 設置追蹤 ID
     */
    public ApiResponse<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 添加元數據
     */
    public ApiResponse<T> withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }

    /**
     * 添加多個元數據
     */
    public ApiResponse<T> withMetadata(Map<String, Object> metadata) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.putAll(metadata);
        return this;
    }
}
