## 使用 Spring AI 打造企業 RAG 知識庫【3】- Hello AI World（2025最新版本更新）

> **更新說明：** 本文章已根據 Spring AI 1.0.1 最新穩定版本進行全面更新，包含最新的 ChatClient 和 ChatModel API 使用方式。

![https://ithelp.ithome.com.tw/upload/images/20240810/201612902bf1GgYwyV.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/201612902bf1GgYwyV.jpg)

寫程式不免俗要來個 Hello World，依據開發 Spring Boot 程式的經驗，我們只需定義變數，並透過自動注入的機制就能開始使用，為了測試方便直接寫個 API

## Spring AI 1.0.1 架構說明

在 Spring AI 最新版本中，ChatClient 和 ChatModel 的關係如下：

- **ChatModel**: 提供底層的 AI 模型介面，是與各種 AI 提供商（OpenAI、Azure、Google 等）溝通的核心抽象層
- **ChatClient**: 提供更高級的 Fluent API，類似於 Spring 生態系統中的 RestClient、WebClient 和 JdbcClient，內部依賴 ChatModel 來執行實際的 AI 呼叫

## 依賴配置

首先確保使用最新的 Spring AI 版本。在 `pom.xml` 中加入：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
    <relativePath/>
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        <version>1.0.1</version>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## 程式碼實作 - 使用 ChatClient（推薦方式）

現在 Spring AI 推薦使用 ChatClient 的 Fluent API：

```java
@RestController
@RequiredArgsConstructor
public class AiController {
    private final ChatClient chatClient;

    @GetMapping("/chat")
    public String chat(@RequestParam String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
```

### 程式碼重點說明

> **@RestController**: 專門用來開發 API 的標註
>
> **@RequiredArgsConstructor**: Lombok 提供的快速標註，可幫我們寫一個建構子並將 final 變數當作參數
>
> **chatClient.prompt()**: 開始建立聊天請求的 Fluent API
>
> **user(prompt)**: 設定使用者訊息
>
> **call()**: 執行同步請求
>
> **content()**: 取得回應內容

## ChatClient 的自動配置

Spring Boot 會自動為我們配置 ChatClient，但我們也可以自定義配置：

```java
@Configuration
public class AiConfig {
    
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是一個友善的AI助手")
                .build();
    }
}
```

## ChatModel 直接使用方式（較底層API）

如果需要更直接的控制，也可以直接使用 ChatModel：

```java
@RestController
@RequiredArgsConstructor
public class AiModelController {
    private final ChatModel chatModel;

    @GetMapping("/chat-model")
    public String chatWithModel(@RequestParam String prompt) {
        return chatModel.call(prompt);
    }
}
```

## 配置檔案設定

在 `application.yml` 中設定 API 金鑰：

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
```

## 測試成果

現在使用 Postman 進行測試：

URL: `http://localhost:8080/chat?prompt=使用Spring AI寫一個Hello World程式`

你會看到 AI 返回詳細的程式指引。

## ChatClient vs ChatModel 使用時機

- **使用 ChatClient 當你需要**：
  - 複雜的提示詞組合
  - 系統提示詞設定
  - 流式輸出
  - 更豐富的 API 功能

- **使用 ChatModel 當你需要**：
  - 簡單直接的 AI 呼叫
  - 更底層的控制
  - 原始的 Prompt 物件操作

## 回顧

總結今天學到的內容:

- 了解 Spring AI 1.0.1 中 ChatClient 與 ChatModel 的關係
- 使用現代化的 ChatClient Fluent API 與 AI 互動
- 配置最新版本的依賴和設定
- 透過 API 完成第一次 AI 對話測試

## 版本遷移說明

如果你正在從舊版本遷移：

1. **從 `ChatClient` 改為 `ChatModel`**（如果直接使用底層API）
2. **推薦使用新的 `ChatClient` Fluent API**（更現代化的方式）
3. **更新依賴版本**至 1.0.1
4. **調整配置檔案**格式

## Source Code

今天的程式碼範例基於 Spring AI 1.0.1：

```java
// 完整的控制器範例
@RestController
@RequiredArgsConstructor
public class ModernAiController {
    private final ChatClient chatClient;
    private final ChatModel chatModel;

    // 推薦使用方式
    @GetMapping("/chat")
    public String chat(@RequestParam String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
    
    // 底層API方式
    @GetMapping("/chat-model")
    public String chatModel(@RequestParam String prompt) {
        return chatModel.call(prompt);
    }
}
```

