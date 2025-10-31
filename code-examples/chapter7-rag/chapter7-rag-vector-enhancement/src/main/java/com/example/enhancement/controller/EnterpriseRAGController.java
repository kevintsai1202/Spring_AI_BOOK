package com.example.enhancement.controller;

import com.example.enhancement.service.EnterpriseRAGService;
import com.example.enhancement.service.ReportGenerationService;
import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企業 RAG 控制器
 * 提供 RAG 查詢和報告生成的 REST API
 */
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
@Slf4j
public class EnterpriseRAGController {

    private final EnterpriseRAGService ragService;
    private final ReportGenerationService reportService;

    // ==================== RAG 查詢 ====================

    /**
     * 執行 RAG 查詢
     */
    @PostMapping("/query")
    @Timed(value = "rag.query", description = "RAG query execution")
    public ResponseEntity<EnterpriseRAGService.RAGQueryResult> query(
            @RequestBody RAGQueryRequest request) {
        log.info("收到 RAG 查詢請求: {}", request.getQuery());

        int topK = request.getTopK() != null ? request.getTopK() : 5;
        double threshold = request.getSimilarityThreshold() != null ?
                request.getSimilarityThreshold() : 0.7;

        EnterpriseRAGService.RAGQueryResult result =
                ragService.query(request.getQuery(), topK, threshold);

        return ResponseEntity.ok(result);
    }

    /**
     * 執行 RAG 查詢（使用快取）
     */
    @PostMapping("/query/cached")
    @Timed(value = "rag.query.cached", description = "Cached RAG query")
    public ResponseEntity<EnterpriseRAGService.RAGQueryResult> queryCached(
            @RequestBody RAGQueryRequest request) {
        log.info("收到快取 RAG 查詢請求: {}", request.getQuery());

        int topK = request.getTopK() != null ? request.getTopK() : 5;
        double threshold = request.getSimilarityThreshold() != null ?
                request.getSimilarityThreshold() : 0.7;

        EnterpriseRAGService.RAGQueryResult result =
                ragService.queryCached(request.getQuery(), topK, threshold);

        return ResponseEntity.ok(result);
    }

    // ==================== 報告生成 ====================

    /**
     * 生成快速報告（單一問題）
     */
    @PostMapping("/report/quick")
    @Timed(value = "report.quick", description = "Quick report generation")
    public ResponseEntity<QuickReportResponse> generateQuickReport(
            @RequestBody QuickReportRequest request) {
        log.info("收到快速報告生成請求: {}", request.getQuestion());

        String report = reportService.generateQuickReport(request.getQuestion());

        return ResponseEntity.ok(new QuickReportResponse(
                request.getQuestion(),
                report,
                "markdown"
        ));
    }

    /**
     * 生成完整報告（多個問題）
     */
    @PostMapping("/report/full")
    @Timed(value = "report.full", description = "Full report generation")
    public ResponseEntity<ReportGenerationService.GeneratedReport> generateFullReport(
            @RequestBody FullReportRequest request) {
        log.info("收到完整報告生成請求: topic={}, questions={}",
                request.getTopic(), request.getQuestions().size());

        ReportGenerationService.GeneratedReport report =
                reportService.generateReport(request.getTopic(), request.getQuestions());

        return ResponseEntity.ok(report);
    }

    // ==================== 健康檢查 ====================

    /**
     * RAG 服務健康檢查
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        log.debug("RAG 服務健康檢查");
        return ResponseEntity.ok(new HealthResponse(
                "healthy",
                "Enterprise RAG Service is running",
                System.currentTimeMillis()
        ));
    }

    // ==================== 請求/響應類 ====================

    /**
     * RAG 查詢請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RAGQueryRequest {
        /**
         * 查詢問題
         */
        private String query;

        /**
         * 檢索文檔數量
         */
        private Integer topK;

        /**
         * 相似度閾值
         */
        private Double similarityThreshold;
    }

    /**
     * 快速報告請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickReportRequest {
        /**
         * 問題
         */
        private String question;
    }

    /**
     * 快速報告響應
     */
    public record QuickReportResponse(
            String question,
            String content,
            String format
    ) {
    }

    /**
     * 完整報告請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FullReportRequest {
        /**
         * 報告主題
         */
        private String topic;

        /**
         * 分析問題列表
         */
        private List<String> questions;
    }

    /**
     * 健康檢查響應
     */
    public record HealthResponse(
            String status,
            String message,
            long timestamp
    ) {
    }
}
