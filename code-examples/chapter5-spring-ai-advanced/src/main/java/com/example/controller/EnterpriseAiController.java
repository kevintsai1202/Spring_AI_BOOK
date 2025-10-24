package com.example.controller;

import com.example.dto.SalesAnalysisRequest;
import com.example.dto.SalesAnalysisResponse;
import com.example.model.Product;
import com.example.service.EnterpriseDataService;
import com.example.tools.ProductSalesTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

/**
 * 企業 AI 控制器 - 提供企業數據工具的 REST API
 * 支持 Tool Calling 和自然語言查詢
 * 用於 5.7 企業數據工具章節
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/enterprise")
@RequiredArgsConstructor
public class EnterpriseAiController {

    private final EnterpriseDataService enterpriseDataService;
    private final ProductSalesTools productSalesTools;
    private final ChatModel chatModel;

    /**
     * 獲取所有產品
     * GET /api/v1/enterprise/products
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("查詢所有產品");
        List<Product> products = enterpriseDataService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * 根據 ID 查詢產品詳情
     * GET /api/v1/enterprise/products/{productId}
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable String productId) {
        log.info("查詢產品詳情: {}", productId);

        var product = enterpriseDataService.getProductById(productId);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        }
        return ResponseEntity.notFound()
                .header("X-Error", "Product not found: " + productId)
                .build();
    }

    /**
     * 按分類查詢產品
     * GET /api/v1/enterprise/products?category=電子產品
     */
    @GetMapping("/products-by-category")
    public ResponseEntity<List<Product>> getProductsByCategory(@RequestParam String category) {
        log.info("按分類查詢產品: {}", category);
        List<Product> products = enterpriseDataService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    /**
     * 執行銷售分析
     * POST /api/v1/enterprise/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<SalesAnalysisResponse> analyzeSales(
            @RequestBody SalesAnalysisRequest request) {
        log.info("執行銷售分析");

        try {
            SalesAnalysisResponse response = enterpriseDataService.analyzeSales(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("無效的分析請求: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 自然語言查詢銷售數據 (使用 Tool Calling)
     * POST /api/v1/enterprise/natural-query
     *
     * 示例:
     * {
     *   "query": "最近6個月的銷售趨勢如何？"
     * }
     */
    @PostMapping("/natural-query")
    public ResponseEntity<String> naturalLanguageQuery(@RequestBody Map<String, String> request) {
        log.info("自然語言查詢: {}", request.get("query"));

        String query = request.get("query");

        try {
            // 使用 ChatClient 和 Tool Calling 執行查詢
            String response = ChatClient.create(chatModel)
                    .prompt()
                    .user(query)
                    .call()
                    .content();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查詢執行失敗", e);
            return ResponseEntity.internalServerError()
                    .body("查詢執行失敗: " + e.getMessage());
        }
    }

    /**
     * 查詢特定月份的銷售排名
     * GET /api/v1/enterprise/sales-ranking/{month}
     *
     * 示例: /api/v1/enterprise/sales-ranking/2024-10
     */
    @GetMapping("/sales-ranking/{month}")
    public ResponseEntity<String> getSalesRanking(@PathVariable String month) {
        log.info("查詢銷售排名: {}", month);

        try {
            String result = productSalesTools.getSalesRankingByMonth(month);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return ResponseEntity.badRequest()
                    .body("無效的月份格式，應為 yyyy-MM");
        }
    }

    /**
     * 查詢年度增長率
     * GET /api/v1/enterprise/yearly-growth/{year}
     *
     * 示例: /api/v1/enterprise/yearly-growth/2024
     */
    @GetMapping("/yearly-growth/{year}")
    public ResponseEntity<String> getYearlyGrowth(@PathVariable String year) {
        log.info("查詢年度增長: {}", year);

        try {
            String result = productSalesTools.getYearlyGrowthRate(year);
            return ResponseEntity.ok(result);
        } catch (NumberFormatException e) {
            log.error("無效的年份", e);
            return ResponseEntity.badRequest()
                    .body("無效的年份格式");
        }
    }

    /**
     * 獲取銷售預測
     * GET /api/v1/enterprise/forecast?months=3
     */
    @GetMapping("/forecast")
    public ResponseEntity<String> getForecast(@RequestParam(defaultValue = "3") int months) {
        log.info("獲取銷售預測: {} 個月", months);

        try {
            String result = productSalesTools.getForecast(months);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("預測獲取失敗", e);
            return ResponseEntity.badRequest()
                    .body("預測獲取失敗: " + e.getMessage());
        }
    }

    /**
     * 比較產品銷售表現
     * GET /api/v1/enterprise/compare-products?ids=PROD001,PROD002
     */
    @GetMapping("/compare-products")
    public ResponseEntity<String> compareProducts(@RequestParam String ids) {
        log.info("比較產品表現: {}", ids);

        try {
            String result = productSalesTools.compareProductPerformance(ids);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("比較失敗", e);
            return ResponseEntity.badRequest()
                    .body("比較失敗: " + e.getMessage());
        }
    }

    /**
     * 分析銷售趨勢
     * GET /api/v1/enterprise/trend-analysis?category=電子產品&months=6
     */
    @GetMapping("/trend-analysis")
    public ResponseEntity<String> analyzeTrend(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "6") int months) {
        log.info("分析銷售趨勢: category={}, months={}", category, months);

        try {
            // 如果 category 為空，使用默認分類
            String analysisCategory = category != null && !category.isEmpty() ? category : "電子產品";
            String result = productSalesTools.analyzeTrend(analysisCategory, months);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("分析失敗", e);
            return ResponseEntity.badRequest()
                    .body("分析失敗: " + e.getMessage());
        }
    }

    /**
     * 自然語言銷售分析 (高級功能)
     * POST /api/v1/enterprise/ai-analysis
     *
     * 示例:
     * {
     *   "question": "哪個產品在過去6個月中增長最快？",
     *   "analysisType": "TREND"
     * }
     */
    @PostMapping("/ai-analysis")
    public ResponseEntity<String> aiAnalysis(@RequestBody Map<String, String> request) {
        log.info("AI 銷售分析");

        String question = request.get("question");
        String analysisType = request.get("analysisType");

        try {
            // 構建系統提示詞
            String systemPrompt = String.format(
                    "你是一個專業的銷售分析顧問。用戶將提出關於銷售數據的問題。" +
                    "使用可用的企業工具來分析銷售數據，並提供專業的見解和建議。" +
                    "分析類型: %s",
                    analysisType != null ? analysisType : "TREND"
            );

            // 使用 ChatClient 執行查詢
            String response = ChatClient.create(chatModel)
                    .prompt()
                    .system(systemPrompt)
                    .user(question)
                    .call()
                    .content();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("AI 分析失敗", e);
            return ResponseEntity.internalServerError()
                    .body("AI 分析失敗: " + e.getMessage());
        }
    }
}
