# 使用 Spring AI 打造企業 RAG 知識庫【29】- RAG的最後一哩路

![RAG Pipeline](https://example.com/rag-pipeline.jpg)

## 所有的準備都是為了最後的演出

前面已詳述過 RAG 最主要的目的，就是提供 LLM 未知的資訊，而這些資料會使用 ETL 技術或是由企業資料庫中取得，經過 embedding 計算後存於向量資料庫，而詢問內容同樣也經過 embedding 後在去向量資料庫中比對，所得近似資料連同問題再一起提供給 LLM，利用 In-Context 的特性將資訊整理後將最終結果呈現出來。

![RAG Flow](https://example.com/rag-flow.jpg)

所以 RAG 最後的動作其實包含了查詢與內容生成，生成的部分與前面說的提示詞應用都大同小異，就是將內容附加在提示詞中，再由 LLM 分析產生最佳的內容，RAG 最關鍵的部分就是如何讓查詢更為精準。

## 優化RAG的查詢結果

昨天講的 Transformer 使用了 **關鍵字** 跟 **摘要** 讓內容更為精準，而查詢時也有許多手法能加強搜尋結果，這些方法還不斷在增加中，以下列出目前主要的優化技巧：

### 1. 排除近似查詢分數較低的結果

正所謂垃圾進垃圾出，若提供的分塊內容偏離主題太多，LLM 的回答就會有所偏離，此時可設定一個分數基準，低於分數的內容就屏棄不用，避免干擾 LLM 生成資料。

### 2. 關鍵字篩選

昨天的關鍵字除了能強化 embedding 分數外，在近似查詢前後也能使用關鍵字過濾內容讓得到的結果更進一步的精煉。

### 3. 重複驗證內容

避免 LLM 胡謅一通可以在生成資料後請 LLM 再驗證一次，可以使用不同的 LLM 來進行複驗讓資料更有公信力。

### 4. 建立上下關聯

同一份資料拆解前都有一定的相關性，分塊後將原始文件的資訊紀錄在 metadata 中，進行 embedding 時可以加強各維度的分數。

### 5. 使用 ReRank 二次過濾

使用 Embedding 做近似查詢雖然很快，但還是有命中率不太高的問題，於是就有 ReRank 進行二次篩選的模型。

ReRank 雖然處理速度較慢，但是比 Embedding 模型更為精準，我們就能使用 Embedding 先搜尋 50~100 筆近似資料，之後再透過 ReRank 從中找出 5~10 筆更為精準的資料，這樣不但兼顧速度又能有更準確的內容。

![ReRank Process](https://example.com/rerank-process.png)

### 6. 使用知識圖譜 (Graph RAG)

這是最近幾個月才由微軟提出的論文，將 ETL 後的資料以及詢問的內容都先經過資料梳理成多個 Entities，並將 Entity 之間建立 Relation，再對這些 Entities 進行 embedding，查詢時使用近似搜尋找出符合的 Entity，再透過 Graph 的特性找到相關的 Entity，最後 Entity 與相關內容整理後再交由 LLM 生成結果。

![Graph RAG](https://example.com/graph-rag.png)

## 程式實作

使用最新的 Spring AI 1.1-SNAPSHOT 實現 RAG，並使用現代化的 ChatClient API。

### RAG 搜尋服務

`RagSearchService.java`

```java
@Service
@RequiredArgsConstructor
public class RagSearchService {
    
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    
    public String search(String query) {
        // 使用最新的 Advisor API
        return chatClient.prompt()
            .user(query)
            .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
            .call()
            .content();
    }
    
    public String searchWithCustomParameters(String query, int topK, double threshold) {
        SearchRequest searchRequest = SearchRequest.defaults()
            .withTopK(topK)
            .withSimilarityThreshold(threshold);
            
        QuestionAnswerAdvisor advisor = new QuestionAnswerAdvisor(vectorStore, searchRequest);
        
        return chatClient.prompt()
            .user(query)
            .advisors(advisor)
            .call()
            .content();
    }
    
    public ChatResponse searchWithMetadata(String query) {
        return chatClient.prompt()
            .user(query)
            .advisors(new QuestionAnswerAdvisor(vectorStore))
            .call()
            .chatResponse();
    }
}
```

### 進階 RAG 實現

建立一個更進階的 RAG 實現，包含多種優化策略：

`AdvancedRagService.java`

```java
@Service
@RequiredArgsConstructor
public class AdvancedRagService {
    
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    
    public String advancedSearch(String query, RagConfig config) {
        return chatClient.prompt()
            .user(query)
            .advisors(createAdvancedAdvisor(config))
            .call()
            .content();
    }
    
    private RequestResponseAdvisor createAdvancedAdvisor(RagConfig config) {
        SearchRequest searchRequest = SearchRequest.defaults()
            .withTopK(config.getTopK())
            .withSimilarityThreshold(config.getSimilarityThreshold());
            
        if (config.hasMetadataFilters()) {
            searchRequest = searchRequest.withFilterExpression(
                parseFilterExpression(config.getMetadataFilters())
            );
        }
        
        return new QuestionAnswerAdvisor(vectorStore, searchRequest, config.getSystemPrompt());
    }
    
    // 混合搜索：結合向量搜索和關鍵字搜索
    public String hybridSearch(String query, RagConfig config) {
        // 1. 向量搜索
        List<Document> vectorResults = vectorStore.similaritySearch(
            SearchRequest.query(query).withTopK(config.getTopK() * 2)
        );
        
        // 2. 關鍵字過濾 (如果有的話)
        List<Document> filteredResults = filterByKeywords(vectorResults, query, config);
        
        // 3. 重新排序 (如果啟用ReRank)
        if (config.isEnableRerank()) {
            filteredResults = reRankDocuments(query, filteredResults, config.getTopK());
        }
        
        // 4. 建立自定義 Context
        String context = buildContextFromDocuments(filteredResults);
        
        // 5. 生成回答
        return chatClient.prompt()
            .system(config.getSystemPrompt())
            .user(buildUserPrompt(query, context))
            .call()
            .content();
    }
    
    private List<Document> filterByKeywords(List<Document> documents, String query, RagConfig config) {
        if (!config.isEnableKeywordFilter()) {
            return documents;
        }
        
        // 提取查詢關鍵字
        Set<String> queryKeywords = extractKeywords(query);
        
        return documents.stream()
            .filter(doc -> hasRelevantKeywords(doc, queryKeywords))
            .collect(Collectors.toList());
    }
    
    private List<Document> reRankDocuments(String query, List<Document> documents, int topK) {
        // 這裡可以整合外部 ReRank API，如 Cohere, Voyage AI 等
        // 或使用本地 ReRank 模型
        return documents.stream()
            .sorted((a, b) -> Double.compare(
                calculateRelevanceScore(query, b),
                calculateRelevanceScore(query, a)
            ))
            .limit(topK)
            .collect(Collectors.toList());
    }
    
    private double calculateRelevanceScore(String query, Document document) {
        // 簡單的相關性評分實現
        // 實際應用中應該使用更sophisticated的算法
        String content = document.getContent().toLowerCase();
        String queryLower = query.toLowerCase();
        
        long matchCount = Arrays.stream(queryLower.split("\\s+"))
            .mapToLong(word -> countOccurrences(content, word))
            .sum();
            
        return (double) matchCount / content.length();
    }
    
    private String buildContextFromDocuments(List<Document> documents) {
        return documents.stream()
            .map(doc -> {
                String content = doc.getContent();
                String source = doc.getMetadata().getOrDefault("source", "Unknown").toString();
                return String.format("來源: %s\n內容: %s", source, content);
            })
            .collect(Collectors.joining("\n\n---\n\n"));
    }
    
    private String buildUserPrompt(String query, String context) {
        return String.format("""
            基於以下資料回答問題。如果資料中沒有相關資訊，請明確說明。
            
            參考資料:
            %s
            
            問題: %s
            
            請提供準確且有幫助的回答:
            """, context, query);
    }
}
```

### RAG 配置類

`RagConfig.java`

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagConfig {
    
    @Builder.Default
    private int topK = 5;
    
    @Builder.Default
    private double similarityThreshold = 0.7;
    
    @Builder.Default
    private boolean enableRerank = false;
    
    @Builder.Default
    private boolean enableKeywordFilter = false;
    
    @Builder.Default
    private String systemPrompt = """
        你是一個專業的助手。請基於提供的上下文資訊回答用戶的問題。
        如果上下文中沒有相關資訊，請誠實地說明你不知道答案。
        """;
    
    private Map<String, Object> metadataFilters;
    
    public boolean hasMetadataFilters() {
        return metadataFilters != null && !metadataFilters.isEmpty();
    }
}
```

### 控制器實現

`RagController.java`

```java
@RestController
@RequestMapping("/ai/rag")
@RequiredArgsConstructor
public class RagController {
    
    private final RagSearchService ragSearchService;
    private final AdvancedRagService advancedRagService;
    
    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam String query) {
        try {
            String response = ragSearchService.search(query);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("搜尋失敗: " + e.getMessage());
        }
    }
    
    @PostMapping("/advanced-search")
    public ResponseEntity<String> advancedSearch(@RequestBody AdvancedSearchRequest request) {
        try {
            RagConfig config = RagConfig.builder()
                .topK(request.topK())
                .similarityThreshold(request.similarityThreshold())
                .enableRerank(request.enableRerank())
                .enableKeywordFilter(request.enableKeywordFilter())
                .systemPrompt(request.systemPrompt())
                .metadataFilters(request.metadataFilters())
                .build();
                
            String response = advancedRagService.advancedSearch(request.query(), config);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("進階搜尋失敗: " + e.getMessage());
        }
    }
    
    @GetMapping("/search-with-metadata")
    public ResponseEntity<RagResponse> searchWithMetadata(@RequestParam String query) {
        try {
            ChatResponse chatResponse = ragSearchService.searchWithMetadata(query);
            
            String content = chatResponse.getResult().getOutput().getContent();
            
            @SuppressWarnings("unchecked")
            List<Document> retrievedDocs = (List<Document>) chatResponse.getMetadata()
                .get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
            
            List<DocumentInfo> documentInfos = retrievedDocs.stream()
                .map(doc -> new DocumentInfo(
                    doc.getId(),
                    doc.getMetadata().getOrDefault("source", "Unknown").toString(),
                    doc.getContent().substring(0, Math.min(200, doc.getContent().length())) + "..."
                ))
                .collect(Collectors.toList());
            
            RagResponse response = new RagResponse(content, documentInfos);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RagResponse("搜尋失敗: " + e.getMessage(), List.of()));
        }
    }
    
    public record AdvancedSearchRequest(
        String query,
        int topK,
        double similarityThreshold,
        boolean enableRerank,
        boolean enableKeywordFilter,
        String systemPrompt,
        Map<String, Object> metadataFilters
    ) {}
    
    public record RagResponse(
        String answer,
        List<DocumentInfo> sources
    ) {}
    
    public record DocumentInfo(
        String id,
        String source,
        String preview
    ) {}
}
```

## 自定義 Advisor

如果需要更多控制權，可以建立自定義的 Advisor：

`CustomRagAdvisor.java`

```java
@Component
public class CustomRagAdvisor implements RequestResponseAdvisor {
    
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    
    public CustomRagAdvisor(VectorStore vectorStore, ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }
    
    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String userText = request.userText();
        
        // 1. 查詢預處理 - 使用AI優化查詢
        String optimizedQuery = optimizeQuery(userText);
        
        // 2. 執行搜索
        SearchRequest searchRequest = SearchRequest.query(optimizedQuery)
            .withTopK(10)
            .withSimilarityThreshold(0.6);
            
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        
        // 3. 文件後處理
        List<Document> processedDocs = postProcessDocuments(documents, optimizedQuery);
        
        // 4. 建立增強的上下文
        String enhancedContext = buildEnhancedContext(processedDocs);
        
        // 5. 更新請求
        Map<String, Object> updatedParams = new HashMap<>(request.userParams());
        updatedParams.put("enhanced_context", enhancedContext);
        
        String enhancedUserText = String.format("""
            基於以下相關資訊回答問題：
            
            %s
            
            用戶問題：%s
            """, enhancedContext, userText);
        
        return AdvisedRequest.from(request)
            .withUserText(enhancedUserText)
            .withUserParams(updatedParams)
            .build();
    }
    
    private String optimizeQuery(String originalQuery) {
        // 使用 AI 優化查詢，提取關鍵概念
        return chatClient.prompt()
            .system("""
                你是查詢優化專家。請將以下用戶查詢優化為更適合語義搜索的格式。
                提取關鍵概念和同義詞，但保持查詢簡潔。
                只返回優化後的查詢文本，不要額外說明。
                """)
            .user(originalQuery)
            .call()
            .content();
    }
    
    private List<Document> postProcessDocuments(List<Document> documents, String query) {
        // 可以在這裡實現：
        // - 去重
        // - 相關性重新排序
        // - 內容摘要
        // - 質量過濾
        
        return documents.stream()
            .filter(doc -> doc.getContent().length() > 50) // 過濾太短的內容
            .distinct()
            .limit(5)
            .collect(Collectors.toList());
    }
    
    private String buildEnhancedContext(List<Document> documents) {
        StringBuilder context = new StringBuilder();
        
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            context.append(String.format("參考資料 %d:\n", i + 1));
            
            // 添加來源資訊
            String source = doc.getMetadata().getOrDefault("source", "未知來源").toString();
            context.append(String.format("來源：%s\n", source));
            
            // 添加內容
            context.append(String.format("內容：%s\n\n", doc.getContent()));
        }
        
        return context.toString();
    }
    
    @Override
    public ChatResponse adviseResponse(ChatResponse response, Map<String, Object> context) {
        // 可以在這裡對回應進行後處理
        return response;
    }
}
```

## 效能監控和指標

`RagMetrics.java`

```java
@Component
@RequiredArgsConstructor
public class RagMetrics {
    
    private final MeterRegistry meterRegistry;
    
    private final Counter searchCounter;
    private final Timer searchTimer;
    private final Gauge activeSearches;
    
    @PostConstruct
    public void initMetrics() {
        this.searchCounter = Counter.builder("rag.search.total")
            .description("Total number of RAG searches")
            .register(meterRegistry);
            
        this.searchTimer = Timer.builder("rag.search.duration")
            .description("RAG search duration")
            .register(meterRegistry);
            
        this.activeSearches = Gauge.builder("rag.search.active")
            .description("Number of active RAG searches")
            .register(meterRegistry, this, RagMetrics::getActiveSearchCount);
    }
    
    public void recordSearch(Duration duration, String status) {
        searchCounter.increment(Tags.of("status", status));
        searchTimer.record(duration);
    }
    
    private double getActiveSearchCount() {
        // 實現活躍搜索計數邏輯
        return 0.0;
    }
}
```

## 驗證程式

### 測試基本搜索

```bash
curl "http://localhost:8080/ai/rag/search?query=Spring AI如何處理文件？"
```

### 測試進階搜索

```bash
curl -X POST http://localhost:8080/ai/rag/advanced-search \
-H "Content-Type: application/json" \
-d '{
    "query": "企業級RAG實現",
    "topK": 5,
    "similarityThreshold": 0.7,
    "enableRerank": true,
    "enableKeywordFilter": true,
    "systemPrompt": "你是企業AI助手",
    "metadataFilters": {"type": "technical"}
}'
```

### 測試帶元數據的搜索

```bash
curl "http://localhost:8080/ai/rag/search-with-metadata?query=向量資料庫配置"
```

## 回顧

總結一下今天學到了什麼？

- 使用最新的 Spring AI 1.1-SNAPSHOT ChatClient API
- 實現現代化的 RAG 搜索服務
- 掌握 QuestionAnswerAdvisor 的使用方法
- 建立進階 RAG 實現，包含多種優化策略
- 自定義 Advisor 的實現方式
- 混合搜索：結合向量搜索和關鍵字過濾
- 企業級 RAG 系統的架構設計
- 效能監控和指標收集

## Source Code

程式碼下載: [https://github.com/kevintsai1202/SpringBoot-AI-Day29-Updated.git](https://github.com/kevintsai1202/SpringBoot-AI-Day29-Updated.git)