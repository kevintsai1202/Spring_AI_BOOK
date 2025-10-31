package com.example.memory.advanced.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 記憶分析資料模型
 *
 * 提供對話記憶的統計分析資訊:
 * - 訊息數量統計
 * - 平均訊息長度
 * - 對話時間範圍
 * - 對話總時長
 */
public record MemoryAnalytics(
    /**
     * 對話唯一識別碼
     */
    String conversationId,

    /**
     * 訊息總數
     */
    int totalMessages,

    /**
     * 用戶訊息數量
     */
    int userMessages,

    /**
     * 助手訊息數量
     */
    int assistantMessages,

    /**
     * 平均訊息長度
     */
    double avgMessageLength,

    /**
     * 首次對話時間
     */
    LocalDateTime firstMessageTime,

    /**
     * 最後對話時間
     */
    LocalDateTime lastMessageTime,

    /**
     * 對話總時長
     */
    Duration totalDuration
) {
    /**
     * 建立空的分析結果
     */
    public static MemoryAnalytics empty(String conversationId) {
        return new MemoryAnalytics(
            conversationId,
            0,
            0,
            0,
            0.0,
            null,
            null,
            Duration.ZERO
        );
    }

    /**
     * 計算對話持續時間 (秒)
     */
    public long getDurationInSeconds() {
        return totalDuration != null ? totalDuration.getSeconds() : 0;
    }

    /**
     * 計算對話持續時間 (分鐘)
     */
    public long getDurationInMinutes() {
        return totalDuration != null ? totalDuration.toMinutes() : 0;
    }

    /**
     * 檢查是否為活躍對話
     */
    public boolean isActive() {
        return totalMessages > 0 && lastMessageTime != null;
    }
}
