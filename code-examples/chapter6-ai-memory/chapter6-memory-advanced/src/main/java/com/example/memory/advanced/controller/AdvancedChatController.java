package com.example.memory.advanced.controller;

import com.example.memory.advanced.dto.ChatRequest;
import com.example.memory.advanced.dto.ChatResponse;
import com.example.memory.advanced.dto.SummaryResponse;
import com.example.memory.advanced.model.ConversationSummary;
import com.example.memory.advanced.service.ConversationSummaryService;
import com.example.memory.advanced.service.HybridMemoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 進階聊天 API 控制器
 *
 * 提供進階記憶管理相關的 API:
 * - 混合記憶策略對話
 * - 對話摘要生成
 */
@RestController
@RequestMapping("/api/advanced")
public class AdvancedChatController {

    private static final Logger log = LoggerFactory.getLogger(AdvancedChatController.class);

    private final HybridMemoryService hybridMemoryService;
    private final ConversationSummaryService summaryService;

    public AdvancedChatController(
        HybridMemoryService hybridMemoryService,
        ConversationSummaryService summaryService
    ) {
        this.hybridMemoryService = hybridMemoryService;
        this.summaryService = summaryService;
    }

    /**
     * 使用混合記憶策略的對話端點
     */
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        log.info("收到聊天請求 - 對話ID: {}, 策略: {}", request.conversationId(), request.strategy());

        String response = hybridMemoryService.chat(
            request.conversationId(),
            request.message(),
            request.strategy()
        );

        return new ChatResponse(
            request.conversationId(),
            response,
            request.strategy(),
            0  // TODO: 從記憶中獲取實際訊息數量
        );
    }

    /**
     * 生成對話摘要
     */
    @PostMapping("/summarize/{conversationId}")
    public SummaryResponse summarize(@PathVariable String conversationId) {
        log.info("收到摘要請求 - 對話ID: {}", conversationId);

        ConversationSummary summary = summaryService.summarize(conversationId);
        return SummaryResponse.from(summary);
    }

    /**
     * 健康檢查端點
     */
    @GetMapping("/health")
    public String health() {
        return "Advanced Memory Service is running!";
    }
}
