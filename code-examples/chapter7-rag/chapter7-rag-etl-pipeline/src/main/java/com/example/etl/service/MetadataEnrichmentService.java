package com.example.etl.service;

import com.example.etl.model.MetadataEnrichmentConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 元資料增強服務
 * 為文檔添加額外的元資料(語言檢測、統計資訊、關鍵詞、摘要等)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MetadataEnrichmentService {

    private final ChatClient.Builder chatClientBuilder;

    /**
     * 增強文檔元資料
     *
     * @param documents 文檔列表
     * @param config 增強配置
     * @return 增強後的文檔列表
     */
    public List<Document> enrichMetadata(List<Document> documents,
                                        MetadataEnrichmentConfig config) {
        log.info("開始元資料增強: {} 個文檔", documents.size());

        if (config == null) {
            return documents;
        }

        for (Document doc : documents) {
            Map<String, Object> metadata = doc.getMetadata();

            // 1. 基礎元資料
            if (config.isEnableBasicMetadata()) {
                addBasicMetadata(metadata);
            }

            // 2. 內容統計
            if (config.isEnableContentStatistics()) {
                addContentStatistics(doc, metadata);
            }

            // 3. 語言檢測(簡化版)
            if (config.isEnableLanguageDetection()) {
                addLanguageDetection(doc, metadata);
            }

            // 4. 關鍵詞提取(需要 AI - 這裡簡化處理)
            if (config.isEnableKeywordExtraction()) {
                log.debug("關鍵詞提取需要 AI 模型,暫時跳過");
                // 實際應用中會調用 AI 模型提取關鍵詞
            }

            // 5. 摘要生成(需要 AI - 這裡簡化處理)
            if (config.isEnableSummaryGeneration()) {
                log.debug("摘要生成需要 AI 模型,暫時跳過");
                // 實際應用中會調用 AI 模型生成摘要
            }
        }

        log.info("元資料增強完成");
        return documents;
    }

    /**
     * 添加基礎元資料
     */
    private void addBasicMetadata(Map<String, Object> metadata) {
        if (!metadata.containsKey("processed_at")) {
            metadata.put("processed_at", LocalDateTime.now().toString());
        }
    }

    /**
     * 添加內容統計資訊
     */
    private void addContentStatistics(Document doc, Map<String, Object> metadata) {
        String content = doc.getText();
        if (content != null) {
            metadata.put("character_count", content.length());
            metadata.put("word_count", countWords(content));
            metadata.put("estimated_tokens", estimateTokens(content));
        }
    }

    /**
     * 簡化的語言檢測
     */
    private void addLanguageDetection(Document doc, Map<String, Object> metadata) {
        String content = doc.getText();
        if (content != null) {
            // 簡單檢測:如果包含中文字符則判定為中文
            boolean hasChinese = content.matches(".*[\\u4e00-\\u9fa5]+.*");
            metadata.put("detected_language", hasChinese ? "zh" : "en");
            metadata.put("language_confidence", 0.8);
        }
    }

    /**
     * 計算單詞數
     */
    private int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.split("\\s+").length;
    }

    /**
     * 估算 Token 數量(簡化版:約 4 個字符 = 1 token)
     */
    private int estimateTokens(String text) {
        if (text == null) {
            return 0;
        }
        return text.length() / 4;
    }
}
