package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文檔分類結果
 *
 * 封裝文檔分類的結果和置信度
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassificationResult {

    /**
     * 分類類別
     */
    private String category;

    /**
     * 置信度（0.0 - 1.0）
     */
    private double confidence;

    /**
     * 額外資訊
     */
    private String additionalInfo;
}
