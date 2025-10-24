/**
 * 範本控制器
 * 展示提示詞範本與 ChatClient 的整合使用
 */
package com.example.controller;

import com.example.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/template")
@RequiredArgsConstructor
public class TemplateController {

    private final ChatClient chatClient;
    private final PromptTemplateService promptTemplateService;

    /**
     * 使用範本進行 AI 對話
     * @param topic 主題
     * @param level 程度（初級/中級/高級）
     * @return AI 回應
     */
    @GetMapping("/explain")
    public String explainTopic(
            @RequestParam String topic,
            @RequestParam(defaultValue = "中級") String level) {

        // 使用範本服務建立 Prompt
        Prompt prompt = promptTemplateService.createBasicPrompt(topic, level);

        // 使用 ChatClient 執行對話
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    /**
     * 直接使用 ChatClient 的範本功能
     * @param framework 框架名稱
     * @return AI 回應
     */
    @GetMapping("/framework")
    public String explainFramework(@RequestParam String framework) {
        String template = "請問 {framework} 目前有哪些版本，各有什麼特殊能力和改進？";

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("framework", framework));

        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}
