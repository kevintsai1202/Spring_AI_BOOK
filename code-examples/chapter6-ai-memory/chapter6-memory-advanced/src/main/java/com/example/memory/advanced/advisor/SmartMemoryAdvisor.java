package com.example.memory.advanced.advisor;

import com.example.memory.advanced.config.MemoryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能記憶管理增強器
 *
 * 核心功能:
 * 1. 監控對話訊息數量
 * 2. 當訊息超過閾值時,自動觸發摘要
 * 3. 保留摘要 + 最近訊息,優化記憶使用
 *
 * 注意: 本類別為示範用途,實際使用需要實作 Advisor 介面
 * TODO: 整合 Spring AI Advisor API
 */
public class SmartMemoryAdvisor {

    private static final Logger log = LoggerFactory.getLogger(SmartMemoryAdvisor.class);

    /**
     * Advisor 名稱
     */
    private static final String ADVISOR_NAME = SmartMemoryAdvisor.class.getSimpleName();

    /**
     * 對話 ID 參數鍵
     */
    private static final String CONVERSATION_ID_KEY = "conversationId";

    /**
     * 記憶摘要標記
     */
    private static final String SUMMARY_PREFIX = "[對話摘要] ";

    /**
     * 聊天記憶儲存
     */
    private final ChatMemory chatMemory;

    /**
     * 摘要生成客戶端
     */
    private final ChatClient summarizerClient;

    /**
     * 記憶配置屬性
     */
    private final MemoryProperties memoryProperties;

    /**
     * 執行順序
     */
    private final int order;

    public SmartMemoryAdvisor(
        ChatMemory chatMemory,
        ChatClient summarizerClient,
        MemoryProperties memoryProperties,
        int order
    ) {
        this.chatMemory = chatMemory;
        this.summarizerClient = summarizerClient;
        this.memoryProperties = memoryProperties;
        this.order = order;
    }

    /**
     * 檢查並優化記憶(如果需要)
     */
    public void checkAndOptimize(String conversationId) {
        // 1. 檢查是否啟用智能摘要
        if (!memoryProperties.getSmartSummary().isEnabled()) {
            log.debug("智能摘要未啟用,跳過處理");
            return;
        }

        log.debug("處理對話: {}", conversationId);

        // 2. 獲取當前對話歷史
        List<Message> history = chatMemory.get(conversationId);
        int messageCount = history.size();
        log.debug("當前訊息數量: {}", messageCount);

        // 3. 檢查是否需要摘要
        int threshold = memoryProperties.getSmartSummary().getThreshold();
        if (messageCount > threshold) {
            log.info("訊息數量 ({}) 超過閾值 ({}),觸發智能摘要", messageCount, threshold);
            optimizeMemory(conversationId, history);
        }
    }

    /**
     * 優化記憶:摘要舊訊息,保留摘要 + 最近訊息
     */
    private void optimizeMemory(String conversationId, List<Message> history) {
        try {
            // 配置參數
            int summarizeCount = memoryProperties.getSmartSummary().getSummarizeCount();
            int keepRecent = memoryProperties.getSmartSummary().getKeepRecent();

            // 計算要摘要的訊息範圍
            int totalMessages = history.size();
            int endIndex = Math.min(summarizeCount, totalMessages - keepRecent);

            if (endIndex <= 0) {
                log.warn("無足夠訊息進行摘要,跳過優化");
                return;
            }

            // 提取要摘要的訊息
            List<Message> toSummarize = history.subList(0, endIndex);
            log.debug("準備摘要 {} 條訊息", toSummarize.size());

            // 生成摘要
            String summary = generateSummary(toSummarize);
            log.debug("摘要生成完成: {}", summary.substring(0, Math.min(50, summary.length())));

            // 構建優化後的記憶
            List<Message> optimizedMemory = new ArrayList<>();
            optimizedMemory.add(new SystemMessage(SUMMARY_PREFIX + summary));
            optimizedMemory.addAll(history.subList(endIndex, totalMessages));

            log.info("優化記憶: 原始 {} 條 -> 摘要 1 條 + 保留 {} 條 = {} 條",
                totalMessages, optimizedMemory.size() - 1, optimizedMemory.size());

            // 更新記憶
            chatMemory.clear(conversationId);
            chatMemory.add(conversationId, optimizedMemory);

        } catch (Exception e) {
            log.error("優化記憶失敗: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成訊息摘要
     */
    private String generateSummary(List<Message> messages) {
        // 構建對話文本
        StringBuilder conversationText = new StringBuilder();
        for (Message msg : messages) {
            conversationText.append(msg.getMessageType())
                .append(": ")
                .append(msg.getText())
                .append("\n");
        }

        // 調用摘要客戶端
        return summarizerClient.prompt()
            .user("請簡潔摘要以下對話的重點,保留關鍵資訊:\n\n" + conversationText)
            .call()
            .content();
    }


    /**
     * Builder 模式
     */
    public static Builder builder(ChatMemory chatMemory, ChatClient summarizerClient, MemoryProperties properties) {
        return new Builder(chatMemory, summarizerClient, properties);
    }

    public static class Builder {
        private final ChatMemory chatMemory;
        private final ChatClient summarizerClient;
        private final MemoryProperties memoryProperties;
        private int order = 0;

        public Builder(ChatMemory chatMemory, ChatClient summarizerClient, MemoryProperties properties) {
            this.chatMemory = chatMemory;
            this.summarizerClient = summarizerClient;
            this.memoryProperties = properties;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public SmartMemoryAdvisor build() {
            return new SmartMemoryAdvisor(chatMemory, summarizerClient, memoryProperties, order);
        }
    }
}
