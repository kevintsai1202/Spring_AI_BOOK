package com.example.advancedrag.dto;

import com.example.advancedrag.model.PreprocessingOptions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 添加文檔請求
 *
 * 客戶端添加文檔到向量數據庫的請求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAddRequest {

    /**
     * 文檔列表
     */
    @NotEmpty(message = "文檔列表不能為空")
    private List<DocumentItem> documents;

    /**
     * 預處理選項（可選）
     */
    private PreprocessingOptions preprocessingOptions;

    /**
     * 是否異步處理
     */
    @Builder.Default
    private Boolean async = false;

    /**
     * 批次大小（異步處理時使用）
     */
    @Builder.Default
    private Integer batchSize = 10;

    /**
     * 文檔項內部類
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentItem {
        /**
         * 文檔 ID（可選，不提供則自動生成）
         */
        private String documentId;

        /**
         * 文檔內容
         */
        @NotBlank(message = "文檔內容不能為空")
        private String content;

        /**
         * 文檔標題（可選）
         */
        private String title;

        /**
         * 文檔來源（可選）
         */
        private String source;

        /**
         * 文檔類型（可選）
         */
        private String type;

        /**
         * 文檔作者（可選）
         */
        private String author;

        /**
         * 文檔標籤（可選）
         */
        @Builder.Default
        private List<String> tags = new ArrayList<>();

        /**
         * 文檔元數據
         */
        @Builder.Default
        private Map<String, Object> metadata = new HashMap<>();

        /**
         * 添加標籤
         */
        public void addTag(String tag) {
            if (this.tags == null) {
                this.tags = new ArrayList<>();
            }
            this.tags.add(tag);
        }

        /**
         * 添加元數據
         */
        public void addMetadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
        }
    }

    /**
     * 添加文檔項
     */
    public void addDocument(DocumentItem document) {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
        this.documents.add(document);
    }

    /**
     * 獲取文檔數量
     */
    public int getDocumentCount() {
        return documents != null ? documents.size() : 0;
    }
}
