package com.example.service.impl;

import com.example.dto.VoiceRequest;
import com.example.dto.VoiceResponse;
import com.example.dto.VoiceOption;
import com.example.service.VoiceGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 語音生成服務實現類
 * 實現文字轉語音的核心業務邏輯
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceGenerationServiceImpl implements VoiceGenerationService {

    // 註：Spring AI 自動配置提供語音生成模型 Bean
    // 可通過 spring-ai-starter-model-openai 依賴注入

    /**
     * 生成語音
     * 根據文本內容調用 AI 模型生成語音文件
     *
     * @param request 語音生成請求
     * @return 語音生成回應
     */
    @Override
    @Cacheable(value = "voices", key = "#request.text + '-' + #request.voice + '-' + #request.speed",
               unless = "#result.status == 'FAILED'")
    public VoiceResponse generateVoice(VoiceRequest request) {
        log.info("開始生成語音，文本長度: {}，語音: {}，速度: {}",
                 request.getText().length(), request.getVoice(), request.getSpeed());

        // 驗證文本內容
        if (!validateText(request.getText())) {
            log.warn("文本驗證失敗");
            return buildFailedResponse("文本無效或不符合規則");
        }

        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        try {
            // 優化文本以提高語音合成品質
            String optimizedText = optimizeText(request.getText());

            // 模擬語音生成調用
            // 註：實際實現應注入 OpenAiAudioSpeechModel 並調用 call(speechPrompt) 方法
            log.debug("使用 Spring AI 模型生成語音，模型: {}，語音: {}，文本長度: {}",
                    request.getModel(), request.getVoice(), optimizedText.length());

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("語音生成成功，耗時: {} ms，請求ID: {}", processingTime, requestId);

            // 估算音頻時長
            Double durationSeconds = estimateAudioDuration(request.getText(), request.getSpeed());

            // 構建成功回應
            return VoiceResponse.builder()
                    .requestId(requestId)
                    .status("SUCCESS")
                    .model(request.getModel())
                    .voice(request.getVoice())
                    .speed(request.getSpeed())
                    .format(request.getFormat())
                    .provider(request.getProvider())
                    .originalText(request.getText())
                    .textLength(request.getText().length())
                    .durationSeconds(durationSeconds)
                    .processingTime(processingTime)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .userId(request.getUserId())
                    .useCase(request.getUseCase())
                    .build();

        } catch (Exception e) {
            log.error("語音生成失敗: {}", e.getMessage(), e);
            return buildFailedResponse("語音生成失敗: " + e.getMessage());
        }
    }

    /**
     * 批次生成語音
     * 批量處理多個語音生成請求
     *
     * @param requests 語音生成請求數組
     * @return 生成結果數組
     */
    @Override
    public VoiceResponse[] generateVoiceBatch(VoiceRequest[] requests) {
        log.info("開始批次生成語音，數量: {}", requests.length);

        VoiceResponse[] responses = new VoiceResponse[requests.length];

        for (int i = 0; i < requests.length; i++) {
            try {
                responses[i] = generateVoice(requests[i]);
            } catch (Exception e) {
                log.error("批次生成語音時第 {} 個請求失敗: {}", i + 1, e.getMessage());
                responses[i] = buildFailedResponse("批次處理失敗: " + e.getMessage());
            }
        }

        return responses;
    }

    /**
     * 獲取可用的語音列表
     * 返回所有支援的語音選項
     *
     * @return 語音選項列表
     */
    @Override
    public List<VoiceOption> getAvailableVoices() {
        log.info("獲取可用語音列表");

        List<VoiceOption> voices = new ArrayList<>();

        // OpenAI 語音選項
        String[] openaiVoices = {"alloy", "echo", "fable", "onyx", "nova", "shimmer"};
        for (String voice : openaiVoices) {
            voices.add(createOpenAIVoiceOption(voice));
        }

        return voices;
    }

    /**
     * 獲取指定語言的語音列表
     *
     * @param languageCode 語言代碼
     * @return 該語言的語音選項列表
     */
    @Override
    public List<VoiceOption> getVoicesByLanguage(String languageCode) {
        log.info("獲取語言 {} 的語音列表", languageCode);

        // 實現邏輯：根據語言代碼篩選語音
        List<VoiceOption> allVoices = getAvailableVoices();
        return allVoices.stream()
                .filter(v -> languageCode.equals(v.getLanguageCode()))
                .toList();
    }

    /**
     * 獲取語音生成進度
     *
     * @param requestId 請求 ID
     * @return 回應對象
     */
    @Override
    @Cacheable(value = "voiceStatus", key = "#requestId")
    public VoiceResponse getGenerationStatus(String requestId) {
        log.info("查詢語音生成進度，請求ID: {}", requestId);

        return VoiceResponse.builder()
                .requestId(requestId)
                .status("COMPLETED")
                .build();
    }

    /**
     * 清除語音快取
     *
     * @param requestId 請求 ID
     */
    @Override
    @CacheEvict(value = "voices", key = "#requestId")
    public void clearCache(String requestId) {
        log.info("清除快取，請求ID: {}", requestId);
    }

    /**
     * 清除所有過期快取
     */
    @Override
    @CacheEvict(value = "voices", allEntries = true)
    public void clearExpiredCache() {
        log.info("清除所有過期快取");
    }

    /**
     * 驗證文本內容
     *
     * @param text 文本內容
     * @return 驗證結果
     */
    @Override
    public boolean validateText(String text) {
        // 基本驗證：非空、長度在範圍內
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        if (text.length() > 4096) {
            return false;
        }

        return true;
    }

    /**
     * 優化文本
     * 對文本進行預處理，提高語音合成品質
     *
     * @param text 原始文本
     * @return 優化後的文本
     */
    @Override
    public String optimizeText(String text) {
        // 移除多餘的空白
        String optimized = text.replaceAll("\\s+", " ").trim();

        // 可以添加更多優化規則
        // 例如數字轉換、縮寫展開等

        return optimized;
    }

    /**
     * 估算音頻時長
     * 根據文本和語速估算生成的音頻時長
     *
     * @param text 文本內容
     * @param speed 語速
     * @return 估算的時長（秒）
     */
    @Override
    public Double estimateAudioDuration(String text, Double speed) {
        // 平均每個字符約需 0.1 秒（基於正常語速）
        double baseTime = text.length() * 0.1;

        // 根據語速調整時長
        // 語速 1.0 為正常，> 1.0 為加快，< 1.0 為減慢
        return baseTime / speed;
    }

    /**
     * 構建失敗的回應
     *
     * @param errorMessage 錯誤信息
     * @return 失敗的回應對象
     */
    private VoiceResponse buildFailedResponse(String errorMessage) {
        return VoiceResponse.builder()
                .requestId(UUID.randomUUID().toString())
                .status("FAILED")
                .errorMessage(errorMessage)
                .retryable(true)
                .build();
    }

    /**
     * 建立 OpenAI 語音選項
     *
     * @param voiceId 語音 ID
     * @return 語音選項對象
     */
    private VoiceOption createOpenAIVoiceOption(String voiceId) {
        return VoiceOption.builder()
                .voiceId(voiceId)
                .voiceName(capitalizeFirst(voiceId))
                .provider("openai")
                .languageCode("en")
                .languageName("English")
                .available(true)
                .supportedFormats(new String[]{"mp3", "opus", "aac", "flac"})
                .speedRange(VoiceOption.SpeedRange.builder()
                        .min(0.25)
                        .max(4.0)
                        .recommended(1.0)
                        .build())
                .build();
    }

    /**
     * 首字母大寫
     *
     * @param str 字符串
     * @return 首字母大寫後的字符串
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
