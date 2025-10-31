package com.example.advancedrag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced RAG 查詢響應
 *
 * 返回給客戶端的 RAG 查詢結果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedRAGResponse {

    /**
     * 原始查詢問題
     */
    private String originalQuery;

    /**
     * 重寫後的查詢（如果啟用查詢重寫）
     */
    private String rewrittenQuery;

    /**
     * 擴展的查詢列表（如果啟用查詢擴展）
     */
    @Builder.Default
    private List<String> expandedQueries = new ArrayList<>();

    /**
     * 生成的答案
     */
    private String answer;

    /**
     * 檢索到的文檔列表
     */
    @Builder.Default
    private List<RetrievedDocument> documents = new ArrayList<>();

    /**
     * 檢索到的文檔數量
     */
    private Integer documentCount;

    /**
     * 處理時間（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 檢索階段耗時（毫秒）
     */
    private Long retrievalTimeMs;

    /**
     * Re-ranking 階段耗時（毫秒）
     */
    private Long rerankingTimeMs;

    /**
     * 生成階段耗時（毫秒）
     */
    private Long generationTimeMs;

    /**
     * 是否通過內容審核
     */
    private Boolean moderationPassed;

    /**
     * 審核風險分數（0-1）
     */
    private Double moderationRiskScore;

    /**
     * 會話 ID
     */
    private String sessionId;

    /**
     * 查詢 ID（用於追蹤）
     */
    private String queryId;

    /**
     * 時間戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 評分細節（如果請求返回評分細節）
     */
    @Builder.Default
    private Map<String, Object> scoringDetails = new HashMap<>();

    /**
     * 額外的元數據
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 檢索到的文檔內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievedDocument {
        /**
         * 文檔 ID
         */
        private String documentId;

        /**
         * 文檔內容
         */
        private String content;

        /**
         * 文檔來源
         */
        private String source;

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
         * 品質分數
         */
        private Double qualityScore;

        /**
         * 新鮮度分數
         */
        private Double freshnessScore;

        /**
         * 排名
         */
        private Integer rank;

        /**
         * 文檔元數據
         */
        @Builder.Default
        private Map<String, Object> metadata = new HashMap<>();
    }

    /**
     * 添加文檔
     */
    public void addDocument(RetrievedDocument document) {
        this.documents.add(document);
        this.documentCount = this.documents.size();
    }

    /**
     * 添加評分細節
     */
    public void addScoringDetail(String key, Object value) {
        this.scoringDetails.put(key, value);
    }

    /**
     * 添加元數據
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
}
