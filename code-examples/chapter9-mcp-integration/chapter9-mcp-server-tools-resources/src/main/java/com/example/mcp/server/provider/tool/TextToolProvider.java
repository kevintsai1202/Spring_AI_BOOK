package com.example.mcp.server.provider.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * 文本處理工具提供者
 * 提供文本處理工具
 */
@Service
@Slf4j
public class TextToolProvider {

    /**
     * 轉換為大寫
     */
    @Tool(description = "Convert text to uppercase")
    public String toUpperCase(String text) {
        log.info("執行大寫轉換: {}", text);
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.toUpperCase();
    }

    /**
     * 轉換為小寫
     */
    @Tool(description = "Convert text to lowercase")
    public String toLowerCase(String text) {
        log.info("執行小寫轉換: {}", text);
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.toLowerCase();
    }

    /**
     * 計算單字數量
     */
    @Tool(description = "Count the number of words in text")
    public int wordCount(String text) {
        log.info("計算單字數量: {}", text);
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}
