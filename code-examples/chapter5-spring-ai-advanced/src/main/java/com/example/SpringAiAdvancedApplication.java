/**
 * Spring AI 進階功能應用主類
 * 提供提示詞範本、Tool Calling 和多模態功能
 */
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

@SpringBootApplication
public class SpringAiAdvancedApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiAdvancedApplication.class, args);
    }

    /**
     * 配置 ChatClient Bean
     * 用於簡化 AI 對話調用
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
