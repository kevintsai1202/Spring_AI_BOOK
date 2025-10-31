package com.example.advancedrag.service;

import com.example.advancedrag.dto.AdvancedRAGRequest;
import com.example.advancedrag.dto.AdvancedRAGResponse;
import com.example.advancedrag.model.RAGQueryOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AdvancedRAGService 整合測試
 *
 * 測試完整的 RAG 流程：
 * 1. 基本查詢流程
 * 2. 查詢重寫功能
 * 3. 查詢擴展功能
 * 4. Re-ranking 功能
 * 5. 內容審核功能
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Advanced RAG 服務整合測試")
class AdvancedRAGServiceTest {

    @Autowired
    private AdvancedRAGService advancedRAGService;

    private AdvancedRAGRequest baseRequest;

    @BeforeEach
    void setUp() {
        // 初始化基礎請求
        baseRequest = AdvancedRAGRequest.builder()
                .query("什麼是 Spring AI？")
                .sessionId("test-session-001")
                .enableQueryRewrite(false)
                .enableQueryExpansion(false)
                .returnDocuments(true)
                .returnScoringDetails(false)
                .build();
    }

    @Test
    @DisplayName("測試1：基本 RAG 查詢流程")
    void testBasicRAGQuery() {
        // Given
        AdvancedRAGRequest request = baseRequest;

        // When
        AdvancedRAGResponse response = advancedRAGService.query(request);

        // Then
        assertNotNull(response, "響應不應為 null");
        assertNotNull(response.getQueryId(), "Query ID 不應為 null");
        assertNotNull(response.getAnswer(), "答案不應為 null");
        assertEquals("什麼是 Spring AI？", response.getOriginalQuery(), "原始查詢應匹配");
        assertNotNull(response.getTimestamp(), "時間戳不應為 null");
        assertTrue(response.getProcessingTimeMs() > 0, "處理時間應大於 0");

        System.out.println("=== 測試1：基本 RAG 查詢流程 ===");
        System.out.println("Query ID: " + response.getQueryId());
        System.out.println("原始查詢: " + response.getOriginalQuery());
        System.out.println("答案: " + response.getAnswer());
        System.out.println("處理時間: " + response.getProcessingTimeMs() + "ms");
        System.out.println("檢索時間: " + response.getRetrievalTimeMs() + "ms");
        System.out.println("生成時間: " + response.getGenerationTimeMs() + "ms");
    }

    @Test
    @DisplayName("測試2：查詢重寫功能")
    void testQueryRewrite() {
        // Given
        AdvancedRAGRequest request = baseRequest.toBuilder()
                .enableQueryRewrite(true)
                .build();

        // When
        AdvancedRAGResponse response = advancedRAGService.query(request);

        // Then
        assertNotNull(response.getRewrittenQuery(), "重寫查詢不應為 null");
        assertNotEquals(response.getOriginalQuery(), response.getRewrittenQuery(),
                "重寫查詢應與原始查詢不同");

        System.out.println("\n=== 測試2：查詢重寫功能 ===");
        System.out.println("原始查詢: " + response.getOriginalQuery());
        System.out.println("重寫查詢: " + response.getRewrittenQuery());
        System.out.println("答案: " + response.getAnswer());
    }

    @Test
    @DisplayName("測試3：查詢擴展功能")
    void testQueryExpansion() {
        // Given
        AdvancedRAGRequest request = baseRequest.toBuilder()
                .enableQueryExpansion(true)
                .options(RAGQueryOptions.builder()
                        .queryExpansionCount(3)
                        .build())
                .build();

        // When
        AdvancedRAGResponse response = advancedRAGService.query(request);

        // Then
        assertNotNull(response.getExpandedQueries(), "擴展查詢列表不應為 null");
        assertTrue(response.getExpandedQueries().size() > 0, "應該有擴展查詢");

        System.out.println("\n=== 測試3：查詢擴展功能 ===");
        System.out.println("原始查詢: " + response.getOriginalQuery());
        System.out.println("擴展查詢數量: " + response.getExpandedQueries().size());
        response.getExpandedQueries().forEach(q ->
                System.out.println("  - " + q));
        System.out.println("答案: " + response.getAnswer());
    }

    @Test
    @DisplayName("測試4：Re-ranking 功能")
    void testReranking() {
        // Given
        AdvancedRAGRequest request = baseRequest.toBuilder()
                .options(RAGQueryOptions.builder()
                        .enableReranking(true)
                        .coarseTopK(10)
                        .finalTopK(5)
                        .build())
                .build();

        // When
        AdvancedRAGResponse response = advancedRAGService.query(request);

        // Then
        assertNotNull(response, "響應不應為 null");
        assertTrue(response.getRerankingTimeMs() >= 0, "Re-ranking 時間應 >= 0");

        System.out.println("\n=== 測試4：Re-ranking 功能 ===");
        System.out.println("檢索時間: " + response.getRetrievalTimeMs() + "ms");
        System.out.println("Re-ranking 時間: " + response.getRerankingTimeMs() + "ms");
        System.out.println("生成時間: " + response.getGenerationTimeMs() + "ms");
        System.out.println("總處理時間: " + response.getProcessingTimeMs() + "ms");
    }

    @Test
    @DisplayName("測試5：返回文檔功能")
    void testReturnDocuments() {
        // Given
        AdvancedRAGRequest request = baseRequest.toBuilder()
                .returnDocuments(true)
                .returnScoringDetails(true)
                .options(RAGQueryOptions.builder()
                        .finalTopK(3)
                        .build())
                .build();

        // When
        AdvancedRAGResponse response = advancedRAGService.query(request);

        // Then
        assertNotNull(response.getDocuments(), "文檔列表不應為 null");
        assertTrue(response.getDocuments().size() > 0, "應該返回文檔");
        assertTrue(response.getDocuments().size() <= 3, "文檔數量不應超過 finalTopK");

        System.out.println("\n=== 測試5：返回文檔功能 ===");
        System.out.println("返回文檔數量: " + response.getDocuments().size());
        response.getDocuments().forEach(doc -> {
            System.out.println("\n文檔 #" + doc.getRank() + ":");
            System.out.println("  ID: " + doc.getDocumentId());
            System.out.println("  分數: " + doc.getScore());
            System.out.println("  來源: " + doc.getSource());
            System.out.println("  內容預覽: " + doc.getContent().substring(0,
                    Math.min(100, doc.getContent().length())) + "...");
            if (doc.getSemanticScore() != null) {
                System.out.println("  語義分數: " + doc.getSemanticScore());
                System.out.println("  BM25 分數: " + doc.getBm25Score());
            }
        });
    }

    @Test
    @DisplayName("測試6：內容審核功能 - 正常查詢")
    void testModerationNormalQuery() {
        // Given
        AdvancedRAGRequest request = baseRequest.toBuilder()
                .query("Spring AI 的主要功能有哪些？")
                .enableModeration(true)
                .build();

        // When
        AdvancedRAGResponse response = advancedRAGService.query(request);

        // Then
        assertNotNull(response, "響應不應為 null");
        assertNotNull(response.getAnswer(), "答案不應為 null");
        assertFalse(response.getAnswer().contains("不當內容"), "正常查詢應該返回正常答案");

        System.out.println("\n=== 測試6：內容審核功能 - 正常查詢 ===");
        System.out.println("查詢: " + response.getOriginalQuery());
        System.out.println("審核結果: 通過");
        System.out.println("答案: " + response.getAnswer());
    }

    @Test
    @DisplayName("測試7：內容審核功能 - 過長查詢")
    void testModerationLongQuery() {
        // Given
        String longQuery = "Spring AI ".repeat(200); // 超長查詢
        AdvancedRAGRequest request = baseRequest.toBuilder()
                .query(longQuery)
                .enableModeration(true)
                .build();

        // When
        AdvancedRAGResponse response = advancedRAGService.query(request);

        // Then
        assertNotNull(response, "響應不應為 null");
        System.out.println("\n=== 測試7：內容審核功能 - 過長查詢 ===");
        System.out.println("查詢長度: " + longQuery.length());
        System.out.println("審核結果: " + (response.getAnswer().contains("不當內容") ? "未通過" : "通過"));
        System.out.println("答案: " + response.getAnswer());
    }

    @Test
    @DisplayName("測試8：完整流程 - 啟用所有功能")
    void testFullPipeline() {
        // Given
        AdvancedRAGRequest request = baseRequest.toBuilder()
                .query("Spring AI 如何與 OpenAI 整合？")
                .enableQueryRewrite(true)
                .enableQueryExpansion(true)
                .enableModeration(true)
                .returnDocuments(true)
                .returnScoringDetails(true)
                .options(RAGQueryOptions.builder()
                        .enableReranking(true)
                        .coarseTopK(15)
                        .finalTopK(5)
                        .queryExpansionCount(3)
                        .build())
                .build();

        // When
        long startTime = System.currentTimeMillis();
        AdvancedRAGResponse response = advancedRAGService.query(request);
        long totalTime = System.currentTimeMillis() - startTime;

        // Then
        assertNotNull(response, "響應不應為 null");
        assertNotNull(response.getAnswer(), "答案不應為 null");
        assertNotNull(response.getRewrittenQuery(), "應該有重寫查詢");
        assertNotNull(response.getExpandedQueries(), "應該有擴展查詢");
        assertNotNull(response.getDocuments(), "應該返回文檔");

        System.out.println("\n=== 測試8：完整流程 - 啟用所有功能 ===");
        System.out.println("原始查詢: " + response.getOriginalQuery());
        System.out.println("重寫查詢: " + response.getRewrittenQuery());
        System.out.println("擴展查詢數: " + response.getExpandedQueries().size());
        System.out.println("返回文檔數: " + response.getDocuments().size());
        System.out.println("\n時間分析:");
        System.out.println("  檢索時間: " + response.getRetrievalTimeMs() + "ms");
        System.out.println("  Re-ranking 時間: " + response.getRerankingTimeMs() + "ms");
        System.out.println("  生成時間: " + response.getGenerationTimeMs() + "ms");
        System.out.println("  總處理時間: " + response.getProcessingTimeMs() + "ms");
        System.out.println("  實測時間: " + totalTime + "ms");
        System.out.println("\n答案: " + response.getAnswer());
    }

    @Test
    @DisplayName("測試9：性能測試 - 並發查詢")
    void testConcurrentQueries() throws InterruptedException {
        // Given
        int queryCount = 5;
        long startTime = System.currentTimeMillis();

        // When - 順序執行多個查詢
        for (int i = 0; i < queryCount; i++) {
            AdvancedRAGRequest request = baseRequest.toBuilder()
                    .query("測試查詢 #" + (i + 1))
                    .build();
            AdvancedRAGResponse response = advancedRAGService.query(request);
            assertNotNull(response, "響應 #" + (i + 1) + " 不應為 null");
        }

        // Then
        long totalTime = System.currentTimeMillis() - startTime;
        double avgTime = (double) totalTime / queryCount;

        System.out.println("\n=== 測試9：性能測試 - 順序查詢 ===");
        System.out.println("查詢數量: " + queryCount);
        System.out.println("總耗時: " + totalTime + "ms");
        System.out.println("平均耗時: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("吞吐量: " + String.format("%.2f", queryCount * 1000.0 / totalTime) + " queries/sec");
    }

    @Test
    @DisplayName("測試10：錯誤處理 - 空查詢")
    void testEmptyQuery() {
        // Given
        AdvancedRAGRequest request = baseRequest.toBuilder()
                .query("")
                .build();

        // When & Then
        assertThrows(Exception.class, () -> {
            advancedRAGService.query(request);
        }, "空查詢應該拋出異常");

        System.out.println("\n=== 測試10：錯誤處理 - 空查詢 ===");
        System.out.println("測試通過：空查詢正確拋出異常");
    }
}
