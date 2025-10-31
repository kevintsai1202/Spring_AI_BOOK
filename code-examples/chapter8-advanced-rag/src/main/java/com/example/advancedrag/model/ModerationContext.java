package com.example.advancedrag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 審核上下文
 *
 * 定義內容審核的上下文信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationContext {

    /**
     * 會話 ID
     */
    private String sessionId;

    /**
     * 用戶 ID
     */
    private String userId;

    /**
     * 內容類型（user_query, system_response, document, etc.）
     */
    @Builder.Default
    private String contentType = "user_query";

    /**
     * 額外的元數據
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 創建默認上下文
     */
    public static ModerationContext defaultContext() {
        return ModerationContext.builder().build();
    }

    /**
     * 創建用戶查詢上下文
     */
    public static ModerationContext userQuery(String sessionId, String userId) {
        return ModerationContext.builder()
                .sessionId(sessionId)
                .userId(userId)
                .contentType("user_query")
                .build();
    }

    /**
     * 創建系統回應上下文
     */
    public static ModerationContext systemResponse(String sessionId) {
        return ModerationContext.builder()
                .sessionId(sessionId)
                .contentType("system_response")
                .build();
    }
}
