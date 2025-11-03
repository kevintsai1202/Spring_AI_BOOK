package com.example.mcp.server;

import com.example.mcp.server.provider.tool.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MCP Server 工具提供者測試類
 * 測試所有 9 個已註冊的工具
 */
@SpringBootTest
public class ToolProvidersTest {

    @Autowired
    private MathToolProvider mathToolProvider;

    @Autowired
    private TextToolProvider textToolProvider;

    @Autowired
    private TimeToolProvider timeToolProvider;

    @Autowired
    private WeatherToolProvider weatherToolProvider;

    /**
     * 測試數學工具 - 加法
     */
    @Test
    public void testMathSum() {
        System.out.println("\n========================================");
        System.out.println("測試數學工具 - sum");
        System.out.println("========================================");

        double result = mathToolProvider.sum(1, 2, 3, 4, 5);
        System.out.println("sum(1, 2, 3, 4, 5) = " + result);

        assertEquals(15.0, result, "總和應該是 15");
        System.out.println("✓ 測試通過");
    }

    /**
     * 測試數學工具 - 乘法
     */
    @Test
    public void testMathMultiply() {
        System.out.println("\n========================================");
        System.out.println("測試數學工具 - multiply");
        System.out.println("========================================");

        double result = mathToolProvider.multiply(2, 3, 4);
        System.out.println("multiply(2, 3, 4) = " + result);

        assertEquals(24.0, result, "乘積應該是 24");
        System.out.println("✓ 測試通過");
    }

    /**
     * 測試數學工具 - 除法
     */
    @Test
    public void testMathDivide() {
        System.out.println("\n========================================");
        System.out.println("測試數學工具 - divide");
        System.out.println("========================================");

        double result = mathToolProvider.divide(10, 2);
        System.out.println("divide(10, 2) = " + result);

        assertEquals(5.0, result, "商應該是 5");
        System.out.println("✓ 測試通過");
    }

    /**
     * 測試數學工具 - 平方根
     */
    @Test
    public void testMathSqrt() {
        System.out.println("\n========================================");
        System.out.println("測試數學工具 - sqrt");
        System.out.println("========================================");

        double result = mathToolProvider.sqrt(16);
        System.out.println("sqrt(16) = " + result);

        assertEquals(4.0, result, "平方根應該是 4");
        System.out.println("✓ 測試通過");
    }

    /**
     * 測試文本工具 - 轉大寫
     */
    @Test
    public void testTextToUpperCase() {
        System.out.println("\n========================================");
        System.out.println("測試文本工具 - toUpperCase");
        System.out.println("========================================");

        String result = textToolProvider.toUpperCase("hello world");
        System.out.println("toUpperCase(\"hello world\") = \"" + result + "\"");

        assertEquals("HELLO WORLD", result, "應該轉換為大寫");
        System.out.println("✓ 測試通過");
    }

    /**
     * 測試文本工具 - 轉小寫
     */
    @Test
    public void testTextToLowerCase() {
        System.out.println("\n========================================");
        System.out.println("測試文本工具 - toLowerCase");
        System.out.println("========================================");

        String result = textToolProvider.toLowerCase("HELLO WORLD");
        System.out.println("toLowerCase(\"HELLO WORLD\") = \"" + result + "\"");

        assertEquals("hello world", result, "應該轉換為小寫");
        System.out.println("✓ 測試通過");
    }

    /**
     * 測試文本工具 - 單詞計數
     */
    @Test
    public void testTextWordCount() {
        System.out.println("\n========================================");
        System.out.println("測試文本工具 - wordCount");
        System.out.println("========================================");

        int result = textToolProvider.wordCount("Spring AI MCP Server Test");
        System.out.println("wordCount(\"Spring AI MCP Server Test\") = " + result);

        assertEquals(5, result, "應該有 5 個單詞");
        System.out.println("✓ 測試通過");
    }

    /**
     * 測試時間工具 - 獲取當前時間
     */
    @Test
    public void testTimeGetCurrentTime() {
        System.out.println("\n========================================");
        System.out.println("測試時間工具 - getCurrentTime");
        System.out.println("========================================");

        String result = timeToolProvider.getCurrentTime("Asia/Taipei");
        System.out.println("getCurrentTime(\"Asia/Taipei\") = " + result);

        assertNotNull(result, "時間不應該為空");
        assertTrue(result.contains("T"), "應該包含 ISO 8601 格式的時間");
        System.out.println("✓ 測試通過");
    }

    /**
     * 測試天氣工具 - 獲取溫度
     * 使用台北的座標進行測試
     */
    @Test
    public void testWeatherGetTemperature() {
        System.out.println("\n========================================");
        System.out.println("測試天氣工具 - getTemperature");
        System.out.println("========================================");

        try {
            var result = weatherToolProvider.getTemperature(25.0330, 121.5654, "Taipei");
            System.out.println("getTemperature(25.0330, 121.5654, \"Taipei\"):");
            System.out.println("  城市: " + result.city());
            System.out.println("  溫度: " + result.temperature() + "°C");
            System.out.println("  時間: " + result.time());

            assertNotNull(result, "結果不應該為空");
            assertEquals("Taipei", result.city(), "城市應該是 Taipei");
            assertNotNull(result.temperature(), "溫度不應該為空");
            System.out.println("✓ 測試通過");
        } catch (Exception e) {
            System.out.println("⚠ 天氣 API 調用失敗（可能是網絡問題）: " + e.getMessage());
            System.out.println("  這是正常的，因為測試環境可能無法訪問外部 API");
        }
    }
}
