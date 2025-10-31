package com.example.memory.controller;

import com.example.memory.dto.ChatRequest;
import com.example.memory.dto.ChatResponse;
import com.example.memory.service.ChatMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 對話REST控制器
 *
 * 提供對話相關的REST API端點
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    /**
     * 對話記憶服務
     */
    @Autowired
    private ChatMemoryService chatMemoryService;

    /**
     * 發送對話消息
     *
     * POST /api/chat/conversation/{conversationId}
     *
     * @param conversationId 對話ID
     * @param chatRequest 對話請求
     * @return 對話回應
     */
    @PostMapping("/conversation/{conversationId}")
    public ResponseEntity<ChatResponse> sendMessage(
            @PathVariable String conversationId,
            @RequestBody ChatRequest chatRequest) {

        log.info("Received message request for conversation: {}", conversationId);

        // 使用URL中的conversationId覆蓋請求中的ID
        chatRequest.setConversationId(conversationId);

        // 驗證必填字段
        if (chatRequest.getMessage() == null || chatRequest.getMessage().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 執行對話
        ChatResponse response = chatMemoryService.chat(chatRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 建立新對話
     *
     * POST /api/chat/new
     *
     * @param chatRequest 對話請求
     * @return 對話回應
     */
    @PostMapping("/new")
    public ResponseEntity<ChatResponse> newConversation(
            @RequestBody ChatRequest chatRequest) {

        log.info("Creating new conversation for user: {}", chatRequest.getUserId());

        // 執行對話
        ChatResponse response = chatMemoryService.chat(chatRequest);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 獲取對話歷史
     *
     * GET /api/chat/conversation/{conversationId}/history
     *
     * @param conversationId 對話ID
     * @param userId 用戶ID
     * @return 對話歷史
     */
    @GetMapping("/conversation/{conversationId}/history")
    public ResponseEntity<?> getConversationHistory(
            @PathVariable String conversationId,
            @RequestParam(required = false) String userId) {

        log.info("Retrieving history for conversation: {}", conversationId);

        try {
            var history = chatMemoryService.getConversationHistory(conversationId, userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error retrieving conversation history", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse("對話歷史不存在")
            );
        }
    }

    /**
     * 刪除對話
     *
     * DELETE /api/chat/conversation/{conversationId}
     *
     * @param conversationId 對話ID
     * @return 刪除結果
     */
    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<MessageResponse> deleteConversation(
            @PathVariable String conversationId) {

        log.info("Deleting conversation: {}", conversationId);

        try {
            chatMemoryService.clearConversation(conversationId);
            return ResponseEntity.ok(
                new MessageResponse("對話已成功刪除")
            );
        } catch (Exception e) {
            log.error("Error deleting conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new MessageResponse("刪除失敗: " + e.getMessage())
            );
        }
    }

    /**
     * 健康檢查端點
     *
     * GET /api/chat/health
     *
     * @return 健康狀態
     */
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(
            new MessageResponse("對話服務正常運行")
        );
    }

    /**
     * 錯誤回應內部類
     */
    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    /**
     * 消息回應內部類
     */
    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
