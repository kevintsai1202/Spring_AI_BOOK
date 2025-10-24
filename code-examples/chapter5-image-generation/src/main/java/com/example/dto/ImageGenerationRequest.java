package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 圖片生成請求 DTO
 * 用於接收用戶端的圖片生成請求參數
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageGenerationRequest {

    /**
     * 圖片描述文字
     */
    @NotBlank(message = "圖片描述不能為空")
    @Size(min = 10, max = 1000, message = "圖片描述長度必須在 10 到 1000 個字元之間")
    private String prompt;

    /**
     * 使用的 AI 模型
     * 可選值: dall-e-3, dall-e-2, cogview-3
     */
    @Builder.Default
    private String model = "dall-e-3";

    /**
     * 圖片大小
     * 可選值: 1024x1024, 1024x1792, 1792x1024, 512x512, 512x768, 768x512
     */
    @Builder.Default
    private String size = "1024x1024";

    /**
     * 圖片品質
     * 可選值: standard, hd
     */
    @Builder.Default
    private String quality = "hd";

    /**
     * 圖片風格
     * 可選值: vivid, natural
     */
    @Builder.Default
    private String style = "vivid";

    /**
     * 生成的圖片數量
     * 取值範圍: 1-10
     */
    @Builder.Default
    private Integer quantity = 1;

    /**
     * 用戶提供的自訂 ID，用於追蹤請求
     */
    private String userId;

    /**
     * 圖片分類標籤（如 product, social-media, illustration）
     */
    private String category;
}
