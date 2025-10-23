/**
 * 文檔處理服務
 * 負責文檔解析、清理和標準化處理
 */
package com.example.service;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class DocumentProcessingService {
    
    public Document processDocument(Document rawDocument) {
        // 1. 提取純文本
        String cleanText = extractText(rawDocument);
        
        // 2. 清理和標準化
        cleanText = cleanAndNormalize(cleanText);
        
        // 3. 提取元資料
        Map<String, Object> metadata = extractMetadata(rawDocument);
        
        return new Document(cleanText, metadata);
    }
    
    private String extractText(Document document) {
        // 提取文檔中的純文本內容
        return document.getContent();
    }
    
    private String cleanAndNormalize(String text) {
        return text
            .replaceAll("\\s+", " ")  // 標準化空白字符
            .replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "")  // 移除特殊字符
            .trim();
    }
    
    private Map<String, Object> extractMetadata(Document document) {
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        
        // 添加處理時間戳
        metadata.put("processed_at", System.currentTimeMillis());
        
        // 添加文檔長度
        metadata.put("content_length", document.getContent().length());
        
        // 添加文檔類型
        metadata.put("document_type", detectDocumentType(document));
        
        return metadata;
    }
    
    private String detectDocumentType(Document document) {
        // 簡單的文檔類型檢測
        String content = document.getContent();
        
        if (content.contains("<html>") || content.contains("<HTML>")) {
            return "html";
        } else if (content.contains("{") && content.contains("}")) {
            return "json";
        } else {
            return "text";
        }
    }
}