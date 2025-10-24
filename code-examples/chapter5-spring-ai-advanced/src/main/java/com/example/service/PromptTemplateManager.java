/**
 * 提示詞範本管理器
 * 提供範本的建立、註冊和驗證功能
 */
package com.example.service;

import com.example.config.PromptTemplateConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptTemplateManager {

    private final PromptTemplateConfig config;

    /**
     * 根據範本名稱建立 Prompt
     * @param templateName 範本名稱
     * @param variables 變數值
     * @return 建立的 Prompt
     */
    public Prompt createPrompt(String templateName, Map<String, Object> variables) {
        String template = config.getLibrary().get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("範本不存在: " + templateName);
        }

        // 合併預設值和使用者提供的變數
        Map<String, Object> mergedVariables = new HashMap<>(config.getDefaults());
        mergedVariables.putAll(variables);

        log.debug("使用範本 '{}' 建立 Prompt，變數：{}", templateName, mergedVariables);

        PromptTemplate promptTemplate = new PromptTemplate(template);
        return promptTemplate.create(mergedVariables);
    }

    /**
     * 動態註冊新範本
     * @param name 範本名稱
     * @param template 範本內容
     */
    public void registerTemplate(String name, String template) {
        config.getLibrary().put(name, template);
        log.info("註冊新範本：{}", name);
    }

    /**
     * 獲取所有可用範本名稱
     * @return 範本名稱列表
     */
    public Set<String> getAvailableTemplates() {
        return config.getLibrary().keySet();
    }

    /**
     * 驗證範本語法
     * @param template 範本內容
     * @param variables 測試變數
     * @return 是否有效
     */
    public boolean validateTemplate(String template, Map<String, Object> variables) {
        try {
            PromptTemplate promptTemplate = new PromptTemplate(template);
            promptTemplate.create(variables);
            return true;
        } catch (Exception e) {
            log.warn("範本驗證失敗：{}", e.getMessage());
            return false;
        }
    }
}
