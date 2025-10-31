# 使用 Spring AI 打造企業 RAG 知識庫【37】- MCP 模型上下文協議：開啟 AI 應用新紀元

## MCP：AI應用的新標準

![https://ithelp.ithome.com.tw/upload/images/20240810/20161290MCP2025.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/20161290MCP2025.jpg)

Model Context Protocol (MCP) 是 Anthropic 推出的一個標準化協議，旨在讓 AI 模型能夠以結構化的方式與外部工具和資源進行互動。Spring AI 1.1 版本正式支援 MCP，為企業級 AI 應用開發帶來了全新的可能性。

今天我們來深入了解 MCP 的核心概念、架構設計，以及它如何革命性地改變 AI 應用的開發方式。

## ▋什麼是 MCP (Model Context Protocol)

MCP 是一個開放標準，定義了 AI 模型與外部系統之間的通信協議。它解決了傳統 AI 應用中的幾個關鍵問題：

### 傳統方案的問題
- **工具調用不標準化**: 每個框架都有自己的 Function Calling 實現
- **資源存取複雜**: 缺乏統一的資源管理機制
- **擴展性限制**: 難以動態添加新的工具和資源
- **互操作性差**: 不同系統間難以整合

### MCP 的解決方案
- **標準化協議**: 統一的通信標準，支援多種傳輸方式
- **模組化設計**: 清晰的 Client/Server 架構分離
- **動態能力**: 支援工具、資源、提示的動態發現和調用
- **跨平台支援**: 支援多種程式語言和運行環境

## ▋MCP 核心概念

### 1. **協議架構**

MCP 採用三層架構設計：

```
┌─────────────────────────────────────┐
│          Client/Server Layer        │  
│    McpClient ←→ McpServer          │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│           Session Layer              │
│  McpClientSession ←→ McpServerSession│
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│          Transport Layer             │
│   STDIO | HTTP/SSE | WebSocket     │
└─────────────────────────────────────┘
```

### 2. **核心元件**

| 元件 | 角色 | 功能 |
|-----|------|------|
| **MCP Client** | 協議客戶端 | 連接到 MCP Server，調用工具和資源 |
| **MCP Server** | 協議伺服器 | 提供工具、資源和提示給客戶端 |
| **MCP Session** | 會話管理 | 管理通信狀態和生命週期 |
| **MCP Transport** | 傳輸層 | 處理實際的消息傳輸 |

### 3. **支援的能力類型**

- **Tools**: 可執行的工具函數
- **Resources**: 可讀取的資源（檔案、API等）
- **Prompts**: 提示範本
- **Completions**: 完成建議

## ▋Spring AI MCP 整合架構

### 依賴配置

Spring AI 提供了多個 MCP Boot Starter：

```xml
<!-- MCP Client 基礎版 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>

<!-- MCP Client WebFlux 版 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
</dependency>

<!-- MCP Server 基礎版 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server</artifactId>
</dependency>

<!-- MCP Server WebMVC 版 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>

<!-- MCP Server WebFlux 版 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
</dependency>
```

### Spring AI MCP 生態系統

```
┌──────────────────────────────────────────┐
│              Spring AI                    │
│  ┌─────────────┐    ┌─────────────┐     │
│  │ ChatClient  │    │ ToolCallback│     │
│  └─────────────┘    └─────────────┘     │
└──────────────┬───────────────────────────┘
               │ 整合
┌──────────────▼───────────────────────────┐
│           Spring AI MCP                   │
│  ┌─────────────┐    ┌─────────────┐     │
│  │ MCP Client  │◄──►│ MCP Server  │     │
│  │Boot Starter │    │Boot Starter │     │
│  └─────────────┘    └─────────────┘     │
└──────────────┬───────────────────────────┘
               │ 基於
┌──────────────▼───────────────────────────┐
│            MCP Java SDK                   │
│  Transport: STDIO | HTTP/SSE | WebSocket │
└──────────────────────────────────────────┘
```

## ▋MCP 傳輸協議詳解

### 1. **STDIO 傳輸**

適合命令列工具和桌面應用：

```yaml
spring:
  ai:
    mcp:
      client:
        stdio:
          connections:
            filesystem-server:
              command: npx
              args:
                - "-y"
                - "@modelcontextprotocol/server-filesystem"
                - "/Users/documents"
              env:
                DEBUG: "true"
```

**特點:**
- 🟢 簡單易用，適合本地開發
- 🟢 無需網路配置
- 🔴 不適合分散式部署
- 🔴 僅支援同步通信

### 2. **HTTP/SSE 傳輸**

適合 Web 應用和微服務：

```yaml
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            web-server:
              url: http://localhost:8080
              sse-endpoint: /sse
```

**特點:**
- 🟢 支援遠端連接
- 🟢 適合雲端部署
- 🟢 支援負載均衡
- 🟢 防火牆友好

### 3. **Streamable HTTP 傳輸**

高效能雙向通信：

```yaml
spring:
  ai:
    mcp:
      client:
        streamable-http:
          connections:
            streaming-server:
              url: http://localhost:8080
              endpoint: /mcp
```

**特點:**
- 🟢 低延遲
- 🟢 雙向串流
- 🟢 適合即時應用
- 🔴 網路需求較高

## ▋MCP vs 傳統 Function Calling

| 比較項目 | 傳統 Function Calling | MCP |
|---------|---------------------|-----|
| **標準化** | 各框架不同實現 | 統一協議標準 |
| **動態性** | 靜態工具註冊 | 動態工具發現 |
| **擴展性** | 框架綁定 | 跨框架支援 |
| **資源管理** | 無統一機制 | 標準化資源存取 |
| **互操作性** | 較差 | 優秀 |
| **開發複雜度** | 中等 | 稍高 |
| **維護成本** | 中等 | 較低 |

## ▋實際應用場景

### 1. **企業工具整合**

```java
@Service
@RequiredArgsConstructor
public class EnterpriseToolsService {
    
    private final List<McpSyncClient> mcpClients;
    
    public String executeBusinessProcess(String processName, Map<String, Object> params) {
        // 動態發現可用工具
        for (McpSyncClient client : mcpClients) {
            List<Tool> tools = client.listTools();
            
            Optional<Tool> targetTool = tools.stream()
                .filter(tool -> tool.name().equals(processName))
                .findFirst();
                
            if (targetTool.isPresent()) {
                // 執行業務流程
                CallToolRequest request = new CallToolRequest(
                    processName, 
                    params
                );
                CallToolResult result = client.callTool(request);
                return result.content().get(0).text();
            }
        }
        
        throw new BusinessException("Tool not found: " + processName);
    }
}
```

### 2. **多模態資源存取**

```java
@Component
public class MultiModalResourceManager {
    
    private final McpSyncClient documentClient;
    private final McpSyncClient imageClient;
    
    public String analyzeDocument(String documentUri) {
        // 讀取文檔資源
        ReadResourceRequest request = new ReadResourceRequest(documentUri);
        ReadResourceResult result = documentClient.readResource(request);
        
        String content = result.contents().get(0).text();
        
        // 使用 ChatClient 分析內容
        return chatClient.prompt()
            .user("分析以下文檔內容：" + content)
            .call()
            .content();
    }
    
    public String processImage(String imageUri) {
        // 讀取圖片資源
        ReadResourceRequest request = new ReadResourceRequest(imageUri);
        ReadResourceResult result = imageClient.readResource(request);
        
        byte[] imageData = result.contents().get(0).blob();
        
        // 處理圖片...
        return "圖片處理完成";
    }
}
```

### 3. **動態提示管理**

```java
@Service
public class DynamicPromptService {
    
    private final McpSyncClient promptClient;
    private final ChatClient chatClient;
    
    public String generateWithPrompt(String promptName, Map<String, Object> arguments) {
        // 獲取動態提示
        GetPromptRequest request = new GetPromptRequest(promptName, arguments);
        GetPromptResult promptResult = promptClient.getPrompt(request);
        
        // 使用動態提示生成回應
        List<PromptMessage> messages = promptResult.messages();
        
        return chatClient.prompt()
            .messages(messages)
            .call()
            .content();
    }
}
```

## ▋MCP 開發最佳實踐

### 1. **錯誤處理策略**

```java
@Component
@Slf4j
public class McpErrorHandler {
    
    @EventListener
    public void handleMcpError(McpErrorEvent event) {
        log.error("MCP Error: {} - {}", event.getCode(), event.getMessage());
        
        // 根據錯誤類型進行處理
        switch (event.getCode()) {
            case "TOOL_NOT_FOUND":
                // 工具不存在，嘗試重新發現
                rediscoverTools();
                break;
            case "CONNECTION_TIMEOUT":
                // 連接超時，嘗試重連
                reconnectClient();
                break;
            case "PERMISSION_DENIED":
                // 權限問題，記錄並通知管理員
                notifyAdmin(event);
                break;
        }
    }
    
    private void rediscoverTools() {
        // 重新發現工具實現
    }
    
    private void reconnectClient() {
        // 重連實現
    }
    
    private void notifyAdmin(McpErrorEvent event) {
        // 通知管理員實現
    }
}
```

### 2. **效能監控**

```java
@Component
@RequiredArgsConstructor
public class McpMonitoringService {
    
    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> toolTimers = new ConcurrentHashMap<>();
    
    @EventListener
    public void onToolCall(McpToolCallEvent event) {
        Timer timer = toolTimers.computeIfAbsent(
            event.getToolName(),
            name -> Timer.builder("mcp.tool.execution")
                .tag("tool", name)
                .register(meterRegistry)
        );
        
        timer.recordCallable(() -> {
            // 記錄工具執行時間
            return event.getResult();
        });
    }
    
    @Scheduled(fixedRate = 60000)
    public void reportHealthMetrics() {
        // 定期報告健康狀態
        Gauge.builder("mcp.connections.active")
            .register(meterRegistry, this, McpMonitoringService::getActiveConnections);
    }
    
    private double getActiveConnections() {
        // 計算活躍連接數
        return 0.0;
    }
}
```

### 3. **安全性配置**

```java
@Configuration
@EnableConfigurationProperties(McpSecurityProperties.class)
public class McpSecurityConfiguration {
    
    @Bean
    public McpSyncClientCustomizer securityCustomizer(McpSecurityProperties properties) {
        return (serverName, spec) -> {
            // 設定請求超時
            spec.requestTimeout(Duration.ofSeconds(properties.getRequestTimeout()));
            
            // 設定根目錄權限
            if (properties.getAllowedRoots().containsKey(serverName)) {
                List<String> allowedPaths = properties.getAllowedRoots().get(serverName);
                List<Root> roots = allowedPaths.stream()
                    .map(path -> new Root(URI.create("file://" + path), path))
                    .toList();
                spec.roots(roots);
            }
            
            // 設定日誌處理
            spec.loggingConsumer(logMessage -> {
                if (logMessage.level().ordinal() >= LoggingLevel.WARN.ordinal()) {
                    // 記錄警告及以上級別的日誌
                    log.warn("MCP Server [{}]: {}", serverName, logMessage.data());
                }
            });
        };
    }
}
```

## ▋總結

MCP 為 AI 應用開發帶來了革命性的變化：

### 🚀 **核心優勢**
- **標準化**: 統一的協議標準，提升互操作性
- **靈活性**: 支援多種傳輸方式，適應不同部署環境
- **可擴展性**: 動態工具發現，易於系統擴展
- **企業級**: 完整的安全性和監控支援

### 🎯 **適用場景**
- 企業級工具整合平台
- 多模態資源處理系統
- 動態 AI 助手服務
- 跨系統數據存取

### 📈 **發展前景**
- 成為 AI 應用開發的標準協議
- 豐富的生態系統和工具支援
- 更好的開發者體驗和維護性

下一篇文章，我們將深入實戰 MCP Client 的開發，學習如何配置和使用各種傳輸協議，打造企業級的 MCP 客戶端應用。