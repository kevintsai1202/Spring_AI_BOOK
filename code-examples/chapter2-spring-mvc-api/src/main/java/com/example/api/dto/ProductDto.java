package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 產品資料傳輸物件
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    /** 產品 ID */
    private Long id;

    /** 產品名稱 */
    private String name;

    /** 產品描述 */
    private String description;

    /** 產品價格 */
    private BigDecimal price;

    /** 庫存數量 */
    private Integer stock;

    /** 產品類別 */
    private String category;

    /** 建立時間 */
    private LocalDateTime createdAt;

    /** 更新時間 */
    private LocalDateTime updatedAt;
}
