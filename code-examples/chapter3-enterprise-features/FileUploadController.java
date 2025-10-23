/**
 * 檔案上傳控制器
 * 提供基本的檔案上傳功能
 */
package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@Slf4j
public class FileUploadController {
    
    @Value("${app.upload.path:./uploads}")
    private String uploadPath;
    
    /**
     * 單檔案上傳
     * @param file 上傳的檔案
     * @return 上傳結果
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        
        // 檢查檔案是否為空
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("請選擇要上傳的檔案");
        }
        
        try {
            // 建立上傳目錄
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 產生唯一檔案名稱
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);
            
            // 儲存檔案
            file.transferTo(filePath.toFile());
            
            log.info("檔案上傳成功: {}", fileName);
            return ResponseEntity.ok("檔案上傳成功: " + fileName);
            
        } catch (IOException e) {
            log.error("檔案上傳失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("檔案上傳失敗: " + e.getMessage());
        }
    }
    
    /**
     * 多檔案上傳
     * @param files 上傳的檔案陣列
     * @return 上傳結果
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<List<String>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files) {
        
        List<String> uploadedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(uploadPath).resolve(fileName);
                    file.transferTo(filePath.toFile());
                    uploadedFiles.add(fileName);
                    
                } catch (IOException e) {
                    log.error("檔案上傳失敗: {}", file.getOriginalFilename(), e);
                }
            }
        }
        
        return ResponseEntity.ok(uploadedFiles);
    }
    
    /**
     * 檔案下載
     * @param fileName 檔案名稱
     * @return 檔案內容
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        
        try {
            Path filePath = Paths.get(uploadPath).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                               "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (MalformedURLException e) {
            log.error("檔案下載失敗: {}", fileName, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 檔案預覽（適用於圖片等媒體檔案）
     * @param fileName 檔案名稱
     * @return 檔案內容
     */
    @GetMapping("/preview/{fileName}")
    public ResponseEntity<Resource> previewFile(@PathVariable String fileName) {
        
        try {
            Path filePath = Paths.get(uploadPath).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // 根據檔案副檔名設定 Content-Type
                String contentType = Files.probeContentType(filePath);
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("檔案預覽失敗: {}", fileName, e);
            return ResponseEntity.badRequest().build();
        }
    }
}