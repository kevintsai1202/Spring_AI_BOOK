package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 市場預測
 * 用于表示市场趋势的预测结果
 */
public record MarketForecast(
        @JsonProperty("overall_trend") String overallTrend,
        @JsonProperty("key_metrics") List<MetricForecast> keyMetrics,
        @JsonProperty("opportunities") List<String> opportunities,
        @JsonProperty("threats") List<String> threats,
        @JsonProperty("strategic_recommendations") List<String> strategicRecommendations,
        @JsonProperty("confidence_level") double confidenceLevel,
        @JsonProperty("timeframe") String timeframe
) {}
