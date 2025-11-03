package com.example.mcp.server.advanced.service;

import lombok.extern.slf4j.Slf4j;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 動態工具管理器
 * 實現運行時動態添加和移除 MCP 工具
 */
@Service
@Slf4j
public class DynamicToolManager {

    @Autowired
    private McpSyncServer mcpSyncServer;

    private final CountDownLatch updateTrigger = new CountDownLatch(1);
    private boolean toolsAdded = false;

    /**
     * 初始工具：天氣預報
     *
     * @param city 城市名稱
     * @return 天氣預報資訊
     */
    @Tool(description = "Get weather forecast for a city")
    public String getWeatherForecast(String city) {
        log.info("取得天氣預報: city={}", city);
        return String.format("Weather forecast for %s: Sunny, 25°C, Light breeze", city);
    }

    /**
     * 應用程式啟動後設定動態更新機制
     */
    @EventListener(ApplicationReadyEvent.class)
    public void setupDynamicUpdate() {
        log.info("設定動態工具更新機制");

        new Thread(() -> {
            try {
                log.info("等待動態工具更新觸發...");
                // 等待觸發信號
                updateTrigger.await();

                if (!toolsAdded) {
                    log.info("開始動態添加工具...");

                    // 添加數學工具
                    addMathTools();

                    toolsAdded = true;
                    log.info("動態工具添加完成！");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("動態工具更新線程被中斷", e);
            }
        }, "DynamicToolUpdater").start();
    }

    /**
     * 動態添加數學工具
     */
    private void addMathTools() {
        log.info("註冊數學工具集");

        // 創建數學工具實例
        MathTools mathTools = new MathTools();

        // 轉換為 MCP 工具規格
        List<SyncToolSpecification> newTools = McpToolUtils
            .toSyncToolSpecifications(ToolCallbacks.from(mathTools));

        // 逐一添加工具
        for (SyncToolSpecification tool : newTools) {
            log.info("添加新工具: {}", tool);
            mcpSyncServer.addTool(tool);
        }

        log.info("數學工具集註冊完成，共 {} 個工具", newTools.size());
    }

    /**
     * 觸發工具更新
     * 此方法可由外部（如 REST API）調用以觸發動態工具添加
     */
    public void triggerUpdate() {
        if (updateTrigger.getCount() > 0) {
            log.info("觸發動態工具更新");
            updateTrigger.countDown();
        } else {
            log.warn("工具更新已經觸發過，無需重複觸發");
        }
    }

    /**
     * 檢查工具是否已添加
     */
    public boolean areToolsAdded() {
        return toolsAdded;
    }

    /**
     * 數學工具類別
     * 這些工具會在運行時動態添加到 MCP Server
     */
    public static class MathTools {

        /**
         * 加法運算
         */
        @Tool(description = "Adds two numbers")
        public int sumNumbers(int number1, int number2) {
            return number1 + number2;
        }

        /**
         * 乘法運算
         */
        @Tool(description = "Multiplies two numbers")
        public int multiplyNumbers(int number1, int number2) {
            return number1 * number2;
        }

        /**
         * 除法運算
         */
        @Tool(description = "Divides two numbers")
        public double divideNumbers(double number1, double number2) {
            if (number2 == 0) {
                throw new IllegalArgumentException("Division by zero is not allowed");
            }
            return number1 / number2;
        }

        /**
         * 次方運算
         */
        @Tool(description = "Calculates power of a number")
        public double powerNumbers(double base, double exponent) {
            return Math.pow(base, exponent);
        }
    }
}
