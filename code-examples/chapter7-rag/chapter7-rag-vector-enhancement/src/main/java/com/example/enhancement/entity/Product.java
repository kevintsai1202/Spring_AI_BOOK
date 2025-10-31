package com.example.enhancement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 產品實體類
 * 對應 PostgreSQL 企業資料庫中的 products 表
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", unique = true, nullable = false, length = 50)
    private String productCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "supplier", length = 200)
    private String supplier;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specifications", columnDefinition = "jsonb")
    private Map<String, Object> specifications;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 生成用於向量化的文本描述
     */
    public String toVectorText() {
        StringBuilder text = new StringBuilder();
        text.append("產品名稱: ").append(name).append(". ");
        text.append("產品代碼: ").append(productCode).append(". ");
        if (category != null) {
            text.append("類別: ").append(category).append(". ");
        }
        if (description != null && !description.isEmpty()) {
            text.append("描述: ").append(description).append(". ");
        }
        if (price != null) {
            text.append("價格: ").append(price).append("元. ");
        }
        if (supplier != null) {
            text.append("供應商: ").append(supplier).append(".");
        }
        return text.toString();
    }
}
