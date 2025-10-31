package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 連接測試結果
 * 用於表示資料源連接測試的結果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestResult {
    /**
     * 是否成功
     */
    private boolean successful;

    /**
     * 測試時間
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 響應時間（毫秒）
     */
    private long responseTimeMs;

    /**
     * 錯誤訊息
     */
    private String errorMessage;

    /**
     * 額外資訊
     */
    private String additionalInfo;

    /**
     * 創建成功的測試結果
     */
    public static ConnectionTestResult success(long responseTimeMs) {
        return ConnectionTestResult.builder()
                .successful(true)
                .responseTimeMs(responseTimeMs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 創建失敗的測試結果
     */
    public static ConnectionTestResult failure(String errorMessage) {
        return ConnectionTestResult.builder()
                .successful(false)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
