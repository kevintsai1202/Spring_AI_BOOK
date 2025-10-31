package com.example.enhancement.service;

import com.example.enhancement.model.LanguageDetectionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 語言檢測服務
 *
 * 基於簡單規則檢測文本語言
 * 支援：中文、英文、日文
 */
@Service
@Slf4j
public class LanguageDetectionService {

    /**
     * 中文字符正則（包括繁體和簡體）
     */
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");

    /**
     * 日文字符正則（平假名、片假名）
     */
    private static final Pattern JAPANESE_PATTERN = Pattern.compile("[\\u3040-\\u309f\\u30a0-\\u30ff]");

    /**
     * 英文字符正則
     */
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]");

    /**
     * 檢測文本語言
     *
     * @param text 待檢測文本
     * @return 語言檢測結果
     */
    public LanguageDetectionResult detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return LanguageDetectionResult.builder()
                    .language("unknown")
                    .confidence(0.0)
                    .method("rule-based")
                    .build();
        }

        // 統計各語言字符數量
        Map<String, Integer> languageScores = new HashMap<>();
        languageScores.put("zh", countMatches(text, CHINESE_PATTERN));
        languageScores.put("ja", countMatches(text, JAPANESE_PATTERN));
        languageScores.put("en", countMatches(text, ENGLISH_PATTERN));

        // 找出分數最高的語言
        String detectedLanguage = "unknown";
        int maxScore = 0;
        int totalChars = text.replaceAll("\\s", "").length();

        for (Map.Entry<String, Integer> entry : languageScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                detectedLanguage = entry.getKey();
            }
        }

        // 計算置信度
        double confidence = totalChars > 0 ? (double) maxScore / totalChars : 0.0;

        log.debug("Detected language: {} with confidence: {}", detectedLanguage, confidence);

        return LanguageDetectionResult.builder()
                .language(detectedLanguage)
                .confidence(confidence)
                .method("rule-based")
                .build();
    }

    /**
     * 計算正則表達式匹配次數
     *
     * @param text    文本
     * @param pattern 正則表達式
     * @return 匹配次數
     */
    private int countMatches(String text, Pattern pattern) {
        var matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
