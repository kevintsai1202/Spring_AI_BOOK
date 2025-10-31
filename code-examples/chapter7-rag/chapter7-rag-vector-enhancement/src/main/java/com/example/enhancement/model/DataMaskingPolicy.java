package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 資料脫敏策略
 * 定義如何對敏感資料進行脫敏處理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataMaskingPolicy {
    /**
     * 策略名稱
     */
    private String policyName;

    /**
     * 資料源
     */
    private String dataSource;

    /**
     * 實體類型
     */
    private String entityType;

    /**
     * 需要脫敏的欄位列表
     */
    private List<String> maskedFields;

    /**
     * 脫敏類型
     */
    private MaskingType maskingType;

    /**
     * 可見字符數（用於部分脫敏）
     */
    @Builder.Default
    private int visibleCharacters = 4;

    /**
     * 鹽值（用於雜湊脫敏）
     */
    private String salt;

    /**
     * 豁免角色列表（這些角色不需要脫敏）
     */
    private List<String> exemptRoles;

    /**
     * 是否啟用
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 檢查欄位是否需要脫敏
     */
    public boolean shouldMaskField(String fieldName) {
        return enabled && maskedFields != null && maskedFields.contains(fieldName);
    }

    /**
     * 檢查角色是否豁免
     */
    public boolean isRoleExempt(String role) {
        return exemptRoles != null && exemptRoles.contains(role);
    }
}
