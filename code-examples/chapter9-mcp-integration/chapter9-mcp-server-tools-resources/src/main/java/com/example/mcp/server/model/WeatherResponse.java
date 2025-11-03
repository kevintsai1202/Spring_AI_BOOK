package com.example.mcp.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 天氣 API 回應模型
 * 用於接收 Open-Meteo API 的回應數據
 */
@Data
public class WeatherResponse {

    /**
     * 當前天氣資訊
     */
    @JsonProperty("current")
    private CurrentWeather current;

    /**
     * 當前天氣詳細資料
     */
    @Data
    public static class CurrentWeather {
        /**
         * 時間
         */
        @JsonProperty("time")
        private LocalDateTime time;

        /**
         * 時間間隔（秒）
         */
        @JsonProperty("interval")
        private int interval;

        /**
         * 攝氏溫度（2公尺高度）
         */
        @JsonProperty("temperature_2m")
        private double temperature;
    }
}
