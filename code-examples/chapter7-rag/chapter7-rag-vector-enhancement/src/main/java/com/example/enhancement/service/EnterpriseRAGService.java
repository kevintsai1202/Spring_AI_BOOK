package com.example.enhancement.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 企業 RAG 服務
 * 結合企業資料和向量搜尋，提供智能問答功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnterpriseRAGService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final MeterRegistry meterRegistry;

    /**
     * 執行 RAG 查詢
     *
     * @param query 用戶問題
     * @param topK 檢索的相關文檔數量
     * @param similarityThreshold 相似度閾值
     * @return RAG 查詢結果
     */
    public RAGQueryResult query(String query, int topK, double similarityThreshold) {
        log.info("執行 RAG 查詢: query={}, topK={}, threshold={}", query, topK, similarityThreshold);
        Timer.Sample timer = Timer.start(meterRegistry);

        RAGQueryResult result = RAGQueryResult.builder()
                .query(query)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            // 1. 向量搜尋 - 檢索相關文檔
            List<Document> similarDocuments = searchSimilarDocuments(query, topK, similarityThreshold);
            result.setRetrievedDocuments(similarDocuments.size());

            log.debug("檢索到 {} 個相關文檔", similarDocuments.size());

            // 2. 使用 ChatClient 和 QuestionAnswerAdvisor 生成答案
            String answer = generateAnswer(query, similarDocuments);
            result.setAnswer(answer);

            // 3. 提取文檔來源資訊
            List<DocumentSource> sources = extractDocumentSources(similarDocuments);
            result.setSources(sources);

            result.setSuccess(true);

            // 記錄成功 metrics
            Counter.builder("enterprise.rag.query.success")
                    .tag("type", "standard")
                    .register(meterRegistry)
                    .increment();

        } catch (Exception e) {
            log.error("RAG 查詢失敗", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setAnswer("抱歉，處理您的查詢時發生錯誤：" + e.getMessage());

            Counter.builder("enterprise.rag.query.failure")
                    .tag("type", "standard")
                    .register(meterRegistry)
                    .increment();
        } finally {
            timer.stop(Timer.builder("enterprise.rag.query.duration")
                    .tag("type", "standard")
                    .register(meterRegistry));
        }

        log.info("RAG 查詢完成: success={}, documents={}", result.isSuccess(), result.getRetrievedDocuments());
        return result;
    }

    /**
     * 執行 RAG 查詢（使用快取）
     */
    @Cacheable(value = "rag-queries", key = "#query + '-' + #topK")
    public RAGQueryResult queryCached(String query, int topK, double similarityThreshold) {
        return query(query, topK, similarityThreshold);
    }

    /**
     * 搜尋相似文檔
     */
    private List<Document> searchSimilarDocuments(String query, int topK, double similarityThreshold) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        log.debug("向量搜尋結果: {} 個文檔", documents.size());
        documents.forEach(doc -> {
            log.debug("  - 文檔 ID: {}, 類型: {}, 相似度: {}",
                    doc.getMetadata().get("id"),
                    doc.getMetadata().get("type"),
                    doc.getMetadata().get("distance"));
        });

        return documents;
    }

    /**
     * 使用向量搜尋和 LLM 生成答案
     */
    private String generateAnswer(String query, List<Document> documents) {
        if (documents.isEmpty()) {
            return "很抱歉，我在企業資料庫中找不到與您問題相關的資訊。";
        }

        // 組裝上下文
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // 使用 PromptTemplate 構建提示詞
        String promptText = "你是一個企業資料分析助手。請根據以下企業資料回答用戶的問題。\n\n" +
                "企業資料:\n" +
                "{context}\n\n" +
                "用戶問題: {question}\n\n" +
                "請用繁體中文回答，並基於提供的企業資料進行分析。如果資料中沒有相關信息，請明確說明。";

        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = promptTemplate.create(Map.of(
                "context", context,
                "question", query
        ));

        ChatResponse response = chatModel.call(prompt);
        String answer = response.getResult().getOutput().getText();

        log.debug("AI 生成的答案長度: {} 字符", answer != null ? answer.length() : 0);
        return answer;
    }

    /**
     * 提取文檔來源資訊
     */
    private List<DocumentSource> extractDocumentSources(List<Document> documents) {
        return documents.stream()
                .map(doc -> {
                    Map<String, Object> metadata = doc.getMetadata();
                    String content = doc.getText();
                    String contentPreview = content.substring(0, Math.min(200, content.length())) + "...";
                    return DocumentSource.builder()
                            .id(String.valueOf(metadata.get("id")))
                            .type(String.valueOf(metadata.get("type")))
                            .name(String.valueOf(metadata.get("name")))
                            .content(contentPreview)
                            .metadata(metadata)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * RAG 查詢結果
     */
    @Data
    @Builder
    public static class RAGQueryResult {
        /**
         * 用戶查詢
         */
        private String query;

        /**
         * AI 生成的答案
         */
        private String answer;

        /**
         * 檢索到的文檔數量
         */
        private int retrievedDocuments;

        /**
         * 文檔來源列表
         */
        private List<DocumentSource> sources;

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 錯誤訊息
         */
        private String errorMessage;

        /**
         * 查詢時間戳
         */
        private LocalDateTime timestamp;
    }

    /**
     * 文檔來源資訊
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class DocumentSource {
        /**
         * 文檔 ID
         */
        private String id;

        /**
         * 文檔類型（department, employee, product, project）
         */
        private String type;

        /**
         * 名稱
         */
        private String name;

        /**
         * 內容摘要
         */
        private String content;

        /**
         * 元資料
         */
        private Map<String, Object> metadata;
    }
}
