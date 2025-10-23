/**
 * 聊天記憶控制器
 * 提供對話記憶的 REST API 端點
 */
package com.example.controller;

import com.example.service.BasicChatMemoryService;
import com.example.dto.ChatRequest;
import com.example.dto.ChatResponse;
import com.example.dto.ConversationHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMemoryController {
    
    private final BasicChatMemoryService chatService;
    
    /**
     * 對話 API
     */
    @PostMapping("/conversation/{conversationId}")
    public ResponseEntity<ChatResponse> chat(
            @PathVariable String conversationId,
            @RequestBody ChatRequest request) {
        
        String response = chatService.chat(conversationId, request.getMessage());
        
        return ResponseEntity.ok(ChatResponse.builder()
            .conversationId(conversationId)
            .message(response)
            .timestamp(LocalDateTime.now())
            .build());
    }
    
    /**
     * 獲取對話歷史
     */
    @GetMapping("/conversation/{conversationId}/history")
    public ResponseEntity<ConversationHistory> getHistory(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "50") int limit) {
        
        List<Message> messages = chatService.getConversationHistory(conversationId);
        
        // 限制返回的訊息數量
        if (messages.size() > limit) {
            messages = messages.subList(messages.size() - limit, messages.size());
        }
        
        return ResponseEntity.ok(ConversationHistory.builder()
            .conversationId(conversationId)
            .messages(messages)
            .totalCount(messages.size())
            .build());
    }
    
    /**
     * 清除對話
     */
    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<Void> clearConversation(@PathVariable String conversationId) {
        chatService.clearConversation(conversationId);
        return ResponseEntity.ok().build();
    }
}