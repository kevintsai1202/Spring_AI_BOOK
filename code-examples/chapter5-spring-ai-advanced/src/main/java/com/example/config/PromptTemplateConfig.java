/**
 * 提示詞範本配置類別
 * 管理預定義的提示詞範本庫
 */
package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app.prompt.templates")
public class PromptTemplateConfig {

    /**
     * 預定義範本庫
     * (從 application.yml 讀取，此處保留為備用)
     */
    private Map<String, String> library = Map.of();

    /**
     * 預設參數值
     */
    private Map<String, String> defaults = Map.of(
        "language", "繁體中文",
        "role", "技術專家",
        "audience", "中級開發者"
    );
}
