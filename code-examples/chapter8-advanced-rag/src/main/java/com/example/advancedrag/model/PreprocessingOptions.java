package com.example.advancedrag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本預處理選項
 *
 * 定義 Embedding 生成前的文本預處理配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreprocessingOptions {

    /**
     * 是否移除特殊字符
     */
    @Builder.Default
    private Boolean removeSpecialChars = true;

    /**
     * 是否正規化空白字符
     */
    @Builder.Default
    private Boolean normalizeWhitespace = true;

    /**
     * 是否轉換為小寫
     */
    @Builder.Default
    private Boolean toLowerCase = false;

    /**
     * 是否移除停用詞
     */
    @Builder.Default
    private Boolean removeStopWords = false;

    /**
     * 是否進行詞幹提取
     */
    @Builder.Default
    private Boolean applyStemming = false;

    /**
     * 是否移除 HTML 標籤
     */
    @Builder.Default
    private Boolean removeHtmlTags = true;

    /**
     * 是否移除 URL
     */
    @Builder.Default
    private Boolean removeUrls = false;

    /**
     * 是否移除 Email
     */
    @Builder.Default
    private Boolean removeEmails = false;

    /**
     * 是否移除數字
     */
    @Builder.Default
    private Boolean removeNumbers = false;

    /**
     * 是否移除標點符號
     */
    @Builder.Default
    private Boolean removePunctuation = false;

    /**
     * 是否移除多餘換行
     */
    @Builder.Default
    private Boolean removeExtraNewlines = true;

    /**
     * 最大長度限制（0 表示不限制）
     */
    @Builder.Default
    private Integer maxLength = 0;

    /**
     * 最小長度限制（0 表示不限制）
     */
    @Builder.Default
    private Integer minLength = 0;

    /**
     * 是否截斷過長文本
     */
    @Builder.Default
    private Boolean truncateLongText = true;

    /**
     * 截斷策略（start, end, middle）
     */
    @Builder.Default
    private String truncationStrategy = "end";

    /**
     * 語言代碼（用於停用詞和詞幹提取）
     */
    @Builder.Default
    private String language = "zh";

    /**
     * 創建默認選項（基本清理）
     */
    public static PreprocessingOptions defaultOptions() {
        return PreprocessingOptions.builder().build();
    }

    /**
     * 創建最小處理選項（幾乎不做處理）
     */
    public static PreprocessingOptions minimal() {
        return PreprocessingOptions.builder()
                .removeSpecialChars(false)
                .normalizeWhitespace(true)
                .toLowerCase(false)
                .removeStopWords(false)
                .applyStemming(false)
                .removeHtmlTags(true)
                .removeUrls(false)
                .removeEmails(false)
                .removeNumbers(false)
                .removePunctuation(false)
                .removeExtraNewlines(true)
                .build();
    }

    /**
     * 創建完整處理選項（深度清理）
     */
    public static PreprocessingOptions comprehensive() {
        return PreprocessingOptions.builder()
                .removeSpecialChars(true)
                .normalizeWhitespace(true)
                .toLowerCase(true)
                .removeStopWords(true)
                .applyStemming(true)
                .removeHtmlTags(true)
                .removeUrls(true)
                .removeEmails(true)
                .removeNumbers(false)
                .removePunctuation(false)
                .removeExtraNewlines(true)
                .maxLength(4000)
                .truncateLongText(true)
                .build();
    }

    /**
     * 創建文檔索引專用選項
     */
    public static PreprocessingOptions forIndexing() {
        return PreprocessingOptions.builder()
                .removeSpecialChars(true)
                .normalizeWhitespace(true)
                .toLowerCase(false)
                .removeStopWords(false)
                .applyStemming(false)
                .removeHtmlTags(true)
                .removeUrls(false)
                .removeEmails(false)
                .removeNumbers(false)
                .removePunctuation(false)
                .removeExtraNewlines(true)
                .maxLength(8000)
                .truncateLongText(true)
                .truncationStrategy("end")
                .build();
    }

    /**
     * 創建查詢專用選項
     */
    public static PreprocessingOptions forQuery() {
        return PreprocessingOptions.builder()
                .removeSpecialChars(true)
                .normalizeWhitespace(true)
                .toLowerCase(false)
                .removeStopWords(false)
                .applyStemming(false)
                .removeHtmlTags(true)
                .removeUrls(false)
                .removeEmails(false)
                .removeNumbers(false)
                .removePunctuation(false)
                .removeExtraNewlines(true)
                .maxLength(2000)
                .truncateLongText(true)
                .build();
    }

    /**
     * 驗證選項配置
     */
    public boolean isValid() {
        if (maxLength > 0 && minLength > 0 && maxLength < minLength) {
            return false;
        }

        if (truncationStrategy != null &&
            !truncationStrategy.equals("start") &&
            !truncationStrategy.equals("end") &&
            !truncationStrategy.equals("middle")) {
            return false;
        }

        return true;
    }

    /**
     * 合併另一個選項配置（以當前配置為主，用另一個配置填充 null 值）
     */
    public void mergeWith(PreprocessingOptions other) {
        if (this.removeSpecialChars == null) this.removeSpecialChars = other.removeSpecialChars;
        if (this.normalizeWhitespace == null) this.normalizeWhitespace = other.normalizeWhitespace;
        if (this.toLowerCase == null) this.toLowerCase = other.toLowerCase;
        if (this.removeStopWords == null) this.removeStopWords = other.removeStopWords;
        if (this.applyStemming == null) this.applyStemming = other.applyStemming;
        if (this.removeHtmlTags == null) this.removeHtmlTags = other.removeHtmlTags;
        if (this.removeUrls == null) this.removeUrls = other.removeUrls;
        if (this.removeEmails == null) this.removeEmails = other.removeEmails;
        if (this.removeNumbers == null) this.removeNumbers = other.removeNumbers;
        if (this.removePunctuation == null) this.removePunctuation = other.removePunctuation;
        if (this.removeExtraNewlines == null) this.removeExtraNewlines = other.removeExtraNewlines;
        if (this.maxLength == null || this.maxLength == 0) this.maxLength = other.maxLength;
        if (this.minLength == null || this.minLength == 0) this.minLength = other.minLength;
        if (this.truncateLongText == null) this.truncateLongText = other.truncateLongText;
        if (this.truncationStrategy == null) this.truncationStrategy = other.truncationStrategy;
        if (this.language == null) this.language = other.language;
    }
}
