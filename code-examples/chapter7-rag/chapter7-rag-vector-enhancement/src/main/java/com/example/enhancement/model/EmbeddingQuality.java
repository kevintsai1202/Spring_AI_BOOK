package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向量品質評估結果
 *
 * 包含向量的各種品質指標和驗證結果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddingQuality {

    /**
     * 向量維度
     */
    private int dimension;

    /**
     * 向量範數（L2 norm）
     */
    private double norm;

    /**
     * 向量平均值
     */
    private double mean;

    /**
     * 向量標準差
     */
    private double standardDeviation;

    /**
     * 品質分數（0.0 - 1.0）
     */
    private double qualityScore;

    /**
     * 是否為有效向量
     */
    private boolean isValid;

    /**
     * 維度是否有效
     */
    private boolean validDimension;

    /**
     * 數值範圍是否有效
     */
    private boolean validRange;

    /**
     * 範數是否有效
     */
    private boolean validNorm;

    /**
     * 是否為非零向量
     */
    private boolean notZeroVector;

    /**
     * 文本長度是否有效
     */
    private boolean validTextLength;

    /**
     * 向量最小值
     */
    private double minValue;

    /**
     * 向量最大值
     */
    private double maxValue;

    /**
     * 驗證訊息
     */
    private String validationMessage;
}
