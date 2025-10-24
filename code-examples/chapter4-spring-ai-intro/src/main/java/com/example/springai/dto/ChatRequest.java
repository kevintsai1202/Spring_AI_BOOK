/**
 * 聊天請求 DTO
 * 用於接收帶有系統提示詞的聊天請求
 */
package com.example.springai.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String systemMessage;
    private String userMessage;
}
