package com.example.memory.advanced.service;

import com.example.memory.advanced.model.ConversationSummary;
import com.example.memory.advanced.model.TodoItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 對話摘要服務
 *
 * 提供對話歷史的智能摘要功能:
 * 1. 摘要對話歷史
 * 2. 提取對話主題
 * 3. 提取待辦事項
 */
@Service
public class ConversationSummaryService {

    private static final Logger log = LoggerFactory.getLogger(ConversationSummaryService.class);

    private final ChatMemory chatMemory;
    private final ChatClient summarizerClient;

    public ConversationSummaryService(
        @Qualifier("shortTermMemory") ChatMemory chatMemory,
        @Qualifier("summarizerClient") ChatClient summarizerClient
    ) {
        this.chatMemory = chatMemory;
        this.summarizerClient = summarizerClient;
    }

    /**
     * 摘要對話歷史
     */
    public ConversationSummary summarize(String conversationId) {
        log.info("開始摘要對話: {}", conversationId);

        // 獲取對話歷史
        List<Message> history = chatMemory.get(conversationId);

        if (history.isEmpty()) {
            log.warn("對話 {} 無歷史記錄", conversationId);
            return ConversationSummary.empty(conversationId);
        }

        // 生成摘要
        String summaryText = generateSummary(history);

        // 提取主題、決策和待辦事項
        List<String> topics = extractTopics(summaryText);
        List<String> decisions = extractDecisions(summaryText);
        List<TodoItem> todos = extractTodos(summaryText);

        return new ConversationSummary(
            conversationId,
            summaryText,
            topics,
            decisions,
            todos,
            history.size(),
            LocalDateTime.now()
        );
    }

    /**
     * 生成對話摘要
     */
    private String generateSummary(List<Message> messages) {
        StringBuilder conversation = new StringBuilder();
        for (Message msg : messages) {
            conversation.append(msg.getMessageType())
                .append(": ")
                .append(msg.getText())
                .append("\n");
        }

        String prompt = String.format("""
            請為以下對話提供簡潔的摘要:

            %s

            摘要應包含:
            1. 主要討論話題
            2. 重要決定或結論
            3. 待辦事項或後續行動
            """, conversation);

        return summarizerClient.prompt()
            .user(prompt)
            .call()
            .content();
    }

    /**
     * 從摘要中提取主題
     */
    private List<String> extractTopics(String summary) {
        // 簡化實作:從摘要中提取關鍵詞
        return List.of("主題提取功能待實作");
    }

    /**
     * 從摘要中提取決策
     */
    private List<String> extractDecisions(String summary) {
        // 簡化實作
        return List.of("決策提取功能待實作");
    }

    /**
     * 從摘要中提取待辦事項
     */
    private List<TodoItem> extractTodos(String summary) {
        // 簡化實作
        return List.of();
    }
}
