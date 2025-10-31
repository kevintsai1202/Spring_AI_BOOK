package com.example.enhancement.service;

import com.example.enhancement.model.TextCleaningConfig;
import com.example.enhancement.model.TextCleaningRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 智能文本清理服務
 *
 * 提供多語言支援的文本預處理功能，包括：
 * 1. 移除多餘空白字符
 * 2. 移除敏感資訊（郵件、電話、身分證等）
 * 3. 標準化換行符
 * 4. 語言特定清理（中文、英文、日文）
 * 5. 自定義清理規則
 */
@Service
@Slf4j
public class TextCleaningService {

    /**
     * 電子郵件正則表達式
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    /**
     * URL 正則表達式
     */
    private static final Pattern URL_PATTERN =
            Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");

    /**
     * 電話號碼正則表達式（支援多種格式）
     */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\b\\d{2,4}[-\\s]?\\d{3,4}[-\\s]?\\d{3,4}\\b");

    /**
     * 台灣身分證號碼正則表達式
     */
    private static final Pattern TW_ID_PATTERN =
            Pattern.compile("\\b[A-Z]\\d{9}\\b");

    /**
     * 信用卡號碼正則表達式
     */
    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile("\\b\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}\\b");

    /**
     * 綜合文本清理
     *
     * @param rawText 原始文本
     * @param config  清理配置
     * @return 清理後的文本
     */
    public String cleanText(String rawText, TextCleaningConfig config) {
        if (rawText == null || rawText.trim().isEmpty()) {
            log.debug("Input text is null or empty, returning empty string");
            return "";
        }

        log.debug("Starting text cleaning with config: {}", config);
        String cleanedText = rawText;

        // 1. 基礎清理 - 移除多餘空白
        if (config.isRemoveExtraWhitespace()) {
            cleanedText = removeExtraWhitespace(cleanedText);
            log.debug("Extra whitespace removed");
        }

        // 2. 移除特殊字符
        if (config.isRemoveSpecialCharacters()) {
            cleanedText = removeSpecialCharacters(cleanedText);
            log.debug("Special characters removed");
        }

        // 3. 標準化換行符
        if (config.isNormalizeLineBreaks()) {
            cleanedText = normalizeLineBreaks(cleanedText);
            log.debug("Line breaks normalized");
        }

        // 4. 移除敏感資訊
        if (config.isRemoveSensitiveInfo()) {
            cleanedText = removeSensitiveInformation(cleanedText);
            log.debug("Sensitive information removed");
        }

        // 5. 語言特定清理
        if (config.getLanguage() != null) {
            cleanedText = applyLanguageSpecificCleaning(cleanedText, config.getLanguage());
            log.debug("Language-specific cleaning applied for: {}", config.getLanguage());
        }

        // 6. 自定義清理規則
        if (config.getCustomRules() != null && !config.getCustomRules().isEmpty()) {
            cleanedText = applyCustomRules(cleanedText, config.getCustomRules());
            log.debug("Custom rules applied, count: {}", config.getCustomRules().size());
        }

        log.info("Text cleaning completed. Original length: {}, Cleaned length: {}",
                rawText.length(), cleanedText.length());

        return cleanedText.trim();
    }

    /**
     * 移除多餘空白字符
     *
     * @param text 文本
     * @return 清理後的文本
     */
    private String removeExtraWhitespace(String text) {
        return text
                // 將多個空白字符替換為單個空格
                .replaceAll("\\s+", " ")
                // 將多個連續換行替換為最多兩個換行
                .replaceAll("\\n\\s*\\n", "\n\n")
                .trim();
    }

    /**
     * 移除特殊字符
     * 保留字母、數字、標點符號、空白和換行符
     *
     * @param text 文本
     * @return 清理後的文本
     */
    private String removeSpecialCharacters(String text) {
        // \p{L} = 字母, \p{N} = 數字, \p{P} = 標點, \p{Z} = 分隔符, \r\n = 換行
        return text.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\r\\n]", "");
    }

    /**
     * 標準化換行符
     * 將所有換行符統一為 \n
     *
     * @param text 文本
     * @return 標準化後的文本
     */
    private String normalizeLineBreaks(String text) {
        return text
                .replaceAll("\\r\\n", "\n")  // Windows 換行符
                .replaceAll("\\r", "\n");     // Mac 舊式換行符
    }

    /**
     * 移除敏感資訊
     * 包括：電子郵件、URL、電話號碼、身分證號碼、信用卡號碼
     *
     * @param text 文本
     * @return 清理後的文本
     */
    private String removeSensitiveInformation(String text) {
        String result = text;

        // 移除電子郵件
        result = EMAIL_PATTERN.matcher(result).replaceAll("[EMAIL]");

        // 移除 URL
        result = URL_PATTERN.matcher(result).replaceAll("[URL]");

        // 移除電話號碼
        result = PHONE_PATTERN.matcher(result).replaceAll("[PHONE]");

        // 移除身分證號碼（台灣格式）
        result = TW_ID_PATTERN.matcher(result).replaceAll("[ID]");

        // 移除信用卡號碼
        result = CREDIT_CARD_PATTERN.matcher(result).replaceAll("[CARD]");

        return result;
    }

    /**
     * 應用語言特定清理
     *
     * @param text     文本
     * @param language 語言代碼
     * @return 清理後的文本
     */
    private String applyLanguageSpecificCleaning(String text, String language) {
        String lang = language.toLowerCase();
        if (lang.equals("zh") || lang.equals("zh-tw") || lang.equals("zh-cn")) {
            return cleanChineseText(text);
        } else if (lang.equals("en")) {
            return cleanEnglishText(text);
        } else if (lang.equals("ja")) {
            return cleanJapaneseText(text);
        } else {
            log.warn("Unsupported language: {}, skipping language-specific cleaning", language);
            return text;
        }
    }

    /**
     * 中文文本清理
     * 標準化中文標點符號
     *
     * @param text 文本
     * @return 清理後的文本
     */
    private String cleanChineseText(String text) {
        String result = text;
        // 全形空格轉半形
        result = result.replace("　", " ");
        return result;
    }

    /**
     * 英文文本清理
     * 標準化英文標點符號
     *
     * @param text 文本
     * @return 清理後的文本
     */
    private String cleanEnglishText(String text) {
        String result = text;
        // 移除多餘空格
        result = result.replaceAll("\\s+", " ");
        return result;
    }

    /**
     * 日文文本清理
     * 標準化日文標點符號
     *
     * @param text 文本
     * @return 清理後的文本
     */
    private String cleanJapaneseText(String text) {
        return text
                // 全形空格轉半形
                .replace("　", " ");
    }

    /**
     * 應用自定義清理規則
     *
     * @param text        文本
     * @param customRules 自定義規則列表
     * @return 清理後的文本
     */
    private String applyCustomRules(String text, java.util.List<TextCleaningRule> customRules) {
        String result = text;
        for (TextCleaningRule rule : customRules) {
            try {
                result = rule.apply(result);
            } catch (Exception e) {
                log.error("Error applying custom cleaning rule", e);
                // 繼續處理其他規則
            }
        }
        return result;
    }

    /**
     * 批次清理多個文本
     *
     * @param texts  文本列表
     * @param config 清理配置
     * @return 清理後的文本列表
     */
    public java.util.List<String> cleanTexts(java.util.List<String> texts, TextCleaningConfig config) {
        log.info("Starting batch text cleaning for {} texts", texts.size());
        return texts.stream()
                .map(text -> cleanText(text, config))
                .toList();
    }
}
