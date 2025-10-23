/**
 * 聊天記憶配置類別
 * 配置不同類型的記憶存儲後端
 */
package com.example.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(ChatMemoryProperties.class)
public class ChatMemoryConfig {
    
    /**
     * 記憶體存儲（開發環境）
     */
    @Bean
    @ConditionalOnProperty(name = "app.chat.memory.type", havingValue = "memory")
    public ChatMemory inMemoryChatMemory() {
        return new InMemoryChatMemory();
    }
    
    /**
     * JDBC 存儲（生產環境）
     */
    @Bean
    @ConditionalOnProperty(name = "app.chat.memory.type", havingValue = "jdbc")
    public ChatMemory jdbcChatMemory(DataSource dataSource) {
        return MessageWindowChatMemory.builder()
            .maxMessages(200)
            .chatMemoryRepository(new JdbcChatMemoryRepository(dataSource))
            .build();
    }
    
    /**
     * Redis 存儲（高效能環境）
     */
    @Bean
    @ConditionalOnProperty(name = "app.chat.memory.type", havingValue = "redis")
    public ChatMemory redisChatMemory(RedisTemplate<String, Object> redisTemplate) {
        return MessageWindowChatMemory.builder()
            .maxMessages(500)
            .chatMemoryRepository(new RedisChatMemoryRepository(redisTemplate))
            .build();
    }
    
    /**
     * Neo4j 存儲（圖形資料庫）
     */
    @Bean
    @ConditionalOnProperty(name = "app.chat.memory.type", havingValue = "neo4j")
    public ChatMemory neo4jChatMemory(Neo4jTemplate neo4jTemplate) {
        return MessageWindowChatMemory.builder()
            .maxMessages(1000)
            .chatMemoryRepository(new Neo4jChatMemoryRepository(neo4jTemplate))
            .build();
    }
}