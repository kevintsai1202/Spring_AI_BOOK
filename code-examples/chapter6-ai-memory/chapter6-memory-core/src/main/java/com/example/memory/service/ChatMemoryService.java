package com.example.memory.service;

import com.example.memory.advisor.AdvisorContext;
import com.example.memory.dto.ChatRequest;
import com.example.memory.dto.ChatResponse;
import com.example.memory.dto.ConversationHistory;
import com.example.memory.memory.ChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 對話記憶服務
 *
 * 負責管理對話記憶和與AI模型的交互
 * 集成Advisor鏈機制
 */
@Slf4j
@Service
public class ChatMemoryService {

    /**
     * 記憶存儲
     */
    @Autowired
    private ChatMemory chatMemory;

    /**
     * Advisor鏈服務
     */
    @Autowired
    private AdvisorChainService advisorChainService;

    /**
     * Spring AI ChatClient
     */
    @Autowired
    private ChatClient chatClient;

    /**
     * 執行對話
     *
     * @param chatRequest 對話請求
     * @return 對話回應
     */
    public ChatResponse chat(ChatRequest chatRequest) {
        log.info("Processing chat request - Conversation: {}, User: {}",
            chatRequest.getConversationId(), chatRequest.getUserId());

        try {
            // 1. 構造Advisor上下文
            AdvisorContext context = AdvisorContext.success(
                chatRequest.getConversationId(),
                chatRequest.getMessage()
            );

            // 2. 執行Advisor鏈
            context = advisorChainService.executeChain(context);

            // 3. 檢查是否有錯誤
            if (context.isError()) {
                log.error("Error in advisor chain: {}", context.getErrorMessage());
                return ChatResponse.failure(
                    chatRequest.getConversationId(),
                    context.getErrorMessage()
                );
            }

            // 4. 調用LLM模型
            String aiResponse = callLLM(context.getModifiedPrompt());

            // 5. 設置回應
            context.setAiResponse(aiResponse);
            context.setResponseTime(LocalDateTime.now());

            // 6. 執行回應後的Advisor處理
            context = advisorChainService.executeAfterChain(context);

            // 7. 保存到記憶
            chatMemory.add(chatRequest.getConversationId(),
                new UserMessage(chatRequest.getMessage()));
            chatMemory.add(chatRequest.getConversationId(),
                new AssistantMessage(context.getAiResponse()));

            log.info("Chat completed successfully");

            // 8. 返回回應
            return ChatResponse.success(
                chatRequest.getConversationId(),
                context.getAiResponse()
            );

        } catch (Exception e) {
            log.error("Error in chat processing", e);
            return ChatResponse.failure(
                chatRequest.getConversationId(),
                "系統錯誤: " + e.getMessage()
            );
        }
    }

    /**
     * 調用LLM模型
     *
     * @param prompt 提示詞
     * @return AI回應
     */
    private String callLLM(String prompt) {
        log.debug("Calling LLM with prompt: {}", prompt);

        try {
            String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

            log.debug("LLM response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error calling LLM", e);
            return "抱歉，AI模型發生錯誤。";
        }
    }

    /**
     * 獲取對話歷史
     *
     * @param conversationId 對話ID
     * @param userId 用戶ID
     * @return 對話歷史
     */
    public ConversationHistory getConversationHistory(String conversationId, String userId) {
        log.info("Retrieving conversation history - Conversation: {}, User: {}",
            conversationId, userId);

        List<Message> messages = chatMemory.get(conversationId);

        ConversationHistory history = ConversationHistory.builder()
            .conversationId(conversationId)
            .userId(userId)
            .createdAt(LocalDateTime.now())
            .lastActivityAt(LocalDateTime.now())
            .build();

        // 轉換消息列表
        for (Message msg : messages) {
            String role = msg.getClass().getSimpleName();
            ConversationHistory.MessageDto messageDto = ConversationHistory.MessageDto.builder()
                .role(role)
                .timestamp(LocalDateTime.now())
                .isToolCall(false)
                .build();

            history.getMessages().add(messageDto);
        }

        return history;
    }

    /**
     * 清除對話歷史
     *
     * @param conversationId 對話ID
     */
    public void clearConversation(String conversationId) {
        log.info("Clearing conversation: {}", conversationId);
        chatMemory.clear(conversationId);
    }

    /**
     * 檢查對話是否存在
     *
     * @param conversationId 對話ID
     * @return 是否存在
     */
    public boolean conversationExists(String conversationId) {
        return chatMemory.exists(conversationId);
    }

    /**
     * 獲取對話的消息數量
     *
     * @param conversationId 對話ID
     * @return 消息數量
     */
    public long getMessageCount(String conversationId) {
        return chatMemory.count(conversationId);
    }
}
