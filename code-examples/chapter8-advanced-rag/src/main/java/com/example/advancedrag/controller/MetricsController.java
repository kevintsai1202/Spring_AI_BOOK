package com.example.advancedrag.controller;

import com.example.advancedrag.dto.ApiResponse;
import com.example.advancedrag.service.RAGMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 指標監控控制器
 *
 * 提供 RAG 系統監控指標查詢 REST API：
 * - GET /api/v1/metrics/summary - 整體指標摘要
 * - GET /api/v1/metrics/query - 查詢相關指標
 * - GET /api/v1/metrics/performance - 性能指標
 * - GET /api/v1/metrics/evaluation - 評估指標
 * - GET /api/v1/metrics/health - 健康狀態
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final RAGMetricsService metricsService;

    /**
     * 獲取整體指標摘要
     *
     * @return 整體指標摘要
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<MetricsSummary>> getMetricsSummary() {
        log.info("獲取整體指標摘要");

        try {
            MetricsSummary summary = MetricsSummary.builder()
                    .timestamp(LocalDateTime.now())
                    .totalQueries(metricsService.getTotalQueries())
                    .successQueries(metricsService.getSuccessQueries())
                    .failureQueries(metricsService.getFailureQueries())
                    .successRate(metricsService.getSuccessRate())
                    .activeQueries(metricsService.getActiveQueries())
                    .averageResponseTime(metricsService.getAverageResponseTime())
                    .p95ResponseTime(metricsService.getP95ResponseTime())
                    .p99ResponseTime(metricsService.getP99ResponseTime())
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success("指標摘要獲取成功", summary)
            );

        } catch (Exception e) {
            log.error("獲取指標摘要失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("獲取指標摘要失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 獲取查詢相關指標
     *
     * @return 查詢指標
     */
    @GetMapping("/query")
    public ResponseEntity<ApiResponse<QueryMetrics>> getQueryMetrics() {
        log.info("獲取查詢指標");

        try {
            QueryMetrics metrics = QueryMetrics.builder()
                    .timestamp(LocalDateTime.now())
                    .totalQueries(metricsService.getTotalQueries())
                    .successQueries(metricsService.getSuccessQueries())
                    .failureQueries(metricsService.getFailureQueries())
                    .successRate(metricsService.getSuccessRate())
                    .activeQueries(metricsService.getActiveQueries())
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success("查詢指標獲取成功", metrics)
            );

        } catch (Exception e) {
            log.error("獲取查詢指標失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("獲取查詢指標失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 獲取性能指標
     *
     * @return 性能指標
     */
    @GetMapping("/performance")
    public ResponseEntity<ApiResponse<PerformanceMetrics>> getPerformanceMetrics() {
        log.info("獲取性能指標");

        try {
            PerformanceMetrics metrics = PerformanceMetrics.builder()
                    .timestamp(LocalDateTime.now())
                    .averageResponseTime(metricsService.getAverageResponseTime())
                    .p50ResponseTime(metricsService.getAverageResponseTime())
                    .p95ResponseTime(metricsService.getP95ResponseTime())
                    .p99ResponseTime(metricsService.getP99ResponseTime())
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success("性能指標獲取成功", metrics)
            );

        } catch (Exception e) {
            log.error("獲取性能指標失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("獲取性能指標失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 獲取評估指標
     *
     * @return 評估指標
     */
    @GetMapping("/evaluation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEvaluationMetrics() {
        log.info("獲取評估指標");

        try {
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("timestamp", LocalDateTime.now());
            metrics.put("note", "評估指標需要通過 Prometheus 查詢");
            metrics.put("prometheusMetrics", Map.of(
                    "accuracy", "rag.evaluation.accuracy",
                    "relevance", "rag.evaluation.relevance",
                    "overall", "rag.evaluation.overall"
            ));

            return ResponseEntity.ok(
                    ApiResponse.success("評估指標獲取成功", metrics)
            );

        } catch (Exception e) {
            log.error("獲取評估指標失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("獲取評估指標失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 獲取健康狀態
     *
     * @return 健康狀態
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthStatus>> getHealthStatus() {
        log.info("獲取健康狀態");

        try {
            // 定義健康狀態閾值
            double successRate = metricsService.getSuccessRate();
            double p99ResponseTime = metricsService.getP99ResponseTime();
            int activeQueries = metricsService.getActiveQueries();

            // 判斷健康狀態
            String status = "HEALTHY";
            String message = "系統運行正常";

            if (successRate < 90.0) {
                status = "DEGRADED";
                message = "成功率低於 90%";
            } else if (p99ResponseTime > 10000) {
                status = "DEGRADED";
                message = "P99 響應時間超過 10 秒";
            } else if (activeQueries > 100) {
                status = "OVERLOADED";
                message = "活躍查詢數過高";
            }

            HealthStatus health = HealthStatus.builder()
                    .timestamp(LocalDateTime.now())
                    .status(status)
                    .message(message)
                    .successRate(successRate)
                    .p99ResponseTime(p99ResponseTime)
                    .activeQueries(activeQueries)
                    .totalQueries(metricsService.getTotalQueries())
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success("健康狀態獲取成功", health)
            );

        } catch (Exception e) {
            log.error("獲取健康狀態失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("獲取健康狀態失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 重置指標（僅用於測試環境）
     *
     * @return 重置結果
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<String>> resetMetrics() {
        log.warn("重置指標（僅用於測試環境）");

        try {
            // 注意：Micrometer 的 Counter 和 Timer 不支持重置
            // 如需重置，應該重啟應用或使用 Gauge
            return ResponseEntity.ok(
                    ApiResponse.success("指標重置功能未實現（Micrometer 計數器不支持重置）", null)
            );

        } catch (Exception e) {
            log.error("重置指標失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("重置指標失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 指標摘要內部類
     */
    @lombok.Data
    @lombok.Builder
    public static class MetricsSummary {
        /**
         * 時間戳
         */
        private LocalDateTime timestamp;

        /**
         * 總查詢數
         */
        private Double totalQueries;

        /**
         * 成功查詢數
         */
        private Double successQueries;

        /**
         * 失敗查詢數
         */
        private Double failureQueries;

        /**
         * 成功率（%）
         */
        private Double successRate;

        /**
         * 當前活躍查詢數
         */
        private Integer activeQueries;

        /**
         * 平均響應時間（毫秒）
         */
        private Double averageResponseTime;

        /**
         * P95 響應時間（毫秒）
         */
        private Double p95ResponseTime;

        /**
         * P99 響應時間（毫秒）
         */
        private Double p99ResponseTime;
    }

    /**
     * 查詢指標內部類
     */
    @lombok.Data
    @lombok.Builder
    public static class QueryMetrics {
        /**
         * 時間戳
         */
        private LocalDateTime timestamp;

        /**
         * 總查詢數
         */
        private Double totalQueries;

        /**
         * 成功查詢數
         */
        private Double successQueries;

        /**
         * 失敗查詢數
         */
        private Double failureQueries;

        /**
         * 成功率（%）
         */
        private Double successRate;

        /**
         * 當前活躍查詢數
         */
        private Integer activeQueries;
    }

    /**
     * 性能指標內部類
     */
    @lombok.Data
    @lombok.Builder
    public static class PerformanceMetrics {
        /**
         * 時間戳
         */
        private LocalDateTime timestamp;

        /**
         * 平均響應時間（毫秒）
         */
        private Double averageResponseTime;

        /**
         * P50 響應時間（毫秒）
         */
        private Double p50ResponseTime;

        /**
         * P95 響應時間（毫秒）
         */
        private Double p95ResponseTime;

        /**
         * P99 響應時間（毫秒）
         */
        private Double p99ResponseTime;
    }

    /**
     * 健康狀態內部類
     */
    @lombok.Data
    @lombok.Builder
    public static class HealthStatus {
        /**
         * 時間戳
         */
        private LocalDateTime timestamp;

        /**
         * 狀態：HEALTHY, DEGRADED, OVERLOADED
         */
        private String status;

        /**
         * 狀態描述
         */
        private String message;

        /**
         * 成功率（%）
         */
        private Double successRate;

        /**
         * P99 響應時間（毫秒）
         */
        private Double p99ResponseTime;

        /**
         * 當前活躍查詢數
         */
        private Integer activeQueries;

        /**
         * 總查詢數
         */
        private Double totalQueries;
    }
}
