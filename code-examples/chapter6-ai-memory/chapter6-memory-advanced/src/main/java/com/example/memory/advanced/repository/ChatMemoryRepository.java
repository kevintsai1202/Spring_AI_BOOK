package com.example.memory.advanced.repository;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 聊天記憶儲存庫介面
 *
 * 定義記憶存儲的基本操作
 */
public interface ChatMemoryRepository {

    /**
     * 添加訊息到指定對話
     *
     * @param conversationId 對話 ID
     * @param messages       訊息列表
     */
    void add(String conversationId, List<Message> messages);

    /**
     * 獲取指定對話的所有訊息
     *
     * @param conversationId 對話 ID
     * @return 訊息列表
     */
    List<Message> get(String conversationId);

    /**
     * 清除指定對話的所有訊息
     *
     * @param conversationId 對話 ID
     */
    void clear(String conversationId);

    /**
     * 獲取指定對話的訊息數量
     *
     * @param conversationId 對話 ID
     * @return 訊息數量
     */
    int count(String conversationId);

    /**
     * 獲取所有對話 ID
     *
     * @return 對話 ID 列表
     */
    List<String> getAllConversationIds();
}
