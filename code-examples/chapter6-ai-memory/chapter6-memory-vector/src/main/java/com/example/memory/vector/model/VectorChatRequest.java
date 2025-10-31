package com.example.memory.vector.model;

import lombok.Data;

/**
 * 向量聊天請求
 */
@Data
public class VectorChatRequest {
    /**
     * 用戶訊息
     */
    private String message;

    /**
     * 相似性閾值 (可選, 0-1之間)
     */
    private Double similarityThreshold;

    /**
     * 返回的最相似結果數量 (可選)
     */
    private Integer topK;
}
