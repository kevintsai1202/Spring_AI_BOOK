package com.example.memory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * 對話請求DTO
 *
 * 封裝用戶的對話請求信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 對話ID，用於識別不同的會話
     */
    private String conversationId;

    /**
     * 用戶的輸入消息
     */
    private String message;

    /**
     * 用戶ID，用於多租戶隔離
     */
    private String userId;

    /**
     * 是否需要工具調用
     */
    private boolean enableTools = true;

    /**
     * 創建新對話請求，自動生成conversationId
     */
    public static ChatRequest newConversation(String message, String userId) {
        return new ChatRequest(
            UUID.randomUUID().toString(),
            message,
            userId,
            true
        );
    }
}
