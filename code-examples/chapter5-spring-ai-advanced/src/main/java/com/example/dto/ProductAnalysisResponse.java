package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品分析响应 DTO
 * 用于 Tool 链控制器返回的产品分析结果
 */
@Data
@Builder
public class ProductAnalysisResponse {
    /** 分析是否成功 */
    private boolean success;

    /** 产品代码 */
    private String productCode;

    /** 分析类型 */
    private String analysisType;

    /** 详细分析结果 */
    private String analysis;

    /** 执行耗时（毫秒） */
    private Long executionTime;

    /** 错误信息（失败时） */
    private String error;

    /** 时间戳 */
    private LocalDateTime timestamp;
}
