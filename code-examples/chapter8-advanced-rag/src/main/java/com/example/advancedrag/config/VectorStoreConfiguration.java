package com.example.advancedrag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * 向量數據庫配置
 *
 * PgVector VectorStore 由 Spring AI 自動配置
 * 通過 spring-ai-starter-vector-store-pgvector 依賴和 application.yml 配置自動創建
 *
 * 配置項參考:
 * - spring.ai.vectorstore.pgvector.host
 * - spring.ai.vectorstore.pgvector.port
 * - spring.ai.vectorstore.pgvector.database
 * - spring.ai.vectorstore.pgvector.username
 * - spring.ai.vectorstore.pgvector.password
 */
@Slf4j
@Configuration
public class VectorStoreConfiguration {

    /**
     * 應用啟動完成後的初始化日誌
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("===========================================");
        log.info("PgVector VectorStore 已由 Spring AI 自動配置完成");
        log.info("===========================================");
    }
}
