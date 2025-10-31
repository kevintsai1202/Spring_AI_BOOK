package com.example.memory.vector.dto;

import com.example.memory.vector.model.MemoryItem;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 記憶檢索結果 DTO
 */
@Data
@Builder
public class MemoryRetrievalResult {
    /**
     * 對話 ID
     */
    private String conversationId;

    /**
     * 查詢文本
     */
    private String query;

    /**
     * 短期記憶列表
     */
    private List<Message> shortTermMemories;

    /**
     * 長期記憶列表 (向量檢索結果)
     */
    private List<Document> longTermMemories;

    /**
     * 融合後的記憶項目 (統一格式)
     */
    private List<MemoryItem> fusedMemories;

    /**
     * 檢索時間戳
     */
    private LocalDateTime retrievalTime;

    /**
     * 檢索耗時 (毫秒)
     */
    private long retrievalTimeMs;
}
