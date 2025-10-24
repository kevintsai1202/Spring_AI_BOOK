package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 語音生成回應 DTO
 * 返回生成的語音文件信息和元數據
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceResponse {

    /**
     * 請求 ID
     */
    private String requestId;

    /**
     * 生成狀態
     * 可選值: SUCCESS, PROCESSING, FAILED
     */
    private String status;

    /**
     * 語音文件 URL
     * 可以是本地存儲路徑或遠端 CDN URL
     */
    private String audioUrl;

    /**
     * 本地儲存路徑
     */
    private String localPath;

    /**
     * 使用的語音模型
     */
    private String model;

    /**
     * 語音選擇
     */
    private String voice;

    /**
     * 語音速度
     */
    private Double speed;

    /**
     * 輸出音頻格式
     */
    private String format;

    /**
     * 文件大小（位元組）
     */
    private Long fileSizeBytes;

    /**
     * 音頻時長（秒）
     */
    private Double durationSeconds;

    /**
     * 生成所需時間（毫秒）
     */
    private Long processingTime;

    /**
     * 生成時間
     */
    private LocalDateTime createdAt;

    /**
     * 過期時間
     */
    private LocalDateTime expiresAt;

    /**
     * 用戶 ID
     */
    private String userId;

    /**
     * 應用場景
     */
    private String useCase;

    /**
     * 原始文本
     */
    private String originalText;

    /**
     * 文本長度
     */
    private Integer textLength;

    /**
     * 音頻品質指標
     */
    private AudioQualityMetrics qualityMetrics;

    /**
     * 錯誤信息（如果請求失敗）
     */
    private String errorMessage;

    /**
     * 可重試標誌
     */
    private Boolean retryable;

    /**
     * 語音使用的服務提供者
     */
    private String provider;

    /**
     * 音頻品質指標
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AudioQualityMetrics {
        /**
         * 整體品質分數（1-10）
         */
        private Double overallQuality;

        /**
         * 清晰度分數
         */
        private Double clarity;

        /**
         * 自然度分數
         */
        private Double naturalness;

        /**
         * 韻律分數（語言的起伏和節奏）
         */
        private Double prosody;

        /**
         * 速度評估
         */
        private String speedAssessment;

        /**
         * 推薦評論
         */
        private String recommendation;
    }
}
