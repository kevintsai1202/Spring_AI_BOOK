package com.example.memory.advanced.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 對話摘要資料模型
 *
 * 記錄對話的摘要資訊,包括:
 * - 摘要內容
 * - 主要主題
 * - 關鍵決策
 * - 待辦事項
 */
public record ConversationSummary(
    /**
     * 對話唯一識別碼
     */
    String conversationId,

    /**
     * 對話摘要內容
     */
    String summary,

    /**
     * 主要討論主題列表
     */
    List<String> mainTopics,

    /**
     * 關鍵決策或結論列表
     */
    List<String> keyDecisions,

    /**
     * 待辦事項或後續行動列表
     */
    List<TodoItem> actionItems,

    /**
     * 對話訊息總數
     */
    int messageCount,

    /**
     * 摘要建立時間
     */
    LocalDateTime createdAt
) {
    /**
     * 建立預設的對話摘要
     */
    public static ConversationSummary empty(String conversationId) {
        return new ConversationSummary(
            conversationId,
            "",
            List.of(),
            List.of(),
            List.of(),
            0,
            LocalDateTime.now()
        );
    }
}
