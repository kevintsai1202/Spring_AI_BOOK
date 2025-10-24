package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 產品推薦列表
 * 用于表示一组推荐的产品
 */
public record ProductRecommendations(
        @JsonProperty("category") String category,
        @JsonProperty("count") int count,
        @JsonProperty("products") List<ProductItem> products,
        @JsonProperty("error") String error
) {
    /**
     * 便捷构造函数（不包含error字段）
     */
    public ProductRecommendations(String category, int count, List<ProductItem> products) {
        this(category, count, products, null);
    }
}

/**
 * 產品項目
 */
record ProductItem(
        @JsonProperty("name") String name,
        @JsonProperty("brand") String brand,
        @JsonProperty("price") double price,
        @JsonProperty("rating") double rating,
        @JsonProperty("description") String description
) {}
