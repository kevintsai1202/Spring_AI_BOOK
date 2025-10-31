package com.example.rag.basic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * RAG 查詢請求模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGQueryRequest {

    /**
     * 用戶問題
     */
    private String question;

    /**
     * 檢索文檔數量 (Top-K)
     */
    @Builder.Default
    private int topK = 5;

    /**
     * 相似度閾值 (0-1)
     */
    @Builder.Default
    private double similarityThreshold = 0.7;

    /**
     * 元資料過濾條件
     */
    private Map<String, Object> filters;

}
