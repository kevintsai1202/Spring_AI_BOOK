/**
 * 對話歷史 DTO
 * 用於返回對話歷史資料
 */
package com.example.dto;

import lombok.Data;
import lombok.Builder;
import org.springframework.ai.chat.messages.Message;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ConversationHistory {
    private String conversationId;
    private List<Message> messages;
    private int totalCount;
    private LocalDateTime lastUpdated;
}