package com.example.memory.vector.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 記憶分析結果 DTO
 */
@Data
@Builder
public class MemoryAnalytics {
    /**
     * 對話 ID
     */
    private String conversationId;

    /**
     * 短期記憶數量
     */
    private int shortTermCount;

    /**
     * 長期記憶數量 (估算)
     */
    private int longTermCount;

    /**
     * 總訊息字符數
     */
    private long totalCharacterCount;

    /**
     * 平均相關性分數
     */
    private double averageRelevanceScore;

    /**
     * 最後同步時間
     */
    private LocalDateTime lastSyncTime;

    /**
     * 對話創建時間
     */
    private LocalDateTime createdAt;

    /**
     * 最後活動時間
     */
    private LocalDateTime lastActivityTime;

    /**
     * 同步狀態 (SYNCED, PENDING, NEVER)
     */
    private String syncStatus;

    /**
     * 記憶健康分數 (0-100)
     */
    private int healthScore;
}
