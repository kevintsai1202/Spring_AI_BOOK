package com.example.mcpclient.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 對話回應
 *
 * 封裝 ChatClient 返回的對話回應信息。
 */
@Data
@Builder
public class ChatResponse {

    /**
     * AI 回答
     * AI 生成的回答內容
     */
    private String answer;

    /**
     * 使用的工具列表
     * 記錄 AI 在生成回答過程中調用了哪些工具
     */
    @Builder.Default
    private List<String> usedTools = new ArrayList<>();

    /**
     * 處理時間 (毫秒)
     * 從接收請求到生成回答的總耗時
     */
    private long processingTime;

    /**
     * 是否成功
     * 標識請求是否成功處理
     */
    @Builder.Default
    private boolean success = true;

    /**
     * 錯誤訊息 (如果有)
     * 當 success 為 false 時，包含錯誤詳情
     */
    private String errorMessage;
}
