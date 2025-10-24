package com.example.tools;

import com.example.dto.SalesAnalysisRequest;
import com.example.dto.SalesAnalysisResponse;
import com.example.model.Product;
import com.example.service.EnterpriseDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.function.Function;

/**
 * 產品銷售工具 - 提供銷售分析相關的 Tool Calling 功能
 * 用於 5.7 企業數據工具章節
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSalesTools {

    private final EnterpriseDataService enterpriseDataService;

    /**
     * 分析銷售數據
     * 參數: 分析類型, 開始月份, 結束月份, 產品分類
     * 返回: 完整的銷售分析結果
     */
    public SalesAnalysisResponse analyzeSalesData(
            String analysisType,
            String startDate,
            String endDate,
            String category) {

        log.info("執行銷售分析: type={}, start={}, end={}, category={}",
                analysisType, startDate, endDate, category);

        SalesAnalysisRequest request = SalesAnalysisRequest.builder()
                .analysisType(analysisType != null ? analysisType : "MONTHLY")
                .startDate(parseYearMonth(startDate))
                .endDate(parseYearMonth(endDate))
                .category(category)
                .includeTrendAnalysis(true)
                .includeForecast(true)
                .build();

        return enterpriseDataService.analyzeSales(request);
    }

    /**
     * 查詢特定時期的銷售排名
     * 參數: 月份 (格式: yyyy-MM)
     * 返回: 該月銷售排名 JSON
     */
    public String getSalesRankingByMonth(String month) {
        log.info("查詢銷售排名: month={}", month);

        YearMonth yearMonth = parseYearMonth(month);
        SalesAnalysisRequest request = SalesAnalysisRequest.builder()
                .analysisType("MONTHLY")
                .startDate(yearMonth)
                .endDate(yearMonth)
                .includeTrendAnalysis(false)
                .includeForecast(false)
                .build();

        SalesAnalysisResponse response = enterpriseDataService.analyzeSales(request);

        // 返回排名信息 (JSON 格式)
        StringBuilder sb = new StringBuilder();
        sb.append("當月銷售排名 (").append(month).append("):\n");
        for (var detail : response.getProductDetails()) {
            sb.append(String.format("%d. %s: %d 單位, 金額: %s\n",
                    detail.getRanking(),
                    detail.getProductName(),
                    detail.getSalesVolume(),
                    detail.getSalesAmount()));
        }

        return sb.toString();
    }

    /**
     * 計算年度銷售增長率
     * 參數: 年份 (格式: yyyy)
     * 返回: 增長率信息
     */
    public String getYearlyGrowthRate(String year) {
        log.info("查詢年度增長率: year={}", year);

        int yearNum = Integer.parseInt(year);
        YearMonth startDate = YearMonth.of(yearNum, 1);
        YearMonth endDate = YearMonth.of(yearNum, 12);

        SalesAnalysisRequest request = SalesAnalysisRequest.builder()
                .analysisType("YEARLY")
                .startDate(startDate)
                .endDate(endDate)
                .includeTrendAnalysis(true)
                .includeForecast(false)
                .build();

        SalesAnalysisResponse response = enterpriseDataService.analyzeSales(request);

        return String.format(
                "%s 年銷售分析:\n" +
                "總銷售額: %s\n" +
                "總銷售件數: %d\n" +
                "環比增長: %s%%\n" +
                "同比增長: %s%%",
                year,
                response.getSummary().getTotalSalesAmount(),
                response.getSummary().getTotalSalesVolume(),
                response.getSummary().getMonthOverMonthGrowth(),
                response.getSummary().getYearOverYearGrowth()
        );
    }

    /**
     * 獲取銷售預測
     * 參數: 預測月份數量 (1-12)
     * 返回: 預測數據
     */
    public String getForecast(int months) {
        log.info("獲取銷售預測: months={}", months);

        if (months < 1 || months > 12) {
            return "預測月份必須在 1-12 之間";
        }

        YearMonth endDate = YearMonth.now();
        YearMonth startDate = endDate.minusMonths(Math.max(0, 12 - months));

        SalesAnalysisRequest request = SalesAnalysisRequest.builder()
                .analysisType("FORECAST")
                .startDate(startDate)
                .endDate(endDate)
                .includeTrendAnalysis(true)
                .includeForecast(true)
                .build();

        SalesAnalysisResponse response = enterpriseDataService.analyzeSales(request);

        StringBuilder sb = new StringBuilder();
        sb.append("未來 ").append(months).append(" 個月銷售預測:\n");
        for (var forecast : response.getForecasts()) {
            // 格式化數字，只顯示兩位小數
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
            String formattedAmount = df.format(forecast.getForecastedSalesAmount());
            String formattedConfidence = df.format(forecast.getConfidence());

            sb.append(String.format("%s: 預測銷售額 %s, 件數 %d, 置信度 %s%%\n",
                    forecast.getForecastPeriod(),
                    formattedAmount,
                    forecast.getForecastedVolume(),
                    formattedConfidence));
        }

        return sb.toString();
    }

    /**
     * 比較產品銷售表現
     * 參數: 產品 ID 列表 (逗號分隔)
     * 返回: 產品對比分析結果
     */
    public String compareProductPerformance(String productIds) {
        log.info("比較產品表現: productIds={}", productIds);

        String[] ids = productIds.split(",");
        java.util.List<String> idList = java.util.Arrays.asList(ids);

        YearMonth startDate = YearMonth.now().minusMonths(6);
        YearMonth endDate = YearMonth.now();

        SalesAnalysisRequest request = SalesAnalysisRequest.builder()
                .analysisType("COMPARISON")
                .startDate(startDate)
                .endDate(endDate)
                .productIds(idList)
                .includeTrendAnalysis(true)
                .includeForecast(false)
                .build();

        SalesAnalysisResponse response = enterpriseDataService.analyzeSales(request);

        StringBuilder sb = new StringBuilder();
        sb.append("產品對比分析 (").append(startDate).append(" - ").append(endDate).append("):\n");
        for (var detail : response.getProductDetails()) {
            sb.append(String.format(
                    "%s: 銷售額 %s, 件數 %d, 市場占有率 %s%%\n",
                    detail.getProductName(),
                    detail.getSalesAmount(),
                    detail.getSalesVolume(),
                    detail.getMarketShare()));
        }

        return sb.toString();
    }

    /**
     * 分析銷售趨勢
     * 參數: 分類, 月數 (默認6)
     * 返回: 趨勢分析結果
     */
    public String analyzeTrend(String category, int months) {
        log.info("分析銷售趨勢: category={}, months={}", category, months);

        YearMonth endDate = YearMonth.now();
        YearMonth startDate = endDate.minusMonths(months - 1);

        SalesAnalysisRequest request = SalesAnalysisRequest.builder()
                .analysisType("TREND")
                .startDate(startDate)
                .endDate(endDate)
                .category(category)
                .includeTrendAnalysis(true)
                .includeForecast(false)
                .build();

        SalesAnalysisResponse response = enterpriseDataService.analyzeSales(request);

        StringBuilder sb = new StringBuilder();
        sb.append(category).append(" 銷售趨勢分析 (最近 ").append(months).append(" 個月):\n");
        for (var trend : response.getTrends()) {
            sb.append(String.format("%s: %s | 金額: %s | 件數: %d | 成長率: %s%%\n",
                    trend.getPeriod(),
                    trend.getTrend(),
                    trend.getSalesAmount(),
                    trend.getSalesVolume(),
                    trend.getGrowthRate()));
        }

        return sb.toString();
    }

    /**
     * 解析年月字符串
     */
    private YearMonth parseYearMonth(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return YearMonth.now();
        }

        try {
            if (dateStr.length() == 4) {
                // 只有年份，使用 1 月
                return YearMonth.of(Integer.parseInt(dateStr), 1);
            } else if (dateStr.contains("-")) {
                // 格式: yyyy-MM
                String[] parts = dateStr.split("-");
                return YearMonth.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
        } catch (Exception e) {
            log.warn("無法解析日期: {}, 使用當前月份", dateStr);
        }

        return YearMonth.now();
    }
}
