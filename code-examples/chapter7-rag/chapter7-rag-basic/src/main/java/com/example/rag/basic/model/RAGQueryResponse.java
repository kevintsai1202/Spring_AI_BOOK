package com.example.rag.basic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RAG 查詢響應模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGQueryResponse {

    /**
     * 原始問題
     */
    private String question;

    /**
     * 生成的答案
     */
    private String answer;

    /**
     * 引用來源列表
     */
    private List<DocumentSource> sources;

    /**
     * 處理時間 (毫秒)
     */
    private long processingTimeMs;

    /**
     * 時間戳
     */
    private LocalDateTime timestamp;

}
