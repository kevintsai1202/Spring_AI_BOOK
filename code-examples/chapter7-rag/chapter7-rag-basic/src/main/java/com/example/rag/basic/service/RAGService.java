package com.example.rag.basic.service;

import com.example.rag.basic.exception.RAGException;
import com.example.rag.basic.model.DocumentSource;
import com.example.rag.basic.model.RAGQueryRequest;
import com.example.rag.basic.model.RAGQueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 服務實現
 *
 * 主要功能:
 * 1. RAG 查詢 (使用 Spring AI QuestionAnswerAdvisor)
 * 2. 文檔上傳與向量化
 * 3. 向量資料庫管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RAGService {

    private final ChatClient ragChatClient;
    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;
    private final DocumentProcessingService documentProcessor;

    /**
     * RAG 查詢 - Spring AI 自動處理檢索增強
     *
     * QuestionAnswerAdvisor 會自動:
     * 1. 向量化問題
     * 2. 檢索相關文檔
     * 3. 組裝上下文
     * 4. 調用 LLM 生成答案
     */
    public RAGQueryResponse query(RAGQueryRequest request) {
        log.info("Processing RAG query: {}", request.getQuestion());

        long startTime = System.currentTimeMillis();

        try {
            // Spring AI QuestionAnswerAdvisor 會自動處理 RAG 流程
            String answer = ragChatClient.prompt()
                    .user(request.getQuestion())
                    .call()
                    .content();

            // 手動檢索相關文檔以便返回來源資訊
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(request.getQuestion())
                    .topK(request.getTopK())
                    .similarityThreshold(request.getSimilarityThreshold())
                    .build();

            List<Document> retrievedDocs = vectorStore.similaritySearch(searchRequest);

            List<DocumentSource> sources = retrievedDocs.stream()
                    .map(this::convertToDocumentSource)
                    .collect(Collectors.toList());

            long processingTime = System.currentTimeMillis() - startTime;

            return RAGQueryResponse.builder()
                    .question(request.getQuestion())
                    .answer(answer)
                    .sources(sources)
                    .processingTimeMs(processingTime)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("RAG query failed", e);
            throw new RAGException("RAG 查詢失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 帶過濾條件的 RAG 查詢
     */
    public RAGQueryResponse queryWithFilter(RAGQueryRequest request) {
        log.info("Processing RAG query with filters: {}", request.getQuestion());

        try {
            // 構建過濾表達式
            String filterExpression = buildFilterExpression(request.getFilters());

            String answer;
            if (filterExpression != null && !filterExpression.isEmpty()) {
                answer = ragChatClient.prompt()
                        .user(request.getQuestion())
                        .advisors(advisorSpec -> advisorSpec
                                .param("FILTER_EXPRESSION", filterExpression))
                        .call()
                        .content();
            } else {
                answer = ragChatClient.prompt()
                        .user(request.getQuestion())
                        .call()
                        .content();
            }

            return RAGQueryResponse.builder()
                    .question(request.getQuestion())
                    .answer(answer)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("RAG query with filter failed", e);
            throw new RAGException("RAG 查詢失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 添加文檔到知識庫
     */
    public void addDocuments(List<Resource> resources) {
        log.info("Adding {} documents to knowledge base", resources.size());

        List<Document> allDocuments = new ArrayList<>();

        for (Resource resource : resources) {
            try {
                // 處理文檔
                List<Document> processedDocs = documentProcessor.process(resource);
                allDocuments.addAll(processedDocs);

                log.info("Processed document: {}, created {} chunks",
                        resource.getFilename(), processedDocs.size());

            } catch (Exception e) {
                log.error("Failed to process document: {}", resource.getFilename(), e);
            }
        }

        // 寫入向量資料庫（自動生成嵌入向量）
        if (!allDocuments.isEmpty()) {
            vectorStore.add(allDocuments);
            log.info("Successfully added {} document chunks to vector store", allDocuments.size());
        }
    }

    /**
     * 從知識庫中刪除文檔
     */
    public void deleteDocuments(List<String> documentIds) {
        log.info("Deleting {} documents from knowledge base", documentIds.size());
        vectorStore.delete(documentIds);
        log.info("Documents deleted successfully");
    }

    /**
     * 搜尋相似文檔
     */
    public List<Document> searchSimilarDocuments(String query, int topK, double threshold) {
        log.info("Searching for similar documents: query={}, topK={}, threshold={}",
                query, topK, threshold);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    /**
     * 構建過濾表達式
     */
    private String buildFilterExpression(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        List<String> conditions = filters.entrySet().stream()
                .map(entry -> String.format("metadata['%s'] == '%s'",
                        entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return String.join(" && ", conditions);
    }

    /**
     * 轉換為 DocumentSource
     */
    private DocumentSource convertToDocumentSource(Document document) {
        Map<String, Object> metadata = document.getMetadata();

        // 處理距離分數 - Neo4j 可能返回 Float 或 Double
        Object distanceObj = metadata.getOrDefault("distance", 0.0);
        double relevanceScore = 0.0;
        if (distanceObj instanceof Number) {
            relevanceScore = ((Number) distanceObj).doubleValue();
        }

        return DocumentSource.builder()
                .documentId((String) metadata.getOrDefault("id", ""))
                .title((String) metadata.getOrDefault("title", "未命名文檔"))
                .excerpt(truncateContent(document.getText(), 200))
                .relevanceScore(relevanceScore)
                .metadata(metadata)
                .build();
    }

    /**
     * 截斷內容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

}
