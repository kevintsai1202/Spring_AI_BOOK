package com.example.rag.basic.controller;

import com.example.rag.basic.model.RAGQueryRequest;
import com.example.rag.basic.model.RAGQueryResponse;
import com.example.rag.basic.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 系統 REST API Controller
 *
 * 提供的端點:
 * - POST /api/rag/query - RAG 查詢
 * - POST /api/rag/documents - 上傳文檔
 * - DELETE /api/rag/documents - 刪除文檔
 * - GET /api/rag/health - 健康檢查
 */
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
@Slf4j
public class RAGController {

    private final RAGService ragService;

    /**
     * RAG 查詢端點
     *
     * @param request 查詢請求
     * @return RAG 查詢響應
     */
    @PostMapping("/query")
    public ResponseEntity<RAGQueryResponse> query(@RequestBody RAGQueryRequest request) {
        log.info("Received RAG query: {}", request.getQuestion());

        try {
            RAGQueryResponse response = ragService.query(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("RAG query failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 帶過濾條件的 RAG 查詢
     *
     * @param request 查詢請求（包含過濾條件）
     * @return RAG 查詢響應
     */
    @PostMapping("/query-with-filter")
    public ResponseEntity<RAGQueryResponse> queryWithFilter(@RequestBody RAGQueryRequest request) {
        log.info("Received RAG query with filters: {}", request.getQuestion());

        try {
            RAGQueryResponse response = ragService.queryWithFilter(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("RAG query with filter failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 上傳文檔端點
     *
     * @param files 上傳的文件列表
     * @return 處理結果
     */
    @PostMapping("/documents")
    public ResponseEntity<Map<String, Object>> uploadDocuments(
            @RequestParam("files") List<MultipartFile> files) {

        log.info("Received {} documents for upload", files.size());

        try {
            // 轉換為 Resource 列表
            List<Resource> resources = files.stream()
                    .map(this::convertToResource)
                    .toList();

            // 添加到知識庫
            ragService.addDocuments(resources);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "成功添加 " + files.size() + " 個文檔到知識庫");
            response.put("documentsProcessed", files.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to upload documents", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "文檔上傳失敗: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 刪除文檔端點
     *
     * @param documentIds 要刪除的文檔 ID 列表
     * @return 處理結果
     */
    @DeleteMapping("/documents")
    public ResponseEntity<Map<String, Object>> deleteDocuments(
            @RequestBody List<String> documentIds) {

        log.info("Deleting {} documents", documentIds.size());

        try {
            ragService.deleteDocuments(documentIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "成功刪除 " + documentIds.size() + " 個文檔");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to delete documents", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "文檔刪除失敗: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 健康檢查端點
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "RAG Basic System");
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    /**
     * 轉換 MultipartFile 為 Resource
     */
    private Resource convertToResource(MultipartFile file) {
        return new org.springframework.core.io.ByteArrayResource(getBytes(file)) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }

    /**
     * 取得文件字節
     */
    private byte[] getBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file bytes", e);
        }
    }

}
