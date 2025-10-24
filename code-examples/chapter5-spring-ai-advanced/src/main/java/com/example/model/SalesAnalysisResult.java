package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 銷售分析結果
 * 用于表示销售数据的结构化分析
 */
public record SalesAnalysisResult(
        @JsonProperty("trend") String trend,
        @JsonProperty("total_revenue") double totalRevenue,
        @JsonProperty("growth_rate") double growthRate,
        @JsonProperty("top_products") List<String> topProducts,
        @JsonProperty("opportunities") List<String> opportunities,
        @JsonProperty("recommendations") List<String> recommendations,
        @JsonProperty("risk_factors") List<String> riskFactors
) {}
