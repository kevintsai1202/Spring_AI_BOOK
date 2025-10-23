/**
 * 基本聊天記憶服務
 * 提供基礎的對話記憶功能
 */
package com.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BasicChatMemoryService {
    
    private final ChatClient chatClient;
    private final ChatMemory chatMemory = new InMemoryChatMemory();
    
    /**
     * 基本對話記憶實現
     */
    public String chat(String conversationId, String userMessage) {
        return chatClient.prompt()
            .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(conversationId)
                .lastN(30)  // 保留最近 30 條訊息
                .build())
            .user(userMessage)
            .call()
            .content();
    }
    
    /**
     * 獲取對話歷史
     */
    public List<Message> getConversationHistory(String conversationId) {
        return chatMemory.get(conversationId);
    }
    
    /**
     * 清除對話記憶
     */
    public void clearConversation(String conversationId) {
        chatMemory.clear(conversationId);
    }
}