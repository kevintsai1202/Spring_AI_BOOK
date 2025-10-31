package com.example.etl.service;

import com.example.etl.exception.EtlPipelineException;
import com.example.etl.model.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ETL Pipeline 核心服務
 * 實現完整的 Extract-Transform-Load 流程
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EtlPipelineService {

    private final DocumentChunkingService chunkingService;
    private final MetadataEnrichmentService metadataEnrichmentService;
    private final MultiFormatDocumentReader documentReader;
    private final VectorStore vectorStore;
    private final MeterRegistry meterRegistry;

    /**
     * 執行完整的 ETL Pipeline
     *
     * @param config ETL 配置
     * @return ETL 執行結果
     */
    public EtlPipelineResult executeEtlPipeline(EtlPipelineConfig config) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            log.info("=== 開始執行 ETL Pipeline ===");

            // Phase 1: Extract - 提取文檔
            List<Document> extractedDocuments = extractDocuments(config.getDataSources());
            log.info("Extract Phase 完成: 提取 {} 個文檔", extractedDocuments.size());

            // Phase 2: Transform - 轉換處理
            List<Document> transformedDocuments = transformDocuments(extractedDocuments, config);
            log.info("Transform Phase 完成: 轉換為 {} 個文檔", transformedDocuments.size());

            // Phase 3: Load - 載入向量資料庫
            loadDocuments(transformedDocuments, config.getLoadConfig());
            log.info("Load Phase 完成: 載入 {} 個文檔", transformedDocuments.size());

            // 建立結果
            long processingTime = sample.stop(Timer.builder("etl.pipeline.time")
                    .register(meterRegistry));

            EtlPipelineResult result = EtlPipelineResult.builder()
                    .success(true)
                    .extractedCount(extractedDocuments.size())
                    .transformedCount(transformedDocuments.size())
                    .loadedCount(transformedDocuments.size())
                    .processingTime(processingTime)
                    .metrics(buildMetrics(extractedDocuments, transformedDocuments))
                    .build();

            meterRegistry.counter("etl.pipeline.success").increment();
            log.info("=== ETL Pipeline 執行成功 ===");

            return result;

        } catch (Exception e) {
            log.error("ETL Pipeline 執行失敗", e);
            meterRegistry.counter("etl.pipeline.errors").increment();

            return EtlPipelineResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Phase 1: Extract - 提取文檔
     */
    private List<Document> extractDocuments(List<DataSource> dataSources) {
        log.info("--- Phase 1: Extract ---");
        List<Document> allDocuments = new ArrayList<>();

        for (DataSource dataSource : dataSources) {
            try {
                log.debug("處理資料源: {}", dataSource.getName());

                List<Document> documents = documentReader.readDocument(
                        dataSource.getResource(),
                        dataSource.getType()
                );

                // 添加數據源元資料
                documents.forEach(doc -> addDataSourceMetadata(doc, dataSource));

                allDocuments.addAll(documents);
                log.info("從 {} 提取 {} 個文檔", dataSource.getName(), documents.size());

            } catch (Exception e) {
                log.error("提取失敗: {}", dataSource.getName(), e);
            }
        }

        return allDocuments;
    }

    /**
     * Phase 2: Transform - 轉換文檔
     */
    private List<Document> transformDocuments(List<Document> documents,
                                              EtlPipelineConfig config) {
        log.info("--- Phase 2: Transform ---");
        List<Document> transformedDocuments = documents;

        // 1. 元資料增強
        if (config.getEnrichmentConfig() != null) {
            transformedDocuments = metadataEnrichmentService.enrichMetadata(
                    transformedDocuments,
                    config.getEnrichmentConfig()
            );
            log.debug("元資料增強完成");
        }

        // 2. 文檔分塊
        if (config.getChunkingConfig() != null) {
            transformedDocuments = chunkingService.chunkDocuments(
                    transformedDocuments,
                    config.getChunkingConfig()
            );
            log.debug("文檔分塊完成: {} 個分塊", transformedDocuments.size());
        }

        return transformedDocuments;
    }

    /**
     * Phase 3: Load - 載入文檔
     */
    private void loadDocuments(List<Document> documents, LoadConfig loadConfig) {
        log.info("--- Phase 3: Load ---");

        if (loadConfig == null) {
            loadConfig = LoadConfig.builder().build();
        }

        if (loadConfig.getBatchSize() <= 1 || documents.size() <= loadConfig.getBatchSize()) {
            // 單批次載入
            vectorStore.add(documents);
            log.info("單批次載入完成: {} 個文檔", documents.size());
        } else {
            // 批次載入
            List<List<Document>> batches = partitionList(documents, loadConfig.getBatchSize());

            for (int i = 0; i < batches.size(); i++) {
                List<Document> batch = batches.get(i);
                try {
                    vectorStore.add(batch);
                    log.info("載入批次 {}/{}: {} 個文檔", i + 1, batches.size(), batch.size());

                    if (loadConfig.getBatchDelayMs() > 0) {
                        Thread.sleep(loadConfig.getBatchDelayMs());
                    }

                } catch (Exception e) {
                    log.error("批次 {} 載入失敗", i + 1, e);
                    if (!loadConfig.isContinueOnError()) {
                        throw new EtlPipelineException("批次載入失敗", e);
                    }
                }
            }
        }
    }

    /**
     * 添加數據源元資料
     */
    private void addDataSourceMetadata(Document document, DataSource dataSource) {
        Map<String, Object> metadata = document.getMetadata();
        metadata.put("data_source_type", dataSource.getType().name());
        metadata.put("data_source_name", dataSource.getName());
        if (dataSource.getPath() != null) {
            metadata.put("data_source_path", dataSource.getPath());
        }
    }

    /**
     * 建立處理指標
     */
    private Map<String, Object> buildMetrics(List<Document> extracted,
                                             List<Document> transformed) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("documents_extracted", extracted.size());
        metrics.put("documents_transformed", transformed.size());
        metrics.put("average_chunk_size",
                transformed.stream()
                        .mapToInt(d -> d.getText().length())
                        .average()
                        .orElse(0));
        return metrics;
    }

    /**
     * 將列表分割為多個批次
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }
}
