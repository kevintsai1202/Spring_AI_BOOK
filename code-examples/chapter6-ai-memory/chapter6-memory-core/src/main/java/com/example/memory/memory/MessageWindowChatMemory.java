package com.example.memory.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import java.util.*;

/**
 * 滑動窗口對話記憶實現
 *
 * 使用滑動窗口策略，只保留最近的N條消息
 * 適用於長時間的對話，減少內存占用和上下文長度
 */
@Slf4j
public class MessageWindowChatMemory implements ChatMemory {

    /**
     * 滑動窗口大小，預設保留最近50條消息
     */
    private final int maxMessages;

    /**
     * 底層記憶存儲
     */
    private final ChatMemory underlyingMemory;

    public MessageWindowChatMemory(ChatMemory underlyingMemory, int maxMessages) {
        this.underlyingMemory = underlyingMemory;
        this.maxMessages = maxMessages;
        log.info("Initializing MessageWindowChatMemory with window size: {}", maxMessages);
    }

    /**
     * 添加消息並執行滑動窗口清理
     */
    @Override
    public void add(String conversationId, Message message) {
        log.debug("Adding message to conversation: {} - Window size: {}",
            conversationId, maxMessages);

        underlyingMemory.add(conversationId, message);
        slideWindow(conversationId);
    }

    /**
     * 批量添加消息
     */
    @Override
    public void addAll(String conversationId, List<Message> messages) {
        log.debug("Adding {} messages to conversation: {} - Window size: {}",
            messages.size(), conversationId, maxMessages);

        underlyingMemory.addAll(conversationId, messages);
        slideWindow(conversationId);
    }

    /**
     * 獲取所有消息
     */
    @Override
    public List<Message> get(String conversationId) {
        return underlyingMemory.get(conversationId);
    }

    /**
     * 獲取最近N條消息
     */
    @Override
    public List<Message> getRecent(String conversationId, int limit) {
        return underlyingMemory.getRecent(conversationId, limit);
    }

    /**
     * 清除對話
     */
    @Override
    public void clear(String conversationId) {
        underlyingMemory.clear(conversationId);
    }

    /**
     * 檢查對話是否存在
     */
    @Override
    public boolean exists(String conversationId) {
        return underlyingMemory.exists(conversationId);
    }

    /**
     * 獲取消息數量
     */
    @Override
    public long count(String conversationId) {
        return underlyingMemory.count(conversationId);
    }

    /**
     * 執行滑動窗口清理
     *
     * 保留最近的maxMessages條消息
     * 刪除超過限制的舊消息
     */
    private void slideWindow(String conversationId) {
        List<Message> messages = underlyingMemory.get(conversationId);

        if (messages.size() > maxMessages) {
            log.info("Sliding window for conversation: {} - Messages: {} -> {}",
                conversationId, messages.size(), maxMessages);

            // 獲取要刪除的消息數量
            int deleteCount = messages.size() - maxMessages;

            // 保留最後的maxMessages條消息
            List<Message> recentMessages = new ArrayList<>(
                messages.subList(deleteCount, messages.size())
            );

            // 清除舊記憶並重新添加最近的消息
            underlyingMemory.clear(conversationId);
            underlyingMemory.addAll(conversationId, recentMessages);

            log.debug("Window slide completed - Deleted {} messages", deleteCount);
        }
    }

    /**
     * 設置新的窗口大小
     */
    public void setMaxMessages(int newMaxMessages) {
        log.info("Changing window size from {} to {}", maxMessages, newMaxMessages);
    }

    /**
     * 獲取當前窗口大小
     */
    public int getMaxMessages() {
        return maxMessages;
    }
}
