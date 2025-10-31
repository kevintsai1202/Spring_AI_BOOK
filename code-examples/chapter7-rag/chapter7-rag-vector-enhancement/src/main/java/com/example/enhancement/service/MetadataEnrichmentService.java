package com.example.enhancement.service;

import com.example.enhancement.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher.SummaryType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 元資料增強服務
 *
 * 提供全面的文檔元資料增強功能：
 * 1. 基礎元資料（時間戳、哈希、ID）
 * 2. 語言檢測
 * 3. 內容統計（字數、句子數、token 估算）
 * 4. 自定義分類
 *
 * 注意：AI 關鍵詞提取和摘要生成功能需要額外的 Spring AI 組件
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetadataEnrichmentService {

    private final ChatModel chatModel;
    private final LanguageDetectionService languageDetector;

    /**
     * 增強關鍵詞
     *
     * 使用 Spring AI KeywordMetadataEnricher 從文檔內容中提取關鍵詞
     * 提取的關鍵詞會添加到元資料的 "excerpt_keywords" 鍵下
     *
     * @param documents    文檔列表
     * @param keywordCount 關鍵詞數量
     * @return 增強後的文檔列表
     */
    public List<Document> enrichWithKeywords(List<Document> documents, int keywordCount) {
        log.info("Keyword enrichment for {} documents with {} keywords per document",
                 documents.size(), keywordCount);

        try {
            // 使用 Spring AI KeywordMetadataEnricher
            // 直接使用構造函數，傳入 chatModel 和 keywordCount
            KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel, keywordCount);

            List<Document> enrichedDocuments = enricher.apply(documents);
            log.info("Keyword enrichment completed for {} documents", enrichedDocuments.size());

            return enrichedDocuments;
        } catch (Exception e) {
            log.error("Error during keyword enrichment, returning original documents", e);
            return documents;
        }
    }

    /**
     * 增強摘要
     *
     * 使用 Spring AI SummaryMetadataEnricher 為文檔生成摘要
     * 可以生成當前文檔、前一文檔、後一文檔的摘要
     *
     * 添加的元資料鍵：
     * - section_summary：當前文檔摘要
     * - prev_section_summary：前一文檔摘要（如適用）
     * - next_section_summary：後一文檔摘要（如適用）
     *
     * @param documents    文檔列表
     * @param summaryTypes 摘要類型列表
     * @return 增強後的文檔列表
     */
    public List<Document> enrichWithSummaries(List<Document> documents,
                                               List<SummaryType> summaryTypes) {
        log.info("Summary enrichment for {} documents with types: {}",
                 documents.size(), summaryTypes);

        try {
            // 使用 Spring AI SummaryMetadataEnricher
            // 直接使用構造函數，傳入 chatModel 和 summaryTypes
            SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(chatModel, summaryTypes);

            List<Document> enrichedDocuments = enricher.apply(documents);
            log.info("Summary enrichment completed for {} documents", enrichedDocuments.size());

            return enrichedDocuments;
        } catch (Exception e) {
            log.error("Error during summary enrichment, returning original documents", e);
            return documents;
        }
    }

    /**
     * 綜合元資料增強
     *
     * @param documents 文檔列表
     * @param config    增強配置
     * @return 增強後的文檔列表
     */
    public List<Document> enrichMetadata(List<Document> documents,
                                          MetadataEnrichmentConfig config) {
        log.info("Starting comprehensive metadata enrichment for {} documents", documents.size());

        List<Document> enrichedDocuments = new ArrayList<>(documents);

        // 1. 基礎元資料增強
        if (config.isEnableBasicMetadata()) {
            enrichedDocuments = enrichBasicMetadata(enrichedDocuments);
            log.info("Basic metadata enrichment completed");
        }

        // 2. 語言檢測
        if (config.isEnableLanguageDetection()) {
            enrichedDocuments = enrichLanguageMetadata(enrichedDocuments);
            log.info("Language detection completed");
        }

        // 3. 內容統計
        if (config.isEnableContentStatistics()) {
            enrichedDocuments = enrichContentStatistics(enrichedDocuments);
            log.info("Content statistics enrichment completed");
        }

        // 4. 關鍵詞提取（使用 AI）
        if (config.isEnableKeywordExtraction() && config.getKeywordCount() > 0) {
            enrichedDocuments = enrichWithKeywords(enrichedDocuments, config.getKeywordCount());
            log.info("AI keyword extraction completed");
        }

        // 5. 摘要生成（使用 AI）
        if (config.isEnableSummaryGeneration() && config.getSummaryTypes() != null && !config.getSummaryTypes().isEmpty()) {
            enrichedDocuments = enrichWithSummaries(enrichedDocuments, config.getSummaryTypes());
            log.info("AI summary generation completed");
        }

        // 6. 自定義分類
        if (config.isEnableCustomClassification() && config.getCustomClassifiers() != null) {
            enrichedDocuments = enrichCustomClassification(
                    enrichedDocuments,
                    config.getCustomClassifiers()
            );
            log.info("Custom classification completed");
        }

        log.info("Comprehensive metadata enrichment completed for {} documents", enrichedDocuments.size());
        return enrichedDocuments;
    }

    /**
     * 基礎元資料增強
     *
     * @param documents 文檔列表
     * @return 增強後的文檔列表
     */
    private List<Document> enrichBasicMetadata(List<Document> documents) {
        return documents.stream()
                .map(this::enrichBasicMetadataForDocument)
                .collect(Collectors.toList());
    }

    /**
     * 為單個文檔增強基礎元資料
     *
     * @param document 文檔
     * @return 增強後的文檔
     */
    private Document enrichBasicMetadataForDocument(Document document) {
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());

        // 處理時間戳
        metadata.put("processed_at", LocalDateTime.now().toString());

        // 內容哈希（用於去重）
        metadata.put("content_hash", calculateContentHash(document.getFormattedContent()));

        // 文檔 ID
        if (!metadata.containsKey("id")) {
            metadata.put("id", UUID.randomUUID().toString());
        }

        return Document.builder()
                .id(document.getId())
                .text(document.getFormattedContent())
                .metadata(metadata)
                .build();
    }

    /**
     * 語言檢測增強
     *
     * @param documents 文檔列表
     * @return 增強後的文檔列表
     */
    private List<Document> enrichLanguageMetadata(List<Document> documents) {
        return documents.stream()
                .map(this::detectLanguageForDocument)
                .collect(Collectors.toList());
    }

    /**
     * 為單個文檔檢測語言
     *
     * @param document 文檔
     * @return 增強後的文檔
     */
    private Document detectLanguageForDocument(Document document) {
        LanguageDetectionResult detection = languageDetector.detectLanguage(document.getFormattedContent());

        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        metadata.put("detected_language", detection.getLanguage());
        metadata.put("language_confidence", detection.getConfidence());

        return Document.builder()
                .id(document.getId())
                .text(document.getFormattedContent())
                .metadata(metadata)
                .build();
    }

    /**
     * 內容統計增強
     *
     * @param documents 文檔列表
     * @return 增強後的文檔列表
     */
    private List<Document> enrichContentStatistics(List<Document> documents) {
        return documents.stream()
                .map(this::calculateContentStatistics)
                .collect(Collectors.toList());
    }

    /**
     * 計算單個文檔的內容統計
     *
     * @param document 文檔
     * @return 增強後的文檔
     */
    private Document calculateContentStatistics(Document document) {
        String content = document.getFormattedContent();

        Map<String, Object> metadata = new HashMap<>(document.getMetadata());

        // 基本統計
        metadata.put("character_count", content.length());
        metadata.put("word_count", countWords(content));
        metadata.put("sentence_count", countSentences(content));
        metadata.put("paragraph_count", countParagraphs(content));
        metadata.put("estimated_tokens", estimateTokenCount(content));

        // 內容特徵
        metadata.put("has_code_blocks", containsCodeBlocks(content));
        metadata.put("has_tables", containsTables(content));
        metadata.put("has_urls", containsUrls(content));
        metadata.put("has_emails", containsEmails(content));

        return Document.builder()
                .id(document.getId())
                .text(document.getFormattedContent())
                .metadata(metadata)
                .build();
    }

    /**
     * 自定義分類增強
     *
     * @param documents   文檔列表
     * @param classifiers 分類器列表
     * @return 增強後的文檔列表
     */
    private List<Document> enrichCustomClassification(List<Document> documents,
                                                       List<DocumentClassifier> classifiers) {
        return documents.stream()
                .map(doc -> classifyDocument(doc, classifiers))
                .collect(Collectors.toList());
    }

    /**
     * 對單個文檔進行分類
     *
     * @param document    文檔
     * @param classifiers 分類器列表
     * @return 分類後的文檔
     */
    private Document classifyDocument(Document document, List<DocumentClassifier> classifiers) {
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());

        for (DocumentClassifier classifier : classifiers) {
            try {
                ClassificationResult result = classifier.classify(document.getFormattedContent());
                metadata.put(classifier.getMetadataKey(), result.getCategory());
                metadata.put(classifier.getMetadataKey() + "_confidence", result.getConfidence());
            } catch (Exception e) {
                log.error("Error applying classifier: {}", classifier.getMetadataKey(), e);
            }
        }

        return Document.builder()
                .id(document.getId())
                .text(document.getFormattedContent())
                .metadata(metadata)
                .build();
    }

    // ========== 輔助方法 ==========

    /**
     * 計算內容哈希值
     */
    private String calculateContentHash(String content) {
        return DigestUtils.md5Hex(content);
    }

    /**
     * 統計單詞數量
     */
    private int countWords(String text) {
        return text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    }

    /**
     * 統計句子數量
     */
    private int countSentences(String text) {
        return text.split("[.!?。！？]+").length;
    }

    /**
     * 統計段落數量
     */
    private int countParagraphs(String text) {
        return text.split("\\n\\s*\\n").length;
    }

    /**
     * 估算 token 數量（簡單估算：字符數 / 4）
     */
    private int estimateTokenCount(String text) {
        return (int) Math.ceil(text.length() * 0.25);
    }

    /**
     * 檢查是否包含程式碼區塊
     */
    private boolean containsCodeBlocks(String text) {
        return text.contains("```") || (text.contains("    ") && text.contains("\n"));
    }

    /**
     * 檢查是否包含表格
     */
    private boolean containsTables(String text) {
        return text.contains("|") && text.contains("\n");
    }

    /**
     * 檢查是否包含 URL
     */
    private boolean containsUrls(String text) {
        return text.matches(".*https?://.*");
    }

    /**
     * 檢查是否包含電子郵件
     */
    private boolean containsEmails(String text) {
        return text.matches(".*[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}.*");
    }

}
