/**
 * MCP Client 核心服務
 * 負責 MCP 工具發現、調用和管理
 */
package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.client.McpSyncClient;
import org.springframework.ai.mcp.client.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.ai.model.function.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpClientService {
    
    private final List<McpSyncClient> syncClients;
    private final SyncMcpToolCallbackProvider toolProvider;
    private final McpMetricsCollector metricsCollector;
    
    /**
     * 獲取所有可用的 MCP 工具回調
     */
    public ToolCallback[] getToolCallbacks() {
        try {
            log.debug("獲取 MCP 工具回調，客戶端數量: {}", syncClients.size());
            
            List<ToolCallback> callbacks = new ArrayList<>();
            
            for (McpSyncClient client : syncClients) {
                try {
                    // 獲取客戶端的工具回調
                    ToolCallback[] clientCallbacks = toolProvider.getToolCallbacks(client);
                    callbacks.addAll(Arrays.asList(clientCallbacks));
                    
                    log.debug("客戶端 {} 提供 {} 個工具", 
                        client.getServerName(), clientCallbacks.length);
                        
                } catch (Exception e) {
                    log.warn("獲取客戶端 {} 的工具失敗", client.getServerName(), e);
                }
            }
            
            ToolCallback[] result = callbacks.toArray(new ToolCallback[0]);
            log.info("總共獲取到 {} 個 MCP 工具", result.length);
            
            return result;
            
        } catch (Exception e) {
            log.error("獲取 MCP 工具回調失敗", e);
            return new ToolCallback[0];
        }
    }
    
    /**
     * 獲取可用工具的詳細信息
     */
    public List<ToolDefinition> getAvailableTools() {
        List<ToolDefinition> tools = new ArrayList<>();
        
        for (McpSyncClient client : syncClients) {
            try {
                // 列出客戶端的工具
                McpSchema.ListToolsResult result = client.listTools(
                    new McpSchema.ListToolsRequest());
                
                for (McpSchema.Tool tool : result.tools()) {
                    tools.add(convertToToolDefinition(tool, client.getServerName()));
                }
                
            } catch (Exception e) {
                log.warn("列出客戶端 {} 的工具失敗", client.getServerName(), e);
            }
        }
        
        return tools;
    }
    
    /**
     * 執行指定的 MCP 工具
     */
    public String executeTool(String toolName, Map<String, Object> parameters) {
        
        log.debug("執行 MCP 工具: {} with parameters: {}", toolName, parameters);
        
        for (McpSyncClient client : syncClients) {
            try {
                // 檢查客戶端是否有此工具
                if (hasToolInClient(client, toolName)) {
                    return executeToolInClient(client, toolName, parameters);
                }
                
            } catch (McpToolNotFoundException e) {
                // 繼續嘗試下一個客戶端
                continue;
            } catch (Exception e) {
                log.error("在客戶端 {} 執行工具 {} 失敗", 
                    client.getServerName(), toolName, e);
                throw new McpToolExecutionException("工具執行失敗: " + toolName, e);
            }
        }
        
        throw new McpToolNotFoundException("找不到工具: " + toolName);
    }
    
    /**
     * 獲取 MCP 伺服器信息
     */
    public List<McpServerInfo> getServerInfo() {
        return syncClients.stream()
            .map(client -> new McpServerInfo(
                client.getServerName(),
                client.getServerVersion(),
                client.isConnected(),
                getToolCountForClient(client)
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * 檢查客戶端是否有指定工具
     */
    private boolean hasToolInClient(McpSyncClient client, String toolName) {
        try {
            McpSchema.ListToolsResult result = client.listTools(
                new McpSchema.ListToolsRequest());
            
            return result.tools().stream()
                .anyMatch(tool -> tool.name().equals(toolName));
                
        } catch (Exception e) {
            log.warn("檢查客戶端 {} 工具失敗", client.getServerName(), e);
            return false;
        }
    }
    
    /**
     * 在指定客戶端執行工具
     */
    private String executeToolInClient(McpSyncClient client, String toolName, 
                                     Map<String, Object> parameters) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 構建工具調用請求
            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                toolName, parameters);
            
            // 執行工具
            McpSchema.CallToolResult result = client.callTool(request);
            
            // 處理結果
            String response = processToolResult(result);
            
            // 記錄指標
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordToolExecution(toolName, duration, true);
            
            log.debug("工具 {} 執行成功，耗時 {}ms", toolName, duration);
            return response;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordToolExecution(toolName, duration, false);
            
            log.error("工具 {} 執行失敗，耗時 {}ms", toolName, duration, e);
            throw e;
        }
    }
    
    /**
     * 處理工具執行結果
     */
    private String processToolResult(McpSchema.CallToolResult result) {
        if (result.content() == null || result.content().isEmpty()) {
            return "工具執行完成，無返回內容";
        }
        
        // 處理不同類型的內容
        StringBuilder response = new StringBuilder();
        for (McpSchema.Content content : result.content()) {
            if (content instanceof McpSchema.TextContent textContent) {
                response.append(textContent.text()).append("\n");
            } else if (content instanceof McpSchema.ImageContent imageContent) {
                response.append("[圖片: ").append(imageContent.data()).append("]\n");
            } else {
                response.append("[未知內容類型]\n");
            }
        }
        
        return response.toString().trim();
    }
    
    /**
     * 轉換 MCP 工具為 ToolDefinition
     */
    private ToolDefinition convertToToolDefinition(McpSchema.Tool tool, String serverName) {
        return ToolDefinition.builder()
            .name(tool.name())
            .description(tool.description())
            .inputSchema(tool.inputSchema())
            .metadata(Map.of(
                "serverName", serverName,
                "toolType", "mcp"
            ))
            .build();
    }
    
    /**
     * 獲取客戶端的工具數量
     */
    private int getToolCountForClient(McpSyncClient client) {
        try {
            McpSchema.ListToolsResult result = client.listTools(
                new McpSchema.ListToolsRequest());
            return result.tools().size();
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 工具定義類別
     */
    public static class ToolDefinition {
        private String name;
        private String description;
        private Object inputSchema;
        private Map<String, Object> metadata;
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Object getInputSchema() { return inputSchema; }
        public Map<String, Object> getMetadata() { return metadata; }
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private ToolDefinition definition = new ToolDefinition();
            
            public Builder name(String name) {
                definition.name = name;
                return this;
            }
            
            public Builder description(String description) {
                definition.description = description;
                return this;
            }
            
            public Builder inputSchema(Object inputSchema) {
                definition.inputSchema = inputSchema;
                return this;
            }
            
            public Builder metadata(Map<String, Object> metadata) {
                definition.metadata = metadata;
                return this;
            }
            
            public ToolDefinition build() { return definition; }
        }
    }
}

/**
 * MCP 伺服器信息記錄
 */
record McpServerInfo(
    String name,
    String version,
    boolean connected,
    int toolCount
) {}

/**
 * MCP 工具執行異常
 */
class McpToolExecutionException extends RuntimeException {
    public McpToolExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * MCP 工具未找到異常
 */
class McpToolNotFoundException extends RuntimeException {
    public McpToolNotFoundException(String message) {
        super(message);
    }
}

/**
 * MCP 指標收集器介面
 */
interface McpMetricsCollector {
    void recordToolExecution(String toolName, long duration, boolean success);
}