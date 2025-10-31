package com.example.advancedrag.service;

import com.example.advancedrag.dto.ModerationResult;
import com.example.advancedrag.properties.RAGProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定義規則審核服務
 *
 * 實現基於規則的內容審核：
 * 1. 敏感詞過濾（黑名單）
 * 2. 內容長度檢查
 * 3. 特殊字符比例檢查
 * 4. URL/郵箱限制
 * 5. 重複內容檢測
 *
 * 審核權重：50%
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomRuleModerationService {

    private final RAGProperties ragProperties;

    /**
     * 敏感詞黑名單（示例）
     * 實際應用中應該從配置文件或數據庫加載
     */
    private static final Set<String> SENSITIVE_WORDS = Set.of(
            // 暴力相關
            "暴力", "殺害", "攻擊",
            // 政治敏感
            "政治敏感詞1", "政治敏感詞2",
            // 色情相關
            "色情", "淫穢",
            // 賭博相關
            "賭博", "博彩"
            // 實際使用時應該有更完整的詞庫
    );

    /**
     * URL 匹配模式
     */
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[\\w.-]+\\.[a-zA-Z]{2,}(/[\\w./?%&=-]*)?",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 郵箱匹配模式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 審核內容
     *
     * @param content 待審核內容
     * @return 審核結果
     */
    public ModerationResult moderateContent(String content) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("開始自定義規則審核，內容長度: {}", content.length());

            ModerationResult.ModerationResultBuilder builder = ModerationResult.builder();
            List<String> violations = new ArrayList<>();
            Map<String, Object> details = new HashMap<>();
            double riskScore = 0.0;

            // 規則1：敏感詞檢測（權重：40%）
            SensitiveWordResult sensitiveWordResult = checkSensitiveWords(content);
            if (sensitiveWordResult.hasViolation()) {
                violations.add("包含敏感詞：" + String.join(", ", sensitiveWordResult.getMatchedWords()));
                riskScore += 0.4 * sensitiveWordResult.getSeverity();
            }
            details.put("sensitive_words", sensitiveWordResult.getMatchedWords());

            // 規則2：內容長度檢查（權重：10%）
            ContentLengthResult lengthResult = checkContentLength(content);
            if (lengthResult.hasViolation()) {
                violations.add(lengthResult.reason());
                riskScore += 0.1;
            }
            details.put("content_length", content.length());
            details.put("length_valid", !lengthResult.hasViolation());

            // 規則3：特殊字符比例檢查（權重：15%）
            SpecialCharResult specialCharResult = checkSpecialCharRatio(content);
            if (specialCharResult.hasViolation()) {
                violations.add(specialCharResult.reason());
                riskScore += 0.15 * specialCharResult.ratio();
            }
            details.put("special_char_ratio", specialCharResult.ratio());

            // 規則4：URL/郵箱限制（權重：15%）
            UrlEmailResult urlEmailResult = checkUrlAndEmail(content);
            if (urlEmailResult.hasViolation()) {
                violations.add(urlEmailResult.reason());
                riskScore += 0.15;
            }
            details.put("url_count", urlEmailResult.urlCount());
            details.put("email_count", urlEmailResult.emailCount());

            // 規則5：重複內容檢測（權重：20%）
            RepetitionResult repetitionResult = checkRepetition(content);
            if (repetitionResult.hasViolation()) {
                violations.add(repetitionResult.reason());
                riskScore += 0.2 * repetitionResult.repetitionRatio();
            }
            details.put("repetition_ratio", repetitionResult.repetitionRatio());

            // 綜合判斷
            boolean flagged = riskScore > 0.3; // 風險分數超過 30% 則標記
            boolean passed = !flagged;

            builder.flagged(flagged)
                    .passed(passed)
                    .moderationScore(riskScore)
                    .details(details);

            if (flagged) {
                builder.reason("內容違反自定義規則：" + String.join("; ", violations));
                builder.flaggedCategories(violations);
            } else {
                builder.reason("內容符合自定義規則");
            }

            long processingTime = System.currentTimeMillis() - startTime;
            builder.processingTimeMs(processingTime);

            ModerationResult result = builder.build();

            log.info("自定義規則審核完成，耗時: {}ms，風險分數: {:.2f}，標記為: {}",
                    processingTime, riskScore, flagged ? "違規" : "正常");

            return result;

        } catch (Exception e) {
            log.error("自定義規則審核失敗", e);

            return ModerationResult.builder()
                    .flagged(true)
                    .passed(false)
                    .moderationScore(1.0)
                    .reason("審核過程發生錯誤：" + e.getMessage())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 檢查敏感詞
     *
     * @param content 內容
     * @return 敏感詞檢測結果
     */
    private SensitiveWordResult checkSensitiveWords(String content) {
        List<String> matchedWords = new ArrayList<>();
        String lowerContent = content.toLowerCase();

        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (lowerContent.contains(sensitiveWord.toLowerCase())) {
                matchedWords.add(sensitiveWord);
            }
        }

        // 計算嚴重程度（匹配詞數越多越嚴重）
        double severity = matchedWords.isEmpty() ? 0.0 :
                Math.min(1.0, matchedWords.size() / 3.0);

        return new SensitiveWordResult(matchedWords, severity);
    }

    /**
     * 檢查內容長度
     *
     * @param content 內容
     * @return 長度檢查結果
     */
    private ContentLengthResult checkContentLength(String content) {
        int length = content.length();

        // 太短（可能是垃圾內容）
        if (length < 5) {
            return new ContentLengthResult(true, "內容過短（少於5字符）");
        }

        // 太長（可能是惡意灌水）
        if (length > 10000) {
            return new ContentLengthResult(true, "內容過長（超過10000字符）");
        }

        return new ContentLengthResult(false, "長度正常");
    }

    /**
     * 檢查特殊字符比例
     *
     * @param content 內容
     * @return 特殊字符檢查結果
     */
    private SpecialCharResult checkSpecialCharRatio(String content) {
        if (content.isEmpty()) {
            return new SpecialCharResult(false, 0.0, "內容為空");
        }

        // 統計特殊字符數量
        long specialCharCount = content.chars()
                .filter(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
                .count();

        double ratio = (double) specialCharCount / content.length();

        // 特殊字符比例超過 50% 可能是亂碼或惡意內容
        if (ratio > 0.5) {
            return new SpecialCharResult(true, ratio,
                    String.format("特殊字符比例過高（%.1f%%）", ratio * 100));
        }

        return new SpecialCharResult(false, ratio, "特殊字符比例正常");
    }

    /**
     * 檢查 URL 和郵箱
     *
     * @param content 內容
     * @return URL/郵箱檢查結果
     */
    private UrlEmailResult checkUrlAndEmail(String content) {
        // 統計 URL 數量
        Matcher urlMatcher = URL_PATTERN.matcher(content);
        int urlCount = 0;
        while (urlMatcher.find()) {
            urlCount++;
        }

        // 統計郵箱數量
        Matcher emailMatcher = EMAIL_PATTERN.matcher(content);
        int emailCount = 0;
        while (emailMatcher.find()) {
            emailCount++;
        }

        // 過多 URL 或郵箱可能是垃圾郵件
        if (urlCount > 3 || emailCount > 2) {
            return new UrlEmailResult(true, urlCount, emailCount,
                    String.format("包含過多 URL（%d）或郵箱（%d）", urlCount, emailCount));
        }

        return new UrlEmailResult(false, urlCount, emailCount, "URL/郵箱數量正常");
    }

    /**
     * 檢查重複內容
     *
     * @param content 內容
     * @return 重複內容檢測結果
     */
    private RepetitionResult checkRepetition(String content) {
        if (content.length() < 20) {
            return new RepetitionResult(false, 0.0, "內容太短，無需檢查重複");
        }

        // 簡化版重複檢測：檢查連續重複的字符序列
        int maxRepetitionLength = 0;
        int windowSize = Math.min(10, content.length() / 4);

        for (int i = 0; i <= content.length() - windowSize * 2; i++) {
            String window = content.substring(i, i + windowSize);
            String remaining = content.substring(i + windowSize);

            if (remaining.contains(window)) {
                maxRepetitionLength = Math.max(maxRepetitionLength, windowSize);
            }
        }

        double repetitionRatio = (double) maxRepetitionLength / content.length();

        // 重複比例超過 30% 可能是惡意灌水
        if (repetitionRatio > 0.3) {
            return new RepetitionResult(true, repetitionRatio,
                    String.format("檢測到大量重複內容（%.1f%%）", repetitionRatio * 100));
        }

        return new RepetitionResult(false, repetitionRatio, "無明顯重複內容");
    }

    // ========== 內部結果類 ==========

    /**
     * 敏感詞檢測結果
     */
    private record SensitiveWordResult(List<String> matchedWords, double severity) {
        boolean hasViolation() {
            return !matchedWords.isEmpty();
        }

        List<String> getMatchedWords() {
            return matchedWords;
        }

        double getSeverity() {
            return severity;
        }
    }

    /**
     * 內容長度檢查結果
     */
    private record ContentLengthResult(boolean hasViolation, String reason) {
    }

    /**
     * 特殊字符檢查結果
     */
    private record SpecialCharResult(boolean hasViolation, double ratio, String reason) {
    }

    /**
     * URL/郵箱檢查結果
     */
    private record UrlEmailResult(boolean hasViolation, int urlCount, int emailCount, String reason) {
    }

    /**
     * 重複內容檢測結果
     */
    private record RepetitionResult(boolean hasViolation, double repetitionRatio, String reason) {
    }
}
