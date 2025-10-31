package com.example.memory.vector.controller;

import com.example.memory.vector.model.VectorChatRequest;
import com.example.memory.vector.model.VectorChatResponse;
import com.example.memory.vector.service.VectorChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 向量記憶聊天控制器
 *
 * 提供對話 API
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final VectorChatService vectorChatService;

    /**
     * 健康檢查
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Vector Chat Service is running");
    }

    /**
     * 混合記憶對話 (短期 + 長期)
     */
    @PostMapping("/{conversationId}/hybrid")
    public ResponseEntity<VectorChatResponse> hybridChat(
            @PathVariable String conversationId,
            @RequestBody VectorChatRequest request) {

        log.info("混合記憶對話請求: conversationId={}, message={}",
            conversationId, request.getMessage());

        String response = vectorChatService.chat(conversationId, request.getMessage());
        int memoryCount = vectorChatService.getMemoryCount(conversationId);

        return ResponseEntity.ok(VectorChatResponse.builder()
            .conversationId(conversationId)
            .response(response)
            .strategy("HYBRID")
            .shortTermCount(memoryCount)
            .longTermCount(0)  // 簡化版本
            .timestamp(LocalDateTime.now())
            .build());
    }

    /**
     * 短期記憶對話
     */
    @PostMapping("/{conversationId}/short-term")
    public ResponseEntity<VectorChatResponse> shortTermChat(
            @PathVariable String conversationId,
            @RequestBody VectorChatRequest request) {

        log.info("短期記憶對話請求: conversationId={}, message={}",
            conversationId, request.getMessage());

        String response = vectorChatService.chatWithShortTermMemory(
            conversationId, request.getMessage());
        int memoryCount = vectorChatService.getMemoryCount(conversationId);

        return ResponseEntity.ok(VectorChatResponse.builder()
            .conversationId(conversationId)
            .response(response)
            .strategy("SHORT_TERM")
            .shortTermCount(memoryCount)
            .longTermCount(0)
            .timestamp(LocalDateTime.now())
            .build());
    }

    /**
     * 長期記憶對話 (向量檢索)
     */
    @PostMapping("/long-term")
    public ResponseEntity<VectorChatResponse> longTermChat(
            @RequestBody VectorChatRequest request) {

        log.info("長期記憶對話請求: message={}", request.getMessage());

        String response = vectorChatService.chatWithLongTermMemory(request.getMessage());

        return ResponseEntity.ok(VectorChatResponse.builder()
            .conversationId("vector-search")
            .response(response)
            .strategy("LONG_TERM")
            .shortTermCount(0)
            .longTermCount(0)  // 簡化版本
            .timestamp(LocalDateTime.now())
            .build());
    }

    /**
     * 清除對話記憶
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<String> clearMemory(@PathVariable String conversationId) {
        log.info("清除對話記憶: conversationId={}", conversationId);
        vectorChatService.clearMemory(conversationId);
        return ResponseEntity.ok("Memory cleared for conversation: " + conversationId);
    }
}
