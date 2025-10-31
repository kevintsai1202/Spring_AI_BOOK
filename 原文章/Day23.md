# 使用 Spring AI 打造企業 RAG 知識庫【23】- 如何將內容向量化與最新 ChatClient API

> 以下內容已根據 Spring AI 1.1-SNAPSHOT 版本進行更新，使用最新的 ChatClient API 和 @Tool 註解語法

![Embedding示意圖](https://ithelp.ithome.com.tw/upload/images/20240823/201612904M7YeyrfkS.jpg)

剛看到 Embedding 這個名詞一直無法跟向量化聯想在一起，直到看了[台大教授陳縕儂的影片](https://www.youtube.com/watch?v=K2oYKdK--9U)才瞭解其中含意。有興趣的朋友可自行看影片學習，這裡結合 Spring AI 1.1 的最新特性來說明概念。

## Embedding 原理深度解析

### 什麼是 Embedding？

Embedding 就是將內容轉成向量數據的一種表現方式，演算法會：

1. **Token 化**：將文字拆分成 Token
2. **上下文計算**：分析每個 Token 與一定大小的上下文關係
3. **向量轉換**：透過神經網路模型計算得到向量值
4. **維度壓縮**：將原本的矩陣資料壓縮進一個維度內

這些 Embedding 數據是向量資料庫用來做語意搜尋的依據，也是 RAG 儲存資料跟檢索資料會用到的向量資訊。查詢時會把每個維度的向量利用餘弦相似度或歐式距離法算出向量間的距離，求出結果最相似的前 N 筆數據。

## Spring AI 1.1 中的 Embedding Model 架構

### 核心介面設計

Spring AI 1.1 提供了更強大和一致的 EmbeddingModel 介面：

```java
public interface EmbeddingModel extends Model<EmbeddingRequest, EmbeddingResponse> {
    
    // 核心方法
    EmbeddingResponse call(EmbeddingRequest request);
    
    // 便利方法 - 單一文件
    float[] embed(Document document);
    
    // 便利方法 - 單一文字
    default float[] embed(String text) {
        return this.embed(List.of(text)).iterator().next();
    }
    
    // 批次處理
    default List<float[]> embed(List<String> texts) {
        return this.call(new EmbeddingRequest(texts, EmbeddingOptions.EMPTY))
            .getResults()
            .stream()
            .map(Embedding::getOutput)
            .toList();
    }
    
    // 完整回應
    default EmbeddingResponse embedForResponse(List<String> texts) {
        return this.call(new EmbeddingRequest(texts, EmbeddingOptions.EMPTY));
    }
    
    // 獲取維度數
    default int dimensions() {
        return embed("Test String").length;
    }
}
```

### 請求和回應結構

```java
// 請求結構
public class EmbeddingRequest implements ModelRequest<List<String>> {
    private final List<String> inputs;
    private final EmbeddingOptions options;
}

// 回應結構
public class EmbeddingResponse implements ModelResponse<Embedding> {
    private List<Embedding> embeddings;
    private EmbeddingResponseMetadata metadata;
}

// 單一 Embedding
public class Embedding implements ModelResult<float[]> {
    private float[] embedding;
    private Integer index;
    private EmbeddingResultMetadata metadata;
}
```

## 程式實作：使用最新 ChatClient API

### 基本 Embedding 服務

```java
@RestController
@RequiredArgsConstructor
public class EmbeddingController {
    
    private final EmbeddingModel embeddingModel;
    
    @GetMapping("/embedding/single")
    public EmbeddingResult embedSingle(@RequestParam String text) {
        float[] embedding = embeddingModel.embed(text);
        return new EmbeddingResult(
            text, 
            embedding, 
            embeddingModel.dimensions()
        );
    }
    
    @PostMapping("/embedding/batch")
    public List<EmbeddingResult> embedBatch(@RequestBody List<String> texts) {
        List<float[]> embeddings = embeddingModel.embed(texts);
        
        return IntStream.range(0, texts.size())
            .mapToObj(i -> new EmbeddingResult(
                texts.get(i), 
                embeddings.get(i), 
                embeddingModel.dimensions()
            ))
            .toList();
    }
    
    @PostMapping("/embedding/detailed")
    public DetailedEmbeddingResponse embedWithMetadata(@RequestBody List<String> texts) {
        EmbeddingResponse response = embeddingModel.embedForResponse(texts);
        
        return new DetailedEmbeddingResponse(
            response.getResults(),
            response.getMetadata(),
            embeddingModel.dimensions()
        );
    }
}

record EmbeddingResult(String text, float[] embedding, int dimensions) {}

record DetailedEmbeddingResponse(
    List<Embedding> embeddings, 
    EmbeddingResponseMetadata metadata,
    int dimensions
) {}
```

### 整合 ChatClient 與 Embedding

```java
@Service
@RequiredArgsConstructor
public class EnhancedChatService {
    
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    
    public String chatWithEmbeddingAnalysis(String userMessage) {
        // 先分析用戶訊息的向量特徵
        float[] userEmbedding = embeddingModel.embed(userMessage);
        
        return chatClient.prompt()
            .user(userMessage)
            .tools(new EmbeddingAnalysisTools(embeddingModel, vectorStore))
            .call()
            .content();
    }
}
```

### 使用最新 @Tool 註解的 Embedding 工具

```java
@Component
public class EmbeddingAnalysisTools {
    
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    
    public EmbeddingAnalysisTools(EmbeddingModel embeddingModel, VectorStore vectorStore) {
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }
    
    @Tool(description = "分析文字的向量特徵，包括維度和相似度計算")
    public String analyzeTextEmbedding(String text) {
        float[] embedding = embeddingModel.embed(text);
        
        return String.format("""
            文字: %s
            向量維度: %d
            向量範數: %.4f
            前5個維度值: %s
            """, 
            text,
            embedding.length,
            calculateNorm(embedding),
            Arrays.stream(embedding).limit(5)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", "))
        );
    }
    
    @Tool(description = "計算兩段文字之間的語意相似度")
    public String calculateSimilarity(String text1, String text2) {
        float[] embedding1 = embeddingModel.embed(text1);
        float[] embedding2 = embeddingModel.embed(text2);
        
        double similarity = cosineSimilarity(embedding1, embedding2);
        
        return String.format("""
            文字1: %s
            文字2: %s
            餘弦相似度: %.4f
            相似程度: %s
            """,
            text1, text2, similarity, 
            similarity > 0.8 ? "非常相似" : 
            similarity > 0.6 ? "相似" : 
            similarity > 0.4 ? "部分相似" : "不相似"
        );
    }
    
    @Tool(description = "在向量資料庫中搜尋相似內容")
    public String searchSimilarContent(String query, int topK) {
        List<Document> results = vectorStore.similaritySearch(
            SearchRequest.query(query).withTopK(topK)
        );
        
        StringBuilder response = new StringBuilder();
        response.append(String.format("搜尋「%s」的結果（前%d個）:\n\n", query, topK));
        
        for (int i = 0; i < results.size(); i++) {
            Document doc = results.get(i);
            response.append(String.format("""
                %d. 內容: %s
                   相關度: %.4f
                   來源: %s
                
                """, 
                i + 1,
                doc.getContent().substring(0, Math.min(100, doc.getContent().length())),
                doc.getMetadata().getOrDefault("distance", 0.0),
                doc.getMetadata().getOrDefault("source", "未知")
            ));
        }
        
        return response.toString();
    }
    
    private double calculateNorm(float[] vector) {
        return Math.sqrt(Arrays.stream(vector)
            .mapToDouble(f -> f * f)
            .sum());
    }
    
    private double cosineSimilarity(float[] vector1, float[] vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
```

## 企業級 Embedding 配置

### 多 Embedding 模型配置

```java
@Configuration
public class EmbeddingConfig {
    
    @Bean
    @Primary
    public EmbeddingModel primaryEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
            .apiKey(apiKey)
            .model("text-embedding-3-large")  // 最新模型
            .dimensions(3072)  // 可配置維度
            .build();
    }
    
    @Bean
    @Qualifier("fast")
    public EmbeddingModel fastEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
            .apiKey(apiKey)
            .model("text-embedding-3-small")  // 快速模型
            .dimensions(1536)
            .build();
    }
    
    @Bean
    @Qualifier("multilingual")
    public EmbeddingModel multilingualEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
            .baseUrl("http://localhost:11434")
            .model("nomic-embed-text")  // 多語言支援
            .build();
    }
}
```

### Embedding 快取策略

```java
@Service
@RequiredArgsConstructor
public class CachedEmbeddingService {
    
    private final EmbeddingModel embeddingModel;
    private final RedisTemplate<String, float[]> redisTemplate;
    
    @Cacheable(value = "embeddings", key = "#text.hashCode()")
    public float[] getEmbedding(String text) {
        return embeddingModel.embed(text);
    }
    
    @CacheEvict(value = "embeddings", allEntries = true)
    public void clearEmbeddingCache() {
        // 清除快取
    }
    
    public List<float[]> getBatchEmbeddings(List<String> texts) {
        List<float[]> results = new ArrayList<>();
        List<String> uncachedTexts = new ArrayList<>();
        List<Integer> uncachedIndices = new ArrayList<>();
        
        // 檢查快取
        for (int i = 0; i < texts.size(); i++) {
            String cacheKey = "embeddings::" + texts.get(i).hashCode();
            float[] cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                results.add(cached);
            } else {
                results.add(null);
                uncachedTexts.add(texts.get(i));
                uncachedIndices.add(i);
            }
        }
        
        // 批次處理未快取的文字
        if (!uncachedTexts.isEmpty()) {
            List<float[]> newEmbeddings = embeddingModel.embed(uncachedTexts);
            
            for (int i = 0; i < newEmbeddings.size(); i++) {
                int originalIndex = uncachedIndices.get(i);
                float[] embedding = newEmbeddings.get(i);
                
                results.set(originalIndex, embedding);
                
                // 快取新的 embedding
                String cacheKey = "embeddings::" + uncachedTexts.get(i).hashCode();
                redisTemplate.opsForValue().set(cacheKey, embedding, Duration.ofHours(24));
            }
        }
        
        return results;
    }
}
```

## 驗證成果：使用 ChatClient 測試

```java
@RestController
@RequiredArgsConstructor
public class EmbeddingTestController {
    
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    
    @GetMapping("/test/embedding-info")
    public String testEmbeddingInfo(@RequestParam String text) {
        return chatClient.prompt()
            .user("請分析這段文字的向量特徵：" + text)
            .tools(new EmbeddingAnalysisTools(embeddingModel, null))
            .call()
            .content();
    }
    
    @GetMapping("/test/similarity")
    public String testSimilarity(@RequestParam String text1, @RequestParam String text2) {
        return chatClient.prompt()
            .user(String.format("請計算這兩段文字的相似度：\n1. %s\n2. %s", text1, text2))
            .tools(new EmbeddingAnalysisTools(embeddingModel, null))
            .call()
            .content();
    }
    
    @GetMapping("/test/dimensions")
    public Map<String, Object> testDimensions() {
        float[] embedding = embeddingModel.embed("測試文字");
        
        return Map.of(
            "text", "測試文字",
            "dimensions", embeddingModel.dimensions(),
            "actualLength", embedding.length,
            "firstFiveValues", Arrays.stream(embedding)
                .limit(5)
                .boxed()
                .toList(),
            "norm", Math.sqrt(Arrays.stream(embedding)
                .mapToDouble(f -> f * f)
                .sum())
        );
    }
}
```

## 向量資料庫整合的最佳實踐

### Document 建立與 Metadata 管理

```java
@Service
@RequiredArgsConstructor
public class DocumentEmbeddingService {
    
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    
    public void storeDocumentWithMetadata(String content, Map<String, Object> metadata) {
        // 增強 metadata
        Map<String, Object> enhancedMetadata = new HashMap<>(metadata);
        enhancedMetadata.put("timestamp", Instant.now().toString());
        enhancedMetadata.put("content_length", content.length());
        enhancedMetadata.put("content_hash", content.hashCode());
        enhancedMetadata.put("embedding_model", "text-embedding-3-large");
        enhancedMetadata.put("embedding_dimensions", embeddingModel.dimensions());
        
        Document document = new Document(content, enhancedMetadata);
        vectorStore.add(List.of(document));
    }
    
    public List<Document> searchWithFilters(String query, Map<String, Object> filters, int topK) {
        SearchRequest.Builder builder = SearchRequest.query(query).withTopK(topK);
        
        // 構建過濾表達式
        if (!filters.isEmpty()) {
            String filterExpression = filters.entrySet().stream()
                .map(entry -> String.format("%s == '%s'", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" && "));
            builder.withFilterExpression(filterExpression);
        }
        
        return vectorStore.similaritySearch(builder.build());
    }
}
```

## 效能監控與最佳化

### Embedding 效能監控

```java
@Component
@RequiredArgsConstructor
public class EmbeddingMetrics {
    
    private final MeterRegistry meterRegistry;
    private final EmbeddingModel embeddingModel;
    
    @EventListener
    public void handleEmbeddingRequest(EmbeddingRequestEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            float[] result = embeddingModel.embed(event.getText());
            sample.stop(Timer.builder("embedding.request.duration")
                .tag("model", "text-embedding-3-large")
                .tag("status", "success")
                .register(meterRegistry));
                
            meterRegistry.counter("embedding.request.count",
                "model", "text-embedding-3-large",
                "status", "success").increment();
                
            meterRegistry.gauge("embedding.dimensions", 
                "model", "text-embedding-3-large", result.length);
                
        } catch (Exception e) {
            sample.stop(Timer.builder("embedding.request.duration")
                .tag("model", "text-embedding-3-large")
                .tag("status", "error")
                .register(meterRegistry));
                
            meterRegistry.counter("embedding.request.error",
                "model", "text-embedding-3-large",
                "error", e.getClass().getSimpleName()).increment();
        }
    }
}
```

## 我們能做什麼？企業應用場景

### 1. 智能文件分類

```java
@Service
public class DocumentClassificationService {
    
    public String classifyDocument(String content) {
        return chatClient.prompt()
            .user("請根據向量特徵分析這個文件的類型：" + content)
            .tools(new EmbeddingAnalysisTools(embeddingModel, vectorStore))
            .call()
            .content();
    }
}
```

### 2. 內容相似度檢測

```java
@Service  
public class ContentSimilarityService {
    
    public double detectPlagiarism(String originalText, String checkText) {
        float[] embedding1 = embeddingModel.embed(originalText);
        float[] embedding2 = embeddingModel.embed(checkText);
        return cosineSimilarity(embedding1, embedding2);
    }
}
```

### 3. 知識庫智能搜尋

思考一下向量資料庫還能加入什麼內容：

- **聊天訊息**：加入 `conversationId`，透過 ID 篩選內容
- **RAG 資料**：包含檔案名稱，查詢時能列出原始檔案  
- **網路爬蟲資料**：加上 URL，追蹤來源
- **企業內部資料**：加上系統和資料 ID，快速連結到內部系統
- **多媒體內容**：加上內容類型、建立時間等 metadata
- **用戶行為**：加上用戶偏好、點擊記錄等個人化資訊

## Spring AI 1.1 的 Embedding 新特性

### 1. 多模態 Embedding 支援

```java
// 未來版本將支援圖片和音訊的 Embedding
@Tool(description = "分析多模態內容的向量特徵")
public String analyzeMultimodalEmbedding(String text, byte[] image) {
    // 將支援圖片+文字的聯合 Embedding
    return "多模態分析結果...";
}
```

### 2. 批次最佳化

```java
// 自動批次處理，提高效率
List<String> texts = Arrays.asList("文字1", "文字2", "文字3");
List<float[]> embeddings = embeddingModel.embed(texts);  // 一次 API 調用
```

### 3. 維度自適應

```java
// 根據應用需求調整向量維度
EmbeddingModel adaptiveModel = OpenAiEmbeddingModel.builder()
    .model("text-embedding-3-large")
    .dimensions(512)  // 可配置較小維度以節省儲存空間
    .build();
```

## 回顧

今天學到了什麼？

- 深入了解 Embedding 的原理和 Spring AI 1.1 的架構改進
- 學會使用最新的 ChatClient API 和 @Tool 註解
- 掌握 EmbeddingModel 的各種使用方式和最佳實踐
- 了解向量資料庫的 Metadata 管理重要性
- 實作企業級的 Embedding 服務與效能監控

> Spring AI 1.1 中的 Embedding 系統提供了更強大的向量化能力，結合最新的 ChatClient API 和 @Tool 註解，讓開發者能夠建構更智能和高效的 AI 應用。

> 明天我們將探討 RAG 流程的詳細實現，了解如何整合 Embedding 和 Advisor 系統。

## Source Code

程式碼範例：[https://github.com/kevintsai1202/SpringBoot-AI-Day23-Updated.git](https://github.com/kevintsai1202/SpringBoot-AI-Day23-Updated.git)