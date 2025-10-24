package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 電影資訊
 * 用于表示电影的详细信息
 */
public record MovieInfo(
        @JsonProperty("title") String title,
        @JsonProperty("director") String director,
        @JsonProperty("year") int year,
        @JsonProperty("genre") String genre,
        @JsonProperty("rating") double rating,
        @JsonProperty("plot") String plot
) {}
