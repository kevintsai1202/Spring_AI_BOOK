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
     */
    private Map<String, String> library = Map.of(
        "explain", """
            作為一個 {role} 專家，請詳細說明 {topic}。
            
            請包含：
            1. 核心概念
            2. 主要特性
            3. 使用場景
            4. 實際範例
            
            目標受眾：{audience}
            回答語言：{language}
            """,
            
        "code-review", """
            你是一個資深的 {language} 程式碼審查專家。
            
            請審查以下程式碼：
            ```{language}
            {code}
            ```
            
            請提供：
            1. 程式碼品質評估
            2. 潛在問題識別
            3. 改進建議
            4. 最佳實踐建議
            
            請用 {language} 回答。
            """,
            
        "troubleshoot", """
            你是一個 {technology} 故障排除專家。
            
            **問題描述**：{problem}
            **錯誤訊息**：{error}
            **環境資訊**：{environment}
            
            請提供：
            ## 🔍 問題診斷
            ## 🛠️ 解決步驟
            ## 🚀 預防措施
            
            請提供具體可執行的解決方案。
            """
    );
    
    /**
     * 預設參數值
     */
    private Map<String, String> defaults = Map.of(
        "language", "繁體中文",
        "role", "技術專家",
        "audience", "中級開發者"
    );
}