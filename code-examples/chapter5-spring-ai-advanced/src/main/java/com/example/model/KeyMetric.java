package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 關鍵指標
 * 用于表示业务分析中的关键指标
 */
public record KeyMetric(
        @JsonProperty("name") String name,
        @JsonProperty("value") String value,
        @JsonProperty("trend") String trend,
        @JsonProperty("importance") String importance
) {}
