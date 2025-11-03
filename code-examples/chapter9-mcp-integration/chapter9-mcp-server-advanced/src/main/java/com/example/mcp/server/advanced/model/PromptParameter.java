package com.example.mcp.server.advanced.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提示參數模型
 * 用於描述提示模板的參數資訊
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptParameter {

    /**
     * 參數名稱
     */
    private String name;

    /**
     * 參數描述
     */
    private String description;

    /**
     * 是否必填
     */
    @Builder.Default
    private boolean required = false;

    /**
     * 預設值
     */
    private Object defaultValue;

    /**
     * 參數類型（如 string, integer, boolean）
     */
    @Builder.Default
    private String type = "string";
}
