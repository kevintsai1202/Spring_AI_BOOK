package com.example.advancedrag.reranking;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Voyage AI Re-ranking 提供者簡單測試
 *
 * 不依賴 Spring Boot 上下文，直接測試 Voyage API 功能
 */
@Slf4j
class VoyageRerankingProviderSimpleTest {

    // 從環境變數讀取 API Key
    private static final String VOYAGE_API_KEY = System.getenv("VOYAGE_API_KEY");
    private static final String TEST_MODEL = "rerank-2.5";

    private VoyageRerankingProvider provider;

    @BeforeEach
    void setUp() {
        log.info("=== 設定測試環境 ===");
        log.info("API Key: {}", VOYAGE_API_KEY != null && !VOYAGE_API_KEY.isEmpty() ? "已配置 (" + VOYAGE_API_KEY.substring(0, 10) + "...)" : "未配置");
        log.info("測試模型: {}", TEST_MODEL);

        if (VOYAGE_API_KEY == null || VOYAGE_API_KEY.isEmpty()) {
            log.warn("警告: VOYAGE_API_KEY 未設定，測試可能會失敗");
        }

        RestClient restClient = RestClient.builder().build();
        provider = new VoyageRerankingProvider(restClient, VOYAGE_API_KEY, TEST_MODEL);
    }

    /**
     * 測試 API 可用性
     */
    @Test
    void testProviderAvailability() {
        log.info("\n=== 測試 1: 檢查 Voyage AI Provider 是否可用 ===");

        assertNotNull(provider, "Provider 不應為 null");
        assertTrue(provider.isAvailable(), "Provider 應該是可用的（需要配置 VOYAGE_API_KEY）");
        assertEquals("voyage-ai", provider.getProviderName(), "Provider 名稱應該是 voyage-ai");

        log.info("✓ Provider 可用性測試通過");
    }

    /**
     * 測試基本的 Re-ranking 功能
     */
    @Test
    void testBasicReranking() {
        log.info("\n=== 測試 2: 基本 Re-ranking 功能（使用 rerank-2.5 模型）===");

        // 準備測試文檔
        String query = "如何使用 Spring AI 實現 RAG 系統？";
        List<Document> documents = List.of(
                Document.builder()
                        .text("Spring AI 提供了完整的 RAG 實現框架，包括向量存儲、文檔檢索和生成等功能。")
                        .build(),
                Document.builder()
                        .text("Python 是一種廣泛使用的編程語言，適合數據科學和機器學習。")
                        .build(),
                Document.builder()
                        .text("RAG（Retrieval-Augmented Generation）是一種結合檢索和生成的 AI 技術。")
                        .build(),
                Document.builder()
                        .text("Java 21 引入了許多新特性，包括虛擬線程和模式匹配。")
                        .build(),
                Document.builder()
                        .text("使用 Spring AI 可以輕鬆整合 OpenAI、Azure OpenAI 等多種 AI 服務。")
                        .build()
        );

        log.info("查詢: {}", query);
        log.info("文檔數量: {}", documents.size());
        log.info("請求 Top-K: 3");

        try {
            // 執行 Re-ranking
            List<RerankResult> results = provider.rerank(query, documents, 3);

            // 驗證結果
            assertNotNull(results, "Re-ranking 結果不應為 null");
            assertEquals(3, results.size(), "應該返回 3 個結果");

            // 驗證分數遞減
            for (int i = 0; i < results.size() - 1; i++) {
                assertTrue(
                        results.get(i).getRelevanceScore() >= results.get(i + 1).getRelevanceScore(),
                        String.format("分數應該遞減排序: results[%d]=%.4f >= results[%d]=%.4f",
                                i, results.get(i).getRelevanceScore(),
                                i + 1, results.get(i + 1).getRelevanceScore())
                );
            }

            // 輸出結果
            log.info("\n=== Re-ranking 結果（使用 {} 模型）===", TEST_MODEL);
            for (int i = 0; i < results.size(); i++) {
                RerankResult result = results.get(i);
                log.info("排名 {}: 分數 {:.4f} | 原始索引: {} | 內容: {}",
                        i + 1,
                        result.getRelevanceScore(),
                        result.getOriginalIndex(),
                        truncate(result.getContent(), 50)
                );
            }

            // 驗證最相關的文檔應該包含 "Spring AI" 或 "RAG"
            String topContent = results.get(0).getContent();
            assertTrue(
                    topContent.contains("Spring AI") || topContent.contains("RAG"),
                    "最相關的文檔應該包含 'Spring AI' 或 'RAG'"
            );

            log.info("\n✓ 基本 Re-ranking 測試通過（rerank-2.5 模型正常運作）");

        } catch (Exception e) {
            log.error("測試失敗", e);
            fail("Re-ranking 測試失敗: " + e.getMessage());
        }
    }

    /**
     * 測試相關性排序正確性
     */
    @Test
    void testRelevanceOrdering() {
        log.info("\n=== 測試 3: 相關性排序正確性 ===");

        String query = "Spring Boot 配置方法";
        List<Document> documents = List.of(
                Document.builder()
                        .text("貓是一種可愛的寵物，有各種不同的品種。")
                        .build(),
                Document.builder()
                        .text("Spring Boot 使用 application.yml 或 application.properties 進行配置。")
                        .build(),
                Document.builder()
                        .text("配置 Spring Boot 應用時，可以使用 @ConfigurationProperties 註解。")
                        .build()
        );

        log.info("查詢: {}", query);

        try {
            List<RerankResult> results = provider.rerank(query, documents, 3);

            // 最相關的文檔應該排在前面
            String topResult = results.get(0).getContent();
            log.info("排名第 1 的文檔: {}", truncate(topResult, 60));

            assertTrue(
                    topResult.contains("Spring Boot") || topResult.contains("配置"),
                    "最相關的文檔應該包含 'Spring Boot' 或 '配置'"
            );

            // 不相關的文檔應該分數較低
            String lastResult = results.get(results.size() - 1).getContent();
            log.info("排名最後的文檔: {}", truncate(lastResult, 60));

            if (lastResult.contains("貓")) {
                log.info("✓ 不相關文檔（關於貓）被正確排在最後");
            }

            log.info("✓ 相關性排序測試通過");

        } catch (Exception e) {
            log.error("測試失敗", e);
            fail("相關性排序測試失敗: " + e.getMessage());
        }
    }

    /**
     * 測試空文檔列表處理
     */
    @Test
    void testEmptyDocuments() {
        log.info("\n=== 測試 4: 空文檔列表處理 ===");

        String query = "測試查詢";
        List<Document> emptyDocs = List.of();

        List<RerankResult> results = provider.rerank(query, emptyDocs, 5);

        assertNotNull(results, "結果不應為 null");
        assertTrue(results.isEmpty(), "空文檔列表應返回空結果");

        log.info("✓ 空文檔列表處理測試通過");
    }

    /**
     * 測試中文支援
     */
    @Test
    void testChineseSupport() {
        log.info("\n=== 測試 5: 中文支援（rerank-2.5 模型）===");

        String query = "什麼是深度學習？";
        List<Document> documents = List.of(
                Document.builder()
                        .text("深度學習是機器學習的一個分支，使用多層神經網絡來學習數據的表示。")
                        .build(),
                Document.builder()
                        .text("今天天氣很好，適合出去散步。")
                        .build(),
                Document.builder()
                        .text("神經網絡是深度學習的核心組件，模擬人腦的工作方式。")
                        .build()
        );

        log.info("中文查詢: {}", query);

        try {
            List<RerankResult> results = provider.rerank(query, documents, 3);

            assertNotNull(results, "中文查詢應該正常工作");
            assertTrue(results.size() > 0, "應該返回結果");

            // 輸出結果
            log.info("\n中文查詢 Re-ranking 結果:");
            for (int i = 0; i < results.size(); i++) {
                RerankResult result = results.get(i);
                log.info("排名 {}: 分數 {:.4f} | {}", i + 1, result.getRelevanceScore(), truncate(result.getContent(), 50));
            }

            // 最相關的應該是關於深度學習的文檔
            String topResult = results.get(0).getContent();
            assertTrue(
                    topResult.contains("深度學習") || topResult.contains("神經網絡"),
                    "最相關的文檔應該包含 '深度學習' 或 '神經網絡'"
            );

            log.info("✓ 中文支援測試通過（rerank-2.5 對中文支援良好）");

        } catch (Exception e) {
            log.error("中文測試失敗", e);
            fail("中文支援測試失敗: " + e.getMessage());
        }
    }

    /**
     * 測試多語言混合
     */
    @Test
    void testMixedLanguages() {
        log.info("\n=== 測試 6: 多語言混合（中英文）===");

        String query = "Spring AI RAG implementation";
        List<Document> documents = List.of(
                Document.builder()
                        .text("Spring AI provides comprehensive RAG framework with vector stores and retrieval.")
                        .build(),
                Document.builder()
                        .text("Spring AI 提供了完整的 RAG 框架實現。")
                        .build(),
                Document.builder()
                        .text("The weather is nice today.")
                        .build()
        );

        log.info("英文查詢: {}", query);

        try {
            List<RerankResult> results = provider.rerank(query, documents, 3);

            assertNotNull(results, "混合語言應該正常工作");
            assertEquals(3, results.size(), "應該返回 3 個結果");

            log.info("\n多語言 Re-ranking 結果:");
            for (int i = 0; i < results.size(); i++) {
                RerankResult result = results.get(i);
                log.info("排名 {}: 分數 {:.4f} | {}", i + 1, result.getRelevanceScore(), truncate(result.getContent(), 60));
            }

            log.info("✓ 多語言混合測試通過");

        } catch (Exception e) {
            log.error("多語言測試失敗", e);
            fail("多語言測試失敗: " + e.getMessage());
        }
    }

    /**
     * 測試模型版本驗證
     */
    @Test
    void testModelVersion() {
        log.info("\n=== 測試 7: 驗證使用 rerank-2.5 模型 ===");

        String query = "測試模型版本";
        List<Document> documents = List.of(
                Document.builder().text("測試文檔 1").build(),
                Document.builder().text("測試文檔 2").build()
        );

        try {
            long startTime = System.currentTimeMillis();
            List<RerankResult> results = provider.rerank(query, documents, 2);
            long duration = System.currentTimeMillis() - startTime;

            assertNotNull(results, "rerank-2.5 模型應該正常工作");
            assertEquals(2, results.size(), "應該返回 2 個結果");

            log.info("✓ rerank-2.5 模型測試通過");
            log.info("  - 處理時間: {} ms", duration);
            log.info("  - 返回結果數: {}", results.size());

        } catch (Exception e) {
            log.error("模型版本測試失敗", e);
            fail("rerank-2.5 模型調用失敗: " + e.getMessage());
        }
    }

    /**
     * 截斷文本用於顯示
     */
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
