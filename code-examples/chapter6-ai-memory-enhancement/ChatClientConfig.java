/**
 * ChatClient 配置類別
 * 配置帶有記憶功能的 ChatClient
 */
package com.example.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    
    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build()
            )
            .build();
    }
    
    @Bean
    public ChatClient streamingChatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
            .defaultAdvisors(
                // 串流場景使用不同的配置
                MessageChatMemoryAdvisor.builder(chatMemory)
                    .lastN(20)  // 串流時使用較少的歷史訊息
                    .build()
            )
            .build();
    }
}