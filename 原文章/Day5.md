## 使用 Spring AI 打造企業 RAG 知識庫【5】- 深入瞭解 ChatModel 與 ChatClient（2025最新版本更新）

> **更新說明：** 本文章已根據 Spring AI 1.0.1 最新穩定版本進行全面更新，詳細說明 ChatClient 與 ChatModel 的關係及最新使用方式。

![https://ithelp.ithome.com.tw/upload/images/20240810/20161290nfq1H0dDoW.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/20161290nfq1H0dDoW.jpg)

到目前為止我們做得比直接上 ChatGPT 發問還不如，ChatGPT 起碼還能記住你是誰（不信自己去問），今天我們就來深入了解 Spring AI 1.0.1 中 ChatClient 和 ChatModel 的完整架構與功能

## Spring AI 1.0.1 架構全貌

### ChatModel vs ChatClient 關係圖

```
┌─────────────────────────────────────────────────┐
│                  ChatClient                     │
│  ┌─────────────────────────────────────────┐   │
│  │           Fluent API                    │   │
│  │  .prompt()                             │   │
│  │  .user(message)                        │   │
│  │  .system(context)                      │   │
│  │  .call() / .stream()                   │   │
│  └─────────────────────────────────────────┘   │
│                     ↓                           │
│  ┌─────────────────────────────────────────┐   │
│  │           ChatModel                     │   │
│  │  - call(Prompt)                        │   │
│  │  - stream(Prompt)                      │   │
│  │  - 底層AI模型介面                      │   │
│  └─────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────┐
│              AI Provider                        │
│  (OpenAI, Azure, Google, Anthropic...)         │
└─────────────────────────────────────────────────┘
```

### 核心組件說明

1. **ChatClient**: 高階 Fluent API，提供簡潔的程式介面
2. **ChatModel**: 底層模型介面，ChatClient 內部使用它與 AI 提供商溝通
3. **AI Provider**: 實際的 AI 服務提供商

## ChatClient 深度解析

### 基本 Fluent API 用法

```java
@RestController
@RequiredArgsConstructor
public class ModernChatController {
    private final ChatClient chatClient;

    // 基本對話
    @GetMapping("/chat/basic")
    public String basicChat(@RequestParam String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    // 帶系統提示詞的對話
    @GetMapping("/chat/with-system")
    public String chatWithSystem(
            @RequestParam String prompt,
            @RequestParam String role) {
        
        String systemPrompt = "你是一個" + role + "，請用專業的角度回答問題";
        
        return chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();
    }
}
```

### 提示詞模板功能

Spring AI 1.0.1 支援動態變數替換：

```java
@Service
@RequiredArgsConstructor
public class TemplateService {
    private final ChatClient chatClient;

    public String analyzeStock(String stockName, String period) {
        return chatClient.prompt()
                .user(u -> u
                    .text("請分析 {stock} 在 {period} 的表現趨勢")
                    .param("stock", stockName)
                    .param("period", period))
                .call()
                .content();
    }

    // 更複雜的模板範例
    public String generateReport(Map<String, Object> data) {
        return chatClient.prompt()
                .system("你是一個專業的數據分析師")
                .user(u -> u
                    .text("""
                        請根據以下數據生成分析報告：
                        公司：{company}
                        收入：{revenue}
                        成長率：{growth}
                        請提供專業分析和建議。
                        """)
                    .params(data))
                .call()
                .content();
    }
}
```

## ChatOptions 深度配置

### 運行時配置選項

```java
@RestController
@RequiredArgsConstructor
public class ConfigurableChatController {
    private final ChatClient chatClient;

    @GetMapping("/chat/configurable")
    public String configurableChat(
            @RequestParam String prompt,
            @RequestParam(defaultValue = "0.7") Float temperature,
            @RequestParam(defaultValue = "gpt-4o-mini") String model,
            @RequestParam(defaultValue = "1000") Integer maxTokens) {
        
        return chatClient.prompt()
                .user(prompt)
                .options(ChatOptionsBuilder.builder()
                    .withModel(model)
                    .withTemperature(temperature)
                    .withMaxTokens(maxTokens)
                    .build())
                .call()
                .content();
    }
}
```

### 全域預設配置

```java
@Configuration
public class ChatConfiguration {
    
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是一個友善且專業的AI助手，請用繁體中文回答")
                .defaultOptions(ChatOptionsBuilder.builder()
                    .withModel("gpt-4o-mini")
                    .withTemperature(0.7f)
                    .withMaxTokens(2000)
                    .build())
                .build();
    }
}
```

## Message 類型詳解

### 四種 Message 類型的使用

```java
@Service
@RequiredArgsConstructor
public class AdvancedChatService {
    private final ChatClient chatClient;

    public String rolePlayChat(String userMessage, String character) {
        return chatClient.prompt()
                // SystemMessage: 設定AI角色
                .system("你是" + character + "，請完全沉浸在這個角色中回答")
                // UserMessage: 使用者訊息
                .user(userMessage)
                .call()
                .content();
    }

    // 多輪對話範例
    public String continuousChat(List<ChatMessage> history, String newMessage) {
        var promptBuilder = chatClient.prompt();
        
        // 加入歷史對話
        for (ChatMessage msg : history) {
            switch (msg.getType()) {
                case USER:
                    promptBuilder.user(msg.getContent());
                    break;
                case ASSISTANT:
                    // 注意：AssistantMessage 需要特殊處理
                    // 在實際應用中通常存在聊天記憶中
                    break;
                case SYSTEM:
                    promptBuilder.system(msg.getContent());
                    break;
            }
        }
        
        // 加入新訊息
        promptBuilder.user(newMessage);
        
        return promptBuilder.call().content();
    }
}
```

### 多媒體訊息支援

Spring AI 1.0.1 支援多模態輸入：

```java
@RestController
@RequiredArgsConstructor
public class MultimodalController {
    private final ChatClient chatClient;

    @PostMapping("/chat/image")
    public String analyzeImage(
            @RequestParam String question,
            @RequestParam MultipartFile image) throws IOException {
        
        return chatClient.prompt()
                .user(u -> u
                    .text(question)
                    .media(MimeTypeUtils.IMAGE_JPEG, image.getBytes()))
                .call()
                .content();
    }
}
```

## ChatModel 底層API使用

當需要更精細的控制時，可以直接使用 ChatModel：

```java
@Service
@RequiredArgsConstructor
public class LowLevelChatService {
    private final ChatModel chatModel;

    public ChatResponse detailedChat(String userMessage, String systemMessage) {
        List<Message> messages = List.of(
                new SystemMessage(systemMessage),
                new UserMessage(userMessage)
        );
        
        Prompt prompt = new Prompt(
                messages,
                OpenAiChatOptions.builder()
                    .withModel("gpt-4o")
                    .withTemperature(0.8f)
                    .withMaxTokens(1500)
                    .build()
        );
        
        ChatResponse response = chatModel.call(prompt);
        
        // 可以獲取詳細的回應資訊
        Generation result = response.getResult();
        PromptMetadata promptMetadata = response.getMetadata().getPromptMetadata();
        
        return response;
    }
}
```

## 實際應用範例：說故事機器人

讓我們創建一個更有趣的應用，結合系統提示詞和高溫度設定：

```java
@RestController
@RequiredArgsConstructor
public class StorytellerController {
    private final ChatClient chatClient;

    @GetMapping("/story")
    public String tellStory(
            @RequestParam String topic,
            @RequestParam(defaultValue = "童話") String style) {
        
        String systemPrompt = """
                你是一個創意豐富的說故事大師，專精於%s風格的故事創作。
                如果缺乏相關資料，請發揮創意編造一個引人入勝且積極正面的故事。
                故事要有完整的起承轉合，生動的角色描述，以及深刻的寓意。
                """.formatted(style);
        
        return chatClient.prompt()
                .system(systemPrompt)
                .user("請為我創作一個關於「" + topic + "」的故事")
                .options(ChatOptionsBuilder.builder()
                    .withTemperature(0.9f)  // 高創意度
                    .withMaxTokens(2000)
                    .build())
                .call()
                .content();
    }

    // 流式說故事
    @GetMapping(value = "/story/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> tellStoryStream(@RequestParam String topic) {
        return chatClient.prompt()
                .system("你是一個說故事大師，請用生動有趣的方式說故事")
                .user("請講一個關於「" + topic + "」的故事")
                .options(ChatOptionsBuilder.builder()
                    .withTemperature(1.0f)  // 最高創意度
                    .build())
                .stream()
                .content();
    }
}
```

## 錯誤處理與最佳實踐

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class RobustChatService {
    private final ChatClient chatClient;

    public String safeChat(String userMessage) {
        try {
            return chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("聊天請求失敗", e);
            return "抱歉，我暫時無法回應您的問題，請稍後再試。";
        }
    }

    // 帶重試機制的聊天
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public String chatWithRetry(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
```

## 效能優化配置

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
          max-tokens: 1000
        enabled: true
      connection-timeout: 10s
      read-timeout: 60s

# 連線池優化
server:
  servlet:
    encoding:
      charset: UTF-8
      enabled: true
      force: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

## 測試驗收

使用以下 API 測試不同功能：

```bash
# 基本對話
curl "http://localhost:8080/chat/basic?prompt=你好"

# 角色扮演
curl "http://localhost:8080/chat/with-system?prompt=如何投資股票&role=投資顧問"

# 說故事
curl "http://localhost:8080/story?topic=勇敢的小老鼠&style=童話"

# 流式說故事
curl -N "http://localhost:8080/story/stream?topic=太空探險"
```

## 回顧

今天深入學習的內容：

- **ChatClient 與 ChatModel 的完整架構關係**
- **Fluent API 的各種使用方式**
- **提示詞模板與動態變數功能**
- **ChatOptions 的運行時與全域配置**
- **四種 Message 類型的應用場景**
- **多模態輸入支援**
- **錯誤處理與效能優化**

## 新舊版本遷移指南

### 從舊版本升級

```java
// 舊版本 (pre-1.0)
String result = chatClient.call("Hello AI");

// 新版本 (1.0.1) - 推薦使用
String result = chatClient.prompt()
    .user("Hello AI")
    .call()
    .content();

// 或使用底層 ChatModel
String result = chatModel.call("Hello AI");
```

### API 對照表

| 功能 | 舊版本 | 新版本 ChatClient | 新版本 ChatModel |
|------|--------|------------------|------------------|
| 基本對話 | `chatClient.call(msg)` | `chatClient.prompt().user(msg).call().content()` | `chatModel.call(msg)` |
| 流式輸出 | `chatClient.stream(msg)` | `chatClient.prompt().user(msg).stream().content()` | `streamingChatModel.stream(msg)` |
| 系統提示詞 | 需手動組裝 | `.system(prompt)` | 手動建立 Prompt |
| 配置選項 | 較複雜 | `.options(...)` | Prompt 建構時設定 |

## Source Code

完整的現代化聊天服務實作：

```java
@RestController
@RequiredArgsConstructor
@Slf4j
public class ComprehensiveChatController {
    private final ChatClient chatClient;
    private final ChatModel chatModel;

    // 現代化 ChatClient 方式（推薦）
    @GetMapping("/chat/modern")
    public String modernChat(
            @RequestParam String prompt,
            @RequestParam(required = false) String system,
            @RequestParam(defaultValue = "0.7") Float temperature) {
        
        var builder = chatClient.prompt().user(prompt);
        
        if (system != null) {
            builder.system(system);
        }
        
        return builder
                .options(ChatOptionsBuilder.builder()
                    .withTemperature(temperature)
                    .build())
                .call()
                .content();
    }

    // 底層 ChatModel 方式
    @GetMapping("/chat/lowlevel")
    public ChatResponse lowLevelChat(@RequestParam String prompt) {
        Prompt chatPrompt = new Prompt(new UserMessage(prompt));
        return chatModel.call(chatPrompt);
    }
}
```

Spring AI 1.0.1 為我們提供了更強大、更易用的 AI 整合能力，無論是高階的 ChatClient 還是底層的 ChatModel，都能滿足不同層次的需求。

