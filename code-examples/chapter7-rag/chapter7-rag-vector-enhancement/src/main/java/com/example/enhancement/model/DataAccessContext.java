package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 資料存取上下文
 * 用於追蹤和驗證使用者的資料存取請求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataAccessContext {
    /**
     * 用戶 ID
     */
    private String userId;

    /**
     * 會話 ID
     */
    private String sessionId;

    /**
     * 用戶資訊
     */
    private UserInfo userInfo;

    /**
     * 權限列表
     */
    private List<Permission> permissions;

    /**
     * 創建時間
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 過期時間
     */
    private LocalDateTime expiresAt;

    /**
     * 客戶端 IP 地址
     */
    private String clientIp;

    /**
     * 用戶代理
     */
    private String userAgent;

    /**
     * 檢查是否已過期
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 檢查是否有特定權限
     */
    public boolean hasPermission(String dataSource, String entityType, AccessType accessType) {
        if (permissions == null) {
            return false;
        }
        return permissions.stream()
                .anyMatch(p -> p.matches(dataSource, entityType, accessType));
    }
}
