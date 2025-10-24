package com.example.service;

import com.example.dto.VoiceRequest;
import com.example.dto.VoiceResponse;
import com.example.dto.VoiceOption;
import java.util.List;

/**
 * 語音生成服務接口
 * 定義語音生成相關的核心業務操作
 */
public interface VoiceGenerationService {

    /**
     * 生成語音
     * 根據文本內容生成語音文件
     *
     * @param request 語音生成請求
     * @return 語音生成回應
     */
    VoiceResponse generateVoice(VoiceRequest request);

    /**
     * 批次生成語音
     * 批量處理多個語音生成請求
     *
     * @param requests 語音生成請求數組
     * @return 生成結果數組
     */
    VoiceResponse[] generateVoiceBatch(VoiceRequest[] requests);

    /**
     * 獲取可用的語音列表
     * 返回所有支援的語音選項
     *
     * @return 語音選項列表
     */
    List<VoiceOption> getAvailableVoices();

    /**
     * 獲取指定語言的語音列表
     *
     * @param languageCode 語言代碼（如 en, zh-CN）
     * @return 該語言的語音選項列表
     */
    List<VoiceOption> getVoicesByLanguage(String languageCode);

    /**
     * 獲取語音生成進度
     * 查詢特定請求的生成進度
     *
     * @param requestId 請求 ID
     * @return 回應對象
     */
    VoiceResponse getGenerationStatus(String requestId);

    /**
     * 清除語音快取
     * 清除指定的語音快取
     *
     * @param requestId 請求 ID
     */
    void clearCache(String requestId);

    /**
     * 清除所有過期快取
     */
    void clearExpiredCache();

    /**
     * 驗證文本內容
     * 檢查文本是否有效
     *
     * @param text 文本內容
     * @return 驗證結果
     */
    boolean validateText(String text);

    /**
     * 優化文本
     * 對文本進行預處理和優化，提高語音合成品質
     *
     * @param text 原始文本
     * @return 優化後的文本
     */
    String optimizeText(String text);

    /**
     * 估算音頻時長
     * 根據文本和語速估算生成的音頻時長
     *
     * @param text 文本內容
     * @param speed 語速
     * @return 估算的時長（秒）
     */
    Double estimateAudioDuration(String text, Double speed);
}
