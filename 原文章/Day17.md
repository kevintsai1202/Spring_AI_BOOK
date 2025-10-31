# 使用 Spring AI 打造企業 RAG 知識庫【17】- Spring AI 1.1 記憶管理系統

## 土炮記憶 vs 企業級記憶

![https://ithelp.ithome.com.tw/upload/images/20240817/20161290GI4axNwalT.png](https://ithelp.ithome.com.tw/upload/images/20240817/20161290GI4axNwalT.png)

要讓 AI 記住對話，最簡單的方式是建立一個 List，並將對話紀錄加進 List 中，就如昨天所說 AI 就能根據歷史訊息回答相關問題。但 Spring AI 1.1 提供了更完善的企業級記憶管理系統。

## 傳統做法的程式碼實作

程式碼中只需在發問前將訊息加入 List，之後將整串 List 發送給 AI

```java
@RestController
@RequiredArgsConstructor
public class BasicMemoryController {
    private final ChatClient chatClient;
    private List<Message> memMessage = new ArrayList<>();

    @GetMapping("/basic-memory")
    public String chat(@RequestParam String prompt) {
        memMessage.add(new UserMessage(prompt));
        
        String response = chatClient.prompt()
            .messages(memMessage)
            .call()
            .content();
            
        // 將 AI 的回應也加入記憶中
        memMessage.add(new AssistantMessage(response));
        
        return response;
    }
}
```

可以看出 API 早就設計好了，`.messages()` 可以直接傳入 List，AI 會自動將最後一筆 UserMessage 當成提問，之前的 UserMessage 則會當成歷史對話

## Spring AI 1.1 的企業級記憶系統

### 1. ChatMemory 配置

Spring AI 1.1 提供了完整的記憶管理系統：

```java
@Configuration
public class ChatMemoryConfig {
    
    @Bean
    public ChatMemoryRepository chatMemoryRepository() {
        // 可選擇不同的儲存實現
        return new InMemoryChatMemoryRepository(); // 開發環境
        // return new JdbcChatMemoryRepository(dataSource); // 生產環境
    }
    
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
            .maxMessages(20) // 最多保留20條訊息
            .repository(repository)
            .build();
    }
    
    @Bean
    public ChatClient memoryChatClient(
            ChatClient.Builder builder, 
            ChatMemory chatMemory) {
        return builder
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory)
                    .order(1) // 最高優先級，確保記憶先被載入
                    .build()
            )
            .build();
    }
}
```

### 2. 使用記憶功能的控制器

```java
@RestController
@RequestMapping("/api/chat")
public class MemoryEnabledController {
    
    private final ChatClient memoryChatClient;
    
    public MemoryEnabledController(
            @Qualifier("memoryChatClient") ChatClient memoryChatClient) {
        this.memoryChatClient = memoryChatClient;
    }
    
    @PostMapping("/conversation")
    public ResponseEntity<ChatResponse> chat(
            @RequestParam String message,
            @RequestParam String conversationId) {
        
        ChatResponse response = memoryChatClient.prompt()
            .user(message)
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .chatResponse();
            
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/stream")
    public Flux<String> streamChat(
            @RequestParam String message,
            @RequestParam String conversationId) {
        
        return memoryChatClient.prompt()
            .user(message)
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
            .stream()
            .content();
    }
    
    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<Message>> getHistory(@PathVariable String conversationId) {
        ChatMemory chatMemory = applicationContext.getBean(ChatMemory.class);
        List<Message> history = chatMemory.get(conversationId);
        return ResponseEntity.ok(history);
    }
    
    @DeleteMapping("/history/{conversationId}")
    public ResponseEntity<Void> clearHistory(@PathVariable String conversationId) {
        ChatMemory chatMemory = applicationContext.getBean(ChatMemory.class);
        chatMemory.clear(conversationId);
        return ResponseEntity.ok().build();
    }
    
    @Autowired
    private ApplicationContext applicationContext;
}
```

### 3. 進階：結合 RAG 和記憶功能

```java
@Configuration
public class AdvancedChatConfig {
    
    @Bean
    public ChatClient ragWithMemoryChatClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            VectorStore vectorStore) {
        return builder
            .defaultAdvisors(
                // 記憶管理 - 第一優先級
                MessageChatMemoryAdvisor.builder(chatMemory)
                    .order(1)
                    .build(),
                // RAG 功能 - 第二優先級，可以基於對話歷史進行檢索
                QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.defaults()
                        .withTopK(5)
                        .withSimilarityThreshold(0.75))
                    .order(2)
                    .build()
            )
            .build();
    }
}

@RestController
public class AdvancedChatController {
    
    private final ChatClient ragWithMemoryChatClient;
    
    public AdvancedChatController(
            @Qualifier("ragWithMemoryChatClient") ChatClient ragWithMemoryChatClient) {
        this.ragWithMemoryChatClient = ragWithMemoryChatClient;
    }
    
    @PostMapping("/advanced-chat")
    public String advancedChat(
            @RequestParam String question,
            @RequestParam String conversationId) {
        
        return ragWithMemoryChatClient.prompt()
            .user(question)
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();
    }
}
```

### 4. 不同的記憶儲存選項

```java
@Configuration
@Profile("production")
public class ProductionMemoryConfig {
    
    // 使用資料庫儲存記憶
    @Bean
    @ConditionalOnProperty(name = "app.memory.store", havingValue = "jdbc")
    public ChatMemoryRepository jdbcChatMemoryRepository(DataSource dataSource) {
        return new JdbcChatMemoryRepository(dataSource);
    }
    
    // 使用 Redis 儲存記憶（如果有 Redis 配置）
    @Bean
    @ConditionalOnProperty(name = "app.memory.store", havingValue = "redis")
    public ChatMemoryRepository redisChatMemoryRepository(RedisTemplate<String, Object> redisTemplate) {
        return new RedisChatMemoryRepository(redisTemplate);
    }
    
    // 使用 Cassandra 儲存記憶
    @Bean
    @ConditionalOnProperty(name = "app.memory.store", havingValue = "cassandra")
    public ChatMemoryRepository cassandraChatMemoryRepository(CassandraTemplate cassandraTemplate) {
        return new CassandraChatMemoryRepository(cassandraTemplate);
    }
}
```

### 5. 自定義記憶策略

```java
@Component
public class CustomChatMemoryAdvisor implements CallAdvisor {
    
    private final ChatMemory chatMemory;
    
    public CustomChatMemoryAdvisor(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }
    
    @Override
    public String getName() {
        return "CustomMemoryAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String conversationId = (String) request.adviseContext()
            .getOrDefault(ChatMemory.CONVERSATION_ID, "default");
        
        // 載入歷史對話
        List<Message> history = chatMemory.get(conversationId);
        
        // 只保留最近5輪對話（10條訊息）
        if (history.size() > 10) {
            history = history.subList(history.size() - 10, history.size());
        }
        
        // 構建包含歷史的新請求
        List<Message> allMessages = new ArrayList<>(history);
        allMessages.addAll(request.prompt().getInstructions());
        
        Prompt newPrompt = new Prompt(allMessages, request.prompt().getOptions());
        ChatClientRequest newRequest = ChatClientRequest.builder(request)
            .prompt(newPrompt)
            .build();
        
        // 繼續處理
        ChatClientResponse response = chain.nextCall(newRequest);
        
        // 儲存新的對話
        chatMemory.add(conversationId, request.prompt().getUserMessage());
        chatMemory.add(conversationId, response.chatResponse().getResult().getOutput());
        
        return response;
    }
}
```

## 驗收成果

來看看測試結果

1. **發問**: `我是凱文大叔,之後回答問題請都先叫我的名字`

![https://ithelp.ithome.com.tw/upload/images/20240817/20161290RoTAQ8UDFr.png](https://ithelp.ithome.com.tw/upload/images/20240817/20161290RoTAQ8UDFr.png)

2. **發問**: `目前的模型是甚麼版本`

![https://ithelp.ithome.com.tw/upload/images/20240817/20161290SqWbhqAO1X.png](https://ithelp.ithome.com.tw/upload/images/20240817/20161290SqWbhqAO1X.png)

AI 不只能記住之前的對話，還能依要求每次都先稱呼我

## 企業級記憶系統的優勢

### 解決傳統做法的問題

1. **對話隔離**：每個 conversationId 都有獨立的記憶空間
2. **記憶容量管理**：自動管理記憶大小，避免 Token 過多
3. **持久化儲存**：支援多種儲存後端，重啟不會丟失記憶
4. **併發安全**：支援多用戶同時使用
5. **可觀測性**：內建 metrics 和 logging

### 配置選項

```yaml
# application.yml
app:
  memory:
    store: jdbc  # 選擇儲存類型：memory, jdbc, redis, cassandra
    max-messages: 50  # 每個對話最多保留的訊息數
    cleanup-interval: 3600  # 清理間隔（秒）

spring:
  ai:
    chat:
      memory:
        enabled: true
        window-size: 20  # 記憶視窗大小
```

## 與舊版本的差異

### 舊版本（手動管理）
```java
// 需要手動管理記憶邏輯
List<Message> messages = new ArrayList<>();
messages.add(new UserMessage(question));
ChatResponse response = chatModel.call(new Prompt(messages));
messages.add(response.getResult().getOutput()); // 手動加入回應
```

### 新版本（Advisor 自動管理）
```java
// Advisor 自動處理記憶管理
String response = chatClient.prompt()
    .user(question)
    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
    .call()
    .content();
```

## 回顧

今天學到了什麼？

1. **傳統記憶實現的限制**：簡單但缺乏企業級功能
2. **Spring AI 1.1 記憶系統**：完整的企業級解決方案
3. **MessageChatMemoryAdvisor**：自動記憶管理
4. **多種儲存選項**：記憶體、資料庫、NoSQL
5. **記憶與 RAG 的結合**：建構更智能的對話系統

明天我們將探討如何結合向量資料庫實現真正的 RAG 知識庫系統！

## Source Code

今日程式碼: [https://github.com/kevintsai1202/SpringBoot-AI-Day17.git](https://github.com/kevintsai1202/SpringBoot-AI-Day17.git)