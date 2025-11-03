package com.example.mcp.server;

import com.example.mcp.server.provider.tool.MathToolProvider;
import com.example.mcp.server.provider.tool.TextToolProvider;
import com.example.mcp.server.provider.tool.TimeToolProvider;
import com.example.mcp.server.provider.tool.WeatherToolProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * MCP Server 應用程式主類
 * Spring AI MCP Server - 工具與資源開發範例
 *
 * 此應用程式展示如何使用 Spring AI 1.0.3 開發 MCP Server:
 * - 工具 (Tools): 提供可調用的工具服務
 * - 支援 STDIO 和 SSE 雙傳輸模式
 *
 * @author Spring AI Book
 * @version 1.0.0
 */
@Slf4j
@SpringBootApplication
public class McpServerApplication {

    /**
     * 應用程式入口
     *
     * @param args 命令行參數
     */
    public static void main(String[] args) {
        log.info("========================================");
        log.info("  啟動 MCP Server - 工具與資源");
        log.info("  Spring AI 1.0.3 穩定版");
        log.info("========================================");

        SpringApplication.run(McpServerApplication.class, args);

        log.info("========================================");
        log.info("  MCP Server 已啟動完成");
        log.info("========================================");
    }

    /**
     * 註冊天氣工具
     *
     * @param weatherToolProvider 天氣工具提供者
     * @return ToolCallbackProvider
     */
    @Bean
    public ToolCallbackProvider weatherTools(WeatherToolProvider weatherToolProvider) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherToolProvider)
                .build();
    }

    /**
     * 註冊數學工具
     *
     * @param mathToolProvider 數學工具提供者
     * @return ToolCallbackProvider
     */
    @Bean
    public ToolCallbackProvider mathTools(MathToolProvider mathToolProvider) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mathToolProvider)
                .build();
    }

    /**
     * 註冊文本工具
     *
     * @param textToolProvider 文本工具提供者
     * @return ToolCallbackProvider
     */
    @Bean
    public ToolCallbackProvider textTools(TextToolProvider textToolProvider) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(textToolProvider)
                .build();
    }

    /**
     * 註冊時間工具
     *
     * @param timeToolProvider 時間工具提供者
     * @return ToolCallbackProvider
     */
    @Bean
    public ToolCallbackProvider timeTools(TimeToolProvider timeToolProvider) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(timeToolProvider)
                .build();
    }
}
