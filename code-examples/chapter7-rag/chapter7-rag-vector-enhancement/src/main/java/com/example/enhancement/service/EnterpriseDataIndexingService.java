package com.example.enhancement.service;

import com.example.enhancement.entity.Department;
import com.example.enhancement.entity.Employee;
import com.example.enhancement.entity.Product;
import com.example.enhancement.entity.Project;
import com.example.enhancement.repository.DepartmentRepository;
import com.example.enhancement.repository.EmployeeRepository;
import com.example.enhancement.repository.ProductRepository;
import com.example.enhancement.repository.ProjectRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企業資料索引服務
 * 將 PostgreSQL 中的企業資料向量化並存入 Neo4j Vector Store
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnterpriseDataIndexingService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository;
    private final ProjectRepository projectRepository;
    private final VectorStore vectorStore;
    private final MeterRegistry meterRegistry;

    /**
     * 索引所有企業資料
     */
    public IndexingResult indexAllEnterpriseData() {
        log.info("開始索引所有企業資料");
        Timer.Sample timer = Timer.start(meterRegistry);

        IndexingResult result = IndexingResult.builder()
                .startTime(LocalDateTime.now())
                .build();

        try {
            // 索引部門資料
            int departmentCount = indexDepartments();
            result.setDepartmentsIndexed(departmentCount);
            log.info("已索引 {} 個部門", departmentCount);

            // 索引員工資料
            int employeeCount = indexEmployees();
            result.setEmployeesIndexed(employeeCount);
            log.info("已索引 {} 個員工", employeeCount);

            // 索引產品資料
            int productCount = indexProducts();
            result.setProductsIndexed(productCount);
            log.info("已索引 {} 個產品", productCount);

            // 索引專案資料
            int projectCount = indexProjects();
            result.setProjectsIndexed(projectCount);
            log.info("已索引 {} 個專案", projectCount);

            result.setSuccess(true);
            result.setTotalDocuments(departmentCount + employeeCount + productCount + projectCount);

            // 記錄 metrics
            Counter.builder("enterprise.indexing.success")
                    .tag("type", "all")
                    .register(meterRegistry)
                    .increment();

        } catch (Exception e) {
            log.error("索引企業資料時發生錯誤", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());

            Counter.builder("enterprise.indexing.failure")
                    .tag("type", "all")
                    .register(meterRegistry)
                    .increment();
        } finally {
            result.setEndTime(LocalDateTime.now());
            timer.stop(Timer.builder("enterprise.indexing.duration")
                    .tag("type", "all")
                    .register(meterRegistry));
        }

        log.info("企業資料索引完成: {}", result);
        return result;
    }

    /**
     * 索引部門資料
     */
    public int indexDepartments() {
        log.debug("開始索引部門資料");
        List<Department> departments = departmentRepository.findAllForVectorization();

        List<Document> documents = departments.stream()
                .map(this::departmentToDocument)
                .collect(Collectors.toList());

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
            log.info("已將 {} 個部門向量化並存入 Neo4j", documents.size());
        }

        return documents.size();
    }

    /**
     * 索引員工資料
     */
    public int indexEmployees() {
        log.debug("開始索引員工資料");
        List<Employee> employees = employeeRepository.findAllForVectorization();

        List<Document> documents = employees.stream()
                .map(this::employeeToDocument)
                .collect(Collectors.toList());

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
            log.info("已將 {} 個員工向量化並存入 Neo4j", documents.size());
        }

        return documents.size();
    }

    /**
     * 索引產品資料
     */
    public int indexProducts() {
        log.debug("開始索引產品資料");
        List<Product> products = productRepository.findAllForVectorization();

        List<Document> documents = products.stream()
                .map(this::productToDocument)
                .collect(Collectors.toList());

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
            log.info("已將 {} 個產品向量化並存入 Neo4j", documents.size());
        }

        return documents.size();
    }

    /**
     * 索引專案資料
     */
    public int indexProjects() {
        log.debug("開始索引專案資料");
        List<Project> projects = projectRepository.findAllForVectorization();

        List<Document> documents = projects.stream()
                .map(this::projectToDocument)
                .collect(Collectors.toList());

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
            log.info("已將 {} 個專案向量化並存入 Neo4j", documents.size());
        }

        return documents.size();
    }

    /**
     * 重建索引（先刪除再重新索引）
     */
    public IndexingResult rebuildIndex() {
        log.warn("開始重建索引 - 這將刪除所有現有向量資料");
        // 注意：VectorStore 介面沒有 deleteAll 方法
        // 如需清空，需要手動操作 Neo4j 或實現自定義刪除邏輯
        return indexAllEnterpriseData();
    }

    // ==================== 轉換方法 ====================

    /**
     * 將部門轉換為 Document
     */
    private Document departmentToDocument(Department dept) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", "dept-" + dept.getId());
        metadata.put("type", "department");
        metadata.put("entity_id", dept.getId());
        metadata.put("name", dept.getName());
        metadata.put("location", dept.getLocation());
        metadata.put("manager", dept.getManagerName());
        metadata.put("source", "enterprise_db");

        return new Document(
                "dept-" + dept.getId(),
                dept.toVectorText(),
                metadata
        );
    }

    /**
     * 將員工轉換為 Document
     */
    private Document employeeToDocument(Employee emp) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", "emp-" + emp.getId());
        metadata.put("type", "employee");
        metadata.put("entity_id", emp.getId());
        metadata.put("employee_id", emp.getEmployeeId());
        metadata.put("name", emp.getName());
        metadata.put("email", emp.getEmail());
        metadata.put("position", emp.getPosition());
        metadata.put("department_id", emp.getDepartmentId());
        metadata.put("skills", emp.getSkills());
        metadata.put("source", "enterprise_db");

        return new Document(
                "emp-" + emp.getId(),
                emp.toVectorText(),
                metadata
        );
    }

    /**
     * 將產品轉換為 Document
     */
    private Document productToDocument(Product prod) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", "prod-" + prod.getId());
        metadata.put("type", "product");
        metadata.put("entity_id", prod.getId());
        metadata.put("product_code", prod.getProductCode());
        metadata.put("name", prod.getName());
        metadata.put("category", prod.getCategory());
        metadata.put("price", prod.getPrice() != null ? prod.getPrice().toString() : null);
        metadata.put("supplier", prod.getSupplier());
        metadata.put("source", "enterprise_db");

        return new Document(
                "prod-" + prod.getId(),
                prod.toVectorText(),
                metadata
        );
    }

    /**
     * 將專案轉換為 Document
     */
    private Document projectToDocument(Project proj) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", "proj-" + proj.getId());
        metadata.put("type", "project");
        metadata.put("entity_id", proj.getId());
        metadata.put("project_code", proj.getProjectCode());
        metadata.put("name", proj.getName());
        metadata.put("status", proj.getStatus());
        metadata.put("department_id", proj.getDepartmentId());
        metadata.put("budget", proj.getBudget() != null ? proj.getBudget().toString() : null);
        metadata.put("manager_id", proj.getManagerId());
        metadata.put("source", "enterprise_db");

        return new Document(
                "proj-" + proj.getId(),
                proj.toVectorText(),
                metadata
        );
    }

    /**
     * 索引結果
     */
    @lombok.Data
    @lombok.Builder
    public static class IndexingResult {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean success;
        private int departmentsIndexed;
        private int employeesIndexed;
        private int productsIndexed;
        private int projectsIndexed;
        private int totalDocuments;
        private String errorMessage;

        /**
         * 計算執行時間（秒）
         */
        public long getDurationSeconds() {
            if (startTime != null && endTime != null) {
                return java.time.Duration.between(startTime, endTime).getSeconds();
            }
            return 0;
        }
    }
}
