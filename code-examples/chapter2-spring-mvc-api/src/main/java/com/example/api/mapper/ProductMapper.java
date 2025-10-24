package com.example.api.mapper;

import com.example.api.dto.CreateProductRequest;
import com.example.api.dto.ProductDto;
import com.example.api.dto.UpdateProductRequest;
import com.example.api.entity.Product;
import org.springframework.stereotype.Component;

/**
 * 產品映射器
 * 負責 Product 實體與各種 DTO 之間的轉換
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Component
public class ProductMapper {

    /**
     * 將 Product 實體轉換為 ProductDto
     *
     * @param product 產品實體
     * @return 產品 DTO
     */
    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * 將 CreateProductRequest 轉換為 Product 實體
     *
     * @param request 建立產品請求
     * @return 產品實體
     */
    public Product toEntity(CreateProductRequest request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .build();
    }

    /**
     * 將 UpdateProductRequest 轉換為 Product 實體
     *
     * @param request 更新產品請求
     * @return 產品實體
     */
    public Product toEntity(UpdateProductRequest request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .build();
    }
}
