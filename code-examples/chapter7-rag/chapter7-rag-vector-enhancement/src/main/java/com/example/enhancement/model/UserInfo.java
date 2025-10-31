package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用戶資訊
 * 包含用戶的基本資訊和角色權限
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    /**
     * 用戶 ID
     */
    private String userId;

    /**
     * 用戶名
     */
    private String username;

    /**
     * 電子郵件
     */
    private String email;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 部門
     */
    private String department;

    /**
     * 是否啟用
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 是否鎖定
     */
    @Builder.Default
    private boolean locked = false;

    /**
     * 創建時間
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 最後登入時間
     */
    private LocalDateTime lastLoginAt;

    /**
     * 檢查用戶是否有指定角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * 檢查用戶是否有任一指定角色
     */
    public boolean hasAnyRole(List<String> requiredRoles) {
        if (roles == null || requiredRoles == null) {
            return false;
        }
        return requiredRoles.stream().anyMatch(roles::contains);
    }
}
