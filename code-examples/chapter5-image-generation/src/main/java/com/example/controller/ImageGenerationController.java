package com.example.controller;

import com.example.dto.ImageGenerationRequest;
import com.example.dto.ImageGenerationResponse;
import com.example.dto.ProductImageRequest;
import com.example.dto.ProductImageResponse;
import com.example.service.ImageGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * 圖片生成 REST API 控制器
 * 提供圖片生成相關的 HTTP 端點
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Validated
public class ImageGenerationController {

    // 注入圖片生成服務
    private final ImageGenerationService imageGenerationService;

    /**
     * 生成單個圖片
     * POST /api/images/generate
     *
     * @param request 圖片生成請求
     * @return 圖片生成回應
     */
    @PostMapping("/generate")
    public ResponseEntity<ImageGenerationResponse> generateImage(
            @Valid @RequestBody ImageGenerationRequest request) {
        log.info("接收圖片生成請求，提示詞: {}", request.getPrompt());

        ImageGenerationResponse response = imageGenerationService.generateImage(request);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 生成產品圖片
     * POST /api/images/products
     *
     * @param request 產品圖片生成請求
     * @return 產品圖片生成回應
     */
    @PostMapping("/products")
    public ResponseEntity<ProductImageResponse> generateProductImage(
            @Valid @RequestBody ProductImageRequest request) {
        log.info("接收產品圖片生成請求，產品: {}", request.getProductName());

        ProductImageResponse response = imageGenerationService.generateProductImage(request);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 批次生成圖片
     * POST /api/images/batch
     *
     * @param requests 多個圖片生成請求
     * @return 生成結果數組
     */
    @PostMapping("/batch")
    public ResponseEntity<ImageGenerationResponse[]> generateImageBatch(
            @Valid @RequestBody ImageGenerationRequest[] requests) {
        log.info("接收批次圖片生成請求，數量: {}", requests.length);

        ImageGenerationResponse[] responses = imageGenerationService.generateImageBatch(requests);

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * 獲取圖片生成進度
     * GET /api/images/{requestId}/status
     *
     * @param requestId 請求 ID
     * @return 圖片生成狀態
     */
    @GetMapping("/{requestId}/status")
    public ResponseEntity<ImageGenerationResponse> getGenerationStatus(
            @PathVariable String requestId) {
        log.info("查詢圖片生成進度，請求ID: {}", requestId);

        ImageGenerationResponse response = imageGenerationService.getGenerationStatus(requestId);

        return ResponseEntity.ok(response);
    }

    /**
     * 清除圖片快取
     * DELETE /api/images/{requestId}/cache
     *
     * @param requestId 請求 ID
     * @return 清除結果
     */
    @DeleteMapping("/{requestId}/cache")
    public ResponseEntity<Void> clearCache(@PathVariable String requestId) {
        log.info("清除圖片快取，請求ID: {}", requestId);

        imageGenerationService.clearCache(requestId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 驗證提示詞
     * POST /api/images/validate-prompt
     *
     * @param request 包含提示詞的請求體
     * @return 驗證結果
     */
    @PostMapping("/validate-prompt")
    public ResponseEntity<ValidatePromptResponse> validatePrompt(
            @RequestBody ValidatePromptRequest request) {
        log.info("驗證提示詞");

        boolean isValid = imageGenerationService.validatePrompt(request.getPrompt());

        ValidatePromptResponse response = ValidatePromptResponse.builder()
                .valid(isValid)
                .prompt(request.getPrompt())
                .message(isValid ? "提示詞有效" : "提示詞無效")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 獲取 API 健康狀態
     * GET /api/images/health
     *
     * @return 健康狀態
     */
    @GetMapping("/health")
    public ResponseEntity<HealthCheckResponse> healthCheck() {
        log.info("檢查圖片生成服務健康狀態");

        HealthCheckResponse response = HealthCheckResponse.builder()
                .status("UP")
                .service("Image Generation Service")
                .message("服務正常運行")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 驗證提示詞請求類
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ValidatePromptRequest {
        private String prompt;
    }

    /**
     * 驗證提示詞回應類
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class ValidatePromptResponse {
        private boolean valid;
        private String prompt;
        private String message;
    }

    /**
     * 健康檢查回應類
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class HealthCheckResponse {
        private String status;
        private String service;
        private String message;
    }
}
