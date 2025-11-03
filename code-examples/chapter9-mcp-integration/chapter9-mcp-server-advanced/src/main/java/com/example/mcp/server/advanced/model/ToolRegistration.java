package com.example.mcp.server.advanced.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工具註冊資訊模型
 * 用於追蹤動態註冊的工具資訊
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolRegistration {

    /**
     * 工具名稱
     */
    private String name;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 輸入結構定義（JSON Schema）
     */
    private Map<String, Object> inputSchema;

    /**
     * 註冊時間
     */
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    /**
     * 工具狀態（active, inactive）
     */
    @Builder.Default
    private String status = "active";

    /**
     * 工具版本
     */
    @Builder.Default
    private String version = "1.0.0";
}
