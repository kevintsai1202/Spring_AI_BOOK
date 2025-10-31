package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher.SummaryType;

import java.util.List;

/**
 * 元資料增強配置類
 *
 * 定義元資料增強的各種選項
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataEnrichmentConfig {

    /**
     * 是否啟用基礎元資料（時間戳、哈希等）
     */
    @Builder.Default
    private boolean enableBasicMetadata = true;

    /**
     * 是否啟用語言檢測
     */
    @Builder.Default
    private boolean enableLanguageDetection = true;

    /**
     * 是否啟用內容統計（字數、句子數等）
     */
    @Builder.Default
    private boolean enableContentStatistics = true;

    /**
     * 是否啟用 AI 關鍵詞提取
     */
    @Builder.Default
    private boolean enableKeywordExtraction = true;

    /**
     * 提取的關鍵詞數量
     */
    @Builder.Default
    private int keywordCount = 5;

    /**
     * 是否啟用 AI 摘要生成
     */
    @Builder.Default
    private boolean enableSummaryGeneration = true;

    /**
     * 摘要類型列表
     */
    private List<SummaryType> summaryTypes;

    /**
     * 是否啟用自定義分類
     */
    @Builder.Default
    private boolean enableCustomClassification = false;

    /**
     * 自定義分類器列表
     */
    private List<DocumentClassifier> customClassifiers;
}
