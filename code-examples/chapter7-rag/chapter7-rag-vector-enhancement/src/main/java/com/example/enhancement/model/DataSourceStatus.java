package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 資料源狀態
 * 用於監控資料源的健康狀態和連接資訊
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceStatus {
    /**
     * 資料源名稱
     */
    private String name;

    /**
     * 資料源類型
     */
    private DataSourceType type;

    /**
     * 是否健康
     */
    private boolean healthy;

    /**
     * 最後檢查時間
     */
    @Builder.Default
    private LocalDateTime lastChecked = LocalDateTime.now();

    /**
     * 活躍連接數
     */
    private int connectionCount;

    /**
     * 總請求數
     */
    private long totalRequests;

    /**
     * 失敗請求數
     */
    private long failedRequests;

    /**
     * 平均響應時間（毫秒）
     */
    private double averageResponseTimeMs;

    /**
     * 最後錯誤訊息
     */
    private String lastError;

    /**
     * 上線時間
     */
    private LocalDateTime upSince;

    /**
     * 計算成功率
     */
    public double getSuccessRate() {
        if (totalRequests == 0) {
            return 100.0;
        }
        return ((double) (totalRequests - failedRequests) / totalRequests) * 100.0;
    }
}
