/**
 * Tool Calling 控制器
 * 展示 Spring AI 的工具調用功能
 */
package com.example.controller;

import com.example.tools.DateTimeTools;
import com.example.tools.CalculatorTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ToolCallingController {

    private final ChatModel chatModel;
    private final DateTimeTools dateTimeTools;
    private final CalculatorTools calculatorTools;

    // ============ V1 API 端點 ============

    /**
     * V1 版本：獲取當前時間
     * GET /api/v1/tools/current-time
     */
    @GetMapping("/v1/tools/current-time")
    public ResponseEntity<String> getToolCurrentTime(
            @RequestParam(defaultValue = "yyyy-MM-dd HH:mm:ss") String format) {
        log.info("查詢當前時間: format={}", format);
        try {
            String result = dateTimeTools.getCurrentTimeWithFormat(format);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("時間查詢失敗", e);
            return ResponseEntity.internalServerError()
                    .body("時間查詢失敗：" + e.getMessage());
        }
    }

    /**
     * V1 版本：計算工具
     * GET /api/v1/tools/calculate?expression=10+5
     *
     * 支持的運算符：+ - * /
     */
    @GetMapping("/v1/tools/calculate")
    public ResponseEntity<String> calculate(@RequestParam String expression) {
        log.info("執行計算: {}", expression);
        try {
            // 簡單的表達式計算
            String result = evaluateExpression(expression);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("計算失敗", e);
            return ResponseEntity.badRequest()
                    .body("計算失敗：" + e.getMessage());
        }
    }

    /**
     * 評估數學表達式
     */
    private String evaluateExpression(String expression) {
        try {
            // 移除空格
            expression = expression.replaceAll("\\s", "");

            // 簡單的遞歸下降解析器
            double result = parseExpression(expression);
            return "計算結果: " + expression + " = " + result;
        } catch (Exception e) {
            throw new RuntimeException("無效的表達式: " + expression);
        }
    }

    private double parseExpression(String expr) {
        String[] addTerms = expr.split("(?=\\+|-(?!\\d))");
        double result = parseTerm(addTerms[0]);

        for (int i = 1; i < addTerms.length; i++) {
            String term = addTerms[i];
            if (term.startsWith("+")) {
                result += parseTerm(term.substring(1));
            } else if (term.startsWith("-")) {
                result -= parseTerm(term.substring(1));
            }
        }
        return result;
    }

    private double parseTerm(String expr) {
        String[] factors = expr.split("(?=[*/])");
        double result = Double.parseDouble(factors[0].trim());

        for (int i = 1; i < factors.length; i++) {
            String factor = factors[i].trim();
            if (factor.startsWith("*")) {
                result *= Double.parseDouble(factor.substring(1).trim());
            } else if (factor.startsWith("/")) {
                result /= Double.parseDouble(factor.substring(1).trim());
            }
        }
        return result;
    }

    // ============ 原有 API 端點 (保留向後相容) ============

    /**
     * 基礎 Tool Calling 示例 (原路由)
     * @param prompt 使用者提示詞
     * @return AI 回應
     */
    @GetMapping("/tool-calling/basic")
    public String basicToolCalling(@RequestParam String prompt) {
        try {
            log.info("Tool Calling 請求：{}", prompt);

            // 使用 ChatClient 進行 AI 對話
            String response = ChatClient.create(chatModel)
                    .prompt(prompt)
                    .call()
                    .content();

            log.info("Tool Calling 回應：{}", response);
            return response;

        } catch (Exception e) {
            log.error("執行失敗", e);
            return "執行失敗：" + e.getMessage();
        }
    }

    /**
     * 時間查詢示例 (原路由)
     * @param format 時間格式
     * @return 當前時間
     */
    @GetMapping("/tool-calling/current-time")
    public String getCurrentTime(@RequestParam(defaultValue = "yyyy-MM-dd HH:mm:ss") String format) {
        try {
            return dateTimeTools.getCurrentTimeWithFormat(format);
        } catch (Exception e) {
            log.error("時間查詢失敗", e);
            return "時間查詢失敗：" + e.getMessage();
        }
    }
}
