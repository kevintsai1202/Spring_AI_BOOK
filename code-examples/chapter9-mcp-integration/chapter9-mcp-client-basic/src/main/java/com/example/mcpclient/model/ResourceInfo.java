package com.example.mcpclient.model;

import lombok.Builder;
import lombok.Data;

/**
 * MCP 資源的元數據信息
 *
 * 封裝資源的URI、名稱、描述等信息。
 */
@Data
@Builder
public class ResourceInfo {

    /**
     * 資源 URI
     * 例如: "user://john", "config://database"
     */
    private String uri;

    /**
     * 資源名稱
     * 人類可讀的資源名稱
     */
    private String name;

    /**
     * 資源描述
     * 說明資源的內容和用途
     */
    private String description;

    /**
     * 資源 MIME 類型
     * 例如: "text/plain", "application/json"
     */
    private String mimeType;
}
