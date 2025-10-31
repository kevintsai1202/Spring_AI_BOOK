package com.example.etl.service;

import com.example.etl.model.ChunkingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文檔分塊服務
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentChunkingService {

    /**
     * 對文檔進行分塊處理
     *
     * @param documents 原始文檔列表
     * @param config 分塊配置
     * @return 分塊後的文檔列表
     */
    public List<Document> chunkDocuments(List<Document> documents, ChunkingConfig config) {
        log.info("開始文檔分塊: {} 個文檔", documents.size());

        if (config == null) {
            config = ChunkingConfig.builder().build();
        }

        // 使用 Spring AI 的 TokenTextSplitter
        TokenTextSplitter splitter = new TokenTextSplitter(
                config.getDefaultChunkSize(),
                config.getMinChunkSizeChars(),
                config.getMinChunkLengthToEmbed(),
                config.getMaxNumChunks(),
                config.isKeepSeparator()
        );

        // 執行分塊
        List<Document> chunkedDocuments = splitter.apply(documents);

        // 添加分塊索引元資料
        for (int i = 0; i < chunkedDocuments.size(); i++) {
            Document doc = chunkedDocuments.get(i);
            doc.getMetadata().put("chunk_index", i);
            doc.getMetadata().put("chunk_method", "token");
            doc.getMetadata().put("chunk_size", config.getDefaultChunkSize());
        }

        log.info("文檔分塊完成: {} 個原始文檔 -> {} 個分塊",
                documents.size(), chunkedDocuments.size());

        return chunkedDocuments;
    }
}
