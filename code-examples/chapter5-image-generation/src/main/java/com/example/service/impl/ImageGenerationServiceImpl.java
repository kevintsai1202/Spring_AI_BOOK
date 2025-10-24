package com.example.service.impl;

import com.example.dto.ImageGenerationRequest;
import com.example.dto.ImageGenerationResponse;
import com.example.dto.ProductImageRequest;
import com.example.dto.ProductImageResponse;
import com.example.service.ImageGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 圖片生成服務實現類
 * 實現圖片生成的核心業務邏輯
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationServiceImpl implements ImageGenerationService {

    // 注入 ImageModel 依賴
    private final ImageModel imageModel;

    /**
     * 生成圖片
     * 根據提示詞調用 AI 模型生成圖片
     *
     * @param request 圖片生成請求
     * @return 圖片生成回應
     */
    @Override
    @Cacheable(value = "images", key = "#request.prompt", unless = "#result.status == 'FAILED'")
    public ImageGenerationResponse generateImage(ImageGenerationRequest request) {
        log.info("開始生成圖片，提示詞: {}", request.getPrompt());

        // 驗證提示詞
        if (!validatePrompt(request.getPrompt())) {
            log.warn("提示詞驗證失敗: {}", request.getPrompt());
            return buildFailedResponse("提示詞無效或不符合規則");
        }

        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        try {
            // 建立圖片提示
            ImagePrompt imagePrompt = new ImagePrompt(request.getPrompt());

            // 調用 AI 模型生成圖片
            ImageResponse response = imageModel.call(imagePrompt);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("圖片生成成功，耗時: {} ms，請求ID: {}", processingTime, requestId);

            // 構建成功回應
            return ImageGenerationResponse.builder()
                    .requestId(requestId)
                    .status("SUCCESS")
                    .model(request.getModel())
                    .processingTime(processingTime)
                    .createdAt(LocalDateTime.now())
                    .userId(request.getUserId())
                    .category(request.getCategory())
                    .build();

        } catch (Exception e) {
            log.error("圖片生成失敗: {}", e.getMessage(), e);
            return buildFailedResponse("圖片生成失敗: " + e.getMessage());
        }
    }

    /**
     * 生成產品圖片
     * 為電子商務產品生成高品質商品圖片
     *
     * @param request 產品圖片生成請求
     * @return 產品圖片生成回應
     */
    @Override
    public ProductImageResponse generateProductImage(ProductImageRequest request) {
        log.info("開始生成產品圖片，產品名稱: {}", request.getProductName());

        // 構建詳細的產品圖片描述提示詞
        String detailedPrompt = buildProductPrompt(request);

        // 使用基礎圖片生成方法
        ImageGenerationRequest imageRequest = ImageGenerationRequest.builder()
                .prompt(detailedPrompt)
                .model(request.getProductCategory())
                .size("1024x1024")
                .quality("hd")
                .style("vivid")
                .quantity(request.getQuantity())
                .category(request.getProductCategory())
                .build();

        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        try {
            // 調用圖片生成
            ImageGenerationResponse imageResponse = generateImage(imageRequest);

            long processingTime = System.currentTimeMillis() - startTime;

            // 構建產品圖片回應
            return ProductImageResponse.builder()
                    .requestId(requestId)
                    .productId(request.getProductId())
                    .sku(request.getSku())
                    .status(imageResponse.getStatus())
                    .backgroundStyle(request.getBackgroundStyle())
                    .perspective(request.getPerspective())
                    .lighting(request.getLighting())
                    .processingTime(processingTime)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("產品圖片生成失敗: {}", e.getMessage(), e);
            return ProductImageResponse.builder()
                    .requestId(requestId)
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 批次生成圖片
     * 批量處理多個圖片生成請求
     *
     * @param requests 圖片生成請求數組
     * @return 生成結果數組
     */
    @Override
    public ImageGenerationResponse[] generateImageBatch(ImageGenerationRequest[] requests) {
        log.info("開始批次生成圖片，數量: {}", requests.length);

        ImageGenerationResponse[] responses = new ImageGenerationResponse[requests.length];

        for (int i = 0; i < requests.length; i++) {
            try {
                responses[i] = generateImage(requests[i]);
            } catch (Exception e) {
                log.error("批次生成圖片時第 {} 個請求失敗: {}", i + 1, e.getMessage());
                responses[i] = buildFailedResponse("批次處理失敗: " + e.getMessage());
            }
        }

        return responses;
    }

    /**
     * 獲取圖片生成進度
     * 從快取中查詢生成結果
     *
     * @param requestId 請求 ID
     * @return 回應對象
     */
    @Override
    @Cacheable(value = "imageStatus", key = "#requestId")
    public ImageGenerationResponse getGenerationStatus(String requestId) {
        log.info("查詢圖片生成進度，請求ID: {}", requestId);

        // 從快取中獲取結果（實現中應該從數據庫查詢）
        return ImageGenerationResponse.builder()
                .requestId(requestId)
                .status("COMPLETED")
                .build();
    }

    /**
     * 清除圖片快取
     * 清除指定請求的快取
     *
     * @param requestId 請求 ID
     */
    @Override
    @CacheEvict(value = "images", key = "#requestId")
    public void clearCache(String requestId) {
        log.info("清除快取，請求ID: {}", requestId);
    }

    /**
     * 清除所有過期快取
     */
    @Override
    @CacheEvict(value = "images", allEntries = true)
    public void clearExpiredCache() {
        log.info("清除所有過期快取");
    }

    /**
     * 驗證提示詞
     * 檢查提示詞是否有效
     *
     * @param prompt 提示詞
     * @return 驗證結果
     */
    @Override
    public boolean validatePrompt(String prompt) {
        // 基本驗證：非空、長度在範圍內
        if (prompt == null || prompt.trim().isEmpty()) {
            return false;
        }

        if (prompt.length() < 10 || prompt.length() > 1000) {
            return false;
        }

        // 可以添加更多的驗證規則
        // 例如檢查不合法的詞語等

        return true;
    }

    /**
     * 構建失敗的回應
     *
     * @param errorMessage 錯誤信息
     * @return 失敗的回應對象
     */
    private ImageGenerationResponse buildFailedResponse(String errorMessage) {
        return ImageGenerationResponse.builder()
                .requestId(UUID.randomUUID().toString())
                .status("FAILED")
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 構建產品圖片的詳細描述提示詞
     *
     * @param request 產品圖片生成請求
     * @return 詳細的提示詞
     */
    private String buildProductPrompt(ProductImageRequest request) {
        return String.format(
                "Generate a professional product image for: %s. Description: %s. " +
                "Background: %s, Perspective: %s, Lighting: %s. " +
                "Studio-quality, high resolution, professional photography.",
                request.getProductName(),
                request.getProductDescription(),
                request.getBackgroundStyle(),
                request.getPerspective(),
                request.getLighting()
        );
    }
}
