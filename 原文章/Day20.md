# 使用 Spring AI 打造企業 RAG 知識庫【20】- 自行開發Spring AI插件（2025年最新版）

> **重要更新說明**：本文已更新至Spring AI 1.0.0以上版本，使用最新的Advisor API。相較於原版本使用已被廢棄的`RequestResponseAdvisor`，現在採用新的`CallAdvisor`和`StreamAdvisor`介面。

## 揭開Advisor面紗

![Advisor概念圖](https://ithelp.ithome.com.tw/upload/images/20240820/20161290CbVKKopXpq.png)

延續昨天的 Advisor，新版的Advisor設計方式更加清晰，採用責任鏈模式（Chain of Responsibility），每個 Advisor 節點都可以處理 Request 與 Response，讓我們來看看最新的API設計。

## 最新API原始碼說明

### 核心介面

```java
public interface Advisor extends Ordered {
    String getName();
}
```

### CallAdvisor（非串流場景）

```java
public interface CallAdvisor extends Advisor {
    ChatClientResponse adviseCall(
        ChatClientRequest chatClientRequest, 
        CallAdvisorChain callAdvisorChain
    );
}
```

### StreamAdvisor（串流場景）

```java
public interface StreamAdvisor extends Advisor {
    Flux<ChatClientResponse> adviseStream(
        ChatClientRequest chatClientRequest, 
        StreamAdvisorChain streamAdvisorChain
    );
}
```

### 新舊API對比

#### 舊版API（已廢棄）
```java
public interface RequestResponseAdvisor {
    default AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        return request;
    }
    default ChatResponse adviseResponse(ChatResponse response, Map<String, Object> context) {
        return response;
    }
    default Flux<ChatResponse> adviseResponse(Flux<ChatResponse> fluxResponse, Map<String, Object> context) {
        return fluxResponse;
    }
}
```

#### 新版API（當前使用）
- 更清晰的介面分離：`CallAdvisor` 專門處理非串流，`StreamAdvisor` 專門處理串流
- 鏈式處理：使用 `CallAdvisorChain` 和 `StreamAdvisorChain` 管理執行順序
- 統一的請求/回應物件：`ChatClientRequest` 和 `ChatClientResponse`
- 內建的上下文管理：`adviseContext` 提供更好的狀態共享

## 程式實作

### 目標: 寫一隻 Log Advisor，記錄傳送訊息以及使用 Token 數

首先實作 TokenUsageLogAdvisor 類別，支援最新的CallAdvisor和StreamAdvisor介面：

```java
@Slf4j
public class TokenUsageLogAdvisor implements CallAdvisor, StreamAdvisor {
    
    @Override
    public String getName() {
        return "TokenUsageLogAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 0; // 設定執行順序，數值越小越先執行
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // 記錄請求訊息
        logRequest(request);
        
        // 呼叫鏈中的下一個advisor
        ChatClientResponse response = chain.nextCall(request);
        
        // 記錄回應訊息和Token使用量
        logResponse(response);
        
        return response;
    }
    
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // 記錄請求訊息
        logRequest(request);
        
        // 呼叫鏈中的下一個advisor並處理串流回應
        return chain.nextStream(request)
            .doOnNext(this::logResponse) // 對每個串流項目記錄
            .doOnComplete(() -> log.info("Stream completed for Chat ID: {}", 
                request.adviseContext().get("chatId")))
            .doOnError(error -> log.error("Stream error for Chat ID: {}", 
                request.adviseContext().get("chatId"), error));
    }
    
    private void logRequest(ChatClientRequest request) {
        String chatId = (String) request.adviseContext().get("chatId");
        String userMessage = request.prompt().getUserMessage().getText();
        log.info("Chat ID:{} User Message:{}", chatId, userMessage);
    }
    
    private void logResponse(ChatClientResponse response) {
        String chatId = (String) response.adviseContext().get("chatId");
        String assistantMessage = response.response().getResult().getOutput().getContent();
        
        log.info("Chat ID:{} Assistant Message:{}", chatId, assistantMessage);
        
        // 記錄Token使用量
        if (response.response().getMetadata() != null && 
            response.response().getMetadata().getUsage() != null) {
            var usage = response.response().getMetadata().getUsage();
            log.info("Chat ID:{} PromptTokens:{}", chatId, usage.getPromptTokens());
            log.info("Chat ID:{} GenerationTokens:{}", chatId, usage.getGenerationTokens());
            log.info("Chat ID:{} TotalTokens:{}", chatId, usage.getTotalTokens());
        }
    }
}
```

### 整合到ChatService

#### 方法一：執行時添加Advisor

```java
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatClient chatClient;
    private final ChatMemory chatMemory = new InMemoryChatMemory();

    public String chat(String chatId, String userMessage) {
        return this.chatClient.prompt()
            .advisors(
                MessageChatMemoryAdvisor.builder(chatMemory)
                    .conversationId(chatId)
                    .lastN(30)
                    .build(),
                new TokenUsageLogAdvisor() // 添加自訂的Advisor
            )
            .advisors(advisor -> advisor.param("chatId", chatId)) // 傳遞context參數
            .user(userMessage)
            .call().content();
    }
}
```

#### 方法二：建構時預設Advisor（推薦）

```java
@Configuration
public class ChatConfig {
    
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        ChatMemory chatMemory = new InMemoryChatMemory();
        
        return ChatClient.builder(chatModel)
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                new TokenUsageLogAdvisor()
            )
            .build();
    }
}

@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatClient chatClient;

    public String chat(String chatId, String userMessage) {
        return this.chatClient.prompt()
            .advisors(advisor -> {
                advisor.param(ChatMemory.CONVERSATION_ID, chatId);
                advisor.param("chatId", chatId);
                advisor.param("lastN", 30);
            })
            .user(userMessage)
            .call().content();
    }
}
```

### 程式碼重點說明

#### 新版API的關鍵特性

1. **介面分離**：`CallAdvisor` 和 `StreamAdvisor` 分別處理不同的使用場景
2. **鏈式處理**：使用 `chain.nextCall()` 或 `chain.nextStream()` 繼續執行鏈
3. **上下文管理**：`adviseContext()` 提供不可變的上下文映射
4. **執行順序**：透過 `getOrder()` 控制執行順序，數值越小越先執行

#### 上下文參數傳遞

```java
// 新版API - 取得上下文參數
String chatId = (String) request.adviseContext().get("chatId");

// 新版API - 更新上下文（創建新的不可變映射）
ChatClientRequest updatedRequest = request.mutate()
    .adviseContext(request.adviseContext().with("newKey", "newValue"))
    .build();
```

#### 與舊版的差異

| 項目 | 舊版API | 新版API |
|------|---------|---------|
| 介面 | `RequestResponseAdvisor` | `CallAdvisor` / `StreamAdvisor` |
| 請求物件 | `AdvisedRequest` | `ChatClientRequest` |
| 回應物件 | `AdvisedResponse` / `ChatResponse` | `ChatClientResponse` |
| 上下文 | `Map<String, Object> context` | `request.adviseContext()` |
| 可變性 | 可變上下文 | 不可變上下文 |
| 鏈處理 | 隱式處理 | 明確的鏈呼叫 |

## 進階使用：同時支援串流和非串流

```java
@Slf4j
public class AdvancedLogAdvisor implements CallAdvisor, StreamAdvisor {
    
    private final ChatClientMessageAggregator messageAggregator = new ChatClientMessageAggregator();
    
    @Override
    public String getName() {
        return "AdvancedLogAdvisor";
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // 最高優先級
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long startTime = System.currentTimeMillis();
        logRequest(request);
        
        ChatClientResponse response = chain.nextCall(request);
        
        long duration = System.currentTimeMillis() - startTime;
        logResponse(response, duration);
        
        return response;
    }
    
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        long startTime = System.currentTimeMillis();
        logRequest(request);
        
        return messageAggregator.aggregateChatClientResponse(
            chain.nextStream(request),
            response -> {
                long duration = System.currentTimeMillis() - startTime;
                logResponse(response, duration);
            }
        );
    }
    
    private void logRequest(ChatClientRequest request) {
        log.info("=== Request Start ===");
        log.info("Chat ID: {}", request.adviseContext().get("chatId"));
        log.info("User Message: {}", request.prompt().getUserMessage().getText());
        log.info("=== Request End ===");
    }
    
    private void logResponse(ChatClientResponse response, long duration) {
        log.info("=== Response Start ===");
        log.info("Duration: {}ms", duration);
        log.info("Assistant Message: {}", response.response().getResult().getOutput().getContent());
        
        if (response.response().getMetadata() != null && 
            response.response().getMetadata().getUsage() != null) {
            var usage = response.response().getMetadata().getUsage();
            log.info("Token Usage - Prompt: {}, Generation: {}, Total: {}", 
                usage.getPromptTokens(), usage.getGenerationTokens(), usage.getTotalTokens());
        }
        log.info("=== Response End ===");
    }
}
```

## 驗收成果

![測試結果](https://ithelp.ithome.com.tw/upload/images/20240820/20161290zCJN7gXZPG.png)

可以看到 log 成功輸出，Chat ID 也能通過 adviseContext 取得，另外多了 Token 數量輸出後可以很清楚知道每次發問使用的 Token 數量。新版API提供了更穩定和高效的處理方式。

## Advisor執行順序

新版API提供了更清晰的執行順序控制：

```java
public class OrderExample {
    // 執行順序：數值越小越先執行
    public static final int SECURITY_ORDER = Ordered.HIGHEST_PRECEDENCE;     // -2147483648
    public static final int LOGGING_ORDER = Ordered.HIGHEST_PRECEDENCE + 100; // -2147483548
    public static final int MEMORY_ORDER = 0;                                  // 0
    public static final int RAG_ORDER = 100;                                  // 100
    public static final int FINAL_ORDER = Ordered.LOWEST_PRECEDENCE;          // 2147483647
}
```

## 最佳實踐建議

1. **同時實作兩個介面**：為了最大的靈活性，建議同時實作 `CallAdvisor` 和 `StreamAdvisor`
2. **適當的執行順序**：使用 `getOrder()` 設定合理的執行順序
3. **不可變上下文**：善用不可變的 `adviseContext` 進行狀態共享
4. **錯誤處理**：在串流場景中適當處理錯誤和完成事件
5. **效能考量**：將常用的Advisor設定為預設Advisor以獲得更好效能

## 回顧

今天學到了甚麼?

- 最新Spring AI Advisor API的完整介紹
- 新舊API的詳細對比和遷移指南
- 如何自行開發同時支援串流和非串流的 Advisor
- 多個 Advisor 如何串接與執行順序控制
- 新版不可變上下文的參數傳遞機制
- 最佳實踐和效能優化建議

> 新版的 Advisor API 方法採用明確的介面分離設計，提供了更好的型別安全和效能優化。開發者可以根據需求選擇實作 `CallAdvisor`、`StreamAdvisor` 或兩者皆實作。

## 升級指南

如果您正在從舊版API升級：

1. 將 `RequestResponseAdvisor` 替換為 `CallAdvisor` 和/或 `StreamAdvisor`
2. 更新方法簽名：
   - `adviseRequest()` → `adviseCall()` 或 `adviseStream()`
   - `adviseResponse()` → 在 `adviseCall()` 或 `adviseStream()` 中處理
3. 更新上下文存取方式：
   - `context.get("key")` → `request.adviseContext().get("key")`
4. 使用鏈式呼叫：
   - 明確呼叫 `chain.nextCall()` 或 `chain.nextStream()`

## Source Code:

程式碼下載: [https://github.com/kevintsai1202/SpringBoot-AI-Day20.git](https://github.com/kevintsai1202/SpringBoot-AI-Day20.git)
（注意：GitHub程式碼已更新至最新版本API）

