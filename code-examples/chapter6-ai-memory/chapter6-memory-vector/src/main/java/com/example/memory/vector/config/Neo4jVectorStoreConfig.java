package com.example.memory.vector.config;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore.Neo4jDistanceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Neo4j 向量存儲配置類
 *
 * 配置 Neo4j Driver、VectorStore 和相關設置
 */
@Slf4j
@Configuration
public class Neo4jVectorStoreConfig {

    @Value("${spring.neo4j.uri:bolt://localhost:7687}")
    private String neo4jUri;

    @Value("${spring.neo4j.authentication.username:neo4j}")
    private String neo4jUsername;

    @Value("${spring.neo4j.authentication.password:password}")
    private String neo4jPassword;

    @Value("${spring.neo4j.database:neo4j}")
    private String neo4jDatabase;

    /**
     * 配置 Neo4j Driver Bean
     */
    @Bean
    public Driver neo4jDriver() {
        log.info("初始化 Neo4j Driver: {}", neo4jUri);
        return GraphDatabase.driver(
            neo4jUri,
            AuthTokens.basic(neo4jUsername, neo4jPassword)
        );
    }

    /**
     * 配置 Neo4jVectorStore Bean
     *
     * @param driver Neo4j 驅動
     * @param embeddingModel Embedding 模型
     * @return 配置好的 VectorStore
     */
    @Bean
    public VectorStore vectorStore(Driver driver, EmbeddingModel embeddingModel) {
        log.info("初始化 Neo4jVectorStore");

        return Neo4jVectorStore.builder(driver, embeddingModel)
            .databaseName(neo4jDatabase)
            .distanceType(Neo4jDistanceType.COSINE)
            .embeddingDimension(1536)
            .label("MemoryDocument")
            .embeddingProperty("embedding")
            .indexName("memory-vector-index")
            .initializeSchema(true)
            .build();
    }
}
