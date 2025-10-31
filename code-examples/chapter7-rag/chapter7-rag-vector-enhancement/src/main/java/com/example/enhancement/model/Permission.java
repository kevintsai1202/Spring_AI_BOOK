package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 權限定義
 * 定義用戶對特定資料源和實體類型的存取權限
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    /**
     * 權限 ID
     */
    private String id;

    /**
     * 資料源名稱
     */
    private String dataSource;

    /**
     * 實體類型
     */
    private String entityType;

    /**
     * 存取類型（READ, WRITE, DELETE 等）
     */
    private AccessType accessType;

    /**
     * 權限條件列表（可選）
     * 例如：["department=IT", "level>=5"]
     */
    private List<String> conditions;

    /**
     * 權限描述
     */
    private String description;

    /**
     * 檢查權限是否匹配
     */
    public boolean matches(String dataSourceName, String entity, AccessType access) {
        return (dataSource == null || dataSource.equals(dataSourceName)) &&
               (entityType == null || entityType.equals(entity)) &&
               accessType == access;
    }
}
