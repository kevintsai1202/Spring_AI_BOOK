/**
 * 智能查詢處理器
 * 負責查詢分析、重寫和擴展
 */
package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryProcessor {
    
    private final QueryAnalyzer queryAnalyzer;
    private final QueryRewriter queryRewriter;
    private final QueryExpander queryExpander;
    private final IntentClassifier intentClassifier;
    
    /**
     * 處理查詢
     */
    public ProcessedQuery process(String originalQuery, QueryContext context) {
        
        // 1. 查詢分析
        QueryAnalysis analysis = queryAnalyzer.analyze(originalQuery);
        
        // 2. 意圖分類
        QueryIntent intent = intentClassifier.classify(originalQuery, context);
        
        // 3. 查詢重寫
        String rewrittenQuery = queryRewriter.rewrite(originalQuery, analysis, intent);
        
        // 4. 查詢擴展
        ExpandedQuery expandedQuery = queryExpander.expand(rewrittenQuery, context);
        
        return ProcessedQuery.builder()
            .originalQuery(originalQuery)
            .rewrittenQuery(rewrittenQuery)
            .expandedQuery(expandedQuery)
            .analysis(analysis)
            .intent(intent)
            .context(context)
            .build();
    }
    
    /**
     * 處理後的查詢物件
     */
    public static class ProcessedQuery {
        private String originalQuery;
        private String rewrittenQuery;
        private ExpandedQuery expandedQuery;
        private QueryAnalysis analysis;
        private QueryIntent intent;
        private QueryContext context;
        
        // Getters
        public String getOriginalQuery() { return originalQuery; }
        public String getRewrittenQuery() { return rewrittenQuery; }
        public ExpandedQuery getExpandedQuery() { return expandedQuery; }
        public QueryAnalysis getAnalysis() { return analysis; }
        public QueryIntent getIntent() { return intent; }
        public QueryContext getContext() { return context; }
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private ProcessedQuery query = new ProcessedQuery();
            
            public Builder originalQuery(String originalQuery) {
                query.originalQuery = originalQuery;
                return this;
            }
            
            public Builder rewrittenQuery(String rewrittenQuery) {
                query.rewrittenQuery = rewrittenQuery;
                return this;
            }
            
            public Builder expandedQuery(ExpandedQuery expandedQuery) {
                query.expandedQuery = expandedQuery;
                return this;
            }
            
            public Builder analysis(QueryAnalysis analysis) {
                query.analysis = analysis;
                return this;
            }
            
            public Builder intent(QueryIntent intent) {
                query.intent = intent;
                return this;
            }
            
            public Builder context(QueryContext context) {
                query.context = context;
                return this;
            }
            
            public ProcessedQuery build() { return query; }
        }
    }
    
    /**
     * 查詢分析器
     */
    @org.springframework.stereotype.Component
    @Slf4j
    public static class QueryAnalyzer {
        
        /**
         * 分析查詢
         */
        public QueryAnalysis analyze(String query) {
            
            // 1. 語言檢測
            Language language = detectLanguage(query);
            
            // 2. 實體識別
            List<Entity> entities = extractEntities(query);
            
            // 3. 關鍵詞提取
            List<Keyword> keywords = extractKeywords(query);
            
            // 4. 語義分析
            SemanticAnalysis semantics = analyzeSemantics(query);
            
            // 5. 複雜度評估
            ComplexityLevel complexity = assessComplexity(query, entities, keywords);
            
            return QueryAnalysis.builder()
                .language(language)
                .entities(entities)
                .keywords(keywords)
                .semantics(semantics)
                .complexity(complexity)
                .build();
        }
        
        private Language detectLanguage(String query) {
            // 語言檢測邏輯
            if (query.matches(".*[\u4e00-\u9fff].*")) {
                return Language.CHINESE;
            } else if (query.matches(".*[a-zA-Z].*")) {
                return Language.ENGLISH;
            }
            return Language.UNKNOWN;
        }
        
        private List<Entity> extractEntities(String query) {
            // 實體識別邏輯
            List<Entity> entities = new ArrayList<>();
            
            // 使用 NLP 模型或規則提取實體
            // 這裡簡化實現
            String[] words = query.split("\\s+");
            for (String word : words) {
                if (isEntity(word)) {
                    entities.add(Entity.builder()
                        .text(word)
                        .type(determineEntityType(word))
                        .confidence(0.8)
                        .build());
                }
            }
            
            return entities;
        }
        
        private List<Keyword> extractKeywords(String query) {
            // 關鍵詞提取邏輯
            List<Keyword> keywords = new ArrayList<>();
            
            String[] words = query.split("\\s+");
            for (String word : words) {
                if (isKeyword(word)) {
                    keywords.add(new Keyword(word, calculateImportance(word)));
                }
            }
            
            return keywords;
        }
        
        private SemanticAnalysis analyzeSemantics(String query) {
            // 語義分析邏輯
            return new SemanticAnalysis(query, "positive", 0.8);
        }
        
        private ComplexityLevel assessComplexity(String query, List<Entity> entities, List<Keyword> keywords) {
            // 複雜度評估邏輯
            int score = query.length() + entities.size() * 10 + keywords.size() * 5;
            
            if (score > 100) {
                return ComplexityLevel.HIGH;
            } else if (score > 50) {
                return ComplexityLevel.MEDIUM;
            } else {
                return ComplexityLevel.LOW;
            }
        }
        
        private boolean isEntity(String word) {
            // 簡化的實體判斷邏輯
            return word.length() > 2 && Character.isUpperCase(word.charAt(0));
        }
        
        private EntityType determineEntityType(String word) {
            // 簡化的實體類型判斷
            if (word.matches(".*[0-9].*")) {
                return EntityType.NUMBER;
            } else if (word.endsWith("公司") || word.endsWith("Corp")) {
                return EntityType.ORGANIZATION;
            } else {
                return EntityType.GENERAL;
            }
        }
        
        private boolean isKeyword(String word) {
            // 簡化的關鍵詞判斷
            return word.length() > 2 && !isStopWord(word);
        }
        
        private boolean isStopWord(String word) {
            // 停用詞判斷
            String[] stopWords = {"的", "是", "在", "有", "和", "與", "或", "但", "如果", "因為"};
            return Arrays.asList(stopWords).contains(word);
        }
        
        private double calculateImportance(String word) {
            // 計算詞語重要性
            return 1.0 / (1.0 + word.length() * 0.1);
        }
    }
    
    // 資料類別定義
    public enum Language {
        CHINESE, ENGLISH, UNKNOWN
    }
    
    public enum EntityType {
        PERSON, ORGANIZATION, LOCATION, NUMBER, GENERAL
    }
    
    public enum ComplexityLevel {
        LOW, MEDIUM, HIGH
    }
    
    public enum QueryIntent {
        SEARCH, QUESTION, SUMMARY, COMPARISON, EXPLANATION
    }
    
    public static class Entity {
        private String text;
        private EntityType type;
        private double confidence;
        
        public String getText() { return text; }
        public EntityType getType() { return type; }
        public double getConfidence() { return confidence; }
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private Entity entity = new Entity();
            
            public Builder text(String text) {
                entity.text = text;
                return this;
            }
            
            public Builder type(EntityType type) {
                entity.type = type;
                return this;
            }
            
            public Builder confidence(double confidence) {
                entity.confidence = confidence;
                return this;
            }
            
            public Entity build() { return entity; }
        }
    }
    
    public static class Keyword {
        private String word;
        private double importance;
        
        public Keyword(String word, double importance) {
            this.word = word;
            this.importance = importance;
        }
        
        public String getWord() { return word; }
        public double getImportance() { return importance; }
    }
    
    public static class SemanticAnalysis {
        private String query;
        private String sentiment;
        private double confidence;
        
        public SemanticAnalysis(String query, String sentiment, double confidence) {
            this.query = query;
            this.sentiment = sentiment;
            this.confidence = confidence;
        }
        
        public String getQuery() { return query; }
        public String getSentiment() { return sentiment; }
        public double getConfidence() { return confidence; }
    }
    
    public static class QueryAnalysis {
        private Language language;
        private List<Entity> entities;
        private List<Keyword> keywords;
        private SemanticAnalysis semantics;
        private ComplexityLevel complexity;
        
        public Language getLanguage() { return language; }
        public List<Entity> getEntities() { return entities; }
        public List<Keyword> getKeywords() { return keywords; }
        public SemanticAnalysis getSemantics() { return semantics; }
        public ComplexityLevel getComplexity() { return complexity; }
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private QueryAnalysis analysis = new QueryAnalysis();
            
            public Builder language(Language language) {
                analysis.language = language;
                return this;
            }
            
            public Builder entities(List<Entity> entities) {
                analysis.entities = entities;
                return this;
            }
            
            public Builder keywords(List<Keyword> keywords) {
                analysis.keywords = keywords;
                return this;
            }
            
            public Builder semantics(SemanticAnalysis semantics) {
                analysis.semantics = semantics;
                return this;
            }
            
            public Builder complexity(ComplexityLevel complexity) {
                analysis.complexity = complexity;
                return this;
            }
            
            public QueryAnalysis build() { return analysis; }
        }
    }
    
    public static class ExpandedQuery {
        private String embeddingQuery;
        private String keywordQuery;
        
        public String getEmbeddingQuery() { return embeddingQuery; }
        public String getKeywordQuery() { return keywordQuery; }
    }
    
    public static class QueryContext {
        private String sessionId;
        private Object userProfile;
        
        public String getSessionId() { return sessionId; }
        public Object getUserProfile() { return userProfile; }
    }
    
    // 簡化的介面定義
    public interface QueryRewriter {
        String rewrite(String query, QueryAnalysis analysis, QueryIntent intent);
    }
    
    public interface QueryExpander {
        ExpandedQuery expand(String query, QueryContext context);
    }
    
    public interface IntentClassifier {
        QueryIntent classify(String query, QueryContext context);
    }
}