package com.example.enhancement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 員工實體類
 * 對應 PostgreSQL 企業資料庫中的 employees 表
 */
@Entity
@Table(name = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    /**
     * 主鍵 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 員工編號
     */
    @Column(name = "employee_id", unique = true, nullable = false, length = 20)
    private String employeeId;

    /**
     * 員工姓名
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 電子郵件
     */
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    /**
     * 部門 ID
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 職位
     */
    @Column(name = "position", length = 100)
    private String position;

    /**
     * 薪資
     */
    @Column(name = "salary", precision = 10, scale = 2)
    private BigDecimal salary;

    /**
     * 入職日期
     */
    @Column(name = "hire_date")
    private LocalDate hireDate;

    /**
     * 電話號碼
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 地址
     */
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /**
     * 技能列表（逗號分隔）
     */
    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    /**
     * 創建時間
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 生成用於向量化的文本描述
     */
    public String toVectorText() {
        StringBuilder text = new StringBuilder();
        text.append("員工姓名: ").append(name).append(". ");
        text.append("員工編號: ").append(employeeId).append(". ");
        if (position != null) {
            text.append("職位: ").append(position).append(". ");
        }
        if (skills != null && !skills.isEmpty()) {
            text.append("技能: ").append(skills).append(". ");
        }
        if (email != null) {
            text.append("電子郵件: ").append(email).append(". ");
        }
        if (hireDate != null) {
            text.append("入職日期: ").append(hireDate).append(".");
        }
        return text.toString();
    }
}
