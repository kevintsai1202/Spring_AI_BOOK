package com.example.enhancement.repository;

import com.example.enhancement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 員工資料存取層
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    /**
     * 根據員工編號查詢
     */
    Optional<Employee> findByEmployeeId(String employeeId);

    /**
     * 根據電子郵件查詢
     */
    Optional<Employee> findByEmail(String email);

    /**
     * 根據部門 ID 查詢員工列表
     */
    List<Employee> findByDepartmentId(Long departmentId);

    /**
     * 根據職位查詢員工列表
     */
    List<Employee> findByPosition(String position);

    /**
     * 根據技能關鍵字搜尋員工
     */
    @Query("SELECT e FROM Employee e WHERE e.skills LIKE %:skill%")
    List<Employee> findBySkillsContaining(@Param("skill") String skill);

    /**
     * 查詢所有員工用於向量化
     */
    @Query("SELECT e FROM Employee e")
    List<Employee> findAllForVectorization();
}
