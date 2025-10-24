package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 指標預測
 * 用于表示单个指标的预测信息
 */
public record MetricForecast(
        @JsonProperty("metric_name") String metricName,
        @JsonProperty("current_value") double currentValue,
        @JsonProperty("predicted_value") double predictedValue,
        @JsonProperty("change_percentage") double changePercentage,
        @JsonProperty("confidence") double confidence
) {}
