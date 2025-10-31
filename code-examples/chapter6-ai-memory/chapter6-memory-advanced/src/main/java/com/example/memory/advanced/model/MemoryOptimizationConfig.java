package com.example.memory.advanced.model;

import java.time.Duration;

/**
 * 記憶優化配置資料模型
 *
 * 定義記憶管理和優化的配置參數:
 * - 訊息數量限制
 * - 摘要觸發條件
 * - 自動清理設定
 */
public record MemoryOptimizationConfig(
    /**
     * 單一對話的最大訊息數
     */
    int maxMessages,

    /**
     * 觸發自動摘要的訊息數閾值
     */
    int summaryTriggerThreshold,

    /**
     * 摘要後保留的最近訊息數
     */
    int keepRecentMessages,

    /**
     * 是否啟用自動清理
     */
    boolean enableAutoCleanup,

    /**
     * 記憶保留期限
     */
    Duration retentionPeriod
) {
    /**
     * 建立預設配置
     */
    public static MemoryOptimizationConfig defaultConfig() {
        return new MemoryOptimizationConfig(
            100,                      // 最大 100 條訊息
            50,                       // 超過 50 條觸發摘要
            20,                       // 保留最近 20 條
            true,                     // 啟用自動清理
            Duration.ofDays(30)       // 保留 30 天
        );
    }

    /**
     * 建立寬鬆配置 (較多記憶保留)
     */
    public static MemoryOptimizationConfig relaxedConfig() {
        return new MemoryOptimizationConfig(
            200,                      // 最大 200 條訊息
            100,                      // 超過 100 條觸發摘要
            50,                       // 保留最近 50 條
            true,                     // 啟用自動清理
            Duration.ofDays(90)       // 保留 90 天
        );
    }

    /**
     * 建立嚴格配置 (較少記憶保留)
     */
    public static MemoryOptimizationConfig strictConfig() {
        return new MemoryOptimizationConfig(
            50,                       // 最大 50 條訊息
            30,                       // 超過 30 條觸發摘要
            10,                       // 保留最近 10 條
            true,                     // 啟用自動清理
            Duration.ofDays(7)        // 保留 7 天
        );
    }

    /**
     * 檢查是否需要觸發摘要
     */
    public boolean shouldTriggerSummary(int currentMessageCount) {
        return currentMessageCount >= summaryTriggerThreshold;
    }

    /**
     * 檢查是否需要清理舊訊息
     */
    public boolean shouldCleanup(int currentMessageCount) {
        return enableAutoCleanup && currentMessageCount > maxMessages;
    }
}
