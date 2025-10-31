package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 語言檢測結果
 *
 * 包含檢測到的語言和置信度
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LanguageDetectionResult {

    /**
     * 檢測到的語言代碼（如：zh-TW, en, ja）
     */
    private String language;

    /**
     * 置信度（0.0 - 1.0）
     */
    private double confidence;

    /**
     * 檢測方法
     */
    private String method;
}
