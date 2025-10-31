package com.example.advancedrag.controller;

import com.example.advancedrag.dto.ApiResponse;
import com.example.advancedrag.dto.ModerationRequest;
import com.example.advancedrag.dto.ModerationResult;
import com.example.advancedrag.service.ContentModerationService;
import com.example.advancedrag.service.CustomRuleModerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 內容審核控制器
 *
 * 提供內容審核 REST API：
 * - POST /api/v1/moderation/openai - OpenAI 審核
 * - POST /api/v1/moderation/custom - 自定義規則審核
 * - POST /api/v1/moderation/combined - 綜合審核（OpenAI + 自定義規則）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ContentModerationService contentModerationService;
    private final CustomRuleModerationService customRuleModerationService;

    /**
     * OpenAI 內容審核
     *
     * @param request 審核請求
     * @return 審核結果
     */
    @PostMapping("/openai")
    public ResponseEntity<ApiResponse<ModerationResult>> moderateWithOpenAI(
            @Valid @RequestBody ModerationRequest request) {

        log.info("收到 OpenAI 審核請求，內容長度: {}", request.getContent().length());

        try {
            ModerationResult result = contentModerationService.moderateContent(request.getContent());

            return ResponseEntity.ok(
                    ApiResponse.success("OpenAI 審核完成", result)
            );

        } catch (Exception e) {
            log.error("OpenAI 審核失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("OpenAI 審核失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 自定義規則審核
     *
     * @param request 審核請求
     * @return 審核結果
     */
    @PostMapping("/custom")
    public ResponseEntity<ApiResponse<ModerationResult>> moderateWithCustomRules(
            @Valid @RequestBody ModerationRequest request) {

        log.info("收到自定義規則審核請求，內容長度: {}", request.getContent().length());

        try {
            ModerationResult result = customRuleModerationService.moderateContent(request.getContent());

            return ResponseEntity.ok(
                    ApiResponse.success("自定義規則審核完成", result)
            );

        } catch (Exception e) {
            log.error("自定義規則審核失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("自定義規則審核失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 綜合審核（OpenAI + 自定義規則）
     *
     * 結合兩種審核方式，計算綜合分數：
     * - OpenAI 審核：50% 權重
     * - 自定義規則審核：50% 權重
     *
     * @param request 審核請求
     * @return 綜合審核結果
     */
    @PostMapping("/combined")
    public ResponseEntity<ApiResponse<CombinedModerationResult>> moderateCombined(
            @Valid @RequestBody ModerationRequest request) {

        log.info("收到綜合審核請求，內容長度: {}", request.getContent().length());

        try {
            long startTime = System.currentTimeMillis();

            // 並行執行兩種審核（可優化為異步）
            ModerationResult openAIResult = contentModerationService.moderateContent(request.getContent());
            ModerationResult customRuleResult = customRuleModerationService.moderateContent(request.getContent());

            // 計算綜合結果
            CombinedModerationResult combinedResult = combineModerationResults(
                    openAIResult,
                    customRuleResult,
                    System.currentTimeMillis() - startTime
            );

            return ResponseEntity.ok(
                    ApiResponse.success("綜合審核完成", combinedResult)
            );

        } catch (Exception e) {
            log.error("綜合審核失敗", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("綜合審核失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 合併審核結果
     *
     * @param openAIResult OpenAI 審核結果
     * @param customRuleResult 自定義規則審核結果
     * @param totalTime 總耗時
     * @return 綜合審核結果
     */
    private CombinedModerationResult combineModerationResults(
            ModerationResult openAIResult,
            ModerationResult customRuleResult,
            long totalTime) {

        // 計算綜合分數（各占 50% 權重）
        double combinedScore = (openAIResult.getModerationScore() * 0.5) +
                (customRuleResult.getModerationScore() * 0.5);

        // 只要有一個審核不通過，則綜合結果不通過
        boolean flagged = openAIResult.getFlagged() || customRuleResult.getFlagged();
        boolean passed = !flagged;

        // 構建原因說明
        StringBuilder reason = new StringBuilder();
        if (flagged) {
            if (openAIResult.getFlagged()) {
                reason.append("OpenAI 審核不通過：").append(openAIResult.getReason());
            }
            if (customRuleResult.getFlagged()) {
                if (reason.length() > 0) {
                    reason.append("; ");
                }
                reason.append("自定義規則審核不通過：").append(customRuleResult.getReason());
            }
        } else {
            reason.append("內容通過所有審核檢查");
        }

        return CombinedModerationResult.builder()
                .flagged(flagged)
                .passed(passed)
                .combinedScore(combinedScore)
                .reason(reason.toString())
                .openAIResult(openAIResult)
                .customRuleResult(customRuleResult)
                .totalProcessingTimeMs(totalTime)
                .build();
    }

    /**
     * 綜合審核結果
     */
    @lombok.Data
    @lombok.Builder
    public static class CombinedModerationResult {
        /**
         * 是否被標記為不當內容
         */
        private boolean flagged;

        /**
         * 是否通過審核
         */
        private boolean passed;

        /**
         * 綜合分數（0-1，越高越危險）
         */
        private double combinedScore;

        /**
         * 原因說明
         */
        private String reason;

        /**
         * OpenAI 審核結果
         */
        private ModerationResult openAIResult;

        /**
         * 自定義規則審核結果
         */
        private ModerationResult customRuleResult;

        /**
         * 總處理時間（毫秒）
         */
        private long totalProcessingTimeMs;
    }
}
