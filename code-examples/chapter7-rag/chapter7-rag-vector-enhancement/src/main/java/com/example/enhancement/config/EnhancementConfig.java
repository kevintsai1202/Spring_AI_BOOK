package com.example.enhancement.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * RAG 向量增強主配置類
 *
 * 負責配置：
 * 1. Spring AI ChatClient
 * 2. 執行緒池（用於非同步處理）
 * 3. 其他核心 Bean
 */
@Configuration
public class EnhancementConfig {

    @Value("${app.thread-pool.core-size:5}")
    private int threadPoolCoreSize;

    @Value("${app.thread-pool.max-size:10}")
    private int threadPoolMaxSize;

    @Value("${app.thread-pool.queue-capacity:100}")
    private int queueCapacity;

    /**
     * 配置 Spring AI ChatClient
     * 用於與 LLM 進行對話互動
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .build();
    }

    /**
     * 配置非同步執行緒池
     * 用於處理文本清理、元資料增強等耗時操作
     */
    @Bean(name = "enhancementTaskExecutor")
    public Executor enhancementTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolCoreSize);
        executor.setMaxPoolSize(threadPoolMaxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("enhancement-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
