package com.example.mcp.server.advanced.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 補全建議模型
 * 用於表示自動完成的建議項目
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionSuggestion {

    /**
     * 建議值
     */
    private String value;

    /**
     * 建議說明
     */
    private String description;

    /**
     * 相關性分數（0-100）
     */
    @Builder.Default
    private int score = 100;

    /**
     * 顯示標籤（可選，用於UI顯示）
     */
    private String label;

    /**
     * 類別
     */
    private String category;
}
