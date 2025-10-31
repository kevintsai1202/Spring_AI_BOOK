package com.example.advancedrag.reranking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.document.Document;

/**
 * Re-ranking 結果
 *
 * 包含重新排序後的文檔和相關評分信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RerankResult {

    /**
     * 原始文檔
     */
    private Document document;

    /**
     * 原始索引位置
     */
    private Integer originalIndex;

    /**
     * 重排後的索引位置
     */
    private Integer newIndex;

    /**
     * 相關性分數（0-1）
     */
    private Double relevanceScore;

    /**
     * 提供者名稱
     */
    private String providerName;

    /**
     * 文檔內容（可選，用於調試）
     */
    private String content;

    /**
     * 從 Document 創建結果
     *
     * @param document 文檔
     * @param index 索引
     * @param score 分數
     * @param providerName 提供者名稱
     * @return RerankResult
     */
    public static RerankResult from(Document document, int index, double score, String providerName) {
        return RerankResult.builder()
                .document(document)
                .originalIndex(index)
                .newIndex(index)
                .relevanceScore(score)
                .providerName(providerName)
                .content(document.getText())
                .build();
    }
}
