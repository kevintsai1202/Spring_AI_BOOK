package com.example.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 增强版销售工具 - 用于 Tool 链调用
 * 提供产品销售数据分析和比较功能
 */
@Component
@Slf4j
public class EnhancedSalesTools {

    /**
     * 产品销售数据模型
     */
    public record ProductSales(
            String product,
            String productName,
            int salesVolume,
            BigDecimal revenue,
            String category,
            double marketShare
    ) {}

    /**
     * 销售排行数据模型
     */
    public record SalesRanking(
            List<ProductSales> topProducts,
            ProductSales bestSeller,
            int totalVolume,
            BigDecimal totalRevenue,
            String analysisYear
    ) {}

    /**
     * 获取指定年份的产品销售数据
     * @param year 查询年份
     * @return 销售数据列表
     */
    public SalesRanking getProductSalesRanking(int year) {
        try {
            log.info("查询年度销售排行：{}", year);

            List<ProductSales> salesData = getSalesDataByYear(year);

            if (salesData.isEmpty()) {
                return new SalesRanking(
                        List.of(),
                        null,
                        0,
                        BigDecimal.ZERO,
                        String.valueOf(year)
                );
            }

            // 计算总销量和总营收
            int totalVolume = salesData.stream()
                    .mapToInt(ProductSales::salesVolume)
                    .sum();

            BigDecimal totalRevenue = salesData.stream()
                    .map(ProductSales::revenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 按销量排序
            List<ProductSales> topProducts = salesData.stream()
                    .sorted((p1, p2) -> Integer.compare(p2.salesVolume(), p1.salesVolume()))
                    .collect(Collectors.toList());

            ProductSales bestSeller = topProducts.get(0);

            SalesRanking ranking = new SalesRanking(
                    topProducts,
                    bestSeller,
                    totalVolume,
                    totalRevenue,
                    String.valueOf(year)
            );

            log.info("销售排行查询完成：{}年，最佳销售产品：{}",
                    year, bestSeller.productName());

            return ranking;

        } catch (Exception e) {
            log.error("查询销售排行失败：{}", year, e);
            throw new RuntimeException("查询销售排行失败：" + e.getMessage());
        }
    }

    /**
     * 比较不同产品的销售表现
     * @param year 比较年份
     * @param products 产品代码列表
     * @return 比较结果
     */
    public String compareProductPerformance(int year, List<String> products) {
        try {
            log.info("比较产品销售表现：{}年，产品：{}", year, products);

            List<ProductSales> allSales = getSalesDataByYear(year);

            List<ProductSales> targetProducts = allSales.stream()
                    .filter(sale -> products.contains(sale.product()))
                    .sorted((p1, p2) -> Integer.compare(p2.salesVolume(), p1.salesVolume()))
                    .collect(Collectors.toList());

            if (targetProducts.isEmpty()) {
                return "未找到指定产品的销售数据";
            }

            StringBuilder comparison = new StringBuilder();
            comparison.append(String.format("📊 %d年产品销售表现比较\n\n", year));

            for (int i = 0; i < targetProducts.size(); i++) {
                ProductSales product = targetProducts.get(i);
                comparison.append(String.format(
                        "%d. %s (%s)\n" +
                        "   销售量：%,d 台\n" +
                        "   营收：%s\n" +
                        "   市场占有率：%.2f%%\n\n",
                        i + 1,
                        product.productName(),
                        product.product(),
                        product.salesVolume(),
                        formatCurrency(product.revenue()),
                        product.marketShare()
                ));
            }

            // 添加分析洞察
            ProductSales winner = targetProducts.get(0);
            comparison.append("🏆 **分析洞察**\n");
            comparison.append(String.format(
                    "- 最佳表现：%s，销售量领先\n",
                    winner.productName()
            ));

            if (targetProducts.size() > 1) {
                ProductSales second = targetProducts.get(1);
                double gap = ((double) (winner.salesVolume() - second.salesVolume()) / second.salesVolume()) * 100;
                comparison.append(String.format(
                        "- 领先优势：比第二名多 %.1f%%\n",
                        gap
                ));
            }

            log.info("产品比较完成");
            return comparison.toString();

        } catch (Exception e) {
            log.error("比较产品销售表现失败", e);
            return "比较产品销售表现失败：" + e.getMessage();
        }
    }

    private List<ProductSales> getSalesDataByYear(int year) {
        // 模拟不同年份的销售数据
        Map<Integer, List<ProductSales>> salesDatabase = Map.of(
            2023, List.of(
                new ProductSales("PD-1385", "智能手表系列", 15000,
                        new BigDecimal("300000000"), "穿戴装置", 35.7),
                new ProductSales("PD-1234", "笔记型电脑 Ultra 系列", 10000,
                        new BigDecimal("800000000"), "笔记型电脑", 23.8),
                new ProductSales("PD-1405", "智能手机 Pro 系列", 8500,
                        new BigDecimal("425000000"), "智能手机", 20.2),
                new ProductSales("PD-1235", "平板电脑系列", 1500,
                        new BigDecimal("75000000"), "平板电脑", 3.6),
                new ProductSales("PD-1255", "无线耳机系列", 800,
                        new BigDecimal("24000000"), "音响设备", 1.9),
                new ProductSales("PD-1300", "智能音箱系列", 500,
                        new BigDecimal("15000000"), "智能家居", 1.2)
            ),
            2024, List.of(
                new ProductSales("PD-1405", "智能手机 Pro 系列", 18500,
                        new BigDecimal("925000000"), "智能手机", 28.5),
                new ProductSales("PD-1385", "智能手表系列", 17000,
                        new BigDecimal("510000000"), "穿戴装置", 26.2),
                new ProductSales("PD-1234", "笔记型电脑 Ultra 系列", 12000,
                        new BigDecimal("960000000"), "笔记型电脑", 18.5),
                new ProductSales("PD-1235", "平板电脑系列", 8500,
                        new BigDecimal("425000000"), "平板电脑", 13.1),
                new ProductSales("PD-1255", "无线耳机系列", 5500,
                        new BigDecimal("165000000"), "音响设备", 8.5),
                new ProductSales("PD-1300", "智能音箱系列", 3500,
                        new BigDecimal("105000000"), "智能家居", 5.4)
            )
        );

        return salesDatabase.getOrDefault(year, List.of());
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("100000000")) >= 0) {
            return String.format("NT$ %.1f亿",
                    amount.divide(new BigDecimal("100000000"), 1, RoundingMode.HALF_UP).doubleValue());
        } else if (amount.compareTo(new BigDecimal("10000")) >= 0) {
            return String.format("NT$ %.0f万",
                    amount.divide(new BigDecimal("10000"), 0, RoundingMode.HALF_UP).doubleValue());
        } else {
            return String.format("NT$ %,d", amount.intValue());
        }
    }
}
