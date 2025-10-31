package com.example.advancedrag.config;

import com.example.advancedrag.advisor.RerankRAGAdvisor;
import com.example.advancedrag.properties.RAGProperties;
import com.example.advancedrag.reranking.RerankingProvider;
import com.example.advancedrag.reranking.RerankingProviderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * OpenAI 配置
 *
 * 配置 OpenAI Chat Client 和相關服務
 */
@Slf4j
@Configuration
public class OpenAIConfiguration {

    /**
     * 配置 RestClient
     *
     * 用於 Re-ranking 提供者（如 Voyage AI、Cohere 等）的 HTTP 請求
     *
     * @return RestClient 實例
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }

    /**
     * 配置 RerankingProvider
     *
     * 根據配置創建相應的 Re-ranking 提供者
     *
     * @param factory Re-ranking 提供者工廠
     * @return RerankingProvider 實例
     */
    @Bean
    public RerankingProvider rerankingProvider(RerankingProviderFactory factory) {
        RerankingProvider provider = factory.createProvider();
        log.info("Re-ranking 提供者已註冊: {}", provider.getProviderName());
        return provider;
    }

    /**
     * 配置 RerankRAGAdvisor
     *
     * 使用 Spring AI 1.0.3 的 Advisor 模式實現 RAG + Re-ranking
     *
     * @param vectorStore 向量存儲
     * @param rerankingProvider Re-ranking 提供者
     * @param ragProperties RAG 配置
     * @return RerankRAGAdvisor 實例
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.rag.reranking", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RerankRAGAdvisor rerankRAGAdvisor(
            VectorStore vectorStore,
            RerankingProvider rerankingProvider,
            RAGProperties ragProperties) {

        log.info("創建 RerankRAGAdvisor，提供者: {}", rerankingProvider.getProviderName());
        return new RerankRAGAdvisor(vectorStore, rerankingProvider, ragProperties);
    }

    /**
     * 配置 ChatClient（整合 Re-ranking RAG）
     *
     * 當啟用 Re-ranking 時，自動整合 RerankRAGAdvisor
     *
     * @param chatModel Chat 模型
     * @param rerankRAGAdvisor Re-ranking RAG Advisor（可選）
     * @return ChatClient 實例
     */
    @Bean
    public ChatClient chatClient(
            ChatModel chatModel,
            RerankRAGAdvisor rerankRAGAdvisor) {

        ChatClient.Builder builder = ChatClient.builder(chatModel);

        // 如果啟用了 Re-ranking，添加 Advisor
        if (rerankRAGAdvisor != null) {
            log.info("ChatClient 整合 RerankRAGAdvisor");
            builder.defaultAdvisors(rerankRAGAdvisor);
        }

        return builder.build();
    }

    /**
     * 配置 ChatClient Builder（用於自定義配置）
     *
     * @param chatModel Chat 模型
     * @return ChatClient.Builder 實例
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
