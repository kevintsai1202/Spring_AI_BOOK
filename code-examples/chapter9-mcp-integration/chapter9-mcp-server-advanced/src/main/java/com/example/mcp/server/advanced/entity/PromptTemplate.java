package com.example.mcp.server.advanced.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 提示模板實體類別
 * 用於儲存動態提示模板資料
 */
@Entity
@Table(name = "prompt_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptTemplate {

    /**
     * 主鍵 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 提示名稱（唯一）
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 提示描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 提示參數列表
     * 使用 @ElementCollection 儲存簡單的字串列表
     */
    @ElementCollection
    @CollectionTable(
        name = "prompt_template_parameters",
        joinColumns = @JoinColumn(name = "template_id")
    )
    @Column(name = "parameter_name")
    @Builder.Default
    private List<String> parameters = new ArrayList<>();

    /**
     * 提示模板內容
     * 使用 @Lob 儲存大文本
     */
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String template;

    /**
     * 建立時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 在持久化前設定建立時間
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 在更新前設定更新時間
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
