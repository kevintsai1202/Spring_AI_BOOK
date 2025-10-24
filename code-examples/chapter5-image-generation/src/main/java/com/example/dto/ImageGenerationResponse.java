package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 圖片生成回應 DTO
 * 返回生成的圖片信息和元數據
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageGenerationResponse {

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
     * 生成的圖片 URL 列表
     */
    private List<String> imageUrls;

    /**
     * 修訂後的提示詞
     * OpenAI 可能會修訂原始提示詞以改進結果
     */
    private String revisedPrompt;

    /**
     * 使用的模型
     */
    private String model;

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
     * 通常圖片 URL 在生成後 1 小時內有效
     */
    private LocalDateTime expiresAt;

    /**
     * 本地儲存路徑（如果啟用了本地儲存）
     */
    private List<String> localPaths;

    /**
     * 錯誤信息（如果請求失敗）
     */
    private String errorMessage;

    /**
     * 用戶 ID
     */
    private String userId;

    /**
     * 分類標籤
     */
    private String category;

    /**
     * 圖片元數據（如大小、品質等）
     */
    private ImageMetadata metadata;

    /**
     * 內部圖片元數據
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageMetadata {
        /**
         * 圖片大小
         */
        private String size;

        /**
         * 圖片品質
         */
        private String quality;

        /**
         * 圖片風格
         */
        private String style;

        /**
         * 文件大小（位元組）
         */
        private Long fileSizeBytes;

        /**
         * 水平像素數
         */
        private Integer width;

        /**
         * 垂直像素數
         */
        private Integer height;
    }
}
