# 使用 Spring AI 打造企業 RAG 知識庫【15】- ChatClient 與 Advisor API

## 既生瑜，何生亮

![https://ithelp.ithome.com.tw/upload/images/20240815/20161290XZLxuW3Mlf.png](https://ithelp.ithome.com.tw/upload/images/20240815/20161290XZLxuW3Mlf.png)

記得我們 [Day3](https://ithelp.ithome.com.tw/articles/10343175) 提到要自動綁定 ChatClient 卻失敗吧，今天來看看如何解決，並深入探討 Spring AI 1.1 中最重要的新功能 - Advisor API

## 程式碼實作

要初始化 ChatClient 只能使用 Builder 建立

```java
@RestController
class MyController {
    private final ChatClient chatClient;
    public MyController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
}
```

若有其他 Component 會使用 AI，可以在 Config 類別建立 Bean，要使用的 Component 在自動綁定即可

```java
@Configuration
class Config {
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
```

> 其實凱文大叔一直覺得之後的改版一定會讓 ChatClient 可以自動綁定

## Client在Spring框架的意義

前面我們 ChatModel 也用得好好的，為何在講記憶之前要先說 ChatClient ?

在 Spring 框架中，結尾冠上 Client 的類別，就是更高級的封裝，例如：RestClient、WebClient、JdbcClient等，而且這一系列的類別都提供 Fluent 風格的 API，雖然 ChatModel 也提供一部分的 Fluent API，不過僅限於類別本身的參數，許多外部設定還是得先建立其他物件在引入

ChatClient 最大的差別就是將這些外部類別設定的資料也一起整合進來，下面就來比較 ChatModel 與 ChatClient 寫法的差異吧

## ChatClient與ChatModel用法差異

### 1. Prompt

**ChatModel**:

```java
ChatResponse response = chatModel
			.call(new Prompt(
		    	new UserMessage("Tell me a joke"),
		    ));
```

**ChatClient**:

```java
ChatResponse chatResponse = chatClient.prompt()
    .user("Tell me a joke")
    .call()
    .chatResponse();
```

這個範例看起來雖然程式碼差不多，不過可以看到 ChatClient 在設定 Prompt 時直接使用 .prompt()，ChatModel 卻要自己 new 一個，這就是兩者最大的差別

### 2. PromptTemplate

**ChatModel**:

```java
@GetMapping("/template")
public String template1(@RequestParam String llm) {
    String template = "請問{llm}目前有哪些模型，各有甚麼特殊能力";
    PromptTemplate promptTemplate = new PromptTemplate(template);
    Prompt prompt = promptTemplate.create(Map.of("llm", llm));
    ChatResponse response = chatModel.call(prompt);
    return response.getResult().getOutput().getContent();
}
```

**ChatClient**:

```java
@GetMapping("/template")
public String template(@RequestParam String llm) {
    String template = "請問{llm}目前有哪些模型，各有甚麼特殊能力";
    return chatClient.prompt()
        .user(u -> u.text(template).param("llm", llm))
        .call()
        .content();
}
```

這個例子可以看出 ChatClient 在設定 Message 時直接使用 Lambda 語法建立 PromptTemplate 並使用 param 帶入參數，一氣呵成

### 3. Structured Output Converter

**ChatModel**:

```java
record ActorsFilms(String actor, List<String> movies) {};
@GetMapping("/films")
public ActorsFilms films(String actor) {
    String template = """
            列出演員{actor}最有名的五部電影，需用繁體中文回答
            {format}
            """;
    BeanOutputConverter<ActorsFilms> beanOutputConverter =
        new BeanOutputConverter<>(ActorsFilms.class);
    String format = beanOutputConverter.getFormat();
    Generation generation = chatModel.call(
        new Prompt(new PromptTemplate(template, Map.of("actor", actor, "format", format)).createMessage())).getResult();
    ActorsFilms actorsFilms = beanOutputConverter.convert(generation.getOutput().getContent());
    return actorsFilms;
}
```

**ChatClient**:

```java
record ActorsFilms(String actor, List<String> movies) {};
@GetMapping("/films")
public ActorsFilms films(String actor) {
    String template = """
            列出演員{actor}最有名的五部電影，需用繁體中文回答
            """;
    return chatClient.prompt()
        .user(u -> u.text(template).param("actor", actor))
        .call()
        .entity(ActorsFilms.class);
}
```

結構化輸出時 ChatClient 竟然只用 .entity() 就取代 BeanOutputConverter 越複雜的案例使用 ChatClient 就會讓程式碼更簡潔，ChatClient 其實是將 BeanOutputConverter 包進類別裡處理結構化輸出

### 4. Tool Calling (新版本語法)

因為 ChatClient 把 .prompt() 加入 Fluent API 中，原本要使用 Options 設定的 Function 的部分也跟著一起簡化了

**ChatModel (舊版本)**

```java
@GetMapping("/func")
public String func(String prompt) {
    return chatModel.call(
        new Prompt(prompt,
           OpenAiChatOptions.builder()
           .withFunction("ProductSalesInfo")
           .withFunction("ProductDetailsInfo")
           .build())
    ).getResult().getOutput().getContent();
}
```

**ChatClient (新版本)**

```java
@GetMapping("/func")
public String func(String prompt) {
    return chatClient.prompt()
        .user(prompt)
        .tools(new ProductSalesTools(), new ProductDetailsTools())
        .call()
        .content();
}
```

新版本可以直接使用 `.tools()` 方法傳入工具類別實例，這比舊版本的字串引用更加類型安全。

### 5. Using Defaults

在 ChatClient 中還能設定 Default 參數，這部分需要在 builder 時設定，之後執行時沒帶入參數就會使用預設的內容，舉個實際例子

```java
@Configuration
class Config {
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("你是個友善的聊天機器人,不管甚麼問題都盡可能提供答案")
                .build();
    }
}
```

之後呼叫 `chatClient` 時若沒使用 `.system()` 覆蓋 `SystemMessage`，Spring AI 就會使用 `defaultSystem` 的內容來作為 `SystemMessage`

下面是預設的方法以及運行時的方法

| Default | Runtime |
| --- | --- |
| defaultSystem | system |
| defaultUser | user |
| defaultTools | tools |
| defaultOptions | options |
| defaultAdvisors | advisors |

有注意到 `advisors` 嗎？這是前面沒提過的內容，也會是後面記憶跟 RAG 最重要的調用方法。

## Spring AI 1.1 的 Advisor API

Spring AI 1.1 引入了革命性的 Advisor API，這是實現 RAG（檢索增強生成）和對話記憶的核心機制。

### Advisor 的概念

Advisor 是一個攔截器模式的實現，可以在 AI 請求和響應的過程中進行干預和處理：

1. **請求處理**：在發送給 AI 模型前修改 Prompt
2. **響應處理**：在返回給用戶前處理 AI 的回應
3. **上下文管理**：維護對話上下文和相關資訊

### RAG Advisor 實作

讓我們看看如何使用新的 QuestionAnswerAdvisor 來實現 RAG：

```java
@Configuration
public class RAGConfig {
    
    @Bean
    public ChatClient ragChatClient(
            ChatClient.Builder builder,
            VectorStore vectorStore) {
        return builder
            .defaultAdvisors(
                QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.defaults().withTopK(5))
                    .build()
            )
            .build();
    }
}
```

### 記憶功能的實現

結合記憶功能的完整範例：

```java
@Configuration
public class ChatConfig {
    
    @Bean
    public ChatClient enhancedChatClient(
            ChatClient.Builder builder,
            VectorStore vectorStore,
            ChatMemory chatMemory) {
        return builder
            .defaultAdvisors(
                // 記憶管理 - 較高優先級（先執行）
                MessageChatMemoryAdvisor.builder(chatMemory)
                    .order(1)
                    .build(),
                // RAG 功能 - 較低優先級（後執行）  
                QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.defaults().withTopK(5))
                    .order(2)
                    .build()
            )
            .build();
    }
}
```

### 使用 Advisor 的 ChatClient

```java
@RestController
@RequestMapping("/api/chat")
public class EnhancedChatController {
    
    private final ChatClient chatClient;
    
    public EnhancedChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    @PostMapping("/ask")
    public String ask(@RequestParam String question, 
                     @RequestParam String conversationId) {
        return chatClient.prompt()
            .user(question)
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();
    }
    
    @PostMapping("/stream")
    public Flux<String> streamChat(@RequestParam String question,
                                   @RequestParam String conversationId) {
        return chatClient.prompt()
            .user(question)
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
            .stream()
            .content();
    }
}
```

### Advisor 的執行順序

Advisor 按照 `order` 值執行，數值越小優先級越高：

1. **記憶 Advisor** (order=1)：先載入對話歷史
2. **RAG Advisor** (order=2)：基於問題和歷史進行檢索

### 自定義 Advisor

你也可以建立自定義的 Advisor：

```java
@Component
public class LoggingAdvisor implements CallAdvisor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingAdvisor.class);
    
    @Override
    public String getName() {
        return "LoggingAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 0; // 最高優先級
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("Processing request: {}", request.prompt().getUserMessage());
        
        ChatClientResponse response = chain.nextCall(request);
        
        logger.info("Generated response: {}", response.chatResponse().getResult().getOutput().getContent());
        
        return response;
    }
}
```

## 回顧

今天學到的內容:

1. 了解 Client 結尾的 Class 在 Spring 框架的含意
2. ChatClient 與 ChatModel 用法比較
3. **Spring AI 1.1 的 Advisor API**
4. **RAG 功能的實現方式**
5. **記憶功能與 RAG 的結合**
6. **自定義 Advisor 的開發**

### Spring AI 1.1 的重大改進

1. **統一的 Advisor 架構**：提供一致的擴展點
2. **簡化的 RAG 實現**：QuestionAnswerAdvisor 讓 RAG 變得簡單
3. **內建記憶管理**：MessageChatMemoryAdvisor 處理對話歷史
4. **可觀測性支援**：內建 metrics 和 tracing
5. **流式處理支援**：Advisor 同時支援同步和流式處理

> 今天以後程式碼的部分會盡量使用 ChatClient 和 Advisor API 來撰寫，這是 Spring AI 1.1 的最佳實踐方式。

## Source Code

今日程式碼: [https://github.com/kevintsai1202/SpringBoot-AI-Day15.git](https://github.com/kevintsai1202/SpringBoot-AI-Day15.git)