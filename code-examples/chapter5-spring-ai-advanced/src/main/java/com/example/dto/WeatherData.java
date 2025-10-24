package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 天氣數據 DTO - 用於 5.9 天氣 API 集成
 * 包含完整的天氣預報信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherData {

    /** 地點名稱 */
    private String locationName;

    /** 緯度 */
    private Double latitude;

    /** 經度 */
    private Double longitude;

    /** 數據獲取時間 */
    private LocalDateTime dataTime;

    /** 當前天氣 */
    private CurrentWeather currentWeather;

    /** 逐小時預報 */
    private List<HourlyForecast> hourlyForecasts;

    /** 逐日預報 */
    private List<DailyForecast> dailyForecasts;

    /** 警告信息 */
    private List<WeatherAlert> alerts;

    /**
     * 當前天氣信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrentWeather {
        /** 溫度 (攝氏度) */
        private Double temperature;

        /** 體感溫度 */
        private Double feelsLike;

        /** 天氣狀況 */
        private String condition;

        /** 天氣描述 */
        private String description;

        /** 濕度 (%) */
        private Integer humidity;

        /** 氣壓 (hPa) */
        private Integer pressure;

        /** 風速 (m/s) */
        private Double windSpeed;

        /** 風向 (度) */
        private Integer windDirection;

        /** 風向名稱 */
        private String windDirectionName;

        /** 能見度 (km) */
        private Double visibility;

        /** 降水概率 (%) */
        private Integer precipitationProbability;

        /** 紫外線指數 */
        private Integer uvIndex;

        /** 更新時間 */
        private LocalDateTime updateTime;
    }

    /**
     * 逐小時預報
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HourlyForecast {
        /** 預報時間 */
        private LocalDateTime forecastTime;

        /** 溫度 */
        private Double temperature;

        /** 體感溫度 */
        private Double feelsLike;

        /** 天氣狀況 */
        private String condition;

        /** 降水概率 (%) */
        private Integer precipitationProbability;

        /** 風速 */
        private Double windSpeed;

        /** 降水量 (mm) */
        private Double precipitation;

        /** 相對濕度 (%) */
        private Integer humidity;
    }

    /**
     * 逐日預報
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyForecast {
        /** 預報日期 */
        private String forecastDate;

        /** 最高溫度 */
        private Double maxTemperature;

        /** 最低溫度 */
        private Double minTemperature;

        /** 平均溫度 */
        private Double avgTemperature;

        /** 白天天氣狀況 */
        private String dayCondition;

        /** 夜晚天氣狀況 */
        private String nightCondition;

        /** 降水概率 (%) */
        private Integer precipitationProbability;

        /** 累積降水量 (mm) */
        private Double totalPrecipitation;

        /** 最大風速 */
        private Double maxWindSpeed;

        /** 最大相對濕度 (%) */
        private Integer maxHumidity;

        /** 最小相對濕度 (%) */
        private Integer minHumidity;

        /** 紫外線指數 */
        private Integer uvIndex;

        /** 日出時間 */
        private String sunrise;

        /** 日落時間 */
        private String sunset;

        /** 建議 */
        private String recommendation;
    }

    /**
     * 天氣警告
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeatherAlert {
        /** 警告類型 (颱風, 豪雨, 低溫等) */
        private String alertType;

        /** 警告等級 (普通, 警戒, 警報) */
        private String alertLevel;

        /** 警告內容 */
        private String content;

        /** 生效時間 */
        private LocalDateTime effectiveTime;

        /** 結束時間 */
        private LocalDateTime expiryTime;

        /** 發布單位 */
        private String issuedBy;
    }
}
