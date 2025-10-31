package com.example.memory.vector.dto;

import com.example.memory.vector.model.MemoryItem;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 向量記憶對話響應 DTO
 */
@Data
@Builder
public class VectorChatResponse {
    /**
     * 對話 ID
     */
    private String conversationId;

    /**
     * AI 回應內容
     */
    private String response;

    /**
     * 使用的記憶策略
     */
    private String strategy;

    /**
     * 短期記憶數量
     */
    private int shortTermCount;

    /**
     * 長期記憶數量
     */
    private int longTermCount;

    /**
     * 使用的記憶項目 (用於調試)
     */
    private List<MemoryItem> usedMemories;

    /**
     * 響應時間戳
     */
    private LocalDateTime timestamp;
}
