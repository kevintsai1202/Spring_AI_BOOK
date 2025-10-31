# 使用 Spring AI 打造企業 RAG 知識庫【21】- Spring AI 1.1 向量資料庫全攻略

## 鯨魚是開發測試的好朋友

Spring AI 1.1 大幅擴展了向量資料庫的支援，從原本的幾種選擇增加到超過20種不同的向量儲存解決方案。讓我們來看看如何在企業環境中選擇和部署合適的向量資料庫。

## Spring AI 1.1 支援的向量資料庫

### 主流向量資料庫支援列表

```yaml
# 可支援的向量資料庫
spring:
  ai:
    vectorstore:
      # 專用向量資料庫
      pinecone:
        api-key: ${PINECONE_API_KEY}
        environment: us-east1-gcp
      
      qdrant:
        url: http://localhost:6333
        api-key: ${QDRANT_API_KEY}
      
      weaviate:
        url: http://localhost:8080
        api-key: ${WEAVIATE_API_KEY}
      
      chroma:
        url: http://localhost:8000
      
      milvus:
        host: localhost
        port: 19530
      
      # 傳統資料庫 + 向量擴展
      pgvector:
        url: jdbc:postgresql://localhost:5432/vectordb
        username: postgres
        password: password
        
      redis:
        url: redis://localhost:6379
        
      elasticsearch:
        url: http://localhost:9200
        
      opensearch:
        url: http://localhost:9200
        
      # 圖形資料庫
      neo4j:
        uri: bolt://localhost:7687
        username: neo4j
        password: password
        
      # 雲端解決方案
      azure-cosmos:
        endpoint: ${AZURE_COSMOS_ENDPOINT}
        key: ${AZURE_COSMOS_KEY}
        
      # NoSQL 向量支援
      mongodb:
        connection-string: mongodb://localhost:27017
        database: vectordb
        
      cassandra:
        keyspace: vector_keyspace
        contact-points: localhost:9042
```

## Docker Compose 多向量資料庫環境

```yaml
# docker-compose.yml - 企業級向量資料庫測試環境
version: '3.8'

services:
  # PostgreSQL + pgvector (推薦用於生產環境)
  postgres-vector:
    image: pgvector/pgvector:pg16
    container_name: postgres-vector
    environment:
      POSTGRES_DB: vectordb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    networks:
      - ai-network

  # Qdrant (高性能向量搜索)
  qdrant:
    image: qdrant/qdrant:latest
    container_name: qdrant
    ports:
      - "6333:6333"
      - "6334:6334"
    volumes:
      - qdrant_data:/qdrant/storage
    networks:
      - ai-network

  # Weaviate (語義搜索專家)
  weaviate:
    image: semitechnologies/weaviate:1.22.4
    container_name: weaviate
    ports:
      - "8080:8080"
    environment:
      QUERY_DEFAULTS_LIMIT: 25
      AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED: 'true'
      PERSISTENCE_DATA_PATH: '/var/lib/weaviate'
      DEFAULT_VECTORIZER_MODULE: 'none'
      ENABLE_MODULES: 'text2vec-openai,text2vec-cohere,generative-openai'
    volumes:
      - weaviate_data:/var/lib/weaviate
    networks:
      - ai-network

  # Milvus (超大規模向量搜索)  
  etcd:
    image: quay.io/coreos/etcd:v3.5.5
    container_name: milvus-etcd
    environment:
      - ETCD_AUTO_COMPACTION_MODE=revision
      - ETCD_AUTO_COMPACTION_RETENTION=1000
      - ETCD_QUOTA_BACKEND_BYTES=4294967296
      - ETCD_SNAPSHOT_COUNT=50000
    volumes:
      - etcd_data:/etcd
    command: etcd -advertise-client-urls=http://127.0.0.1:2379 -listen-client-urls http://0.0.0.0:2379 --data-dir /etcd
    networks:
      - ai-network

  minio:
    image: minio/minio:RELEASE.2023-03-20T20-16-18Z
    container_name: milvus-minio
    environment:
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin
    ports:
      - "9001:9001"
      - "9000:9000"
    volumes:
      - minio_data:/minio_data
    command: minio server /minio_data --console-address ":9001"
    networks:
      - ai-network

  milvus:
    image: milvusdb/milvus:v2.3.2
    container_name: milvus
    command: ["milvus", "run", "standalone"]
    environment:
      ETCD_ENDPOINTS: etcd:2379
      MINIO_ADDRESS: minio:9000
    volumes:
      - milvus_data:/var/lib/milvus
    ports:
      - "19530:19530"
      - "9091:9091"
    depends_on:
      - "etcd"
      - "minio"
    networks:
      - ai-network

  # Redis Stack (向量 + 快取)
  redis-stack:
    image: redis/redis-stack:latest
    container_name: redis-stack
    ports:
      - "6379:6379"
      - "8001:8001"  # RedisInsight
    volumes:
      - redis_data:/data
    networks:
      - ai-network

  # Elasticsearch (文字 + 向量搜索)
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms1g -Xmx1g"
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - es_data:/usr/share/elasticsearch/data
    networks:
      - ai-network

  # Neo4j (圖形 + 向量)
  neo4j:
    image: neo4j:5.13-community
    container_name: neo4j
    ports:
      - "7474:7474"  # HTTP
      - "7687:7687"  # Bolt
    environment:
      NEO4J_AUTH: neo4j/password
      NEO4J_PLUGINS: '["apoc"]'
      NEO4J_apoc_export_file_enabled: 'true'
      NEO4J_apoc_import_file_enabled: 'true'
    volumes:
      - neo4j_data:/data
      - neo4j_logs:/logs
    networks:
      - ai-network

  # MongoDB Atlas 本地版（向量搜索）
  mongodb:
    image: mongo:7.0
    container_name: mongodb
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: password
    volumes:
      - mongodb_data:/data/db
    networks:
      - ai-network

volumes:
  postgres_data:
  qdrant_data:
  weaviate_data:
  etcd_data:
  minio_data:
  milvus_data:
  redis_data:
  es_data:
  neo4j_data:
  neo4j_logs:
  mongodb_data:

networks:
  ai-network:
    driver: bridge
```

## Spring AI 1.1 向量資料庫配置

### 1. 統一配置管理

```java
@Configuration
@ConfigurationProperties(prefix = "app.vectorstore")
@Data
public class VectorStoreProperties {
    private String type = "pgvector";  // 預設使用 PostgreSQL
    private int dimensions = 1536;     // OpenAI embedding 維度
    private String indexType = "ivfflat";
    private int lists = 100;
    private double similarityThreshold = 0.8;
    
    // 不同資料庫的特定配置
    private PgVectorConfig pgvector = new PgVectorConfig();
    private QdrantConfig qdrant = new QdrantConfig();
    private WeaviateConfig weaviate = new WeaviateConfig();
    
    @Data
    public static class PgVectorConfig {
        private String tableName = "vector_store";
        private String schema = "public";
        private boolean createTable = true;
    }
    
    @Data
    public static class QdrantConfig {
        private String collection = "documents";
        private boolean recreateCollection = false;
    }
    
    @Data  
    public static class WeaviateConfig {
        private String className = "Document";
        private String consistencyLevel = "ONE";
    }
}
```

### 2. 動態向量資料庫選擇

```java
@Configuration
@EnableConfigurationProperties(VectorStoreProperties.class)
public class VectorStoreAutoConfiguration {
    
    @Bean
    @ConditionalOnProperty(name = "app.vectorstore.type", havingValue = "pgvector")
    public VectorStore pgVectorStore(
            JdbcTemplate jdbcTemplate, 
            EmbeddingModel embeddingModel,
            VectorStoreProperties properties) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            .tableName(properties.getPgvector().getTableName())
            .schemaName(properties.getPgvector().getSchema())
            .dimensions(properties.getDimensions())
            .createTable(properties.getPgvector().isCreateTable())
            .build();
    }
    
    @Bean
    @ConditionalOnProperty(name = "app.vectorstore.type", havingValue = "qdrant")
    public VectorStore qdrantVectorStore(
            QdrantClient qdrantClient,
            EmbeddingModel embeddingModel,
            VectorStoreProperties properties) {
        return QdrantVectorStore.builder(qdrantClient, embeddingModel)
            .collectionName(properties.getQdrant().getCollection())
            .dimensions(properties.getDimensions())
            .recreateCollection(properties.getQdrant().isRecreateCollection())
            .build();
    }
    
    @Bean
    @ConditionalOnProperty(name = "app.vectorstore.type", havingValue = "weaviate")
    public VectorStore weaviateVectorStore(
            WeaviateClient weaviateClient,
            EmbeddingModel embeddingModel,
            VectorStoreProperties properties) {
        return WeaviateVectorStore.builder(weaviateClient, embeddingModel)
            .className(properties.getWeaviate().getClassName())
            .consistencyLevel(properties.getWeaviate().getConsistencyLevel())
            .build();
    }
    
    // 可以同時支援多個向量資料庫
    @Bean
    @Qualifier("hybridVectorStore")
    public VectorStore hybridVectorStore(
            List<VectorStore> vectorStores) {
        return new HybridVectorStore(vectorStores);
    }
}
```

### 3. 企業級向量資料庫服務

```java
@Service
@Slf4j
public class EnterpriseVectorStoreService {
    
    private final VectorStore primaryVectorStore;
    private final VectorStore backupVectorStore;
    private final EmbeddingModel embeddingModel;
    private final MeterRegistry meterRegistry;
    
    public EnterpriseVectorStoreService(
            @Qualifier("primary") VectorStore primaryVectorStore,
            @Qualifier("backup") VectorStore backupVectorStore,
            EmbeddingModel embeddingModel,
            MeterRegistry meterRegistry) {
        this.primaryVectorStore = primaryVectorStore;
        this.backupVectorStore = backupVectorStore;
        this.embeddingModel = embeddingModel;
        this.meterRegistry = meterRegistry;
    }
    
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public List<Document> searchWithFallback(String query, int topK, double threshold) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // 優先使用主要向量資料庫
            List<Document> results = primaryVectorStore.similaritySearch(
                SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(threshold)
                    .build()
            );
            
            sample.stop(Timer.builder("vectorstore.search")
                .tag("store", "primary")
                .tag("status", "success")
                .register(meterRegistry));
                
            return results;
            
        } catch (Exception e) {
            log.warn("Primary vector store failed, trying backup", e);
            
            try {
                List<Document> results = backupVectorStore.similaritySearch(
                    SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(threshold)
                        .build()
                );
                
                sample.stop(Timer.builder("vectorstore.search")
                    .tag("store", "backup")
                    .tag("status", "fallback")
                    .register(meterRegistry));
                    
                return results;
                
            } catch (Exception backupException) {
                sample.stop(Timer.builder("vectorstore.search")
                    .tag("store", "backup")
                    .tag("status", "failed")
                    .register(meterRegistry));
                    
                throw new VectorStoreException("Both primary and backup stores failed", backupException);
            }
        }
    }
    
    @Async
    public CompletableFuture<Void> addDocumentsWithSync(List<Document> documents) {
        try {
            // 同時寫入主要和備份資料庫
            CompletableFuture<Void> primaryFuture = CompletableFuture.runAsync(() -> 
                primaryVectorStore.add(documents));
                
            CompletableFuture<Void> backupFuture = CompletableFuture.runAsync(() -> 
                backupVectorStore.add(documents));
                
            return CompletableFuture.allOf(primaryFuture, backupFuture);
            
        } catch (Exception e) {
            log.error("Failed to add documents to vector stores", e);
            throw new VectorStoreException("Document addition failed", e);
        }
    }
    
    public VectorStoreStats getStats() {
        return VectorStoreStats.builder()
            .primaryStoreType(primaryVectorStore.getClass().getSimpleName())
            .backupStoreType(backupVectorStore.getClass().getSimpleName())
            .embeddingDimensions(embeddingModel.dimensions())
            .build();
    }
}
```

## 性能基準測試

### 向量資料庫性能對比

```java
@Component
@Slf4j
public class VectorStoreBenchmark {
    
    @EventListener
    @Async
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (shouldRunBenchmark()) {
            runBenchmarkSuite();
        }
    }
    
    private void runBenchmarkSuite() {
        List<VectorStore> stores = getAvailableVectorStores();
        List<Document> testDocuments = generateTestDocuments(1000);
        String testQuery = "What is machine learning?";
        
        Map<String, BenchmarkResult> results = new HashMap<>();
        
        for (VectorStore store : stores) {
            BenchmarkResult result = benchmarkVectorStore(store, testDocuments, testQuery);
            results.put(store.getClass().getSimpleName(), result);
        }
        
        logBenchmarkResults(results);
    }
    
    private BenchmarkResult benchmarkVectorStore(VectorStore store, List<Document> docs, String query) {
        long startTime, endTime;
        
        // 測試寫入性能
        startTime = System.currentTimeMillis();
        store.add(docs);
        endTime = System.currentTimeMillis();
        long writeTime = endTime - startTime;
        
        // 測試查詢性能
        startTime = System.currentTimeMillis();
        List<Document> results = store.similaritySearch(SearchRequest.builder()
            .query(query)
            .topK(10)
            .build());
        endTime = System.currentTimeMillis();
        long searchTime = endTime - startTime;
        
        return BenchmarkResult.builder()
            .writeTimeMs(writeTime)
            .searchTimeMs(searchTime)
            .resultsCount(results.size())
            .build();
    }
}

@Builder
@Data
public class BenchmarkResult {
    private long writeTimeMs;
    private long searchTimeMs;
    private int resultsCount;
}
```

## 選擇建議

### 不同場景下的最佳選擇

```java
@Component
public class VectorStoreRecommendationService {
    
    public VectorStoreRecommendation recommend(UseCase useCase) {
        return switch (useCase) {
            case DEVELOPMENT_TESTING -> VectorStoreRecommendation.builder()
                .primary("InMemoryVectorStore")
                .reasoning("快速開發測試，無需額外依賴")
                .build();
                
            case SMALL_TO_MEDIUM_ENTERPRISE -> VectorStoreRecommendation.builder()
                .primary("PgVectorStore")
                .backup("RedisVectorStore")
                .reasoning("PostgreSQL 穩定可靠，Redis 提供快速快取")
                .build();
                
            case LARGE_SCALE_ENTERPRISE -> VectorStoreRecommendation.builder()
                .primary("QdrantVectorStore")
                .backup("MilvusVectorStore")
                .reasoning("專用向量資料庫，支援大規模高性能查詢")
                .build();
                
            case CLOUD_NATIVE -> VectorStoreRecommendation.builder()
                .primary("PineconeVectorStore")
                .backup("WeaviateVectorStore")
                .reasoning("雲端托管，自動擴展，無需維護")
                .build();
        };
    }
}

public enum UseCase {
    DEVELOPMENT_TESTING,
    SMALL_TO_MEDIUM_ENTERPRISE,
    LARGE_SCALE_ENTERPRISE,
    CLOUD_NATIVE
}
```

## 回顧

今天學到的內容：

1. **Spring AI 1.1 向量資料庫生態**：20+ 種向量儲存選擇
2. **Docker 部署環境**：一鍵部署多種向量資料庫
3. **企業級配置**：動態選擇、備份容錯、性能監控
4. **最佳選擇指南**：不同規模企業的向量資料庫選擇建議

明天我們將深入 RAG 系統的進階優化技術！

## Source Code

今日程式碼: [https://github.com/kevintsai1202/SpringBoot-AI-Day21.git](https://github.com/kevintsai1202/SpringBoot-AI-Day21.git)