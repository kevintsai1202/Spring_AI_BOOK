package com.example.advancedrag.controller;

import com.example.advancedrag.dto.AdvancedRAGRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * RAGController REST API 測試
 *
 * 測試所有 REST API 端點：
 * 1. POST /api/v1/rag/query - RAG 查詢
 * 2. GET /api/v1/rag/session/{sessionId} - 會話歷史
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RAG Controller API 測試")
class RAGControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AdvancedRAGRequest baseRequest;

    @BeforeEach
    void setUp() {
        baseRequest = AdvancedRAGRequest.builder()
                .query("什麼是 Spring AI？")
                .sessionId("test-session-api")
                .enableQueryRewrite(false)
                .enableQueryExpansion(false)
                .enableModeration(false)
                .returnDocuments(false)
                .returnScoringDetails(false)
                .build();
    }

    @Test
    @DisplayName("API測試1：基本 RAG 查詢請求")
    void testBasicRAGQueryAPI() throws Exception {
        // Given
        String requestJson = objectMapper.writeValueAsString(baseRequest);

        // When & Then
        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("RAG 查詢完成"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.queryId").exists())
                .andExpect(jsonPath("$.data.originalQuery").value("什麼是 Spring AI？"))
                .andExpect(jsonPath("$.data.answer").exists())
                .andExpect(jsonPath("$.data.processingTimeMs").isNumber())
                .andDo(result -> {
                    System.out.println("\n=== API測試1：基本 RAG 查詢請求 ===");
                    System.out.println("狀態碼: " + result.getResponse().getStatus());
                    System.out.println("響應: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    @DisplayName("API測試2：查詢重寫請求")
    void testQueryRewriteAPI() throws Exception {
        // Given
        AdvancedRAGRequest request = baseRequest.toBuilder()
                .enableQueryRewrite(true)
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rewrittenQuery").exists())
                .andDo(result -> {
                    System.out.println("\n=== API測試2：查詢重寫請求 ===");
                    System.out.println("響應: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    @DisplayName("API測試3：返回文檔請求")
    void testReturnDocumentsAPI() throws Exception {
        // Given
        AdvancedRAGRequest request = baseRequest.toBuilder()
                .returnDocuments(true)
                .returnScoringDetails(true)
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documents").isArray())
                .andExpect(jsonPath("$.data.documents").isNotEmpty())
                .andDo(result -> {
                    System.out.println("\n=== API測試3：返回文檔請求 ===");
                    System.out.println("響應: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    @DisplayName("API測試4：查詢驗證 - 缺少必填欄位")
    void testQueryValidationMissingField() throws Exception {
        // Given - 缺少 query 欄位
        String invalidJson = "{\"sessionId\":\"test-session\"}";

        // When & Then
        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andDo(result -> {
                    System.out.println("\n=== API測試4：查詢驗證 - 缺少必填欄位 ===");
                    System.out.println("狀態碼: " + result.getResponse().getStatus());
                    System.out.println("響應: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    @DisplayName("API測試5：查詢驗證 - 空查詢")
    void testQueryValidationEmptyQuery() throws Exception {
        // Given
        AdvancedRAGRequest request = baseRequest.toBuilder()
                .query("")
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andDo(result -> {
                    System.out.println("\n=== API測試5：查詢驗證 - 空查詢 ===");
                    System.out.println("狀態碼: " + result.getResponse().getStatus());
                    System.out.println("響應: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    @DisplayName("API測試6：內容類型驗證 - 錯誤的 Content-Type")
    void testContentTypeValidation() throws Exception {
        // Given
        String requestJson = objectMapper.writeValueAsString(baseRequest);

        // When & Then
        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestJson))
                .andExpect(status().isUnsupportedMediaType())
                .andDo(result -> {
                    System.out.println("\n=== API測試6：內容類型驗證 ===");
                    System.out.println("狀態碼: " + result.getResponse().getStatus());
                });
    }

    @Test
    @DisplayName("API測試7：會話歷史查詢")
    void testGetSessionHistory() throws Exception {
        // Given - 先執行一次查詢建立會話
        String requestJson = objectMapper.writeValueAsString(baseRequest);
        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        // When & Then - 查詢會話歷史
        mockMvc.perform(get("/api/v1/rag/session/test-session-api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(result -> {
                    System.out.println("\n=== API測試7：會話歷史查詢 ===");
                    System.out.println("響應: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    @DisplayName("API測試8：批次查詢性能測試")
    void testBatchQueryPerformance() throws Exception {
        // Given
        int queryCount = 3;
        long startTime = System.currentTimeMillis();

        // When - 執行多次查詢
        for (int i = 0; i < queryCount; i++) {
            AdvancedRAGRequest request = baseRequest.toBuilder()
                    .query("測試查詢 #" + (i + 1))
                    .sessionId("perf-test-session")
                    .build();
            String requestJson = objectMapper.writeValueAsString(request);

            mockMvc.perform(post("/api/v1/rag/query")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk());
        }

        // Then
        long totalTime = System.currentTimeMillis() - startTime;
        double avgTime = (double) totalTime / queryCount;

        System.out.println("\n=== API測試8：批次查詢性能測試 ===");
        System.out.println("查詢數量: " + queryCount);
        System.out.println("總耗時: " + totalTime + "ms");
        System.out.println("平均耗時: " + String.format("%.2f", avgTime) + "ms");
    }

    @Test
    @DisplayName("API測試9：完整請求參數測試")
    void testFullRequestParameters() throws Exception {
        // Given
        AdvancedRAGRequest request = AdvancedRAGRequest.builder()
                .query("Spring AI 的核心功能")
                .sessionId("full-param-session")
                .enableQueryRewrite(true)
                .enableQueryExpansion(true)
                .enableModeration(true)
                .returnDocuments(true)
                .returnScoringDetails(true)
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        System.out.println("\n=== API測試9：完整請求參數測試 ===");
        System.out.println("請求 JSON: " + requestJson);

        // When & Then
        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.queryId").exists())
                .andExpect(jsonPath("$.data.answer").exists())
                .andExpect(jsonPath("$.data.rewrittenQuery").exists())
                .andExpect(jsonPath("$.data.expandedQueries").isArray())
                .andExpect(jsonPath("$.data.documents").isArray())
                .andDo(result -> {
                    System.out.println("響應狀態: " + result.getResponse().getStatus());
                    System.out.println("響應內容: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    @DisplayName("API測試10：錯誤處理測試")
    void testErrorHandling() throws Exception {
        // Given - 無效的 JSON
        String invalidJson = "{invalid json}";

        // When & Then
        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andDo(result -> {
                    System.out.println("\n=== API測試10：錯誤處理測試 ===");
                    System.out.println("狀態碼: " + result.getResponse().getStatus());
                    System.out.println("錯誤訊息: " + result.getResponse().getContentAsString());
                });
    }
}
