package com.example.enhancement.controller;

import com.example.enhancement.entity.Department;
import com.example.enhancement.entity.Employee;
import com.example.enhancement.entity.Product;
import com.example.enhancement.entity.Project;
import com.example.enhancement.service.EnterpriseDataIndexingService;
import com.example.enhancement.service.EnterpriseDataService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企業資料控制器
 * 提供企業資料查詢和索引功能的 REST API
 */
@RestController
@RequestMapping("/api/enterprise")
@RequiredArgsConstructor
@Slf4j
public class EnterpriseDataController {

    private final EnterpriseDataService enterpriseDataService;
    private final EnterpriseDataIndexingService indexingService;

    // ==================== 索引管理 ====================

    /**
     * 索引所有企業資料到向量資料庫
     */
    @PostMapping("/index/all")
    @Timed(value = "enterprise.index.all", description = "Index all enterprise data")
    public ResponseEntity<EnterpriseDataIndexingService.IndexingResult> indexAllData() {
        log.info("收到索引所有企業資料的請求");
        EnterpriseDataIndexingService.IndexingResult result = indexingService.indexAllEnterpriseData();
        return ResponseEntity.ok(result);
    }

    /**
     * 索引部門資料
     */
    @PostMapping("/index/departments")
    @Timed(value = "enterprise.index.departments", description = "Index departments")
    public ResponseEntity<IndexResponse> indexDepartments() {
        log.info("收到索引部門資料的請求");
        int count = indexingService.indexDepartments();
        return ResponseEntity.ok(new IndexResponse("departments", count, true));
    }

    /**
     * 索引員工資料
     */
    @PostMapping("/index/employees")
    @Timed(value = "enterprise.index.employees", description = "Index employees")
    public ResponseEntity<IndexResponse> indexEmployees() {
        log.info("收到索引員工資料的請求");
        int count = indexingService.indexEmployees();
        return ResponseEntity.ok(new IndexResponse("employees", count, true));
    }

    /**
     * 索引產品資料
     */
    @PostMapping("/index/products")
    @Timed(value = "enterprise.index.products", description = "Index products")
    public ResponseEntity<IndexResponse> indexProducts() {
        log.info("收到索引產品資料的請求");
        int count = indexingService.indexProducts();
        return ResponseEntity.ok(new IndexResponse("products", count, true));
    }

    /**
     * 索引專案資料
     */
    @PostMapping("/index/projects")
    @Timed(value = "enterprise.index.projects", description = "Index projects")
    public ResponseEntity<IndexResponse> indexProjects() {
        log.info("收到索引專案資料的請求");
        int count = indexingService.indexProjects();
        return ResponseEntity.ok(new IndexResponse("projects", count, true));
    }

    // ==================== 部門查詢 ====================

    /**
     * 查詢所有部門
     */
    @GetMapping("/departments")
    @Timed(value = "enterprise.query.departments", description = "Query all departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        log.debug("查詢所有部門");
        return ResponseEntity.ok(enterpriseDataService.getAllDepartments());
    }

    /**
     * 根據 ID 查詢部門
     */
    @GetMapping("/departments/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        log.debug("查詢部門 ID: {}", id);
        return enterpriseDataService.getDepartmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== 員工查詢 ====================

    /**
     * 查詢所有員工
     */
    @GetMapping("/employees")
    @Timed(value = "enterprise.query.employees", description = "Query all employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.debug("查詢所有員工");
        return ResponseEntity.ok(enterpriseDataService.getAllEmployees());
    }

    /**
     * 根據 ID 查詢員工
     */
    @GetMapping("/employees/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        log.debug("查詢員工 ID: {}", id);
        return enterpriseDataService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根據技能搜尋員工
     */
    @GetMapping("/employees/search")
    public ResponseEntity<List<Employee>> searchEmployeesBySkill(@RequestParam String skill) {
        log.debug("搜尋技能: {}", skill);
        return ResponseEntity.ok(enterpriseDataService.searchEmployeesBySkill(skill));
    }

    // ==================== 產品查詢 ====================

    /**
     * 查詢所有產品
     */
    @GetMapping("/products")
    @Timed(value = "enterprise.query.products", description = "Query all products")
    public ResponseEntity<List<Product>> getAllProducts() {
        log.debug("查詢所有產品");
        return ResponseEntity.ok(enterpriseDataService.getAllProducts());
    }

    /**
     * 根據 ID 查詢產品
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.debug("查詢產品 ID: {}", id);
        return enterpriseDataService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== 專案查詢 ====================

    /**
     * 查詢所有專案
     */
    @GetMapping("/projects")
    @Timed(value = "enterprise.query.projects", description = "Query all projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        log.debug("查詢所有專案");
        return ResponseEntity.ok(enterpriseDataService.getAllProjects());
    }

    /**
     * 根據 ID 查詢專案
     */
    @GetMapping("/projects/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        log.debug("查詢專案 ID: {}", id);
        return enterpriseDataService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查詢進行中的專案
     */
    @GetMapping("/projects/active")
    public ResponseEntity<List<Project>> getActiveProjects() {
        log.debug("查詢進行中的專案");
        return ResponseEntity.ok(enterpriseDataService.getActiveProjects());
    }

    // ==================== 統計資訊 ====================

    /**
     * 獲取企業資料統計
     */
    @GetMapping("/statistics")
    @Timed(value = "enterprise.query.statistics", description = "Query statistics")
    public ResponseEntity<EnterpriseDataService.EnterpriseDataStatistics> getStatistics() {
        log.info("查詢企業資料統計");
        return ResponseEntity.ok(enterpriseDataService.getStatistics());
    }

    // ==================== 內部類 ====================

    /**
     * 索引響應
     */
    public record IndexResponse(String entityType, int indexedCount, boolean success) {
    }
}
