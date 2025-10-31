package com.example.memory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 對話歷史DTO
 *
 * 表示一個完整的對話會話及其所有消息記錄
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationHistory {

    /**
     * 對話ID
     */
    private String conversationId;

    /**
     * 用戶ID
     */
    private String userId;

    /**
     * 對話創建時間
     */
    private LocalDateTime createdAt;

    /**
     * 最後活動時間
     */
    private LocalDateTime lastActivityAt;

    /**
     * 消息列表
     */
    @Builder.Default
    private List<MessageDto> messages = new ArrayList<>();

    /**
     * 對話摘要（可選）
     */
    private String summary;

    /**
     * 消息內嵌類
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageDto {
        /**
         * 消息角色（user/assistant）
         */
        private String role;

        /**
         * 消息內容
         */
        private String content;

        /**
         * 消息發送時間
         */
        private LocalDateTime timestamp;

        /**
         * 消息ID
         */
        private String messageId;

        /**
         * 是否是工具調用
         */
        private boolean isToolCall;

        /**
         * 工具名稱（如果是工具調用）
         */
        private String toolName;
    }

    /**
     * 添加用戶消息
     */
    public void addUserMessage(String content) {
        messages.add(MessageDto.builder()
            .role("user")
            .content(content)
            .timestamp(LocalDateTime.now())
            .isToolCall(false)
            .build());
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * 添加助手消息
     */
    public void addAssistantMessage(String content) {
        messages.add(MessageDto.builder()
            .role("assistant")
            .content(content)
            .timestamp(LocalDateTime.now())
            .isToolCall(false)
            .build());
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * 獲取最近N條消息
     */
    public List<MessageDto> getRecentMessages(int limit) {
        int startIndex = Math.max(0, messages.size() - limit);
        return messages.subList(startIndex, messages.size());
    }
}
