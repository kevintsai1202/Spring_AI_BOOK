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

        // 從配置讀取或使用預設模板
        String template = "You are a senior {language} developer expert in {framework} framework. "
                + "Generate high-quality code based on the following requirements: "
                + "Requirement: {functionality}, Complexity Level: {complexity}";

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

        String template = "You are an experienced {technology} expert. "
                + "Problem Context: {context} "
                + "Error Message: {errorMessage} "
                + "Please provide detailed analysis and solutions.";

        PromptTemplate promptTemplate = new PromptTemplate(template);

        return promptTemplate.create(Map.of(
            "technology", technology,
            "errorMessage", errorMessage,
            "context", context
        ));
    }
}
