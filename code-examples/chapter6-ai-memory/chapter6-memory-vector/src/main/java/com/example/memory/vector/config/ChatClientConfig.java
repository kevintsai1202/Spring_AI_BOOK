package com.example.memory.vector.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 配置類
 *
 * 提供 ChatClient.Builder Bean,用於構建帶有 Advisor 的對話客戶端
 */
@Slf4j
@Configuration
public class ChatClientConfig {

    /**
     * 創建 ChatClient.Builder Bean
     *
     * @param chatModel ChatModel 實例
     * @return ChatClient.Builder
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        log.info("初始化 ChatClient.Builder");
        return ChatClient.builder(chatModel);
    }
}
