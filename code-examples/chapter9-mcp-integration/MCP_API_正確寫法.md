# Spring AI MCP 正確寫法驗證

## 🔍 問題發現

在開發 `chapter9-mcp-client-basic` 時，發現以下編譯錯誤：
```
package org.springframework.ai.mcp.client does not exist
package org.springframework.ai.mcp.spec does not exist
```

## ✅ 正確的依賴結構

### Maven 依賴樹
```
org.springframework.ai:spring-ai-starter-mcp-client:1.0.3
├─ org.springframework.ai:spring-ai-autoconfigure-mcp-client:1.0.3
└─ org.springframework.ai:spring-ai-mcp:1.0.3
   └─ io.modelcontextprotocol.sdk:mcp:0.10.0  ⬅️ MCP Java SDK
```

### 兩個核心模組

#### 1. Spring AI MCP 模組
**JAR**: `spring-ai-mcp-1.0.3.jar`
**包名**: `org.springframework.ai.mcp.*`
**提供的類**:
```java
org.springframework.ai.mcp.SyncMcpToolCallbackProvider
org.springframework.ai.mcp.AsyncMcpToolCallbackProvider
org.springframework.ai.mcp.SyncMcpToolCallback
org.springframework.ai.mcp.AsyncMcpToolCallback
org.springframework.ai.mcp.McpToolUtils
org.springframework.ai.mcp.customizer.McpSyncClientCustomizer
org.springframework.ai.mcp.customizer.McpAsyncClientCustomizer
```

#### 2. MCP Java SDK
**JAR**: `mcp-0.10.0.jar`
**包名**: `io.modelcontextprotocol.*`
**提供的類**:
```java
io.modelcontextprotocol.client.McpSyncClient
io.modelcontextprotocol.client.McpAsyncClient
io.modelcontextprotocol.spec.McpSchema
io.modelcontextprotocol.transport.Transport
```

## 📝 正確的 Import 語句

### ❌ 錯誤寫法（Context7 文檔中的示例）
```java
import org.springframework.ai.mcp.client.McpSyncClient;  // ❌ 不存在
import org.springframework.ai.mcp.spec.McpSchema;        // ❌ 不存在
```

### ✅ 正確寫法（實際可用的 API）
```java
// MCP Client 相關
import io.modelcontextprotocol.client.McpSyncClient;   // ✅
import io.modelcontextprotocol.client.McpAsyncClient;  // ✅

// MCP Schema 相關
import io.modelcontextprotocol.spec.McpSchema;         // ✅

// Spring AI MCP 整合類
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;  // ✅
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;  // ✅
```

## 🔧 修正示例

### ChatClientConfig.java
```java
package com.example.mcp.client.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;  // ✅ 正確
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    private final SyncMcpToolCallbackProvider toolCallbackProvider;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultFunctions(toolCallbackProvider.getToolCallbacks())
                .build();
    }
}
```

### McpClientService.java
```java
package com.example.mcp.client.service;

import io.modelcontextprotocol.client.McpSyncClient;  // ✅ 正確
import io.modelcontextprotocol.spec.McpSchema;        // ✅ 正確
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class McpClientService {

    private final List<McpSyncClient> mcpSyncClients;

    public List<ServerInfo> getAllServers() {
        return mcpSyncClients.stream()
                .map(client -> {
                    McpSchema.ServerInfo info = client.serverInfo();
                    // 處理 server info...
                })
                .toList();
    }
}
```

## 📊 驗證結果

### Demo 專案編譯測試
```bash
cd E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\demo
mvn clean compile
```

**結果**: ✅ BUILD SUCCESS

**確認事項**:
- ✅ Spring AI 1.0.3 MCP 依賴可從 Maven Central 下載
- ✅ MCP Java SDK 0.10.0 正常引入
- ✅ 使用正確的包名即可編譯成功

## 🎯 行動項

1. **更新所有 Service 類** - 修改 import 語句
   - `McpClientService.java`
   - `McpToolService.java`
   - `McpResourceService.java`

2. **更新配置類** - 修改 import 語句
   - `ChatClientConfig.java`
   - `McpClientConfig.java`

3. **重新編譯** - 確認所有編譯錯誤已解決

## 📚 參考資源

- **Spring AI 官方文檔**: https://docs.spring.io/spring-ai/reference/api/mcp/
- **MCP Java SDK**: https://modelcontextprotocol.io/sdk/java
- **MCP 規範**: https://spec.modelcontextprotocol.io/

## ⚠️ 注意事項

Context7 提供的文檔範例中使用的包名（`org.springframework.ai.mcp.client.*`）與實際 Spring AI 1.0.3 實現不符。實際上：

- **Spring AI** 只提供 Tool Callback 和 Customizer 的封裝
- **MCP Java SDK** 提供核心的 Client、Server、Schema 等類
- 應用程式需要**同時導入兩個模組**的類

這種設計讓 Spring AI 專注於與 Spring 框架的整合，而將 MCP 協議的核心實現委託給 MCP Java SDK。
