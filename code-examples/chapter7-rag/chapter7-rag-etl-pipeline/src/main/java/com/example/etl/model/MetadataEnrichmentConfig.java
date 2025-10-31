package com.example.etl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 元資料增強配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataEnrichmentConfig {
    /** 啟用基礎元資料 */
    @Builder.Default
    private boolean enableBasicMetadata = true;

    /** 啟用語言檢測 */
    @Builder.Default
    private boolean enableLanguageDetection = true;

    /** 啟用內容統計 */
    @Builder.Default
    private boolean enableContentStatistics = true;

    /** 啟用關鍵詞提取 */
    @Builder.Default
    private boolean enableKeywordExtraction = false;

    /** 關鍵詞數量 */
    @Builder.Default
    private int keywordCount = 5;

    /** 啟用摘要生成 */
    @Builder.Default
    private boolean enableSummaryGeneration = false;
}
