package com.example.enhancement.repository;

import com.example.enhancement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 專案資料存取層
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    /**
     * 根據專案代碼查詢
     */
    Optional<Project> findByProjectCode(String projectCode);

    /**
     * 根據部門 ID 查詢專案列表
     */
    List<Project> findByDepartmentId(Long departmentId);

    /**
     * 根據狀態查詢專案列表
     */
    List<Project> findByStatus(String status);

    /**
     * 根據負責人 ID 查詢專案列表
     */
    List<Project> findByManagerId(Long managerId);

    /**
     * 查詢進行中的專案
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'In Progress'")
    List<Project> findActiveProjects();

    /**
     * 查詢所有專案用於向量化
     */
    @Query("SELECT p FROM Project p")
    List<Project> findAllForVectorization();
}
