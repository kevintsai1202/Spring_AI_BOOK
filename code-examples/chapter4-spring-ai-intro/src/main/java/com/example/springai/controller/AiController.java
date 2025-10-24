/**
 * AI 控制器
 * 提供基本的 AI 對話功能
 * 展示 ChatClient Fluent API 的使用
 */
package com.example.springai.controller;

import com.example.springai.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final ChatClient chatClient;

    /**
     * 基本 AI 對話端點
     * @param prompt 使用者輸入的提示詞
     * @return AI 的回應
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String prompt) {
        log.info("收到 AI 對話請求：{}", prompt);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        log.info("AI 回應：{}", response);
        return response;
    }

    /**
     * 帶有系統提示詞的 AI 對話
     * @param request 聊天請求
     * @return AI 回應
     */
    @PostMapping("/chat/system")
    public String chatWithSystem(@RequestBody ChatRequest request) {
        return chatClient.prompt()
                .system(request.getSystemMessage())
                .user(request.getUserMessage())
                .call()
                .content();
    }

    /**
     * Hello World 專用端點
     * @return AI 生成的 Hello World 程式
     */
    @GetMapping("/hello-world")
    public String helloWorld() {
        String prompt = "請用 Java Spring Boot 寫一個 Hello World 程式，" +
                       "包含一個簡單的 REST API 端點";

        return chatClient.prompt()
                .system("你是一個專業的 Java 開發專家，" +
                       "請提供清晰、可執行的程式碼範例")
                .user(prompt)
                .call()
                .content();
    }
}
