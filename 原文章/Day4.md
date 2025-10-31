## 使用 Spring AI 打造企業 RAG 知識庫【4】- 如何像 ChatGPT 產生流式輸出（2025最新版本更新）

> **更新說明：** 本文章已根據 Spring AI 1.0.1 最新穩定版本進行全面更新，使用最新的 ChatClient Fluent API 實現流式輸出功能。

![https://ithelp.ithome.com.tw/upload/images/20240810/20161290gaE6Faz85x.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/20161290gaE6Faz85x.jpg)

由於 AI 產生內容得靠伺服器運算後產生結果，資料多的話得等不少時間，若能產生資料後馬上分段送出，可大大提升使用者感受，要達到這種效果主要靠的是 Server-Send Event(以下簡稱SSE) 伺服器主動推播協定

前端只需寫個事件監聽函式，收到資料就立刻加在聊天訊息上，不需等全部內容產出就能開始觀看，而且資料產生的速度遠比人觀看的速度快，使用者可以不用對著螢幕發呆

## ChatClient 流式輸出實作

Spring AI 1.0.1 的 ChatClient 提供了優雅的流式輸出 API，讓實作變得更加簡潔：

```java
@RestController
@RequiredArgsConstructor
public class StreamingAiController {
    private final ChatClient chatClient;

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
}
```

### 程式碼重點說明

- **`stream()`**: 替代 `call()` 來啟用流式輸出
- **`content()`**: 返回 `Flux<String>` 流式字串內容
- **`produces = MediaType.TEXT_EVENT_STREAM_VALUE`**: 指定回應格式為 SSE

## 更進階的流式輸出控制

如果需要更多控制選項，可以使用完整的 ChatResponse 流：

```java
@GetMapping(value = "/chat/stream-response", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ChatResponse> chatStreamResponse(@RequestParam String prompt) {
    return chatClient.prompt()
            .user(prompt)
            .stream()
            .chatResponse();
}
```

### 自定義流式輸出處理

```java
@GetMapping(value = "/chat/stream-custom", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chatStreamCustom(@RequestParam String prompt) {
    return chatClient.prompt()
            .user(prompt)
            .stream()
            .content()
            .map(content -> {
                // 自定義處理每個流式片段
                return "AI: " + content;
            })
            .doOnNext(content -> {
                // 記錄或處理每個片段
                log.debug("Streaming content: {}", content);
            });
}
```

## 使用 ChatModel 的流式輸出（底層API）

如果直接使用 ChatModel，也可以實現流式輸出：

```java
@RestController
@RequiredArgsConstructor
public class StreamingModelController {
    private final StreamingChatModel streamingChatModel;

    @GetMapping(value = "/model/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> modelStream(@RequestParam String prompt) {
        return streamingChatModel.stream(prompt)
                .map(chatResponse -> chatResponse.getResult().getOutput().getContent());
    }
}
```

## 應用程式配置

確保在 `application.yml` 中正確設定字元編碼，避免中文亂碼：

```yaml
server:
  servlet:
    encoding:
      charset: UTF-8
      enabled: true
      force: true

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
          stream: true  # 啟用流式輸出
```

## 完整的聊天服務範例

以下是一個完整的聊天服務實作，結合了系統提示詞和流式輸出：

```java
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatClient chatClient;

    public Flux<String> streamChat(String userMessage, String systemPrompt) {
        return chatClient.prompt()
                .system(systemPrompt != null ? systemPrompt : "你is一個友善的AI助手")
                .user(userMessage)
                .stream()
                .content()
                .onErrorResume(error -> {
                    log.error("聊天流式輸出錯誤", error);
                    return Flux.just("抱歉，發生錯誤，請稍後再試。");
                });
    }
}

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @RequestParam String prompt,
            @RequestParam(required = false) String system) {
        return chatService.streamChat(prompt, system);
    }
}
```

## 前端 JavaScript 整合範例

前端可以使用 EventSource 來接收流式資料：

```javascript
function streamChat(prompt) {
    const eventSource = new EventSource(`/chat?prompt=${encodeURIComponent(prompt)}`);
    const chatContainer = document.getElementById('chat-response');
    
    eventSource.onmessage = function(event) {
        chatContainer.innerHTML += event.data;
    };
    
    eventSource.onerror = function(event) {
        console.error('Stream error:', event);
        eventSource.close();
    };
    
    eventSource.addEventListener('close', function() {
        eventSource.close();
    });
}
```

## 測試成果

### 使用 Postman 測試

URL: `http://localhost:8080/chat/stream?prompt=解釋什麼是Spring AI`

在 Postman 中你會看到：
- Response Type 顯示為 `text/event-stream`
- 內容會分段即時顯示
- 不會等待完整回應才顯示

### 使用 curl 測試

```bash
curl -N "http://localhost:8080/chat/stream?prompt=介紹Spring框架"
```

參數 `-N` 確保不緩衝輸出，能即時看到流式結果。

## 效能優化建議

1. **連線池設定**：
```yaml
spring:
  ai:
    openai:
      chat:
        options:
          max-tokens: 1000  # 限制輸出長度
  reactor:
    netty:
      connection-provider:
        max-connections: 100
```

2. **背壓處理**：
```java
public Flux<String> chatStreamWithBackpressure(@RequestParam String prompt) {
    return chatClient.prompt()
            .user(prompt)
            .stream()
            .content()
            .onBackpressureBuffer(1000)  // 設定緩衝區大小
            .publishOn(Schedulers.boundedElastic());
}
```

## 錯誤處理

```java
@GetMapping(value = "/chat/stream-safe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chatStreamSafe(@RequestParam String prompt) {
    return chatClient.prompt()
            .user(prompt)
            .stream()
            .content()
            .timeout(Duration.ofSeconds(30))  // 設定超時
            .retry(2)  // 重試機制
            .onErrorReturn("很抱歉，服務暫時不可用，請稍後再試。");
}
```

## 回顧

今天學到的內容：

- 使用 ChatClient 的 `stream()` API 實現流式輸出
- 了解 SSE（Server-Sent Events）的應用
- 配置字元編碼解決中文顯示問題
- 實作錯誤處理和效能優化
- 前端整合 EventSource 接收流式資料

Spring AI 1.0.1 的 ChatClient 讓流式輸出的實作變得更加優雅和易用，相比舊版本有了大幅改善。

## 新舊版本對比

| 功能 | 舊版本 (pre-1.0) | 新版本 (1.0.1) |
|------|------------------|----------------|
| API 風格 | `chatModel.stream(prompt)` | `chatClient.prompt().user(prompt).stream().content()` |
| 可讀性 | 較低 | 高（Fluent API） |
| 功能豐富度 | 基礎 | 豐富（系統提示詞、選項配置等） |
| 錯誤處理 | 需手動實作 | 內建支援 |

## Source Code

完整的現代化流式聊天實作：

```java
@RestController
@RequiredArgsConstructor
@Slf4j
public class ModernStreamingController {
    private final ChatClient chatClient;

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestParam String prompt,
            @RequestParam(defaultValue = "你是一個友善的AI助手") String system) {
        
        return chatClient.prompt()
                .system(system)
                .user(prompt)
                .stream()
                .content()
                .doOnSubscribe(subscription -> log.info("開始流式聊天: {}", prompt))
                .doOnComplete(() -> log.info("流式聊天完成"))
                .onErrorResume(error -> {
                    log.error("流式聊天錯誤", error);
                    return Flux.just("抱歉，發生錯誤：" + error.getMessage());
                });
    }
}
```

