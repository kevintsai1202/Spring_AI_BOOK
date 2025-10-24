package com.example.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 建立產品請求 DTO
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    /** 產品名稱 */
    @NotBlank(message = "產品名稱不能為空")
    @Size(max = 200, message = "產品名稱長度不能超過 200 字元")
    private String name;

    /** 產品描述 */
    @Size(max = 1000, message = "產品描述長度不能超過 1000 字元")
    private String description;

    /** 產品價格 */
    @NotNull(message = "產品價格不能為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "產品價格必須大於 0")
    private BigDecimal price;

    /** 庫存數量 */
    @NotNull(message = "庫存數量不能為空")
    @Min(value = 0, message = "庫存數量不能小於 0")
    private Integer stock;

    /** 產品類別 */
    @Size(max = 50, message = "產品類別長度不能超過 50 字元")
    private String category;
}
