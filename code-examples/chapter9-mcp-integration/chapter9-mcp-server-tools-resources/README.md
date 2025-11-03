# Chapter 9 - MCP Server 工具開發範例

## 專案簡介

本專案展示如何使用 **Spring AI 1.0.3 穩定版** 開發 MCP (Model Context Protocol) Server，提供可調用的工具（Tools）服務給 AI 模型使用。這是《Spring AI 實戰指南》第9章的第2個實作專案。

### 核心特色

- ✅ **@Tool 註解**：使用 Spring AI 的 `@Tool` 註解定義工具方法
- ✅ **ToolCallbackProvider**：透過 `MethodToolCallbackProvider` 註冊工具
- ✅ **雙傳輸模式**：支援 STDIO 和 SSE 兩種通訊方式
- ✅ **外部 API 整合**：示範調用 Open-Meteo 天氣 API
- ✅ **穩定版本**：使用 Spring AI 1.0.3 正式版，確保生產環境穩定性

---

## 技術棧

| 技術 | 版本 | 說明 |
|------|------|------|
| **Java** | 21 | 主要開發語言 |
| **Spring Boot** | 3.5.7 | 應用框架 |
| **Spring AI** | 1.0.3 | MCP Server 核心（穩定版） |
| **Maven** | 3.9+ | 專案管理 |
| **Lombok** | Latest | 程式碼簡化 |
| **WebFlux** | Latest | 外部 API 調用 |

---

## 專案結構

```
chapter9-mcp-server-tools-resources/
├── src/main/java/com/example/mcp/server/
│   ├── McpServerApplication.java          # 主程式
│   ├── model/
│   │   ├── WeatherResponse.java          # 天氣回應模型
│   │   ├── UserProfile.java              # 使用者資料模型
│   │   └── ApplicationConfig.java        # 應用配置模型
│   └── provider/tool/
│       ├── WeatherToolProvider.java      # 天氣工具服務
│       ├── MathToolProvider.java         # 數學工具服務
│       ├── TextToolProvider.java         # 文本工具服務
│       └── TimeToolProvider.java         # 時間工具服務
├── src/main/resources/
│   └── application.yml                   # Spring Boot 配置
├── pom.xml                               # Maven 配置
├── compile.bat                           # Windows 編譯腳本
├── compile.ps1                           # PowerShell 編譯腳本
├── start-stdio.bat                       # STDIO 模式啟動腳本
├── start-sse.bat                         # SSE 模式啟動腳本
└── README.md                             # 本文件
```

---

## 快速開始

### 1. 環境準備

確保已安裝：
- **Java 21**（路徑：`D:\java\jdk-21`）
- **Maven 3.9+**（路徑：`D:\apache-maven-3.9.11`）

### 2. 編譯專案

#### 使用 PowerShell 腳本（推薦）

```powershell
.\compile.ps1
```

#### 使用 Batch 腳本

```cmd
compile.bat
```

#### 手動編譯

```powershell
# 設定環境變數
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 編譯
mvn clean compile
```

### 3. 啟動應用

#### 方式一：STDIO 模式（標準輸入輸出）

適用於本地開發、命令行整合、測試環境

```cmd
start-stdio.bat
```

或使用 Maven：

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.ai.mcp.server.stdio=true"
```

**特點**：
- 透過標準輸入輸出進行 JSON-RPC 通訊
- 適合本地開發和測試
- 低延遲、高效能

#### 方式二：SSE 模式（Server-Sent Events）

適用於遠端服務、HTTP 整合、生產環境

```cmd
start-sse.bat
```

或使用 Maven：

```powershell
mvn spring-boot:run
```

**特點**：
- HTTP 端點：`http://localhost:8080/mcp/message`
- 支援遠端訪問
- 易於整合到 Web 應用

---

## 可用工具（Tools）

### 1. WeatherToolProvider（天氣工具）

整合 Open-Meteo API，提供天氣查詢服務

| 工具名稱 | 說明 | 參數 |
|----------|------|------|
| `getTemperature` | 獲取指定位置的溫度 | `latitude`（緯度）, `longitude`（經度）, `city`（城市名稱） |

**功能特點**：
- 調用 Open-Meteo 免費天氣 API
- 支援全球任意經緯度座標
- 回傳當前溫度（攝氏度）

**實作位置**：`src/main/java/com/example/mcp/server/provider/tool/WeatherToolProvider.java:36`

### 2. MathToolProvider（數學工具）

提供基本數學運算功能

| 工具名稱 | 說明 | 參數 |
|----------|------|------|
| `sum` | 計算多個數字的總和 | `numbers...`（可變參數） |
| `multiply` | 計算多個數字的乘積 | `numbers...`（可變參數） |
| `divide` | 兩數相除 | `dividend`（被除數）, `divisor`（除數） |
| `sqrt` | 計算平方根 | `number`（數字） |

**功能特點**：
- 支援可變參數運算（sum、multiply）
- 包含除零檢查
- 包含負數平方根檢查

**實作位置**：`src/main/java/com/example/mcp/server/provider/tool/MathToolProvider.java:22`

### 3. TextToolProvider（文本工具）

提供文本處理功能

| 工具名稱 | 說明 | 參數 |
|----------|------|------|
| `toUpperCase` | 轉換為大寫 | `text`（文本內容） |
| `toLowerCase` | 轉換為小寫 | `text`（文本內容） |
| `wordCount` | 計算單字數量 | `text`（文本內容） |

**實作位置**：`src/main/java/com/example/mcp/server/provider/tool/TextToolProvider.java:17`

### 4. TimeToolProvider（時間工具）

提供時間查詢功能

| 工具名稱 | 說明 | 參數 |
|----------|------|------|
| `getCurrentTime` | 獲取當前時間 | `timezone`（時區，可選） |

**功能特點**：
- 支援指定時區查詢
- 預設使用系統時區
- 格式化輸出：`yyyy-MM-dd HH:mm:ss z`

**實作位置**：`src/main/java/com/example/mcp/server/provider/tool/TimeToolProvider.java:24`

---

## 配置說明

### application.yml 核心配置

```yaml
spring:
  ai:
    mcp:
      server:
        name: tools-resources-server        # Server 名稱
        version: 1.0.0                      # Server 版本
        type: SYNC                          # 同步處理
        stdio: false                        # false=SSE, true=STDIO
        sse-message-endpoint: /mcp/message  # SSE 端點

server:
  port: 8080                                # HTTP 埠號

logging:
  level:
    com.example.mcp: DEBUG                  # MCP 詳細日誌
    org.springframework.ai: DEBUG           # Spring AI 日誌
```

### 切換傳輸模式

#### 方法一：修改 application.yml

```yaml
spring.ai.mcp.server.stdio: true  # STDIO 模式
```

#### 方法二：啟動參數

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.ai.mcp.server.stdio=true"
```

---

## 開發指南

### 新增工具（Tool）

本專案使用 Spring AI 1.0.3 穩定版的官方方式開發 MCP Tools。

#### 步驟 1：建立工具服務類別（使用 @Tool 註解）

```java
package com.example.mcp.server.provider.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * 自訂工具提供者
 */
@Service
@Slf4j
public class MyToolProvider {

    /**
     * 工具方法範例
     *
     * @param input 輸入參數
     * @return 處理結果
     */
    @Tool(description = "Process input data and convert to uppercase")
    public String processData(String input) {
        log.info("執行工具: processData, 輸入: {}", input);

        // 實作您的業務邏輯
        String result = "處理結果: " + input.toUpperCase();

        log.info("工具執行完成，結果: {}", result);
        return result;
    }
}
```

#### 步驟 2：在 Application 類別中註冊 ToolCallbackProvider

```java
package com.example.mcp.server;

import com.example.mcp.server.provider.tool.MyToolProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    /**
     * 註冊自訂工具
     */
    @Bean
    public ToolCallbackProvider myTools(MyToolProvider myToolProvider) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(myToolProvider)
                .build();
    }
}
```

**關鍵點**：
1. 使用 `@Service` 標記服務類別
2. 使用 `@Tool(description = "...")` 標記工具方法
3. 在 Application 中使用 `MethodToolCallbackProvider` 註冊工具
4. Spring AI 會自動將 `@Tool` 方法轉換為 MCP Tools
5. 可以有多個 `ToolCallbackProvider` Bean，會自動合併

### 整合外部 API

範例：整合 REST API

```java
@Service
@Slf4j
public class ExternalApiToolProvider {

    private final WebClient webClient;

    public ExternalApiToolProvider() {
        this.webClient = WebClient.create();
    }

    @Tool(description = "Fetch data from external API")
    public String fetchData(String url) {
        log.info("調用外部 API: {}", url);

        String response = webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        return response;
    }
}
```

---

## 測試範例

### 使用 curl 測試 SSE 端點

啟動應用後，可使用 curl 測試 MCP Server 功能：

```bash
# 1. 測試工具列表
curl -X POST http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'

# 2. 測試初始化
curl -X POST http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test-client","version":"1.0.0"}}}'
```

### 使用 Spring AI MCP Client 測試

請參考專案 `chapter9-mcp-client-basic` 範例程式碼，展示如何透過 Spring AI MCP Client 調用本 Server 的工具。

### 日誌輸出範例

成功啟動後，您會看到類似以下的日誌：

```
========================================
  啟動 MCP Server - 工具與資源
  Spring AI 1.0.3 穩定版
========================================
... Spring Boot 啟動日誌 ...
========================================
  MCP Server 已啟動完成
========================================
```

---

## 常見問題

### Q1: 為什麼要使用 @Tool 註解和 ToolCallbackProvider？

**答**：這是 **Spring AI 1.0.3 官方推薦的方式**：

1. **@Tool 註解**：明確標記哪些方法可以被 AI 模型調用
2. **ToolCallbackProvider**：統一管理和註冊工具
3. **自動轉換**：Spring AI 會自動將 `@Tool` 方法轉換為 MCP 工具規範

這種方式：
- 類型安全且易於維護
- 支援自動文檔生成
- 符合 Spring Boot 的自動配置機制

### Q2: 如何驗證編譯成功？

**答**：執行編譯腳本後，確認以下輸出：

```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

若出現編譯錯誤，請確認：
1. Java 版本為 21（`java -version`）
2. Maven 版本為 3.9+（`mvn -version`）
3. 環境變數 `JAVA_HOME` 正確設定為 `D:\java\jdk-21`

### Q3: STDIO 模式和 SSE 模式有什麼差別？

**答**：

| 特性 | STDIO 模式 | SSE 模式 |
|------|-----------|---------|
| 通訊方式 | 標準輸入/輸出 | HTTP 端點 |
| 適用場景 | 本地開發、CLI 整合 | 遠端服務、Web 整合 |
| 網路需求 | 無 | 需要 HTTP 連線 |
| 測試工具 | 直接輸入 JSON-RPC | curl、Postman |
| 配置方式 | `spring.ai.mcp.server.stdio=true` | `spring.ai.mcp.server.stdio=false`（預設） |

### Q4: 如何新增更多天氣資料（風速、濕度）？

**答**：修改 `WeatherToolProvider.java:44`，在 Open-Meteo API 請求中添加參數：

```java
.uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m,wind_speed_10m,relative_humidity_2m",
        latitude, longitude)
```

然後更新 `WeatherResponse` 模型添加對應欄位。

### Q5: 如何新增多個工具方法到同一個 Provider？

**答**：只需在同一個 `@Service` 類別中添加多個 `@Tool` 方法：

```java
@Service
@Slf4j
public class MathToolProvider {

    @Tool(description = "Calculate sum")
    public double sum(double... numbers) { /* ... */ }

    @Tool(description = "Calculate product")
    public double multiply(double... numbers) { /* ... */ }

    @Tool(description = "Divide two numbers")
    public double divide(double a, double b) { /* ... */ }
}
```

然後在 Application 中註冊一次即可：

```java
@Bean
public ToolCallbackProvider mathTools(MathToolProvider mathToolProvider) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(mathToolProvider)
            .build();
}
```

---

## 參考資源

- [Spring AI 1.0.3 官方文檔](https://docs.spring.io/spring-ai/reference/1.0.3/)
- [Spring Boot 3.5.7 文檔](https://docs.spring.io/spring-boot/docs/3.5.7/reference/)
- [MCP Protocol 規範](https://modelcontextprotocol.io/)
- [Open-Meteo API 文檔](https://open-meteo.com/en/docs)
- [Spring AI Examples - MCP](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol)

---

## 技術說明

### 為什麼選擇 Spring AI 1.0.3？

1. **穩定性**：1.0.3 是正式發布的穩定版本，適合生產環境使用
2. **兼容性**：與 Spring Boot 3.5.7 完全兼容
3. **長期支援**：穩定版本提供更好的長期維護和支援
4. **官方推薦**：使用官方文檔中推薦的 `@Tool` 和 `ToolCallbackProvider` 模式

### MCP Tools 開發模式

本專案使用 Spring AI 1.0.3 官方推薦的開發模式：

#### 1. @Tool 註解模式（本專案使用）

```java
@Service
public class MyService {
    @Tool(description = "Tool description")
    public String myTool(String param) {
        return "result";
    }
}

@Bean
public ToolCallbackProvider myTools(MyService service) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(service)
            .build();
}
```

**優點**：
- 符合 Spring AI 官方規範
- 類型安全
- IDE 友好
- 易於測試

#### 2. 低階 API 模式（進階使用）

```java
@Bean
public List<McpServerFeatures.SyncToolSpecification> customTools() {
    return List.of(
        new McpServerFeatures.SyncToolSpecification(
            new McpSchema.Tool("tool-name", "description", inputSchema),
            (exchange, request) -> {
                // 自定義邏輯
                return "result";
            }
        )
    );
}
```

**使用時機**：
- 需要完全控制工具規範
- 動態生成工具定義
- 特殊的參數處理需求

本專案選擇 `@Tool` 註解模式，因為它：
- 更簡潔易懂
- 適合大多數使用場景
- 遵循 Spring 的最佳實踐

---

## 授權

本專案為《Spring AI 實戰指南》配套範例程式碼，僅供學習使用。

---

## 更新日誌

- **2025-01-02**: 初始版本，使用 Spring AI 1.1.0-SNAPSHOT
- **2025-01-02**: 調整為 Spring AI 1.0.3 穩定版 + Spring Boot 3.5.7
- **2025-01-02**: 採用官方推薦的 `@Tool` 註解 + `MethodToolCallbackProvider` 模式
- **2025-01-02**: 為所有工具方法添加 `@Tool` 註解（WeatherToolProvider, MathToolProvider, TextToolProvider, TimeToolProvider）
- **2025-01-02**: 在 McpServerApplication 中註冊 4 個 ToolCallbackProvider Bean
- **2025-01-02**: 移除資源提供者（Spring AI 1.0.3 不支持 @McpResource 註解）
- **2025-01-02**: 編譯測試成功，確認專案可正常運行
- **2025-01-02**: 更新文檔，補充官方開發模式說明和最佳實踐

---

## 聯絡方式

如有問題或建議，請透過以下方式聯繫：
- GitHub Issues
- Email: [您的郵箱]

---

**Happy Coding! 🚀**
