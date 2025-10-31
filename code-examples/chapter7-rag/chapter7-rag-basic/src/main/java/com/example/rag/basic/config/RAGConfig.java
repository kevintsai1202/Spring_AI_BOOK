package com.example.rag.basic.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * RAG 系統配置類
 *
 * 配置內容:
 * - Neo4j 向量資料庫
 * - QuestionAnswerAdvisor
 * - RAG ChatClient
 * - TokenTextSplitter
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RAGConfig {

    @Value("${app.rag.top-k:5}")
    private int topK;

    @Value("${app.rag.similarity-threshold:0.7}")
    private double similarityThreshold;

    @Value("${app.rag.chunk-size:800}")
    private int chunkSize;

    @Value("${app.rag.chunk-overlap:200}")
    private int chunkOverlap;

    /**
     * 配置 TokenTextSplitter - 文本分塊工具
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(
                chunkSize,      // defaultChunkSize
                chunkOverlap,   // minChunkSizeChars
                10,             // minChunkLengthToEmbed
                10000,          // maxNumChunks
                true            // keepSeparator
        );
    }

    /**
     * 配置 RAG ChatClient
     *
     * 使用 QuestionAnswerAdvisor 自動處理 RAG 流程:
     * 1. 向量化用戶問題
     * 2. 從向量資料庫檢索相關文檔
     * 3. 組裝上下文
     * 4. 調用 LLM 生成答案
     */
    @Bean
    public ChatClient ragChatClient(ChatModel chatModel, VectorStore vectorStore) {
        log.info("Configuring RAG ChatClient with QuestionAnswerAdvisor");
        log.info("Top-K: {}, Similarity Threshold: {}", topK, similarityThreshold);

        // 使用最新的 Spring AI SearchRequest Builder API
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();

        // 使用 Spring AI 1.0.3 的 QuestionAnswerAdvisor
        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();

        return ChatClient.builder(chatModel)
                .defaultAdvisors(qaAdvisor)
                .build();
    }

}
