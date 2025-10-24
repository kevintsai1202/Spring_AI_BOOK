package com.example.api;

import com.example.api.dto.CreateProductRequest;
import com.example.api.dto.ProductDto;
import com.example.api.dto.UpdateProductRequest;
import com.example.api.entity.Product;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.ProductMapper;
import com.example.api.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * 完整的 CRUD 操作實現
 * 展示各種 HTTP 方法的正確使用
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductRestController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductRestController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    /**
     * 建立新產品
     * POST /api/v1/products
     * 回傳 201 Created 和 Location 標頭
     */
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody @Valid CreateProductRequest request) {
        // 轉換請求為實體
        Product product = productMapper.toEntity(request);

        // 儲存產品
        Product savedProduct = productService.save(product);

        // 轉換為 DTO
        ProductDto productDto = productMapper.toDto(savedProduct);

        // 建立資源位置 URI
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedProduct.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(productDto);
    }

    /**
     * 完整更新產品
     * PUT /api/v1/products/{id}
     * 冪等操作，完整替換資源
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        // 檢查產品是否存在
        if (!productService.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        // 轉換請求為實體
        Product product = productMapper.toEntity(request);
        product.setId(id);

        // 更新產品
        Product updatedProduct = productService.update(product);

        // 轉換為 DTO
        ProductDto productDto = productMapper.toDto(updatedProduct);

        return ResponseEntity.ok(productDto);
    }

    /**
     * 部分更新產品
     * PATCH /api/v1/products/{id}
     * 只更新提供的欄位
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> patchProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        // 執行部分更新
        Product updatedProduct = productService.partialUpdate(id, updates);

        // 轉換為 DTO
        ProductDto productDto = productMapper.toDto(updatedProduct);

        return ResponseEntity.ok(productDto);
    }

    /**
     * 刪除產品
     * DELETE /api/v1/products/{id}
     * 冪等操作，重複刪除不會產生錯誤
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // 檢查產品是否存在
        if (!productService.existsById(id)) {
            // 冪等性:即使資源不存在也回傳 204
            return ResponseEntity.noContent().build();
        }

        // 刪除產品
        productService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
