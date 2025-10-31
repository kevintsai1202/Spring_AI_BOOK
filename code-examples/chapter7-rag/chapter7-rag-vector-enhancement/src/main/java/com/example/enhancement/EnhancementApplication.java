package com.example.enhancement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * RAG 向量增強與企業級優化應用程式主類
 *
 * 本應用程式實現以下功能：
 * 1. 智能文本清理與預處理
 * 2. AI 元資料增強（關鍵詞提取、摘要生成）
 * 3. 向量品質評估與優化
 * 4. 企業資料來源整合（PostgreSQL, Neo4j）
 * 5. Redis 快取優化
 * 6. 生產級監控（Prometheus + Grafana）
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
public class EnhancementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnhancementApplication.class, args);
    }
}
