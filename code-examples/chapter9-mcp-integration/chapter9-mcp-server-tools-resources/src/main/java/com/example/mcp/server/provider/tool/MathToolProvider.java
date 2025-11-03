package com.example.mcp.server.provider.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 數學工具提供者
 * 提供數學運算工具
 */
@Service
@Slf4j
public class MathToolProvider {

    /**
     * 加法運算 - 計算多個數字的總和
     *
     * @param numbers 要相加的數字列表
     * @return 總和
     */
    @Tool(description = "Calculate the sum of multiple numbers")
    public double sum(double... numbers) {
        log.info("執行加法運算: {}", Arrays.toString(numbers));

        if (numbers == null || numbers.length == 0) {
            log.warn("加法運算: 未提供數字");
            return 0.0;
        }

        double result = Arrays.stream(numbers).sum();
        log.info("加法結果: {} = {}", Arrays.toString(numbers), result);

        return result;
    }

    /**
     * 乘法運算 - 計算多個數字的乘積
     *
     * @param numbers 要相乘的數字列表
     * @return 乘積
     */
    @Tool(description = "Calculate the product of multiple numbers")
    public double multiply(double... numbers) {
        log.info("執行乘法運算: {}", Arrays.toString(numbers));

        if (numbers == null || numbers.length == 0) {
            log.warn("乘法運算: 未提供數字");
            return 0.0;
        }

        double result = Arrays.stream(numbers).reduce(1.0, (a, b) -> a * b);
        log.info("乘法結果: {} = {}", Arrays.toString(numbers), result);

        return result;
    }

    /**
     * 除法運算 - 計算兩個數字的商
     *
     * @param dividend 被除數
     * @param divisor  除數
     * @return 商
     * @throws ArithmeticException 當除數為零時拋出異常
     */
    @Tool(description = "Divide two numbers (dividend / divisor)")
    public double divide(double dividend, double divisor) {

        log.info("執行除法運算: {} ÷ {}", dividend, divisor);

        if (divisor == 0) {
            log.error("除法運算錯誤: 除數不能為零");
            throw new ArithmeticException("除數不能為零");
        }

        double result = dividend / divisor;
        log.info("除法結果: {} ÷ {} = {}", dividend, divisor, result);

        return result;
    }

    /**
     * 取平方根
     *
     * @param number 要計算平方根的數字
     * @return 平方根
     * @throws ArithmeticException 當數字為負數時拋出異常
     */
    @Tool(description = "Calculate the square root of a number")
    public double sqrt(double number) {
        log.info("執行平方根運算: √{}", number);

        if (number < 0) {
            log.error("平方根運算錯誤: 數字不能為負數");
            throw new ArithmeticException("無法計算負數的平方根");
        }

        double result = Math.sqrt(number);
        log.info("平方根結果: √{} = {}", number, result);

        return result;
    }
}
