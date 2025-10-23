/**
 * RAG 檢索服務
 * 負責從向量存儲中檢索相關文檔
 */
package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RAGRetrievalService {
    
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    
    public List<Document> retrieveRelevantDocuments(
            ProcessedQuery query, 
            int topK, 
            double threshold) {
        
        // 1. 生成查詢向量
        float[] queryEmbedding = embeddingModel.embed(query.getCleanQuery());
        
        // 2. 向量相似性搜尋
        SearchRequest searchRequest = SearchRequest.builder()
            .query(query.getCleanQuery())
            .topK(topK)
            .similarityThreshold(threshold)
            .build();
        
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        
        // 3. 結果後處理
        return postProcessResults(results, query);
    }
    
    private List<Document> postProcessResults(List<Document> results, ProcessedQuery query) {
        return results.stream()
            .filter(doc -> isRelevant(doc, query))
            .sorted(Comparator.comparingDouble(this::calculateRelevanceScore).reversed())
            .collect(Collectors.toList());
    }
    
    private boolean isRelevant(Document document, ProcessedQuery query) {
        // 簡單的相關性檢查
        String content = document.getContent().toLowerCase();
        String queryText = query.getCleanQuery().toLowerCase();
        
        // 檢查是否包含查詢關鍵字
        String[] queryWords = queryText.split("\\s+");
        int matchCount = 0;
        
        for (String word : queryWords) {
            if (content.contains(word)) {
                matchCount++;
            }
        }
        
        // 至少要匹配一半的關鍵字
        return matchCount >= queryWords.length / 2;
    }
    
    private double calculateRelevanceScore(Document document) {
        // 簡單的相關性評分
        // 實際實現會考慮更多因素，如文檔新鮮度、權威性等
        
        double score = 1.0;
        
        // 根據文檔長度調整分數
        int contentLength = document.getContent().length();
        if (contentLength > 500 && contentLength < 2000) {
            score += 0.1;  // 中等長度的文檔可能更相關
        }
        
        // 根據元資料調整分數
        Object timestamp = document.getMetadata().get("timestamp");
        if (timestamp != null) {
            // 較新的文檔得分更高
            long time = (Long) timestamp;
            long currentTime = System.currentTimeMillis();
            long daysDiff = (currentTime - time) / (1000 * 60 * 60 * 24);
            
            if (daysDiff < 30) {
                score += 0.2;
            } else if (daysDiff < 90) {
                score += 0.1;
            }
        }
        
        return score;
    }
    
    /**
     * 處理後的查詢物件
     */
    public static class ProcessedQuery {
        private final String originalQuery;
        private final String cleanQuery;
        private final List<String> expandedQueries;
        private final QueryIntent intent;
        
        public ProcessedQuery(String originalQuery, String cleanQuery, 
                            List<String> expandedQueries, QueryIntent intent) {
            this.originalQuery = originalQuery;
            this.cleanQuery = cleanQuery;
            this.expandedQueries = expandedQueries;
            this.intent = intent;
        }
        
        public String getOriginalQuery() { return originalQuery; }
        public String getCleanQuery() { return cleanQuery; }
        public List<String> getExpandedQueries() { return expandedQueries; }
        public QueryIntent getIntent() { return intent; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String originalQuery;
            private String cleanQuery;
            private List<String> expandedQueries;
            private QueryIntent intent;
            
            public Builder originalQuery(String originalQuery) {
                this.originalQuery = originalQuery;
                return this;
            }
            
            public Builder cleanQuery(String cleanQuery) {
                this.cleanQuery = cleanQuery;
                return this;
            }
            
            public Builder expandedQueries(List<String> expandedQueries) {
                this.expandedQueries = expandedQueries;
                return this;
            }
            
            public Builder intent(QueryIntent intent) {
                this.intent = intent;
                return this;
            }
            
            public ProcessedQuery build() {
                return new ProcessedQuery(originalQuery, cleanQuery, expandedQueries, intent);
            }
        }
    }
    
    /**
     * 查詢意圖枚舉
     */
    public enum QueryIntent {
        SEARCH,      // 搜尋資訊
        QUESTION,    // 問答
        SUMMARY,     // 摘要
        COMPARISON,  // 比較
        EXPLANATION  // 解釋
    }
}