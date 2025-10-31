package com.example.advancedrag.service;

import com.example.advancedrag.dto.ModerationResult;
import com.example.advancedrag.properties.RAGProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 內容審核服務
 *
 * 使用 OpenAI Moderation API 檢測不當內容：
 * - 仇恨言論（hate）
 * - 騷擾內容（harassment）
 * - 自我傷害（self-harm）
 * - 性內容（sexual）
 * - 暴力內容（violence）
 *
 * 審核權重：50%
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentModerationService {

    private final RAGProperties ragProperties;
    private final RestTemplate restTemplate;

    /**
     * OpenAI Moderation API 端點
     */
    private static final String MODERATION_API_URL = "https://api.openai.com/v1/moderations";

    /**
     * 審核內容
     *
     * @param content 待審核內容
     * @return 審核結果
     */
    public ModerationResult moderateContent(String content) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("開始 OpenAI 內容審核，內容長度: {}", content.length());

            // 調用 OpenAI Moderation API
            ModerationApiResponse apiResponse = callOpenAIModerationAPI(content);

            // 解析審核結果
            ModerationResult result = parseApiResponse(apiResponse);

            long processingTime = System.currentTimeMillis() - startTime;
            result.setProcessingTimeMs(processingTime);

            log.info("OpenAI 審核完成，耗時: {}ms，標記為: {}",
                    processingTime, result.getFlagged() ? "不當內容" : "正常內容");

            return result;

        } catch (Exception e) {
            log.error("OpenAI 內容審核失敗", e);

            // 審核失敗時返回安全默認值（標記為不通過）
            return ModerationResult.builder()
                    .flagged(true)
                    .passed(false)
                    .moderationScore(1.0)
                    .reason("審核服務暫時不可用")
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 批次審核內容
     *
     * @param contents 待審核內容列表
     * @return 審核結果列表
     */
    public List<ModerationResult> moderateContents(List<String> contents) {
        log.info("開始批次審核，數量: {}", contents.size());

        return contents.stream()
                .map(this::moderateContent)
                .toList();
    }

    /**
     * 調用 OpenAI Moderation API
     *
     * @param content 待審核內容
     * @return API 響應
     */
    private ModerationApiResponse callOpenAIModerationAPI(String content) {
        try {
            // 構建請求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("input", content);
            requestBody.put("model", "text-moderation-stable");

            // 設置請求頭
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + ragProperties.getOpenaiApiKey());
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<Map<String, Object>> request =
                    new org.springframework.http.HttpEntity<>(requestBody, headers);

            // 調用 API
            org.springframework.http.ResponseEntity<ModerationApiResponse> response =
                    restTemplate.postForEntity(
                            MODERATION_API_URL,
                            request,
                            ModerationApiResponse.class
                    );

            if (response.getBody() == null || response.getBody().getResults() == null ||
                    response.getBody().getResults().isEmpty()) {
                throw new RuntimeException("OpenAI Moderation API 返回空結果");
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("調用 OpenAI Moderation API 失敗", e);
            throw new RuntimeException("Moderation API 調用失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 API 響應
     *
     * @param apiResponse API 響應
     * @return 審核結果
     */
    private ModerationResult parseApiResponse(ModerationApiResponse apiResponse) {
        ModerationApiResult result = apiResponse.getResults().get(0);

        ModerationResult.ModerationResultBuilder builder = ModerationResult.builder()
                .flagged(result.isFlagged())
                .passed(!result.isFlagged());

        // 提取最高分數的類別
        Map<String, Double> categoryScores = result.getCategoryScores();
        double maxScore = 0.0;
        String flaggedCategory = null;

        if (categoryScores != null) {
            for (Map.Entry<String, Double> entry : categoryScores.entrySet()) {
                if (entry.getValue() > maxScore) {
                    maxScore = entry.getValue();
                    flaggedCategory = entry.getKey();
                }
            }
        }

        builder.moderationScore(maxScore);

        // 設置原因
        if (result.isFlagged()) {
            builder.reason(buildFlaggedReason(result.getCategories(), flaggedCategory));
            builder.flaggedCategories(List.of(flaggedCategory != null ? flaggedCategory : "unknown"));
        } else {
            builder.reason("內容正常");
        }

        // 添加詳細信息
        Map<String, Object> details = new HashMap<>();
        details.put("categories", result.getCategories());
        details.put("category_scores", result.getCategoryScores());
        builder.details(details);

        return builder.build();
    }

    /**
     * 構建標記原因
     *
     * @param categories 標記類別
     * @param primaryCategory 主要類別
     * @return 原因描述
     */
    private String buildFlaggedReason(Map<String, Boolean> categories, String primaryCategory) {
        if (categories == null || categories.isEmpty()) {
            return "內容包含不當信息";
        }

        List<String> flaggedCategories = categories.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();

        if (flaggedCategories.isEmpty()) {
            return "內容包含不當信息";
        }

        return String.format("內容包含不當信息：%s（主要類別：%s）",
                String.join(", ", flaggedCategories),
                primaryCategory != null ? primaryCategory : flaggedCategories.get(0));
    }

    /**
     * OpenAI Moderation API 響應
     */
    @lombok.Data
    private static class ModerationApiResponse {
        private String id;
        private String model;
        private List<ModerationApiResult> results;
    }

    /**
     * OpenAI Moderation API 結果
     */
    @lombok.Data
    private static class ModerationApiResult {
        private boolean flagged;
        private Map<String, Boolean> categories;
        private Map<String, Double> categoryScores;

        /**
         * 獲取類別分數映射
         */
        public Map<String, Double> getCategoryScores() {
            if (categoryScores == null) {
                return new HashMap<>();
            }
            return categoryScores;
        }
    }
}
