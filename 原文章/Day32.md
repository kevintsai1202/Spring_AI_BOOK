# 使用 Spring AI 打造企業 RAG 知識庫【32】- 提高RAG準確率從Embedding換起

## 預設不見得最好

![Embedding Models](https://example.com/embedding-models.jpg)

開始用 RAG 處理資料後，發現抓出的內容似乎有點落差，而影響查詢結果最重要的就是 Embedding。本文將介紹如何選擇和配置最適合的 Embedding 模型，並使用最新的 Spring AI 1.1-SNAPSHOT API。

## Embedding 模型效能比較

根據最新的繁體中文 Embedding 評測結果，不同模型的表現差異很大：

### 1. OpenAI Embedding 模型比較

| 模型 | 排名 | 準確率 | 價格 | Dimensions | 特點 |
|------|------|--------|------|------------|------|
| text-embedding-ada-002 | 24 | ~83% | 較高 | 1536 | 舊版預設模型 |
| text-embedding-3-small | 23 | ~84% | 便宜5倍 | 512-1536 | 性價比高 |
| text-embedding-3-large | 13 | ~90% | 最貴 | 256-3072 | 高準確率 |

### 2. 其他優秀模型

| 模型 | 排名 | 準確率 | 特點 |
|------|------|--------|------|
| voyage-multilingual-2 | 1 | ~97% | 多語言最佳 |
| multilingual-e5-small | 4 | ~92% | 開源，384維度 |
| multilingual-e5-large | 6 | ~94% | 開源，1024維度 |

> **重要觀念**：dimensions 不是越高就越好！更高的維度意味著更多的計算成本和儲存空間。

## Spring AI 1.1-SNAPSHOT Embedding 配置

### 1. OpenAI Embedding 配置

最簡單的升級方式是使用更好的 OpenAI 模型：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-3-small
          dimensions: 1024  # 可選：自定義維度
```

對應的 Java 配置：

```java
@Configuration
public class EmbeddingConfig {
    
    @Bean
    public OpenAiEmbeddingModel openAiEmbeddingModel(
            @Value("${spring.ai.openai.api-key}") String apiKey) {
        
        return OpenAiEmbeddingModel.builder()
            .withApiKey(apiKey)
            .withModel(OpenAiEmbeddingOptions.DEFAULT_MODEL) // text-embedding-3-small
            .withOptions(OpenAiEmbeddingOptions.builder()
                .withModel("text-embedding-3-small")
                .withDimensions(1024)
                .withUser("enterprise-rag-system")
                .build())
            .build();
    }
}
```

### 2. Voyage AI Embedding 配置

使用排名第一的 Voyage AI 模型：

```yaml
spring:
  ai:
    embedding:
      voyage:
        api-key: ${VOYAGE_API_KEY}
        base-url: https://api.voyageai.com/v1
        model: voyage-multilingual-2
        max-retries: 3
        timeout: 30s
```

由於 Spring AI 還沒有原生支援 Voyage AI，我們需要自己實現：

```java
@Configuration
public class VoyageEmbeddingConfig {
    
    @Bean
    @ConditionalOnProperty(name = "spring.ai.embedding.provider", havingValue = "voyage")
    public EmbeddingModel voyageEmbeddingModel(
            @Value("${spring.ai.embedding.voyage.api-key}") String apiKey,
            @Value("${spring.ai.embedding.voyage.base-url}") String baseUrl,
            @Value("${spring.ai.embedding.voyage.model}") String model) {
        
        return new VoyageEmbeddingModel(apiKey, baseUrl, model);
    }
}

@Component
public class VoyageEmbeddingModel implements EmbeddingModel {
    
    private final RestClient restClient;
    private final String model;
    private final ObjectMapper objectMapper;
    
    public VoyageEmbeddingModel(String apiKey, String baseUrl, String model) {
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }
    
    @Override
    public List<Double> embed(String text) {
        return embed(List.of(text)).get(0);
    }
    
    @Override
    public List<List<Double>> embed(List<String> texts) {
        try {
            VoyageEmbeddingRequest request = new VoyageEmbeddingRequest(texts, model);
            
            VoyageEmbeddingResponse response = restClient.post()
                .uri("/embeddings")
                .body(request)
                .retrieve()
                .body(VoyageEmbeddingResponse.class);
            
            return response.data().stream()
                .map(VoyageEmbedding::embedding)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            throw new RuntimeException("Failed to get embeddings from Voyage AI", e);
        }
    }
    
    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> texts = request.getInstructions();
        List<List<Double>> embeddings = embed(texts);
        
        List<Embedding> embeddingResults = embeddings.stream()
            .map(embedding -> new Embedding(embedding, 0))
            .collect(Collectors.toList());
        
        return new EmbeddingResponse(embeddingResults);
    }
    
    // 資料傳輸物件
    public record VoyageEmbeddingRequest(List<String> input, String model) {}
    
    public record VoyageEmbeddingResponse(List<VoyageEmbedding> data, VoyageUsage usage) {}
    
    public record VoyageEmbedding(List<Double> embedding, int index) {}
    
    public record VoyageUsage(int total_tokens) {}
}
```

### 3. 本地開源模型配置 (Ollama)

對於隱私要求較高的企業，可以使用本地部署的開源模型：

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      embedding:
        options:
          model: nomic-embed-text  # 或 mxbai-embed-large
```

```java
@Configuration
@ConditionalOnProperty(name = "spring.ai.embedding.provider", havingValue = "ollama")
public class OllamaEmbeddingConfig {
    
    @Bean
    public OllamaEmbeddingModel ollamaEmbeddingModel(
            @Value("${spring.ai.ollama.base-url}") String baseUrl,
            @Value("${spring.ai.ollama.embedding.options.model}") String model) {
        
        return OllamaEmbeddingModel.builder()
            .withBaseUrl(baseUrl)
            .withModel(model)
            .withOptions(OllamaOptions.builder()
                .withTemperature(0f)
                .build())
            .build();
    }
}
```

## 企業級 Embedding 服務

### 1. 多模型支援服務

```java
@Service
@RequiredArgsConstructor
public class EnterpriseEmbeddingService {
    
    private final Map<String, EmbeddingModel> embeddingModels;
    private final EmbeddingModelSelector modelSelector;
    private final MeterRegistry meterRegistry;
    
    public List<Double> embed(String text, String preferredModel) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            EmbeddingModel model = selectModel(preferredModel, text);
            List<Double> embedding = model.embed(text);
            
            // 記錄成功指標
            meterRegistry.counter("embedding.requests.success",
                "model", model.getClass().getSimpleName(),
                "preferred_model", preferredModel)
                .increment();
            
            return embedding;
            
        } catch (Exception e) {
            meterRegistry.counter("embedding.requests.error",
                "preferred_model", preferredModel,
                "error", e.getClass().getSimpleName())
                .increment();
            
            throw new EmbeddingException("Failed to generate embedding", e);
            
        } finally {
            sample.stop(Timer.builder("embedding.request.duration")
                .tag("preferred_model", preferredModel)
                .register(meterRegistry));
        }
    }
    
    public List<List<Double>> embedBatch(List<String> texts, String preferredModel, int batchSize) {
        List<List<Double>> results = new ArrayList<>();
        
        // 分批處理避免超過API限制
        List<List<String>> batches = Lists.partition(texts, batchSize);
        
        for (List<String> batch : batches) {
            EmbeddingModel model = selectModel(preferredModel, batch.get(0));
            List<List<Double>> batchResults = model.embed(batch);
            results.addAll(batchResults);
            
            // 添加短暫延遲避免API限流
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        return results;
    }
    
    private EmbeddingModel selectModel(String preferredModel, String text) {
        return modelSelector.selectModel(preferredModel, text, embeddingModels);
    }
}

@Component
public class EmbeddingModelSelector {
    
    public EmbeddingModel selectModel(String preferredModel, String text, 
                                    Map<String, EmbeddingModel> availableModels) {
        
        // 1. 如果指定了模型且可用，直接使用
        if (preferredModel != null && availableModels.containsKey(preferredModel)) {
            return availableModels.get(preferredModel);
        }
        
        // 2. 根據文本長度選擇合適模型
        int textLength = text.length();
        
        if (textLength > 8000) {
            // 長文本使用支援較長序列的模型
            return availableModels.getOrDefault("text-embedding-3-large", 
                   availableModels.values().iterator().next());
        } else if (textLength < 500) {
            // 短文本使用小模型節省成本
            return availableModels.getOrDefault("text-embedding-3-small",
                   availableModels.values().iterator().next());
        }
        
        // 3. 預設使用最佳性價比模型
        return availableModels.getOrDefault("voyage-multilingual-2",
               availableModels.values().iterator().next());
    }
}
```

### 2. Embedding 快取系統

```java
@Configuration
@EnableCaching
public class EmbeddingCacheConfig {
    
    @Bean("embeddingCacheManager")
    public CacheManager embeddingCacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
        
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(24))  // 24小時過期
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}

@Service
public class CachedEmbeddingService {
    
    private final EnterpriseEmbeddingService embeddingService;
    
    @Cacheable(value = "embeddings", key = "#text.hashCode() + '_' + #model", 
               cacheManager = "embeddingCacheManager")
    public List<Double> embed(String text, String model) {
        return embeddingService.embed(text, model);
    }
    
    @CachePut(value = "embeddings", key = "#text.hashCode() + '_' + #model",
              cacheManager = "embeddingCacheManager")
    public List<Double> refreshEmbedding(String text, String model) {
        return embeddingService.embed(text, model);
    }
    
    @CacheEvict(value = "embeddings", allEntries = true, 
                cacheManager = "embeddingCacheManager")
    public void clearEmbeddingCache() {
        // 清空所有快取
    }
}
```

### 3. Embedding 品質評估

```java
@Service
@RequiredArgsConstructor
public class EmbeddingQualityService {
    
    private final List<EmbeddingModel> embeddingModels;
    
    public EmbeddingQualityReport evaluateModels(List<String> testTexts, 
                                               List<String> expectedSimilarTexts) {
        
        Map<String, ModelPerformance> performances = new HashMap<>();
        
        for (EmbeddingModel model : embeddingModels) {
            ModelPerformance performance = evaluateModel(model, testTexts, expectedSimilarTexts);
            performances.put(model.getClass().getSimpleName(), performance);
        }
        
        return new EmbeddingQualityReport(performances, LocalDateTime.now());
    }
    
    private ModelPerformance evaluateModel(EmbeddingModel model, 
                                         List<String> testTexts,
                                         List<String> expectedSimilarTexts) {
        
        List<Double> similarities = new ArrayList<>();
        long totalTime = 0;
        int successCount = 0;
        
        for (int i = 0; i < testTexts.size(); i++) {
            try {
                long startTime = System.currentTimeMillis();
                
                List<Double> embedding1 = model.embed(testTexts.get(i));
                List<Double> embedding2 = model.embed(expectedSimilarTexts.get(i));
                
                double similarity = cosineSimilarity(embedding1, embedding2);
                similarities.add(similarity);
                
                totalTime += (System.currentTimeMillis() - startTime);
                successCount++;
                
            } catch (Exception e) {
                log.warn("Failed to evaluate model {} for text pair {}: {}", 
                        model.getClass().getSimpleName(), i, e.getMessage());
            }
        }
        
        double avgSimilarity = similarities.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        double avgResponseTime = successCount > 0 ? (double) totalTime / successCount : 0;
        
        return new ModelPerformance(
            avgSimilarity,
            avgResponseTime,
            successCount,
            testTexts.size(),
            similarities.stream().mapToDouble(Double::doubleValue).min().orElse(0),
            similarities.stream().mapToDouble(Double::doubleValue).max().orElse(0)
        );
    }
    
    private double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must have the same dimension");
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    public record ModelPerformance(
        double avgSimilarity,
        double avgResponseTime,
        int successCount,
        int totalCount,
        double minSimilarity,
        double maxSimilarity
    ) {}
    
    public record EmbeddingQualityReport(
        Map<String, ModelPerformance> performances,
        LocalDateTime evaluationTime
    ) {}
}
```

### 4. 自動模型選擇

```java
@Service
@RequiredArgsConstructor
public class AdaptiveEmbeddingService {
    
    private final EmbeddingQualityService qualityService;
    private final Map<String, EmbeddingModel> embeddingModels;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Scheduled(fixedRate = 3600000) // 每小時評估一次
    public void updateModelRankings() {
        try {
            // 使用測試數據集評估所有模型
            List<String> testTexts = loadTestTexts();
            List<String> expectedSimilarTexts = loadExpectedSimilarTexts();
            
            EmbeddingQualityService.EmbeddingQualityReport report = 
                qualityService.evaluateModels(testTexts, expectedSimilarTexts);
            
            // 根據評估結果更新模型排名
            List<String> rankedModels = report.performances().entrySet().stream()
                .sorted((e1, e2) -> {
                    // 綜合考慮準確率和響應時間
                    double score1 = e1.getValue().avgSimilarity() - 
                                   (e1.getValue().avgResponseTime() / 10000.0);
                    double score2 = e2.getValue().avgSimilarity() - 
                                   (e2.getValue().avgResponseTime() / 10000.0);
                    return Double.compare(score2, score1);
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
            // 儲存到 Redis
            redisTemplate.opsForValue().set("embedding:model:rankings", rankedModels, 
                                           Duration.ofHours(2));
            
            log.info("Updated embedding model rankings: {}", rankedModels);
            
        } catch (Exception e) {
            log.error("Failed to update model rankings", e);
        }
    }
    
    public List<Double> embed(String text) {
        // 使用當前最佳模型
        String bestModel = getBestModel();
        EmbeddingModel model = embeddingModels.get(bestModel);
        
        if (model != null) {
            return model.embed(text);
        }
        
        // 回退到預設模型
        return embeddingModels.values().iterator().next().embed(text);
    }
    
    @SuppressWarnings("unchecked")
    private String getBestModel() {
        List<String> rankings = (List<String>) redisTemplate.opsForValue()
            .get("embedding:model:rankings");
        
        return (rankings != null && !rankings.isEmpty()) ? 
               rankings.get(0) : "text-embedding-3-small";
    }
}
```

## 監控和告警

```java
@RestController
@RequestMapping("/ai/embedding/monitoring")
@RequiredArgsConstructor
public class EmbeddingMonitoringController {
    
    private final MeterRegistry meterRegistry;
    private final EmbeddingQualityService qualityService;
    
    @GetMapping("/metrics")
    public Map<String, Object> getEmbeddingMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // 請求統計
        Counter successCounter = meterRegistry.find("embedding.requests.success").counter();
        Counter errorCounter = meterRegistry.find("embedding.requests.error").counter();
        
        if (successCounter != null && errorCounter != null) {
            double totalRequests = successCounter.count() + errorCounter.count();
            double successRate = totalRequests > 0 ? successCounter.count() / totalRequests : 0;
            
            metrics.put("totalRequests", totalRequests);
            metrics.put("successRate", successRate);
            metrics.put("errorRate", 1 - successRate);
        }
        
        // 響應時間統計
        Timer durationTimer = meterRegistry.find("embedding.request.duration").timer();
        if (durationTimer != null) {
            metrics.put("avgResponseTime", durationTimer.mean(TimeUnit.MILLISECONDS));
            metrics.put("maxResponseTime", durationTimer.max(TimeUnit.MILLISECONDS));
            metrics.put("requestCount", durationTimer.count());
        }
        
        return metrics;
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkEmbeddingHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 測試各個模型的健康狀態
            // 實現省略...
            
            health.put("status", "healthy");
            health.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }
}
```

## 驗收成果

### 測試不同模型效能

```bash
# 測試 OpenAI 模型
curl -X POST http://localhost:8080/ai/embedding/test \
-H "Content-Type: application/json" \
-d '{"text": "這是測試文本", "model": "text-embedding-3-small"}'

# 測試 Voyage AI 模型
curl -X POST http://localhost:8080/ai/embedding/test \
-H "Content-Type: application/json" \
-d '{"text": "這是測試文本", "model": "voyage-multilingual-2"}'

# 獲取模型品質報告
curl http://localhost:8080/ai/embedding/quality-report

# 監控指標
curl http://localhost:8080/ai/embedding/monitoring/metrics
```

## 回顧

今天學到了什麼？

- 繁體中文 Embedding 的排行榜和模型選擇原則
- dimensions 不是越大越好，價格也不是越貴越好
- 使用最新的 Spring AI 1.1-SNAPSHOT API 配置不同 Embedding 模型
- 企業級多模型支援和自動選擇機制
- Embedding 快取和效能優化策略
- 模型品質評估和監控系統
- 自適應模型選擇機制

選擇合適的 Embedding 模型對 RAG 系統的效果至關重要，需要在準確率、成本、響應時間之間找到最佳平衡點。

## Source Code

程式碼下載: [https://github.com/kevintsai1202/SpringBoot-AI-Day32-Updated.git](https://github.com/kevintsai1202/SpringBoot-AI-Day32-Updated.git)