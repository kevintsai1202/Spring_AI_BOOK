/**
 * Re-ranking Advisor 實現
 * 基於 Spring AI 現有 API 的自定義 Re-ranking 功能
 */
package com.example.advisor;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RerankingAdvisor implements CallAdvisor {
    
    private final EmbeddingModel rerankingModel;
    private final int finalTopK;
    private final RerankingMetrics metrics;
    
    public RerankingAdvisor(EmbeddingModel rerankingModel, int finalTopK) {
        this.rerankingModel = rerankingModel;
        this.finalTopK = finalTopK;
        this.metrics = new RerankingMetrics();
    }
    
    @Override
    public String getName() {
        return "RerankingAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 3;
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 取得原始檢索結果
            ChatClientResponse response = chain.nextCall(request);
            
            // 檢查是否有檢索到的文檔
            List<Document> retrievedDocs = extractRetrievedDocuments(response);
            if (retrievedDocs.isEmpty() || retrievedDocs.size() <= finalTopK) {
                log.debug("No re-ranking needed, document count: {}", retrievedDocs.size());
                return response;
            }
            
            // 執行重排序
            String query = extractUserQuery(request);
            List<Document> rerankedDocs = performReranking(query, retrievedDocs);
            
            // 更新回應中的文檔
            ChatClientResponse rerankedResponse = updateResponseWithRerankedDocs(
                response, rerankedDocs);
            
            // 記錄指標
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordReranking(retrievedDocs.size(), rerankedDocs.size(), duration);
            
            log.debug("Re-ranking completed: {} -> {} docs in {}ms", 
                retrievedDocs.size(), rerankedDocs.size(), duration);
            
            return rerankedResponse;
            
        } catch (Exception e) {
            log.warn("Re-ranking failed, using original order", e);
            return chain.nextCall(request);
        }
    }
    
    /**
     * 執行重排序
     */
    private List<Document> performReranking(String query, List<Document> documents) {
        
        List<RerankingCandidate> candidates = new ArrayList<>();
        
        // 使用 EmbeddingModel 計算查詢向量（創新實現）
        float[] queryEmbedding = rerankingModel.embed(query);
        
        // 為每個文檔計算重排序分數
        for (Document doc : documents) {
            try {
                // 語義相似度分數 - 基於 EmbeddingModel 的創新 Re-ranking 實現
                float[] contentEmbedding = rerankingModel.embed(doc.getContent());
                double semanticScore = calculateCosineSimilarity(queryEmbedding, contentEmbedding);
                
                // 計算其他特徵分數
                double lengthScore = calculateLengthScore(doc.getContent());
                double keywordScore = calculateKeywordScore(query, doc.getContent());
                double metadataScore = calculateMetadataScore(doc.getMetadata());
                
                // 綜合分數計算
                double finalScore = calculateFinalScore(
                    semanticScore, lengthScore, keywordScore, metadataScore);
                
                candidates.add(RerankingCandidate.builder()
                    .document(doc)
                    .semanticScore(semanticScore)
                    .lengthScore(lengthScore)
                    .keywordScore(keywordScore)
                    .metadataScore(metadataScore)
                    .finalScore(finalScore)
                    .build());
                    
            } catch (Exception e) {
                log.warn("Failed to calculate re-ranking score for document: {}", 
                    doc.getId(), e);
                // 使用原始分數
                candidates.add(RerankingCandidate.builder()
                    .document(doc)
                    .finalScore(doc.getScore())
                    .build());
            }
        }
        
        // 按最終分數排序並取前 K 個
        return candidates.stream()
            .sorted((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()))
            .limit(finalTopK)
            .map(RerankingCandidate::getDocument)
            .collect(Collectors.toList());
    }
    
    /**
     * 計算餘弦相似度
     */
    private double calculateCosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 計算長度分數
     */
    private double calculateLengthScore(String content) {
        int length = content.length();
        
        // 理想長度範圍：200-1000 字符
        if (length >= 200 && length <= 1000) {
            return 1.0;
        } else if (length < 200) {
            return length / 200.0;
        } else {
            return Math.max(0.5, 1000.0 / length);
        }
    }
    
    /**
     * 計算關鍵字分數
     */
    private double calculateKeywordScore(String query, String content) {
        String[] queryWords = query.toLowerCase().split("\\s+");
        String lowerContent = content.toLowerCase();
        
        int matchCount = 0;
        for (String word : queryWords) {
            if (lowerContent.contains(word)) {
                matchCount++;
            }
        }
        
        return queryWords.length > 0 ? (double) matchCount / queryWords.length : 0.0;
    }
    
    /**
     * 計算元數據分數
     */
    private double calculateMetadataScore(Map<String, Object> metadata) {
        double score = 0.5; // 基礎分數
        
        // 檢查文檔類型
        String docType = (String) metadata.get("type");
        if ("official".equals(docType) || "policy".equals(docType)) {
            score += 0.2;
        }
        
        // 檢查更新時間
        Object lastUpdated = metadata.get("lastUpdated");
        if (lastUpdated != null) {
            // 較新的文檔獲得更高分數
            score += 0.1;
        }
        
        // 檢查權威性
        Object authority = metadata.get("authority");
        if (authority != null && "high".equals(authority.toString())) {
            score += 0.2;
        }
        
        return Math.min(1.0, score);
    }
    
    /**
     * 計算最終分數
     */
    private double calculateFinalScore(double semanticScore, double lengthScore, 
                                     double keywordScore, double metadataScore) {
        
        // 權重配置
        double semanticWeight = 0.5;
        double lengthWeight = 0.2;
        double keywordWeight = 0.2;
        double metadataWeight = 0.1;
        
        return semanticScore * semanticWeight +
               lengthScore * lengthWeight +
               keywordScore * keywordWeight +
               metadataScore * metadataWeight;
    }
    
    /**
     * 提取檢索到的文檔
     */
    private List<Document> extractRetrievedDocuments(ChatClientResponse response) {
        // 從回應中提取文檔的邏輯
        // 這裡需要根據實際的 Spring AI API 實現
        return Collections.emptyList(); // 簡化實現
    }
    
    /**
     * 提取用戶查詢
     */
    private String extractUserQuery(ChatClientRequest request) {
        // 從請求中提取查詢的邏輯
        return request.getUserText(); // 簡化實現
    }
    
    /**
     * 更新回應中的文檔
     */
    private ChatClientResponse updateResponseWithRerankedDocs(
            ChatClientResponse response, List<Document> rerankedDocs) {
        // 更新回應中文檔順序的邏輯
        return response; // 簡化實現
    }
    
    /**
     * Re-ranking 候選文檔
     */
    @Data
    @Builder
    public static class RerankingCandidate {
        private Document document;
        private double semanticScore;
        private double lengthScore;
        private double keywordScore;
        private double metadataScore;
        private double finalScore;
    }
    
    /**
     * Re-ranking 指標收集
     */
    public static class RerankingMetrics {
        private long totalRerankings = 0;
        private long totalProcessingTime = 0;
        
        /**
         * 記錄 Re-ranking 指標
         */
        public void recordReranking(int originalCount, int finalCount, long processingTime) {
            
            totalRerankings++;
            totalProcessingTime += processingTime;
            
            // 記錄壓縮比例
            double compressionRatio = originalCount > 0 ? 
                (double) finalCount / originalCount : 0.0;
            
            log.debug("Recorded re-ranking metrics: {} -> {} docs, {}ms, ratio: {:.2f}",
                originalCount, finalCount, processingTime, compressionRatio);
        }
        
        /**
         * 獲取平均處理時間
         */
        public double getAverageProcessingTime() {
            return totalRerankings > 0 ? (double) totalProcessingTime / totalRerankings : 0.0;
        }
        
        /**
         * 獲取 Re-ranking 統計報告
         */
        public RerankingReport getReport() {
            return RerankingReport.builder()
                .totalRerankings(totalRerankings)
                .averageProcessingTime(getAverageProcessingTime())
                .totalProcessingTime(totalProcessingTime)
                .build();
        }
    }
    
    /**
     * Re-ranking 報告
     */
    @Data
    @Builder
    public static class RerankingReport {
        private long totalRerankings;
        private double averageProcessingTime;
        private long totalProcessingTime;
    }
}