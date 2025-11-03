package com.example.mcp.server.advanced.repository;

import com.example.mcp.server.advanced.entity.CompletionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 補全數據 Repository
 * 提供對補全數據的存取方法
 */
@Repository
public interface CompletionDataRepository extends JpaRepository<CompletionData, String> {

    /**
     * 根據類別查找補全數據
     *
     * @param category 類別名稱
     * @return 補全數據（如果存在）
     */
    Optional<CompletionData> findByCategory(String category);

    /**
     * 查找所有啟用的補全數據
     *
     * @return 啟用的補全數據列表
     */
    List<CompletionData> findByEnabledTrue();

    /**
     * 檢查類別是否存在
     *
     * @param category 類別名稱
     * @return true 如果存在，否則 false
     */
    boolean existsByCategory(String category);

    /**
     * 根據類別模糊查詢補全數據
     *
     * @param categoryPattern 類別模式
     * @return 符合條件的補全數據列表
     */
    @Query("SELECT c FROM CompletionData c WHERE c.category LIKE %:categoryPattern%")
    List<CompletionData> findByCategoryContaining(@Param("categoryPattern") String categoryPattern);

    /**
     * 刪除指定類別的補全數據
     *
     * @param category 類別名稱
     */
    void deleteByCategory(String category);
}
