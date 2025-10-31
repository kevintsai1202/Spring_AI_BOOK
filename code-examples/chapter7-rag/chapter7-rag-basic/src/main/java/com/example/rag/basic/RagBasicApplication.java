package com.example.rag.basic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI Chapter 7 - RAG 基礎系統應用程序
 *
 * 本應用程序展示 RAG (Retrieval-Augmented Generation) 的核心功能:
 * - 文檔向量化處理
 * - 向量資料庫存儲 (Neo4j)
 * - QuestionAnswerAdvisor 自動檢索增強
 * - 基礎 RAG 查詢流程
 */
@SpringBootApplication
public class RagBasicApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagBasicApplication.class, args);
    }

}
