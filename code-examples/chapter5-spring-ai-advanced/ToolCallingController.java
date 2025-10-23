/**
 * Tool Calling 控制器
 * 展示 Spring AI 的工具調用功能
 */
package com.example.controller;

import com.example.tools.DateTimeTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tool-calling")
@RequiredArgsConstructor
@Slf4j
public class ToolCallingController {
    
    private final ChatModel chatModel;
    private final DateTimeTools dateTimeTools;
    
    /**
     * 基礎 Tool Calling 示例
     * @param prompt 使用者提示詞
     * @return AI 回應（可能包含工具調用結果）
     */
    @GetMapping("/basic")
    public String basicToolCalling(@RequestParam String prompt) {
        try {
            log.info("收到 Tool Calling 請求：{}", prompt);
            
            // 使用新的 ChatClient API 整合工具
            String response = ChatClient.create(chatModel)
                    .prompt(prompt)
                    .tools(dateTimeTools)  // 直接傳入 Tool 實例
                    .call()
                    .content();
            
            log.info("Tool Calling 回應：{}", response);
            return response;
            
        } catch (Exception e) {
            log.error("Tool Calling 執行失敗", e);
            return "Tool Calling 執行失敗：" + e.getMessage();
        }
    }
    
    /**
     * 多工具整合示例
     * @param prompt 使用者提示詞
     * @return AI 回應
     */
    @GetMapping("/multi-tools")
    public String multiToolCalling(@RequestParam String prompt) {
        try {
            // 可以同時註冊多個工具
            String response = ChatClient.create(chatModel)
                    .prompt(prompt)
                    .tools(dateTimeTools)  // 可以添加更多工具
                    .call()
                    .content();
            
            return response;
            
        } catch (Exception e) {
            log.error("多工具調用失敗", e);
            return "多工具調用失敗：" + e.getMessage();
        }
    }
}