package com.example.memory.vector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 向量記憶對話請求 DTO
 */
@Data
public class VectorChatRequest {
    /**
     * 用戶訊息內容
     */
    @NotBlank(message = "訊息內容不能為空")
    private String message;

    /**
     * 使用的記憶策略 (可選)
     * 不指定則使用配置中的默認策略
     */
    private String strategy;

    /**
     * 相似性閾值 (可選)
     * 覆蓋配置中的默認值
     */
    private Double similarityThreshold;

    /**
     * 返回的最相關記憶數量 (可選)
     */
    private Integer topK;
}
