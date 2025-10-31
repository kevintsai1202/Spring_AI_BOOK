package com.example.rag.basic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文檔處理服務
 *
 * 負責:
 * 1. 讀取各種格式的文檔
 * 2. 文本分塊
 * 3. 元資料增強
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingService {

    private final TokenTextSplitter textSplitter;

    /**
     * 處理單個文檔
     */
    public List<Document> process(Resource resource) {
        try {
            log.info("Processing document: {}", resource.getFilename());

            // 1. 讀取文檔內容
            DocumentReader reader = createDocumentReader(resource);
            List<Document> documents = reader.get();

            // 2. 文本分割
            List<Document> chunks = textSplitter.apply(documents);

            // 3. 添加元資料
            chunks.forEach(doc -> enrichMetadata(doc, resource));

            log.info("Document processed: {} chunks created", chunks.size());
            return chunks;

        } catch (Exception e) {
            log.error("Failed to process document: {}", resource.getFilename(), e);
            throw new RuntimeException("文檔處理失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 創建文檔讀取器
     */
    private DocumentReader createDocumentReader(Resource resource) {
        String filename = resource.getFilename();
        if (filename == null) {
            throw new IllegalArgumentException("文件名不能為空");
        }

        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".pdf")) {
            // PDF 文檔讀取器
            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPageTopMargin(0)
                    .withPageBottomMargin(0)
                    .withPagesPerDocument(1)
                    .build();

            return new PagePdfDocumentReader(resource, config);

        } else if (lowerFilename.endsWith(".txt")) {
            // 文本文件讀取器
            return new TextReader(resource);

        } else {
            throw new UnsupportedOperationException("不支援的文件格式: " + filename);
        }
    }

    /**
     * 元資料增強
     */
    private void enrichMetadata(Document document, Resource resource) {
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());

        // 基礎元資料
        metadata.put("source_file", resource.getFilename());
        metadata.put("processed_at", LocalDateTime.now().toString());
        metadata.put("document_type", getDocumentType(resource.getFilename()));

        // 文檔 ID
        if (!metadata.containsKey("id")) {
            metadata.put("id", UUID.randomUUID().toString());
        }

        // 內容統計
        String content = document.getText();
        metadata.put("character_count", content.length());
        metadata.put("word_count", countWords(content));

        document.getMetadata().putAll(metadata);
    }

    /**
     * 取得文檔類型
     */
    private String getDocumentType(String filename) {
        if (filename == null) {
            return "UNKNOWN";
        }

        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".pdf")) {
            return "PDF";
        } else if (lowerFilename.endsWith(".txt")) {
            return "TEXT";
        } else if (lowerFilename.endsWith(".md")) {
            return "MARKDOWN";
        }

        return "UNKNOWN";
    }

    /**
     * 計算單詞數
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

}
