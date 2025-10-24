/**
 * AI 配置類別
 * 自定義 ChatClient 和多模型配置
 */
package com.example.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    /**
     * 自定義 ChatClient 配置
     * @param builder ChatClient 建構器
     * @return 配置好的 ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是一個友善且專業的 AI 助手，" +
                             "專門協助 Java Spring Boot 開發。" +
                             "請用繁體中文回答，並提供實用的程式碼範例。")
                .build();
    }

    /**
     * 專門用於程式碼生成的 ChatClient
     * @param builder ChatClient 建構器
     * @return 程式碼生成專用的 ChatClient
     */
    @Bean("codeChatClient")
    public ChatClient codeChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是一個資深的 Java 開發專家。" +
                             "請提供高品質、可執行的程式碼，" +
                             "包含適當的註解和最佳實踐。")
                .build();
    }

}
