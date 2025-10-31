# 使用 Spring AI 打造企業 RAG 知識庫【39】- MCP Server 開發實戰：打造智能服務提供者

## MCP Server：AI 生態的智能服務中心

![https://ithelp.ithome.com.tw/upload/images/20240810/20161290MCPServer2025.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/20161290MCPServer2025.jpg)

MCP Server 是 AI 生態系統中的重要組件，它為 AI 應用提供工具、資源和提示服務。通過標準化的 MCP 協議，Server 可以讓任何支援 MCP 的客戶端存取其服務，實現真正的互操作性。

今天我們將深入學習如何使用 Spring AI 開發企業級的 MCP Server，包括工具註冊、資源管理、提示系統，以及不同傳輸協議的選擇與配置。

## ▋MCP Server Boot Starter 選擇指南

### 依賴配置策略

根據不同的部署需求選擇合適的 Starter：

```xml
<!-- 標準版 - STDIO 傳輸，適合命令列工具 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server</artifactId>
</dependency>

<!-- WebMVC 版 - HTTP/SSE 傳輸，適合傳統 Web 應用 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>

<!-- WebFlux 版 - 響應式 SSE 傳輸，適合高併發應用 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
</dependency>

<!-- 基礎依賴 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
```

### 傳輸協議選擇指南

| 傳輸協議 | 適用場景 | 優點 | 缺點 |
|---------|---------|------|------|
| **STDIO** | 命令列工具、桌面應用 | 簡單、無網路需求 | 僅限本地、無負載均衡 |
| **WebMVC SSE** | 傳統企業應用 | 成熟穩定、防火牆友好 | 阻塞式 I/O |
| **WebFlux SSE** | 高併發應用 | 非阻塞、高效能 | 學習曲線較陡 |

## ▋MCP Server 基礎配置

### 通用配置

在 `application.yml` 中配置 MCP Server：

```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        name: enterprise-mcp-server
        version: 2.0.0
        type: SYNC  # SYNC 或 ASYNC
        
        # 伺服器說明
        instructions: |
          這是企業級 MCP 伺服器，提供以下服務：
          - 檔案系統操作工具
          - 資料庫查詢工具  
          - 企業內部 API 整合
          - 動態提示範本
        
        # 功能開關
        capabilities:
          tool: true      # 工具功能
          resource: true  # 資源功能
          prompt: true    # 提示功能
          completion: true # 完成功能
        
        # 變更通知
        tool-change-notification: true
        resource-change-notification: true
        prompt-change-notification: true
        
        # 請求設定
        request-timeout: 30s
```

### WebMVC 專用配置

```yaml
spring:
  ai:
    mcp:
      server:
        # SSE 端點配置
        sse-endpoint: /mcp/sse
        sse-message-endpoint: /mcp/message
        base-url: /api/v1  # 可選的 URL 前綴
        
        # 可選啟用 STDIO（預設關閉）
        stdio: false
```

### WebFlux 專用配置

```yaml
spring:
  ai:
    mcp:
      server:
        # 響應式配置
        sse-endpoint: /mcp/sse
        sse-message-endpoint: /mcp/message
        
        # WebFlux 特定設定
        type: ASYNC  # 推薦使用非同步模式
```

## ▋Tools 工具開發實戰

### 1. **使用 @Tool 註解快速開發**

```java
@Service
@Slf4j
public class FileSystemToolsService {
    
    @Tool(description = "讀取指定路徑的檔案內容")
    public String readFile(
        @ToolParam(description = "檔案路徑", required = true) String filePath
    ) {
        try {
            Path path = Paths.get(filePath);
            
            // 安全檢查
            if (!isPathSafe(path)) {
                throw new SecurityException("不安全的檔案路徑: " + filePath);
            }
            
            return Files.readString(path, StandardCharsets.UTF_8);
            
        } catch (IOException e) {
            log.error("讀取檔案失敗: {}", filePath, e);
            throw new RuntimeException("讀取檔案失敗: " + e.getMessage());
        }
    }
    
    @Tool(description = "列出指定目錄下的所有檔案")
    public List<String> listFiles(
        @ToolParam(description = "目錄路徑", required = true) String directoryPath,
        @ToolParam(description = "是否包含隱藏檔案", required = false) Boolean includeHidden
    ) {
        try {
            Path dir = Paths.get(directoryPath);
            
            if (!isPathSafe(dir) || !Files.isDirectory(dir)) {
                throw new IllegalArgumentException("無效的目錄路徑: " + directoryPath);
            }
            
            boolean includeHiddenFiles = Boolean.TRUE.equals(includeHidden);
            
            try (Stream<Path> paths = Files.list(dir)) {
                return paths
                    .filter(path -> includeHiddenFiles || !path.getFileName().toString().startsWith("."))
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
            }
            
        } catch (IOException e) {
            log.error("列出檔案失敗: {}", directoryPath, e);
            throw new RuntimeException("列出檔案失敗: " + e.getMessage());
        }
    }
    
    @Tool(description = "建立新目錄")  
    public String createDirectory(
        @ToolParam(description = "要建立的目錄路徑", required = true) String directoryPath
    ) {
        try {
            Path dir = Paths.get(directoryPath);
            
            if (!isPathSafe(dir)) {
                throw new SecurityException("不安全的目錄路徑: " + directoryPath);
            }
            
            Files.createDirectories(dir);
            return "成功建立目錄: " + directoryPath;
            
        } catch (IOException e) {
            log.error("建立目錄失敗: {}", directoryPath, e);
            throw new RuntimeException("建立目錄失敗: " + e.getMessage());
        }
    }
    
    private boolean isPathSafe(Path path) {
        // 實現路徑安全檢查邏輯
        Path normalized = path.normalize();
        String pathStr = normalized.toString();
        
        // 禁止訪問系統關鍵目錄
        List<String> forbiddenPaths = List.of(
            "/etc", "/sys", "/proc", "/root",  
            "C:\\Windows", "C:\\System32"
        );
        
        return forbiddenPaths.stream()
            .noneMatch(pathStr::startsWith);
    }
}
```

### 2. **資料庫操作工具**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseToolsService {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Tool(description = "執行安全的 SELECT 查詢")
    public List<Map<String, Object>> executeQuery(
        @ToolParam(description = "SQL 查詢語句", required = true) String sql,
        @ToolParam(description = "查詢參數", required = false) Map<String, Object> parameters
    ) {
        try {
            // SQL 安全檢查
            if (!isSafeQuery(sql)) {
                throw new SecurityException("不安全的 SQL 查詢");
            }
            
            if (parameters == null || parameters.isEmpty()) {
                return jdbcTemplate.queryForList(sql);
            } else {
                // 使用命名參數
                NamedParameterJdbcTemplate namedTemplate = 
                    new NamedParameterJdbcTemplate(jdbcTemplate);
                return namedTemplate.queryForList(sql, parameters);
            }
            
        } catch (Exception e) {
            log.error("資料庫查詢失敗: {}", sql, e);
            throw new RuntimeException("資料庫查詢失敗: " + e.getMessage());
        }
    }
    
    @Tool(description = "獲取資料表結構資訊")
    public List<TableColumn> getTableSchema(
        @ToolParam(description = "資料表名稱", required = true) String tableName
    ) {
        try {
            String sql = """
                SELECT 
                    COLUMN_NAME as columnName,
                    DATA_TYPE as dataType,
                    IS_NULLABLE as nullable,
                    COLUMN_DEFAULT as defaultValue,
                    COLUMN_COMMENT as comment
                FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """;
                
            return jdbcTemplate.query(sql, 
                (rs, rowNum) -> new TableColumn(
                    rs.getString("columnName"),
                    rs.getString("dataType"),
                    "YES".equals(rs.getString("nullable")),
                    rs.getString("defaultValue"),
                    rs.getString("comment")
                ),
                tableName
            );
            
        } catch (Exception e) {
            log.error("獲取資料表結構失敗: {}", tableName, e);
            throw new RuntimeException("獲取資料表結構失敗: " + e.getMessage());
        }
    }
    
    private boolean isSafeQuery(String sql) {
        // 只允許 SELECT 查詢
        String normalizedSql = sql.trim().toLowerCase();
        
        if (!normalizedSql.startsWith("select")) {
            return false;
        }
        
        // 檁止危險關鍵字
        List<String> dangerousKeywords = List.of(
            "drop", "delete", "update", "insert", "alter", 
            "create", "truncate", "exec", "execute"
        );
        
        return dangerousKeywords.stream()
            .noneMatch(normalizedSql::contains);
    }
    
    public record TableColumn(
        String name,
        String dataType,
        boolean nullable,
        String defaultValue,
        String comment
    ) {}
}
```

### 3. **企業 API 整合工具**

```java
@Service
@RequiredArgsConstructor
@Slf4j  
public class EnterpriseApiToolsService {
    
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Tool(description = "獲取員工資訊")
    public EmployeeInfo getEmployeeInfo(
        @ToolParam(description = "員工ID", required = true) String employeeId
    ) {
        try {
            // 先檢查快取
            String cacheKey = "employee:" + employeeId;
            EmployeeInfo cached = (EmployeeInfo) redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                return cached;
            }
            
            // 調用企業 HR API
            String url = "http://hr-api.company.com/api/employees/" + employeeId;
            ResponseEntity<EmployeeInfo> response = restTemplate.getForEntity(
                url, EmployeeInfo.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                EmployeeInfo employee = response.getBody();
                
                // 快取結果（5分鐘）
                redisTemplate.opsForValue().set(cacheKey, employee, Duration.ofMinutes(5));
                
                return employee;
            }
            
            throw new RuntimeException("員工資訊不存在");
            
        } catch (Exception e) {
            log.error("獲取員工資訊失敗: {}", employeeId, e);
            throw new RuntimeException("獲取員工資訊失敗: " + e.getMessage());
        }
    }
    
    @Tool(description = "建立任務工單")
    public String createTask(
        @ToolParam(description = "任務標題", required = true) String title,
        @ToolParam(description = "任務描述", required = true) String description,
        @ToolParam(description = "優先級", required = false) String priority,
        @ToolParam(description = "負責人ID", required = false) String assigneeId
    ) {
        try {
            CreateTaskRequest request = new CreateTaskRequest(
                title,
                description, 
                priority != null ? Priority.valueOf(priority.toUpperCase()) : Priority.MEDIUM,
                assigneeId,
                Instant.now()
            );
            
            String url = "http://task-api.company.com/api/tasks";
            ResponseEntity<CreateTaskResponse> response = restTemplate.postForEntity(
                url, request, CreateTaskResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String taskId = response.getBody().taskId();
                return "成功建立任務，任務ID: " + taskId;
            }
            
            throw new RuntimeException("建立任務失敗");
            
        } catch (Exception e) {
            log.error("建立任務失敗", e);
            throw new RuntimeException("建立任務失敗: " + e.getMessage());
        }
    }
    
    public record EmployeeInfo(
        String id,
        String name, 
        String email,
        String department,
        String position
    ) {}
    
    public record CreateTaskRequest(
        String title,
        String description,
        Priority priority,
        String assigneeId,
        Instant createdAt
    ) {}
    
    public record CreateTaskResponse(String taskId) {}
    
    public enum Priority { LOW, MEDIUM, HIGH, URGENT }
}
```

### 4. **工具註冊配置**

```java
@Configuration
@RequiredArgsConstructor
public class McpToolsConfiguration {
    
    private final FileSystemToolsService fileSystemTools;
    private final DatabaseToolsService databaseTools;
    private final EnterpriseApiToolsService apiTools;
    
    @Bean
    public ToolCallbackProvider fileSystemToolProvider() {
        return MethodToolCallbackProvider.builder()
            .toolObjects(fileSystemTools)
            .build();
    }
    
    @Bean  
    public ToolCallbackProvider databaseToolProvider() {
        return MethodToolCallbackProvider.builder()
            .toolObjects(databaseTools)
            .build();
    }
    
    @Bean
    public ToolCallbackProvider enterpriseApiToolProvider() {
        return MethodToolCallbackProvider.builder()
            .toolObjects(apiTools)
            .build();
    }
    
    // 也可以手動創建 ToolCallback
    @Bean
    public List<ToolCallback> customToolCallbacks() {
        return List.of(
            // 自定義工具實現
            ToolCallback.builder()
                .name("weather")
                .description("獲取天氣資訊")
                .inputTypeClass(WeatherRequest.class)
                .function(this::getWeather)
                .build()
        );
    }
    
    private String getWeather(WeatherRequest request) {
        // 天氣 API 調用實現
        return "今天天氣晴朗，溫度 25°C";
    }
    
    public record WeatherRequest(String city) {}
}
```

## ▋Resources 資源管理實戰

### 1. **靜態資源提供**

```java
@Configuration
public class McpResourcesConfiguration {
    
    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> systemResources() {
        List<McpServerFeatures.SyncResourceSpecification> resources = new ArrayList<>();
        
        // 系統資訊資源
        var systemInfoResource = new McpSchema.Resource(
            "system://info",
            "系統資訊",
            Optional.of("提供伺服器系統資訊"),
            Optional.of("application/json")
        );
        
        resources.add(new McpServerFeatures.SyncResourceSpecification(
            systemInfoResource,
            this::getSystemInfo
        ));
        
        // 配置資源
        var configResource = new McpSchema.Resource(
            "config://application",
            "應用程式配置",
            Optional.of("應用程式配置資訊"),
            Optional.of("application/json")
        );
        
        resources.add(new McpServerFeatures.SyncResourceSpecification(
            configResource,
            this::getApplicationConfig
        ));
        
        return resources;
    }
    
    private McpSchema.ReadResourceResult getSystemInfo(
        McpSyncServerExchange exchange, 
        McpSchema.ReadResourceRequest request
    ) {
        try {
            Map<String, Object> systemInfo = Map.of(
                "hostname", InetAddress.getLocalHost().getHostName(),
                "os", System.getProperty("os.name"),
                "javaVersion", System.getProperty("java.version"),
                "processors", Runtime.getRuntime().availableProcessors(),
                "memory", Map.of(
                    "total", Runtime.getRuntime().totalMemory(),
                    "free", Runtime.getRuntime().freeMemory(),
                    "max", Runtime.getRuntime().maxMemory()
                ),
                "timestamp", Instant.now()
            );
            
            String jsonContent = new ObjectMapper().writeValueAsString(systemInfo);
            
            return new McpSchema.ReadResourceResult(
                List.of(new McpSchema.TextResourceContents(
                    request.uri(),
                    "application/json", 
                    jsonContent
                ))
            );
            
        } catch (Exception e) {
            throw new RuntimeException("獲取系統資訊失敗", e);
        }
    }
    
    private McpSchema.ReadResourceResult getApplicationConfig(
        McpSyncServerExchange exchange,
        McpSchema.ReadResourceRequest request
    ) {
        try {
            // 返回安全的配置資訊（不包含敏感資料）
            Map<String, Object> config = Map.of(
                "server", Map.of(
                    "name", "enterprise-mcp-server",
                    "version", "2.0.0"
                ),
                "features", Map.of(
                    "tools", true,
                    "resources", true,
                    "prompts", true
                ),
                "limits", Map.of(
                    "maxRequestSize", "10MB",
                    "requestTimeout", "30s"
                )
            );
            
            String jsonContent = new ObjectMapper().writeValueAsString(config);
            
            return new McpSchema.ReadResourceResult(
                List.of(new McpSchema.TextResourceContents(
                    request.uri(),
                    "application/json",
                    jsonContent
                ))
            );
            
        } catch (Exception e) {
            throw new RuntimeException("獲取配置資訊失敗", e);
        }
    }
}
```

### 2. **動態檔案資源**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class FileResourceService {
    
    private final String allowedBasePath = "/safe/documents";
    
    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> fileResources() {
        // 動態檔案資源不需要預先定義，通過 listResources 動態發現
        return List.of();
    }
    
    @EventListener
    public void handleListResources(ListResourcesEvent event) {
        try {
            List<McpSchema.Resource> fileResources = new ArrayList<>();
            Path basePath = Paths.get(allowedBasePath);
            
            if (Files.exists(basePath)) {
                try (Stream<Path> paths = Files.walk(basePath, 3)) {  // 最多3層深度
                    paths.filter(Files::isRegularFile)
                         .forEach(path -> {
                             String uri = "file://" + path.toAbsolutePath();
                             String name = path.getFileName().toString();
                             String description = "檔案: " + path.toString();
                             
                             try {
                                 String mimeType = Files.probeContentType(path);
                                 
                                 fileResources.add(new McpSchema.Resource(
                                     uri,
                                     name,
                                     Optional.of(description),
                                     Optional.ofNullable(mimeType)
                                 ));
                             } catch (IOException e) {
                                 log.warn("無法確定檔案類型: {}", path, e);
                             }
                         });
                }
            }
            
            event.addResources(fileResources);
            
        } catch (Exception e) {
            log.error("列出檔案資源失敗", e);
        }
    }
    
    @EventListener
    public void handleReadResource(ReadResourceEvent event) {
        String uri = event.getRequest().uri();
        
        if (uri.startsWith("file://")) {
            try {
                Path filePath = Paths.get(URI.create(uri));
                
                // 安全檢查
                if (!filePath.startsWith(allowedBasePath)) {
                    throw new SecurityException("檔案路徑不在允許範圍內");
                }
                
                if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                    throw new FileNotFoundException("檔案不存在: " + filePath);
                }
                
                // 讀取檔案內容
                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                String mimeType = Files.probeContentType(filePath);
                
                McpSchema.ReadResourceResult result = new McpSchema.ReadResourceResult(
                    List.of(new McpSchema.TextResourceContents(
                        uri,
                        mimeType != null ? mimeType : "text/plain",
                        content
                    ))
                );
                
                event.setResult(result);
                
            } catch (Exception e) {
                log.error("讀取檔案資源失敗: {}", uri, e);
                throw new RuntimeException("讀取檔案資源失敗: " + e.getMessage());
            }
        }
    }
}
```

## ▋Prompts 提示系統實戰

### 1. **靜態提示範本**

```java
@Configuration
public class McpPromptsConfiguration {
    
    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> businessPrompts() {
        List<McpServerFeatures.SyncPromptSpecification> prompts = new ArrayList<>();
        
        // 文檔分析提示
        var documentAnalysisPrompt = new McpSchema.Prompt(
            "document-analysis",
            "文檔分析專家提示",
            List.of(
                new McpSchema.PromptArgument("document", "要分析的文檔內容", true),
                new McpSchema.PromptArgument("focus", "分析重點", false)
            )
        );
        
        prompts.add(new McpServerFeatures.SyncPromptSpecification(
            documentAnalysisPrompt,
            this::generateDocumentAnalysisPrompt
        ));
        
        // 程式碼審查提示
        var codeReviewPrompt = new McpSchema.Prompt(
            "code-review",
            "程式碼審查專家提示", 
            List.of(
                new McpSchema.PromptArgument("code", "程式碼內容", true),
                new McpSchema.PromptArgument("language", "程式語言", false),
                new McpSchema.PromptArgument("style", "審查風格", false)
            )
        );
        
        prompts.add(new McpServerFeatures.SyncPromptSpecification(
            codeReviewPrompt,
            this::generateCodeReviewPrompt
        ));
        
        return prompts;
    }
    
    private GetPromptResult generateDocumentAnalysisPrompt(
        McpSyncServerExchange exchange,
        GetPromptRequest request
    ) {
        String document = (String) request.arguments().get("document");
        String focus = (String) request.arguments().getOrDefault("focus", "全面分析");
        
        String systemPrompt = """
            你是一位專業的文檔分析專家。請仔細分析以下文檔，重點關注：%s
            
            分析應包括：
            1. 文檔結構和組織
            2. 主要內容和關鍵信息
            3. 邏輯流程和論證
            4. 可能的改進建議
            5. 總結和結論
            
            請提供詳細且專業的分析報告。
            """.formatted(focus);
            
        String userPrompt = "請分析以下文檔：\n\n" + document;
        
        List<PromptMessage> messages = List.of(
            new PromptMessage(Role.SYSTEM, new TextContent(systemPrompt)),
            new PromptMessage(Role.USER, new TextContent(userPrompt))
        );
        
        return new GetPromptResult(
            "文檔分析專家提示已生成，專注於：" + focus,
            messages
        );
    }
    
    private GetPromptResult generateCodeReviewPrompt(
        McpSyncServerExchange exchange,
        GetPromptRequest request
    ) {
        String code = (String) request.arguments().get("code");
        String language = (String) request.arguments().getOrDefault("language", "自動偵測");
        String style = (String) request.arguments().getOrDefault("style", "詳細");
        
        String systemPrompt = """
            你是一位資深的軟體開發專家和程式碼審查員。
            請對以下%s程式碼進行%s審查：
            
            審查重點：
            1. 程式碼品質和可讀性
            2. 設計模式和架構
            3. 效能和最佳化機會
            4. 安全性考量
            5. 測試覆蓋度
            6. 文檔和註解
            
            請提供建設性的反饋和具體的改進建議。
            """.formatted(language, style);
            
        String userPrompt = "請審查以下程式碼：\n\n```\n" + code + "\n```";
        
        List<PromptMessage> messages = List.of(
            new PromptMessage(Role.SYSTEM, new TextContent(systemPrompt)),
            new PromptMessage(Role.USER, new TextContent(userPrompt))
        );
        
        return new GetPromptResult(
            String.format("%s程式碼審查提示已生成（%s風格）", language, style),
            messages
        );
    }
}
```

### 2. **動態提示生成器**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicPromptService {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> dynamicPrompts() {
        // 從資料庫載入動態提示範本
        return loadPromptsFromDatabase();
    }
    
    private List<McpServerFeatures.SyncPromptSpecification> loadPromptsFromDatabase() {
        try {
            String sql = """
                SELECT 
                    prompt_name,
                    prompt_description,
                    prompt_template,
                    arguments_schema
                FROM prompt_templates 
                WHERE active = true
                """;
                
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                String name = rs.getString("prompt_name");
                String description = rs.getString("prompt_description");
                String template = rs.getString("prompt_template");
                String argumentsJson = rs.getString("arguments_schema");
                
                // 解析參數定義
                List<McpSchema.PromptArgument> arguments = parseArguments(argumentsJson);
                
                var prompt = new McpSchema.Prompt(name, description, arguments);
                
                return new McpServerFeatures.SyncPromptSpecification(
                    prompt,
                    (exchange, request) -> generateDynamicPrompt(template, request)
                );
            });
            
        } catch (Exception e) {
            log.error("載入動態提示失敗", e);
            return List.of();
        }
    }
    
    private List<McpSchema.PromptArgument> parseArguments(String argumentsJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode argsNode = mapper.readTree(argumentsJson);
            
            List<McpSchema.PromptArgument> arguments = new ArrayList<>();
            
            argsNode.fields().forEachRemaining(entry -> {
                String name = entry.getKey();
                JsonNode config = entry.getValue();
                
                String description = config.path("description").asText("參數描述");
                boolean required = config.path("required").asBoolean(false);
                
                arguments.add(new McpSchema.PromptArgument(name, description, required));
            });
            
            return arguments;
            
        } catch (Exception e) {
            log.error("解析參數定義失敗", e);
            return List.of();
        }
    }
    
    private GetPromptResult generateDynamicPrompt(
        String template, 
        GetPromptRequest request
    ) {
        try {
            // 使用簡單的範本引擎替換變數
            String processedTemplate = template;
            
            for (Map.Entry<String, Object> entry : request.arguments().entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = String.valueOf(entry.getValue());
                processedTemplate = processedTemplate.replace(placeholder, value);
            }
            
            List<PromptMessage> messages = List.of(
                new PromptMessage(Role.USER, new TextContent(processedTemplate))
            );
            
            return new GetPromptResult("動態提示已生成", messages);
            
        } catch (Exception e) {
            log.error("生成動態提示失敗", e);
            throw new RuntimeException("生成動態提示失敗: " + e.getMessage());
        }
    }
}
```

## ▋MCP Server 監控與管理

### 1. **伺服器健康監控**

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class McpServerMonitoringService {
    
    private final MeterRegistry meterRegistry;
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final Map<String, AtomicLong> toolCallCounts = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initializeMetrics() {
        // 註冊基礎指標
        Gauge.builder("mcp.server.connections.active")
            .description("Active MCP client connections")
            .register(meterRegistry, activeConnections, AtomicLong::get);
            
        Gauge.builder("mcp.server.tools.count")
            .description("Number of registered tools")
            .register(meterRegistry, this, McpServerMonitoringService::getToolCount);
    }
    
    @EventListener
    public void onClientConnected(McpClientConnectedEvent event) {
        activeConnections.incrementAndGet();
        log.info("MCP 客戶端連接: {}", event.getClientId());
        
        Counter.builder("mcp.server.connections.total")
            .description("Total client connections")
            .tag("event", "connected")
            .register(meterRegistry)
            .increment();
    }
    
    @EventListener
    public void onClientDisconnected(McpClientDisconnectedEvent event) {
        activeConnections.decrementAndGet();
        log.info("MCP 客戶端斷線: {}", event.getClientId());
        
        Counter.builder("mcp.server.connections.total")
            .description("Total client connections")
            .tag("event", "disconnected")
            .register(meterRegistry)
            .increment();
    }
    
    @EventListener
    public void onToolCall(McpToolCallEvent event) {
        String toolName = event.getToolName();
        
        // 記錄工具調用次數
        toolCallCounts.computeIfAbsent(toolName, k -> new AtomicLong(0))
                      .incrementAndGet();
        
        // 記錄執行時間
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("mcp.server.tool.execution.time")
            .description("Tool execution time")
            .tag("tool", toolName)
            .tag("status", event.isSuccess() ? "success" : "error")
            .register(meterRegistry));
        
        log.debug("工具調用: {} - {}", toolName, 
            event.isSuccess() ? "成功" : "失敗");
    }
    
    private double getToolCount() {
        // 實際實現中需要從 MCP Server 獲取工具數量
        return 10.0; // 示例值
    }
    
    @Scheduled(fixedRate = 60000) // 每分鐘執行
    public void reportMetrics() {
        log.info("MCP Server 狀態 - 活躍連接: {}, 工具調用統計: {}", 
            activeConnections.get(), toolCallCounts);
    }
}
```

### 2. **安全性配置**

```java
@Configuration
@EnableConfigurationProperties(McpSecurityProperties.class)
public class McpSecurityConfiguration {
    
    @Bean
    public McpServerCustomizer securityCustomizer(McpSecurityProperties properties) {
        return spec -> {
            // 設定請求大小限制
            spec.maxRequestSize(properties.getMaxRequestSize());
            
            // 設定速率限制
            spec.rateLimiter(RateLimiter.create(properties.getRequestsPerSecond()));
            
            // 設定 IP 白名單
            if (!properties.getAllowedIps().isEmpty()) {
                spec.ipFilter(ip -> properties.getAllowedIps().contains(ip));
            }
            
            // 設定工具權限
            spec.toolFilter(toolName -> 
                properties.getAllowedTools().isEmpty() || 
                properties.getAllowedTools().contains(toolName)
            );
        };
    }
}

@ConfigurationProperties(prefix = "mcp.security")
@Data
public class McpSecurityProperties {
    private long maxRequestSize = 10 * 1024 * 1024; // 10MB
    private double requestsPerSecond = 100.0;
    private List<String> allowedIps = new ArrayList<>();
    private List<String> allowedTools = new ArrayList<>();
}
```

## ▋部署與最佳實踐

### 1. **Docker 容器化**

```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# 複製應用程式檔案
COPY target/mcp-server-*.jar app.jar

# 建立非 root 使用者
RUN useradd -r -s /bin/false mcpuser
RUN chown mcpuser:mcpuser /app

# 切換到非 root 使用者
USER mcpuser

# 暴露端點
EXPOSE 8080

# 健康檢查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 啟動應用程式
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. **Kubernetes 部署**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mcp-server
  labels:
    app: mcp-server
spec:
  replicas: 3
  selector:
    matchLabels:
      app: mcp-server
  template:
    metadata:
      labels:
        app: mcp-server
    spec:
      containers:
      - name: mcp-server
        image: company/mcp-server:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi" 
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/ready
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: mcp-server-service
spec:
  selector:
    app: mcp-server
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

## ▋總結

MCP Server 為 AI 生態系統提供了標準化的服務能力：

### 🚀 **核心能力**
- **工具提供**: 通過 @Tool 註解快速開發業務工具
- **資源管理**: 靜態和動態資源的統一存取介面
- **提示系統**: 靈活的提示範本管理機制
- **多協議支援**: STDIO、WebMVC、WebFlux 三種傳輸方式

### 🎯 **企業應用**
- 企業內部工具平台
- API 服務整合中心
- 知識庫資源提供者
- 動態提示管理系統

### 📈 **最佳實踐**
- 實施完整的安全檢查機制
- 建立監控和指標收集系統
- 採用容器化部署策略
- 規劃合理的錯誤處理流程

下一篇文章，我們將學習如何將 MCP 整合到企業級 RAG 系統中，實現更智能、更靈活的 AI 應用架構。