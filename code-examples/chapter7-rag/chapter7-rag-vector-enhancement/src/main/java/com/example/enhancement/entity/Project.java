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
 * 專案實體類
 * 對應 PostgreSQL 企業資料庫中的 projects 表
 */
@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_code", unique = true, nullable = false, length = 50)
    private String projectCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "budget", precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

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
        text.append("專案名稱: ").append(name).append(". ");
        text.append("專案代碼: ").append(projectCode).append(". ");
        if (description != null && !description.isEmpty()) {
            text.append("描述: ").append(description).append(". ");
        }
        if (status != null) {
            text.append("狀態: ").append(status).append(". ");
        }
        if (budget != null) {
            text.append("預算: ").append(budget).append("元. ");
        }
        if (startDate != null && endDate != null) {
            text.append("時間: ").append(startDate).append(" 至 ").append(endDate).append(".");
        }
        return text.toString();
    }
}
