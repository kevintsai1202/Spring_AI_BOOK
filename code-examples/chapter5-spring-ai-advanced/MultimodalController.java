/**
 * 多模態控制器
 * 提供圖片、音訊等多媒體檔案的 AI 分析功能
 */
package com.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/multimodal")
@RequiredArgsConstructor
@Slf4j
public class MultimodalController {
    
    private final ChatClient chatClient;
    
    /**
     * 基礎圖片分析
     * @param file 上傳的圖片檔案
     * @param message 分析要求
     * @return AI 分析結果
     */
    @PostMapping(value = "/image-analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String analyzeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("message") String message) {
        
        try {
            // 驗證檔案類型
            if (!isValidImageFile(file)) {
                return "❌ 不支援的圖片格式，請上傳 JPG、PNG、GIF 或 WebP 格式的圖片";
            }
            
            // 驗證檔案大小（限制 10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return "❌ 圖片檔案過大，請上傳小於 10MB 的圖片";
            }
            
            log.info("開始分析圖片：{}，大小：{} bytes", file.getOriginalFilename(), file.getSize());
            
            // 使用 ChatClient 進行圖片分析
            String response = chatClient.prompt()
                    .user(u -> u.text(message)
                            .media(MimeTypeUtils.parseMimeType(file.getContentType()), 
                                   file.getResource()))
                    .call()
                    .content();
            
            log.info("圖片分析完成");
            return response;
            
        } catch (IOException e) {
            log.error("圖片處理失敗", e);
            return "❌ 圖片處理失敗：" + e.getMessage();
        } catch (Exception e) {
            log.error("AI 分析失敗", e);
            return "❌ AI 分析失敗，請稍後再試：" + e.getMessage();
        }
    }
    
    /**
     * 驗證圖片檔案格式
     */
    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif") ||
            contentType.equals("image/webp")
        );
    }
}