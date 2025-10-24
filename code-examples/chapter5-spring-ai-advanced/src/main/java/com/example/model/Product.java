package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 產品模型 - 用於 5.7 企業數據工具
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    /** 產品ID */
    private String productId;

    /** 產品名稱 */
    private String productName;

    /** 產品描述 */
    private String description;

    /** 產品分類 */
    private String category;

    /** 單價 */
    private BigDecimal unitPrice;

    /** 庫存數量 */
    private Integer stockQuantity;

    /** 月銷售量平均值 */
    private Integer avgMonthlySales;

    /** 產品狀態 (ACTIVE, DISCONTINUED, OUT_OF_STOCK) */
    private String status;

    /** 上架時間 */
    private LocalDateTime launchDate;

    /** 最後更新時間 */
    private LocalDateTime lastUpdated;

    /** 製造商 */
    private String manufacturer;

    /** 供應商 */
    private String supplier;

    /**
     * 計算庫存價值
     */
    public BigDecimal calculateInventoryValue() {
        if (unitPrice == null || stockQuantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(new BigDecimal(stockQuantity));
    }

    /**
     * 計算月度營收
     */
    public BigDecimal calculateMonthlySales() {
        if (unitPrice == null || avgMonthlySales == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(new BigDecimal(avgMonthlySales));
    }

    /**
     * 是否缺貨
     */
    public boolean isOutOfStock() {
        return stockQuantity != null && stockQuantity <= 0;
    }

    /**
     * 是否需要補貨
     */
    public boolean needsRestocking() {
        // 當庫存少於平均月銷售量時需要補貨
        return stockQuantity != null && avgMonthlySales != null &&
               stockQuantity < avgMonthlySales;
    }
}
