package com.example.mcp.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 應用配置類
 * 從 application.yml 中讀取應用配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationConfig {

    /**
     * 應用名稱
     */
    private String name;

    /**
     * 應用版本
     */
    private String version;

    /**
     * 功能開關配置
     * key: 功能名稱
     * value: 是否啟用
     */
    private Map<String, Boolean> features;

    /**
     * 獲取JSON格式的配置資訊
     * @return JSON 字符串
     */
    public String toJson() {
        return String.format("""
            {
              "name": "%s",
              "version": "%s",
              "features": %s
            }
            """, name, version, formatFeatures());
    }

    /**
     * 格式化功能配置為 JSON 格式
     * @return JSON 字符串
     */
    private String formatFeatures() {
        if (features == null || features.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{\n");
        features.forEach((key, value) ->
            sb.append(String.format("    \"%s\": %s,\n", key, value))
        );

        // 移除最後一個逗號
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
            sb.append("\n");
        }

        sb.append("  }");
        return sb.toString();
    }
}
