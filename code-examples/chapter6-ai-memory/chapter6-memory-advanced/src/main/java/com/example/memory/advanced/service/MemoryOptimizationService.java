package com.example.memory.advanced.service;

import com.example.memory.advanced.config.MemoryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 記憶優化服務
 *
 * 提供記憶優化和清理功能:
 * 1. 自動清理過期記憶
 * 2. 限制記憶數量
 * 3. 記憶壓縮
 */
@Service
public class MemoryOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(MemoryOptimizationService.class);

    private final ChatMemory chatMemory;
    private final MemoryProperties memoryProperties;

    public MemoryOptimizationService(
        @Qualifier("shortTermMemory") ChatMemory chatMemory,
        MemoryProperties memoryProperties
    ) {
        this.chatMemory = chatMemory;
        this.memoryProperties = memoryProperties;
    }

    /**
     * 檢查並優化指定對話的記憶
     *
     * @param conversationId 對話 ID
     */
    public void optimizeMemory(String conversationId) {
        log.info("開始優化記憶 - 對話ID: {}", conversationId);

        // 獲取當前記憶
        List<Message> messages = chatMemory.get(conversationId);
        int currentCount = messages.size();

        // 檢查是否需要清理
        int maxMessages = memoryProperties.getOptimization().getMaxMessages();
        if (currentCount > maxMessages) {
            log.warn("記憶數量 ({}) 超過限制 ({}),執行清理", currentCount, maxMessages);
            cleanupExcessMessages(conversationId, messages, maxMessages);
        } else {
            log.debug("記憶數量 ({}) 未超過限制 ({})", currentCount, maxMessages);
        }
    }

    /**
     * 清理超出限制的訊息
     */
    private void cleanupExcessMessages(String conversationId, List<Message> messages, int maxMessages) {
        // 保留最近的 maxMessages 條訊息
        int toRemove = messages.size() - maxMessages;
        if (toRemove > 0) {
            List<Message> recentMessages = messages.subList(toRemove, messages.size());

            // 清除舊記憶並添加最近記憶
            chatMemory.clear(conversationId);
            chatMemory.add(conversationId, recentMessages);

            log.info("已清理 {} 條舊訊息,保留最近 {} 條", toRemove, maxMessages);
        }
    }

    /**
     * 定期清理過期記憶 (每小時執行一次)
     */
    @Scheduled(fixedRate = 3600000) // 每小時
    public void scheduledCleanup() {
        if (!memoryProperties.getOptimization().isAutoCleanup()) {
            log.debug("自動清理未啟用,跳過定期清理");
            return;
        }

        log.info("執行定期記憶清理");
        // TODO: 實作基於時間的記憶清理邏輯
        // 需要追蹤每個對話的最後活動時間
    }

    /**
     * 清除指定對話的所有記憶
     *
     * @param conversationId 對話 ID
     */
    public void clearMemory(String conversationId) {
        log.info("清除對話記憶 - 對話ID: {}", conversationId);
        chatMemory.clear(conversationId);
    }

    /**
     * 獲取記憶統計資訊
     *
     * @param conversationId 對話 ID
     * @return 記憶統計
     */
    public MemoryStats getMemoryStats(String conversationId) {
        List<Message> messages = chatMemory.get(conversationId);
        int maxMessages = memoryProperties.getOptimization().getMaxMessages();

        return new MemoryStats(
            conversationId,
            messages.size(),
            maxMessages,
            calculateUsagePercentage(messages.size(), maxMessages),
            LocalDateTime.now()
        );
    }

    /**
     * 計算記憶使用率
     */
    private double calculateUsagePercentage(int current, int max) {
        if (max == 0) return 0.0;
        return (double) current / max * 100.0;
    }

    /**
     * 記憶統計資訊
     */
    public record MemoryStats(
        String conversationId,
        int currentMessages,
        int maxMessages,
        double usagePercentage,
        LocalDateTime timestamp
    ) {}
}
