package com.example.memory.advanced.dto;

import com.example.memory.advanced.model.MemoryStrategy;

/**
 * 聊天回應 DTO
 *
 * 用於回傳給客戶端的聊天回應
 */
public record ChatResponse(
    /**
     * 對話唯一識別碼
     */
    String conversationId,

    /**
     * AI 回應內容
     */
    String message,

    /**
     * 使用的記憶策略
     */
    MemoryStrategy usedStrategy,

    /**
     * 當前對話的訊息總數
     */
    int messageCount
) {
    /**
     * 建立簡單的聊天回應
     */
    public static ChatResponse of(String conversationId, String message) {
        return new ChatResponse(conversationId, message, MemoryStrategy.HYBRID, 0);
    }

    /**
     * 建立帶策略的聊天回應
     */
    public static ChatResponse of(String conversationId, String message, MemoryStrategy strategy) {
        return new ChatResponse(conversationId, message, strategy, 0);
    }
}
