/**
 * 聊天記憶配置屬性
 * 定義記憶系統的配置參數
 */
package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.chat.memory")
@Data
public class ChatMemoryProperties {
    
    /**
     * 記憶存儲類型
     */
    private String type = "memory";
    
    /**
     * 最大訊息數量
     */
    private int maxMessages = 100;
    
    /**
     * 記憶過期時間（小時）
     */
    private int expirationHours = 24;
    
    /**
     * 是否啟用記憶壓縮
     */
    private boolean compressionEnabled = false;
    
    /**
     * 壓縮閾值
     */
    private int compressionThreshold = 200;
}