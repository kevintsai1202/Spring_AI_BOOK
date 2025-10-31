package com.example.advancedrag.service;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RAG 指標收集服務
 *
 * 使用 Micrometer 收集 Prometheus 指標：
 * - RAG 查詢計數和成功率
 * - 響應時間分布
 * - 檢索質量指標
 * - Re-ranking 性能
 * - 內容審核統計
 * - 評估分數統計
 */
@Slf4j
@Service
public class RAGMetricsService {

    private final MeterRegistry meterRegistry;

    // 計數器
    private final Counter ragQueryCounter;
    private final Counter ragQuerySuccessCounter;
    private final Counter ragQueryFailureCounter;
    private final Counter moderationCounter;
    private final Counter moderationFailedCounter;

    // 計時器
    private final Timer ragQueryTimer;
    private final Timer retrievalTimer;
    private final Timer rerankingTimer;
    private final Timer generationTimer;
    private final Timer evaluationTimer;

    // 儀表（Gauge）
    private final AtomicInteger activeQueries;
    private final AtomicLong totalDocumentsRetrieved;
    private final AtomicInteger averageDocumentsPerQuery;

    // 分布摘要
    private final DistributionSummary accuracyScoreDistribution;
    private final DistributionSummary relevanceScoreDistribution;
    private final DistributionSummary overallScoreDistribution;

    // 緩存統計
    private final ConcurrentHashMap<String, AtomicLong> customMetrics;

    public RAGMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.customMetrics = new ConcurrentHashMap<>();

        // 初始化計數器
        this.ragQueryCounter = Counter.builder("rag.query.total")
                .description("RAG 查詢總數")
                .register(meterRegistry);

        this.ragQuerySuccessCounter = Counter.builder("rag.query.success")
                .description("RAG 查詢成功數")
                .register(meterRegistry);

        this.ragQueryFailureCounter = Counter.builder("rag.query.failure")
                .description("RAG 查詢失敗數")
                .register(meterRegistry);

        this.moderationCounter = Counter.builder("rag.moderation.total")
                .description("內容審核總數")
                .register(meterRegistry);

        this.moderationFailedCounter = Counter.builder("rag.moderation.failed")
                .description("內容審核未通過數")
                .register(meterRegistry);

        // 初始化計時器
        this.ragQueryTimer = Timer.builder("rag.query.duration")
                .description("RAG 查詢響應時間")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.retrievalTimer = Timer.builder("rag.retrieval.duration")
                .description("文檔檢索時間")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.rerankingTimer = Timer.builder("rag.reranking.duration")
                .description("Re-ranking 時間")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.generationTimer = Timer.builder("rag.generation.duration")
                .description("答案生成時間")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.evaluationTimer = Timer.builder("rag.evaluation.duration")
                .description("評估時間")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // 初始化 Gauge
        this.activeQueries = new AtomicInteger(0);
        Gauge.builder("rag.query.active", activeQueries, AtomicInteger::get)
                .description("當前活躍查詢數")
                .register(meterRegistry);

        this.totalDocumentsRetrieved = new AtomicLong(0);
        Gauge.builder("rag.documents.retrieved.total", totalDocumentsRetrieved, AtomicLong::get)
                .description("檢索文檔總數")
                .register(meterRegistry);

        this.averageDocumentsPerQuery = new AtomicInteger(0);
        Gauge.builder("rag.documents.per.query.avg", averageDocumentsPerQuery, AtomicInteger::get)
                .description("平均每次查詢檢索文檔數")
                .register(meterRegistry);

        // 初始化分布摘要
        this.accuracyScoreDistribution = DistributionSummary.builder("rag.evaluation.accuracy")
                .description("準確性評分分布")
                .scale(10.0)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.relevanceScoreDistribution = DistributionSummary.builder("rag.evaluation.relevance")
                .description("相關性評分分布")
                .scale(10.0)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.overallScoreDistribution = DistributionSummary.builder("rag.evaluation.overall")
                .description("總體評分分布")
                .scale(10.0)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        log.info("RAG 指標服務初始化完成");
    }

    // ========== RAG 查詢指標 ==========

    /**
     * 記錄 RAG 查詢
     */
    public void recordQuery() {
        ragQueryCounter.increment();
        activeQueries.incrementAndGet();
    }

    /**
     * 記錄 RAG 查詢成功
     */
    public void recordQuerySuccess() {
        ragQuerySuccessCounter.increment();
        activeQueries.decrementAndGet();
    }

    /**
     * 記錄 RAG 查詢失敗
     */
    public void recordQueryFailure() {
        ragQueryFailureCounter.increment();
        activeQueries.decrementAndGet();
    }

    /**
     * 記錄 RAG 查詢時間
     *
     * @param durationMs 持續時間（毫秒）
     */
    public void recordQueryDuration(long durationMs) {
        ragQueryTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    // ========== 檢索指標 ==========

    /**
     * 記錄檢索時間
     *
     * @param durationMs 持續時間（毫秒）
     */
    public void recordRetrievalDuration(long durationMs) {
        retrievalTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 記錄檢索文檔數量
     *
     * @param count 文檔數量
     */
    public void recordDocumentsRetrieved(int count) {
        totalDocumentsRetrieved.addAndGet(count);

        // 更新平均值
        long totalQueries = (long) ragQueryCounter.count();
        if (totalQueries > 0) {
            averageDocumentsPerQuery.set((int) (totalDocumentsRetrieved.get() / totalQueries));
        }
    }

    // ========== Re-ranking 指標 ==========

    /**
     * 記錄 Re-ranking 時間
     *
     * @param durationMs 持續時間（毫秒）
     */
    public void recordRerankingDuration(long durationMs) {
        rerankingTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    // ========== 生成指標 ==========

    /**
     * 記錄答案生成時間
     *
     * @param durationMs 持續時間（毫秒）
     */
    public void recordGenerationDuration(long durationMs) {
        generationTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    // ========== 內容審核指標 ==========

    /**
     * 記錄內容審核
     */
    public void recordModeration() {
        moderationCounter.increment();
    }

    /**
     * 記錄內容審核失敗
     */
    public void recordModerationFailed() {
        moderationFailedCounter.increment();
    }

    // ========== 評估指標 ==========

    /**
     * 記錄評估時間
     *
     * @param durationMs 持續時間（毫秒）
     */
    public void recordEvaluationDuration(long durationMs) {
        evaluationTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 記錄準確性評分
     *
     * @param score 評分（0-10）
     */
    public void recordAccuracyScore(double score) {
        accuracyScoreDistribution.record(score);
    }

    /**
     * 記錄相關性評分
     *
     * @param score 評分（0-10）
     */
    public void recordRelevanceScore(double score) {
        relevanceScoreDistribution.record(score);
    }

    /**
     * 記錄總體評分
     *
     * @param score 評分（0-10）
     */
    public void recordOverallScore(double score) {
        overallScoreDistribution.record(score);
    }

    // ========== 自定義指標 ==========

    /**
     * 增加自定義計數器
     *
     * @param metricName 指標名稱
     * @param increment  增量
     */
    public void incrementCustomCounter(String metricName, long increment) {
        customMetrics.computeIfAbsent(metricName, k -> {
            AtomicLong counter = new AtomicLong(0);
            Gauge.builder("rag.custom." + metricName, counter, AtomicLong::get)
                    .description("自定義指標: " + metricName)
                    .register(meterRegistry);
            return counter;
        }).addAndGet(increment);
    }

    /**
     * 設置自定義 Gauge 值
     *
     * @param metricName 指標名稱
     * @param value      值
     */
    public void setCustomGauge(String metricName, long value) {
        customMetrics.computeIfAbsent(metricName, k -> {
            AtomicLong gauge = new AtomicLong(0);
            Gauge.builder("rag.custom." + metricName, gauge, AtomicLong::get)
                    .description("自定義指標: " + metricName)
                    .register(meterRegistry);
            return gauge;
        }).set(value);
    }

    // ========== 統計查詢 ==========

    /**
     * 獲取 RAG 查詢總數
     */
    public double getTotalQueries() {
        return ragQueryCounter.count();
    }

    /**
     * 獲取 RAG 查詢成功數
     */
    public double getSuccessQueries() {
        return ragQuerySuccessCounter.count();
    }

    /**
     * 獲取 RAG 查詢失敗數
     */
    public double getFailureQueries() {
        return ragQueryFailureCounter.count();
    }

    /**
     * 獲取成功率
     */
    public double getSuccessRate() {
        double total = getTotalQueries();
        if (total == 0) {
            return 0.0;
        }
        return (getSuccessQueries() / total) * 100;
    }

    /**
     * 獲取當前活躍查詢數
     */
    public int getActiveQueries() {
        return activeQueries.get();
    }

    /**
     * 獲取平均響應時間（毫秒）
     */
    public double getAverageResponseTime() {
        return ragQueryTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 獲取 P95 響應時間（毫秒）
     */
    public double getP95ResponseTime() {
        return ragQueryTimer.percentile(0.95, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 獲取 P99 響應時間（毫秒）
     */
    public double getP99ResponseTime() {
        return ragQueryTimer.percentile(0.99, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
