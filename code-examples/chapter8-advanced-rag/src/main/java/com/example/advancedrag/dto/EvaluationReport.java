package com.example.advancedrag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 評估測試報告
 *
 * 返回給客戶端的完整評估報告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationReport {

    /**
     * 報告生成時間
     */
    @Builder.Default
    private LocalDateTime reportTime = LocalDateTime.now();

    /**
     * 測試開始時間
     */
    private LocalDateTime testStartTime;

    /**
     * 測試結束時間
     */
    private LocalDateTime testEndTime;

    /**
     * 總測試案例數
     */
    private Integer totalCount;

    /**
     * 通過的測試案例數
     */
    private Integer passedCount;

    /**
     * 失敗的測試案例數
     */
    private Integer failedCount;

    /**
     * 總測試案例數（別名）
     */
    private Integer totalTestCases;

    /**
     * 通過的測試案例數（別名）
     */
    private Integer passedTestCases;

    /**
     * 失敗的測試案例數（別名）
     */
    private Integer failedTestCases;

    /**
     * 整體通過率（%）
     */
    private Double passRate;

    /**
     * 整體性能指標
     */
    private PerformanceMetrics performanceMetrics;

    /**
     * 整體準確性指標
     */
    private AccuracyMetrics accuracyMetrics;

    /**
     * 整體相關性指標
     */
    private RelevanceMetrics relevanceMetrics;

    /**
     * 各類別測試結果
     */
    @Builder.Default
    private Map<String, CategoryResult> categoryResults = new HashMap<>();

    /**
     * 各難度測試結果
     */
    @Builder.Default
    private Map<String, DifficultyResult> difficultyResults = new HashMap<>();

    /**
     * 詳細測試結果列表
     */
    @Builder.Default
    private List<EvaluationResult> detailedResults = new ArrayList<>();

    /**
     * 詳細測試結果列表（別名）
     */
    @Builder.Default
    private List<EvaluationResult> results = new ArrayList<>();

    /**
     * 平均準確性分數
     */
    private Double averageAccuracy;

    /**
     * 平均相關性分數
     */
    private Double averageRelevance;

    /**
     * 平均完整性分數
     */
    private Double averageCompleteness;

    /**
     * 平均總分
     */
    private Double averageOverallScore;

    /**
     * 處理時間（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 評估時間
     */
    private LocalDateTime evaluatedAt;

    /**
     * 發現的問題列表
     */
    @Builder.Default
    private List<String> issues = new ArrayList<>();

    /**
     * 改進建議列表
     */
    @Builder.Default
    private List<String> recommendations = new ArrayList<>();

    /**
     * 總結
     */
    private String summary;

    /**
     * 性能指標內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        /**
         * 平均響應時間（毫秒）
         */
        private Double avgResponseTime;

        /**
         * 最小響應時間（毫秒）
         */
        private Long minResponseTime;

        /**
         * 最大響應時間（毫秒）
         */
        private Long maxResponseTime;

        /**
         * P50 響應時間（毫秒）
         */
        private Long p50ResponseTime;

        /**
         * P95 響應時間（毫秒）
         */
        private Long p95ResponseTime;

        /**
         * P99 響應時間（毫秒）
         */
        private Long p99ResponseTime;

        /**
         * 平均檢索時間（毫秒）
         */
        private Double avgRetrievalTime;

        /**
         * 平均生成時間（毫秒）
         */
        private Double avgGenerationTime;
    }

    /**
     * 準確性指標內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccuracyMetrics {
        /**
         * 平均準確性分數（0-1）
         */
        private Double avgAccuracyScore;

        /**
         * 關鍵詞匹配率（%）
         */
        private Double keywordMatchRate;

        /**
         * 上下文相關性分數（0-1）
         */
        private Double contextRelevanceScore;

        /**
         * 答案完整性分數（0-1）
         */
        private Double answerCompletenessScore;
    }

    /**
     * 相關性指標內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelevanceMetrics {
        /**
         * 平均相關性分數（0-1）
         */
        private Double avgRelevanceScore;

        /**
         * 文檔相關性分數（0-1）
         */
        private Double documentRelevanceScore;

        /**
         * 答案相關性分數（0-1）
         */
        private Double answerRelevanceScore;

        /**
         * 平均檢索文檔數量
         */
        private Double avgRetrievedDocuments;
    }

    /**
     * 類別結果內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryResult {
        /**
         * 類別名稱
         */
        private String category;

        /**
         * 測試案例數
         */
        private Integer testCaseCount;

        /**
         * 通過的測試案例數
         */
        private Integer passedCount;

        /**
         * 失敗的測試案例數
         */
        private Integer failedCount;

        /**
         * 通過率（%）
         */
        private Double passRate;

        /**
         * 平均分數
         */
        private Double avgScore;
    }

    /**
     * 難度結果內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DifficultyResult {
        /**
         * 難度級別（easy, medium, hard）
         */
        private String difficultyLevel;

        /**
         * 測試案例數
         */
        private Integer testCaseCount;

        /**
         * 通過的測試案例數
         */
        private Integer passedCount;

        /**
         * 失敗的測試案例數
         */
        private Integer failedCount;

        /**
         * 通過率（%）
         */
        private Double passRate;

        /**
         * 平均分數
         */
        private Double avgScore;
    }

    /**
     * 添加詳細結果
     */
    public void addDetailedResult(EvaluationResult result) {
        if (this.detailedResults == null) {
            this.detailedResults = new ArrayList<>();
        }
        this.detailedResults.add(result);
    }

    /**
     * 添加類別結果
     */
    public void addCategoryResult(String category, CategoryResult result) {
        if (this.categoryResults == null) {
            this.categoryResults = new HashMap<>();
        }
        this.categoryResults.put(category, result);
    }

    /**
     * 添加難度結果
     */
    public void addDifficultyResult(String level, DifficultyResult result) {
        if (this.difficultyResults == null) {
            this.difficultyResults = new HashMap<>();
        }
        this.difficultyResults.put(level, result);
    }

    /**
     * 添加問題
     */
    public void addIssue(String issue) {
        if (this.issues == null) {
            this.issues = new ArrayList<>();
        }
        this.issues.add(issue);
    }

    /**
     * 添加建議
     */
    public void addRecommendation(String recommendation) {
        if (this.recommendations == null) {
            this.recommendations = new ArrayList<>();
        }
        this.recommendations.add(recommendation);
    }

    /**
     * 計算統計數據
     */
    public void calculateStatistics() {
        if (totalTestCases != null && totalTestCases > 0) {
            this.passRate = (double) passedTestCases / totalTestCases * 100;
        }
    }
}
