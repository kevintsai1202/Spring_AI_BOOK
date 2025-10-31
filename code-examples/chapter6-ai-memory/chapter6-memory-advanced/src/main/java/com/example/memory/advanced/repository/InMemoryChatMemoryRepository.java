package com.example.memory.advanced.repository;

import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 記憶體聊天記憶儲存庫實作
 *
 * 使用 ConcurrentHashMap 在記憶體中儲存對話記憶
 */
@Repository
public class InMemoryChatMemoryRepository implements ChatMemoryRepository {

    /**
     * 對話記憶儲存 (conversationId -> messages)
     */
    private final Map<String, List<Message>> memory = new ConcurrentHashMap<>();

    @Override
    public void add(String conversationId, List<Message> messages) {
        memory.computeIfAbsent(conversationId, k -> new ArrayList<>())
            .addAll(messages);
    }

    @Override
    public List<Message> get(String conversationId) {
        return new ArrayList<>(memory.getOrDefault(conversationId, List.of()));
    }

    @Override
    public void clear(String conversationId) {
        memory.remove(conversationId);
    }

    @Override
    public int count(String conversationId) {
        return memory.getOrDefault(conversationId, List.of()).size();
    }

    @Override
    public List<String> getAllConversationIds() {
        return new ArrayList<>(memory.keySet());
    }
}
