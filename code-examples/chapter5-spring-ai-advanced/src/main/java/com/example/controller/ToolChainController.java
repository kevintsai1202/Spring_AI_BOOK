package com.example.controller;

import com.example.dto.ProductAnalysisRequest;
import com.example.dto.ProductAnalysisResponse;
import com.example.tools.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Tool 链调用控制器
 * 实现复杂的工具链调用功能（5.8 章节）
 */
@RestController
@RequestMapping("/api/tool-chain")
@RequiredArgsConstructor
@Slf4j
public class ToolChainController {

    private final ChatModel chatModel;
    private final ProductDetailsTools productDetailsTools;
    private final EnhancedSalesTools enhancedSalesTools;
    private final DateTimeTools dateTimeTools;
    private final CalculatorTools calculatorTools;

    /**
     * 复杂工具链查询
     * @param prompt 自然语言查询
     * @return AI 分析结果
     */
    @GetMapping("/complex-query")
    public String complexQuery(@RequestParam String prompt) {
        try {
            log.info("收到复杂工具链查询：{}", prompt);

            long startTime = System.currentTimeMillis();

            // 创建支持工具链的 ChatClient
            ChatClient toolChainClient = ChatClient.create(chatModel);

            String response = toolChainClient
                    .prompt(prompt)
                    .call()
                    .content();

            long endTime = System.currentTimeMillis();

            log.info("复杂查询完成，耗时：{}ms，响应长度：{} 字符",
                    endTime - startTime, response.length());

            return response;

        } catch (Exception e) {
            log.error("复杂工具链查询失败：{}", prompt, e);
            return "查询失败：" + e.getMessage();
        }
    }

    /**
     * 产品深度分析
     * @param request 分析请求
     * @return 详细分析报告
     */
    @PostMapping("/product-analysis")
    public ProductAnalysisResponse productAnalysis(@RequestBody ProductAnalysisRequest request) {
        try {
            log.info("产品深度分析请求：{}", request);

            long startTime = System.currentTimeMillis();

            // 构建分析提示词
            String prompt = buildAnalysisPrompt(request);

            // 创建支持工具链的 ChatClient
            ChatClient analysisClient = ChatClient.create(chatModel);

            String analysis = analysisClient
                    .prompt(prompt)
                    .call()
                    .content();

            long endTime = System.currentTimeMillis();

            return ProductAnalysisResponse.builder()
                    .success(true)
                    .productCode(request.getProductCode())
                    .analysisType(request.getAnalysisType())
                    .analysis(analysis)
                    .executionTime(endTime - startTime)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("产品深度分析失败", e);

            return ProductAnalysisResponse.builder()
                    .success(false)
                    .productCode(request.getProductCode())
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 工具链执行追踪
     * @param prompt 查询内容
     * @return 执行追踪信息
     */
    @GetMapping("/trace")
    public Map<String, Object> traceExecution(@RequestParam String prompt) {
        try {
            log.info("工具链执行追踪：{}", prompt);

            long startTime = System.currentTimeMillis();

            // 创建支持工具链的 ChatClient
            ChatClient traceClient = ChatClient.create(chatModel);

            // 这里可以添加工具调用追踪逻辑
            String response = traceClient
                    .prompt(prompt)
                    .call()
                    .content();

            long endTime = System.currentTimeMillis();

            return Map.of(
                "prompt", prompt,
                "response", response,
                "executionTime", endTime - startTime,
                "toolsUsed", List.of("EnhancedSalesTools", "ProductDetailsTools"),
                "timestamp", LocalDateTime.now()
            );

        } catch (Exception e) {
            log.error("工具链执行追踪失败", e);
            return Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            );
        }
    }

    /**
     * 销售排行分析
     * 场景 1：查询最热销产品及其详细信息
     */
    @GetMapping("/best-seller-analysis")
    public String bestSellerAnalysis(@RequestParam(defaultValue = "2024") int year) {
        try {
            String prompt = String.format(
                "请分析 %d 年最热销的产品，并列出该产品所有型号的详细规格和价格。" +
                "请提供完整的产品分析报告，包括销售数据、产品线覆盖和商业建议。",
                year
            );

            log.info("最热销产品分析：{}", year);
            return complexQuery(prompt);

        } catch (Exception e) {
            log.error("最热销产品分析失败", e);
            return "分析失败：" + e.getMessage();
        }
    }

    /**
     * 竞品比较分析
     * 场景 2：比较多个产品的销售表现
     */
    @GetMapping("/competitor-analysis")
    public String competitorAnalysis(
            @RequestParam(defaultValue = "2024") int year,
            @RequestParam(defaultValue = "PD-1405,PD-1234") String products) {
        try {
            String productList = products.replace(",", "、");
            String prompt = String.format(
                "请比较 %d 年 %s 这些产品的销售表现，并分析各自的产品优势。" +
                "请提供详细的对比分析、市场占有率评估和竞争优势分析。",
                year, productList
            );

            log.info("竞品分析：{}年，产品：{}", year, products);
            return complexQuery(prompt);

        } catch (Exception e) {
            log.error("竞品分析失败", e);
            return "分析失败：" + e.getMessage();
        }
    }

    /**
     * 产品线优化建议
     * 查询产品详情并提供优化建议
     */
    @GetMapping("/product-optimization")
    public String productOptimization(@RequestParam String productCode) {
        try {
            String prompt = String.format(
                "请详细分析产品代码 %s 的产品线。" +
                "1. 列出所有型号及规格\n" +
                "2. 分析价格分层\n" +
                "3. 评估产品线完整性\n" +
                "4. 提供优化建议",
                productCode
            );

            log.info("产品线优化分析：{}", productCode);
            return complexQuery(prompt);

        } catch (Exception e) {
            log.error("产品线优化分析失败", e);
            return "分析失败：" + e.getMessage();
        }
    }

    /**
     * 市场份额分析
     */
    @GetMapping("/market-share-analysis")
    public String marketShareAnalysis(@RequestParam(defaultValue = "2024") int year) {
        try {
            String prompt = String.format(
                "请进行 %d 年的市场份额分析。\n" +
                "1. 列出各产品的销售量和营收\n" +
                "2. 计算市场占有率\n" +
                "3. 分析市场分布\n" +
                "4. 提供市场趋势见解",
                year
            );

            log.info("市场份额分析：{}", year);
            return complexQuery(prompt);

        } catch (Exception e) {
            log.error("市场份额分析失败", e);
            return "分析失败：" + e.getMessage();
        }
    }

    private String buildAnalysisPrompt(ProductAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("请进行深度产品分析：\n");
        prompt.append("产品代码：").append(request.getProductCode()).append("\n");
        prompt.append("分析类型：").append(request.getAnalysisType()).append("\n");

        if (request.getYear() != null) {
            prompt.append("分析年份：").append(request.getYear()).append("\n");
        }

        prompt.append("\n请提供：\n");

        switch (request.getAnalysisType()) {
            case "comprehensive" -> {
                prompt.append("1. 产品销售表现分析\n");
                prompt.append("2. 所有产品型号详细信息\n");
                prompt.append("3. 市场竞争力评估\n");
                prompt.append("4. 改进建议和策略\n");
            }
            case "sales" -> {
                prompt.append("1. 详细销售数据分析\n");
                prompt.append("2. 销售趋势和模式\n");
                prompt.append("3. 市场占有率分析\n");
            }
            case "product" -> {
                prompt.append("1. 产品型号完整信息\n");
                prompt.append("2. 规格和定价分析\n");
                prompt.append("3. 产品线优化建议\n");
            }
            default -> prompt.append("1. 全面产品分析\n");
        }

        return prompt.toString();
    }
}
