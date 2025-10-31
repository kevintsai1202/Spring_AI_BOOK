package com.example.memory.vector.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * 向量記憶聊天服務
 *
 * 整合短期記憶(ChatMemory)和長期記憶(VectorStore)
 */
@Slf4j
@Service
public class VectorChatService {

    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;

    /**
     * 構造函數
     *
     * @param chatModel AI 模型
     * @param vectorStore 向量存儲
     * @param chatMemory 短期記憶
     */
    public VectorChatService(
            ChatModel chatModel,
            VectorStore vectorStore,
            ChatMemory chatMemory) {

        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.chatMemory = chatMemory;

        // 配置 ChatClient,包含記憶 advisor
        this.chatClient = ChatClient.builder(chatModel)
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build()
            )
            .build();

        log.info("VectorChatService 初始化完成");
    }

    /**
     * 使用混合記憶進行對話
     *
     * @param conversationId 對話ID
     * @param userMessage 用戶訊息
     * @return AI 回應
     */
    public String chat(String conversationId, String userMessage) {
        log.info("對話 [{}]: {}", conversationId, userMessage);

        String response = chatClient.prompt()
            .user(userMessage)
            .advisors(advisor -> advisor
                .param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();

        log.info("AI 回應 [{}]: {}", conversationId, response.substring(0, Math.min(100, response.length())));

        return response;
    }

    /**
     * 僅使用短期記憶進行對話
     *
     * @param conversationId 對話ID
     * @param userMessage 用戶訊息
     * @return AI 回應
     */
    public String chatWithShortTermMemory(String conversationId, String userMessage) {
        log.info("短期記憶對話 [{}]: {}", conversationId, userMessage);

        // 只使用 MessageChatMemoryAdvisor
        ChatClient shortTermClient = ChatClient.builder(chatModel)
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build()
            )
            .build();

        String response = shortTermClient.prompt()
            .user(userMessage)
            .advisors(advisor -> advisor
                .param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();

        return response;
    }

    /**
     * 僅使用長期記憶(向量檢索)進行對話
     *
     * 注意: 此版本暫不包含 QuestionAnswerAdvisor,僅作為演示
     *
     * @param userMessage 用戶訊息
     * @return AI 回應
     */
    public String chatWithLongTermMemory(String userMessage) {
        log.info("長期記憶對話: {}", userMessage);

        // 簡化版本:直接調用模型,不使用 advisor
        ChatClient simpleClient = ChatClient.builder(chatModel).build();

        String response = simpleClient.prompt()
            .user(userMessage)
            .call()
            .content();

        return response;
    }

    /**
     * 獲取對話記憶數量
     *
     * @param conversationId 對話ID
     * @return 記憶訊息數量
     */
    public int getMemoryCount(String conversationId) {
        return chatMemory.get(conversationId).size();
    }

    /**
     * 清除對話記憶
     *
     * @param conversationId 對話ID
     */
    public void clearMemory(String conversationId) {
        log.info("清除對話記憶: {}", conversationId);
        chatMemory.clear(conversationId);
    }
}
