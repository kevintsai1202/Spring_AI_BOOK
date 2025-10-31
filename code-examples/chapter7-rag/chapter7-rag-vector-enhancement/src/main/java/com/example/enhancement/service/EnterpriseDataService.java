package com.example.enhancement.service;

import com.example.enhancement.entity.Department;
import com.example.enhancement.entity.Employee;
import com.example.enhancement.entity.Product;
import com.example.enhancement.entity.Project;
import com.example.enhancement.repository.DepartmentRepository;
import com.example.enhancement.repository.EmployeeRepository;
import com.example.enhancement.repository.ProductRepository;
import com.example.enhancement.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 企業資料服務
 * 從 PostgreSQL 讀取企業資料
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnterpriseDataService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository;
    private final ProjectRepository projectRepository;

    // ==================== Department 相關方法 ====================

    /**
     * 查詢所有部門
     */
    @Cacheable(value = "departments", key = "'all'")
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        log.info("查詢所有部門資料");
        return departmentRepository.findAll();
    }

    /**
     * 根據 ID 查詢部門
     */
    @Cacheable(value = "departments", key = "#id")
    @Transactional(readOnly = true)
    public Optional<Department> getDepartmentById(Long id) {
        log.debug("查詢部門 ID: {}", id);
        return departmentRepository.findById(id);
    }

    /**
     * 根據名稱查詢部門
     */
    @Cacheable(value = "departments", key = "'name:' + #name")
    @Transactional(readOnly = true)
    public Optional<Department> getDepartmentByName(String name) {
        log.debug("查詢部門名稱: {}", name);
        return departmentRepository.findByName(name);
    }

    // ==================== Employee 相關方法 ====================

    /**
     * 查詢所有員工
     */
    @Cacheable(value = "employees", key = "'all'")
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        log.info("查詢所有員工資料");
        return employeeRepository.findAll();
    }

    /**
     * 根據 ID 查詢員工
     */
    @Cacheable(value = "employees", key = "#id")
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        log.debug("查詢員工 ID: {}", id);
        return employeeRepository.findById(id);
    }

    /**
     * 根據員工編號查詢
     */
    @Cacheable(value = "employees", key = "'empId:' + #employeeId")
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmployeeId(String employeeId) {
        log.debug("查詢員工編號: {}", employeeId);
        return employeeRepository.findByEmployeeId(employeeId);
    }

    /**
     * 根據部門查詢員工
     */
    @Cacheable(value = "employees", key = "'dept:' + #departmentId")
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        log.debug("查詢部門 {} 的員工", departmentId);
        return employeeRepository.findByDepartmentId(departmentId);
    }

    /**
     * 根據技能搜尋員工
     */
    @Cacheable(value = "employees", key = "'skill:' + #skill")
    @Transactional(readOnly = true)
    public List<Employee> searchEmployeesBySkill(String skill) {
        log.debug("搜尋具備技能 {} 的員工", skill);
        return employeeRepository.findBySkillsContaining(skill);
    }

    // ==================== Product 相關方法 ====================

    /**
     * 查詢所有產品
     */
    @Cacheable(value = "products", key = "'all'")
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        log.info("查詢所有產品資料");
        return productRepository.findAll();
    }

    /**
     * 根據 ID 查詢產品
     */
    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        log.debug("查詢產品 ID: {}", id);
        return productRepository.findById(id);
    }

    /**
     * 根據產品代碼查詢
     */
    @Cacheable(value = "products", key = "'code:' + #productCode")
    @Transactional(readOnly = true)
    public Optional<Product> getProductByCode(String productCode) {
        log.debug("查詢產品代碼: {}", productCode);
        return productRepository.findByProductCode(productCode);
    }

    /**
     * 根據類別查詢產品
     */
    @Cacheable(value = "products", key = "'category:' + #category")
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        log.debug("查詢類別 {} 的產品", category);
        return productRepository.findByCategory(category);
    }

    // ==================== Project 相關方法 ====================

    /**
     * 查詢所有專案
     */
    @Cacheable(value = "projects", key = "'all'")
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        log.info("查詢所有專案資料");
        return projectRepository.findAll();
    }

    /**
     * 根據 ID 查詢專案
     */
    @Cacheable(value = "projects", key = "#id")
    @Transactional(readOnly = true)
    public Optional<Project> getProjectById(Long id) {
        log.debug("查詢專案 ID: {}", id);
        return projectRepository.findById(id);
    }

    /**
     * 根據專案代碼查詢
     */
    @Cacheable(value = "projects", key = "'code:' + #projectCode")
    @Transactional(readOnly = true)
    public Optional<Project> getProjectByCode(String projectCode) {
        log.debug("查詢專案代碼: {}", projectCode);
        return projectRepository.findByProjectCode(projectCode);
    }

    /**
     * 查詢進行中的專案
     */
    @Cacheable(value = "projects", key = "'active'")
    @Transactional(readOnly = true)
    public List<Project> getActiveProjects() {
        log.debug("查詢進行中的專案");
        return projectRepository.findActiveProjects();
    }

    /**
     * 根據部門查詢專案
     */
    @Cacheable(value = "projects", key = "'dept:' + #departmentId")
    @Transactional(readOnly = true)
    public List<Project> getProjectsByDepartment(Long departmentId) {
        log.debug("查詢部門 {} 的專案", departmentId);
        return projectRepository.findByDepartmentId(departmentId);
    }

    // ==================== 統計方法 ====================

    /**
     * 取得資料統計
     */
    @Cacheable(value = "statistics", key = "'overview'")
    @Transactional(readOnly = true)
    public EnterpriseDataStatistics getStatistics() {
        log.info("計算企業資料統計");
        return EnterpriseDataStatistics.builder()
                .totalDepartments(departmentRepository.count())
                .totalEmployees(employeeRepository.count())
                .totalProducts(productRepository.count())
                .totalProjects(projectRepository.count())
                .activeProjects(projectRepository.findActiveProjects().size())
                .build();
    }

    /**
     * 企業資料統計
     */
    @lombok.Data
    @lombok.Builder
    public static class EnterpriseDataStatistics implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private long totalDepartments;
        private long totalEmployees;
        private long totalProducts;
        private long totalProjects;
        private long activeProjects;
    }
}
