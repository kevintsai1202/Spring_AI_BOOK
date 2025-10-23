/**
 * RAG 查詢效能優化服務
 * 負責查詢分析、優化和執行策略
 */
package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryOptimizationService {
    
    private final VectorStoreService vectorStoreService;
    private final CacheManager cacheManager;
    private final QueryAnalyzer queryAnalyzer;
    private final PerformanceMetrics performanceMetrics;
    
    /**
     * 優化查詢執行
     */
    public QueryResult optimizeQuery(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 查詢分析和預處理
            OptimizedQuery optimizedQuery = analyzeAndOptimizeQuery(request);
            
            // 2. 快取檢查
            QueryResult cachedResult = checkCache(optimizedQuery);
            if (cachedResult != null) {
                performanceMetrics.recordCacheHit(request.getQueryId());
                return cachedResult;
            }
            
            // 3. 執行優化查詢
            QueryResult result = executeOptimizedQuery(optimizedQuery);
            
            // 4. 結果快取
            cacheResult(optimizedQuery, result);
            
            // 5. 記錄效能指標
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMetrics.recordQueryExecution(request.getQueryId(), executionTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("Query optimization failed for: {}", request.getQueryId(), e);
            performanceMetrics.recordQueryError(request.getQueryId());
            throw new QueryOptimizationException("Query optimization failed", e);
        }
    }
    
    /**
     * 分析和優化查詢
     */
    private OptimizedQuery analyzeAndOptimizeQuery(QueryRequest request) {
        QueryAnalysisResult analysis = queryAnalyzer.analyze(request);
        
        return OptimizedQuery.builder()
            .originalQuery(request.getQuery())
            .optimizedQuery(optimizeQueryText(request.getQuery(), analysis))
            .searchStrategy(determineSearchStrategy(analysis))
            .filterCriteria(optimizeFilters(request.getFilters(), analysis))
            .resultLimit(optimizeResultLimit(request.getLimit(), analysis))
            .similarityThreshold(optimizeSimilarityThreshold(analysis))
            .build();
    }
    
    /**
     * 優化查詢文本
     */
    private String optimizeQueryText(String originalQuery, QueryAnalysisResult analysis) {
        String optimized = originalQuery;
        
        // 1. 移除停用詞
        if (analysis.hasStopWords()) {
            optimized = removeStopWords(optimized, analysis.getLanguage());
        }
        
        // 2. 詞幹提取
        if (analysis.needsStemming()) {
            optimized = applyStemming(optimized, analysis.getLanguage());
        }
        
        // 3. 同義詞擴展
        if (analysis.canExpandSynonyms()) {
            optimized = expandSynonyms(optimized);
        }
        
        // 4. 查詢重寫
        if (analysis.needsRewriting()) {
            optimized = rewriteQuery(optimized, analysis);
        }
        
        return optimized;
    }
    
    /**
     * 決定搜尋策略
     */
    private SearchStrategy determineSearchStrategy(QueryAnalysisResult analysis) {
        if (analysis.getQueryComplexity() == QueryComplexity.HIGH) {
            return SearchStrategy.HYBRID_SEARCH;
        } else if (analysis.hasKeywords()) {
            return SearchStrategy.KEYWORD_SEARCH;
        } else {
            return SearchStrategy.SEMANTIC_SEARCH;
        }
    }
    
    /**
     * 執行優化查詢
     */
    private QueryResult executeOptimizedQuery(OptimizedQuery query) {
        return switch (query.getSearchStrategy()) {
            case SEMANTIC_SEARCH -> executeSemanticSearch(query);
            case KEYWORD_SEARCH -> executeKeywordSearch(query);
            case HYBRID_SEARCH -> executeHybridSearch(query);
        };
    }
    
    /**
     * 執行語義搜尋
     */
    private QueryResult executeSemanticSearch(OptimizedQuery query) {
        // 向量相似性搜尋
        List<Document> documents = vectorStoreService.similaritySearch(
            query.getOptimizedQuery(),
            query.getResultLimit(),
            query.getSimilarityThreshold()
        );
        
        return QueryResult.builder()
            .documents(documents)
            .searchStrategy(SearchStrategy.SEMANTIC_SEARCH)
            .executionTimeMs(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 執行關鍵字搜尋
     */
    private QueryResult executeKeywordSearch(OptimizedQuery query) {
        // 關鍵字搜尋實現
        List<Document> documents = vectorStoreService.keywordSearch(
            query.getOptimizedQuery(),
            query.getResultLimit()
        );
        
        return QueryResult.builder()
            .documents(documents)
            .searchStrategy(SearchStrategy.KEYWORD_SEARCH)
            .executionTimeMs(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 執行混合搜尋
     */
    private QueryResult executeHybridSearch(OptimizedQuery query) {
        // 並行執行語義搜尋和關鍵字搜尋
        CompletableFuture<List<Document>> semanticFuture = CompletableFuture
            .supplyAsync(() -> executeSemanticSearch(query).getDocuments());
        
        CompletableFuture<List<Document>> keywordFuture = CompletableFuture
            .supplyAsync(() -> executeKeywordSearch(query).getDocuments());
        
        try {
            List<Document> semanticResults = semanticFuture.get(5, TimeUnit.SECONDS);
            List<Document> keywordResults = keywordFuture.get(5, TimeUnit.SECONDS);
            
            // 結果融合和重新排序
            List<Document> mergedResults = mergeAndRerankResults(
                semanticResults, keywordResults, query);
            
            return QueryResult.builder()
                .documents(mergedResults)
                .searchStrategy(SearchStrategy.HYBRID_SEARCH)
                .executionTimeMs(System.currentTimeMillis())
                .build();
                
        } catch (Exception e) {
            log.warn("Hybrid search failed, falling back to semantic search", e);
            return executeSemanticSearch(query);
        }
    }
    
    /**
     * 結果融合和重新排序
     */
    private List<Document> mergeAndRerankResults(List<Document> semanticResults,
                                               List<Document> keywordResults,
                                               OptimizedQuery query) {
        
        Map<String, Document> documentMap = new HashMap<>();
        Map<String, Double> scoreMap = new HashMap<>();
        
        // 語義搜尋結果權重
        double semanticWeight = 0.7;
        for (int i = 0; i < semanticResults.size(); i++) {
            Document doc = semanticResults.get(i);
            String docId = doc.getId();
            double score = semanticWeight * (1.0 - (double) i / semanticResults.size());
            
            documentMap.put(docId, doc);
            scoreMap.put(docId, score);
        }
        
        // 關鍵字搜尋結果權重
        double keywordWeight = 0.3;
        for (int i = 0; i < keywordResults.size(); i++) {
            Document doc = keywordResults.get(i);
            String docId = doc.getId();
            double score = keywordWeight * (1.0 - (double) i / keywordResults.size());
            
            if (scoreMap.containsKey(docId)) {
                scoreMap.put(docId, scoreMap.get(docId) + score);
            } else {
                documentMap.put(docId, doc);
                scoreMap.put(docId, score);
            }
        }
        
        // 按分數排序
        return scoreMap.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(query.getResultLimit())
            .map(entry -> documentMap.get(entry.getKey()))
            .collect(Collectors.toList());
    }
    
    // 輔助方法實現（簡化版）
    private QueryResult checkCache(OptimizedQuery query) {
        // 快取檢查實現
        return null;
    }
    
    private void cacheResult(OptimizedQuery query, QueryResult result) {
        // 結果快取實現
    }
    
    private String removeStopWords(String text, String language) {
        // 停用詞移除實現
        return text;
    }
    
    private String applyStemming(String text, String language) {
        // 詞幹提取實現
        return text;
    }
    
    private String expandSynonyms(String text) {
        // 同義詞擴展實現
        return text;
    }
    
    private String rewriteQuery(String text, QueryAnalysisResult analysis) {
        // 查詢重寫實現
        return text;
    }
    
    private Map<String, Object> optimizeFilters(Map<String, Object> filters, QueryAnalysisResult analysis) {
        return filters != null ? filters : new HashMap<>();
    }
    
    private int optimizeResultLimit(int limit, QueryAnalysisResult analysis) {
        return Math.max(1, Math.min(limit, 100));
    }
    
    private double optimizeSimilarityThreshold(QueryAnalysisResult analysis) {
        return 0.7; // 預設閾值
    }
    
    // 內部類別和枚舉
    public enum SearchStrategy {
        SEMANTIC_SEARCH, KEYWORD_SEARCH, HYBRID_SEARCH
    }
    
    public enum QueryComplexity {
        LOW, MEDIUM, HIGH
    }
    
    public static class QueryOptimizationException extends RuntimeException {
        public QueryOptimizationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    // 簡化的介面定義
    public interface VectorStoreService {
        List<Document> similaritySearch(String query, int limit, double threshold);
        List<Document> keywordSearch(String query, int limit);
    }
    
    public interface QueryAnalyzer {
        QueryAnalysisResult analyze(QueryRequest request);
    }
    
    public interface PerformanceMetrics {
        void recordCacheHit(String queryId);
        void recordQueryExecution(String queryId, long executionTime);
        void recordQueryError(String queryId);
    }
    
    // 簡化的資料類別
    public static class QueryRequest {
        private String queryId;
        private String query;
        private Map<String, Object> filters;
        private int limit;
        
        // getters and setters
        public String getQueryId() { return queryId; }
        public String getQuery() { return query; }
        public Map<String, Object> getFilters() { return filters; }
        public int getLimit() { return limit; }
    }
    
    public static class OptimizedQuery {
        private String originalQuery;
        private String optimizedQuery;
        private SearchStrategy searchStrategy;
        private Map<String, Object> filterCriteria;
        private int resultLimit;
        private double similarityThreshold;
        
        // getters and builder
        public String getOriginalQuery() { return originalQuery; }
        public String getOptimizedQuery() { return optimizedQuery; }
        public SearchStrategy getSearchStrategy() { return searchStrategy; }
        public Map<String, Object> getFilterCriteria() { return filterCriteria; }
        public int getResultLimit() { return resultLimit; }
        public double getSimilarityThreshold() { return similarityThreshold; }
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private OptimizedQuery query = new OptimizedQuery();
            
            public Builder originalQuery(String originalQuery) {
                query.originalQuery = originalQuery;
                return this;
            }
            
            public Builder optimizedQuery(String optimizedQuery) {
                query.optimizedQuery = optimizedQuery;
                return this;
            }
            
            public Builder searchStrategy(SearchStrategy searchStrategy) {
                query.searchStrategy = searchStrategy;
                return this;
            }
            
            public Builder filterCriteria(Map<String, Object> filterCriteria) {
                query.filterCriteria = filterCriteria;
                return this;
            }
            
            public Builder resultLimit(int resultLimit) {
                query.resultLimit = resultLimit;
                return this;
            }
            
            public Builder similarityThreshold(double similarityThreshold) {
                query.similarityThreshold = similarityThreshold;
                return this;
            }
            
            public OptimizedQuery build() { return query; }
        }
    }
    
    public static class QueryResult {
        private List<Document> documents;
        private SearchStrategy searchStrategy;
        private long executionTimeMs;
        
        public List<Document> getDocuments() { return documents; }
        public SearchStrategy getSearchStrategy() { return searchStrategy; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private QueryResult result = new QueryResult();
            
            public Builder documents(List<Document> documents) {
                result.documents = documents;
                return this;
            }
            
            public Builder searchStrategy(SearchStrategy searchStrategy) {
                result.searchStrategy = searchStrategy;
                return this;
            }
            
            public Builder executionTimeMs(long executionTimeMs) {
                result.executionTimeMs = executionTimeMs;
                return this;
            }
            
            public QueryResult build() { return result; }
        }
    }
    
    public static class QueryAnalysisResult {
        private QueryComplexity queryComplexity;
        private String language;
        private boolean hasStopWords;
        private boolean hasKeywords;
        
        public QueryComplexity getQueryComplexity() { return queryComplexity; }
        public String getLanguage() { return language; }
        public boolean hasStopWords() { return hasStopWords; }
        public boolean hasKeywords() { return hasKeywords; }
        public boolean needsStemming() { return true; }
        public boolean canExpandSynonyms() { return true; }
        public boolean needsRewriting() { return false; }
    }
}