package com.example.memory.advanced;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring AI 進階記憶管理應用程式
 *
 * 主要功能:
 * - 智能記憶摘要系統
 * - 混合記憶策略 (短期 + 長期)
 * - 對話分析與管理
 * - 記憶優化與清理
 */
@SpringBootApplication
@EnableScheduling
public class AdvancedMemoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdvancedMemoryApplication.class, args);
    }
}
