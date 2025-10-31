# 使用 Spring AI 打造企業 RAG 知識庫【25】- Spring AI 1.1 進階 RAG 系統實戰

## 企業級 RAG 系統架構

今天我們將使用 Spring AI 1.1 的最新功能，建構一個完整的企業級 RAG 知識庫系統，整合多種 Advisor、向量資料庫和進階優化技術。

## 完整的企業級 RAG 系統

### 1. 系統架構配置

```java
@Configuration
@EnableConfigurationProperties(RAGProperties.class)
public class EnterpriseRAGConfiguration {
    
    @Bean
    public ChatClient enterpriseRAGClient(
            ChatClient.Builder builder,
            VectorStore vectorStore,
            ChatMemory chatMemory,
            DocumentRetriever documentRetriever,
            @Qualifier("rerankingModel") EmbeddingModel rerankingModel) {
        
        return builder
            .defaultSystem("""
                你是一個企業級 AI 知識助手，具備以下能力：
                1. 檢索企業知識庫並提供準確答案
                2. 記住對話歷史，提供連貫的對話體驗
                3. 根據檢索到的文檔提供有引用的專業回答
                4. 當資訊不確定時，明確告知用戶
                
                回答時請遵循以下格式：
                - 直接回答用戶問題
                - 在答案末尾註明資料來源
                - 如果沒有找到相關資訊，請誠實告知
                """)
            .defaultAdvisors(
                // 1. 對話記憶管理 - 最高優先級
                MessageChatMemoryAdvisor.builder(chatMemory)
                    .order(1)
                    .build(),
                    
                // 2. 智能文檔檢索 - 第二優先級
                QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.defaults()
                        .withTopK(10)  // 初始檢索更多文檔
                        .withSimilarityThreshold(0.7))
                    .order(2)
                    .build(),
                    
                // 3. 重排序優化 - 第三優先級
                new RerankingAdvisor(rerankingModel, 5),
                
                // 4. 答案質量控制 - 第四優先級
                new AnswerQualityAdvisor(),
                
                // 5. 日誌和監控 - 最低優先級
                SimpleLoggerAdvisor.builder()
                    .order(100)
                    .build()
            )
            .build();
    }
}
```

### 2. 自定義重排序 Advisor

```java
@Component
@Slf4j
public class RerankingAdvisor implements CallAdvisor {
    
    private final EmbeddingModel rerankingModel;
    private final int finalTopK;
    
    public RerankingAdvisor(EmbeddingModel rerankingModel, int finalTopK) {
        this.rerankingModel = rerankingModel;
        this.finalTopK = finalTopK;
    }
    
    @Override
    public String getName() {
        return "RerankingAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 3;
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // 取得原始檢索結果
        ChatClientResponse response = chain.nextCall(request);
        
        // 檢查是否有檢索到的文檔
        List<Document> retrievedDocs = extractRetrievedDocuments(response);
        if (retrievedDocs.isEmpty()) {
            return response;
        }
        
        // 執行重排序
        String query = request.prompt().getUserMessage().getContent();
        List<Document> rerankedDocs = rerank(query, retrievedDocs);
        
        // 更新回應中的文檔資訊
        return updateResponseWithRerankedDocs(response, rerankedDocs);
    }
    
    private List<Document> rerank(String query, List<Document> documents) {
        try {
            // 計算查詢和文檔的相關性分數
            List<ScoredDocument> scoredDocs = documents.stream()
                .map(doc -> {
                    double score = calculateRelevanceScore(query, doc.getContent());
                    return new ScoredDocument(doc, score);
                })
                .sorted((a, b) -> Double.compare(b.score(), a.score())) // 降序排列
                .limit(finalTopK)
                .toList();
                
            log.info("Reranked {} documents, selected top {}", documents.size(), finalTopK);
            
            return scoredDocs.stream()
                .map(ScoredDocument::document)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.warn("Reranking failed, using original order", e);
            return documents.subList(0, Math.min(finalTopK, documents.size()));
        }
    }
    
    private double calculateRelevanceScore(String query, String content) {
        // 使用 embedding 計算語義相似度
        List<Double> queryEmbedding = rerankingModel.embed(query);
        List<Double> contentEmbedding = rerankingModel.embed(content);
        
        return cosineSimilarity(queryEmbedding, contentEmbedding);
    }
    
    private double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
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
    
    record ScoredDocument(Document document, double score) {}
}
```

### 3. 答案質量控制 Advisor

```java
@Component
public class AnswerQualityAdvisor implements CallAdvisor {
    
    private final ChatClient qualityChecker;
    
    public AnswerQualityAdvisor(ChatClient.Builder builder) {
        this.qualityChecker = builder
            .defaultSystem("""
                你是一個答案質量評估專家。請評估給定答案的質量，並提供改進建議。
                評估標準：
                1. 準確性：答案是否基於提供的文檔內容
                2. 完整性：答案是否充分回答了問題
                3. 清晰度：答案是否易於理解
                4. 相關性：答案是否切中問題要點
                
                如果答案質量不佳，請提供改進後的答案。
                """)
            .build();
    }
    
    @Override
    public String getName() {
        return "AnswerQualityAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 4;
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientResponse originalResponse = chain.nextCall(request);
        
        // 評估答案質量
        String originalAnswer = originalResponse.chatResponse().getResult().getOutput().getContent();
        String question = request.prompt().getUserMessage().getContent();
        
        QualityAssessment assessment = assessAnswerQuality(question, originalAnswer);
        
        if (assessment.needsImprovement()) {
            log.info("Answer quality below threshold, attempting improvement");
            String improvedAnswer = improveAnswer(question, originalAnswer, assessment);
            return createImprovedResponse(originalResponse, improvedAnswer);
        }
        
        return originalResponse;
    }
    
    private QualityAssessment assessAnswerQuality(String question, String answer) {
        String assessmentPrompt = String.format("""
            問題：%s
            答案：%s
            
            請評估此答案的質量分數（0-100），並說明是否需要改進。
            回答格式：分數: XX, 需改進: true/false, 理由: XXX
            """, question, answer);
            
        String evaluation = qualityChecker.prompt()
            .user(assessmentPrompt)
            .call()
            .content();
            
        return parseQualityAssessment(evaluation);
    }
    
    private QualityAssessment parseQualityAssessment(String evaluation) {
        // 解析評估結果（簡化實現）
        boolean needsImprovement = evaluation.contains("需改進: true");
        int score = extractScore(evaluation);
        
        return new QualityAssessment(score, needsImprovement, evaluation);
    }
    
    private String improveAnswer(String question, String originalAnswer, QualityAssessment assessment) {
        String improvementPrompt = String.format("""
            原始問題：%s
            原始答案：%s
            質量評估：%s
            
            請根據評估結果改進答案，使其更準確、完整和清晰。
            """, question, originalAnswer, assessment.reason());
            
        return qualityChecker.prompt()
            .user(improvementPrompt)
            .call()
            .content();
    }
    
    record QualityAssessment(int score, boolean needsImprovement, String reason) {}
}
```

### 4. 企業級 RAG 控制器

```java
@RestController
@RequestMapping("/api/enterprise-rag")
@Validated
@Slf4j
public class EnterpriseRAGController {
    
    private final ChatClient enterpriseRAGClient;
    private final ChatMemory chatMemory;
    private final DocumentService documentService;
    private final RAGMetricsService metricsService;
    
    @PostMapping("/chat")
    public ResponseEntity<EnterpriseRAGResponse> chat(
            @RequestBody @Valid RAGChatRequest request) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            ChatClientResponse response = enterpriseRAGClient.prompt()
                .user(request.question())
                .advisors(advisor -> {
                    advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId());
                    if (request.searchConfig() != null) {
                        advisor.param("search_threshold", request.searchConfig().threshold());
                        advisor.param("search_top_k", request.searchConfig().topK());
                    }
                })
                .call()
                .chatClientResponse();
                
            EnterpriseRAGResponse ragResponse = buildRAGResponse(response, request);
            
            // 記錄成功指標
            metricsService.recordSuccess(System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(ragResponse);
            
        } catch (Exception e) {
            log.error("RAG chat failed for conversation {}", request.conversationId(), e);
            metricsService.recordError(e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EnterpriseRAGResponse.builder()
                    .answer("抱歉，處理您的問題時發生錯誤，請稍後再試。")
                    .error(e.getMessage())
                    .build());
        }
    }
    
    @PostMapping("/stream")
    public Flux<ServerSentEvent<RAGStreamChunk>> streamChat(
            @RequestBody @Valid RAGChatRequest request) {
        
        return enterpriseRAGClient.prompt()
            .user(request.question())
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
            .stream()
            .chatClientResponse()
            .map(response -> {
                String content = response.chatResponse().getResult().getOutput().getContent();
                List<CitedDocument> citations = extractCitations(response);
                
                return ServerSentEvent.<RAGStreamChunk>builder()
                    .data(RAGStreamChunk.builder()
                        .content(content)
                        .citations(citations)
                        .conversationId(request.conversationId())
                        .build())
                    .build();
            })
            .onErrorResume(error -> {
                log.error("Streaming error", error);
                return Flux.just(ServerSentEvent.<RAGStreamChunk>builder()
                    .data(RAGStreamChunk.builder()
                        .content("發生錯誤：" + error.getMessage())
                        .error(true)
                        .build())
                    .build());
            });
    }
    
    @PostMapping("/feedback")
    public ResponseEntity<Void> submitFeedback(
            @RequestBody @Valid RAGFeedback feedback) {
        
        metricsService.recordFeedback(feedback);
        
        // 如果回饋為負面，觸發改進流程
        if (feedback.rating() < 3) {
            documentService.flagForImprovement(feedback);
        }
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/analytics/{conversationId}")
    public ResponseEntity<RAGAnalytics> getAnalytics(@PathVariable String conversationId) {
        RAGAnalytics analytics = metricsService.getConversationAnalytics(conversationId);
        return ResponseEntity.ok(analytics);
    }
    
    private EnterpriseRAGResponse buildRAGResponse(ChatClientResponse response, RAGChatRequest request) {
        return EnterpriseRAGResponse.builder()
            .answer(response.chatResponse().getResult().getOutput().getContent())
            .conversationId(request.conversationId())
            .citations(extractCitations(response))
            .confidence(calculateConfidence(response))
            .processingTimeMs(extractProcessingTime(response))
            .sources(extractSources(response))
            .build();
    }
}
```

### 5. 資料模型定義

```java
public record RAGChatRequest(
    @NotBlank String question,
    @NotBlank String conversationId,
    SearchConfig searchConfig,
    Map<String, Object> context
) {}

public record SearchConfig(
    @Min(1) @Max(20) Integer topK,
    @DecimalMin("0.0") @DecimalMax("1.0") Double threshold,
    List<String> filters
) {}

@Builder
public record EnterpriseRAGResponse(
    String answer,
    String conversationId,
    List<CitedDocument> citations,
    Double confidence,
    Long processingTimeMs,
    List<DocumentSource> sources,
    String error
) {}

@Builder
public record CitedDocument(
    String content,
    String source,
    Double relevanceScore,
    Map<String, Object> metadata
) {}

@Builder
public record RAGStreamChunk(
    String content,
    List<CitedDocument> citations,
    String conversationId,
    Boolean error
) {}

public record RAGFeedback(
    String conversationId,
    String questionId,
    @Min(1) @Max(5) Integer rating,
    String comment,
    List<String> issues
) {}

@Builder
public record RAGAnalytics(
    String conversationId,
    Integer totalQuestions,
    Double averageConfidence,
    Double averageProcessingTime,
    Map<String, Integer> topicDistribution,
    List<String> frequentQuestions
) {}
```

### 6. 配置檔案

```yaml
# application.yml
app:
  rag:
    enabled: true
    search:
      default-top-k: 5
      default-threshold: 0.8
      max-top-k: 20
    
    quality:
      min-confidence: 0.7
      enable-improvement: true
      
    reranking:
      enabled: true
      model: sentence-transformers/all-MiniLM-L6-v2
      
    monitoring:
      metrics-enabled: true
      slow-query-threshold: 5000
      
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      embedding:
        model: text-embedding-3-large
        
    vectorstore:
      pgvector:
        url: jdbc:postgresql://localhost:5432/vectordb
        username: postgres
        password: password
        table-name: enterprise_documents
        dimensions: 3072  # text-embedding-3-large 維度
        
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,rag-metrics
        
logging:
  level:
    org.springframework.ai: DEBUG
    com.enterprise.rag: INFO
```

## 總結

今天我們建構了一個完整的企業級 RAG 系統，包含：

1. **多層 Advisor 架構**：記憶、檢索、重排序、質量控制
2. **智能重排序**：提升檢索結果的相關性
3. **答案質量控制**：自動改進低質量答案
4. **完整的 API 設計**：支援同步、流式、回饋機制
5. **監控和分析**：全面的性能指標追蹤

這個系統展示了 Spring AI 1.1 在企業級應用中的強大能力和靈活性。

## Source Code

今日程式碼: [https://github.com/kevintsai1202/SpringBoot-AI-Day25.git](https://github.com/kevintsai1202/SpringBoot-AI-Day25.git)