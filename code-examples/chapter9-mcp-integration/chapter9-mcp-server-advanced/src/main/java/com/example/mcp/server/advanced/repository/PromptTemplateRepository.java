package com.example.mcp.server.advanced.repository;

import com.example.mcp.server.advanced.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提示模板 Repository
 * 提供對提示模板資料的存取方法
 */
@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    /**
     * 根據名稱查找提示模板
     *
     * @param name 提示名稱
     * @return 提示模板（如果存在）
     */
    Optional<PromptTemplate> findByName(String name);

    /**
     * 根據名稱模糊查詢提示模板
     *
     * @param namePattern 名稱模式（支援 SQL LIKE 語法）
     * @return 符合條件的提示模板列表
     */
    @Query("SELECT p FROM PromptTemplate p WHERE p.name LIKE %:namePattern%")
    List<PromptTemplate> findByNameContaining(@Param("namePattern") String namePattern);

    /**
     * 查詢所有包含特定參數的提示模板
     *
     * @param parameter 參數名稱
     * @return 包含該參數的提示模板列表
     */
    @Query("SELECT p FROM PromptTemplate p JOIN p.parameters param WHERE param = :parameter")
    List<PromptTemplate> findByParameter(@Param("parameter") String parameter);

    /**
     * 檢查提示名稱是否存在
     *
     * @param name 提示名稱
     * @return true 如果存在，否則 false
     */
    boolean existsByName(String name);

    /**
     * 刪除指定名稱的提示模板
     *
     * @param name 提示名稱
     */
    void deleteByName(String name);
}
