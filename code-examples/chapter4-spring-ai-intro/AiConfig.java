/**
 * AI 配置類別
 * 自定義 ChatClient 和多模型配置
 */
package com.example.config;

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
    
    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;
    
    @Value("${spring.ai.groq.api-key:}")
    private String groqApiKey;
    
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
    
    /**
     * 高性能模型 - GPT-4o
     */
    @Bean("highPerformanceModel")
    public ChatModel highPerformanceModel() {
        return OpenAiChatModel.builder()
            .apiKey(openaiApiKey)
            .modelName(OpenAiApi.ChatModel.GPT_4_O.getValue())
            .temperature(0.7)
            .maxTokens(2000)
            .build();
    }
    
    /**
     * 經濟型模型 - GPT-4o mini
     */
    @Bean("economicModel")
    @Primary
    public ChatModel economicModel() {
        return OpenAiChatModel.builder()
            .apiKey(openaiApiKey)
            .modelName(OpenAiApi.ChatModel.GPT_4_O_MINI.getValue())
            .temperature(0.7)
            .maxTokens(1000)
            .build();
    }
    
    /**
     * 高速模型 - Groq
     */
    @Bean("speedModel")
    public ChatModel speedModel() {
        if (groqApiKey == null || groqApiKey.isEmpty()) {
            return economicModel(); // 降級到經濟型模型
        }
        
        return OpenAiChatModel.builder()
            .apiKey(groqApiKey)
            .baseUrl("https://api.groq.com/openai/v1")
            .modelName("llama-3.1-70b-versatile")
            .temperature(0.7)
            .maxTokens(1000)
            .build();
    }
    
    /**
     * 預設聊天選項配置
     */
    @Bean
    public OpenAiChatOptions defaultChatOptions() {
        return OpenAiChatOptions.builder()
                .withModel(OpenAiApi.ChatModel.GPT_4_O_MINI.getValue())  // 模型選擇
                .withTemperature(0.7)      // 創意度：0.0-2.0
                .withMaxTokens(1000)       // 最大輸出 token 數
                .withTopP(1.0)            // 核心採樣：0.0-1.0
                .withFrequencyPenalty(0.0) // 頻率懲罰：-2.0-2.0
                .withPresencePenalty(0.0)  // 存在懲罰：-2.0-2.0
                .build();
    }
    
    /**
     * 創意寫作專用配置
     */
    @Bean("creativeChatOptions")
    public OpenAiChatOptions creativeChatOptions() {
        return OpenAiChatOptions.builder()
                .withModel(OpenAiApi.ChatModel.GPT_4_O.getValue())
                .withTemperature(1.2)      // 高創意度
                .withMaxTokens(2000)       // 較長輸出
                .withTopP(0.9)            // 稍微限制選擇範圍
                .withFrequencyPenalty(0.5) // 避免重複
                .withPresencePenalty(0.3)  // 鼓勵新話題
                .build();
    }
    
    /**
     * 程式碼生成專用配置
     */
    @Bean("codeChatOptions")
    public OpenAiChatOptions codeChatOptions() {
        return OpenAiChatOptions.builder()
                .withModel(OpenAiApi.ChatModel.GPT_4_O.getValue())
                .withTemperature(0.2)      // 低創意度，更精確
                .withMaxTokens(1500)       // 適中輸出長度
                .withTopP(0.95)           // 較保守的選擇
                .withFrequencyPenalty(0.0) // 不懲罰重複（程式碼可能需要重複結構）
                .withPresencePenalty(0.0)
                .build();
    }
}