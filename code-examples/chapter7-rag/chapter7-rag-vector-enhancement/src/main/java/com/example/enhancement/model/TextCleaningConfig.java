package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文本清理配置類
 *
 * 定義文本清理的各種選項和規則
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TextCleaningConfig {

    /**
     * 是否移除多餘空白字符
     */
    @Builder.Default
    private boolean removeExtraWhitespace = true;

    /**
     * 是否移除特殊字符
     */
    @Builder.Default
    private boolean removeSpecialCharacters = false;

    /**
     * 是否標準化換行符
     */
    @Builder.Default
    private boolean normalizeLineBreaks = true;

    /**
     * 是否移除敏感資訊（郵件、電話、身分證等）
     */
    @Builder.Default
    private boolean removeSensitiveInfo = true;

    /**
     * 語言代碼（zh-TW, en, ja 等）
     */
    private String language;

    /**
     * 自定義清理規則列表
     */
    private List<TextCleaningRule> customRules;
}
