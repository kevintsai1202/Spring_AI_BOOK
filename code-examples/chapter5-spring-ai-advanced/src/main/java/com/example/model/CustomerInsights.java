package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 客戶洞察
 * 用于表示客户行为分析的洞察结果
 */
public record CustomerInsights(
        @JsonProperty("segments") List<CustomerSegment> segments,
        @JsonProperty("behavior_patterns") List<String> behaviorPatterns,
        @JsonProperty("preferences") List<String> preferences,
        @JsonProperty("churn_risk") ChurnRisk churnRisk,
        @JsonProperty("personalization_suggestions") List<String> personalizationSuggestions
) {}
