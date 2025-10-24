package com.example.api.service;

import com.example.api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 產品服務介面
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
public interface ProductService {

    /**
     * 查詢所有產品（分頁）
     *
     * @param pageable 分頁參數
     * @return 產品分頁結果
     */
    Page<Product> findAll(Pageable pageable);

    /**
     * 根據 ID 查詢產品
     *
     * @param id 產品 ID
     * @return 產品
     */
    Product findById(Long id);

    /**
     * 儲存產品
     *
     * @param product 產品
     * @return 儲存後的產品
     */
    Product save(Product product);

    /**
     * 更新產品
     *
     * @param product 產品
     * @return 更新後的產品
     */
    Product update(Product product);

    /**
     * 部分更新產品
     *
     * @param id      產品 ID
     * @param updates 更新欄位
     * @return 更新後的產品
     */
    Product partialUpdate(Long id, Map<String, Object> updates);

    /**
     * 刪除產品
     *
     * @param id 產品 ID
     */
    void deleteById(Long id);

    /**
     * 檢查產品是否存在
     *
     * @param id 產品 ID
     * @return 是否存在
     */
    boolean existsById(Long id);
}
