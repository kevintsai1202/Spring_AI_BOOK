# 使用 Spring AI 打造企業 RAG 知識庫【18】- Spring AI 1.1 企業級記憶與向量檢索

## 動手寫程式的機會越來越少了

![https://ithelp.ithome.com.tw/upload/images/20240818/20161290r2Eu5opGWt.png](https://ithelp.ithome.com.tw/upload/images/20240818/20161290r2Eu5opGWt.png)

使用 Spring Boot 開發程式真的很快，透過 Lombok 不用寫 Getter / Setter / Constructor，現在 Spring AI 1.1 連記憶儲存、向量檢索的類別都不讓我們動手，在不努力點就真的要被 AI 取代了XD

Spring AI 1.1 提供了完整的企業級記憶管理系統，包含多種儲存後端和進階功能。

## Spring AI 1.1 記憶系統架構

### 1. ChatMemory 核心介面

Spring AI 1.1 提供了更完善的 ChatMemory 體系：

```java
public interface ChatMemory {
    // 新增訊息到對話
    void add(String conversationId, Message message);
    void add(String conversationId, List<Message> messages);
    
    // 取得對話歷史
    List<Message> get(String conversationId);
    List<Message> get(String conversationId, int lastN);
    
    // 清除對話
    void clear(String conversationId);
    
    // 檢查對話是否存在
    boolean exists(String conversationId);
}
```

### 2. MessageWindowChatMemory 實現

Spring AI 1.1 的主要實現，支援滑動視窗記憶：

```java
@Configuration
public class ChatMemoryConfiguration {
    
    @Bean
    public ChatMemory messageWindowChatMemory(ChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
            .maxMessages(100)        // 最多保留100條訊息
            .repository(repository)  // 指定儲存後端
            .build();
    }
    
    // 記憶體儲存（開發環境）
    @Bean
    @Profile("dev")
    public ChatMemoryRepository inMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }
    
    // 資料庫儲存（生產環境）
    @Bean
    @Profile("prod")
    public ChatMemoryRepository jdbcRepository(DataSource dataSource) {
        return new JdbcChatMemoryRepository(dataSource);
    }
}
```

### 3. 多種儲存後端支援

```java
@Configuration
public class MemoryStorageConfig {
    
    // JDBC 儲存
    @Bean
    @ConditionalOnProperty(name = "spring.ai.memory.store", havingValue = "jdbc")
    public ChatMemoryRepository jdbcChatMemoryRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcChatMemoryRepository(jdbcTemplate);
    }
    
    // Cassandra 儲存
    @Bean
    @ConditionalOnProperty(name = "spring.ai.memory.store", havingValue = "cassandra")
    public ChatMemoryRepository cassandraChatMemoryRepository(CassandraTemplate template) {
        return new CassandraChatMemoryRepository(template);
    }
    
    // Neo4j 儲存（新增支援）
    @Bean
    @ConditionalOnProperty(name = "spring.ai.memory.store", havingValue = "neo4j")
    public ChatMemoryRepository neo4jChatMemoryRepository(Neo4jTemplate template) {
        return new Neo4jChatMemoryRepository(template);
    }
}
```

## 整合 RAG 與記憶功能

### 1. 完整的企業級對話系統

```java
@Configuration
public class EnterpriseAIConfig {
    
    @Bean
    public ChatClient enterpriseChatClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            VectorStore vectorStore) {
        return builder
            .defaultSystem("""
                你是一個企業 AI 助手，具備以下能力：
                1. 記住對話歷史
                2. 檢索企業知識庫
                3. 提供準確的技術支援
                
                請根據對話歷史和檢索到的文檔提供專業回答。
                """)
            .defaultAdvisors(
                // 記憶管理 - 最高優先級
                MessageChatMemoryAdvisor.builder(chatMemory)
                    .order(Ordered.HIGHEST_PRECEDENCE)
                    .build(),
                    
                // RAG 檢索 - 中等優先級
                QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.defaults()
                        .withTopK(5)
                        .withSimilarityThreshold(0.8))
                    .order(Ordered.HIGHEST_PRECEDENCE + 100)
                    .build(),
                    
                // 日誌記錄 - 最低優先級
                SimpleLoggerAdvisor.builder()
                    .order(Ordered.LOWEST_PRECEDENCE)
                    .build()
            )
            .build();
    }
}
```

### 2. 高級對話控制器

```java
@RestController
@RequestMapping("/api/enterprise")
@Validated
public class EnterpriseAIController {
    
    private final ChatClient enterpriseChatClient;
    private final ChatMemory chatMemory;
    
    public EnterpriseAIController(ChatClient enterpriseChatClient, ChatMemory chatMemory) {
        this.enterpriseChatClient = enterpriseChatClient;
        this.chatMemory = chatMemory;
    }
    
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody @Valid ChatRequest request) {
        
        ChatClientResponse response = enterpriseChatClient.prompt()
            .user(request.message())
            .advisors(advisor -> {
                advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId());
                // 可以動態設定其他參數
                if (request.searchThreshold() != null) {
                    advisor.param("similarity_threshold", request.searchThreshold());
                }
            })
            .call()
            .chatClientResponse();
            
        return ResponseEntity.ok(ChatResponse.builder()
            .content(response.chatResponse().getResult().getOutput().getContent())
            .conversationId(request.conversationId())
            .retrievedDocuments(extractRetrievedDocuments(response))
            .usage(response.chatResponse().getMetadata().getUsage())
            .build());
    }
    
    @PostMapping("/stream")
    public Flux<ServerSentEvent<String>> streamChat(@RequestBody @Valid ChatRequest request) {
        return enterpriseChatClient.prompt()
            .user(request.message())
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
            .stream()
            .content()
            .map(chunk -> ServerSentEvent.<String>builder()
                .data(chunk)
                .build());
    }
    
    @GetMapping("/conversations/{conversationId}/history")
    public ResponseEntity<ConversationHistory> getHistory(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "50") int limit) {
        
        List<Message> messages = chatMemory.get(conversationId, limit);
        return ResponseEntity.ok(ConversationHistory.builder()
            .conversationId(conversationId)
            .messages(messages.stream()
                .map(this::toHistoryMessage)
                .collect(Collectors.toList()))
            .build());
    }
    
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> clearConversation(@PathVariable String conversationId) {
        chatMemory.clear(conversationId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/conversations/{conversationId}/summarize")  
    public ResponseEntity<ConversationSummary> summarizeConversation(
            @PathVariable String conversationId) {
        
        List<Message> history = chatMemory.get(conversationId);
        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        String summaryPrompt = createSummaryPrompt(history);
        String summary = enterpriseChatClient.prompt()
            .user(summaryPrompt)
            .call()
            .content();
            
        return ResponseEntity.ok(ConversationSummary.builder()
            .conversationId(conversationId)
            .summary(summary)
            .messageCount(history.size())
            .build());
    }
    
    private List<RetrievedDocument> extractRetrievedDocuments(ChatClientResponse response) {
        // 從 advisor context 中提取檢索到的文檔
        return response.advisorContext().entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("retrieved_documents"))
            .flatMap(entry -> ((List<Document>) entry.getValue()).stream())
            .map(doc -> RetrievedDocument.builder()
                .content(doc.getContent())
                .metadata(doc.getMetadata())
                .build())
            .collect(Collectors.toList());
    }
    
    private HistoryMessage toHistoryMessage(Message message) {
        return HistoryMessage.builder()
            .role(message instanceof UserMessage ? "user" : "assistant")
            .content(message.getContent())
            .timestamp(Instant.now()) // 實際應從 metadata 取得
            .build();
    }
    
    private String createSummaryPrompt(List<Message> history) {
        String conversation = history.stream()
            .map(msg -> (msg instanceof UserMessage ? "用戶: " : "助手: ") + msg.getContent())
            .collect(Collectors.joining("\n"));
            
        return String.format("""
            請為以下對話提供簡潔的摘要：
            
            %s
            
            摘要應包含：
            1. 主要討論話題
            2. 重要決定或結論
            3. 待辦事項或後續行動
            """, conversation);
    }
}
```

### 3. 資料模型

```java
public record ChatRequest(
    @NotBlank String message,
    @NotBlank String conversationId,
    @DecimalMin("0.0") @DecimalMax("1.0") Double searchThreshold
) {}

@Builder
public record ChatResponse(
    String content,
    String conversationId,
    List<RetrievedDocument> retrievedDocuments,
    Usage usage
) {}

@Builder  
public record ConversationHistory(
    String conversationId,
    List<HistoryMessage> messages
) {}

@Builder
public record HistoryMessage(
    String role,
    String content,
    Instant timestamp
) {}

@Builder
public record ConversationSummary(
    String conversationId,
    String summary,
    int messageCount
) {}

@Builder
public record RetrievedDocument(
    String content,
    Map<String, Object> metadata
) {}
```

## 進階功能實現

### 1. 自定義記憶策略

```java
@Component
public class SmartMemoryAdvisor implements CallAdvisor {
    
    private final ChatMemory chatMemory;
    private final ChatClient summarizerClient;
    
    public SmartMemoryAdvisor(ChatMemory chatMemory, ChatClient summarizerClient) {
        this.chatMemory = chatMemory;
        this.summarizerClient = summarizerClient;
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String conversationId = (String) request.adviseContext()
            .get(ChatMemory.CONVERSATION_ID);
            
        List<Message> history = chatMemory.get(conversationId);
        
        // 如果對話太長，進行智能摘要
        if (history.size() > 50) {
            String summary = summarizeOldConversation(history.subList(0, 30));
            
            // 保留摘要 + 最近20條訊息
            List<Message> optimizedHistory = new ArrayList<>();
            optimizedHistory.add(new SystemMessage("對話摘要: " + summary));
            optimizedHistory.addAll(history.subList(history.size() - 20, history.size()));
            
            // 更新記憶
            chatMemory.clear(conversationId);
            chatMemory.add(conversationId, optimizedHistory);
        }
        
        return chain.nextCall(request);
    }
    
    private String summarizeOldConversation(List<Message> messages) {
        String conversation = messages.stream()
            .map(Message::getContent)
            .collect(Collectors.joining("\n"));
            
        return summarizerClient.prompt()
            .user("請簡潔摘要以下對話的重點: " + conversation)
            .call()
            .content();
    }
    
    @Override
    public String getName() { return "SmartMemoryAdvisor"; }
    
    @Override
    public int getOrder() { return 1; }
}
```

### 2. 配置檔案

```yaml
# application.yml
spring:
  ai:
    memory:
      enabled: true
      store: jdbc  # 選項：memory, jdbc, cassandra, neo4j
      max-messages: 100
      cleanup-interval: 3600
      
    vectorstore:
      type: pgvector
      dimensions: 1536
      
logging:
  level:
    org.springframework.ai.chat.client.advisor: DEBUG
    
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,ai-memory
```

## 與舊版本的比較

### Spring AI 1.0（基礎版本）
```java
// 簡單的記憶實現
InMemoryChatMemory memory = new InMemoryChatMemory();
memory.add("conv1", List.of(new UserMessage("問題")));
```

### Spring AI 1.1（企業級）  
```java
// 完整的企業級解決方案
ChatClient client = ChatClient.builder(chatModel)
    .defaultAdvisors(
        MessageChatMemoryAdvisor.builder(chatMemory).build(),
        QuestionAnswerAdvisor.builder(vectorStore).build()
    )
    .build();
```

## 回顧

今天學到的內容：

1. **Spring AI 1.1 記憶系統**：MessageWindowChatMemory 和多種儲存後端
2. **企業級整合**：記憶 + RAG + 工具調用的完整解決方案
3. **進階功能**：對話摘要、智能記憶管理、文檔檢索
4. **實用 API**：完整的 REST API 設計和錯誤處理
5. **可觀測性**：內建監控和日誌功能

明天我們將深入向量資料庫的世界，探討如何建構真正高效的企業知識庫！

## Source Code

今日程式碼: [https://github.com/kevintsai1202/SpringBoot-AI-Day18.git](https://github.com/kevintsai1202/SpringBoot-AI-Day18.git)