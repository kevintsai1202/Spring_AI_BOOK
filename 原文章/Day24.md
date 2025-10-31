# 使用 Spring AI 打造企業 RAG 知識庫【24】- RAG 流程詳解與最新 Advisor API

> 以下內容已根據 Spring AI 1.1-SNAPSHOT 版本進行更新，使用最新的 Advisor API 和模組化 RAG 架構

![RAG流程示意圖](https://ithelp.ithome.com.tw/upload/images/20240824/20161290vv98vgjkCI.jpg)

之前的章節幾乎涵蓋了 Spring AI 的基本功能，是時候向下一個里程邁進了，接下來就是企業應用的重頭戲 - RAG（Retrieval Augmented Generation）。

Spring AI 1.1 對 RAG 的定義：一種被稱為「檢索增強生成」的技術，RAG 用來將相關數據加入提示詞，以獲得準確的回覆內容，有效減少 AI 的幻覺現象。

## Spring AI 1.1 中的 RAG 架構革新

### 模組化 RAG 設計理念

Spring AI 1.1 實現了受論文「[Modular RAG: Transforming RAG Systems into LEGO-like Reconfigurable Frameworks](https://arxiv.org/abs/2407.21059)」啟發的模組化 RAG 架構，將 RAG 系統設計成像樂高積木一樣可重構的框架。

![Spring AI 1.1 RAG架構](https://ithelp.ithome.com.tw/upload/images/20240824/20161290mp7HNCEqdf.jpg)

### RAG 的兩個主要階段

#### 1. RAG ETL 階段（離線處理）

這個階段採用批次運作的方式，從檔案中讀取非結構化數據，對其進行 Embedding，然後將內容與 Embeddings 一起寫入向量資料庫。

#### 2. RAG Runtime 階段（即時處理）

當 AI 模型要回答使用者問題時，問題和所有「相似」的 Chunks 都會被放入提示詞中，透過向量資料庫快速找到相似內容。

## Spring AI 1.1 的 Advisor API 架構

### 核心 Advisor 介面

```java
// 基礎 Advisor 介面
public interface Advisor extends Ordered {
    String getName();
}

// 同步處理的 CallAdvisor
public interface CallAdvisor extends Advisor {
    ChatClientResponse adviseCall(
        ChatClientRequest chatClientRequest, 
        CallAdvisorChain callAdvisorChain
    );
}

// 流式處理的 StreamAdvisor
public interface StreamAdvisor extends Advisor {
    Flux<ChatClientResponse> adviseStream(
        ChatClientRequest chatClientRequest, 
        StreamAdvisorChain streamAdvisorChain
    );
}
```

### RAG 專用的 Advisor 實現

Spring AI 1.1 提供了多種 RAG Advisor：

1. **QuestionAnswerAdvisor** - 傳統的樸素 RAG 實現
2. **RetrievalAugmentationAdvisor** - 模組化的進階 RAG 實現
3. **VectorStoreChatMemoryAdvisor** - 向量記憶 RAG

## QuestionAnswerAdvisor 的最新用法

### 基本配置

```java
@Configuration
public class RAGConfig {
    
    @Bean
    public ChatClient ragChatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        return builder
            .defaultAdvisors(
                QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.builder()
                        .similarityThreshold(0.8)
                        .topK(6)
                        .build())
                    .build()
            )
            .build();
    }
}
```

### 動態過濾器表達式

```java
@Service
@RequiredArgsConstructor
public class EnhancedRAGService {
    
    private final ChatClient chatClient;
    
    public String searchByCategory(String question, String category) {
        return chatClient.prompt()
            .user(question)
            .advisors(a -> a.param(
                QuestionAnswerAdvisor.FILTER_EXPRESSION, 
                String.format("category == '%s'", category)
            ))
            .call()
            .content();
    }
    
    public String searchByMultipleFilters(String question, Map<String, String> filters) {
        String filterExpression = filters.entrySet().stream()
            .map(entry -> String.format("%s == '%s'", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining(" && "));
            
        return chatClient.prompt()
            .user(question)
            .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, filterExpression))
            .call()
            .content();
    }
}
```

### 自訂 RAG 模板

```java
@Bean
public QuestionAnswerAdvisor customRAGAdvisor(VectorStore vectorStore) {
    PromptTemplate customTemplate = PromptTemplate.builder()
        .renderer(StTemplateRenderer.builder()
            .startDelimiterToken('<')
            .endDelimiterToken('>')
            .build())
        .template("""
            用戶問題: <query>
            
            相關資料:
            ========================
            <question_answer_context>
            ========================
            
            請根據上述資料回答問題，並遵循以下規則：
            1. 如果資料中沒有答案，請明確說明「資料中沒有相關資訊」
            2. 不要使用「根據提供的資料」等措辭
            3. 回答要準確且簡潔
            4. 如果可能，請提供資料來源
            """)
        .build();
        
    return QuestionAnswerAdvisor.builder(vectorStore)
        .promptTemplate(customTemplate)
        .searchRequest(SearchRequest.builder()
            .similarityThreshold(0.75)
            .topK(8)
            .build())
        .build();
}
```

## RetrievalAugmentationAdvisor 進階 RAG 實現

### 樸素 RAG 實現

```java
@Service
public class ModularRAGService {
    
    public String performNaiveRAG(String question, VectorStore vectorStore) {
        Advisor retrievalAdvisor = RetrievalAugmentationAdvisor.builder()
            .documentRetriever(VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.50)
                .vectorStore(vectorStore)
                .build())
            .build();
            
        return ChatClient.create(chatModel)
            .prompt()
            .advisors(retrievalAdvisor)
            .user(question)
            .call()
            .content();
    }
}
```

### 進階 RAG 實現（含查詢轉換）

```java
@Service
public class AdvancedRAGService {
    
    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;
    
    public String performAdvancedRAG(String question) {
        Advisor retrievalAdvisor = RetrievalAugmentationAdvisor.builder()
            .queryTransformers(
                // 查詢重寫轉換器
                RewriteQueryTransformer.builder()
                    .chatClientBuilder(chatClientBuilder.mutate())
                    .build(),
                // 查詢壓縮轉換器（適用於對話歷史）
                CompressionQueryTransformer.builder()
                    .chatClientBuilder(chatClientBuilder.mutate())
                    .build()
            )
            .documentRetriever(VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.60)
                .vectorStore(vectorStore)
                .build())
            .queryAugmenter(ContextualQueryAugmenter.builder()
                .allowEmptyContext(false) // 不允許空上下文
                .build())
            .build();
            
        return ChatClient.create(chatModel)
            .prompt()
            .advisors(retrievalAdvisor)
            .user(question)
            .call()
            .content();
    }
}
```

### 自訂文件後處理器

```java
@Component
public class CustomDocumentPostProcessor implements DocumentPostProcessor {
    
    @Override
    public List<Document> process(List<Document> documents) {
        return documents.stream()
            // 按相關性排序
            .sorted((d1, d2) -> compareRelevance(d1, d2))
            // 移除重複內容
            .filter(this::isNotDuplicate)
            // 壓縮內容
            .map(this::compressContent)
            // 限制返回數量
            .limit(5)
            .toList();
    }
    
    private int compareRelevance(Document d1, Document d2) {
        Double score1 = (Double) d1.getMetadata().get("distance");
        Double score2 = (Double) d2.getMetadata().get("distance");
        return Double.compare(score2, score1);
    }
    
    private boolean isNotDuplicate(Document doc) {
        // 實現重複內容檢測邏輯
        return true;
    }
    
    private Document compressContent(Document doc) {
        // 實現內容壓縮邏輯
        String compressedContent = doc.getContent().length() > 500 
            ? doc.getContent().substring(0, 500) + "..."
            : doc.getContent();
            
        return new Document(compressedContent, doc.getMetadata());
    }
}
```

## Spring AI 1.1 的 ETL Pipeline 架構

### 核心 ETL 介面

```java
// 文件讀取器 - 從各種來源讀取文件
public interface DocumentReader extends Supplier<List<Document>> {
    default List<Document> read() {
        return get();
    }
}

// 文件轉換器 - 處理文件分割和轉換
public interface DocumentTransformer extends Function<List<Document>, List<Document>> {
    default List<Document> transform(List<Document> documents) {
        return apply(documents);
    }
}

// 文件寫入器 - 將文件寫入向量資料庫
public interface DocumentWriter extends Consumer<List<Document>> {
    default void write(List<Document> documents) {
        accept(documents);
    }
}
```

### 完整的 ETL Pipeline 實現

```java
@Service
@RequiredArgsConstructor
public class EnhancedETLService {
    
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    
    public void processDocuments(Resource resource) {
        // 讀取階段 - 支援多種文件格式
        DocumentReader reader = createDocumentReader(resource);
        
        // 轉換階段 - 文件分割和增強
        DocumentTransformer splitter = new TokenTextSplitter(
            TokenTextSplitter.builder()
                .chunkSize(1000)
                .chunkOverlap(200)
                .keepSeparator(true)
                .build()
        );
        
        DocumentTransformer enhancer = new MetadataEnhancerTransformer();
        
        // 寫入階段
        DocumentWriter writer = vectorStore;
        
        // 執行 ETL Pipeline
        List<Document> documents = reader.read();
        List<Document> chunkedDocs = splitter.transform(documents);
        List<Document> enhancedDocs = enhancer.transform(chunkedDocs);
        writer.write(enhancedDocs);
        
        log.info("Successfully processed {} documents into vector store", enhancedDocs.size());
    }
    
    private DocumentReader createDocumentReader(Resource resource) {
        String filename = resource.getFilename();
        
        if (filename.endsWith(".pdf")) {
            return new PagePdfDocumentReader(resource);
        } else if (filename.endsWith(".json")) {
            return new JsonReader(resource);
        } else if (filename.endsWith(".txt")) {
            return new TextReader(resource);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + filename);
        }
    }
}

// 自訂文件轉換器 - 增強 Metadata
@Component
public class MetadataEnhancerTransformer implements DocumentTransformer {
    
    @Override
    public List<Document> apply(List<Document> documents) {
        return documents.stream()
            .map(this::enhanceMetadata)
            .toList();
    }
    
    private Document enhanceMetadata(Document doc) {
        Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
        
        // 添加處理時間戳
        metadata.put("processed_at", Instant.now().toString());
        
        // 添加內容統計
        metadata.put("word_count", countWords(doc.getContent()));
        metadata.put("char_count", doc.getContent().length());
        
        // 添加語言檢測
        metadata.put("language", detectLanguage(doc.getContent()));
        
        // 添加內容摘要
        metadata.put("summary", generateSummary(doc.getContent()));
        
        return new Document(doc.getContent(), metadata);
    }
    
    private int countWords(String content) {
        return content.split("\\s+").length;
    }
    
    private String detectLanguage(String content) {
        // 實現語言檢測邏輯
        return content.matches(".*[\\u4e00-\\u9fff].*") ? "zh" : "en";
    }
    
    private String generateSummary(String content) {
        // 實現內容摘要生成
        return content.length() > 100 
            ? content.substring(0, 100) + "..."
            : content;
    }
}
```

## 企業級 RAG 最佳實踐

### 1. 混合檢索策略

```java
@Service
public class HybridRetrievalService {
    
    private final VectorStore vectorStore;
    private final KeywordSearchService keywordSearchService;
    
    public String hybridSearch(String question) {
        // 向量搜尋
        List<Document> vectorResults = vectorStore.similaritySearch(
            SearchRequest.query(question).withTopK(5)
        );
        
        // 關鍵字搜尋
        List<Document> keywordResults = keywordSearchService.search(question, 5);
        
        // 合併和重新排序結果
        List<Document> combinedResults = combineAndRank(vectorResults, keywordResults);
        
        return generateResponseWithDocuments(question, combinedResults);
    }
    
    private List<Document> combineAndRank(List<Document> vectorResults, 
                                         List<Document> keywordResults) {
        Map<String, Document> uniqueDocs = new HashMap<>();
        
        // 添加向量搜尋結果（權重 0.7）
        vectorResults.forEach(doc -> {
            String key = doc.getContent().hashCode() + "";
            doc.getMetadata().put("vector_score", 0.7);
            uniqueDocs.put(key, doc);
        });
        
        // 添加關鍵字搜尋結果（權重 0.3）
        keywordResults.forEach(doc -> {
            String key = doc.getContent().hashCode() + "";
            if (uniqueDocs.containsKey(key)) {
                // 合併分數
                double vectorScore = (Double) uniqueDocs.get(key).getMetadata().get("vector_score");
                doc.getMetadata().put("combined_score", vectorScore + 0.3);
            } else {
                doc.getMetadata().put("combined_score", 0.3);
            }
            uniqueDocs.put(key, doc);
        });
        
        return uniqueDocs.values().stream()
            .sorted((d1, d2) -> {
                Double score1 = (Double) d1.getMetadata().get("combined_score");
                Double score2 = (Double) d2.getMetadata().get("combined_score");
                return Double.compare(score2, score1);
            })
            .limit(8)
            .toList();
    }
}
```

### 2. 階層式 RAG 架構

```java
@Service
public class HierarchicalRAGService {
    
    private final ChatClient summaryClient;  // 用於總結
    private final ChatClient detailClient;   // 用於詳細回答
    
    public String performHierarchicalRAG(String question) {
        // 第一階段：快速總結
        String initialResponse = summaryClient.prompt()
            .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(3).build())
                .build())
            .user("請簡要回答：" + question)
            .call()
            .content();
            
        // 判斷是否需要更詳細的回答
        if (needsDetailedResponse(initialResponse, question)) {
            // 第二階段：詳細分析
            return detailClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.builder().topK(10).build())
                    .build())
                .user(String.format("""
                    基於初始回答：%s
                    
                    請提供更詳細的回答：%s
                    """, initialResponse, question))
                .call()
                .content();
        }
        
        return initialResponse;
    }
    
    private boolean needsDetailedResponse(String response, String question) {
        // 實現判斷邏輯
        return response.length() < 100 || 
               response.contains("需要更多資訊") ||
               question.contains("詳細") || 
               question.contains("具體");
    }
}
```

### 3. 多輪對話 RAG

```java
@Service
public class ConversationalRAGService {
    
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final VectorStore vectorStore;
    
    public String conversationalRAG(String conversationId, String question) {
        return chatClient.prompt()
            .advisors(
                // 對話記憶
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                // RAG 檢索
                QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.builder()
                        .topK(5)
                        .similarityThreshold(0.7)
                        .build())
                    .build()
            )
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .user(question)
            .call()
            .content();
    }
}
```

## 效能監控與最佳化

### RAG Pipeline 監控

```java
@Component
public class RAGMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public RAGMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @EventListener
    public void handleRAGQuery(RAGQueryEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // 記錄查詢延遲
            sample.stop(Timer.builder("rag.query.duration")
                .tag("type", event.getQueryType())
                .tag("status", "success")
                .register(meterRegistry));
                
            // 記錄檢索文件數量
            meterRegistry.gauge("rag.retrieved.documents", 
                event.getRetrievedDocuments().size());
                
            // 記錄向量相似度分佈
            event.getRetrievedDocuments().forEach(doc -> {
                Double similarity = (Double) doc.getMetadata().get("distance");
                meterRegistry.gauge("rag.similarity.score", similarity);
            });
            
        } catch (Exception e) {
            sample.stop(Timer.builder("rag.query.duration")
                .tag("type", event.getQueryType())
                .tag("status", "error")
                .register(meterRegistry));
        }
    }
}
```

## 回顧

今天學到了什麼？

- 了解 Spring AI 1.1 中模組化 RAG 架構的設計理念
- 掌握最新的 Advisor API 和 RAG 實現方式
- 學會使用 QuestionAnswerAdvisor 和 RetrievalAugmentationAdvisor
- 了解 ETL Pipeline 的核心介面和實現
- 實作企業級的混合檢索和階層式 RAG 策略
- 建立 RAG 系統的效能監控機制

> Spring AI 1.1 的模組化 RAG 架構讓開發者能夠像組合樂高積木一樣建構靈活且強大的 RAG 系統，滿足不同企業場景的需求。

> 明天我們將深入探討 ETL Pipeline 的實際應用，學習如何處理各種文件格式和資料來源。

## Source Code

程式碼範例：[https://github.com/kevintsai1202/SpringBoot-AI-Day24-Updated.git](https://github.com/kevintsai1202/SpringBoot-AI-Day24-Updated.git)