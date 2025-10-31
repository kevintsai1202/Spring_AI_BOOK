package com.example.enhancement.repository;

import com.example.enhancement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 部門資料存取層
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    /**
     * 根據部門名稱查詢
     */
    Optional<Department> findByName(String name);

    /**
     * 根據位置查詢部門列表
     */
    List<Department> findByLocation(String location);

    /**
     * 查詢所有部門的向量化文本
     */
    @Query("SELECT d FROM Department d")
    List<Department> findAllForVectorization();
}
