package com.example.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 產品實體類別
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /** 產品 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 產品名稱 */
    @Column(nullable = false, length = 200)
    private String name;

    /** 產品描述 */
    @Column(length = 1000)
    private String description;

    /** 產品價格 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 庫存數量 */
    @Column(nullable = false)
    private Integer stock;

    /** 產品類別 */
    @Column(length = 50)
    private String category;

    /** 建立時間 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間 */
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
}
