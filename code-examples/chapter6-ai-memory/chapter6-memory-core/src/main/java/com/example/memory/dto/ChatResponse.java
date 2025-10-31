package com.example.memory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 對話回應DTO
 *
 * 封裝AI模型的回應信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    /**
     * 對話ID
     */
    private String conversationId;

    /**
     * AI的回應消息
     */
    private String message;

    /**
     * 回應生成的時間戳
     */
    private LocalDateTime timestamp;

    /**
     * 是否調用了工具
     */
    private boolean toolsUsed;

    /**
     * 調用的工具名稱列表
     */
    private String[] toolsUsedNames;

    /**
     * 回應的TOKEN使用量
     */
    private long tokenUsed;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 錯誤信息（如果有）
     */
    private String error;

    /**
     * 構造成功的回應
     */
    public static ChatResponse success(String conversationId, String message) {
        return ChatResponse.builder()
            .conversationId(conversationId)
            .message(message)
            .timestamp(LocalDateTime.now())
            .success(true)
            .build();
    }

    /**
     * 構造失敗的回應
     */
    public static ChatResponse failure(String conversationId, String error) {
        return ChatResponse.builder()
            .conversationId(conversationId)
            .error(error)
            .timestamp(LocalDateTime.now())
            .success(false)
            .build();
    }
}
