package com.example.memory.advanced.dto;

import com.example.memory.advanced.model.MemoryStrategy;

/**
 * 聊天請求 DTO
 *
 * 用於接收客戶端的聊天請求
 */
public record ChatRequest(
    /**
     * 對話唯一識別碼
     */
    String conversationId,

    /**
     * 用戶訊息內容
     */
    String message,

    /**
     * 記憶策略 (可選,預設為 HYBRID)
     */
    MemoryStrategy strategy
) {
    /**
     * 建構函數,設定預設策略
     */
    public ChatRequest {
        if (strategy == null) {
            strategy = MemoryStrategy.HYBRID;
        }
    }

    /**
     * 建立簡單的聊天請求 (使用預設策略)
     */
    public static ChatRequest of(String conversationId, String message) {
        return new ChatRequest(conversationId, message, null);
    }
}
