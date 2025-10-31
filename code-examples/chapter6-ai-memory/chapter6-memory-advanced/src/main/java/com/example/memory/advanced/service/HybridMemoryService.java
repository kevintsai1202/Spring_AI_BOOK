package com.example.memory.advanced.service;

import com.example.memory.advanced.model.MemoryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 混合記憶服務
 *
 * 結合短期記憶和長期記憶的優勢:
 * - 短期記憶: 最近的對話內容
 * - 長期記憶: 語義相似的歷史對話
 * - 混合策略: 根據查詢類型動態選擇
 */
@Service
public class HybridMemoryService {

    private static final Logger log = LoggerFactory.getLogger(HybridMemoryService.class);

    private final ChatClient basicChatClient;
    private final ChatMemory shortTermMemory;

    public HybridMemoryService(
        @Qualifier("basicChatClient") ChatClient basicChatClient,
        @Qualifier("shortTermMemory") ChatMemory shortTermMemory
    ) {
        this.basicChatClient = basicChatClient;
        this.shortTermMemory = shortTermMemory;
    }

    /**
     * 使用混合記憶策略進行對話
     */
    public String chat(String conversationId, String userMessage, MemoryStrategy strategy) {
        log.info("混合記憶對話 - ID: {}, 策略: {}", conversationId, strategy);

        return switch (strategy) {
            case SHORT_TERM_ONLY -> chatWithShortTerm(conversationId, userMessage);
            case LONG_TERM_ONLY -> chatWithLongTerm(conversationId, userMessage);
            case HYBRID -> chatWithHybrid(conversationId, userMessage);
        };
    }

    /**
     * 僅使用短期記憶
     */
    private String chatWithShortTerm(String conversationId, String userMessage) {
        return basicChatClient.prompt()
            .advisors(MessageChatMemoryAdvisor.builder(shortTermMemory).build())
            .advisors(a -> a.param("conversationId", conversationId))
            .user(userMessage)
            .call()
            .content();
    }

    /**
     * 僅使用長期記憶 (暫時實作為短期記憶)
     */
    private String chatWithLongTerm(String conversationId, String userMessage) {
        // TODO: 整合向量儲存作為長期記憶
        return chatWithShortTerm(conversationId, userMessage);
    }

    /**
     * 混合策略 (結合短期和長期記憶)
     */
    private String chatWithHybrid(String conversationId, String userMessage) {
        // TODO: 實作混合策略
        return chatWithShortTerm(conversationId, userMessage);
    }

    /**
     * 根據用戶訊息自動判斷策略
     */
    public MemoryStrategy determineStrategy(String userMessage) {
        if (userMessage.contains("剛才") || userMessage.contains("剛剛")) {
            return MemoryStrategy.SHORT_TERM_ONLY;
        }
        if (userMessage.contains("之前") || userMessage.contains("記得")) {
            return MemoryStrategy.LONG_TERM_ONLY;
        }
        return MemoryStrategy.HYBRID;
    }
}
