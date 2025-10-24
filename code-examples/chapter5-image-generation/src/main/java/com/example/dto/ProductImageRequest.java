package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 產品圖片生成請求 DTO
 * 用於電子商務產品圖片生成
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageRequest {

    /**
     * 產品名稱
     */
    @NotBlank(message = "產品名稱不能為空")
    @Size(min = 2, max = 100, message = "產品名稱長度必須在 2 到 100 個字元之間")
    private String productName;

    /**
     * 產品描述
     */
    @NotBlank(message = "產品描述不能為空")
    @Size(min = 10, max = 500, message = "產品描述長度必須在 10 到 500 個字元之間")
    private String productDescription;

    /**
     * 產品類別
     * 如: electronics, fashion, furniture, home-decor 等
     */
    @NotBlank(message = "產品類別不能為空")
    private String productCategory;

    /**
     * 背景風格
     * 可選值: white, gradient, studio, natural, transparent
     */
    @Builder.Default
    private String backgroundStyle = "white";

    /**
     * 產品視角
     * 可選值: front, side, top, 3d
     */
    @Builder.Default
    private String perspective = "front";

    /**
     * 光線環境
     * 可選值: studio, daylight, warm, cool, professional
     */
    @Builder.Default
    private String lighting = "professional";

    /**
     * 生成圖片的數量
     */
    @Builder.Default
    private Integer quantity = 1;

    /**
     * 產品 ID
     */
    private String productId;

    /**
     * SKU
     */
    private String sku;

    /**
     * 願景預算（用於圖片定價）
     */
    private String usageContext;
}
