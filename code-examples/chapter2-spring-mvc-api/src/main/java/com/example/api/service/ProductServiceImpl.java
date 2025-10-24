package com.example.api.service;

import com.example.api.entity.Product;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 產品服務實作類別
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(Product product) {
        if (!productRepository.existsById(product.getId())) {
            throw new ResourceNotFoundException("Product not found with id: " + product.getId());
        }
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product partialUpdate(Long id, Map<String, Object> updates) {
        Product product = findById(id);

        // 根據提供的欄位進行更新
        updates.forEach((key, value) -> {
            switch (key) {
                case "name" -> product.setName((String) value);
                case "description" -> product.setDescription((String) value);
                case "price" -> product.setPrice(new BigDecimal(value.toString()));
                case "stock" -> product.setStock((Integer) value);
                case "category" -> product.setCategory((String) value);
            }
        });

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }
}
