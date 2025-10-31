package com.example.advancedrag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 評估測試請求
 *
 * 客戶端請求執行 RAG 系統評估測試
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequest {

    /**
     * 測試案例文件路徑列表（可選，不提供則使用默認測試集）
     */
    @Builder.Default
    private List<String> testCaseFiles = new ArrayList<>();

    /**
     * 測試類別過濾器（可選，例如：basic, domain-specific, edge-cases）
     */
    @Builder.Default
    private List<String> categoryFilters = new ArrayList<>();

    /**
     * 難度過濾器（可選，0-1 範圍）
     */
    private DifficultyFilter difficultyFilter;

    /**
     * 是否執行性能評估
     */
    @Builder.Default
    private Boolean evaluatePerformance = true;

    /**
     * 是否執行準確性評估
     */
    @Builder.Default
    private Boolean evaluateAccuracy = true;

    /**
     * 是否執行相關性評估
     */
    @Builder.Default
    private Boolean evaluateRelevance = true;

    /**
     * 是否使用 AI 輔助評估
     */
    @Builder.Default
    private Boolean useAIEvaluation = true;

    /**
     * 是否異步執行
     */
    @Builder.Default
    private Boolean async = false;

    /**
     * 並發執行數量
     */
    @Builder.Default
    private Integer concurrency = 5;

    /**
     * 是否生成詳細報告
     */
    @Builder.Default
    private Boolean generateDetailedReport = true;

    /**
     * 難度過濾器內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DifficultyFilter {
        /**
         * 最小難度（0-1）
         */
        @Builder.Default
        private Double minDifficulty = 0.0;

        /**
         * 最大難度（0-1）
         */
        @Builder.Default
        private Double maxDifficulty = 1.0;
    }

    /**
     * 添加測試案例文件
     */
    public void addTestCaseFile(String filePath) {
        if (this.testCaseFiles == null) {
            this.testCaseFiles = new ArrayList<>();
        }
        this.testCaseFiles.add(filePath);
    }

    /**
     * 添加類別過濾器
     */
    public void addCategoryFilter(String category) {
        if (this.categoryFilters == null) {
            this.categoryFilters = new ArrayList<>();
        }
        this.categoryFilters.add(category);
    }

    /**
     * 是否有類別過濾器
     */
    public boolean hasCategoryFilters() {
        return categoryFilters != null && !categoryFilters.isEmpty();
    }

    /**
     * 是否有難度過濾器
     */
    public boolean hasDifficultyFilter() {
        return difficultyFilter != null;
    }
}
