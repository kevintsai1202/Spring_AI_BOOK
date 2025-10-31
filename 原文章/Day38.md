# 使用 Spring AI 打造企業 RAG 知識庫【38】- MCP Client 開發實戰：打造智能工具連接器

## MCP Client：AI 應用的智能橋樑

![https://ithelp.ithome.com.tw/upload/images/20240810/20161290MCPClient2025.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/20161290MCPClient2025.jpg)

MCP Client 是 AI 應用與外部工具、資源之間的重要橋樑。它負責連接到 MCP Server，發現可用的工具和資源，並將它們整合到 AI 工作流程中。

今天我們將深入學習如何使用 Spring AI 開發企業級的 MCP Client 應用，包括各種傳輸協議的配置、客戶端自定義，以及與 ChatClient 的完美整合。

## ▋MCP Client Boot Starter 快速入門

### 依賴配置

根據不同的需求選擇合適的 Starter：

```xml
<!-- 標準版 - 支援 STDIO 和 HTTP/SSE -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>

<!-- WebFlux 版 - 推薦用於生產環境 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
</dependency>

<!-- 其他必要依賴 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
```

### 基礎配置

在 `application.yml` 中配置 MCP Client：

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        name: enterprise-mcp-client
        version: 1.0.0
        type: SYNC  # SYNC 或 ASYNC
        request-timeout: 30s
        initialized: true
        root-change-notification: true
        toolcallback:
          enabled: true  # 整合 Spring AI 工具框架
```

## ▋多種傳輸協議配置

### 1. **STDIO 傳輸配置**

適合本地工具和命令列應用：

```yaml
spring:
  ai:
    mcp:
      client:
        stdio:
          connections:
            # 檔案系統伺服器
            filesystem:
              command: npx
              args:
                - "-y" 
                - "@modelcontextprotocol/server-filesystem"
                - "/Users/documents"
                - "/Users/downloads"
              env:
                DEBUG: "true"
                
            # SQLite 資料庫伺服器
            sqlite:
              command: npx
              args:
                - "-y"
                - "@modelcontextprotocol/server-sqlite"
                - "/path/to/database.db"
              env:
                SQLITE_READONLY: "false"
                
            # 自定義 Python 工具
            custom-tools:
              command: python
              args:
                - "/path/to/custom_mcp_server.py"
              env:
                API_KEY: "${CUSTOM_API_KEY}"
                LOG_LEVEL: "INFO"
```

### 2. **HTTP/SSE 傳輸配置**

適合遠端服務和微服務架構：

```yaml
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            # 企業內部工具服務
            enterprise-tools:
              url: http://internal-tools.company.com:8080
              sse-endpoint: /mcp/sse
              
            # 外部 API 服務
            external-api:
              url: https://api.external-service.com
              sse-endpoint: /mcp/events
              
            # 本地開發服務
            local-dev:
              url: http://localhost:8081
              sse-endpoint: /sse
```

### 3. **Streamable HTTP 傳輸配置**

適合高效能和即時通信需求：

```yaml
spring:
  ai:
    mcp:
      client:
        streamable-http:
          connections:
            # 即時數據處理服務
            realtime-processor:
              url: http://realtime.company.com:8082
              endpoint: /mcp/stream
              
            # 大數據分析服務
            bigdata-analytics:
              url: http://analytics.company.com:8083
              endpoint: /mcp/analytics
```

### 4. **混合傳輸配置**

企業級應用通常需要混合多種傳輸方式：

```yaml
spring:
  ai:
    mcp:
      client:
        # 本地工具 - STDIO
        stdio:
          connections:
            git-tools:
              command: /usr/local/bin/git-mcp-server
              args: ["--repo", "/path/to/repo"]
              
        # 企業服務 - SSE  
        sse:
          connections:
            crm-system:
              url: http://crm.company.com:8080
            erp-system:
              url: http://erp.company.com:8081
              
        # 高效能服務 - Streamable HTTP
        streamable-http:
          connections:
            ml-pipeline:
              url: http://ml.company.com:8082
              endpoint: /mcp/ml
```

## ▋MCP Client 開發實戰

### 1. **基礎 MCP Client 服務**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class McpClientService {
    
    private final List<McpSyncClient> syncClients;
    private final SyncMcpToolCallbackProvider toolProvider;
    
    /**
     * 獲取所有可用工具
     */
    public List<ToolDefinition> getAvailableTools() {
        return Arrays.stream(toolProvider.getToolCallbacks())
            .map(ToolCallback::getToolDefinition)
            .toList();
    }
    
    /**
     * 執行指定工具
     */
    public String executeTool(String toolName, String arguments) {
        ToolCallback[] callbacks = toolProvider.getToolCallbacks();
        
        for (ToolCallback callback : callbacks) {
            if (callback.getToolDefinition().getName().equals(toolName)) {
                try {
                    return callback.call(arguments);
                } catch (Exception e) {
                    log.error("工具執行失敗: {} - {}", toolName, e.getMessage());
                    throw new McpToolExecutionException("工具執行失敗: " + toolName, e);
                }
            }
        }
        
        throw new McpToolNotFoundException("找不到工具: " + toolName);
    }
    
    /**
     * 獲取伺服器資訊
     */
    public List<McpServerInfo> getServerInfo() {
        return syncClients.stream()
            .map(client -> new McpServerInfo(
                client.getServerInfo().name(),
                client.getServerInfo().version(),
                client.isConnected(),
                client.getCapabilities()
            ))
            .toList();
    }
    
    public record McpServerInfo(
        String name,
        String version, 
        boolean connected,
        Object capabilities
    ) {}
}
```

### 2. **與 ChatClient 整合**

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class McpChatController {
    
    private final ChatClient chatClient;
    private final McpClientService mcpService;
    
    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            // 獲取 MCP 工具
            ToolCallback[] mcpTools = mcpService.getToolCallbacks();
            
            // 使用 ChatClient 與 MCP 工具整合
            String response = chatClient.prompt()
                .user(request.getMessage())
                .tools(mcpTools)  // 自動整合 MCP 工具
                .call()
                .content();
            
            return ResponseEntity.ok(new ChatResponse(response));
            
        } catch (Exception e) {
            log.error("聊天處理失敗", e);
            return ResponseEntity.status(500)
                .body(new ChatResponse("抱歉，處理您的請求時發生錯誤"));
        }
    }
    
    @GetMapping("/tools")
    public ResponseEntity<List<ToolInfo>> getAvailableTools() {
        List<ToolDefinition> tools = mcpService.getAvailableTools();
        
        List<ToolInfo> toolInfos = tools.stream()
            .map(tool -> new ToolInfo(
                tool.getName(),
                tool.getDescription(),
                tool.getInputTypeSchema()
            ))
            .toList();
            
        return ResponseEntity.ok(toolInfos);
    }
    
    public record ChatRequest(String message) {}
    public record ChatResponse(String response) {}
    public record ToolInfo(String name, String description, String schema) {}
}
```

### 3. **高級 MCP Client 自定義**

```java
@Component
@Slf4j
public class EnterpriseMcpClientCustomizer implements McpSyncClientCustomizer {
    
    @Override
    public void customize(String serverName, McpClient.SyncSpec spec) {
        log.info("自定義 MCP Client: {}", serverName);
        
        // 設定請求超時
        spec.requestTimeout(Duration.ofSeconds(45));
        
        // 根據伺服器類型設定不同的根目錄權限
        configureRoots(serverName, spec);
        
        // 設定取樣處理器（用於 LLM 調用）
        spec.sampling(this::handleSamplingRequest);
        
        // 設定事件監聽器
        configureEventListeners(serverName, spec);
        
        // 設定日誌處理
        spec.loggingConsumer(logMessage -> 
            handleServerLog(serverName, logMessage));
    }
    
    private void configureRoots(String serverName, McpClient.SyncSpec spec) {
        Map<String, List<String>> serverRoots = Map.of(
            "filesystem", List.of("/safe/documents", "/safe/uploads"),
            "git-tools", List.of("/repositories", "/workspace"),
            "database-tools", List.of("/db/readonly")
        );
        
        if (serverRoots.containsKey(serverName)) {
            List<Root> roots = serverRoots.get(serverName).stream()
                .map(path -> new Root(URI.create("file://" + path), path))
                .toList();
            spec.roots(roots);
        }
    }
    
    private CreateMessageResult handleSamplingRequest(CreateMessageRequest request) {
        log.info("處理取樣請求: {}", request.messages().size());
        
        try {
            // 使用企業內部的 LLM 服務處理取樣請求
            String prompt = request.messages().stream()
                .map(msg -> msg.content().text())
                .collect(Collectors.joining("\n"));
                
            String response = enterpriseLlmService.process(prompt);
            
            return new CreateMessageResult(
                "enterprise-llm",
                StopReason.STOP,
                List.of(new TextContent(response))
            );
            
        } catch (Exception e) {
            log.error("取樣處理失敗", e);
            return new CreateMessageResult(
                "error",
                StopReason.ERROR,
                List.of(new TextContent("取樣處理失敗: " + e.getMessage()))
            );
        }
    }
    
    private void configureEventListeners(String serverName, McpClient.SyncSpec spec) {
        // 工具變更監聽
        spec.toolsChangeConsumer(tools -> {
            log.info("伺服器 {} 工具列表更新，共 {} 個工具", serverName, tools.size());
            notifyToolsChanged(serverName, tools);
        });
        
        // 資源變更監聽
        spec.resourcesChangeConsumer(resources -> {
            log.info("伺服器 {} 資源列表更新，共 {} 個資源", serverName, resources.size());
            notifyResourcesChanged(serverName, resources);
        });
        
        // 提示變更監聽
        spec.promptsChangeConsumer(prompts -> {
            log.info("伺服器 {} 提示列表更新，共 {} 個提示", serverName, prompts.size());
            notifyPromptsChanged(serverName, prompts);
        });
    }
    
    private void handleServerLog(String serverName, McpSchema.LoggingMessageNotification log) {
        LogLevel level = LogLevel.valueOf(log.level().name());
        String message = String.format("[%s] %s", serverName, log.data());
        
        switch (level) {
            case ERROR -> this.log.error(message);
            case WARN -> this.log.warn(message);
            case INFO -> this.log.info(message);
            case DEBUG -> this.log.debug(message);
        }
        
        // 如果是錯誤，記錄到監控系統
        if (level == LogLevel.ERROR) {
            monitoringService.recordError(serverName, log.data());
        }
    }
    
    // 通知方法實現...
    private void notifyToolsChanged(String serverName, List<McpSchema.Tool> tools) {
        // 發送工具變更事件
    }
    
    private void notifyResourcesChanged(String serverName, List<McpSchema.Resource> resources) {
        // 發送資源變更事件
    }
    
    private void notifyPromptsChanged(String serverName, List<McpSchema.Prompt> prompts) {
        // 發送提示變更事件
    }
}
```

### 4. **MCP 資源存取服務**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class McpResourceService {
    
    private final List<McpSyncClient> syncClients;
    
    /**
     * 讀取指定資源
     */
    public McpResourceContent readResource(String resourceUri) {
        URI uri = URI.create(resourceUri);
        
        for (McpSyncClient client : syncClients) {
            try {
                // 檢查客戶端是否支援該資源
                if (supportsResource(client, uri)) {
                    ReadResourceRequest request = new ReadResourceRequest(resourceUri);
                    ReadResourceResult result = client.readResource(request);
                    
                    if (!result.contents().isEmpty()) {
                        ResourceContent content = result.contents().get(0);
                        
                        return new McpResourceContent(
                            resourceUri,
                            content.mimeType().orElse("text/plain"),
                            content.text().orElse(null),
                            content.blob().orElse(null)
                        );
                    }
                }
            } catch (Exception e) {
                log.warn("讀取資源失敗 - 客戶端: {}, 資源: {}", 
                    client.getServerInfo().name(), resourceUri, e);
            }
        }
        
        throw new McpResourceNotFoundException("找不到資源: " + resourceUri);
    }
    
    /**
     * 列出所有可用資源
     */
    public List<McpResourceInfo> listAllResources() {
        List<McpResourceInfo> allResources = new ArrayList<>();
        
        for (McpSyncClient client : syncClients) {
            try {
                ListResourcesResult result = client.listResources(
                    new ListResourcesRequest(null)
                );
                
                List<McpResourceInfo> clientResources = result.resources().stream()
                    .map(resource -> new McpResourceInfo(
                        resource.uri(),
                        resource.name(),
                        resource.description().orElse(null),
                        resource.mimeType().orElse(null),
                        client.getServerInfo().name()
                    ))
                    .toList();
                    
                allResources.addAll(clientResources);
                
            } catch (Exception e) {
                log.error("列出資源失敗 - 客戶端: {}", 
                    client.getServerInfo().name(), e);
            }
        }
        
        return allResources;
    }
    
    /**
     * 搜尋資源
     */
    public List<McpResourceInfo> searchResources(String query) {
        return listAllResources().stream()
            .filter(resource -> 
                resource.name().toLowerCase().contains(query.toLowerCase()) ||
                (resource.description() != null && 
                 resource.description().toLowerCase().contains(query.toLowerCase()))
            )
            .toList();
    }
    
    private boolean supportsResource(McpSyncClient client, URI uri) {
        // 根據 URI scheme 判斷客戶端是否支援該資源
        try {
            ListResourcesResult result = client.listResources(
                new ListResourcesRequest(null)
            );
            
            return result.resources().stream()
                .anyMatch(resource -> resource.uri().equals(uri.toString()));
                
        } catch (Exception e) {
            return false;
        }
    }
    
    public record McpResourceContent(
        String uri,
        String mimeType,
        String textContent,
        byte[] binaryContent
    ) {}
    
    public record McpResourceInfo(
        String uri,
        String name,
        String description,
        String mimeType,
        String serverName
    ) {}
}
```

### 5. **MCP 提示管理服務**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class McpPromptService {
    
    private final List<McpSyncClient> syncClients;
    private final ChatClient chatClient;
    
    /**
     * 使用 MCP 提示生成回應
     */
    public String generateWithPrompt(String promptName, Map<String, Object> arguments) {
        for (McpSyncClient client : syncClients) {
            try {
                // 檢查客戶端是否有該提示
                if (hasPrompt(client, promptName)) {
                    GetPromptRequest request = new GetPromptRequest(promptName, arguments);
                    GetPromptResult result = client.getPrompt(request);
                    
                    // 將 MCP 提示轉換為 ChatClient 消息
                    List<Message> messages = convertToMessages(result.messages());
                    
                    return chatClient.prompt()
                        .messages(messages)
                        .call()
                        .content();
                }
            } catch (Exception e) {
                log.warn("使用提示失敗 - 客戶端: {}, 提示: {}", 
                    client.getServerInfo().name(), promptName, e);
            }
        }
        
        throw new McpPromptNotFoundException("找不到提示: " + promptName);
    }
    
    /**
     * 列出所有可用提示
     */
    public List<McpPromptInfo> listAllPrompts() {
        List<McpPromptInfo> allPrompts = new ArrayList<>();
        
        for (McpSyncClient client : syncClients) {
            try {
                ListPromptsResult result = client.listPrompts(
                    new ListPromptsRequest(null)
                );
                
                List<McpPromptInfo> clientPrompts = result.prompts().stream()
                    .map(prompt -> new McpPromptInfo(
                        prompt.name(),
                        prompt.description().orElse(null),
                        prompt.arguments().stream()
                            .map(arg -> new PromptArgumentInfo(
                                arg.name(),
                                arg.description().orElse(null),
                                arg.required()
                            ))
                            .toList(),
                        client.getServerInfo().name()
                    ))
                    .toList();
                    
                allPrompts.addAll(clientPrompts);
                
            } catch (Exception e) {
                log.error("列出提示失敗 - 客戶端: {}", 
                    client.getServerInfo().name(), e);
            }
        }
        
        return allPrompts;
    }
    
    private boolean hasPrompt(McpSyncClient client, String promptName) {
        try {
            ListPromptsResult result = client.listPrompts(
                new ListPromptsRequest(null)
            );
            
            return result.prompts().stream()
                .anyMatch(prompt -> prompt.name().equals(promptName));
                
        } catch (Exception e) {
            return false;
        }
    }
    
    private List<Message> convertToMessages(List<PromptMessage> mcpMessages) {
        return mcpMessages.stream()
            .map(mcpMsg -> {
                Role role = Role.valueOf(mcpMsg.role().name());
                Content content = new TextContent(mcpMsg.content().text());
                return new Message(role, content);
            })
            .toList();
    }
    
    public record McpPromptInfo(
        String name,
        String description,
        List<PromptArgumentInfo> arguments,
        String serverName
    ) {}
    
    public record PromptArgumentInfo(
        String name,
        String description,
        boolean required
    ) {}
}
```

## ▋MCP Client 監控與管理

### 1. **健康檢查服務**

```java
@Component
@RequiredArgsConstructor
public class McpHealthIndicator implements HealthIndicator {
    
    private final List<McpSyncClient> syncClients;
    
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        Map<String, Object> details = new HashMap<>();
        
        int totalClients = syncClients.size();
        int connectedClients = 0;
        
        for (McpSyncClient client : syncClients) {
            String serverName = client.getServerInfo().name();
            boolean connected = client.isConnected();
            
            details.put(serverName + ".connected", connected);
            details.put(serverName + ".version", client.getServerInfo().version());
            
            if (connected) {
                connectedClients++;
                
                // 檢查工具數量
                try {
                    List<Tool> tools = client.listTools();
                    details.put(serverName + ".tools", tools.size());
                } catch (Exception e) {
                    details.put(serverName + ".tools.error", e.getMessage());
                }
            }
        }
        
        details.put("total", totalClients);
        details.put("connected", connectedClients);
        details.put("disconnected", totalClients - connectedClients);
        
        if (connectedClients == totalClients) {
            builder.up();
        } else if (connectedClients > 0) {
            builder.status("DEGRADED");
        } else {
            builder.down();
        }
        
        return builder.withDetails(details).build();
    }
}
```

### 2. **MCP 指標收集**

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class McpMetricsCollector {
    
    private final List<McpSyncClient> syncClients;
    private final MeterRegistry meterRegistry;
    
    @PostConstruct
    public void initializeMetrics() {
        Gauge.builder("mcp.clients.total")
            .description("Total number of MCP clients")
            .register(meterRegistry, syncClients, List::size);
            
        Gauge.builder("mcp.clients.connected")
            .description("Number of connected MCP clients")
            .register(meterRegistry, this, McpMetricsCollector::getConnectedClients);
    }
    
    @EventListener
    public void onToolCall(McpToolCallEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        sample.stop(Timer.builder("mcp.tool.execution.time")
            .description("Tool execution time")
            .tag("tool", event.getToolName())
            .tag("server", event.getServerName())
            .register(meterRegistry));
            
        Counter.builder("mcp.tool.calls")
            .description("Total tool calls")
            .tag("tool", event.getToolName())
            .tag("server", event.getServerName())
            .tag("status", event.isSuccess() ? "success" : "error")
            .register(meterRegistry)
            .increment();
    }
    
    private double getConnectedClients() {
        return syncClients.stream()
            .mapToLong(client -> client.isConnected() ? 1 : 0)
            .sum();
    }
    
    @Scheduled(fixedRate = 60000)
    public void collectConnectionMetrics() {
        for (McpSyncClient client : syncClients) {
            String serverName = client.getServerInfo().name();
            
            Gauge.builder("mcp.client.connection.status")
                .description("Client connection status")
                .tag("server", serverName)
                .register(meterRegistry, client, c -> c.isConnected() ? 1.0 : 0.0);
        }
    }
}
```

## ▋最佳實踐與注意事項

### 1. **連接管理**
- 實施連接重試機制
- 監控連接狀態變化
- 適當設定超時時間

### 2. **錯誤處理**
- 實現優雅的降級策略
- 記錄詳細的錯誤資訊
- 提供用戶友好的錯誤提示

### 3. **效能優化**
- 使用非同步客戶端處理高併發
- 實施適當的快取策略
- 監控資源使用情況

### 4. **安全性**
- 限制根目錄存取權限
- 驗證工具執行結果
- 記錄所有操作日誌

## ▋總結

MCP Client 為 AI 應用提供了強大的外部整合能力：

### 🚀 **核心特性**
- **多協議支援**: STDIO、HTTP/SSE、Streamable HTTP
- **Spring Boot 整合**: 自動配置和依賴注入
- **工具整合**: 與 ChatClient 無縫整合
- **企業級功能**: 監控、日誌、安全性

### 🎯 **應用場景**
- 企業工具平台整合
- 多模態資源存取
- 動態提示管理
- 智能助手開發

下一篇文章，我們將學習 MCP Server 的開發，了解如何創建和部署企業級的 MCP 服務，為 AI 應用提供豐富的工具和資源。