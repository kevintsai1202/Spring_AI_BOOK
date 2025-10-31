package com.example.rag.basic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 文檔來源模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSource {

    /**
     * 文檔 ID
     */
    private String documentId;

    /**
     * 文檔標題
     */
    private String title;

    /**
     * 摘錄內容
     */
    private String excerpt;

    /**
     * 相關性分數 (0-1)
     */
    private double relevanceScore;

    /**
     * 元資料
     */
    private Map<String, Object> metadata;

}
