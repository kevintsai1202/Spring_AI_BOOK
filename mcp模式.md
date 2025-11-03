# MCP 傳輸模式分析

> 基於 `D:\GitHub\spring-ai-examples\model-context-protocol` 範例分析

## 📋 目錄

- [傳輸模式概述](#傳輸模式概述)
- [HTTP 模式類型](#http-模式類型)
- [主要範例專案](#主要範例專案)
- [配置方式](#配置方式)
- [程式碼範例](#程式碼範例)
- [啟動命令](#啟動命令)
- [模式比較](#模式比較)
- [參考資源](#參考資源)

---

## 傳輸模式概述

Spring AI MCP 支援兩大類傳輸模式：

1. **STDIO 模式**：標準輸入/輸出，適用於本機進程間通信
2. **HTTP 模式**：基於 HTTP 協議，支援遠端連接
   - SSE (Server-Sent Events)
   - Streamable-HTTP
   - Stateless

---

## HTTP 模式類型

### 1. SSE (Server-Sent Events)

**特點：**
- 單向串流通信（Server → Client）
- 基於 HTTP/1.1 長連接
- 適合 Server 主動推送事件

**適用場景：**
- 實時通知
- 進度更新
- 日誌串流

### 2. Streamable-HTTP

**特點：**
- 雙向串流通信
- 基於 HTTP/2 或 HTTP/1.1
- 支援長時間連線

**適用場景：**
- 互動式對話
- 雙向數據交換
- 實時協作

### 3. Stateless

**特點：**
- 無狀態請求-響應
- 傳統 REST API 風格
- 每次請求獨立

**適用場景：**
- 簡單查詢
- 無需保持連接狀態
- 防火牆友好環境

---

## 主要範例專案

### 1. mcp-annotations-server（最完整）

**路徑：** `model-context-protocol/mcp-annotations/mcp-annotations-server`

**功能：**
- 支援所有三種 HTTP 模式
- 完整的 MCP 功能實作（Tools、Resources、Prompts、Completions）
- 提供測試客戶端

**依賴：**
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

### 2. mcp-annotations-client

**路徑：** `model-context-protocol/mcp-annotations/mcp-annotations-client`

**功能：**
- Spring Boot 客戶端實作
- 支援 Streamable-HTTP 和 SSE
- 註解式處理器（@McpProgress, @McpLogging, @McpSampling）

**依賴：**
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
```

### 3. dynamic-tool-update

**路徑：** `model-context-protocol/dynamic-tool-update`

**功能：**
- 動態工具更新示範
- Server-Client 分離架構
- SSE 模式連接

### 4. client-starter (starter-default-client, starter-webflux-client)

**路徑：** `model-context-protocol/client-starter/`

**功能：**
- 快速啟動範例
- 支援 STDIO 和 HTTP 連接
- Webflux 響應式支援

---

## 配置方式

### Server 端配置

#### application.properties

```properties
# Server 基本資訊
spring.ai.mcp.server.name=my-weather-server
spring.ai.mcp.server.version=0.0.1

# 選擇傳輸協議（三選一）
spring.ai.mcp.server.protocol=STREAMABLE
# spring.ai.mcp.server.protocol=SSE
# spring.ai.mcp.server.protocol=STATELESS

# STDIO 模式（可選）
# spring.ai.mcp.server.stdio=true
# spring.main.web-application-type=none

# 日誌配置（STDIO 模式必須）
spring.main.banner-mode=off
# logging.pattern.console=

# 日誌檔案位置
logging.file.name=./logs/server.log
```

### Client 端配置

#### application.properties

```properties
spring.application.name=mcp-client
spring.main.web-application-type=none

# Streamable-HTTP 模式連接
spring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080
# spring.ai.mcp.client.streamable-http.connections.server2.url=http://localhost:8081

# SSE 模式連接（替代方案）
# spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080

# 全域設定
spring.ai.mcp.client.request-timeout=5m

# 啟用工具回調
spring.ai.mcp.client.toolcallback.enabled=true

# 日誌級別
logging.level.io.modelcontextprotocol.client=WARN
logging.level.io.modelcontextprotocol.spec=WARN
```

---

## 程式碼範例

### Server 端實作

```java
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    // 註冊 Spring AI Tool 為 MCP Tools
    @Bean
    public ToolCallbackProvider weatherTools(SpringAiToolProvider weatherService) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(weatherService)
            .build();
    }
}
```

### Tool Provider

```java
@Service
public class SpringAiToolProvider {

    @Tool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(double latitude, double longitude) {
        // 實作邏輯
        return "Weather data...";
    }

    @Tool(description = "Get weather alerts for a US state")
    public String getAlerts(String state) {
        // 實作邏輯
        return "Alert data...";
    }
}
```

### MCP Tool Provider

```java
@Service
public class McpToolProvider {

    @McpTool(description = "Get the temperature (in celsius) for a specific location")
    public WeatherResponse getTemperature(
            McpSyncServerExchange exchange,
            @McpProgressToken String progressToken,
            @McpToolParam(description = "The location latitude") double latitude,
            @McpToolParam(description = "The location longitude") double longitude,
            @McpToolParam(description = "The city name") String city) {
        // 實作邏輯
        return new WeatherResponse(/* ... */);
    }
}
```

### Resource Provider

```java
@Service
public class UserProfileResourceProvider {

    @McpResource(
        uri = "user-profile://{username}",
        name = "User Profile",
        description = "Provides user profile information for a specific user"
    )
    public ReadResourceResult getUserProfile(
            ReadResourceRequest request,
            String username) {
        // 實作邏輯
        return ReadResourceResult.builder()
            .contents(List.of(/* ... */))
            .build();
    }
}
```

### Prompt Provider

```java
@Service
public class PromptProvider {

    @McpPrompt(name = "greeting", description = "A simple greeting prompt")
    public GetPromptResult greetingPrompt(
            @McpArg(name = "name", description = "The name to greet", required = true)
            String name) {
        return GetPromptResult.builder()
            .messages(List.of(
                new PromptMessage(Role.USER, "Hello " + name + "!")
            ))
            .build();
    }
}
```

### Client 端實作（Spring Boot 方式）

```java
@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args).close();
    }

    @Bean
    public CommandLineRunner predefinedQuestions(List<McpSyncClient> mcpClients) {
        return args -> {
            for (McpSyncClient mcpClient : mcpClients) {
                System.out.println(">>> MCP Client: " + mcpClient.getClientInfo());

                // 呼叫工具
                CallToolRequest toolRequest = CallToolRequest.builder()
                    .name("getTemperature")
                    .arguments(Map.of(
                        "latitude", 37.7749,
                        "longitude", -122.4194,
                        "city", "San Francisco"
                    ))
                    .progressToken("test-progress-token")
                    .build();

                CallToolResult response = mcpClient.callTool(toolRequest);
                System.out.println("Tool response: " + response);
            }
        };
    }
}
```

### Client Handler Providers

```java
@Service
public class McpClientHandlerProviders {

    private static final Logger logger = LoggerFactory.getLogger(McpClientHandlerProviders.class);

    // 處理進度通知
    @McpProgress(clients = "server1")
    public void progressHandler(ProgressNotification progressNotification) {
        logger.info("MCP PROGRESS: [{}] progress: {} total: {} message: {}",
            progressNotification.progressToken(),
            progressNotification.progress(),
            progressNotification.total(),
            progressNotification.message());
    }

    // 處理日誌訊息
    @McpLogging(clients = "server1")
    public void loggingHandler(LoggingMessageNotification loggingMessage) {
        logger.info("MCP LOGGING: [{}] {}",
            loggingMessage.level(),
            loggingMessage.data());
    }

    // 處理取樣請求
    @McpSampling(clients = "server1")
    public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) {
        logger.info("MCP SAMPLING: {}", llmRequest);
        String userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();

        return CreateMessageResult.builder()
            .content(new McpSchema.TextContent("Response to: " + userPrompt))
            .build();
    }

    // 處理引導請求
    @McpElicitation(clients = "server1")
    public ElicitResult elicitationHandler(McpSchema.ElicitRequest request) {
        logger.info("MCP ELICITATION: {}", request);
        return new ElicitResult(
            ElicitResult.Action.ACCEPT,
            Map.of("message", request.message())
        );
    }
}
```

### Client 端實作（直接使用傳輸層）

#### SSE 模式

```java
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;

public class ClientSse {

    public static void main(String[] args) {
        var transport = HttpClientSseClientTransport
            .builder("http://localhost:8080")
            .build();

        new SampleClient(transport).run();
    }
}
```

#### Streamable-HTTP 模式

```java
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;

public class ClientStreamableHttp {

    public static void main(String[] args) {
        HttpClientStreamableHttpTransport transport =
            HttpClientStreamableHttpTransport
                .builder("http://localhost:8080")
                .build();

        new SampleClient(transport).run();
    }
}
```

---

## 啟動命令

### Server 端

#### 1. Streamable-HTTP 模式

```bash
java -Dspring.ai.mcp.server.protocol=STREAMABLE \
  -jar target/mcp-annotations-server-0.0.1-SNAPSHOT.jar
```

#### 2. SSE 模式

```bash
java -Dspring.ai.mcp.server.protocol=SSE \
  -jar target/mcp-annotations-server-0.0.1-SNAPSHOT.jar
```

#### 3. Stateless 模式

```bash
java -Dspring.ai.mcp.server.protocol=STATELESS \
  -jar target/mcp-annotations-server-0.0.1-SNAPSHOT.jar
```

#### 4. STDIO 模式

```bash
java -Dspring.ai.mcp.server.stdio=true \
  -Dspring.main.web-application-type=none \
  -jar target/mcp-annotations-server-0.0.1-SNAPSHOT.jar
```

### Client 端

#### 1. Streamable-HTTP 連接

```bash
java -Dspring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080 \
  -jar target/mcp-annotations-client-0.0.1-SNAPSHOT.jar
```

#### 2. SSE 連接

```bash
java -Dspring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080 \
  -jar target/mcp-annotations-client-0.0.1-SNAPSHOT.jar
```

#### 3. 多 Server 連接

```bash
java -Dspring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080 \
  -Dspring.ai.mcp.client.streamable-http.connections.server2.url=http://localhost:8081 \
  -jar target/mcp-annotations-client-0.0.1-SNAPSHOT.jar
```

### Windows PowerShell 命令

#### Server 端（Streamable-HTTP）

```powershell
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"
cd "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\demo"
java -Dspring.ai.mcp.server.protocol=STREAMABLE -jar target\mcp-server-0.0.1-SNAPSHOT.jar
```

#### Client 端（Streamable-HTTP）

```powershell
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"
cd "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\demo"
java -Dspring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080 -jar target\mcp-client-0.0.1-SNAPSHOT.jar
```

---

## 模式比較

### 傳輸模式比較表

| 傳輸模式 | 通信方式 | 連接狀態 | 適用場景 | 防火牆友好 |
|---------|---------|---------|---------|-----------|
| **STDIO** | 標準輸入/輸出 | 單一進程 | 本機整合（如 Claude Desktop） | ✅ |
| **SSE** | HTTP 長連接 | 有狀態 | 實時推送、單向串流 | ⚠️ |
| **Streamable-HTTP** | HTTP/2 雙向串流 | 有狀態 | 互動式對話、雙向交換 | ⚠️ |
| **Stateless** | HTTP 請求-響應 | 無狀態 | 簡單查詢、REST API | ✅ |

### 配置方式比較

| 項目 | STDIO | HTTP (SSE/Streamable/Stateless) |
|-----|-------|----------------------------------|
| **Server 配置** | `spring.ai.mcp.server.stdio=true` | `spring.ai.mcp.server.protocol=SSE/STREAMABLE/STATELESS` |
| **啟動方式** | 子進程啟動 | 獨立 Web 服務 |
| **網路需求** | 不需要 | 需要網路連接 |
| **多客戶端** | 不支援 | 支援 |
| **負載均衡** | 不支援 | 支援 |
| **安全性** | 進程隔離 | 需配置 OAuth2/TLS |

### 功能支援比較

| 功能 | STDIO | SSE | Streamable-HTTP | Stateless |
|-----|-------|-----|-----------------|-----------|
| **Tools** | ✅ | ✅ | ✅ | ✅ |
| **Resources** | ✅ | ✅ | ✅ | ✅ |
| **Prompts** | ✅ | ✅ | ✅ | ✅ |
| **Completions** | ✅ | ✅ | ✅ | ✅ |
| **Progress 通知** | ✅ | ✅ | ✅ | ❌ |
| **Logging 通知** | ✅ | ✅ | ✅ | ❌ |
| **Sampling 請求** | ✅ | ✅ | ✅ | ❌ |
| **雙向串流** | ✅ | ⚠️ (單向) | ✅ | ❌ |

---

## MCP 客戶端註解

### 可用註解列表

| 註解 | 功能 | 必需參數 |
|-----|------|---------|
| `@McpLogging` | 處理日誌訊息通知 | `clients` |
| `@McpProgress` | 處理進度通知 | `clients` |
| `@McpSampling` | 處理 LLM 取樣請求 | `clients` |
| `@McpElicitation` | 處理引導請求 | `clients` |
| `@McpToolListChanged` | 處理工具列表變更通知 | `clients` |
| `@McpResourceListChanged` | 處理資源列表變更通知 | `clients` |
| `@McpPromptListChanged` | 處理提示列表變更通知 | `clients` |

**重要：** 所有 MCP 客戶端註解都**必須**包含 `clients` 參數，用於關聯特定的 MCP 客戶端連接。

### 註解使用範例

```java
@Service
public class McpClientHandlerProviders {

    // clients="server1" 對應配置中的連接名稱
    // spring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080

    @McpProgress(clients = "server1")
    public void progressHandler(ProgressNotification notification) {
        // 處理進度通知
    }

    @McpLogging(clients = "server1")
    public void loggingHandler(LoggingMessageNotification message) {
        // 處理日誌訊息
    }

    // 支援多個 Server
    @McpProgress(clients = {"server1", "server2"})
    public void multiServerProgressHandler(ProgressNotification notification) {
        // 處理來自多個 Server 的進度通知
    }
}
```

---

## 最佳實踐

### 1. 選擇合適的傳輸模式

- **本機整合**：使用 STDIO
- **實時推送**：使用 SSE
- **互動對話**：使用 Streamable-HTTP
- **簡單查詢**：使用 Stateless

### 2. Server 端建議

```java
// 使用 Spring Boot Starter 簡化配置
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    // 註冊工具提供者
    @Bean
    public ToolCallbackProvider toolProvider(MyToolService service) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(service)
            .build();
    }
}
```

### 3. Client 端建議

```properties
# 設定合理的超時時間
spring.ai.mcp.client.request-timeout=5m

# 啟用工具回調（如需整合到 Spring AI）
spring.ai.mcp.client.toolcallback.enabled=true

# 配置多個 Server 連接
spring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080
spring.ai.mcp.client.streamable-http.connections.server2.url=http://localhost:8081
```

### 4. 安全性考量

```properties
# 使用 HTTPS
spring.ai.mcp.client.sse.connections.server1.url=https://api.example.com

# 配置 OAuth2（參考 weather/starter-webmvc-oauth2-server 範例）
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://auth.example.com
```

### 5. 日誌配置

```properties
# STDIO 模式必須禁用 console 日誌
spring.main.banner-mode=off
logging.pattern.console=

# 使用檔案日誌
logging.file.name=./logs/mcp-server.log
logging.level.io.modelcontextprotocol=INFO
```

---

## 範例專案清單

### 完整功能範例

1. **mcp-annotations/mcp-annotations-server**
   - 功能最完整的 Server 範例
   - 支援所有 HTTP 模式和 STDIO
   - 包含 Tools、Resources、Prompts、Completions

2. **mcp-annotations/mcp-annotations-client**
   - Spring Boot 客戶端
   - 註解式處理器
   - Streamable-HTTP 和 SSE 支援

### 專門功能範例

3. **dynamic-tool-update**
   - 動態工具更新
   - Server-Client 分離

4. **sampling**
   - 取樣功能示範
   - LLM 整合

5. **filesystem**
   - 檔案系統操作
   - Resource 使用範例

6. **brave** 系列
   - Brave Search 整合
   - Docker 部署
   - Agent Gateway

### 快速啟動範例

7. **client-starter/starter-default-client**
   - 快速啟動範例
   - STDIO 模式

8. **client-starter/starter-webflux-client**
   - Webflux 響應式客戶端
   - 非阻塞 I/O

### 進階範例

9. **weather** 系列
   - 天氣 API 整合
   - OAuth2 安全性
   - WebMVC/WebFlux 實作

---

## 參考資源

### 官方文件

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
- [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)

### 相關專案

- [MCP Annotations Project](https://github.com/spring-ai-community/mcp-annotations)
- [Spring Boot Auto-configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)

### 範例程式碼位置

- **本機路徑：** `D:\GitHub\spring-ai-examples\model-context-protocol`
- **專案路徑：** `E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration`

---

## 附錄：常見問題

### Q1: 如何選擇 SSE 和 Streamable-HTTP？

**A:**
- 如果只需要 Server 向 Client 推送資料，選擇 **SSE**
- 如果需要雙向實時通信，選擇 **Streamable-HTTP**

### Q2: Stateless 模式有什麼限制？

**A:**
- 不支援進度通知 (Progress)
- 不支援日誌通知 (Logging)
- 不支援取樣請求 (Sampling)
- 適合簡單的請求-響應場景

### Q3: 如何在同一個 Client 連接多個 Server？

**A:**
```properties
spring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080
spring.ai.mcp.client.streamable-http.connections.server2.url=http://localhost:8081
spring.ai.mcp.client.streamable-http.connections.server3.url=http://localhost:8082
```

然後在 Handler 中使用：
```java
@McpProgress(clients = {"server1", "server2", "server3"})
public void progressHandler(ProgressNotification notification) {
    // 處理來自所有 Server 的通知
}
```

### Q4: STDIO 模式可以同時使用 HTTP 模式嗎？

**A:**
理論上可以，但不建議。Server 應該選擇一種主要的傳輸模式。如果需要支援多種模式，建議部署多個 Server 實例。

### Q5: 如何除錯 MCP 連接問題？

**A:**
```properties
# 啟用詳細日誌
logging.level.io.modelcontextprotocol.client=DEBUG
logging.level.io.modelcontextprotocol.spec=DEBUG
logging.level.org.springframework.ai.mcp=DEBUG

# 檢查連接狀態
logging.level.org.springframework.web.client=DEBUG
```

---

**文件版本：** 1.0
**最後更新：** 2025-10-31
**作者：** Kevin Tsai
**基於範例：** spring-ai-examples (GitHub)
