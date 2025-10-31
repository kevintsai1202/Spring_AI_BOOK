package com.example.memory.advanced.controller;

import com.example.memory.advanced.dto.AnalyticsResponse;
import com.example.memory.advanced.model.MemoryAnalytics;
import com.example.memory.advanced.service.MemoryAnalyticsService;
import com.example.memory.advanced.service.MemoryOptimizationService;
import com.example.memory.advanced.service.MemoryOptimizationService.MemoryStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 對話管理控制器
 *
 * 提供對話記憶管理相關的 API:
 * - 記憶統計與分析
 * - 記憶優化與清理
 * - 對話歷史管理
 */
@RestController
@RequestMapping("/api/management")
public class ConversationManagementController {

    private static final Logger log = LoggerFactory.getLogger(ConversationManagementController.class);

    private final MemoryAnalyticsService analyticsService;
    private final MemoryOptimizationService optimizationService;

    public ConversationManagementController(
        MemoryAnalyticsService analyticsService,
        MemoryOptimizationService optimizationService
    ) {
        this.analyticsService = analyticsService;
        this.optimizationService = optimizationService;
    }

    /**
     * 獲取對話分析統計
     */
    @GetMapping("/analytics/{conversationId}")
    public AnalyticsResponse getAnalytics(@PathVariable String conversationId) {
        log.info("獲取對話分析 - 對話ID: {}", conversationId);

        MemoryAnalytics analytics = analyticsService.analyze(conversationId);
        return AnalyticsResponse.from(analytics);
    }

    /**
     * 獲取對話統計摘要 (文字格式)
     */
    @GetMapping("/analytics/{conversationId}/summary")
    public ResponseEntity<String> getAnalyticsSummary(@PathVariable String conversationId) {
        log.info("獲取對話統計摘要 - 對話ID: {}", conversationId);

        String summary = analyticsService.getStatsSummary(conversationId);
        return ResponseEntity.ok(summary);
    }

    /**
     * 獲取記憶統計資訊
     */
    @GetMapping("/memory/stats/{conversationId}")
    public MemoryStats getMemoryStats(@PathVariable String conversationId) {
        log.info("獲取記憶統計 - 對話ID: {}", conversationId);
        return optimizationService.getMemoryStats(conversationId);
    }

    /**
     * 優化指定對話的記憶
     */
    @PostMapping("/memory/optimize/{conversationId}")
    public ResponseEntity<String> optimizeMemory(@PathVariable String conversationId) {
        log.info("優化記憶 - 對話ID: {}", conversationId);

        optimizationService.optimizeMemory(conversationId);
        return ResponseEntity.ok("記憶優化完成");
    }

    /**
     * 清除指定對話的所有記憶
     */
    @DeleteMapping("/memory/{conversationId}")
    public ResponseEntity<String> clearMemory(@PathVariable String conversationId) {
        log.info("清除記憶 - 對話ID: {}", conversationId);

        optimizationService.clearMemory(conversationId);
        return ResponseEntity.ok("記憶已清除");
    }

    /**
     * 健康檢查端點
     */
    @GetMapping("/health")
    public String health() {
        return "Conversation Management Service is running!";
    }
}
