package com.example.enhancement.repository;

import com.example.enhancement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 產品資料存取層
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    /**
     * 根據產品代碼查詢
     */
    Optional<Product> findByProductCode(String productCode);

    /**
     * 根據類別查詢產品列表
     */
    List<Product> findByCategory(String category);

    /**
     * 根據供應商查詢產品列表
     */
    List<Product> findBySupplier(String supplier);

    /**
     * 查詢庫存低於指定數量的產品
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold")
    List<Product> findLowStockProducts(int threshold);

    /**
     * 查詢所有產品用於向量化
     */
    @Query("SELECT p FROM Product p")
    List<Product> findAllForVectorization();
}
