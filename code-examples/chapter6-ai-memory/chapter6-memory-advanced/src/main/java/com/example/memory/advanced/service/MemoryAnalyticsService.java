package com.example.memory.advanced.service;

import com.example.memory.advanced.model.MemoryAnalytics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 記憶分析服務
 *
 * 提供對話記憶的統計分析功能:
 * 1. 訊息數量統計
 * 2. 對話時長分析
 * 3. 平均訊息長度
 */
@Service
public class MemoryAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(MemoryAnalyticsService.class);

    private final ChatMemory chatMemory;

    public MemoryAnalyticsService(@Qualifier("shortTermMemory") ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    /**
     * 分析指定對話的記憶
     *
     * @param conversationId 對話 ID
     * @return 記憶分析結果
     */
    public MemoryAnalytics analyze(String conversationId) {
        log.info("開始分析記憶 - 對話ID: {}", conversationId);

        List<Message> messages = chatMemory.get(conversationId);

        if (messages.isEmpty()) {
            log.warn("對話 {} 無歷史記錄", conversationId);
            return MemoryAnalytics.empty(conversationId);
        }

        // 統計訊息數量
        int totalMessages = messages.size();
        int userMessages = countMessagesByType(messages, MessageType.USER);
        int assistantMessages = countMessagesByType(messages, MessageType.ASSISTANT);

        // 計算平均訊息長度
        double avgLength = calculateAverageMessageLength(messages);

        // 計算對話時間 (簡化實作,使用當前時間)
        LocalDateTime firstMessageTime = LocalDateTime.now().minusHours(1); // 模擬
        LocalDateTime lastMessageTime = LocalDateTime.now(); // 模擬
        Duration duration = Duration.between(firstMessageTime, lastMessageTime);

        MemoryAnalytics analytics = new MemoryAnalytics(
            conversationId,
            totalMessages,
            userMessages,
            assistantMessages,
            avgLength,
            firstMessageTime,
            lastMessageTime,
            duration
        );

        log.info("分析完成 - 總訊息: {}, 用戶: {}, 助手: {}, 平均長度: {}",
            totalMessages, userMessages, assistantMessages, avgLength);

        return analytics;
    }

    /**
     * 統計特定類型的訊息數量
     */
    private int countMessagesByType(List<Message> messages, MessageType type) {
        return (int) messages.stream()
            .filter(msg -> msg.getMessageType() == type)
            .count();
    }

    /**
     * 計算平均訊息長度
     */
    private double calculateAverageMessageLength(List<Message> messages) {
        if (messages.isEmpty()) {
            return 0.0;
        }

        int totalLength = messages.stream()
            .mapToInt(msg -> msg.getText() != null ? msg.getText().length() : 0)
            .sum();

        return (double) totalLength / messages.size();
    }

    /**
     * 獲取對話統計摘要
     */
    public String getStatsSummary(String conversationId) {
        MemoryAnalytics analytics = analyze(conversationId);

        return String.format("""
            對話統計摘要 [%s]:
            - 總訊息數: %d
            - 用戶訊息: %d
            - 助手訊息: %d
            - 平均訊息長度: %.1f 字元
            - 對話時長: %d 分鐘
            - 活躍狀態: %s
            """,
            conversationId,
            analytics.totalMessages(),
            analytics.userMessages(),
            analytics.assistantMessages(),
            analytics.avgMessageLength(),
            analytics.getDurationInMinutes(),
            analytics.isActive() ? "是" : "否"
        );
    }
}
