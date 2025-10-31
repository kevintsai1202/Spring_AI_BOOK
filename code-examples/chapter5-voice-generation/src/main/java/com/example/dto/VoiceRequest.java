package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 語音生成請求 DTO
 * 用於接收用戶端的文字轉語音請求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceRequest {

    /**
     * 待轉換的文本內容
     */
    @NotBlank(message = "文本內容不能為空")
    @Size(min = 1, max = 4096, message = "文本長度必須在 1 到 4096 個字元之間")
    private String text;

    /**
     * 使用的語音模型
     * 可選值: tts-1, tts-1-hd
     * tts-1 快速但品質較低，tts-1-hd 品質更高但速度較慢
     */
    @Builder.Default
    private String model = "tts-1-hd";

    /**
     * 語音選擇
     * 可選值: alloy, echo, fable, onyx, nova, shimmer
     */
    @Builder.Default
    private String voice = "alloy";

    /**
     * 語音速度
     * 取值範圍: 0.25 - 4.0
     * 默認值: 1.0
     */
    @Builder.Default
    @Min(value = 0, message = "速度必須大於 0")
    @Max(value = 4, message = "速度不能超過 4")
    private Double speed = 1.0;

    /**
     * 輸出音頻格式
     * 可選值: mp3, opus, aac, flac
     */
    @Builder.Default
    private String format = "mp3";

    /**
     * 使用的 TTS 服務提供者
     * 可選值: openai, elevenlabs
     */
    @Builder.Default
    private String provider = "openai";

    /**
     * 用戶 ID，用於追蹤請求
     */
    private String userId;

    /**
     * 應用場景標籤
     * 如: notification, podcast, audiobook, commercial
     */
    private String useCase;

    /**
     * 語言代碼（ElevenLabs 特定）
     * 如: en, zh-CN, ja, es
     */
    private String language;

    /**
     * 批次模式標誌
     * 是否為批次處理請求
     */
    @Builder.Default
    private Boolean batchMode = false;

    /**
     * 是否儲存生成的語音
     */
    @Builder.Default
    private Boolean saveOutput = true;
}
