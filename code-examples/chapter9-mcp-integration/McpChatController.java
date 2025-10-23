/**
 * MCP 增強的聊天控制器
 * 整合 MCP 工具與 ChatClient 的 REST API
 */
package com.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.function.ToolCallback;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.springframework.http.codec.ServerSentEvent;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class McpChatController {
    
    private final ChatClient chatClient;
    private final McpClientService mcpService;
    private final McpChatHistoryService historyService;
    
    /**
     * 使用 MCP 工具的聊天端點
     */
    @PostMapping("/mcp")
    public ResponseEntity<ChatResponse> chatWithMcp(@RequestBody ChatRequest request) {
        
        try {
            log.info("收到 MCP 聊天請求: {}", request.getMessage());
            
            // 獲取 MCP 工具
            ToolCallback[] mcpTools = mcpService.getToolCallbacks();
            log.debug("可用 MCP 工具數量: {}", mcpTools.length);
            
            // 使用 ChatClient 與 MCP 工具整合
            String response = chatClient.prompt()
                .system("""
                    你是一個企業級 AI 助手，具備以下能力：
                    1. 可以使用各種 MCP 工具來協助用戶
                    2. 根據用戶需求選擇合適的工具
                    3. 提供準確、有用的回答
                    4. 當需要使用工具時，請明確說明使用的工具和原因
                    """)
                .user(request.getMessage())
                .tools(mcpTools)  // 自動整合 MCP 工具
                .call()
                .content();
            
            // 保存聊天歷史
            historyService.saveChatHistory(request.getMessage(), response, mcpTools.length);
            
            return ResponseEntity.ok(new ChatResponse(response, mcpTools.length));
            
        } catch (Exception e) {
            log.error("MCP 聊天處理失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ChatResponse("抱歉，服務暫時不可用，請稍後再試。", 0));
        }
    }
    
    /**
     * 流式聊天端點
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestBody ChatRequest request) {
        
        try {
            log.info("收到流式 MCP 聊天請求: {}", request.getMessage());
            
            // 獲取 MCP 工具
            ToolCallback[] mcpTools = mcpService.getToolCallbacks();
            
            // 建立流式聊天
            return chatClient.prompt()
                .system("你是一個智能助手，可以使用各種工具來協助用戶。")
                .user(request.getMessage())
                .tools(mcpTools)
                .stream()
                .content()
                .map(content -> ServerSentEvent.<String>builder()
                    .data(content)
                    .build())
                .onErrorResume(error -> {
                    log.error("流式聊天錯誤", error);
                    return Flux.just(ServerSentEvent.<String>builder()
                        .data("[錯誤] 服務暫時不可用")
                        .build());
                });
                
        } catch (Exception e) {
            log.error("流式聊天初始化失敗", e);
            return Flux.just(ServerSentEvent.<String>builder()
                .data("[錯誤] 無法初始化聊天服務")
                .build());
        }
    }
    
    /**
     * 獲取可用的 MCP 工具列表
     */
    @GetMapping("/tools")
    public ResponseEntity<List<ToolInfo>> getAvailableTools() {
        
        try {
            List<McpClientService.ToolDefinition> tools = mcpService.getAvailableTools();
            
            List<ToolInfo> toolInfos = tools.stream()
                .map(tool -> new ToolInfo(
                    tool.getName(),
                    tool.getDescription(),
                    tool.getMetadata().get("serverName").toString()
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(toolInfos);
            
        } catch (Exception e) {
            log.error("獲取工具列表失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.emptyList());
        }
    }
    
    /**
     * 直接執行 MCP 工具
     */
    @PostMapping("/tools/{toolName}/execute")
    public ResponseEntity<ToolExecutionResponse> executeTool(
            @PathVariable String toolName,
            @RequestBody Map<String, Object> parameters) {
        
        try {
            log.info("直接執行 MCP 工具: {} with parameters: {}", toolName, parameters);
            
            String result = mcpService.executeTool(toolName, parameters);
            
            return ResponseEntity.ok(new ToolExecutionResponse(
                toolName, parameters, result, true, null));
            
        } catch (McpToolNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ToolExecutionResponse(
                    toolName, parameters, null, false, "工具未找到: " + toolName));
        } catch (Exception e) {
            log.error("執行工具失敗: {}", toolName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ToolExecutionResponse(
                    toolName, parameters, null, false, "工具執行失敗: " + e.getMessage()));
        }
    }
    
    /**
     * 獲取 MCP 伺服器狀態
     */
    @GetMapping("/servers")
    public ResponseEntity<List<McpServerInfo>> getServerStatus() {
        
        try {
            List<McpServerInfo> servers = mcpService.getServerInfo();
            return ResponseEntity.ok(servers);
            
        } catch (Exception e) {
            log.error("獲取伺服器狀態失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.emptyList());
        }
    }
    
    /**
     * 獲取聊天歷史
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatHistoryEntry>> getChatHistory(
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<ChatHistoryEntry> history = historyService.getChatHistory(limit);
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            log.error("獲取聊天歷史失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.emptyList());
        }
    }
    
    /**
     * 清除聊天歷史
     */
    @DeleteMapping("/history")
    public ResponseEntity<Void> clearChatHistory() {
        
        try {
            historyService.clearChatHistory();
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("清除聊天歷史失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

/**
 * 聊天請求
 */
record ChatRequest(String message) {}

/**
 * 聊天回應
 */
record ChatResponse(String response, int availableTools) {}

/**
 * 工具信息
 */
record ToolInfo(String name, String description, String serverName) {}

/**
 * 工具執行回應
 */
record ToolExecutionResponse(
    String toolName,
    Map<String, Object> parameters,
    String result,
    boolean success,
    String error
) {}

/**
 * 聊天歷史條目
 */
record ChatHistoryEntry(
    String userMessage,
    String aiResponse,
    int toolsUsed,
    long timestamp
) {}

/**
 * MCP 聊天歷史服務介面
 */
interface McpChatHistoryService {
    void saveChatHistory(String userMessage, String aiResponse, int toolsUsed);
    List<ChatHistoryEntry> getChatHistory(int limit);
    void clearChatHistory();
}

/**
 * MCP 客戶端服務介面
 */
interface McpClientService {
    ToolCallback[] getToolCallbacks();
    List<ToolDefinition> getAvailableTools();
    String executeTool(String toolName, Map<String, Object> parameters);
    List<McpServerInfo> getServerInfo();
    
    class ToolDefinition {
        private String name;
        private String description;
        private Map<String, Object> metadata;
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Map<String, Object> getMetadata() { return metadata; }
    }
}