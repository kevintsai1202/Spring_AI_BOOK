package com.example.advancedrag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 內容審核結果
 *
 * 返回給客戶端的審核結果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationResult {

    /**
     * 是否被標記為不當內容
     */
    private Boolean flagged;

    /**
     * 是否通過審核
     */
    private Boolean passed;

    /**
     * 審核分數（0-1，越高越危險）
     */
    private Double moderationScore;

    /**
     * 審核原因/描述
     */
    private String reason;

    /**
     * 被標記的類別列表
     */
    @Builder.Default
    private List<String> flaggedCategories = new ArrayList<>();

    /**
     * 處理時間（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 詳細信息（可選）
     */
    @Builder.Default
    private Map<String, Object> details = new HashMap<>();

    /**
     * 添加標記類別
     */
    public void addFlaggedCategory(String category) {
        if (this.flaggedCategories == null) {
            this.flaggedCategories = new ArrayList<>();
        }
        this.flaggedCategories.add(category);
    }

    /**
     * 添加詳細信息
     */
    public void addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
    }

    /**
     * 判斷是否高風險
     */
    public boolean isHighRisk() {
        return moderationScore != null && moderationScore >= 0.8;
    }

    /**
     * 判斷是否中等風險
     */
    public boolean isMediumRisk() {
        return moderationScore != null && moderationScore >= 0.5 && moderationScore < 0.8;
    }

    /**
     * 判斷是否低風險
     */
    public boolean isLowRisk() {
        return moderationScore != null && moderationScore < 0.5;
    }
}
