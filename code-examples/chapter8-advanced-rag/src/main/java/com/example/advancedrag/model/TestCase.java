package com.example.advancedrag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 測試案例
 *
 * 用於評估測試的測試案例定義
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCase {

    /**
     * 測試案例 ID
     */
    private String id;

    /**
     * 測試問題
     */
    private String question;

    /**
     * 期望的關鍵詞列表
     */
    @Builder.Default
    private List<String> expectedKeywords = new ArrayList<>();

    /**
     * 期望的上下文
     */
    private String expectedContext;

    /**
     * 期望的答案（可選）
     */
    private String expectedAnswer;

    /**
     * 難度係數（0-1，數字越大越難）
     */
    @Builder.Default
    private Double difficulty = 0.5;

    /**
     * 類別
     */
    @Builder.Default
    private String category = "general";
}
