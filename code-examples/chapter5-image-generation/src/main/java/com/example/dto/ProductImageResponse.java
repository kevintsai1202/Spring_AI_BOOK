package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 產品圖片生成回應 DTO
 * 返回生成的產品圖片信息和元數據
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {

    /**
     * 請求 ID
     */
    private String requestId;

    /**
     * 產品 ID
     */
    private String productId;

    /**
     * SKU
     */
    private String sku;

    /**
     * 生成狀態
     */
    private String status;

    /**
     * 生成的圖片 URL 列表
     */
    private List<String> imageUrls;

    /**
     * 本地儲存路徑列表
     */
    private List<String> localPaths;

    /**
     * 生成所需時間（毫秒）
     */
    private Long processingTime;

    /**
     * 生成時間
     */
    private LocalDateTime createdAt;

    /**
     * 圖片背景風格
     */
    private String backgroundStyle;

    /**
     * 產品視角
     */
    private String perspective;

    /**
     * 光線環境
     */
    private String lighting;

    /**
     * 修訂後的提示詞
     */
    private String revisedPrompt;

    /**
     * 圖片品質指標
     */
    private QualityMetrics qualityMetrics;

    /**
     * 錯誤信息
     */
    private String errorMessage;

    /**
     * 圖片品質指標
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QualityMetrics {
        /**
         * 品質分數（1-10）
         */
        private Double qualityScore;

        /**
         * 清晰度評分
         */
        private Double clarityScore;

        /**
         * 背景合適度評分
         */
        private Double backgroundFitScore;

        /**
         * 光線效果評分
         */
        private Double lightingScore;

        /**
         * 顏色準確度評分
         */
        private Double colorAccuracyScore;

        /**
         * 推薦評論
         */
        private String recommendation;
    }
}
