package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * 銷售分析響應 DTO - 用於 5.7 企業數據工具
 * 包含詳細的銷售分析結果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesAnalysisResponse {

    /** 分析類型 */
    private String analysisType;

    /** 分析周期 */
    private String period;

    /** 開始日期 */
    private YearMonth startDate;

    /** 結束日期 */
    private YearMonth endDate;

    /** 分析結果摘要 */
    private SummaryStats summary;

    /** 產品銷售詳情 */
    private List<ProductSalesDetail> productDetails;

    /** 成長趨勢 */
    private List<TrendData> trends;

    /** 預測數據 */
    private List<ForecastData> forecasts;

    /** 異常提示 */
    private List<String> alerts;

    /**
     * 摘要統計
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryStats {
        /** 總銷售額 */
        private BigDecimal totalSalesAmount;

        /** 總銷售件數 */
        private Integer totalSalesVolume;

        /** 平均每件銷售額 */
        private BigDecimal averageUnitPrice;

        /** 環比增長率 (%) */
        private BigDecimal monthOverMonthGrowth;

        /** 同比增長率 (%) */
        private BigDecimal yearOverYearGrowth;

        /** 最暢銷的產品 ID */
        private String topProductId;

        /** 最暢銷的產品名稱 */
        private String topProductName;

        /** 排名變化 (相比上個時期) */
        private Map<String, Integer> rankingChanges;
    }

    /**
     * 產品銷售詳情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSalesDetail {
        /** 產品 ID */
        private String productId;

        /** 產品名稱 */
        private String productName;

        /** 分類 */
        private String category;

        /** 銷售額 */
        private BigDecimal salesAmount;

        /** 銷售件數 */
        private Integer salesVolume;

        /** 單價 */
        private BigDecimal unitPrice;

        /** 排名 */
        private Integer ranking;

        /** 市場占有率 (%) */
        private BigDecimal marketShare;

        /** 同比增長 (%) */
        private BigDecimal yoyGrowth;

        /** 環比增長 (%) */
        private BigDecimal momGrowth;
    }

    /**
     * 成長趨勢數據
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendData {
        /** 時間周期 */
        private YearMonth period;

        /** 銷售額 */
        private BigDecimal salesAmount;

        /** 銷售件數 */
        private Integer salesVolume;

        /** 環比增長率 (%) */
        private BigDecimal growthRate;

        /** 趨勢方向 (UP, DOWN, STABLE) */
        private String trend;
    }

    /**
     * 預測數據
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForecastData {
        /** 預測時間 */
        private YearMonth forecastPeriod;

        /** 預測銷售額 */
        private BigDecimal forecastedSalesAmount;

        /** 預測銷售件數 */
        private Integer forecastedVolume;

        /** 預測準確度 (%) */
        private BigDecimal confidence;

        /** 預測説明 */
        private String description;
    }
}
