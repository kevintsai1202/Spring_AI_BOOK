/**
 * 計算器工具類別
 * 提供基本數學運算功能，解決 AI 數學運算不準確的問題
 */
package com.example.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTools {
    
    /**
     * 基本數學運算
     * @param operation 運算類型（add, subtract, multiply, divide）
     * @param a 第一個數字
     * @param b 第二個數字
     * @return 運算結果
     */
    @Tool(description = "Perform basic mathematical operations: add, subtract, multiply, divide")
    public String calculate(String operation, double a, double b) {
        try {
            double result = switch (operation.toLowerCase()) {
                case "add" -> a + b;
                case "subtract" -> a - b;
                case "multiply" -> a * b;
                case "divide" -> {
                    if (b == 0) {
                        yield Double.NaN;
                    }
                    yield a / b;
                }
                default -> throw new IllegalArgumentException("不支援的運算：" + operation);
            };
            
            if (Double.isNaN(result)) {
                return "錯誤：除數不能為零";
            }
            
            return String.format("%.2f %s %.2f = %.2f", a, getOperationSymbol(operation), b, result);
            
        } catch (Exception e) {
            return "計算錯誤：" + e.getMessage();
        }
    }
    
    /**
     * 複雜數學運算
     * @param expression 數學表達式（如："2 + 3 * 4"）
     * @return 計算結果
     */
    @Tool(description = "Evaluate complex mathematical expressions")
    public String evaluateExpression(String expression) {
        try {
            // 這裡可以整合數學表達式解析器
            // 為了簡化，這裡只做基本示例
            return "表達式 '" + expression + "' 的計算功能正在開發中";
        } catch (Exception e) {
            return "表達式計算錯誤：" + e.getMessage();
        }
    }
    
    private String getOperationSymbol(String operation) {
        return switch (operation.toLowerCase()) {
            case "add" -> "+";
            case "subtract" -> "-";
            case "multiply" -> "×";
            case "divide" -> "÷";
            default -> "?";
        };
    }
}