package com.example.mcp.server.advanced.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * 補全數據實體類別
 * 用於儲存自動完成建議的數據來源
 */
@Entity
@Table(name = "completion_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletionData {

    /**
     * 主鍵：類別名稱（如 username, country）
     */
    @Id
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * 補全值列表
     * 使用 @ElementCollection 儲存字串列表
     */
    @ElementCollection
    @CollectionTable(
        name = "completion_values",
        joinColumns = @JoinColumn(name = "category")
    )
    @Column(name = "value", length = 200)
    @Builder.Default
    private List<String> values = new ArrayList<>();

    /**
     * 描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 是否啟用
     */
    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 最大返回數量
     */
    @Column(name = "max_results")
    @Builder.Default
    private Integer maxResults = 10;
}
