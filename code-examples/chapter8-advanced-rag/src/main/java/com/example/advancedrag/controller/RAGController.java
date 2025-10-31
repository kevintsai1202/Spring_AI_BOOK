package com.example.advancedrag.controller;

import com.example.advancedrag.dto.AdvancedRAGRequest;
import com.example.advancedrag.dto.AdvancedRAGResponse;
import com.example.advancedrag.dto.ApiResponse;
import com.example.advancedrag.service.AdvancedRAGService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Advanced RAG REST API 控制器
 *
 * 提供 RAG 查詢的 HTTP 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RAGController {

    private final AdvancedRAGService ragService;

    /**
     * Advanced RAG 查詢
     *
     * @param request RAG 查詢請求
     * @return RAG 查詢響應
     */
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<AdvancedRAGResponse>> query(
            @Valid @RequestBody AdvancedRAGRequest request) {

        try {
            log.info("收到 RAG 查詢請求：{}", request.getQuery());

            AdvancedRAGResponse response = ragService.query(request);

            return ResponseEntity.ok(
                    ApiResponse.success("查詢成功", response)
                            .withTraceId(response.getQueryId())
            );

        } catch (Exception e) {
            log.error("RAG 查詢失敗", e);
            return ResponseEntity.status(500).body(
                    ApiResponse.error(500, "查詢失敗：" + e.getMessage())
            );
        }
    }

    /**
     * 簡化版 RAG 查詢（僅接受查詢字符串）
     *
     * @param query 查詢字符串
     * @return RAG 查詢響應
     */
    @GetMapping("/query")
    public ResponseEntity<ApiResponse<AdvancedRAGResponse>> querySimple(
            @RequestParam String query) {

        try {
            log.info("收到簡化 RAG 查詢請求：{}", query);

            AdvancedRAGRequest request = AdvancedRAGRequest.builder()
                    .query(query)
                    .build();

            AdvancedRAGResponse response = ragService.query(request);

            return ResponseEntity.ok(
                    ApiResponse.success("查詢成功", response)
                            .withTraceId(response.getQueryId())
            );

        } catch (Exception e) {
            log.error("RAG 查詢失敗", e);
            return ResponseEntity.status(500).body(
                    ApiResponse.error(500, "查詢失敗：" + e.getMessage())
            );
        }
    }
}
