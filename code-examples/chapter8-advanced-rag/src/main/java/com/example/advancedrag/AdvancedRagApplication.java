package com.example.advancedrag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Advanced RAG 系統主應用類
 *
 * 企業級智能檢索增強生成（Retrieval-Augmented Generation）系統
 *
 * 核心特性：
 * - 多階段智能檢索（粗檢索 + Re-ranking 精檢索）
 * - 智能 Embedding 模型選擇和管理
 * - 多層內容審核機制
 * - 自動化評估測試框架
 * - 完整的監控和指標收集
 *
 * @author Advanced RAG Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan("com.example.advancedrag.properties")
public class AdvancedRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdvancedRagApplication.class, args);
    }
}
