package com.example.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 統一錯誤回應格式
 * 用於 API 異常處理的標準化回應
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /** HTTP 狀態碼 */
    private int status;

    /** 錯誤類型 */
    private String error;

    /** 錯誤訊息 */
    private String message;

    /** 錯誤詳細資訊 */
    private Map<String, String> details;

    /** 時間戳記 */
    private Instant timestamp;

    /** 請求路徑 */
    private String path;
}
