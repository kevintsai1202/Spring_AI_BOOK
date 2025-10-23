/**
 * 聊天回應 DTO
 * 用於返回聊天回應資料
 */
package com.example.dto;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ChatResponse {
    private String conversationId;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
}