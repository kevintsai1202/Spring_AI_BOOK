package com.example.advancedrag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.document.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Re-ranking 候選文檔
 *
 * 用於多階段檢索的 Re-ranking 階段，包含詳細的評分細節
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RerankingCandidate {

    /**
     * 原始文檔
     */
    private Document document;

    /**
     * 原始檢索分數（語義相似度）
     */
    private Double originalScore;

    /**
     * 語義相似度分數（佔比 40%）
     */
    private Double semanticScore;

    /**
     * BM25 分數（佔比 30%）
     */
    private Double bm25Score;

    /**
     * 文檔品質分數（佔比 20%）
     */
    private Double qualityScore;

    /**
     * 新鮮度分數（佔比 10%）
     */
    private Double freshnessScore;

    /**
     * 最終綜合分數
     */
    private Double finalScore;

    /**
     * 排名（Re-ranking 後的位置）
     */
    private Integer rank;

    /**
     * 詳細評分細節
     */
    @Builder.Default
    private Map<String, Object> scoringDetails = new HashMap<>();

    /**
     * 從 ScoredDocument 創建 RerankingCandidate
     */
    public static RerankingCandidate fromScoredDocument(ScoredDocument scoredDoc) {
        return RerankingCandidate.builder()
                .document(scoredDoc.getDocument())
                .originalScore(scoredDoc.getScore())
                .semanticScore(scoredDoc.getSemanticScore())
                .bm25Score(scoredDoc.getBm25Score())
                .qualityScore(scoredDoc.getQualityScore())
                .freshnessScore(scoredDoc.getFreshnessScore())
                .finalScore(scoredDoc.getScore())
                .build();
    }

    /**
     * 計算最終分數（多因子加權）
     * 語義相似度 40% + BM25 30% + 品質 20% + 新鮮度 10%
     */
    public void calculateFinalScore() {
        double semantic = semanticScore != null ? semanticScore : 0.0;
        double bm25 = bm25Score != null ? bm25Score : 0.0;
        double quality = qualityScore != null ? qualityScore : 0.5; // 默認中等品質
        double freshness = freshnessScore != null ? freshnessScore : 0.5; // 默認中等新鮮度

        this.finalScore = semantic * 0.4 + bm25 * 0.3 + quality * 0.2 + freshness * 0.1;

        // 保存評分細節
        scoringDetails.put("semantic_contribution", semantic * 0.4);
        scoringDetails.put("bm25_contribution", bm25 * 0.3);
        scoringDetails.put("quality_contribution", quality * 0.2);
        scoringDetails.put("freshness_contribution", freshness * 0.1);
    }

    /**
     * 添加評分細節
     */
    public void addScoringDetail(String key, Object value) {
        scoringDetails.put(key, value);
    }
}
