## 使用 Spring AI 打造企業 RAG 知識庫【11】- 請支援 AI - Tool Calling (上)

> 📝 **更新說明**：本文章已根據 Spring AI 1.1-SNAPSHOT 最新版本進行更新，採用新的 Tool Calling API 取代已廢棄的 Function Calling API。

# AI 的弱點

![https://ithelp.ithome.com.tw/upload/images/20240811/20161290wKqFOXe5Jy.jpg](https://ithelp.ithome.com.tw/upload/images/20240811/20161290wKqFOXe5Jy.jpg)

雖然 AI 很神奇，不過它不是萬能的，遇到以下幾種問題 AI 就沒轍

- **記憶**: LLM 是一種無狀態推論，雖然跟 ChatGPT 對話似乎能記住對話內容，不過實際上它是根據你送出的資料來推論結果
- **即時資料**: LLM 預訓練資料會在一個時間點後關閉，之後所發生的事情 AI 就無法回答你，例如問 AI 2024 巴黎奧運中華隊得了幾面金牌，AI 不是不知道就是隨便編一個答案給你，因為最近的 LLM 預訓練資料都大概都在去年底關閉
- **數學運算**: LLM 顧名思義就是一種語言模型，就跟文科生數理普遍不好一樣，有些簡單的加減法甚至會算錯，雖然最近的模型似乎有加強計算的能力，不過就連 ChatGPT 也不敢跟你保證算出的答案一定是對的
- **企業內部資料**: 很顯然預訓練只能取得公開得資料來訓練，企業不可能公開自己的內部資訊，常見的作法是透過 fine tuning 將內部資料融入模型內，不過耗時費力

第一點我們後續會有專門章節來說明，其他幾點則可靠 Tool Calling 來擴充 AI 的能力

## Tool Calling 調用流程

Tool Calling（也稱為 Function Calling）就像是 AI 的外掛，除了能透過 Tool 取得即時資料外，一些複雜的運算或是需要分析的部分，也可以透過 Tool 來呼叫外部函式取得結果，下圖就是 Tool Calling 的運作流程

![https://ithelp.ithome.com.tw/upload/images/20240811/20161290o7GvM9RvQN.jpg](https://ithelp.ithome.com.tw/upload/images/20240811/20161290o7GvM9RvQN.jpg)

每個流程動作如下

1. 發送提示詞時需要告訴 AI 有哪些 Tool 可以調用，Spring AI 現在使用 ChatClient 的新式 API

```java
return ChatClient.create(chatModel)
    .prompt(prompt)
    .tools(new DateTimeTools())
    .call()
    .content();
```

2. AI 若查不到資料會查看是否有與提示詞相關的 Tool 描述

```java
class DateTimeTools {
    @Tool(description = "Get the current date and time")
    String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }
}
```

3. 找到匹配的 Tool 後執行並調用其資訊

```java
@Tool(description = "Get the current date and time")
String getCurrentDateTime() {
    return LocalDateTime.now().toString();
}
```

4. 將 Tool 回傳的資料以內部訊息格式一起傳送給 AI
5. AI 根據提示詞以及 Tool 回傳結果的綜合結果包成 Chat Response 回傳

1-3 項主要是由我們來撰寫，4、5 則是 Spring AI 幫我們處理了

## 程式碼實作

接下來就教大家從外部工具的撰寫到提示詞如何調用一步一步帶著大家做

### 程式目標 : 詢問目前時間，返回真實的日期及時間

1. **撰寫 Tool 類別**: 使用 `@Tool` 註解

```java
import java.time.LocalDateTime;
import org.springframework.ai.tool.annotation.Tool;

class DateTimeTools {
    
    @Tool(description = "Get the current date and time")
    String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }
}
```

2. **建立 ChatClient**: 使用新的 ChatClient fluent API

```java
@RestController
public class AiController {
    
    private final ChatModel chatModel;
    
    public AiController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }
    
    @GetMapping("/func")
    public String func(String prompt) {
        return ChatClient.create(chatModel)
            .prompt(prompt)
            .tools(new DateTimeTools())  // 直接傳入 Tool 實例
            .call()
            .content();
    }
}
```

3. **使用 ChatClient.Builder 建立可重用的 ChatClient**（推薦作法）

```java
@Configuration
public class AiConfig {
    
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
            .defaultTools(new DateTimeTools())  // 預設工具
            .build();
    }
}
```

```java
@RestController 
public class AiController {
    
    private final ChatClient chatClient;
    
    public AiController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    @GetMapping("/func")
    public String func(String prompt) {
        return chatClient.prompt()
            .user(prompt)
            .call()
            .content();
    }
}
```

### 新 API 的主要改進

1. **更簡潔的 API**: 不再需要 FunctionCallbackWrapper，直接使用 @Tool 註解
2. **更直觀的呼叫方式**: 使用 ChatClient 的 fluent API，程式碼更易讀
3. **更好的整合**: 與 Spring Boot 的整合更緊密
4. **更彈性的配置**: 支援預設工具和執行時工具

## 驗收成果

### 未使用Tool Call

![https://ithelp.ithome.com.tw/upload/images/20240811/2016129074bcm4xeGH.png](https://ithelp.ithome.com.tw/upload/images/20240811/2016129074bcm4xeGH.png)

### 使用Tool Call

![https://ithelp.ithome.com.tw/upload/images/20240811/20161290zYXGhrOtsL.png](https://ithelp.ithome.com.tw/upload/images/20240811/20161290zYXGhrOtsL.png)

## 回顧

今天學到的內容:

1. AI 的限制
2. Tool Calling 的調用流程
3. 新版程式撰寫方式
   1. 使用 @Tool 註解定義工具方法
   2. 使用 ChatClient 的 fluent API 
   3. 支援預設工具配置

### 與舊版 API 的對比

| 舊版 Function Calling | 新版 Tool Calling |
|---------------------|-------------------|
| FunctionCallbackWrapper | @Tool 註解 |
| OpenAiChatOptions.builder().withFunction() | ChatClient.tools() |
| 複雜的 Function 介面實作 | 簡單的方法註解 |

> 新版的 Tool Calling API 更加簡潔直觀，大幅降低了開發的複雜度，同時提供了更好的類型安全性和 IDE 支援

## Source Code

更新後的程式碼範例可參考最新的 Spring AI 官方文檔或 GitHub 範例專案

