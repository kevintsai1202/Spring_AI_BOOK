/**
 * RAG 生成服務
 * 負責基於檢索到的上下文生成最終回應
 */
package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RAGGenerationService {
    
    private final ChatClient chatClient;
    
    public String generateResponse(String query, List<Document> context) {
        // 1. 組裝上下文
        String contextText = assembleContext(context);
        
        // 2. 構建提示詞
        String prompt = buildPrompt(query, contextText);
        
        // 3. 生成回應
        return chatClient.prompt(prompt)
            .call()
            .content();
    }
    
    private String assembleContext(List<Document> documents) {
        StringBuilder context = new StringBuilder();
        
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            context.append(String.format("[文檔 %d]\n%s\n\n", i + 1, doc.getContent()));
        }
        
        return context.toString();
    }
    
    private String buildPrompt(String query, String context) {
        return String.format("""
            你是一個專業的知識助手。請根據以下提供的上下文資訊來回答用戶的問題。
            
            上下文資訊：
            %s
            
            用戶問題：%s
            
            請根據上下文資訊提供準確、詳細的回答。如果上下文中沒有相關資訊，請明確說明。
            回答時請：
            1. 直接回答問題
            2. 引用相關的上下文資訊
            3. 保持回答的準確性和客觀性
            4. 使用繁體中文回答
            """, context, query);
    }
    
    /**
     * 生成帶有來源引用的回應
     */
    public ResponseWithSources generateResponseWithSources(String query, List<Document> context) {
        // 1. 生成主要回應
        String response = generateResponse(query, context);
        
        // 2. 提取來源資訊
        List<SourceInfo> sources = extractSources(context);
        
        return ResponseWithSources.builder()
            .response(response)
            .sources(sources)
            .query(query)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    private List<SourceInfo> extractSources(List<Document> documents) {
        return documents.stream()
            .map(this::createSourceInfo)
            .toList();
    }
    
    private SourceInfo createSourceInfo(Document document) {
        return SourceInfo.builder()
            .title(extractTitle(document))
            .content(document.getContent().substring(0, Math.min(200, document.getContent().length())))
            .metadata(document.getMetadata())
            .build();
    }
    
    private String extractTitle(Document document) {
        // 嘗試從元資料中提取標題
        Object title = document.getMetadata().get("title");
        if (title != null) {
            return title.toString();
        }
        
        // 如果沒有標題，使用內容的前幾個字作為標題
        String content = document.getContent();
        if (content.length() > 50) {
            return content.substring(0, 50) + "...";
        }
        
        return content;
    }
    
    /**
     * 帶來源的回應物件
     */
    public static class ResponseWithSources {
        private final String response;
        private final List<SourceInfo> sources;
        private final String query;
        private final long timestamp;
        
        public ResponseWithSources(String response, List<SourceInfo> sources, 
                                 String query, long timestamp) {
            this.response = response;
            this.sources = sources;
            this.query = query;
            this.timestamp = timestamp;
        }
        
        public String getResponse() { return response; }
        public List<SourceInfo> getSources() { return sources; }
        public String getQuery() { return query; }
        public long getTimestamp() { return timestamp; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String response;
            private List<SourceInfo> sources;
            private String query;
            private long timestamp;
            
            public Builder response(String response) {
                this.response = response;
                return this;
            }
            
            public Builder sources(List<SourceInfo> sources) {
                this.sources = sources;
                return this;
            }
            
            public Builder query(String query) {
                this.query = query;
                return this;
            }
            
            public Builder timestamp(long timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public ResponseWithSources build() {
                return new ResponseWithSources(response, sources, query, timestamp);
            }
        }
    }
    
    /**
     * 來源資訊物件
     */
    public static class SourceInfo {
        private final String title;
        private final String content;
        private final Object metadata;
        
        public SourceInfo(String title, String content, Object metadata) {
            this.title = title;
            this.content = content;
            this.metadata = metadata;
        }
        
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public Object getMetadata() { return metadata; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String title;
            private String content;
            private Object metadata;
            
            public Builder title(String title) {
                this.title = title;
                return this;
            }
            
            public Builder content(String content) {
                this.content = content;
                return this;
            }
            
            public Builder metadata(Object metadata) {
                this.metadata = metadata;
                return this;
            }
            
            public SourceInfo build() {
                return new SourceInfo(title, content, metadata);
            }
        }
    }
}