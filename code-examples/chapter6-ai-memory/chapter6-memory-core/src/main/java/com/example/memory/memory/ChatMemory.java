package com.example.memory.memory;

import org.springframework.ai.chat.messages.Message;
import java.util.List;

/**
 * 對話記憶介面
 *
 * 定義了標準的記憶操作方法
 * 支持多種後端實現（InMemory、JDBC、Redis等）
 */
public interface ChatMemory {

    /**
     * 添加單條消息到記憶
     *
     * @param conversationId 對話ID
     * @param message 消息對象
     */
    void add(String conversationId, Message message);

    /**
     * 批量添加消息
     *
     * @param conversationId 對話ID
     * @param messages 消息列表
     */
    void addAll(String conversationId, List<Message> messages);

    /**
     * 獲取對話的所有消息
     *
     * @param conversationId 對話ID
     * @return 消息列表
     */
    List<Message> get(String conversationId);

    /**
     * 獲取對話的最近N條消息
     *
     * @param conversationId 對話ID
     * @param limit 消息數量限制
     * @return 消息列表
     */
    List<Message> getRecent(String conversationId, int limit);

    /**
     * 清除對話的所有消息
     *
     * @param conversationId 對話ID
     */
    void clear(String conversationId);

    /**
     * 檢查對話是否存在
     *
     * @param conversationId 對話ID
     * @return 是否存在
     */
    boolean exists(String conversationId);

    /**
     * 獲取對話的消息數量
     *
     * @param conversationId 對話ID
     * @return 消息數量
     */
    long count(String conversationId);
}
