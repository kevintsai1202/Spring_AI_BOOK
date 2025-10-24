/**
 * ChatModel 服務類別
 * 展示 ChatModel 底層 API 的使用方法
 */
package com.example.springai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.*;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatModelService {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    /**
     * 簡單字串呼叫
     */
    public String simpleCall(String message) {
        return chatModel.call(message);
    }

    /**
     * 使用 Prompt 物件的完整呼叫
     */
    public ChatResponse fullCall(String systemMessage, String userMessage) {
        Prompt prompt = new Prompt(List.of(
            new SystemMessage(systemMessage),
            new UserMessage(userMessage)
        ));

        return chatModel.call(prompt);
    }

    /**
     * 使用 PromptTemplate 的動態呼叫
     */
    public String templateCall(String template, Map<String, Object> variables) {
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(variables);

        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    /**
     * 流式呼叫
     */
    public Flux<String> streamCall(String message) {
        return streamingChatModel.stream(message);
    }

    /**
     * 帶選項的流式呼叫
     */
    public Flux<String> streamCallWithOptions(String message, ChatOptions options) {
        Prompt prompt = new Prompt(new UserMessage(message), options);
        return streamingChatModel.stream(prompt)
                .map(response -> response.getResult().getOutput().getText());
    }
}
