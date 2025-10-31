package com.example.advancedrag.dto;

import com.example.advancedrag.model.RAGQueryOptions;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Advanced RAG 查詢請求
 *
 * 客戶端發送的 RAG 查詢請求數據
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedRAGRequest {

    /**
     * 用戶查詢問題
     */
    @NotBlank(message = "查詢問題不能為空")
    private String query;

    /**
     * 會話 ID（用於追蹤對話上下文）
     */
    private String sessionId;

    /**
     * 用戶 ID（用於個性化和審核）
     */
    private String userId;

    /**
     * 查詢選項（可選，使用默認配置如果未提供）
     */
    private RAGQueryOptions options;

    /**
     * 是否啟用內容審核
     */
    @Builder.Default
    private Boolean enableModeration = true;

    /**
     * 是否啟用查詢重寫
     */
    @Builder.Default
    private Boolean enableQueryRewrite = true;

    /**
     * 是否啟用查詢擴展
     */
    @Builder.Default
    private Boolean enableQueryExpansion = false;

    /**
     * 是否返回檢索到的文檔
     */
    @Builder.Default
    private Boolean returnDocuments = true;

    /**
     * 是否返回評分細節
     */
    @Builder.Default
    private Boolean returnScoringDetails = false;

    /**
     * 額外的元數據
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 獲取查詢選項（如果為 null 則返回默認值）
     */
    public RAGQueryOptions getOptionsOrDefault() {
        return options != null ? options : RAGQueryOptions.builder().build();
    }
}
