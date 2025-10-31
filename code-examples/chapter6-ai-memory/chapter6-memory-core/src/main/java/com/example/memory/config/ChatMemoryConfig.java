package com.example.memory.config;

import com.example.memory.memory.ChatMemory;
import com.example.memory.memory.InMemoryChatMemory;
import com.example.memory.memory.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ChatMemory 配置類
 *
 * 配置記憶存儲的實現方式
 */
@Slf4j
@Configuration
public class ChatMemoryConfig {

    /**
     * 記憶存儲類型
     * 支援: memory, window, jdbc, redis
     */
    @Value("${memory.storage-type:memory}")
    private String storageType;

    /**
     * 滑動窗口大小
     */
    @Value("${memory.window-size:50}")
    private int windowSize;

    /**
     * 內存記憶實現
     */
    @Bean
    public InMemoryChatMemory inMemoryChatMemory() {
        log.info("Creating InMemoryChatMemory");
        return new InMemoryChatMemory();
    }

    /**
     * 根據配置選擇記憶實現
     *
     * @param inMemoryChatMemory 內存實現
     * @return 配置後的ChatMemory實現
     */
    @Bean
    @Primary
    public ChatMemory chatMemory(@Autowired InMemoryChatMemory inMemoryChatMemory) {
        log.info("Configuring ChatMemory with storage-type: {}", storageType);

        ChatMemory memory = null;

        switch (storageType.toLowerCase()) {
            case "memory":
                log.info("Using InMemory storage");
                memory = inMemoryChatMemory;
                break;

            case "window":
                log.info("Using MessageWindow storage with window size: {}", windowSize);
                memory = new MessageWindowChatMemory(inMemoryChatMemory, windowSize);
                break;

            case "jdbc":
                log.info("JDBC storage type selected (not implemented in this example)");
                memory = inMemoryChatMemory;
                break;

            case "redis":
                log.info("Redis storage type selected (not implemented in this example)");
                memory = inMemoryChatMemory;
                break;

            default:
                log.warn("Unknown storage-type: {}, falling back to InMemory", storageType);
                memory = inMemoryChatMemory;
        }

        return memory;
    }
}
