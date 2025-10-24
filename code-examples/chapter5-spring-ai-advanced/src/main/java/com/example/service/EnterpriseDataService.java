package com.example.service;

import com.example.dto.SalesAnalysisRequest;
import com.example.dto.SalesAnalysisResponse;
import com.example.dto.SalesAnalysisResponse.*;
import com.example.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企業數據服務 - 用於 5.7 企業數據工具
 * 提供產品和銷售數據的查詢和分析功能
 */
@Slf4j
@Service
public class EnterpriseDataService {

    /** 樣本產品數據庫 */
    private final Map<String, Product> productDatabase = new HashMap<>();

    /** 樣本銷售數據庫 (年月 -> 產品ID -> 銷售數量) */
    private final Map<YearMonth, Map<String, Integer>> salesDatabase = new HashMap<>();

    public EnterpriseDataService() {
        initializeSampleData();
    }

    /**
     * 初始化樣本數據
     */
    private void initializeSampleData() {
        // 創建樣本產品
        Product p1 = Product.builder()
                .productId("PROD001")
                .productName("高性能筆記本電腦")
                .description("商務用高性能筆記本")
                .category("電子產品")
                .unitPrice(new BigDecimal("15000"))
                .stockQuantity(150)
                .avgMonthlySales(80)
                .status("ACTIVE")
                .launchDate(LocalDateTime.now().minusYears(2))
                .lastUpdated(LocalDateTime.now())
                .manufacturer("技術公司A")
                .supplier("供應商A")
                .build();

        Product p2 = Product.builder()
                .productId("PROD002")
                .productName("無線藍牙耳機")
                .description("降噪無線耳機")
                .category("音頻設備")
                .unitPrice(new BigDecimal("2500"))
                .stockQuantity(500)
                .avgMonthlySales(300)
                .status("ACTIVE")
                .launchDate(LocalDateTime.now().minusYears(1))
                .lastUpdated(LocalDateTime.now())
                .manufacturer("技術公司B")
                .supplier("供應商B")
                .build();

        Product p3 = Product.builder()
                .productId("PROD003")
                .productName("USB-C 快充器")
                .description("65W 快速充電器")
                .category("配件")
                .unitPrice(new BigDecimal("800"))
                .stockQuantity(1200)
                .avgMonthlySales(600)
                .status("ACTIVE")
                .launchDate(LocalDateTime.now().minusMonths(6))
                .lastUpdated(LocalDateTime.now())
                .manufacturer("技術公司C")
                .supplier("供應商C")
                .build();

        productDatabase.put(p1.getProductId(), p1);
        productDatabase.put(p2.getProductId(), p2);
        productDatabase.put(p3.getProductId(), p3);

        // 初始化銷售數據 (模擬最近12個月的數據)
        YearMonth now = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            YearMonth month = now.minusMonths(i);
            Map<String, Integer> monthlySales = new HashMap<>();

            // PROD001: 70-90 單位/月
            monthlySales.put("PROD001", 70 + (int)(Math.random() * 20));
            // PROD002: 250-350 單位/月
            monthlySales.put("PROD002", 250 + (int)(Math.random() * 100));
            // PROD003: 500-700 單位/月
            monthlySales.put("PROD003", 500 + (int)(Math.random() * 200));

            salesDatabase.put(month, monthlySales);
        }

        log.info("已初始化樣本數據：{} 個產品，{} 個月銷售記錄",
                productDatabase.size(), salesDatabase.size());
    }

    /**
     * 查詢所有產品
     */
    public List<Product> getAllProducts() {
        return new ArrayList<>(productDatabase.values());
    }

    /**
     * 根據 ID 查詢產品
     */
    public Optional<Product> getProductById(String productId) {
        return Optional.ofNullable(productDatabase.get(productId));
    }

    /**
     * 根據分類查詢產品
     */
    public List<Product> getProductsByCategory(String category) {
        return productDatabase.values().stream()
                .filter(p -> p.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * 執行銷售分析
     */
    public SalesAnalysisResponse analyzeSales(SalesAnalysisRequest request) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("無效的分析請求參數");
        }

        YearMonth startDate = request.getStartDate() != null ?
                request.getStartDate() : YearMonth.now().minusMonths(12);
        YearMonth endDate = request.getEndDate() != null ?
                request.getEndDate() : YearMonth.now();

        // 獲取相關產品
        List<String> analysisProductIds = getAnalysisProductIds(request);

        // 計算摘要統計
        SummaryStats summary = calculateSummary(startDate, endDate, analysisProductIds);

        // 計算產品詳情
        List<ProductSalesDetail> productDetails = calculateProductDetails(
                startDate, endDate, analysisProductIds
        );

        // 計算趨勢
        List<TrendData> trends = calculateTrends(startDate, endDate, analysisProductIds);

        // 計算預測 (如果需要)
        List<ForecastData> forecasts = request.getIncludeForecast() != null &&
                request.getIncludeForecast() ?
                calculateForecasts(endDate, analysisProductIds) : new ArrayList<>();

        // 生成警告提示
        List<String> alerts = generateAlerts(productDetails);

        return SalesAnalysisResponse.builder()
                .analysisType(request.getAnalysisType() != null ?
                        request.getAnalysisType() : "MONTHLY")
                .period(startDate + " 到 " + endDate)
                .startDate(startDate)
                .endDate(endDate)
                .summary(summary)
                .productDetails(productDetails)
                .trends(trends)
                .forecasts(forecasts)
                .alerts(alerts)
                .build();
    }

    /**
     * 獲取分析涉及的產品 ID 列表
     */
    private List<String> getAnalysisProductIds(SalesAnalysisRequest request) {
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            return request.getProductIds();
        }

        List<Product> products = productDatabase.values().stream()
                .filter(p -> request.getCategory() == null ||
                            p.getCategory().equals(request.getCategory()))
                .collect(Collectors.toList());

        return products.stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());
    }

    /**
     * 計算摘要統計
     */
    private SummaryStats calculateSummary(YearMonth startDate, YearMonth endDate,
                                          List<String> productIds) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalVolume = 0;

        for (YearMonth month = startDate; !month.isAfter(endDate);
             month = month.plusMonths(1)) {
            Map<String, Integer> monthlySales = salesDatabase.get(month);
            if (monthlySales != null) {
                for (String productId : productIds) {
                    Integer quantity = monthlySales.get(productId);
                    if (quantity != null) {
                        Product product = productDatabase.get(productId);
                        if (product != null) {
                            totalAmount = totalAmount.add(
                                    product.getUnitPrice().multiply(new BigDecimal(quantity))
                            );
                            totalVolume += quantity;
                        }
                    }
                }
            }
        }

        BigDecimal avgUnitPrice = totalVolume > 0 ?
                totalAmount.divide(new BigDecimal(totalVolume), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 簡化計算：無法實現真實的環比和同比增長
        String topProductId = productIds.stream()
                .findFirst()
                .orElse("");

        String topProductName = productDatabase.get(topProductId) != null ?
                productDatabase.get(topProductId).getProductName() : "";

        return SummaryStats.builder()
                .totalSalesAmount(totalAmount)
                .totalSalesVolume(totalVolume)
                .averageUnitPrice(avgUnitPrice)
                .monthOverMonthGrowth(new BigDecimal("5.2"))
                .yearOverYearGrowth(new BigDecimal("12.8"))
                .topProductId(topProductId)
                .topProductName(topProductName)
                .rankingChanges(new HashMap<>())
                .build();
    }

    /**
     * 計算產品詳情
     */
    private List<ProductSalesDetail> calculateProductDetails(YearMonth startDate,
                                                             YearMonth endDate,
                                                             List<String> productIds) {
        Map<String, ProductSalesDetail> detailsMap = new HashMap<>();
        BigDecimal totalSalesAmount = BigDecimal.ZERO;

        // 首先計算所有銷售金額
        for (YearMonth month = startDate; !month.isAfter(endDate);
             month = month.plusMonths(1)) {
            Map<String, Integer> monthlySales = salesDatabase.get(month);
            if (monthlySales != null) {
                for (String productId : productIds) {
                    Integer quantity = monthlySales.get(productId);
                    if (quantity != null) {
                        Product product = productDatabase.get(productId);
                        if (product != null) {
                            BigDecimal saleAmount = product.getUnitPrice().multiply(
                                    new BigDecimal(quantity)
                            );

                            detailsMap.putIfAbsent(productId, ProductSalesDetail.builder()
                                    .productId(productId)
                                    .productName(product.getProductName())
                                    .category(product.getCategory())
                                    .unitPrice(product.getUnitPrice())
                                    .salesAmount(BigDecimal.ZERO)
                                    .salesVolume(0)
                                    .yoyGrowth(BigDecimal.ZERO)
                                    .momGrowth(BigDecimal.ZERO)
                                    .build());

                            ProductSalesDetail detail = detailsMap.get(productId);
                            detail.setSalesAmount(detail.getSalesAmount().add(saleAmount));
                            detail.setSalesVolume(detail.getSalesVolume() + quantity);
                            totalSalesAmount = totalSalesAmount.add(saleAmount);
                        }
                    }
                }
            }
        }

        // 使用 final 變數以支持 Lambda 表達式
        final BigDecimal finalTotalSalesAmount = totalSalesAmount;

        // 計算市場占有率和增長率
        List<ProductSalesDetail> sortedDetails = detailsMap.values().stream()
                .peek(detail -> {
                    // 計算市場占有率
                    if (finalTotalSalesAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal marketShare = detail.getSalesAmount()
                                .divide(finalTotalSalesAmount, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal(100));
                        detail.setMarketShare(marketShare);
                    } else {
                        detail.setMarketShare(BigDecimal.ZERO);
                    }

                    // 計算增長率 (模擬數據)
                    detail.setYoyGrowth(new BigDecimal(Math.random() * 20 - 10).setScale(2, RoundingMode.HALF_UP));
                    detail.setMomGrowth(new BigDecimal(Math.random() * 15 - 7.5).setScale(2, RoundingMode.HALF_UP));
                })
                .sorted((a, b) -> b.getSalesAmount().compareTo(a.getSalesAmount()))
                .collect(Collectors.toList());

        // 設置排名
        for (int i = 0; i < sortedDetails.size(); i++) {
            sortedDetails.get(i).setRanking(i + 1);
        }

        return sortedDetails;
    }

    /**
     * 計算趨勢數據
     */
    private List<TrendData> calculateTrends(YearMonth startDate, YearMonth endDate,
                                            List<String> productIds) {
        List<TrendData> trends = new ArrayList<>();

        for (YearMonth month = startDate; !month.isAfter(endDate);
             month = month.plusMonths(1)) {
            Map<String, Integer> monthlySales = salesDatabase.get(month);
            if (monthlySales != null) {
                BigDecimal totalAmount = BigDecimal.ZERO;
                int totalVolume = 0;

                for (String productId : productIds) {
                    Integer quantity = monthlySales.get(productId);
                    if (quantity != null) {
                        Product product = productDatabase.get(productId);
                        if (product != null) {
                            totalAmount = totalAmount.add(
                                    product.getUnitPrice().multiply(new BigDecimal(quantity))
                            );
                            totalVolume += quantity;
                        }
                    }
                }

                trends.add(TrendData.builder()
                        .period(month)
                        .salesAmount(totalAmount)
                        .salesVolume(totalVolume)
                        .growthRate(new BigDecimal(Math.random() * 10 - 5))
                        .trend(Math.random() > 0.5 ? "UP" : "DOWN")
                        .build());
            }
        }

        return trends;
    }

    /**
     * 計算預測數據
     */
    private List<ForecastData> calculateForecasts(YearMonth startDate,
                                                  List<String> productIds) {
        List<ForecastData> forecasts = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            YearMonth forecastMonth = startDate.plusMonths(i);
            BigDecimal forecastedAmount = new BigDecimal(Math.random() * 100000 + 50000);

            forecasts.add(ForecastData.builder()
                    .forecastPeriod(forecastMonth)
                    .forecastedSalesAmount(forecastedAmount)
                    .forecastedVolume((int)(Math.random() * 1000 + 500))
                    .confidence(new BigDecimal(75 + Math.random() * 20))
                    .description("基於趨勢分析的預測")
                    .build());
        }

        return forecasts;
    }

    /**
     * 生成警告提示
     */
    private List<String> generateAlerts(List<ProductSalesDetail> productDetails) {
        List<String> alerts = new ArrayList<>();

        for (ProductSalesDetail detail : productDetails) {
            Product product = productDatabase.get(detail.getProductId());
            if (product != null && product.needsRestocking()) {
                alerts.add("警告：產品 [" + product.getProductName() + "] 庫存不足，建議補貨");
            }
        }

        if (alerts.isEmpty()) {
            alerts.add("所有產品狀態正常");
        }

        return alerts;
    }

    /**
     * 解析，用於流式輸出計算
     */
    private static void setRanking(List<ProductSalesDetail> list) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRanking(i + 1);
        }
    }
}
