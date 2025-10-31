package com.example.memory.advanced.dto;

import com.example.memory.advanced.model.ConversationSummary;
import com.example.memory.advanced.model.TodoItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 對話摘要回應 DTO
 *
 * 將 ConversationSummary 轉換為 API 回應格式
 */
public record SummaryResponse(
    String conversationId,
    String summary,
    List<String> mainTopics,
    List<String> keyDecisions,
    List<TodoItemDto> actionItems,
    int messageCount,
    LocalDateTime createdAt
) {
    /**
     * 待辦事項 DTO
     */
    public record TodoItemDto(
        String description,
        String priority,
        LocalDateTime dueDate,
        boolean completed
    ) {
        /**
         * 從 TodoItem 轉換
         */
        public static TodoItemDto from(TodoItem item) {
            return new TodoItemDto(
                item.description(),
                item.priority().name(),
                item.dueDate(),
                item.completed()
            );
        }
    }

    /**
     * 從 ConversationSummary 轉換
     */
    public static SummaryResponse from(ConversationSummary summary) {
        return new SummaryResponse(
            summary.conversationId(),
            summary.summary(),
            summary.mainTopics(),
            summary.keyDecisions(),
            summary.actionItems().stream()
                .map(TodoItemDto::from)
                .toList(),
            summary.messageCount(),
            summary.createdAt()
        );
    }
}
