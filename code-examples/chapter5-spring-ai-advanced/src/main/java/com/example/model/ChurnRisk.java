package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 流失風險分析
 * 用于表示客户流失风险的信息
 */
public record ChurnRisk(
        @JsonProperty("high_risk_count") int highRiskCount,
        @JsonProperty("medium_risk_count") int mediumRiskCount,
        @JsonProperty("low_risk_count") int lowRiskCount,
        @JsonProperty("key_indicators") List<String> keyIndicators
) {}
