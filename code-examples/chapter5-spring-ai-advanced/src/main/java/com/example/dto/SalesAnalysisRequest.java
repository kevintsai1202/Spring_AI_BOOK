package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.List;

/**
 * 銷售分析請求 DTO - 用於 5.7 企業數據工具
 * 用於接收銷售分析的查詢參數
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesAnalysisRequest {

    /** 分析類型 (MONTHLY, QUARTERLY, YEARLY, TREND) */
    private String analysisType;

    /** 開始時間 */
    private YearMonth startDate;

    /** 結束時間 */
    private YearMonth endDate;

    /** 產品 ID 列表 (為空表示所有產品) */
    private List<String> productIds;

    /** 產品分類 (為空表示所有分類) */
    private String category;

    /** 最小銷售量閾值 */
    private Integer minSalesThreshold;

    /** 最大銷售量閾值 */
    private Integer maxSalesThreshold;

    /** 排序欄位 (SALES_AMOUNT, SALES_VOLUME, GROWTH_RATE) */
    private String sortBy;

    /** 排序方向 (ASC, DESC) */
    private String sortDirection;

    /** 是否包括成長趨勢分析 */
    private Boolean includeTrendAnalysis;

    /** 是否包括預測數據 */
    private Boolean includeForecast;

    /**
     * 驗證請求參數的有效性
     */
    public boolean isValid() {
        // 檢查開始日期和結束日期
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                return false;
            }
        }

        // 檢查銷售量閾值
        if (minSalesThreshold != null && maxSalesThreshold != null) {
            if (minSalesThreshold > maxSalesThreshold) {
                return false;
            }
        }

        return true;
    }

    /**
     * 獲取時間範圍（月數）
     */
    public int getMonthsInRange() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate) + 1;
    }
}
