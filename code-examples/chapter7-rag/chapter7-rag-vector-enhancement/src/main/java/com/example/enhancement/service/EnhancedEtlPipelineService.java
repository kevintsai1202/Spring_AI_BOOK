package com.example.enhancement.service;

import com.example.enhancement.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增強型 ETL Pipeline 服務
 *
 * 整合所有增強功能的完整 ETL 流程：
 * 1. 文本清理（TextCleaningService）
 * 2. 語義分塊（TokenTextSplitter）
 * 3. 元資料增強（MetadataEnrichmentService）
 * 4. 向量品質評估（VectorQualityService）
 * 5. 載入向量資料庫（VectorStore）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedEtlPipelineService {

    private final TextCleaningService textCleaningService;
    private final MetadataEnrichmentService metadataEnrichmentService;
    private final VectorQualityService vectorQualityService;
    private final VectorStore vectorStore;

    /**
     * 執行完整的增強型 ETL Pipeline
     *
     * @param documents               原始文檔列表
     * @param textCleaningConfig      文本清理配置
     * @param chunkingConfig          分塊配置
     * @param enrichmentConfig        元資料增強配置
     * @return ETL 執行結果
     */
    public EnhancedEtlResult executeEnhancedPipeline(
            List<Document> documents,
            TextCleaningConfig textCleaningConfig,
            SemanticChunkingConfig chunkingConfig,
            MetadataEnrichmentConfig enrichmentConfig) {

        LocalDateTime startTime = LocalDateTime.now();
        log.info("Starting Enhanced ETL Pipeline for {} documents", documents.size());

        try {
            // Step 1: 智能文本清理
            log.info("Step 1: Text Cleaning...");
            List<Document> cleanedDocuments = cleanDocuments(documents, textCleaningConfig);
            log.info("Text cleaning completed. Documents: {}", cleanedDocuments.size());

            // Step 2: 語義分塊
            log.info("Step 2: Semantic Chunking...");
            List<Document> chunkedDocuments = chunkDocuments(cleanedDocuments, chunkingConfig);
            log.info("Chunking completed. Chunks: {}", chunkedDocuments.size());

            // Step 3: 元資料增強
            log.info("Step 3: Metadata Enrichment...");
            List<Document> enrichedDocuments = metadataEnrichmentService.enrichMetadata(
                    chunkedDocuments, enrichmentConfig);
            log.info("Metadata enrichment completed. Documents: {}", enrichedDocuments.size());

            // Step 4: 向量品質評估
            log.info("Step 4: Vector Quality Assessment...");
            List<EmbeddingQuality> qualityResults = vectorQualityService.assessDocumentsQuality(enrichedDocuments);
            List<Document> qualityPassedDocuments = filterByQuality(enrichedDocuments, qualityResults);
            log.info("Quality assessment completed. Passed: {}/{}",
                    qualityPassedDocuments.size(), enrichedDocuments.size());

            // Step 5: 載入向量資料庫
            log.info("Step 5: Loading to Vector Store...");
            vectorStore.add(qualityPassedDocuments);
            log.info("Vector store loading completed. Documents loaded: {}", qualityPassedDocuments.size());

            // 計算執行時間
            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);

            // 建立成功結果
            EnhancedEtlResult result = EnhancedEtlResult.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .duration(duration)
                    .originalDocumentCount(documents.size())
                    .cleanedDocumentCount(cleanedDocuments.size())
                    .chunkedDocumentCount(chunkedDocuments.size())
                    .enrichedDocumentCount(enrichedDocuments.size())
                    .qualityPassedDocumentCount(qualityPassedDocuments.size())
                    .loadedDocumentCount(qualityPassedDocuments.size())
                    .qualityResults(qualityResults)
                    .success(true)
                    .additionalInfo(String.format("Processing speed: %.2f docs/sec, Quality pass rate: %.2f%%",
                            calculateSpeed(qualityPassedDocuments.size(), duration),
                            calculatePassRate(qualityPassedDocuments.size(), enrichedDocuments.size())))
                    .build();

            log.info("Enhanced ETL Pipeline completed successfully. Duration: {}s, Speed: {:.2f} docs/sec",
                    duration.getSeconds(), result.getProcessingSpeed());

            return result;

        } catch (Exception e) {
            log.error("Enhanced ETL Pipeline failed", e);

            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);

            return EnhancedEtlResult.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .duration(duration)
                    .originalDocumentCount(documents.size())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 執行簡化版 Pipeline（跳過品質檢查）
     *
     * @param documents               原始文檔列表
     * @param textCleaningConfig      文本清理配置
     * @param chunkingConfig          分塊配置
     * @param enrichmentConfig        元資料增強配置
     * @return ETL 執行結果
     */
    public EnhancedEtlResult executeSimplifiedPipeline(
            List<Document> documents,
            TextCleaningConfig textCleaningConfig,
            SemanticChunkingConfig chunkingConfig,
            MetadataEnrichmentConfig enrichmentConfig) {

        LocalDateTime startTime = LocalDateTime.now();
        log.info("Starting Simplified ETL Pipeline for {} documents", documents.size());

        try {
            // Step 1: 清理
            List<Document> cleanedDocuments = cleanDocuments(documents, textCleaningConfig);

            // Step 2: 分塊
            List<Document> chunkedDocuments = chunkDocuments(cleanedDocuments, chunkingConfig);

            // Step 3: 增強
            List<Document> enrichedDocuments = metadataEnrichmentService.enrichMetadata(
                    chunkedDocuments, enrichmentConfig);

            // Step 4: 直接載入（跳過品質檢查）
            vectorStore.add(enrichedDocuments);

            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);

            return EnhancedEtlResult.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .duration(duration)
                    .originalDocumentCount(documents.size())
                    .cleanedDocumentCount(cleanedDocuments.size())
                    .chunkedDocumentCount(chunkedDocuments.size())
                    .enrichedDocumentCount(enrichedDocuments.size())
                    .loadedDocumentCount(enrichedDocuments.size())
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Simplified ETL Pipeline failed", e);

            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);

            return EnhancedEtlResult.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .duration(duration)
                    .originalDocumentCount(documents.size())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Step 1: 清理文檔
     */
    private List<Document> cleanDocuments(List<Document> documents, TextCleaningConfig config) {
        return documents.stream()
                .map(doc -> {
                    String cleanedContent = textCleaningService.cleanText(doc.getFormattedContent(), config);
                    return Document.builder()
                            .id(doc.getId())
                            .text(cleanedContent)
                            .metadata(doc.getMetadata())
                            .build();
                })
                .filter(doc -> !doc.getFormattedContent().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Step 2: 分塊文檔
     */
    private List<Document> chunkDocuments(List<Document> documents, SemanticChunkingConfig config) {
        // 使用 Spring AI 的 TokenTextSplitter 進行分塊
        // 建構參數：defaultChunkSize, minChunkSizeChars, minChunkLengthToEmbed, maxNumChunks, keepSeparator
        TokenTextSplitter splitter = new TokenTextSplitter(
                config.getMaxTokensPerChunk(),
                config.getMinChunkSize(),
                config.getOverlapTokens(),
                10000, // maxNumChunks - 最大分塊數量
                config.isPreserveSentences()
        );

        List<Document> chunkedDocuments = new ArrayList<>();

        for (Document doc : documents) {
            try {
                List<Document> chunks = splitter.apply(List.of(doc));
                chunkedDocuments.addAll(chunks);
            } catch (Exception e) {
                log.error("Error chunking document: {}", doc.getId(), e);
                // 如果分塊失敗，保留原始文檔
                chunkedDocuments.add(doc);
            }
        }

        return chunkedDocuments;
    }

    /**
     * 根據品質結果過濾文檔
     */
    private List<Document> filterByQuality(List<Document> documents, List<EmbeddingQuality> qualityResults) {
        List<Document> filteredDocuments = new ArrayList<>();

        for (int i = 0; i < documents.size() && i < qualityResults.size(); i++) {
            if (qualityResults.get(i).isValid()) {
                filteredDocuments.add(documents.get(i));
            }
        }

        return filteredDocuments;
    }

    /**
     * 計算處理速度
     */
    private double calculateSpeed(int count, Duration duration) {
        if (duration.isZero()) {
            return 0.0;
        }
        double seconds = duration.getSeconds() + duration.getNano() / 1_000_000_000.0;
        return count / seconds;
    }

    /**
     * 計算通過率
     */
    private double calculatePassRate(int passed, int total) {
        if (total == 0) {
            return 0.0;
        }
        return (passed * 100.0) / total;
    }

    /**
     * 批次處理多個文檔集合
     *
     * @param documentBatches         文檔批次列表
     * @param textCleaningConfig      文本清理配置
     * @param chunkingConfig          分塊配置
     * @param enrichmentConfig        元資料增強配置
     * @return ETL 執行結果列表
     */
    public List<EnhancedEtlResult> executeBatchPipeline(
            List<List<Document>> documentBatches,
            TextCleaningConfig textCleaningConfig,
            SemanticChunkingConfig chunkingConfig,
            MetadataEnrichmentConfig enrichmentConfig) {

        log.info("Starting Batch ETL Pipeline for {} batches", documentBatches.size());

        List<EnhancedEtlResult> results = new ArrayList<>();

        for (int i = 0; i < documentBatches.size(); i++) {
            log.info("Processing batch {}/{}", i + 1, documentBatches.size());
            EnhancedEtlResult result = executeEnhancedPipeline(
                    documentBatches.get(i),
                    textCleaningConfig,
                    chunkingConfig,
                    enrichmentConfig
            );
            results.add(result);
        }

        log.info("Batch ETL Pipeline completed. Total batches: {}, Success: {}",
                documentBatches.size(),
                results.stream().filter(EnhancedEtlResult::isSuccess).count());

        return results;
    }
}
