/**
 * 進階提示詞服務
 * 提供複雜的提示詞範本功能
 */
package com.example.service;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AdvancedPromptService {
    
    /**
     * 程式碼生成範本
     */
    public Prompt createCodeGenerationPrompt(
            String language, 
            String functionality, 
            String framework,
            String complexity) {
        
        String template = """
            你是一個資深的 {language} 開發專家，精通 {framework} 框架。
            
            請根據以下需求生成高品質的程式碼：
            
            **需求描述**：{functionality}
            **複雜度等級**：{complexity}
            **技術要求**：
            - 使用 {language} 程式語言
            - 基於 {framework} 框架
            - 遵循最佳實踐和設計模式
            - 包含適當的註解和錯誤處理
            
            **輸出格式**：
            1. 簡要說明實現思路
            2. 完整的程式碼實現
            3. 使用說明和注意事項
            
            請確保程式碼具有良好的可讀性和可維護性。
            """;
        
        PromptTemplate promptTemplate = new PromptTemplate(template);
        
        return promptTemplate.create(Map.of(
            "language", language,
            "functionality", functionality,
            "framework", framework,
            "complexity", complexity
        ));
    }
    
    /**
     * 問題診斷範本
     */
    public Prompt createDiagnosisPrompt(
            String technology,
            String errorMessage,
            String context) {
        
        String template = """
            你是一個經驗豐富的 {technology} 技術專家和問題診斷師。
            
            **問題情境**：
            {context}
            
            **錯誤訊息**：
            ```
            {errorMessage}
            ```
            
            請提供詳細的問題分析和解決方案：
            
            ## 🔍 問題分析
            - 錯誤原因分析
            - 可能的觸發條件
            
            ## 🛠️ 解決方案
            1. **立即解決方案**（快速修復）
            2. **根本解決方案**（長期穩定）
            3. **預防措施**（避免再次發生）
            
            ## 📝 相關建議
            - 最佳實踐建議
            - 相關文檔連結
            
            請用繁體中文回答，並提供具體可執行的步驟。
            """;
        
        PromptTemplate promptTemplate = new PromptTemplate(template);
        
        return promptTemplate.create(Map.of(
            "technology", technology,
            "errorMessage", errorMessage,
            "context", context
        ));
    }
}