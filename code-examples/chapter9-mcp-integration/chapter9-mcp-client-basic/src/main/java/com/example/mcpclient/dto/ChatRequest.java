package com.example.mcpclient.dto;

import lombok.Builder;
import lombok.Data;

/**
 * AI 對話請求
 *
 * 封裝用戶向 ChatClient 發送的對話請求信息。
 */
@Data
@Builder
public class ChatRequest {

    /**
     * 用戶問題
     * 用戶輸入的自然語言問題或指令
     */
    private String question;

    /**
     * 是否啟用工具調用
     * 設為 true 時，AI 可以調用 MCP 工具來回答問題
     * 設為 false 時，AI 只使用其內部知識回答
     */
    @Builder.Default
    private boolean enableTools = true;

    /**
     * 指定使用的 Server (可選)
     * 如果指定，則只使用該 Server 的工具
     * 如果為 null，則使用所有可用 Server 的工具
     */
    private String serverName;
}
