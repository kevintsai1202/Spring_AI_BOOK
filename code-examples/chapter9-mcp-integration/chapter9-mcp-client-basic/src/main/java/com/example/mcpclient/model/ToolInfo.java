package com.example.mcpclient.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * MCP 工具的元數據信息
 *
 * 封裝工具的名稱、描述、參數定義等信息。
 */
@Data
@Builder
public class ToolInfo {

    /**
     * 工具名稱
     * 例如: "brave_web_search", "resolve-library-id"
     */
    private String name;

    /**
     * 工具描述
     * 說明工具的功能和用途
     */
    private String description;

    /**
     * 輸入參數定義 (JSON Schema)
     * 定義工具需要哪些參數以及參數的類型、約束等
     */
    private Map<String, Object> inputSchema;

    /**
     * 來源 Server 名稱
     * 標識這個工具來自哪個 MCP Server
     */
    private String serverName;
}
