/**
 * 文本分塊服務
 * 負責將長文檔切分為適當大小的片段
 */
package com.example.service;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TextChunkingService {
    
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_OVERLAP = 200;
    
    public List<Document> chunkDocument(Document document) {
        String text = document.getContent();
        List<Document> chunks = new ArrayList<>();
        
        int start = 0;
        int chunkIndex = 0;
        
        while (start < text.length()) {
            int end = Math.min(start + DEFAULT_CHUNK_SIZE, text.length());
            
            // 尋找適當的分割點（句號、段落等）
            if (end < text.length()) {
                end = findBestSplitPoint(text, start, end);
            }
            
            String chunkText = text.substring(start, end);
            
            // 建立分塊文檔
            Map<String, Object> chunkMetadata = new HashMap<>(document.getMetadata());
            chunkMetadata.put("chunk_index", chunkIndex);
            chunkMetadata.put("chunk_start", start);
            chunkMetadata.put("chunk_end", end);
            
            chunks.add(new Document(chunkText, chunkMetadata));
            
            start = end - DEFAULT_OVERLAP;  // 重疊處理
            chunkIndex++;
        }
        
        return chunks;
    }
    
    /**
     * 尋找最佳分割點
     */
    private int findBestSplitPoint(String text, int start, int end) {
        // 在指定範圍內尋找句號、問號、驚嘆號
        for (int i = end - 1; i > start + DEFAULT_CHUNK_SIZE / 2; i--) {
            char c = text.charAt(i);
            if (c == '.' || c == '?' || c == '!' || c == '\n') {
                return i + 1;
            }
        }
        
        // 如果找不到句子結束符，尋找空格
        for (int i = end - 1; i > start + DEFAULT_CHUNK_SIZE / 2; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        
        // 如果都找不到，就在原位置分割
        return end;
    }
    
    /**
     * 自定義分塊大小
     */
    public List<Document> chunkDocument(Document document, int chunkSize, int overlap) {
        String text = document.getContent();
        List<Document> chunks = new ArrayList<>();
        
        int start = 0;
        int chunkIndex = 0;
        
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            
            if (end < text.length()) {
                end = findBestSplitPoint(text, start, end);
            }
            
            String chunkText = text.substring(start, end);
            
            Map<String, Object> chunkMetadata = new HashMap<>(document.getMetadata());
            chunkMetadata.put("chunk_index", chunkIndex);
            chunkMetadata.put("chunk_start", start);
            chunkMetadata.put("chunk_end", end);
            chunkMetadata.put("chunk_size", chunkSize);
            chunkMetadata.put("overlap", overlap);
            
            chunks.add(new Document(chunkText, chunkMetadata));
            
            start = end - overlap;
            chunkIndex++;
        }
        
        return chunks;
    }
}