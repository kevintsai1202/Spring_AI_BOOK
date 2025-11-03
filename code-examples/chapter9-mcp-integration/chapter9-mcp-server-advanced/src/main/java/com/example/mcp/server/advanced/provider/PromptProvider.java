package com.example.mcp.server.advanced.provider;

import lombok.extern.slf4j.Slf4j;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MCP 提示提供者
 * 實作各種智能提示模板
 */
@Service
@Slf4j
public class PromptProvider {

    /**
     * 簡單問候提示
     *
     * @param name 用戶姓名
     * @return 問候提示結果
     */
    @McpPrompt(
        name = "greeting",
        description = "Simple greeting prompt"
    )
    public McpSchema.GetPromptResult greeting(
            @McpArg(name = "name", required = true, description = "User's name") String name) {

        log.info("生成問候提示: name={}", name);

        String message = String.format("Hello, %s! How can I help you today?", name);

        return new McpSchema.GetPromptResult(
            "Greeting",
            List.of(new McpSchema.PromptMessage(
                McpSchema.Role.ASSISTANT,
                new McpSchema.TextContent(message)
            ))
        );
    }

    /**
     * 個性化訊息（帶參數邏輯）
     * 根據用戶的年齡和興趣生成個性化訊息
     *
     * @param exchange MCP Server Exchange（用於發送日誌）
     * @param name 用戶姓名
     * @param age 用戶年齡（可選）
     * @param interests 用戶興趣（可選）
     * @return 個性化訊息提示結果
     */
    @McpPrompt(
        name = "personalized-message",
        description = "Personalized message based on user info (name, age, interests)"
    )
    public McpSchema.GetPromptResult personalizedMessage(
            McpSyncServerExchange exchange,
            @McpArg(name = "name", required = true, description = "User's name") String name,
            @McpArg(name = "age", required = false, description = "User's age") Integer age,
            @McpArg(name = "interests", required = false, description = "User's interests") String interests) {

        log.info("生成個性化訊息: name={}, age={}, interests={}", name, age, interests);

        // 記錄日誌到 Client
        exchange.loggingNotification(McpSchema.LoggingMessageNotification.builder()
            .level(McpSchema.LoggingLevel.INFO)
            .data("Generating personalized message for: " + name)
            .build());

        StringBuilder message = new StringBuilder("Hello, " + name + "!\n\n");

        // 根據年齡添加不同內容
        if (age != null) {
            if (age < 18) {
                message.append("You're young and full of potential! The world is yours to explore.\n");
            } else if (age < 30) {
                message.append("You have so much ahead of you. This is a great time to build your future.\n");
            } else if (age < 60) {
                message.append("You have gained valuable life experience and wisdom.\n");
            } else {
                message.append("You have accumulated a lifetime of wisdom to share with others.\n");
            }
        }

        // 根據興趣添加內容
        if (interests != null && !interests.isEmpty()) {
            message.append("I see you're interested in ").append(interests).append(". ");
            message.append("That's wonderful! I'd love to hear more about your passion.\n");
        }

        message.append("\nHow can I assist you today?");

        // 記錄完成
        exchange.loggingNotification(McpSchema.LoggingMessageNotification.builder()
            .level(McpSchema.LoggingLevel.INFO)
            .data("Personalized message generated successfully")
            .build());

        return new McpSchema.GetPromptResult(
            "Personalized Message",
            List.of(new McpSchema.PromptMessage(
                McpSchema.Role.ASSISTANT,
                new McpSchema.TextContent(message.toString())
            ))
        );
    }

    /**
     * 多訊息對話流程
     * 建立一個關於特定主題的對話流程
     *
     * @param topic 學習主題
     * @return 對話流程提示結果
     */
    @McpPrompt(
        name = "conversation-starter",
        description = "Multi-message conversation flow for learning a topic"
    )
    public McpSchema.GetPromptResult conversationStarter(
            @McpArg(name = "topic", required = true, description = "Topic to learn about") String topic) {

        log.info("生成對話流程: topic={}", topic);

        List<McpSchema.PromptMessage> messages = new ArrayList<>();

        // 用戶初始請求
        messages.add(new McpSchema.PromptMessage(
            McpSchema.Role.USER,
            new McpSchema.TextContent("I want to learn about " + topic)
        ));

        // 助手回應
        messages.add(new McpSchema.PromptMessage(
            McpSchema.Role.ASSISTANT,
            new McpSchema.TextContent(
                "Great! Let's start with the basics of " + topic + ". " +
                "I'll guide you through the fundamental concepts step by step."
            )
        ));

        // 用戶後續問題
        messages.add(new McpSchema.PromptMessage(
            McpSchema.Role.USER,
            new McpSchema.TextContent("What are the key concepts I should understand?")
        ));

        // 助手詳細回應框架
        messages.add(new McpSchema.PromptMessage(
            McpSchema.Role.ASSISTANT,
            new McpSchema.TextContent(
                "The key concepts of " + topic + " include:\n\n" +
                "1. Foundational Principles\n" +
                "2. Core Techniques\n" +
                "3. Best Practices\n" +
                "4. Common Patterns\n" +
                "5. Advanced Topics\n\n" +
                "Let's explore each area in detail. Which one would you like to start with?"
            )
        ));

        return new McpSchema.GetPromptResult("Conversation Starter", messages);
    }

    /**
     * 時間感知問候
     * 根據當前時間生成合適的問候語
     *
     * @param exchange MCP Server Exchange
     * @param name 用戶姓名
     * @return 時間感知問候提示
     */
    @McpPrompt(
        name = "time-aware-greeting",
        description = "Time-aware greeting based on current time of day"
    )
    public McpSchema.GetPromptResult timeAwareGreeting(
            McpSyncServerExchange exchange,
            @McpArg(name = "name", required = true, description = "User's name") String name) {

        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        String timeGreeting;
        if (hour < 12) {
            timeGreeting = "Good morning";
        } else if (hour < 18) {
            timeGreeting = "Good afternoon";
        } else {
            timeGreeting = "Good evening";
        }

        String message = String.format("%s, %s! It's %s now. How can I assist you?",
            timeGreeting, name, now.toString().substring(0, 5));

        exchange.loggingNotification(McpSchema.LoggingMessageNotification.builder()
            .level(McpSchema.LoggingLevel.DEBUG)
            .data(String.format("Generated time-aware greeting for %s at %02d:%02d",
                name, hour, now.getMinute()))
            .build());

        return new McpSchema.GetPromptResult(
            "Time-Aware Greeting",
            List.of(new McpSchema.PromptMessage(
                McpSchema.Role.ASSISTANT,
                new McpSchema.TextContent(message)
            ))
        );
    }
}
