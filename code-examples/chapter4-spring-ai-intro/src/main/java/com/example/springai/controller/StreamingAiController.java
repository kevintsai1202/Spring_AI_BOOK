/**
 * 流式 AI 控制器
 * 提供 AI 流式輸出功能，實現類似 ChatGPT 的即時回應效果
 */
package com.example.springai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class StreamingAiController {

    private final ChatClient chatClient;

    /**
     * 基本流式 AI 對話
     * @param prompt 使用者輸入的提示詞
     * @return 流式字串回應
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String prompt) {
        log.info("開始流式對話：{}", prompt);

        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

    /**
     * 帶系統提示詞的流式對話
     * @param prompt 使用者輸入
     * @param system 系統提示詞
     * @return 流式回應
     */
    @GetMapping(value = "/chat/stream/system", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamWithSystem(
            @RequestParam String prompt,
            @RequestParam(defaultValue = "你是一個友善且專業的 AI 助手") String system) {

        return chatClient.prompt()
                .system(system)
                .user(prompt)
                .stream()
                .content()
                .doOnSubscribe(subscription ->
                    log.info("開始流式對話 - 系統：{}, 使用者：{}", system, prompt))
                .doOnComplete(() ->
                    log.info("流式對話完成"));
    }

    /**
     * 自定義流式輸出處理
     * @param prompt 使用者輸入
     * @return 處理後的流式回應
     */
    @GetMapping(value = "/chat/stream/custom", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamCustom(@RequestParam String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .map(content -> {
                    // 自定義處理每個流式片段
                    return "🤖 AI: " + content;
                })
                .filter(content -> {
                    // 過濾空內容
                    return content != null && !content.trim().isEmpty();
                })
                .doOnNext(content -> {
                    // 記錄每個片段
                    log.debug("流式內容：{}", content);
                })
                .onErrorResume(error -> {
                    log.error("流式輸出錯誤", error);
                    return Flux.just("❌ 抱歉，發生錯誤：" + error.getMessage());
                });
    }
}
