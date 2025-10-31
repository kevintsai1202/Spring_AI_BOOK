package com.example.controller;

import com.example.dto.VoiceRequest;
import com.example.dto.VoiceResponse;
import com.example.dto.VoiceOption;
import com.example.service.VoiceGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 語音生成 REST API 控制器
 * 提供文字轉語音相關的 HTTP 端點
 */
@Slf4j
@RestController
@RequestMapping("/api/voices")
@RequiredArgsConstructor
@Validated
public class VoiceController {

    // 注入語音生成服務
    private final VoiceGenerationService voiceGenerationService;

    /**
     * 生成單個語音
     * POST /api/voices/generate
     *
     * @param request 語音生成請求
     * @return 語音生成回應
     */
    @PostMapping("/generate")
    public ResponseEntity<VoiceResponse> generateVoice(
            @Valid @RequestBody VoiceRequest request) {
        log.info("接收語音生成請求，文本長度: {}，語音: {}", request.getText().length(), request.getVoice());

        VoiceResponse response = voiceGenerationService.generateVoice(request);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 批次生成語音
     * POST /api/voices/batch
     *
     * @param requests 多個語音生成請求
     * @return 生成結果數組
     */
    @PostMapping("/batch")
    public ResponseEntity<VoiceResponse[]> generateVoiceBatch(
            @Valid @RequestBody VoiceRequest[] requests) {
        log.info("接收批次語音生成請求，數量: {}", requests.length);

        VoiceResponse[] responses = voiceGenerationService.generateVoiceBatch(requests);

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * 獲取所有可用語音列表
     * GET /api/voices/available
     *
     * @return 語音選項列表
     */
    @GetMapping("/available")
    public ResponseEntity<List<VoiceOption>> getAvailableVoices() {
        log.info("獲取可用語音列表");

        List<VoiceOption> voices = voiceGenerationService.getAvailableVoices();

        return ResponseEntity.ok(voices);
    }

    /**
     * 獲取指定語言的語音列表
     * GET /api/voices/by-language/{languageCode}
     *
     * @param languageCode 語言代碼（如 en, zh-CN）
     * @return 該語言的語音選項列表
     */
    @GetMapping("/by-language/{languageCode}")
    public ResponseEntity<List<VoiceOption>> getVoicesByLanguage(
            @PathVariable String languageCode) {
        log.info("獲取語言 {} 的語音列表", languageCode);

        List<VoiceOption> voices = voiceGenerationService.getVoicesByLanguage(languageCode);

        return ResponseEntity.ok(voices);
    }

    /**
     * 獲取語音生成進度
     * GET /api/voices/{requestId}/status
     *
     * @param requestId 請求 ID
     * @return 語音生成狀態
     */
    @GetMapping("/{requestId}/status")
    public ResponseEntity<VoiceResponse> getGenerationStatus(
            @PathVariable String requestId) {
        log.info("查詢語音生成進度，請求ID: {}", requestId);

        VoiceResponse response = voiceGenerationService.getGenerationStatus(requestId);

        return ResponseEntity.ok(response);
    }

    /**
     * 清除語音快取
     * DELETE /api/voices/{requestId}/cache
     *
     * @param requestId 請求 ID
     * @return 清除結果
     */
    @DeleteMapping("/{requestId}/cache")
    public ResponseEntity<Void> clearCache(@PathVariable String requestId) {
        log.info("清除語音快取，請求ID: {}", requestId);

        voiceGenerationService.clearCache(requestId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 驗證文本內容
     * POST /api/voices/validate-text
     *
     * @param request 包含文本的請求體
     * @return 驗證結果
     */
    @PostMapping("/validate-text")
    public ResponseEntity<ValidateTextResponse> validateText(
            @RequestBody ValidateTextRequest request) {
        log.info("驗證文本內容，長度: {}", request.getText().length());

        boolean isValid = voiceGenerationService.validateText(request.getText());
        String optimizedText = voiceGenerationService.optimizeText(request.getText());

        ValidateTextResponse response = ValidateTextResponse.builder()
                .valid(isValid)
                .originalText(request.getText())
                .optimizedText(optimizedText)
                .message(isValid ? "文本有效" : "文本無效或過長")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 估算音頻時長
     * POST /api/voices/estimate-duration
     *
     * @param request 包含文本和語速的請求體
     * @return 時長估算結果
     */
    @PostMapping("/estimate-duration")
    public ResponseEntity<EstimateDurationResponse> estimateDuration(
            @RequestBody EstimateDurationRequest request) {
        log.info("估算音頻時長，文本長度: {}，語速: {}", request.getText().length(), request.getSpeed());

        Double durationSeconds = voiceGenerationService.estimateAudioDuration(
                request.getText(),
                request.getSpeed()
        );

        EstimateDurationResponse response = EstimateDurationResponse.builder()
                .estimatedDuration(durationSeconds)
                .unit("seconds")
                .textLength(request.getText().length())
                .speed(request.getSpeed())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 獲取 API 健康狀態
     * GET /api/voices/health
     *
     * @return 健康狀態
     */
    @GetMapping("/health")
    public ResponseEntity<HealthCheckResponse> healthCheck() {
        log.info("檢查語音生成服務健康狀態");

        HealthCheckResponse response = HealthCheckResponse.builder()
                .status("UP")
                .service("Voice Generation Service")
                .message("服務正常運行")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 驗證文本請求類
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ValidateTextRequest {
        private String text;
    }

    /**
     * 驗證文本回應類
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class ValidateTextResponse {
        private boolean valid;
        private String originalText;
        private String optimizedText;
        private String message;
    }

    /**
     * 估算時長請求類
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EstimateDurationRequest {
        private String text;
        private Double speed = 1.0;
    }

    /**
     * 估算時長回應類
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class EstimateDurationResponse {
        private Double estimatedDuration;
        private String unit;
        private Integer textLength;
        private Double speed;
    }

    /**
     * 健康檢查回應類
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class HealthCheckResponse {
        private String status;
        private String service;
        private String message;
    }
}
