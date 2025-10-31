package com.example.advancedrag.dto;

import com.example.advancedrag.model.ModerationContext;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 內容審核請求
 *
 * 客戶端請求內容審核的數據
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationRequest {

    /**
     * 待審核的內容
     */
    @NotBlank(message = "待審核內容不能為空")
    private String content;

    /**
     * 審核上下文
     */
    private ModerationContext context;

    /**
     * 是否使用 OpenAI 審核
     */
    @Builder.Default
    private Boolean useOpenAI = true;

    /**
     * 是否使用自定義規則審核
     */
    @Builder.Default
    private Boolean useCustomRules = true;

    /**
     * 風險閾值（0-1，超過此值則視為不通過）
     */
    @Builder.Default
    private Double threshold = 0.8;

    /**
     * 是否嚴格模式（任一審核不通過即返回失敗）
     */
    @Builder.Default
    private Boolean strictMode = false;

    /**
     * 自定義審核規則 ID 列表（可選）
     */
    @Builder.Default
    private List<String> customRuleIds = new ArrayList<>();

    /**
     * 獲取審核上下文（如果為 null 則返回默認值）
     */
    public ModerationContext getContextOrDefault() {
        return context != null ? context : ModerationContext.defaultContext();
    }

    /**
     * 添加自定義規則 ID
     */
    public void addCustomRuleId(String ruleId) {
        if (this.customRuleIds == null) {
            this.customRuleIds = new ArrayList<>();
        }
        this.customRuleIds.add(ruleId);
    }
}
