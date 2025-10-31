package com.example.advancedrag.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文本處理工具類
 *
 * 提供文本清理、分詞、統計等功能
 */
public class TextUtil {

    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^\\p{L}\\p{N}\\s]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");

    /**
     * 清理特殊字符
     *
     * @param text 原始文本
     * @return 清理後的文本
     */
    public static String cleanSpecialChars(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        return SPECIAL_CHARS_PATTERN.matcher(text).replaceAll(" ");
    }

    /**
     * 正規化空白字符
     *
     * @param text 原始文本
     * @return 正規化後的文本
     */
    public static String normalizeWhitespace(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        return WHITESPACE_PATTERN.matcher(text.trim()).replaceAll(" ");
    }

    /**
     * 完整的文本預處理
     *
     * @param text 原始文本
     * @param cleanSpecial 是否清理特殊字符
     * @param normalizeSpace 是否正規化空白
     * @return 處理後的文本
     */
    public static String preprocess(String text, boolean cleanSpecial, boolean normalizeSpace) {
        if (StringUtils.isBlank(text)) {
            return "";
        }

        String result = text;
        if (cleanSpecial) {
            result = cleanSpecialChars(result);
        }
        if (normalizeSpace) {
            result = normalizeWhitespace(result);
        }

        return result;
    }

    /**
     * 簡單分詞（按空格分割）
     *
     * @param text 文本
     * @return 詞列表
     */
    public static List<String> tokenize(String text) {
        if (StringUtils.isBlank(text)) {
            return List.of();
        }

        return Arrays.stream(text.toLowerCase().split("\\s+"))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    /**
     * 計算詞頻
     *
     * @param text 文本
     * @param term 詞
     * @return 詞頻
     */
    public static int termFrequency(String text, String term) {
        if (StringUtils.isBlank(text) || StringUtils.isBlank(term)) {
            return 0;
        }

        String lowerText = text.toLowerCase();
        String lowerTerm = term.toLowerCase();

        int count = 0;
        int index = 0;

        while ((index = lowerText.indexOf(lowerTerm, index)) != -1) {
            count++;
            index += lowerTerm.length();
        }

        return count;
    }

    /**
     * 計算文本長度（字符數）
     *
     * @param text 文本
     * @return 字符數
     */
    public static int characterCount(String text) {
        return StringUtils.isBlank(text) ? 0 : text.length();
    }

    /**
     * 計算詞數
     *
     * @param text 文本
     * @return 詞數
     */
    public static int wordCount(String text) {
        return tokenize(text).size();
    }

    /**
     * 截斷文本
     *
     * @param text 原始文本
     * @param maxLength 最大長度
     * @return 截斷後的文本
     */
    public static String truncate(String text, int maxLength) {
        if (StringUtils.isBlank(text) || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * 檢測文本中的 Email
     *
     * @param text 文本
     * @return 是否包含 Email
     */
    public static boolean containsEmail(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(text).find();
    }

    /**
     * 移除文本中的 Email
     *
     * @param text 文本
     * @return 移除 Email 後的文本
     */
    public static String removeEmail(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        return EMAIL_PATTERN.matcher(text).replaceAll("[EMAIL]");
    }

    /**
     * 檢查文本長度是否在範圍內
     *
     * @param text 文本
     * @param minLength 最小長度
     * @param maxLength 最大長度
     * @return 是否在範圍內
     */
    public static boolean isLengthInRange(String text, int minLength, int maxLength) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        int length = text.length();
        return length >= minLength && length <= maxLength;
    }
}
