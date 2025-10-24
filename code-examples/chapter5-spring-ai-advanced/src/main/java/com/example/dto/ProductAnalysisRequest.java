package com.example.dto;

import lombok.Data;

/**
 * 产品分析请求 DTO
 * 用于 Tool 链控制器的产品深度分析请求
 */
@Data
public class ProductAnalysisRequest {
    /** 产品代码 */
    private String productCode;

    /** 分析类型：comprehensive（全面）, sales（销售）, product（产品） */
    private String analysisType;

    /** 分析年份 */
    private Integer year;

    /** 焦点领域 */
    private String focusArea;
}
