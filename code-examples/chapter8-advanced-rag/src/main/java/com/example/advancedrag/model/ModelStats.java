package com.example.advancedrag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Embedding 模型統計數據
 *
 * 用於追蹤 Embedding 模型的使用情況和性能指標
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelStats {

    /**
     * 模型名稱
     */
    private String modelName;

    /**
     * 統計開始時間
     */
    @Builder.Default
    private LocalDateTime startTime = LocalDateTime.now();

    /**
     * 統計結束時間
     */
    private LocalDateTime endTime;

    /**
     * 使用次數
     */
    @Builder.Default
    private Long usageCount = 0L;

    /**
     * 成功次數
     */
    @Builder.Default
    private Long successCount = 0L;

    /**
     * 失敗次數
     */
    @Builder.Default
    private Long failureCount = 0L;

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
     * 最小處理時間（毫秒）
     */
    private Long minProcessingTime;

    /**
     * 最大處理時間（毫秒）
     */
    private Long maxProcessingTime;

    /**
     * 總處理文本長度
     */
    @Builder.Default
    private Long totalTextLength = 0L;

    /**
     * 平均文本長度
     */
    private Double avgTextLength;

    /**
     * 總 Token 消耗
     */
    @Builder.Default
    private Long totalTokens = 0L;

    /**
     * 估算成本（美元）
     */
    @Builder.Default
    private Double estimatedCost = 0.0;

    /**
     * 快取命中次數
     */
    @Builder.Default
    private Long cacheHits = 0L;

    /**
     * 快取未命中次數
     */
    @Builder.Default
    private Long cacheMisses = 0L;

    /**
     * 快取命中率
     */
    private Double cacheHitRate;

    /**
     * 成功率
     */
    private Double successRate;

    /**
     * 記錄一次成功調用
     */
    public void recordSuccess(long processingTime, int textLength, int tokens, boolean fromCache) {
        this.usageCount++;
        this.successCount++;
        this.totalProcessingTime += processingTime;
        this.totalTextLength += textLength;
        this.totalTokens += tokens;

        if (fromCache) {
            this.cacheHits++;
        } else {
            this.cacheMisses++;
        }

        // 更新最小/最大處理時間
        if (this.minProcessingTime == null || processingTime < this.minProcessingTime) {
            this.minProcessingTime = processingTime;
        }
        if (this.maxProcessingTime == null || processingTime > this.maxProcessingTime) {
            this.maxProcessingTime = processingTime;
        }

        // 計算統計指標
        calculateMetrics();
    }

    /**
     * 記錄一次失敗調用
     */
    public void recordFailure() {
        this.usageCount++;
        this.failureCount++;
        calculateMetrics();
    }

    /**
     * 計算統計指標
     */
    public void calculateMetrics() {
        if (usageCount > 0) {
            this.successRate = (double) successCount / usageCount * 100;
            this.avgProcessingTime = (double) totalProcessingTime / usageCount;
            this.avgTextLength = (double) totalTextLength / usageCount;

            long totalCacheRequests = cacheHits + cacheMisses;
            if (totalCacheRequests > 0) {
                this.cacheHitRate = (double) cacheHits / totalCacheRequests * 100;
            }
        }
    }

    /**
     * 估算成本（基於 token 數量）
     * text-embedding-3-small: $0.02 / 1M tokens
     * text-embedding-3-large: $0.13 / 1M tokens
     */
    public void estimateCost() {
        if ("text-embedding-3-small".equals(modelName)) {
            this.estimatedCost = (double) totalTokens / 1_000_000 * 0.02;
        } else if ("text-embedding-3-large".equals(modelName)) {
            this.estimatedCost = (double) totalTokens / 1_000_000 * 0.13;
        }
    }

    /**
     * 結束統計
     */
    public void finish() {
        this.endTime = LocalDateTime.now();
        calculateMetrics();
        estimateCost();
    }

    /**
     * 合併另一個統計數據
     */
    public void merge(ModelStats other) {
        if (!this.modelName.equals(other.modelName)) {
            throw new IllegalArgumentException("Cannot merge stats from different models");
        }

        this.usageCount += other.usageCount;
        this.successCount += other.successCount;
        this.failureCount += other.failureCount;
        this.totalProcessingTime += other.totalProcessingTime;
        this.totalTextLength += other.totalTextLength;
        this.totalTokens += other.totalTokens;
        this.cacheHits += other.cacheHits;
        this.cacheMisses += other.cacheMisses;

        // 更新最小/最大處理時間
        if (other.minProcessingTime != null &&
            (this.minProcessingTime == null || other.minProcessingTime < this.minProcessingTime)) {
            this.minProcessingTime = other.minProcessingTime;
        }
        if (other.maxProcessingTime != null &&
            (this.maxProcessingTime == null || other.maxProcessingTime > this.maxProcessingTime)) {
            this.maxProcessingTime = other.maxProcessingTime;
        }

        calculateMetrics();
        estimateCost();
    }
}
