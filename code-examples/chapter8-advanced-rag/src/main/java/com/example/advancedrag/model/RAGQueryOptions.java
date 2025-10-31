package com.example.advancedrag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RAG 查詢選項
 *
 * 定義 RAG 查詢的各種配置參數
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGQueryOptions {

    /**
     * 最終返回的文檔數量
     */
    @Builder.Default
    private Integer finalTopK = 5;

    /**
     * 粗檢索的文檔數量
     */
    @Builder.Default
    private Integer coarseTopK = 30;

    /**
     * 相似度閾值（0-1）
     */
    @Builder.Default
    private Double similarityThreshold = 0.7;

    /**
     * 最大上下文長度
     */
    @Builder.Default
    private Integer maxContextLength = 4000;

    /**
     * 是否啟用 Re-ranking
     */
    @Builder.Default
    private Boolean enableReranking = true;

    /**
     * 是否啟用查詢重寫
     */
    @Builder.Default
    private Boolean enableQueryRewrite = true;

    /**
     * 是否啟用查詢擴展
     */
    @Builder.Default
    private Boolean enableQueryExpansion = false;

    /**
     * 查詢擴展數量
     */
    @Builder.Default
    private Integer queryExpansionCount = 3;
}
