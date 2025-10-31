package com.example.advancedrag.controller;

import com.example.advancedrag.dto.ApiResponse;
import com.example.advancedrag.dto.SingleEvaluationRequest;
import com.example.advancedrag.dto.EvaluationResult;
import com.example.advancedrag.dto.EvaluationReport;
import com.example.advancedrag.service.RAGEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 評估測試控制器
 *
 * 提供 RAG 系統評估測試 REST API：
 * - POST /api/v1/evaluation/single - 單個問答對評估
 * - POST /api/v1/evaluation/batch - 批次問答對評估
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final RAGEvaluationService evaluationService;

    /**
     * 單個問答對評估
     *
     * @param request 評估請求
     * @return 評估結果
     */
    @PostMapping("/single")
    public ResponseEntity<ApiResponse<EvaluationResult>> evaluateSingle(
            @Valid @RequestBody SingleEvaluationRequest request) {

        log.info("收到單個評估請求，問題: {}", request.getQuestion());

        try {
            EvaluationResult result = evaluationService.evaluateSingle(request);

            return ResponseEntity.ok(
                    ApiResponse.success("評估完成", result)
            );

        } catch (Exception e) {
            log.error("評估失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("評估失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 批次問答對評估
     *
     * @param requests 評估請求列表
     * @return 評估報告
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<EvaluationReport>> evaluateBatch(
            @Valid @RequestBody List<SingleEvaluationRequest> requests) {

        log.info("收到批次評估請求，數量: {}", requests.size());

        try {
            if (requests == null || requests.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.badRequest("請求列表不能為空")
                );
            }

            EvaluationReport report = evaluationService.evaluateBatch(requests);

            return ResponseEntity.ok(
                    ApiResponse.success("批次評估完成", report)
            );

        } catch (Exception e) {
            log.error("批次評估失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("批次評估失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 獲取評估摘要統計
     *
     * @param report 評估報告
     * @return 摘要統計
     */
    @PostMapping("/summary")
    public ResponseEntity<ApiResponse<EvaluationSummary>> getEvaluationSummary(
            @RequestBody EvaluationReport report) {

        log.info("生成評估摘要");

        try {
            EvaluationSummary summary = EvaluationSummary.builder()
                    .totalTests(report.getTotalCount())
                    .passedTests(report.getPassedCount())
                    .failedTests(report.getFailedCount())
                    .passRate(report.getPassRate())
                    .averageAccuracy(report.getAverageAccuracy())
                    .averageRelevance(report.getAverageRelevance())
                    .averageCompleteness(report.getAverageCompleteness())
                    .averageOverallScore(report.getAverageOverallScore())
                    .processingTimeMs(report.getProcessingTimeMs())
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success("摘要生成成功", summary)
            );

        } catch (Exception e) {
            log.error("生成摘要失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("生成摘要失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 評估摘要內部類
     */
    @lombok.Data
    @lombok.Builder
    public static class EvaluationSummary {
        /**
         * 總測試數
         */
        private Integer totalTests;

        /**
         * 通過測試數
         */
        private Integer passedTests;

        /**
         * 失敗測試數
         */
        private Integer failedTests;

        /**
         * 通過率
         */
        private Double passRate;

        /**
         * 平均準確性
         */
        private Double averageAccuracy;

        /**
         * 平均相關性
         */
        private Double averageRelevance;

        /**
         * 平均完整性
         */
        private Double averageCompleteness;

        /**
         * 平均總分
         */
        private Double averageOverallScore;

        /**
         * 處理時間
         */
        private Long processingTimeMs;
    }
}
