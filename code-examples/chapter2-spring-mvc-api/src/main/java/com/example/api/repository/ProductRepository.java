package com.example.api.repository;

import com.example.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 產品資料存取介面
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
