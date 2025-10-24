package com.example.service;

import com.example.dto.ImageGenerationRequest;
import com.example.dto.ImageGenerationResponse;
import com.example.dto.ProductImageRequest;
import com.example.dto.ProductImageResponse;

/**
 * 圖片生成服務接口
 * 定義圖片生成相關的核心業務操作
 */
public interface ImageGenerationService {

    /**
     * 生成圖片
     * 根據提示詞生成指定模型的圖片
     *
     * @param request 圖片生成請求
     * @return 圖片生成回應
     */
    ImageGenerationResponse generateImage(ImageGenerationRequest request);

    /**
     * 生成產品圖片
     * 專門用於電子商務產品圖片生成
     *
     * @param request 產品圖片生成請求
     * @return 產品圖片生成回應
     */
    ProductImageResponse generateProductImage(ProductImageRequest request);

    /**
     * 批次生成圖片
     * 批量生成多個圖片
     *
     * @param requests 多個圖片生成請求數組
     * @return 生成結果數組
     */
    ImageGenerationResponse[] generateImageBatch(ImageGenerationRequest[] requests);

    /**
     * 獲取圖片生成進度
     * 查詢特定請求的生成進度
     *
     * @param requestId 請求 ID
     * @return 回應對象
     */
    ImageGenerationResponse getGenerationStatus(String requestId);

    /**
     * 清除圖片快取
     * 清除指定的圖片快取
     *
     * @param requestId 請求 ID
     */
    void clearCache(String requestId);

    /**
     * 清除所有過期快取
     */
    void clearExpiredCache();

    /**
     * 驗證提示詞
     * 檢查提示詞是否有效
     *
     * @param prompt 提示詞
     * @return 驗證結果
     */
    boolean validatePrompt(String prompt);
}
