package com.example.memory.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * 日誌記錄 Advisor
 *
 * 負責記錄所有的請求和回應信息
 * 用於審計和調試
 */
@Slf4j
@Component
public class LoggingAdvisor implements Advisor {

    /**
     * 返回Advisor名稱
     */
    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    /**
     * 執行順序：800（靠後執行，日誌記錄）
     */
    @Override
    public int getOrder() {
        return 800;
    }

    /**
     * 請求前：記錄請求信息
     */
    @Override
    public AdvisorContext adviseRequest(AdvisorContext context) {
        log.info("=== Logging Advisor (Request) ===");
        log.info("Conversation ID: {}", context.getConversationId());
        log.info("User ID: {}", context.getUserId());
        log.info("User Message: {}", context.getUserMessage());
        log.info("Modified Prompt: {}", context.getModifiedPrompt());
        log.info("Request Time: {}", LocalDateTime.now());
        log.info("===================================");

        return context;
    }

    /**
     * 回應後：記錄回應信息
     */
    @Override
    public AdvisorContext adviseResponse(AdvisorContext context) {
        log.info("=== Logging Advisor (Response) ===");
        log.info("Conversation ID: {}", context.getConversationId());

        if (context.isError()) {
            log.error("Error: {}", context.getErrorMessage());
        } else {
            log.info("AI Response: {}", context.getAiResponse());
            log.info("Response Time: {}", LocalDateTime.now());
        }

        // 計算執行時間
        if (context.getRequestTime() != null && context.getResponseTime() != null) {
            long duration = java.time.temporal.ChronoUnit.MILLIS
                .between(context.getRequestTime(), context.getResponseTime());
            log.info("Execution Time: {} ms", duration);
        }

        log.info("===================================");

        context.setResponseTime(LocalDateTime.now());
        context.setExecuted(true);

        return context;
    }
}
