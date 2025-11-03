package com.example.mcp.server.advanced;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * MCP Server Advanced 主應用程式
 * Spring AI MCP Server 進階功能展示：
 * - 提示系統 (@McpPrompt)
 * - 自動完成 (@McpComplete)
 * - 動態工具更新
 * - 客戶端通知 (Progress, Logging)
 * - 資料庫整合 (JPA + H2)
 */
@SpringBootApplication
@EnableJpaRepositories
@Slf4j
public class McpServerAdvancedApplication {

    public static void main(String[] args) {
        log.info("啟動 MCP Server Advanced 應用程式...");
        SpringApplication.run(McpServerAdvancedApplication.class, args);
        log.info("MCP Server Advanced 應用程式啟動完成");
        log.info("Server 端口: 8081");
        log.info("H2 Console: http://localhost:8081/h2-console");
        log.info("健康檢查: http://localhost:8081/api/admin/health");
    }
}
