package com.example.memory.vector.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 記憶系統配置類
 *
 * 配置內容:
 * - 短期記憶 (MessageWindowChatMemory)
 * - 長期記憶參數
 * - 同步策略參數
 */
@Slf4j
@Configuration
public class MemoryConfig {

    /**
     * 創建短期記憶 Bean
     * 使用 MessageWindowChatMemory 實現
     *
     * @return ChatMemory 實例
     */
    @Bean
    public ChatMemory chatMemory() {
        log.info("初始化短期記憶 (MessageWindowChatMemory)");

        return MessageWindowChatMemory.builder()
            .maxMessages(20)  // 保留最近20條訊息
            .build();
    }

    /**
     * 短期記憶配置屬性
     */
    @Data
    @Component
    @ConfigurationProperties(prefix = "memory.short-term")
    public static class ShortTermMemoryProperties {
        /**
         * 最大訊息數量
         */
        private int maxMessages = 20;

        /**
         * 記憶類型 (in-memory, jdbc)
         */
        private String type = "in-memory";
    }

    /**
     * 長期記憶配置屬性
     */
    @Data
    @Component
    @ConfigurationProperties(prefix = "memory.long-term")
    public static class LongTermMemoryProperties {
        /**
         * 是否啟用長期記憶
         */
        private boolean enabled = true;

        /**
         * 相似性閾值 (0-1之間)
         */
        private double similarityThreshold = 0.75;

        /**
         * 返回的最相似結果數量
         */
        private int topK = 10;
    }

    /**
     * 記憶同步配置屬性
     */
    @Data
    @Component
    @ConfigurationProperties(prefix = "memory.sync")
    public static class MemorySyncProperties {
        /**
         * 是否啟用自動同步
         */
        private boolean enabled = true;

        /**
         * 自動同步間隔(毫秒)
         */
        private long interval = 300000; // 5分鐘

        /**
         * 觸發同步的訊息數量閾值
         */
        private int messageThreshold = 10;

        /**
         * 批量處理大小
         */
        private int batchSize = 50;
    }
}
