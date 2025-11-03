package com.example.mcpclient.model;

import lombok.Builder;
import lombok.Data;

/**
 * MCP Server 基本信息
 *
 * 用於封裝 MCP Server 的元數據，包括名稱、版本、協議版本和能力。
 */
@Data
@Builder
public class ServerInfoResponse {

    /**
     * Server 名稱
     * 例如: "brave-search", "context7"
     */
    private String name;

    /**
     * Server 版本
     * 例如: "1.0.0"
     */
    private String version;

    /**
     * MCP 協議版本
     * 例如: "2024-11-05"
     */
    private String protocolVersion;

    /**
     * Server 能力
     * 描述 Server 支援哪些功能(工具、資源、提示等)
     */
    private ServerCapabilities capabilities;

    /**
     * 傳輸方式
     * 可能值: "STDIO", "SSE"
     */
    private String transport;
}
