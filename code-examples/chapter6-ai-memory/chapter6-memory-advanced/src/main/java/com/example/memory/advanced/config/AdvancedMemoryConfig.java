package com.example.memory.advanced.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 進階記憶管理配置類別
 *
 * 配置 Spring AI 相關的 Bean,包括:
 * - ChatClient (基本客戶端、摘要客戶端)
 * - ChatMemory (短期記憶)
 */
@Configuration
public class AdvancedMemoryConfig {

    /**
     * 配置基本的 ChatClient
     * 用於一般對話互動
     */
    @Bean("basicChatClient")
    public ChatClient basicChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
            .defaultSystem("你是一個友善且專業的AI助手。")
            .build();
    }

    /**
     * 配置摘要用的 ChatClient
     * 用於生成對話摘要
     */
    @Bean("summarizerClient")
    public ChatClient summarizerClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
            .defaultSystem("""
                你是一個專業的對話摘要助手。
                你的任務是將長對話精簡為簡潔的摘要,保留重要資訊。
                摘要應該:
                1. 簡潔明瞭,避免冗長
                2. 保留關鍵資訊和決策
                3. 使用繁體中文
                """)
            .build();
    }

    /**
     * 配置短期記憶 (ChatMemory)
     * 使用記憶體儲存最近的對話
     */
    @Bean("shortTermMemory")
    public ChatMemory shortTermMemory() {
        return MessageWindowChatMemory.builder()
            .maxMessages(100)
            .build();
    }
}
