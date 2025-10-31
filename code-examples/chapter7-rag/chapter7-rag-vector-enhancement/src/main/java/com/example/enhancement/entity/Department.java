package com.example.enhancement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 部門實體類
 * 對應 PostgreSQL 企業資料庫中的 departments 表
 */
@Entity
@Table(name = "departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    /**
     * 主鍵 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 部門名稱
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 部門描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 部門經理姓名
     */
    @Column(name = "manager_name", length = 100)
    private String managerName;

    /**
     * 部門位置
     */
    @Column(name = "location", length = 100)
    private String location;

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

    /**
     * 在持久化之前設置創建時間
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 在更新之前設置更新時間
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 生成用於向量化的文本描述
     */
    public String toVectorText() {
        StringBuilder text = new StringBuilder();
        text.append("部門名稱: ").append(name).append(". ");
        if (description != null && !description.isEmpty()) {
            text.append("描述: ").append(description).append(". ");
        }
        if (managerName != null) {
            text.append("經理: ").append(managerName).append(". ");
        }
        if (location != null) {
            text.append("位置: ").append(location).append(".");
        }
        return text.toString();
    }
}
