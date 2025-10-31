package com.example.memory.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InMemoryChatMemory 單元測試
 */
class InMemoryChatMemoryTest {

    private InMemoryChatMemory memory;

    @BeforeEach
    void setUp() {
        memory = new InMemoryChatMemory();
    }

    /**
     * 測試添加單條消息
     */
    @Test
    void testAddSingleMessage() {
        String conversationId = "conv-123";
        String messageContent = "Hello, World!";

        UserMessage userMessage = new UserMessage(messageContent);
        memory.add(conversationId, userMessage);

        assertTrue(memory.exists(conversationId));
        assertEquals(1, memory.count(conversationId));
    }

    /**
     * 測試批量添加消息
     */
    @Test
    void testAddMultipleMessages() {
        String conversationId = "conv-456";
        List<org.springframework.ai.chat.messages.Message> messages = List.of(
            new UserMessage("第一條消息"),
            new AssistantMessage("回應1"),
            new UserMessage("第二條消息"),
            new AssistantMessage("回應2")
        );

        memory.addAll(conversationId, messages);

        assertEquals(4, memory.count(conversationId));
    }

    /**
     * 測試獲取對話消息
     */
    @Test
    void testGetMessages() {
        String conversationId = "conv-789";
        UserMessage userMessage = new UserMessage("Test message");
        AssistantMessage assistantMessage = new AssistantMessage("Test response");

        memory.add(conversationId, userMessage);
        memory.add(conversationId, assistantMessage);

        List<org.springframework.ai.chat.messages.Message> messages = memory.get(conversationId);

        assertEquals(2, messages.size());
    }

    /**
     * 測試獲取最近的消息
     */
    @Test
    void testGetRecentMessages() {
        String conversationId = "conv-101";

        // 添加5條消息
        for (int i = 1; i <= 5; i++) {
            memory.add(conversationId, new UserMessage("Message " + i));
        }

        // 只獲取最後2條
        List<org.springframework.ai.chat.messages.Message> recentMessages = memory.getRecent(conversationId, 2);

        assertEquals(2, recentMessages.size());
    }

    /**
     * 測試清除對話
     */
    @Test
    void testClearConversation() {
        String conversationId = "conv-202";

        memory.add(conversationId, new UserMessage("Message"));

        assertTrue(memory.exists(conversationId));

        memory.clear(conversationId);

        assertFalse(memory.exists(conversationId));
        assertEquals(0, memory.count(conversationId));
    }

    /**
     * 測試非存在的對話
     */
    @Test
    void testNonExistentConversation() {
        String conversationId = "non-existent";

        assertFalse(memory.exists(conversationId));
        assertEquals(0, memory.count(conversationId));
        assertTrue(memory.get(conversationId).isEmpty());
    }
}
