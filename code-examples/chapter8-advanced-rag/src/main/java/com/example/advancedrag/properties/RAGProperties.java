package com.example.advancedrag.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RAG 系統配置屬性
 *
 * 綁定 application.yml 中的 app.rag 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.rag")
public class RAGProperties {

    /**
     * 最終返回的文檔數量
     */
    private Integer finalTopK = 5;

    /**
     * 粗檢索的文檔數量
     */
    private Integer coarseTopK = 30;

    /**
     * 相似度閾值（0-1）
     */
    private Double similarityThreshold = 0.7;

    /**
     * 最大上下文長度
     */
    private Integer maxContextLength = 4000;

    /**
     * 是否啟用 Re-ranking
     */
    private Boolean enableReranking = true;

    /**
     * 是否啟用查詢重寫
     */
    private Boolean enableQueryRewrite = true;

    /**
     * 是否啟用查詢擴展
     */
    private Boolean enableQueryExpansion = true;

    /**
     * 查詢擴展數量
     */
    private Integer queryExpansionCount = 3;

    /**
     * OpenAI API Key（用於內容審核）
     */
    private String openaiApiKey;

    /**
     * Re-ranking 配置
     */
    private Reranking reranking = new Reranking();

    /**
     * Re-ranking 配置類
     */
    @Data
    public static class Reranking {
        /**
         * 是否啟用 Re-ranking
         */
        private Boolean enabled = true;

        /**
         * Re-ranking 提供者：voyage, local, cohere, jina
         */
        private String provider = "local";

        /**
         * API Key（Voyage AI、Cohere、Jina 需要）
         */
        private String apiKey;

        /**
         * Re-ranking 模型名稱
         */
        private String model = "rerank-1";

        /**
         * 第一階段粗檢索的文檔數量（50-100）
         */
        private Integer firstStageTopK = 50;

        /**
         * 最終返回的文檔數量（通常 5-10）
         */
        private Integer finalTopK = 5;

        /**
         * 是否在響應中包含評分詳情
         */
        private Boolean includeScoreDetails = false;
    }
}
