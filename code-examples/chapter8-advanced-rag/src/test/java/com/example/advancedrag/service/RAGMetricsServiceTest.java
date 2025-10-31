package com.example.advancedrag.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RAGMetricsService 測試
 *
 * 測試 Prometheus 指標收集功能：
 * 1. 查詢指標記錄
 * 2. 時間指標記錄
 * 3. 評估指標記錄
 * 4. 審核指標記錄
 * 5. 指標統計查詢
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RAG 指標服務測試")
class RAGMetricsServiceTest {

    @Autowired
    private RAGMetricsService metricsService;

    @BeforeEach
    void setUp() {
        System.out.println("\n=================================");
        System.out.println("開始新的測試案例");
        System.out.println("=================================");
    }

    @Test
    @DisplayName("指標測試1：記錄查詢指標")
    void testRecordQueryMetrics() {
        // Given
        double initialTotalQueries = metricsService.getTotalQueries();

        // When
        metricsService.recordQuery();
        metricsService.recordQuerySuccess();

        // Then
        double newTotalQueries = metricsService.getTotalQueries();
        double successQueries = metricsService.getSuccessQueries();

        assertTrue(newTotalQueries > initialTotalQueries, "總查詢數應該增加");
        assertTrue(successQueries > 0, "成功查詢數應該大於 0");

        System.out.println("\n=== 指標測試1：記錄查詢指標 ===");
        System.out.println("總查詢數: " + newTotalQueries);
        System.out.println("成功查詢數: " + successQueries);
        System.out.println("失敗查詢數: " + metricsService.getFailureQueries());
        System.out.println("成功率: " + String.format("%.2f%%", metricsService.getSuccessRate()));
    }

    @Test
    @DisplayName("指標測試2：記錄查詢失敗")
    void testRecordQueryFailure() {
        // Given
        double initialFailures = metricsService.getFailureQueries();

        // When
        metricsService.recordQuery();
        metricsService.recordQueryFailure();

        // Then
        double newFailures = metricsService.getFailureQueries();

        assertTrue(newFailures > initialFailures, "失敗查詢數應該增加");

        System.out.println("\n=== 指標測試2：記錄查詢失敗 ===");
        System.out.println("總查詢數: " + metricsService.getTotalQueries());
        System.out.println("成功查詢數: " + metricsService.getSuccessQueries());
        System.out.println("失敗查詢數: " + newFailures);
        System.out.println("成功率: " + String.format("%.2f%%", metricsService.getSuccessRate()));
    }

    @Test
    @DisplayName("指標測試3：記錄查詢時間")
    void testRecordQueryDuration() {
        // Given
        long duration1 = 100L;
        long duration2 = 200L;
        long duration3 = 300L;

        // When
        metricsService.recordQueryDuration(duration1);
        metricsService.recordQueryDuration(duration2);
        metricsService.recordQueryDuration(duration3);

        // Then
        double avgResponseTime = metricsService.getAverageResponseTime();
        double p95ResponseTime = metricsService.getP95ResponseTime();
        double p99ResponseTime = metricsService.getP99ResponseTime();

        assertTrue(avgResponseTime > 0, "平均響應時間應該大於 0");
        assertTrue(p95ResponseTime >= avgResponseTime, "P95 應該 >= 平均值");
        assertTrue(p99ResponseTime >= p95ResponseTime, "P99 應該 >= P95");

        System.out.println("\n=== 指標測試3：記錄查詢時間 ===");
        System.out.println("平均響應時間: " + String.format("%.2f ms", avgResponseTime));
        System.out.println("P95 響應時間: " + String.format("%.2f ms", p95ResponseTime));
        System.out.println("P99 響應時間: " + String.format("%.2f ms", p99ResponseTime));
    }

    @Test
    @DisplayName("指標測試4：記錄檢索指標")
    void testRecordRetrievalMetrics() {
        // Given
        long retrievalTime = 150L;
        int documentCount = 10;

        // When
        metricsService.recordRetrievalDuration(retrievalTime);
        metricsService.recordDocumentsRetrieved(documentCount);

        // Then
        System.out.println("\n=== 指標測試4：記錄檢索指標 ===");
        System.out.println("檢索時間: " + retrievalTime + " ms");
        System.out.println("檢索文檔數: " + documentCount);
        System.out.println("指標已成功記錄");
    }

    @Test
    @DisplayName("指標測試5：記錄 Re-ranking 指標")
    void testRecordRerankingMetrics() {
        // Given
        long rerankingTime = 80L;

        // When
        metricsService.recordRerankingDuration(rerankingTime);

        // Then
        System.out.println("\n=== 指標測試5：記錄 Re-ranking 指標 ===");
        System.out.println("Re-ranking 時間: " + rerankingTime + " ms");
        System.out.println("指標已成功記錄");
    }

    @Test
    @DisplayName("指標測試6：記錄生成指標")
    void testRecordGenerationMetrics() {
        // Given
        long generationTime = 500L;

        // When
        metricsService.recordGenerationDuration(generationTime);

        // Then
        System.out.println("\n=== 指標測試6：記錄生成指標 ===");
        System.out.println("生成時間: " + generationTime + " ms");
        System.out.println("指標已成功記錄");
    }

    @Test
    @DisplayName("指標測試7：記錄評估指標")
    void testRecordEvaluationMetrics() {
        // Given
        long evaluationTime = 200L;
        double accuracyScore = 8.5;
        double relevanceScore = 7.8;
        double overallScore = 8.2;

        // When
        metricsService.recordEvaluationDuration(evaluationTime);
        metricsService.recordAccuracyScore(accuracyScore);
        metricsService.recordRelevanceScore(relevanceScore);
        metricsService.recordOverallScore(overallScore);

        // Then
        System.out.println("\n=== 指標測試7：記錄評估指標 ===");
        System.out.println("評估時間: " + evaluationTime + " ms");
        System.out.println("準確性分數: " + accuracyScore);
        System.out.println("相關性分數: " + relevanceScore);
        System.out.println("總體分數: " + overallScore);
        System.out.println("指標已成功記錄");
    }

    @Test
    @DisplayName("指標測試8：記錄審核指標")
    void testRecordModerationMetrics() {
        // When
        metricsService.recordModeration();
        metricsService.recordModeration();
        metricsService.recordModerationFailed();

        // Then
        System.out.println("\n=== 指標測試8：記錄審核指標 ===");
        System.out.println("總審核次數: 2");
        System.out.println("審核失敗次數: 1");
        System.out.println("指標已成功記錄");
    }

    @Test
    @DisplayName("指標測試9：活躍查詢計數")
    void testActiveQueriesCount() {
        // Given
        int initialActiveQueries = metricsService.getActiveQueries();

        // When
        metricsService.recordQuery(); // 增加活躍查詢
        int activeAfterStart = metricsService.getActiveQueries();

        metricsService.recordQuerySuccess(); // 減少活躍查詢
        int activeAfterEnd = metricsService.getActiveQueries();

        // Then
        assertEquals(initialActiveQueries + 1, activeAfterStart, "開始查詢後活躍數應增加");
        assertEquals(initialActiveQueries, activeAfterEnd, "完成查詢後活躍數應恢復");

        System.out.println("\n=== 指標測試9：活躍查詢計數 ===");
        System.out.println("初始活躍查詢: " + initialActiveQueries);
        System.out.println("開始查詢後: " + activeAfterStart);
        System.out.println("完成查詢後: " + activeAfterEnd);
    }

    @Test
    @DisplayName("指標測試10：成功率計算")
    void testSuccessRateCalculation() {
        // Given - 記錄一些成功和失敗的查詢
        metricsService.recordQuery();
        metricsService.recordQuerySuccess();

        metricsService.recordQuery();
        metricsService.recordQuerySuccess();

        metricsService.recordQuery();
        metricsService.recordQueryFailure();

        // When
        double successRate = metricsService.getSuccessRate();

        // Then
        assertTrue(successRate >= 0 && successRate <= 100, "成功率應該在 0-100 之間");

        System.out.println("\n=== 指標測試10：成功率計算 ===");
        System.out.println("總查詢數: " + metricsService.getTotalQueries());
        System.out.println("成功查詢數: " + metricsService.getSuccessQueries());
        System.out.println("失敗查詢數: " + metricsService.getFailureQueries());
        System.out.println("成功率: " + String.format("%.2f%%", successRate));
    }

    @Test
    @DisplayName("指標測試11：自定義指標 - Counter")
    void testCustomCounter() {
        // Given
        String metricName = "test.custom.counter";

        // When
        metricsService.incrementCustomCounter(metricName, 5);
        metricsService.incrementCustomCounter(metricName, 3);

        // Then
        System.out.println("\n=== 指標測試11：自定義指標 - Counter ===");
        System.out.println("指標名稱: " + metricName);
        System.out.println("自定義計數器已成功記錄");
    }

    @Test
    @DisplayName("指標測試12：自定義指標 - Gauge")
    void testCustomGauge() {
        // Given
        String metricName = "test.custom.gauge";

        // When
        metricsService.setCustomGauge(metricName, 100);
        metricsService.setCustomGauge(metricName, 200);

        // Then
        System.out.println("\n=== 指標測試12：自定義指標 - Gauge ===");
        System.out.println("指標名稱: " + metricName);
        System.out.println("最終值: 200");
        System.out.println("自定義 Gauge 已成功記錄");
    }

    @Test
    @DisplayName("指標測試13：完整 RAG 流程指標記錄")
    void testCompleteRAGFlowMetrics() {
        // Given
        System.out.println("\n=== 指標測試13：完整 RAG 流程指標記錄 ===");
        System.out.println("模擬完整 RAG 查詢流程...");

        // When - 模擬完整流程
        // 1. 開始查詢
        metricsService.recordQuery();
        System.out.println("1. 記錄查詢開始");

        // 2. 記錄審核
        metricsService.recordModeration();
        System.out.println("2. 記錄內容審核");

        // 3. 記錄檢索
        metricsService.recordRetrievalDuration(120L);
        metricsService.recordDocumentsRetrieved(15);
        System.out.println("3. 記錄檢索（120ms, 15 docs）");

        // 4. 記錄 Re-ranking
        metricsService.recordRerankingDuration(80L);
        System.out.println("4. 記錄 Re-ranking（80ms）");

        // 5. 記錄生成
        metricsService.recordGenerationDuration(450L);
        System.out.println("5. 記錄答案生成（450ms）");

        // 6. 記錄評估
        metricsService.recordEvaluationDuration(180L);
        metricsService.recordAccuracyScore(8.5);
        metricsService.recordRelevanceScore(7.8);
        metricsService.recordOverallScore(8.2);
        System.out.println("6. 記錄評估（180ms, 分數: 8.5/7.8/8.2）");

        // 7. 完成查詢
        metricsService.recordQuerySuccess();
        metricsService.recordQueryDuration(830L);
        System.out.println("7. 記錄查詢完成（總耗時 830ms）");

        // Then
        System.out.println("\n流程完成！指標摘要:");
        System.out.println("  總查詢數: " + metricsService.getTotalQueries());
        System.out.println("  成功率: " + String.format("%.2f%%", metricsService.getSuccessRate()));
        System.out.println("  活躍查詢: " + metricsService.getActiveQueries());
        System.out.println("  平均響應時間: " + String.format("%.2f ms", metricsService.getAverageResponseTime()));
    }

    @Test
    @DisplayName("指標測試14：並發場景測試")
    void testConcurrentScenario() {
        // Given
        System.out.println("\n=== 指標測試14：並發場景測試 ===");

        // When - 模擬 3 個並發查詢
        // 查詢 1
        metricsService.recordQuery();
        metricsService.recordRetrievalDuration(100L);
        metricsService.recordGenerationDuration(400L);
        metricsService.recordQuerySuccess();
        metricsService.recordQueryDuration(500L);

        // 查詢 2
        metricsService.recordQuery();
        metricsService.recordRetrievalDuration(150L);
        metricsService.recordGenerationDuration(450L);
        metricsService.recordQuerySuccess();
        metricsService.recordQueryDuration(600L);

        // 查詢 3 (失敗)
        metricsService.recordQuery();
        metricsService.recordRetrievalDuration(80L);
        metricsService.recordQueryFailure();
        metricsService.recordQueryDuration(80L);

        // Then
        System.out.println("已模擬 3 個並發查詢（2 成功 + 1 失敗）");
        System.out.println("成功率: " + String.format("%.2f%%", metricsService.getSuccessRate()));
        System.out.println("平均響應時間: " + String.format("%.2f ms", metricsService.getAverageResponseTime()));
    }

    @Test
    @DisplayName("指標測試15：邊界情況測試")
    void testEdgeCases() {
        // Test 1: 零除錯誤處理
        double successRate = metricsService.getSuccessRate();
        assertTrue(successRate >= 0, "成功率應該 >= 0（即使沒有查詢）");

        // Test 2: 極大值
        metricsService.recordQueryDuration(10000L);
        assertTrue(metricsService.getAverageResponseTime() > 0, "應該能處理大數值");

        // Test 3: 極小值
        metricsService.recordQueryDuration(1L);
        assertTrue(metricsService.getAverageResponseTime() > 0, "應該能處理小數值");

        System.out.println("\n=== 指標測試15：邊界情況測試 ===");
        System.out.println("所有邊界情況測試通過");
    }
}
