package com.example.advancedrag.model;

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
 * Embedding 模型性能報告
 *
 * 綜合多個模型的統計數據，生成性能分析報告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelPerformanceReport {

    /**
     * 報告生成時間
     */
    @Builder.Default
    private LocalDateTime reportTime = LocalDateTime.now();

    /**
     * 報告時間範圍開始
     */
    private LocalDateTime startTime;

    /**
     * 報告時間範圍結束
     */
    private LocalDateTime endTime;

    /**
     * 各模型統計數據
     */
    @Builder.Default
    private Map<String, ModelStats> modelStats = new HashMap<>();

    /**
     * 整體統計數據
     */
    private OverallStats overallStats;

    /**
     * 推薦使用的模型（基於性能和成本平衡）
     */
    private String recommendedModel;

    /**
     * 推薦原因
     */
    private String recommendationReason;

    /**
     * 性能問題列表
     */
    @Builder.Default
    private List<String> performanceIssues = new ArrayList<>();

    /**
     * 優化建議列表
     */
    @Builder.Default
    private List<String> optimizationSuggestions = new ArrayList<>();

    /**
     * 整體統計數據內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallStats {
        /**
         * 總使用次數
         */
        @Builder.Default
        private Long totalUsageCount = 0L;

        /**
         * 總成功次數
         */
        @Builder.Default
        private Long totalSuccessCount = 0L;

        /**
         * 總失敗次數
         */
        @Builder.Default
        private Long totalFailureCount = 0L;

        /**
         * 整體成功率
         */
        private Double overallSuccessRate;

        /**
         * 總處理時間（毫秒）
         */
        @Builder.Default
        private Long totalProcessingTime = 0L;

        /**
         * 平均處理時間（毫秒）
         */
        private Double avgProcessingTime;

        /**
         * 總快取命中次數
         */
        @Builder.Default
        private Long totalCacheHits = 0L;

        /**
         * 總快取未命中次數
         */
        @Builder.Default
        private Long totalCacheMisses = 0L;

        /**
         * 整體快取命中率
         */
        private Double overallCacheHitRate;

        /**
         * 總成本（美元）
         */
        @Builder.Default
        private Double totalCost = 0.0;

        /**
         * 總 Token 消耗
         */
        @Builder.Default
        private Long totalTokens = 0L;
    }

    /**
     * 添加模型統計數據
     */
    public void addModelStats(String modelName, ModelStats stats) {
        modelStats.put(modelName, stats);
    }

    /**
     * 計算整體統計
     */
    public void calculateOverallStats() {
        OverallStats overall = OverallStats.builder().build();

        for (ModelStats stats : modelStats.values()) {
            overall.totalUsageCount += stats.getUsageCount();
            overall.totalSuccessCount += stats.getSuccessCount();
            overall.totalFailureCount += stats.getFailureCount();
            overall.totalProcessingTime += stats.getTotalProcessingTime();
            overall.totalCacheHits += stats.getCacheHits();
            overall.totalCacheMisses += stats.getCacheMisses();
            overall.totalCost += stats.getEstimatedCost();
            overall.totalTokens += stats.getTotalTokens();
        }

        if (overall.totalUsageCount > 0) {
            overall.overallSuccessRate = (double) overall.totalSuccessCount / overall.totalUsageCount * 100;
            overall.avgProcessingTime = (double) overall.totalProcessingTime / overall.totalUsageCount;
        }

        long totalCacheRequests = overall.totalCacheHits + overall.totalCacheMisses;
        if (totalCacheRequests > 0) {
            overall.overallCacheHitRate = (double) overall.totalCacheHits / totalCacheRequests * 100;
        }

        this.overallStats = overall;
    }

    /**
     * 分析並生成推薦
     */
    public void analyzeAndRecommend() {
        if (modelStats.isEmpty()) {
            return;
        }

        // 計算每個模型的綜合得分（性能 50% + 成本效益 30% + 可靠性 20%）
        Map<String, Double> modelScores = new HashMap<>();

        for (Map.Entry<String, ModelStats> entry : modelStats.entrySet()) {
            String modelName = entry.getKey();
            ModelStats stats = entry.getValue();

            // 性能得分（處理速度越快越好）
            double performanceScore = 0.0;
            if (stats.getAvgProcessingTime() != null && stats.getAvgProcessingTime() > 0) {
                performanceScore = Math.min(1.0, 1000.0 / stats.getAvgProcessingTime());
            }

            // 成本效益得分（成本越低越好）
            double costEfficiencyScore = 0.0;
            if (stats.getEstimatedCost() > 0 && stats.getUsageCount() > 0) {
                double costPerRequest = stats.getEstimatedCost() / stats.getUsageCount();
                costEfficiencyScore = Math.max(0.0, 1.0 - costPerRequest * 1000);
            }

            // 可靠性得分（成功率）
            double reliabilityScore = stats.getSuccessRate() != null ? stats.getSuccessRate() / 100.0 : 0.0;

            // 綜合得分
            double totalScore = performanceScore * 0.5 + costEfficiencyScore * 0.3 + reliabilityScore * 0.2;
            modelScores.put(modelName, totalScore);
        }

        // 找出得分最高的模型
        String bestModel = null;
        double bestScore = 0.0;

        for (Map.Entry<String, Double> entry : modelScores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestModel = entry.getKey();
            }
        }

        this.recommendedModel = bestModel;

        if (bestModel != null) {
            ModelStats bestStats = modelStats.get(bestModel);
            this.recommendationReason = String.format(
                    "基於性能、成本和可靠性的綜合評估，%s 是當前最佳選擇。" +
                    "平均處理時間: %.2f ms，成功率: %.2f%%，預估成本: $%.4f",
                    bestModel,
                    bestStats.getAvgProcessingTime(),
                    bestStats.getSuccessRate(),
                    bestStats.getEstimatedCost()
            );
        }
    }

    /**
     * 檢測性能問題
     */
    public void detectPerformanceIssues() {
        performanceIssues.clear();

        // 檢查整體成功率
        if (overallStats != null && overallStats.overallSuccessRate != null &&
            overallStats.overallSuccessRate < 95.0) {
            performanceIssues.add(String.format(
                    "整體成功率偏低 (%.2f%%)，建議檢查 API 配置和網路狀況",
                    overallStats.overallSuccessRate
            ));
        }

        // 檢查快取命中率
        if (overallStats != null && overallStats.overallCacheHitRate != null &&
            overallStats.overallCacheHitRate < 30.0) {
            performanceIssues.add(String.format(
                    "快取命中率偏低 (%.2f%%)，建議增加快取 TTL 或檢查快取策略",
                    overallStats.overallCacheHitRate
            ));
        }

        // 檢查各模型的性能
        for (Map.Entry<String, ModelStats> entry : modelStats.entrySet()) {
            String modelName = entry.getKey();
            ModelStats stats = entry.getValue();

            if (stats.getAvgProcessingTime() != null && stats.getAvgProcessingTime() > 2000) {
                performanceIssues.add(String.format(
                        "%s 平均處理時間過長 (%.2f ms)，建議考慮使用更快的模型",
                        modelName, stats.getAvgProcessingTime()
                ));
            }

            if (stats.getSuccessRate() != null && stats.getSuccessRate() < 90.0) {
                performanceIssues.add(String.format(
                        "%s 成功率偏低 (%.2f%%)，建議檢查模型配置",
                        modelName, stats.getSuccessRate()
                ));
            }
        }
    }

    /**
     * 生成優化建議
     */
    public void generateOptimizationSuggestions() {
        optimizationSuggestions.clear();

        if (overallStats == null) {
            return;
        }

        // 快取優化建議
        if (overallStats.overallCacheHitRate != null && overallStats.overallCacheHitRate < 50.0) {
            optimizationSuggestions.add("增加 Redis 快取的 TTL 時間以提高快取命中率");
            optimizationSuggestions.add("考慮對常見查詢進行預熱快取");
        }

        // 成本優化建議
        if (overallStats.totalCost > 10.0) {
            optimizationSuggestions.add("總成本較高，建議在非關鍵場景使用 text-embedding-3-small 降低成本");
        }

        // 性能優化建議
        if (overallStats.avgProcessingTime != null && overallStats.avgProcessingTime > 1000) {
            optimizationSuggestions.add("考慮啟用批次處理來提升整體處理速度");
            optimizationSuggestions.add("優化文本預處理流程以減少處理時間");
        }

        // 可靠性優化建議
        if (overallStats.overallSuccessRate != null && overallStats.overallSuccessRate < 95.0) {
            optimizationSuggestions.add("增加重試機制以提高整體成功率");
            optimizationSuggestions.add("實施熔斷器模式防止級聯故障");
        }
    }

    /**
     * 生成完整報告
     */
    public void generateReport() {
        calculateOverallStats();
        analyzeAndRecommend();
        detectPerformanceIssues();
        generateOptimizationSuggestions();
    }
}
