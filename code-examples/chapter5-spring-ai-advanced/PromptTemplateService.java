/**
 * 提示詞範本服務
 * 提供基礎的提示詞範本功能
 */
package com.example.service;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromptTemplateService {
    
    /**
     * 基本範本使用範例
     */
    public Prompt createBasicPrompt(String topic, String level) {
        // 定義範本字串，使用 {變數名} 作為佔位符
        String template = """
            作為一個資深的技術專家，請詳細說明 {topic} 的相關知識。
            
            請包含以下內容：
            1. 核心概念和定義
            2. 主要特性和優勢
            3. 實際應用場景
            4. 簡單的程式碼範例
            
            目標受眾：{level} 開發者
            回答語言：繁體中文
            回答風格：專業且易懂
            """;
        
        // 建立 PromptTemplate 實例
        PromptTemplate promptTemplate = new PromptTemplate(template);
        
        // 提供變數值
        Map<String, Object> variables = Map.of(
            "topic", topic,
            "level", level
        );
        
        // 生成最終的 Prompt
        return promptTemplate.create(variables);
    }
}