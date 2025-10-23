/**
 * MCP 資源存取服務
 * 負責 MCP 資源的讀取、列表和管理
 */
package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.client.McpSyncClient;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpResourceService {
    
    private final List<McpSyncClient> syncClients;
    private final McpResourceCache resourceCache;
    
    /**
     * 讀取 MCP 資源
     */
    public McpResourceContent readResource(String resourceUri) {
        
        log.debug("讀取 MCP 資源: {}", resourceUri);
        
        // 檢查快取
        McpResourceContent cached = resourceCache.get(resourceUri);
        if (cached != null) {
            log.debug("從快取獲取資源: {}", resourceUri);
            return cached;
        }
        
        // 嘗試從各個客戶端讀取資源
        for (McpSyncClient client : syncClients) {
            try {
                if (supportsResource(client, URI.create(resourceUri))) {
                    
                    McpSchema.ReadResourceRequest request = 
                        new McpSchema.ReadResourceRequest(URI.create(resourceUri));
                    
                    McpSchema.ReadResourceResult result = client.readResource(request);
                    
                    McpResourceContent content = new McpResourceContent(
                        resourceUri,
                        result.contents(),
                        client.getServerName(),
                        Instant.now()
                    );
                    
                    // 快取結果
                    resourceCache.put(resourceUri, content);
                    
                    log.debug("成功讀取資源: {} from {}", resourceUri, client.getServerName());
                    return content;
                }
                
            } catch (Exception e) {
                log.warn("從客戶端 {} 讀取資源 {} 失敗", client.getServerName(), resourceUri, e);
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
                McpSchema.ListResourcesRequest request = new McpSchema.ListResourcesRequest();
                McpSchema.ListResourcesResult result = client.listResources(request);
                
                List<McpResourceInfo> clientResources = result.resources().stream()
                    .map(resource -> new McpResourceInfo(
                        resource.uri().toString(),
                        resource.name(),
                        resource.description().orElse(""),
                        resource.mimeType().orElse("text/plain"),
                        client.getServerName()
                    ))
                    .collect(Collectors.toList());
                
                allResources.addAll(clientResources);
                
                log.debug("客戶端 {} 提供 {} 個資源", 
                    client.getServerName(), clientResources.size());
                
            } catch (Exception e) {
                log.warn("列出客戶端 {} 的資源失敗", client.getServerName(), e);
            }
        }
        
        log.info("總共找到 {} 個 MCP 資源", allResources.size());
        return allResources;
    }
    
    /**
     * 搜尋資源
     */
    public List<McpResourceInfo> searchResources(String query) {
        return listAllResources().stream()
            .filter(resource -> 
                resource.name().toLowerCase().contains(query.toLowerCase()) ||
                resource.description().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    /**
     * 按類型過濾資源
     */
    public List<McpResourceInfo> getResourcesByType(String mimeType) {
        return listAllResources().stream()
            .filter(resource -> resource.mimeType().equals(mimeType))
            .collect(Collectors.toList());
    }
    
    /**
     * 按伺服器過濾資源
     */
    public List<McpResourceInfo> getResourcesByServer(String serverName) {
        return listAllResources().stream()
            .filter(resource -> resource.serverName().equals(serverName))
            .collect(Collectors.toList());
    }
    
    /**
     * 批次讀取資源
     */
    public Map<String, McpResourceContent> readMultipleResources(List<String> resourceUris) {
        Map<String, McpResourceContent> results = new HashMap<>();
        
        for (String uri : resourceUris) {
            try {
                McpResourceContent content = readResource(uri);
                results.put(uri, content);
            } catch (Exception e) {
                log.warn("讀取資源 {} 失敗", uri, e);
                // 繼續處理其他資源
            }
        }
        
        return results;
    }
    
    /**
     * 檢查資源是否存在
     */
    public boolean resourceExists(String resourceUri) {
        try {
            readResource(resourceUri);
            return true;
        } catch (McpResourceNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 獲取資源統計信息
     */
    public McpResourceStatistics getResourceStatistics() {
        List<McpResourceInfo> allResources = listAllResources();
        
        Map<String, Long> typeCount = allResources.stream()
            .collect(Collectors.groupingBy(
                McpResourceInfo::mimeType,
                Collectors.counting()
            ));
        
        Map<String, Long> serverCount = allResources.stream()
            .collect(Collectors.groupingBy(
                McpResourceInfo::serverName,
                Collectors.counting()
            ));
        
        return new McpResourceStatistics(
            allResources.size(),
            typeCount,
            serverCount,
            Instant.now()
        );
    }
    
    /**
     * 檢查客戶端是否支援指定資源
     */
    private boolean supportsResource(McpSyncClient client, URI resourceUri) {
        try {
            // 檢查客戶端是否列出了此資源
            McpSchema.ListResourcesResult result = client.listResources(
                new McpSchema.ListResourcesRequest());
            
            return result.resources().stream()
                .anyMatch(resource -> resource.uri().equals(resourceUri));
                
        } catch (Exception e) {
            log.debug("檢查客戶端 {} 資源支援失敗", client.getServerName(), e);
            return false;
        }
    }
    
    /**
     * 清除資源快取
     */
    public void clearResourceCache() {
        resourceCache.clear();
        log.info("已清除 MCP 資源快取");
    }
    
    /**
     * 預載入常用資源
     */
    public void preloadCommonResources() {
        List<McpResourceInfo> allResources = listAllResources();
        
        // 預載入前 10 個資源
        allResources.stream()
            .limit(10)
            .forEach(resource -> {
                try {
                    readResource(resource.uri());
                    log.debug("預載入資源: {}", resource.uri());
                } catch (Exception e) {
                    log.warn("預載入資源 {} 失敗", resource.uri(), e);
                }
            });
    }
}

/**
 * MCP 資源內容
 */
record McpResourceContent(
    String uri,
    List<McpSchema.Content> contents,
    String serverName,
    Instant timestamp
) {}

/**
 * MCP 資源信息
 */
record McpResourceInfo(
    String uri,
    String name,
    String description,
    String mimeType,
    String serverName
) {}

/**
 * MCP 資源統計
 */
record McpResourceStatistics(
    int totalResources,
    Map<String, Long> resourcesByType,
    Map<String, Long> resourcesByServer,
    Instant generatedAt
) {}

/**
 * MCP 資源未找到異常
 */
class McpResourceNotFoundException extends RuntimeException {
    public McpResourceNotFoundException(String message) {
        super(message);
    }
}

/**
 * MCP 資源快取介面
 */
interface McpResourceCache {
    McpResourceContent get(String uri);
    void put(String uri, McpResourceContent content);
    void clear();
}