/**
 * 聊天請求 DTO
 * 用於接收聊天請求資料
 */
package com.example.dto;

import lombok.Data;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@Data
@Builder
public class ChatRequest {
    @NotBlank(message = "訊息不能為空")
    private String message;
    
    private Map<String, Object> metadata;
}