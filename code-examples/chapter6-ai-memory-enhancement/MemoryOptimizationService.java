/**
 * 記憶優化服務
 * 提供記憶壓縮和清理功能
 */
package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemoryOptimizationService {
    
    private final ChatMemory chatMemory;
    private final ChatClient summaryClient;
    
    /**
     * 智能記憶清理策略
     */
    @Scheduled(fixedRate = 3600000) // 每小時執行一次
    public void cleanupOldConversations() {
        // 清理超過 7 天的對話
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        
        // 實現具體的清理邏輯
        cleanupConversationsOlderThan(cutoff);
    }
    
    /**
     * 記憶壓縮策略
     */
    public void compressLongConversation(String conversationId) {
        List<Message> messages = chatMemory.get(conversationId);
        
        if (messages.size() > 100) {
            // 保留最近 50 條訊息
            List<Message> recentMessages = messages.subList(messages.size() - 50, messages.size());
            
            // 將較舊的訊息進行摘要
            String summary = summarizeOldMessages(messages.subList(0, messages.size() - 50));
            
            // 重建記憶
            chatMemory.clear(conversationId);
            chatMemory.add(conversationId, new SystemMessage("對話摘要：" + summary));
            chatMemory.add(conversationId, recentMessages);
        }
    }
    
    /**
     * 獲取活躍對話 ID
     */
    private Set<String> getActiveConversations() {
        // 這裡需要根據實際的 ChatMemory 實現來獲取所有對話 ID
        // 由於 ChatMemory 介面沒有提供此方法，這裡提供一個示例實現
        return Set.of(); // 實際實現需要根據具體的存儲後端來獲取
    }
    
    /**
     * 清理舊對話
     */
    private void cleanupConversationsOlderThan(LocalDateTime cutoff) {
        Set<String> activeConversations = getActiveConversations();
        
        for (String conversationId : activeConversations) {
            // 檢查對話的最後更新時間
            // 如果超過截止時間，則清理該對話
            // 這裡需要根據實際的存儲實現來判斷時間
            log.info("檢查對話 {} 是否需要清理", conversationId);
        }
    }
    
    /**
     * 摘要舊訊息
     */
    private String summarizeOldMessages(List<Message> messages) {
        String conversation = messages.stream()
            .map(Message::getContent)
            .collect(Collectors.joining("\n"));
            
        return summaryClient.prompt()
            .user("請簡潔摘要以下對話的重點：\n" + conversation)
            .call()
            .content();
    }
}