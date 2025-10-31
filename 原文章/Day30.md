# 使用 Spring AI 打造企業 RAG 知識庫【30】- Vector Store 企業級配置與優化

## 期待Spring AI能找到自己的特色

![Spring AI Evolution](https://example.com/spring-ai-evolution.jpg)

經過前面29天的深入探討，我們已經了解了 Spring AI 的基本功能。今天來聊聊在企業環境中如何選擇和配置合適的 Vector Store，以及相關的最佳實踐。

## Vector Store 選擇指南

在 Spring AI 1.1-SNAPSHOT 中，支援多種向量資料庫，每種都有其特點：

### 1. Neo4j Vector Index

**優點：**
- 既支援圖數據庫又支援向量搜索
- 適合需要關係數據的複雜查詢
- 強大的 Cypher 查詢語言
- 可視化能力優秀

**缺點：**
- 記憶體消耗較大
- 學習曲線較陡峭
- 授權成本高（企業版）

**適用場景：**
- 需要知識圖譜的應用
- 複雜的關係查詢
- 需要可視化數據關係

```yaml
spring:
  ai:
    vectorstore:
      neo4j:
        uri: ${NEO4J_URI:bolt://localhost:7687}
        username: ${NEO4J_USERNAME:neo4j}
        password: ${NEO4J_PASSWORD:password}
        database-name: ${NEO4J_DATABASE:neo4j}
        # 企業級配置
        connection-pool-size: 50
        connection-timeout: 30s
        max-connection-lifetime: 1h
        # 向量索引配置
        embedding-dimension: 1536
        index-name: "vector_index"
        similarity-function: "cosine"
```

### 2. Pinecone

**優點：**
- 完全託管的雲服務
- 高效能和擴展性
- 簡單易用
- 優秀的延遲性能

**缺點：**
- 僅雲服務，無本地部署
- 成本隨規模增長較快
- 對中國用戶訪問可能有限制

**適用場景：**
- 快速原型開發
- 全球分佈式應用
- 不想維護基礎設施

```yaml
spring:
  ai:
    vectorstore:
      pinecone:
        api-key: ${PINECONE_API_KEY}
        environment: ${PINECONE_ENVIRONMENT:us-west1-gcp}
        project-name: ${PINECONE_PROJECT:default}
        index-name: ${PINECONE_INDEX:spring-ai-index}
        # 企業級配置
        namespace: ${PINECONE_NAMESPACE:production}
        top-k: 10
        include-metadata: true
```

### 3. Weaviate

**優點：**
- 開源且功能豐富
- 內建多模態支援
- 可本地部署或雲託管
- GraphQL API

**缺點：**
- 相對較新，生態系統較小
- 文檔有時不夠詳細
- 複雜配置

**適用場景：**
- 多模態應用（文本+圖片）
- 需要本地部署的企業
- 複雜的向量操作需求

```yaml
spring:
  ai:
    vectorstore:
      weaviate:
        scheme: ${WEAVIATE_SCHEME:http}
        host: ${WEAVIATE_HOST:localhost:8080}
        api-key: ${WEAVIATE_API_KEY:}
        # 企業級配置
        class-name: "Document"
        consistency-level: "ALL"
        object-properties:
          - "content"
          - "metadata"
        vector-index-config:
          distance: "cosine"
          ef-construction: 128
          max-connections: 64
```

### 4. Redis Vector Search

**優點：**
- 基於廣泛使用的 Redis
- 高效能內存操作
- 成熟的運維工具和生態
- 支援多種資料結構

**缺點：**
- 主要是內存存儲，成本較高
- 向量搜索功能相對較新
- 需要 Redis Stack

**適用場景：**
- 已有 Redis 基礎設施
- 需要極高性能的場景
- 混合數據類型應用

```yaml
spring:
  ai:
    vectorstore:
      redis:
        uri: ${REDIS_URI:redis://localhost:6379}
        index: ${REDIS_INDEX:spring-ai-index}
        prefix: ${REDIS_PREFIX:doc:}
        # 企業級配置
        connection-pool-config:
          max-total: 20
          max-idle: 10
          min-idle: 5
        # 向量索引配置
        vector-field: "embedding"
        distance-metric: "COSINE"
        initial-cap: 1000
        m: 16
        ef-construction: 200
```

## 企業級配置最佳實踐

### 1. 連接池與連接管理

```java
@Configuration
public class VectorStoreConfig {

    @Bean
    public Neo4jVectorStore neo4jVectorStore(
            @Qualifier("vectorStoreDriver") Driver driver,
            EmbeddingModel embeddingModel) {
        
        return Neo4jVectorStore.builder()
            .withDriver(driver)
            .withEmbeddingModel(embeddingModel)
            .withDatabaseName("neo4j")
            .withIndexName("spring_ai_document_index")
            .withNodeLabel("Document")
            .withEmbeddingProperty("embedding")
            .withIdProperty("id")
            .withConstraintName("spring_ai_document_id")
            .build();
    }

    @Bean("vectorStoreDriver")
    public Driver vectorStoreDriver() {
        Config config = Config.builder()
            .withMaxConnectionPoolSize(50)
            .withConnectionAcquisitionTimeout(30, TimeUnit.SECONDS)
            .withConnectionTimeout(15, TimeUnit.SECONDS)
            .withMaxConnectionLifetime(1, TimeUnit.HOURS)
            .withConnectionLivenessCheckTimeout(5, TimeUnit.SECONDS)
            .withEncryption()
            .build();

        return GraphDatabase.driver(
            "bolt://localhost:7687",
            AuthTokens.basic("neo4j", "password"),
            config
        );
    }
}
```

### 2. 監控和健康檢查

```java
@Component
public class VectorStoreHealthIndicator implements HealthIndicator {
    
    private final VectorStore vectorStore;
    private final MeterRegistry meterRegistry;
    
    public VectorStoreHealthIndicator(VectorStore vectorStore, MeterRegistry meterRegistry) {
        this.vectorStore = vectorStore;
        this.meterRegistry = meterRegistry;
    }
    
    @Override
    public Health health() {
        try {
            // 執行簡單的健康檢查查詢
            long startTime = System.currentTimeMillis();
            
            List<Document> testResults = vectorStore.similaritySearch(
                SearchRequest.query("health check").withTopK(1)
            );
            
            long duration = System.currentTimeMillis() - startTime;
            
            // 記錄指標
            meterRegistry.timer("vectorstore.health.check.duration")
                .record(duration, TimeUnit.MILLISECONDS);
            
            return Health.up()
                .withDetail("responseTime", duration + "ms")
                .withDetail("documentsFound", testResults.size())
                .withDetail("status", "Connected")
                .build();
                
        } catch (Exception e) {
            meterRegistry.counter("vectorstore.health.check.failures").increment();
            
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("status", "Disconnected")
                .build();
        }
    }
}
```

### 3. 批次操作優化

```java
@Service
@RequiredArgsConstructor
public class BatchVectorStoreService {
    
    private final VectorStore vectorStore;
    private final MeterRegistry meterRegistry;
    
    @Value("${app.vectorstore.batch-size:100}")
    private int batchSize;
    
    @Async("vectorStoreTaskExecutor")
    public CompletableFuture<Void> batchWrite(List<Document> documents) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // 分批處理
            List<List<Document>> batches = Lists.partition(documents, batchSize);
            
            for (List<Document> batch : batches) {
                vectorStore.write(batch);
                
                // 記錄批次指標
                meterRegistry.counter("vectorstore.batch.processed.documents")
                    .increment(batch.size());
                
                // 添加短暫延遲避免過載
                Thread.sleep(10);
            }
            
            meterRegistry.counter("vectorstore.batch.success").increment();
            
        } catch (Exception e) {
            meterRegistry.counter("vectorstore.batch.errors").increment();
            log.error("Batch write failed", e);
            throw new RuntimeException("Batch write failed", e);
            
        } finally {
            sample.stop(Timer.builder("vectorstore.batch.duration")
                .description("Vector store batch operation duration")
                .register(meterRegistry));
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @EventListener
    @Async
    public void handleDocumentUpdate(DocumentUpdateEvent event) {
        try {
            // 更新單個文檔
            vectorStore.write(List.of(event.getDocument()));
            meterRegistry.counter("vectorstore.document.updated").increment();
            
        } catch (Exception e) {
            meterRegistry.counter("vectorstore.document.update.errors").increment();
            log.error("Failed to update document: {}", event.getDocument().getId(), e);
        }
    }
}
```

### 4. 快取層設計

```java
@Configuration
@EnableCaching
public class VectorStoreCacheConfig {
    
    @Bean
    public CacheManager vectorStoreCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .recordStats());
        return cacheManager;
    }
}

@Service
public class CachedVectorStoreService {
    
    private final VectorStore vectorStore;
    
    @Cacheable(value = "vectorSearchResults", key = "#query + '_' + #topK")
    public List<Document> similaritySearch(String query, int topK) {
        return vectorStore.similaritySearch(
            SearchRequest.query(query).withTopK(topK)
        );
    }
    
    @CacheEvict(value = "vectorSearchResults", allEntries = true)
    public void write(List<Document> documents) {
        vectorStore.write(documents);
    }
}
```

### 5. 企業級安全配置

```java
@Configuration
public class VectorStoreSecurityConfig {
    
    @Bean
    public VectorStoreAccessControl vectorStoreAccessControl() {
        return new VectorStoreAccessControl() {
            @Override
            public boolean canRead(String userId, Document document) {
                // 實現基於用戶的讀取權限控制
                String documentOwner = (String) document.getMetadata().get("owner");
                String userRole = getCurrentUserRole(userId);
                
                return userId.equals(documentOwner) || 
                       "ADMIN".equals(userRole) ||
                       hasReadPermission(userId, documentOwner);
            }
            
            @Override
            public boolean canWrite(String userId, Document document) {
                // 實現基於用戶的寫入權限控制
                String userRole = getCurrentUserRole(userId);
                return "ADMIN".equals(userRole) || "EDITOR".equals(userRole);
            }
        };
    }
    
    @Bean
    public SecureVectorStore secureVectorStore(
            VectorStore vectorStore,
            VectorStoreAccessControl accessControl) {
        return new SecureVectorStore(vectorStore, accessControl);
    }
}

@Component
public class SecureVectorStore implements VectorStore {
    
    private final VectorStore delegate;
    private final VectorStoreAccessControl accessControl;
    
    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        String currentUserId = getCurrentUserId();
        
        List<Document> results = delegate.similaritySearch(request);
        
        // 過濾用戶無權訪問的文檔
        return results.stream()
            .filter(doc -> accessControl.canRead(currentUserId, doc))
            .collect(Collectors.toList());
    }
    
    @Override
    public void write(List<Document> documents) {
        String currentUserId = getCurrentUserId();
        
        // 檢查寫入權限
        List<Document> authorizedDocs = documents.stream()
            .filter(doc -> accessControl.canWrite(currentUserId, doc))
            .collect(Collectors.toList());
            
        if (authorizedDocs.size() != documents.size()) {
            throw new SecurityException("Insufficient permissions to write some documents");
        }
        
        delegate.write(authorizedDocs);
    }
}
```

## 多租戶支援

```java
@Service
public class MultiTenantVectorStoreService {
    
    private final Map<String, VectorStore> tenantVectorStores;
    
    public MultiTenantVectorStoreService(List<VectorStore> vectorStores) {
        // 根據配置初始化多個租戶的向量存儲
        this.tenantVectorStores = new ConcurrentHashMap<>();
    }
    
    public VectorStore getVectorStore(String tenantId) {
        return tenantVectorStores.computeIfAbsent(tenantId, this::createTenantVectorStore);
    }
    
    private VectorStore createTenantVectorStore(String tenantId) {
        // 為特定租戶創建獨立的向量存儲實例
        return Neo4jVectorStore.builder()
            .withDatabaseName("tenant_" + tenantId)
            .withIndexName("vector_index_" + tenantId)
            .build();
    }
    
    public List<Document> search(String tenantId, String query, int topK) {
        VectorStore vectorStore = getVectorStore(tenantId);
        return vectorStore.similaritySearch(
            SearchRequest.query(query).withTopK(topK)
        );
    }
}
```

## 災難恢復和備份

```java
@Service
@Scheduled
public class VectorStoreBackupService {
    
    private final VectorStore vectorStore;
    private final CloudStorageService cloudStorage;
    
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2點執行
    public void performBackup() {
        try {
            log.info("Starting vector store backup...");
            
            // 1. 導出所有文檔
            List<Document> allDocuments = exportAllDocuments();
            
            // 2. 序列化到文件
            String backupData = serializeDocuments(allDocuments);
            
            // 3. 上傳到雲存儲
            String backupFileName = "vectorstore_backup_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
            
            cloudStorage.upload(backupFileName, backupData);
            
            // 4. 清理舊備份（保留最近30天）
            cleanupOldBackups();
            
            log.info("Vector store backup completed successfully");
            
        } catch (Exception e) {
            log.error("Vector store backup failed", e);
            // 發送告警通知
            sendAlertNotification("Vector store backup failed: " + e.getMessage());
        }
    }
    
    public void restoreFromBackup(String backupFileName) {
        try {
            log.info("Starting vector store restore from: {}", backupFileName);
            
            // 1. 從雲存儲下載備份
            String backupData = cloudStorage.download(backupFileName);
            
            // 2. 反序列化文檔
            List<Document> documents = deserializeDocuments(backupData);
            
            // 3. 清空現有數據（謹慎操作）
            // clearAllDocuments(); // 實際環境中需要確認
            
            // 4. 導入備份數據
            vectorStore.write(documents);
            
            log.info("Vector store restore completed successfully");
            
        } catch (Exception e) {
            log.error("Vector store restore failed", e);
            throw new RuntimeException("Restore failed", e);
        }
    }
}
```

## 性能調優建議

### 1. 索引優化

```yaml
# Neo4j 向量索引配置
spring:
  ai:
    vectorstore:
      neo4j:
        # 索引配置
        vector-index:
          dimensions: 1536
          similarity-function: "cosine"
          # 調整這些參數以平衡搜索速度和準確性
          provider-config:
            "vector.dimensions": 1536
            "vector.similarity_function": "cosine"
            # HNSW 算法參數
            "vector.hnsw.ef_construction": 200  # 構建時的搜索深度
            "vector.hnsw.m": 16                 # 每個節點的最大連接數
```

### 2. 記憶體管理

```java
@Configuration
public class VectorStoreMemoryConfig {
    
    @Bean
    @ConditionalOnProperty(name = "app.vectorstore.memory-optimization.enabled", havingValue = "true")
    public MemoryOptimizedVectorStore memoryOptimizedVectorStore(VectorStore delegate) {
        return new MemoryOptimizedVectorStore(delegate);
    }
}

public class MemoryOptimizedVectorStore implements VectorStore {
    
    private final VectorStore delegate;
    private final LRUCache<String, List<Document>> resultCache;
    
    public MemoryOptimizedVectorStore(VectorStore delegate) {
        this.delegate = delegate;
        this.resultCache = new LRUCache<>(1000); // 緩存最近1000次查詢結果
    }
    
    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        String cacheKey = generateCacheKey(request);
        
        List<Document> cached = resultCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        List<Document> results = delegate.similaritySearch(request);
        resultCache.put(cacheKey, results);
        
        return results;
    }
}
```

## 監控 Dashboard

```java
@RestController
@RequestMapping("/ai/vectorstore/metrics")
public class VectorStoreMetricsController {
    
    private final MeterRegistry meterRegistry;
    private final VectorStore vectorStore;
    
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 基本統計
        stats.put("totalDocuments", getTotalDocuments());
        stats.put("averageEmbeddingDimension", getAverageEmbeddingDimension());
        
        // 性能指標
        stats.put("averageSearchTime", getAverageSearchTime());
        stats.put("searchesPerMinute", getSearchesPerMinute());
        stats.put("cacheHitRate", getCacheHitRate());
        
        // 健康狀態
        stats.put("healthStatus", getHealthStatus());
        stats.put("lastHealthCheck", getLastHealthCheck());
        
        return stats;
    }
    
    @GetMapping("/performance")
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> performance = new HashMap<>();
        
        // 查詢性能
        Timer searchTimer = meterRegistry.find("vectorstore.search.duration").timer();
        if (searchTimer != null) {
            performance.put("searchDuration", Map.of(
                "mean", searchTimer.mean(TimeUnit.MILLISECONDS),
                "max", searchTimer.max(TimeUnit.MILLISECONDS),
                "count", searchTimer.count()
            ));
        }
        
        // 寫入性能
        Timer writeTimer = meterRegistry.find("vectorstore.write.duration").timer();
        if (writeTimer != null) {
            performance.put("writeDuration", Map.of(
                "mean", writeTimer.mean(TimeUnit.MILLISECONDS),
                "max", writeTimer.max(TimeUnit.MILLISECONDS),
                "count", writeTimer.count()
            ));
        }
        
        return performance;
    }
}
```

## 回顧

今天我們深入了解了：

- 不同 Vector Store 的特點和適用場景
- 企業級配置的最佳實踐
- 連接池、監控、健康檢查的實現
- 批次操作和性能優化
- 安全控制和多租戶支援
- 災難恢復和備份策略
- 監控和指標收集

選擇合適的 Vector Store 對企業 RAG 系統的成功至關重要。需要綜合考慮性能、成本、維護複雜度、安全性等多個因素。

## Source Code

程式碼下載: [https://github.com/kevintsai1202/SpringBoot-AI-Day30-Updated.git](https://github.com/kevintsai1202/SpringBoot-AI-Day30-Updated.git)