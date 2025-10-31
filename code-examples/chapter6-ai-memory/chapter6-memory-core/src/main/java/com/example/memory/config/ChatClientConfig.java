package com.example.memory.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 配置類
 *
 * 配置Spring AI的ChatClient和相關設置
 */
@Slf4j
@Configuration
public class ChatClientConfig {

    /**
     * 構造ChatClient Bean
     *
     * @param chatModel Spring AI提供的ChatModel
     * @return 配置好的ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        log.info("Initializing ChatClient with ChatModel: {}",
            chatModel.getClass().getSimpleName());

        return ChatClient.builder(chatModel)
            .defaultOptions(
                chatModel.getDefaultOptions()
            )
            .build();
    }
}
