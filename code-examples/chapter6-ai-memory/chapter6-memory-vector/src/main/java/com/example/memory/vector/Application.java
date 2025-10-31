package com.example.memory.vector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring AI 向量記憶系統主應用類
 *
 * 功能:
 * - 整合 Neo4j 向量資料庫
 * - 實現短期和長期記憶混合系統
 * - 提供記憶同步和智能檢索
 *
 * @author Spring AI Book
 * @version 1.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
