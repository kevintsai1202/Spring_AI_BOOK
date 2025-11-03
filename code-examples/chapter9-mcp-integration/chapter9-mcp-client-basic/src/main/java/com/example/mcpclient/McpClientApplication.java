package com.example.mcpclient;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Scanner;

/**
 * MCP Client 基礎應用 - 主程序
 *
 * <p>這是一個展示如何使用 Spring AI MCP Client 的基礎範例應用。
 * <p>主要功能：
 * <ul>
 *   <li>自動連接配置的 MCP Server (STDIO 和 SSE)</li>
 *   <li>自動整合 MCP 工具到 ChatClient</li>
 *   <li>提供命令行交互界面</li>
 * </ul>
 *
 * <p>使用方式：
 * <pre>
 * 1. 設置環境變數:
 *    export OPENAI_API_KEY=your-api-key
 *    export BRAVE_API_KEY=your-brave-api-key (可選)
 *
 * 2. 運行應用:
 *    mvn spring-boot:run
 *
 * 3. 與AI對話:
 *    輸入問題，AI會使用MCP工具來回答
 *    輸入 exit 或 quit 退出
 * </pre>
 *
 * @author Spring AI Book
 * @version 1.0.0
 */
@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
    }

    /**
     * 創建 ChatClient 交互界面
     *
     * @param chatClientBuilder Spring AI 自動配置的 ChatClient Builder
     * @param mcpSyncClients Spring AI MCP Starter 自動配置的 MCP Client 列表
     * @return CommandLineRunner
     */
    @Bean
    public CommandLineRunner interactiveChatbot(
            ChatClient.Builder chatClientBuilder,
            List<McpSyncClient> mcpSyncClients) {

        return args -> {

            // 顯示連接資訊
            System.out.println("\n========================================");
            System.out.println("  Spring AI MCP Client 基礎範例");
            System.out.println("========================================");
            System.out.println("\n已連接的 MCP Server: " + mcpSyncClients.size() + " 個");

            for (McpSyncClient client : mcpSyncClients) {
                System.out.println("  - " + client.getClass().getSimpleName());
            }

            // 配置 ChatClient
            var chatClient = chatClientBuilder
                    .defaultSystem("""
                            你是一個智能助手，可以調用各種工具來幫助用戶。
                            當你需要查找最新信息或執行特定任務時，請使用可用的工具。
                            回答時請清晰、準確，並說明你使用了哪些工具。
                            """)
                    .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients))
                    .build();

            // 開始交互式對話
            System.out.println("\n我是你的AI助手，可以調用 MCP 工具來回答問題。");
            System.out.println("輸入 'exit' 或 'quit' 退出程式。\n");

            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("你: ");
                    String input = scanner.nextLine().trim();

                    // 檢查退出命令
                    if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                        System.out.println("\n再見！");
                        break;
                    }

                    // 跳過空輸入
                    if (input.isEmpty()) {
                        continue;
                    }

                    try {
                        // 發送問題並獲取回答
                        System.out.print("\nAI: ");
                        String response = chatClient
                                .prompt(input)
                                .call()
                                .content();
                        System.out.println(response + "\n");
                    } catch (Exception e) {
                        System.err.println("錯誤: " + e.getMessage());
                    }
                }
            }
        };
    }
}
