package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 語音選項 DTO
 * 用於返回可用的語音選項信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceOption {

    /**
     * 語音ID
     */
    private String voiceId;

    /**
     * 語音名稱
     */
    private String voiceName;

    /**
     * 語音描述
     */
    private String description;

    /**
     * 性別
     * 可選值: male, female, neutral
     */
    private String gender;

    /**
     * 年齡層
     * 可選值: child, young-adult, adult, senior
     */
    private String ageGroup;

    /**
     * 方言/口音
     * 如: american, british, french, spanish, etc
     */
    private String accent;

    /**
     * 使用的服務提供者
     * 可選值: openai, elevenlabs
     */
    private String provider;

    /**
     * 語言代碼
     * 如: en, zh-CN, ja, es
     */
    private String languageCode;

    /**
     * 語言名稱
     */
    private String languageName;

    /**
     * 語音特點描述
     * 如: warm, energetic, professional, friendly
     */
    private String characteristics;

    /**
     * 試聽 URL
     */
    private String sampleUrl;

    /**
     * 適用場景
     * 如: news, podcast, audiobook, commercial, assistant
     */
    private String useCase;

    /**
     * 建議的語速範圍
     */
    private SpeedRange speedRange;

    /**
     * 支援的音頻格式
     */
    private String[] supportedFormats;

    /**
     * 該語音是否可用
     */
    private Boolean available;

    /**
     * 語速範圍信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpeedRange {
        /**
         * 最小語速
         */
        private Double min;

        /**
         * 最大語速
         */
        private Double max;

        /**
         * 建議的語速
         */
        private Double recommended;
    }
}
