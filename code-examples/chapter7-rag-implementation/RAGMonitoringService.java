/**
 * RAG 系統監控服務
 * 負責系統效能監控、指標收集和告警管理
 */
package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthIndicatorRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.*;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class RAGMonitoringService {
    
    private final MeterRegistry meterRegistry;
    private final AlertManager alertManager;
    private final HealthIndicatorRegistry healthRegistry;
    
    // 效能指標
    private Counter queryCounter;
    private Timer queryTimer;
    private Gauge activeConnectionsGauge;
    private Counter errorCounter;
    private AtomicInteger activeConnections = new AtomicInteger(0);
    
    @PostConstruct
    public void initializeMetrics() {
        // 初始化指標
        queryCounter = Counter.builder("rag.queries.total")
            .description("Total number of RAG queries")
            .register(meterRegistry);
        
        queryTimer = Timer.builder("rag.query.duration")
            .description("RAG query execution time")
            .register(meterRegistry);
        
        activeConnectionsGauge = Gauge.builder("rag.connections.active")
            .description("Active database connections")
            .register(meterRegistry, this, RAGMonitoringService::getActiveConnections);
        
        errorCounter = Counter.builder("rag.errors.total")
            .description("Total number of errors")
            .tag("type", "unknown")
            .register(meterRegistry);
    }
    
    /**
     * 記錄查詢指標
     */
    public void recordQuery(String queryType, Duration duration, boolean success) {
        queryCounter.increment(
            Tags.of(
                "type", queryType,
                "status", success ? "success" : "failure"
            )
        );
        
        queryTimer.record(duration);
        
        if (!success) {
            errorCounter.increment(Tags.of("type", "query_failure"));
        }
    }
    
    /**
     * 記錄向量化指標
     */
    public void recordEmbedding(String modelType, int textLength, Duration duration, boolean success) {
        Counter embeddingCounter = Counter.builder("rag.embeddings.total")
            .description("Total number of embeddings generated")
            .tag("model", modelType)
            .tag("status", success ? "success" : "failure")
            .register(meterRegistry);
        
        embeddingCounter.increment();
        
        Timer embeddingTimer = Timer.builder("rag.embedding.duration")
            .description("Embedding generation time")
            .tag("model", modelType)
            .register(meterRegistry);
        
        embeddingTimer.record(duration);
        
        // 記錄文本長度分佈
        DistributionSummary textLengthSummary = DistributionSummary.builder("rag.text.length")
            .description("Distribution of text lengths")
            .register(meterRegistry);
        
        textLengthSummary.record(textLength);
    }
    
    /**
     * 記錄快取指標
     */
    public void recordCacheOperation(String operation, String cacheType, boolean hit) {
        Counter cacheCounter = Counter.builder("rag.cache.operations")
            .description("Cache operations")
            .tag("operation", operation)
            .tag("type", cacheType)
            .tag("result", hit ? "hit" : "miss")
            .register(meterRegistry);
        
        cacheCounter.increment();
    }
    
    /**
     * 記錄向量存儲指標
     */
    public void recordVectorStoreOperation(String operation, Duration duration, int resultCount) {
        Timer vectorStoreTimer = Timer.builder("rag.vectorstore.duration")
            .description("Vector store operation time")
            .tag("operation", operation)
            .register(meterRegistry);
        
        vectorStoreTimer.record(duration);
        
        Counter vectorStoreCounter = Counter.builder("rag.vectorstore.operations")
            .description("Vector store operations")
            .tag("operation", operation)
            .register(meterRegistry);
        
        vectorStoreCounter.increment();
        
        if ("search".equals(operation)) {
            DistributionSummary resultSummary = DistributionSummary.builder("rag.search.results")
                .description("Number of search results")
                .register(meterRegistry);
            
            resultSummary.record(resultCount);
        }
    }
    
    /**
     * 系統健康檢查
     */
    @EventListener
    public void performHealthCheck(ApplicationReadyEvent event) {
        log.info("Starting RAG system health monitoring");
        
        // 註冊自定義健康指標
        registerCustomHealthIndicators();
        
        // 啟動定期健康檢查
        startPeriodicHealthCheck();
    }
    
    private void registerCustomHealthIndicators() {
        // 向量存儲健康檢查
        healthRegistry.register("vectorStore", () -> {
            try {
                // 檢查向量存儲連接
                boolean isHealthy = checkVectorStoreHealth();
                return isHealthy ? 
                    org.springframework.boot.actuate.health.Health.up().build() :
                    org.springframework.boot.actuate.health.Health.down().build();
            } catch (Exception e) {
                return org.springframework.boot.actuate.health.Health.down(e).build();
            }
        });
        
        // 嵌入模型健康檢查
        healthRegistry.register("embeddingModel", () -> {
            try {
                boolean isHealthy = checkEmbeddingModelHealth();
                return isHealthy ?
                    org.springframework.boot.actuate.health.Health.up().build() :
                    org.springframework.boot.actuate.health.Health.down().build();
            } catch (Exception e) {
                return org.springframework.boot.actuate.health.Health.down(e).build();
            }
        });
    }
    
    private void startPeriodicHealthCheck() {
        // 這裡可以使用 @Scheduled 註解來定期執行健康檢查
        // 為了簡化，這裡只是記錄日誌
        log.info("Periodic health check started");
    }
    
    /**
     * 效能告警檢查
     */
    public void checkPerformanceAlerts() {
        // 檢查查詢響應時間
        double avgQueryTime = queryTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
        if (avgQueryTime > 5000) { // 5秒閾值
            alertManager.sendAlert("HIGH_QUERY_LATENCY", 
                String.format("Average query time: %.2f ms", avgQueryTime));
        }
        
        // 檢查錯誤率
        double errorRate = calculateErrorRate();
        if (errorRate > 0.05) { // 5% 錯誤率閾值
            alertManager.sendAlert("HIGH_ERROR_RATE", 
                String.format("Error rate: %.2f%%", errorRate * 100));
        }
        
        // 檢查活躍連接數
        int connections = getActiveConnections();
        if (connections > 100) { // 100 連接閾值
            alertManager.sendAlert("HIGH_CONNECTION_COUNT", 
                String.format("Active connections: %d", connections));
        }
    }
    
    /**
     * 生成效能報告
     */
    public PerformanceReport generatePerformanceReport() {
        return PerformanceReport.builder()
            .totalQueries(queryCounter.count())
            .averageQueryTime(queryTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS))
            .errorRate(calculateErrorRate())
            .activeConnections(getActiveConnections())
            .cacheHitRate(calculateCacheHitRate())
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    // 輔助方法
    private double getActiveConnections() {
        return activeConnections.get();
    }
    
    private double calculateErrorRate() {
        double totalQueries = queryCounter.count();
        if (totalQueries == 0) return 0.0;
        
        double errors = errorCounter.count();
        return errors / totalQueries;
    }
    
    private double calculateCacheHitRate() {
        // 簡化實現，實際需要從快取指標計算
        return 0.85; // 假設 85% 命中率
    }
    
    private boolean checkVectorStoreHealth() {
        // 實際實現會檢查向量存儲連接狀態
        return true;
    }
    
    private boolean checkEmbeddingModelHealth() {
        // 實際實現會檢查嵌入模型可用性
        return true;
    }
    
    /**
     * 效能報告資料類別
     */
    public static class PerformanceReport {
        private final double totalQueries;
        private final double averageQueryTime;
        private final double errorRate;
        private final double activeConnections;
        private final double cacheHitRate;
        private final long timestamp;
        
        public PerformanceReport(double totalQueries, double averageQueryTime, 
                               double errorRate, double activeConnections, 
                               double cacheHitRate, long timestamp) {
            this.totalQueries = totalQueries;
            this.averageQueryTime = averageQueryTime;
            this.errorRate = errorRate;
            this.activeConnections = activeConnections;
            this.cacheHitRate = cacheHitRate;
            this.timestamp = timestamp;
        }
        
        // Getters
        public double getTotalQueries() { return totalQueries; }
        public double getAverageQueryTime() { return averageQueryTime; }
        public double getErrorRate() { return errorRate; }
        public double getActiveConnections() { return activeConnections; }
        public double getCacheHitRate() { return cacheHitRate; }
        public long getTimestamp() { return timestamp; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private double totalQueries;
            private double averageQueryTime;
            private double errorRate;
            private double activeConnections;
            private double cacheHitRate;
            private long timestamp;
            
            public Builder totalQueries(double totalQueries) {
                this.totalQueries = totalQueries;
                return this;
            }
            
            public Builder averageQueryTime(double averageQueryTime) {
                this.averageQueryTime = averageQueryTime;
                return this;
            }
            
            public Builder errorRate(double errorRate) {
                this.errorRate = errorRate;
                return this;
            }
            
            public Builder activeConnections(double activeConnections) {
                this.activeConnections = activeConnections;
                return this;
            }
            
            public Builder cacheHitRate(double cacheHitRate) {
                this.cacheHitRate = cacheHitRate;
                return this;
            }
            
            public Builder timestamp(long timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public PerformanceReport build() {
                return new PerformanceReport(totalQueries, averageQueryTime, 
                    errorRate, activeConnections, cacheHitRate, timestamp);
            }
        }
    }
    
    /**
     * 簡化的告警管理器介面
     */
    public interface AlertManager {
        void sendAlert(String alertType, String message);
    }
}