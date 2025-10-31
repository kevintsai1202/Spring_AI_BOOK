package com.example.memory.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基於內存的對話記憶實現
 *
 * 適用於開發環境和測試環境
 * 不提供持久化存儲，應用重啟後記憶丟失
 */
@Slf4j
@Component
public class InMemoryChatMemory implements ChatMemory {

    /**
     * 使用ConcurrentHashMap存儲所有對話及其消息
     * Key: conversationId, Value: 消息列表
     */
    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();

    /**
     * 添加單條消息
     */
    @Override
    public void add(String conversationId, Message message) {
        log.debug("Adding message to conversation: {}",
            conversationId);

        conversations.computeIfAbsent(conversationId, k -> new ArrayList<>())
            .add(message);
    }

    /**
     * 批量添加消息
     */
    @Override
    public void addAll(String conversationId, List<Message> messages) {
        log.debug("Adding {} messages to conversation: {}",
            messages.size(), conversationId);

        conversations.computeIfAbsent(conversationId, k -> new ArrayList<>())
            .addAll(messages);
    }

    /**
     * 獲取對話的所有消息
     */
    @Override
    public List<Message> get(String conversationId) {
        log.debug("Retrieving all messages for conversation: {}", conversationId);
        return conversations.getOrDefault(conversationId, Collections.emptyList());
    }

    /**
     * 獲取最近N條消息
     */
    @Override
    public List<Message> getRecent(String conversationId, int limit) {
        log.debug("Retrieving recent {} messages for conversation: {}",
            limit, conversationId);

        List<Message> messages = conversations.getOrDefault(conversationId, Collections.emptyList());

        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        // 返回最後的 limit 條消息
        int startIndex = Math.max(0, messages.size() - limit);
        return new ArrayList<>(messages.subList(startIndex, messages.size()));
    }

    /**
     * 清除對話的所有消息
     */
    @Override
    public void clear(String conversationId) {
        log.info("Clearing messages for conversation: {}", conversationId);
        conversations.remove(conversationId);
    }

    /**
     * 檢查對話是否存在
     */
    @Override
    public boolean exists(String conversationId) {
        return conversations.containsKey(conversationId);
    }

    /**
     * 獲取對話的消息數量
     */
    @Override
    public long count(String conversationId) {
        return conversations.getOrDefault(conversationId, Collections.emptyList()).size();
    }

    /**
     * 獲取所有對話的ID（調試用）
     */
    public Set<String> getAllConversationIds() {
        return conversations.keySet();
    }

    /**
     * 清除所有對話（調試用）
     */
    public void clearAll() {
        log.warn("Clearing all conversations");
        conversations.clear();
    }
}
