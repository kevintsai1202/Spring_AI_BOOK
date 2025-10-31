package com.example.advancedrag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.document.Document;

/**
 * 評分文檔
 *
 * 包含文檔和多維度評分的數據結構
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoredDocument {

    /**
     * 文檔對象
     */
    private Document document;

    /**
     * 綜合分數
     */
    private Double score;

    /**
     * 語義相似度分數
     */
    private Double semanticScore;

    /**
     * BM25 分數
     */
    private Double bm25Score;

    /**
     * 文檔品質分數
     */
    private Double qualityScore;

    /**
     * 新鮮度分數
     */
    private Double freshnessScore;

    /**
     * 創建只有基本分數的 ScoredDocument
     */
    public static ScoredDocument of(Document document, Double score) {
        return ScoredDocument.builder()
                .document(document)
                .score(score)
                .build();
    }
}
