# 使用 Spring AI 打造企業 RAG 知識庫【22】- 使用向量資料庫作為對話的長久記憶

> 以下內容已根據 Spring AI 1.1-SNAPSHOT 版本進行更新，使用最新的 ChatClient API 和 Advisor 系統

![向量記憶示意圖](https://ithelp.ithome.com.tw/upload/images/20240822/20161290QigCeVYJM5.jpg)

在傳統應用中，儲存對話只會透過 SQL 或 NoSQL 資料庫儲存對話內容，查詢資料也只能使用關鍵字搜尋，只要關鍵字不同就無法搜尋到內容。而向量資料庫會先將文字向量化再儲存，並使用語意相似性搜尋的方式讓我們可以將語意相近的內容也一併找出，除了更容易找出資料外也不會有數量限制，因為查詢的內容是根據整個對話的資料搜尋。

## Spring AI 1.1 中的記憶系統架構

在 Spring AI 1.1 版本中，記憶系統架構有了重大改進：

### ChatMemory 抽象層
```java
public interface ChatMemory {
    void add(String conversationId, List<Message> messages);
    List<Message> get(String conversationId);
    void clear(String conversationId);
}
```

### ChatMemoryRepository 儲存層
```java
public interface ChatMemoryRepository {
    void save(String conversationId, List<Message> messages);
    List<Message> findByConversationId(String conversationId);
    void deleteByConversationId(String conversationId);
}
```

## 記憶類型選擇

Spring AI 1.1 提供了三種主要的記憶 Advisor：

### 1. MessageChatMemoryAdvisor
將對話歷史作為 Message 集合加入到 Prompt 中，保持對話結構。

### 2. PromptChatMemoryAdvisor  
將對話歷史作為純文字附加到系統提示中。

### 3. VectorStoreChatMemoryAdvisor
使用向量資料庫儲存對話記憶，支援語意搜尋。

## 程式實作

### 目標: 使用最新的 VectorStoreChatMemoryAdvisor

在 Spring AI 1.1 版本中，VectorStoreChatMemoryAdvisor 的使用方式更加簡潔和穩定：

```java
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public String chat(String conversationId, String userMessage) {
        return this.chatClient.prompt()
            .advisors(a -> a
                .advisors(VectorStoreChatMemoryAdvisor.builder(vectorStore)
                    .build())
                .param(ChatMemory.CONVERSATION_ID, conversationId))
            .user(userMessage)
            .call()
            .content();
    }
}
```

### 或者在 ChatClient 建立時設定預設 Advisor

```java
@Configuration
public class ChatConfig {
    
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        return builder
            .defaultAdvisors(
                VectorStoreChatMemoryAdvisor.builder(vectorStore)
                    .maxRetrievalSize(100) // 最大檢索數量
                    .build()
            )
            .build();
    }
}
```

### 在服務中使用

```java
@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String conversationId, String userMessage) {
        return this.chatClient.prompt()
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .user(userMessage)
            .call()
            .content();
    }
}
```

## VectorStoreChatMemoryAdvisor 參數說明

- **vectorStore**: 向量資料庫實例
- **conversationId**: 對話識別 ID，用於過濾查詢資料
- **maxRetrievalSize**: 檢索時的最大返回數量（預設 100）

## 自訂記憶模板

VectorStoreChatMemoryAdvisor 支援自訂模板來控制記憶內容的格式：

```java
@Bean
public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore) {
    PromptTemplate customTemplate = new PromptTemplate("""
        {instructions}
        
        Previous conversation context:
        {long_term_memory}
        
        Please consider the conversation history when responding.
        """);
    
    return builder
        .defaultAdvisors(
            VectorStoreChatMemoryAdvisor.builder(vectorStore)
                .promptTemplate(customTemplate)
                .maxRetrievalSize(50)
                .build()
        )
        .build();
}
```

## 記憶儲存實現選擇

### 1. Neo4j Vector Store（推薦用於記憶）
```properties
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=password
```

### 2. Chroma Vector Store
```properties
spring.ai.vectorstore.chroma.baseUrl=http://localhost:8000
```

### 3. PostgreSQL with pgvector
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/vectordb
spring.ai.vectorstore.pgvector.dimensions=1536
```

## 驗收成果

使用更新後的系統進行測試：

```java
@RestController
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/chat")
    public String chat(@RequestParam String conversationId, 
                      @RequestParam String message) {
        return chatService.chat(conversationId, message);
    }
}
```

測試對話：
1. 第一輪對話："我的名字是Kevin"
2. 第二輪對話："我剛才說我的名字是什麼？"

系統應該能夠通過向量搜尋找到相關的對話內容並正確回答。

## Spring AI 1.1 中的記憶優勢

### 1. 改進的架構
- 清晰的抽象層分離
- 更好的可擴展性
- 統一的 Advisor 介面

### 2. 多種儲存選項
- InMemoryChatMemoryRepository
- JdbcChatMemoryRepository  
- CassandraChatMemoryRepository
- Neo4jChatMemoryRepository

### 3. 語意搜尋能力
- 支援相似性搜尋
- 自動過濾重複內容
- 可配置檢索數量

### 4. 模板系統
- 可自訂記憶格式
- 支援多種模板引擎
- 靈活的內容組合

## 記憶系統最佳實踐

### 1. 記憶類型選擇指南

```java
// 短期對話記憶 - 使用 MessageChatMemoryAdvisor
MessageChatMemoryAdvisor.builder(chatMemory)
    .build();

// 系統提示增強 - 使用 PromptChatMemoryAdvisor  
PromptChatMemoryAdvisor.builder(chatMemory)
    .build();

// 大量歷史記憶 - 使用 VectorStoreChatMemoryAdvisor
VectorStoreChatMemoryAdvisor.builder(vectorStore)
    .maxRetrievalSize(100)
    .build();
```

### 2. 記憶清理策略

```java
@Bean
public ChatMemory chatMemory(ChatMemoryRepository repository) {
    return MessageWindowChatMemory.builder()
        .chatMemoryRepository(repository)
        .maxMessages(20) // 保持最近 20 條訊息
        .build();
}
```

### 3. 多層記憶架構

```java
@Bean  
public ChatClient chatClient(ChatClient.Builder builder, 
                           ChatMemory chatMemory,
                           VectorStore vectorStore) {
    return builder
        .defaultAdvisors(
            // 短期記憶
            MessageChatMemoryAdvisor.builder(chatMemory).build(),
            // 長期記憶  
            VectorStoreChatMemoryAdvisor.builder(vectorStore).build()
        )
        .build();
}
```

## 問題討論與改善

### 向量記憶的適用場景

1. **適合的場景**：
   - 長期對話歷史檢索
   - 語意相似問題查找  
   - 大量對話資料管理
   - 知識庫整合

2. **不適合的場景**：
   - 嚴格的對話順序要求
   - 即時上下文依賴
   - 精確的指令記憶

### 混合記憶策略

```java
@Service
public class HybridChatService {
    private final ChatClient shortTermClient;
    private final ChatClient longTermClient;
    
    public String chat(String conversationId, String userMessage) {
        // 先嘗試短期記憶
        String shortTermResponse = shortTermClient.prompt()
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .user(userMessage)
            .call()
            .content();
            
        // 如果需要更多上下文，使用長期記憶
        if (needsMoreContext(shortTermResponse)) {
            return longTermClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(userMessage + "\n\n請參考更多歷史對話")
                .call()
                .content();
        }
        
        return shortTermResponse;
    }
}
```

## 企業級應用建議

### 1. 效能最佳化

```java
@Bean
public VectorStore vectorStore() {
    return Neo4jVectorStore.builder()
        .embeddingModel(embeddingModel)
        .initializeSchema(true)
        .indexType(Neo4jVectorStore.Neo4jDistanceType.COSINE)
        .embeddingDimension(1536)
        .build();
}
```

### 2. 監控與可觀測性

```java
@Bean
public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore) {
    return builder
        .defaultAdvisors(
            new SimpleLoggerAdvisor(), // 日誌監控
            VectorStoreChatMemoryAdvisor.builder(vectorStore).build()
        )
        .build();
}
```

## 回顧

今天學到了什麼？

- 了解 Spring AI 1.1 中改進的記憶系統架構
- 學會使用最新的 VectorStoreChatMemoryAdvisor  
- 掌握不同記憶類型的選擇標準
- 實作混合記憶策略
- 了解企業級記憶系統的最佳實踐

> Spring AI 1.1 中的記憶系統提供了更強大和靈活的對話管理能力，特別是向量記憶的語意搜尋功能，讓 AI 助手能夠更智能地理解和回應用戶。

> 明天我們將深入探討 Embedding Model 的原理和應用，了解向量化是如何工作的。

## Source Code

程式碼範例：[https://github.com/kevintsai1202/SpringBoot-AI-Day22-Updated.git](https://github.com/kevintsai1202/SpringBoot-AI-Day22-Updated.git)