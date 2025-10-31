package com.example.advancedrag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedding 上下文
 *
 * 定義 Embedding 生成的上下文信息，用於智能模型選擇
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingContext {

    /**
     * 是否需要高精度
     */
    @Builder.Default
    private Boolean highAccuracyRequired = false;

    /**
     * 是否成本敏感
     */
    @Builder.Default
    private Boolean costSensitive = false;

    /**
     * 是否批量處理
     */
    @Builder.Default
    private Boolean batchProcessing = false;

    /**
     * 領域（可選）
     */
    private String domain;

    /**
     * 語言（可選）
     */
    @Builder.Default
    private String language = "zh";

    /**
     * 優先級（1-10，數字越大優先級越高）
     */
    @Builder.Default
    private Integer priority = 5;

    /**
     * 創建默認上下文
     */
    public static EmbeddingContext defaultContext() {
        return EmbeddingContext.builder().build();
    }

    /**
     * 創建高精度上下文
     */
    public static EmbeddingContext highAccuracy() {
        return EmbeddingContext.builder()
                .highAccuracyRequired(true)
                .costSensitive(false)
                .priority(10)
                .build();
    }

    /**
     * 創建成本優化上下文
     */
    public static EmbeddingContext costOptimized() {
        return EmbeddingContext.builder()
                .highAccuracyRequired(false)
                .costSensitive(true)
                .priority(3)
                .build();
    }

    /**
     * 創建批量處理上下文
     */
    public static EmbeddingContext batch() {
        return EmbeddingContext.builder()
                .batchProcessing(true)
                .costSensitive(true)
                .priority(5)
                .build();
    }
}
