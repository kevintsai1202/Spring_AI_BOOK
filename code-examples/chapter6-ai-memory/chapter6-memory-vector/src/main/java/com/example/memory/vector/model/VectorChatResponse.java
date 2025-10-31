package com.example.memory.vector.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 向量聊天響應
 */
@Data
@Builder
public class VectorChatResponse {
    /**
     * 對話ID
     */
    private String conversationId;

    /**
     * AI 回應內容
     */
    private String response;

    /**
     * 使用的策略
     */
    private String strategy;

    /**
     * 短期記憶數量
     */
    private Integer shortTermCount;

    /**
     * 長期記憶數量
     */
    private Integer longTermCount;

    /**
     * 時間戳
     */
    private LocalDateTime timestamp;
}
