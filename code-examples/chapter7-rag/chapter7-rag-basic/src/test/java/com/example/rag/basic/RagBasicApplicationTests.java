package com.example.rag.basic;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Spring Boot 應用程序測試
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.ai.openai.api-key=test-key",
        "spring.ai.vectorstore.neo4j.uri=bolt://localhost:7687",
        "spring.ai.vectorstore.neo4j.username=neo4j",
        "spring.ai.vectorstore.neo4j.password=test1234"
})
class RagBasicApplicationTests {

    @Test
    void contextLoads() {
        // 測試應用程序上下文是否能夠正常載入
    }

}
