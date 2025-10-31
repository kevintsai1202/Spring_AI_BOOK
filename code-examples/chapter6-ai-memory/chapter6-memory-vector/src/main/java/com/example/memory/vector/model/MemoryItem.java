package com.example.memory.vector.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 記憶項目模型
 *
 * 表示一個統一的記憶單元,可以來自短期或長期記憶
 */
@Data
@Builder
public class MemoryItem {
    /**
     * 內容文本
     */
    private String content;

    /**
     * 記憶類型 (SHORT_TERM / LONG_TERM)
     */
    private MemoryType type;

    /**
     * 相關性分數 (0-1)
     */
    private double relevanceScore;

    /**
     * 時間戳
     */
    private LocalDateTime timestamp;

    /**
     * 元數據
     */
    private Map<String, Object> metadata;

    /**
     * 對話 ID
     */
    private String conversationId;

    /**
     * 訊息類型 (USER, ASSISTANT, SYSTEM)
     */
    private String messageType;
}
