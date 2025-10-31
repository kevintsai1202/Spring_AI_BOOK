package com.example.memory.advanced.dto;

import com.example.memory.advanced.model.MemoryAnalytics;

import java.time.LocalDateTime;

/**
 * 記憶分析回應 DTO
 *
 * 將 MemoryAnalytics 轉換為 API 回應格式
 */
public record AnalyticsResponse(
    String conversationId,
    int totalMessages,
    int userMessages,
    int assistantMessages,
    double avgMessageLength,
    LocalDateTime firstMessageTime,
    LocalDateTime lastMessageTime,
    long durationInSeconds,
    long durationInMinutes,
    boolean isActive
) {
    /**
     * 從 MemoryAnalytics 轉換
     */
    public static AnalyticsResponse from(MemoryAnalytics analytics) {
        return new AnalyticsResponse(
            analytics.conversationId(),
            analytics.totalMessages(),
            analytics.userMessages(),
            analytics.assistantMessages(),
            analytics.avgMessageLength(),
            analytics.firstMessageTime(),
            analytics.lastMessageTime(),
            analytics.getDurationInSeconds(),
            analytics.getDurationInMinutes(),
            analytics.isActive()
        );
    }
}
