package com.example.memory.advisor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Advisor 上下文對象
 *
 * 在Advisor鏈中流轉，攜帶請求和回應信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvisorContext {

    /**
     * 對話ID
     */
    private String conversationId;

    /**
     * 用戶ID
     */
    private String userId;

    /**
     * 用戶的原始輸入
     */
    private String userMessage;

    /**
     * 修改後的提示詞（可能被Advisor修改）
     */
    private String modifiedPrompt;

    /**
     * AI的回應
     */
    private String aiResponse;

    /**
     * 是否已執行
     */
    private boolean executed = false;

    /**
     * 是否有錯誤
     */
    private boolean error = false;

    /**
     * 錯誤信息
     */
    private String errorMessage;

    /**
     * 請求時間戳
     */
    private LocalDateTime requestTime;

    /**
     * 回應時間戳
     */
    private LocalDateTime responseTime;

    /**
     * 額外的上下文數據
     * Advisor可以在此存儲自定義數據
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 添加元數據
     */
    public void putMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * 獲取元數據
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) {
        return (T) this.metadata.get(key);
    }

    /**
     * 檢查元數據是否存在
     */
    public boolean hasMetadata(String key) {
        return this.metadata.containsKey(key);
    }

    /**
     * 構造成功的上下文
     */
    public static AdvisorContext success(String conversationId, String userMessage) {
        return AdvisorContext.builder()
            .conversationId(conversationId)
            .userMessage(userMessage)
            .modifiedPrompt(userMessage)
            .requestTime(LocalDateTime.now())
            .error(false)
            .build();
    }

    /**
     * 構造失敗的上下文
     */
    public static AdvisorContext failure(String conversationId, String errorMessage) {
        return AdvisorContext.builder()
            .conversationId(conversationId)
            .error(true)
            .errorMessage(errorMessage)
            .requestTime(LocalDateTime.now())
            .build();
    }
}
