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
 * 單個評估測試結果
 *
 * 單個測試案例的詳細評估結果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResult {

    /**
     * 測試案例 ID
     */
    private String testCaseId;

    /**
     * 測試問題
     */
    private String question;

    /**
     * 生成的答案
     */
    private String generatedAnswer;

    /**
     * 期望的答案（如果有）
     */
    private String expectedAnswer;

    /**
     * Ground Truth（參考答案）- 用於評估
     */
    private String groundTruth;

    /**
     * 處理時間（毫秒）- 評估所需時間
     */
    private Long processingTimeMs;

    /**
     * 評估時間
     */
    private LocalDateTime evaluatedAt;

    /**
     * 是否通過
     */
    private Boolean passed;

    /**
     * 綜合分數（0-1）
     */
    private Double overallScore;

    /**
     * 準確性分數（0-1）
     */
    private Double accuracyScore;

    /**
     * 相關性分數（0-1）
     */
    private Double relevanceScore;

    /**
     * 完整性分數（0-1）
     */
    private Double completenessScore;

    /**
     * 關鍵詞匹配情況
     */
    private KeywordMatchResult keywordMatchResult;

    /**
     * 檢索到的文檔數量
     */
    private Integer retrievedDocumentCount;

    /**
     * 檢索到的文檔列表
     */
    @Builder.Default
    private List<String> retrievedDocuments = new ArrayList<>();

    /**
     * 響應時間（毫秒）
     */
    private Long responseTimeMs;

    /**
     * 檢索時間（毫秒）
     */
    private Long retrievalTimeMs;

    /**
     * 生成時間（毫秒）
     */
    private Long generationTimeMs;

    /**
     * AI 評估結果
     */
    private AIEvaluationResult aiEvaluationResult;

    /**
     * 測試類別
     */
    private String category;

    /**
     * 測試難度
     */
    private Double difficulty;

    /**
     * 測試時間
     */
    @Builder.Default
    private LocalDateTime testTime = LocalDateTime.now();

    /**
     * 錯誤信息（如果測試失敗）
     */
    private String errorMessage;

    /**
     * 額外的元數據
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 關鍵詞匹配結果內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeywordMatchResult {
        /**
         * 期望的關鍵詞列表
         */
        @Builder.Default
        private List<String> expectedKeywords = new ArrayList<>();

        /**
         * 匹配到的關鍵詞列表
         */
        @Builder.Default
        private List<String> matchedKeywords = new ArrayList<>();

        /**
         * 未匹配到的關鍵詞列表
         */
        @Builder.Default
        private List<String> missedKeywords = new ArrayList<>();

        /**
         * 匹配率（%）
         */
        private Double matchRate;

        /**
         * 計算匹配率
         */
        public void calculateMatchRate() {
            if (expectedKeywords != null && !expectedKeywords.isEmpty()) {
                int matchedCount = matchedKeywords != null ? matchedKeywords.size() : 0;
                this.matchRate = (double) matchedCount / expectedKeywords.size() * 100;
            }
        }
    }

    /**
     * AI 評估結果內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIEvaluationResult {
        /**
         * AI 評估分數（0-1）
         */
        private Double score;

        /**
         * AI 評估意見
         */
        private String feedback;

        /**
         * 優點列表
         */
        @Builder.Default
        private List<String> strengths = new ArrayList<>();

        /**
         * 缺點列表
         */
        @Builder.Default
        private List<String> weaknesses = new ArrayList<>();

        /**
         * 改進建議列表
         */
        @Builder.Default
        private List<String> suggestions = new ArrayList<>();

        /**
         * AI 評估的詳細評分
         */
        @Builder.Default
        private Map<String, Double> detailedScores = new HashMap<>();
    }

    /**
     * 添加檢索到的文檔
     */
    public void addRetrievedDocument(String documentId) {
        if (this.retrievedDocuments == null) {
            this.retrievedDocuments = new ArrayList<>();
        }
        this.retrievedDocuments.add(documentId);
    }

    /**
     * 添加元數據
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    /**
     * 計算綜合分數
     * 準確性 40% + 相關性 30% + 完整性 30%
     */
    public void calculateOverallScore() {
        double accuracy = accuracyScore != null ? accuracyScore : 0.0;
        double relevance = relevanceScore != null ? relevanceScore : 0.0;
        double completeness = completenessScore != null ? completenessScore : 0.0;

        this.overallScore = accuracy * 0.4 + relevance * 0.3 + completeness * 0.3;
    }

    /**
     * 判斷是否為高分（>= 0.8）
     */
    public boolean isHighScore() {
        return overallScore != null && overallScore >= 0.8;
    }

    /**
     * 判斷是否為中等分數（0.6 - 0.8）
     */
    public boolean isMediumScore() {
        return overallScore != null && overallScore >= 0.6 && overallScore < 0.8;
    }

    /**
     * 判斷是否為低分（< 0.6）
     */
    public boolean isLowScore() {
        return overallScore != null && overallScore < 0.6;
    }
}
