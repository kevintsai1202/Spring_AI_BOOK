package com.example.tools;

import com.example.dto.WeatherData;
import com.example.dto.WeatherData.*;
import com.example.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 天氣工具 - 提供天氣查詢相關的 Tool Calling 功能
 * 用於 5.9 天氣 API 集成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherTools {

    private final WeatherService weatherService;

    /**
     * 查詢特定城市的當前天氣
     * 參數: 城市名稱
     * 返回: 當前天氣信息
     */
    public String getCurrentWeather(String cityName) {
        log.info("查詢當前天氣: {}", cityName);

        try {
            WeatherData weatherData = weatherService.getWeatherByCity(cityName);
            CurrentWeather current = weatherData.getCurrentWeather();

            return String.format(
                    "%s 當前天氣:\n" +
                    "溫度: %.1f°C (體感 %.1f°C)\n" +
                    "天氣狀況: %s\n" +
                    "濕度: %d%%\n" +
                    "風速: %.1f m/s (%s)\n" +
                    "氣壓: %d hPa\n" +
                    "能見度: %.1f km\n" +
                    "降水概率: %d%%\n" +
                    "紫外線指數: %d",
                    cityName,
                    current.getTemperature(),
                    current.getFeelsLike(),
                    current.getCondition(),
                    current.getHumidity(),
                    current.getWindSpeed(),
                    current.getWindDirectionName(),
                    current.getPressure(),
                    current.getVisibility(),
                    current.getPrecipitationProbability(),
                    current.getUvIndex()
            );
        } catch (Exception e) {
            log.error("查詢天氣失敗", e);
            return "無法獲取 " + cityName + " 的天氣數據";
        }
    }

    /**
     * 查詢本週天氣預報
     * 參數: 城市名稱
     * 返回: 七天天氣預報
     */
    public String getWeekForecast(String cityName) {
        log.info("查詢本週天氣預報: {}", cityName);

        try {
            String summary = weatherService.getWeeklyWeatherSummary(cityName);
            return summary;
        } catch (Exception e) {
            log.error("查詢預報失敗", e);
            return "無法獲取 " + cityName + " 的天氣預報";
        }
    }

    /**
     * 查詢週末天氣
     * 參數: 城市名稱
     * 返回: 週末天氣預報
     */
    public String getWeekendWeather(String cityName) {
        log.info("查詢週末天氣: {}", cityName);

        try {
            List<DailyForecast> forecasts = weatherService.getWeekendForecast(cityName);

            if (forecasts.isEmpty()) {
                return "無法獲取 " + cityName + " 的週末天氣";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(cityName).append(" 週末天氣預報:\n");

            for (DailyForecast forecast : forecasts) {
                sb.append(String.format(
                        "【%s】\n" +
                        "高溫: %.1f°C 低溫: %.1f°C\n" +
                        "白天: %s 夜晚: %s\n" +
                        "降雨機率: %d%%\n" +
                        "風速: %.1f m/s\n" +
                        "建議: %s\n\n",
                        forecast.getForecastDate(),
                        forecast.getMaxTemperature(),
                        forecast.getMinTemperature(),
                        forecast.getDayCondition(),
                        forecast.getNightCondition(),
                        forecast.getPrecipitationProbability(),
                        forecast.getMaxWindSpeed(),
                        forecast.getRecommendation()
                ));
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("查詢週末天氣失敗", e);
            return "無法獲取 " + cityName + " 的週末天氣";
        }
    }

    /**
     * 檢查是否需要帶雨具
     * 參數: 城市名稱
     * 返回: 帶雨具建議
     */
    public String shouldBringUmbrella(String cityName) {
        log.info("檢查是否需要帶雨具: {}", cityName);

        try {
            boolean willRain = weatherService.willRainToday(cityName);
            if (willRain) {
                return "是的，" + cityName + " 今日降雨概率較高，建議攜帶雨具";
            } else {
                return "否，" + cityName + " 今日降雨概率低，不需要帶雨具";
            }
        } catch (Exception e) {
            log.error("檢查失敗", e);
            return "無法獲取 " + cityName + " 的天氣信息";
        }
    }

    /**
     * 獲取穿衣建議
     * 參數: 城市名稱
     * 返回: 穿衣建議
     */
    public String getClothingAdvice(String cityName) {
        log.info("查詢穿衣建議: {}", cityName);

        try {
            String advice = weatherService.getClothingRecommendation(cityName);
            return "針對 " + cityName + " 的穿衣建議:\n" + advice;
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return "無法獲取 " + cityName + " 的穿衣建議";
        }
    }

    /**
     * 比較多個城市的天氣
     * 參數: 城市名稱列表 (逗號分隔)
     * 返回: 城市天氣對比
     */
    public String compareWeather(String cityNames) {
        log.info("比較多城市天氣: {}", cityNames);

        try {
            String[] cities = cityNames.split(",");
            List<String> cityList = Arrays.asList(cities);
            return weatherService.compareWeather(cityList);
        } catch (Exception e) {
            log.error("比較失敗", e);
            return "無法比較城市天氣";
        }
    }

    /**
     * 查詢是否有天氣警告
     * 參數: 城市名稱
     * 返回: 警告信息
     */
    public String checkWeatherAlerts(String cityName) {
        log.info("查詢天氣警告: {}", cityName);

        try {
            WeatherData weatherData = weatherService.getWeatherByCity(cityName);

            if (weatherData.getAlerts() == null || weatherData.getAlerts().isEmpty()) {
                return cityName + " 目前無天氣警告";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(cityName).append(" 天氣警告:\n");
            sb.append("=".repeat(40)).append("\n");

            for (WeatherAlert alert : weatherData.getAlerts()) {
                sb.append(String.format(
                        "【%s】%s\n" +
                        "內容: %s\n" +
                        "生效期限: %s 至 %s\n" +
                        "發布單位: %s\n\n",
                        alert.getAlertType(),
                        alert.getAlertLevel(),
                        alert.getContent(),
                        alert.getEffectiveTime(),
                        alert.getExpiryTime(),
                        alert.getIssuedBy()
                ));
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("查詢警告失敗", e);
            return "無法獲取 " + cityName + " 的天氣警告信息";
        }
    }

    /**
     * 根據坐標查詢天氣
     * 參數: 緯度, 經度
     * 返回: 該地點的天氣信息
     */
    public String getWeatherByCoordinates(Double latitude, Double longitude) {
        log.info("根據坐標查詢天氣: lat={}, lon={}", latitude, longitude);

        try {
            WeatherData weatherData = weatherService.getWeatherByCoordinates(latitude, longitude);
            CurrentWeather current = weatherData.getCurrentWeather();

            return String.format(
                    "坐標 (%.4f, %.4f) 的天氣:\n" +
                    "溫度: %.1f°C\n" +
                    "天氣狀況: %s\n" +
                    "風速: %.1f m/s\n" +
                    "濕度: %d%%",
                    latitude,
                    longitude,
                    current.getTemperature(),
                    current.getCondition(),
                    current.getWindSpeed(),
                    current.getHumidity()
            );
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return "無法獲取該坐標的天氣信息";
        }
    }

    /**
     * 獲取特定時間的天氣預測
     * 參數: 城市名稱, 日期 (格式: yyyy-MM-dd)
     * 返回: 該日期的詳細天氣預測
     */
    public String getWeatherForDate(String cityName, String date) {
        log.info("查詢特定日期天氣: {} on {}", cityName, date);

        try {
            WeatherData weatherData = weatherService.getWeatherByCity(cityName);

            // 查找匹配的日期
            for (DailyForecast forecast : weatherData.getDailyForecasts()) {
                if (forecast.getForecastDate().equals(date)) {
                    return String.format(
                            "%s 於 %s 的天氣預測:\n" +
                            "高溫: %.1f°C 低溫: %.1f°C\n" +
                            "白天: %s | 夜晚: %s\n" +
                            "降雨機率: %d%%\n" +
                            "風速: %.1f m/s\n" +
                            "濕度: %d-%d%%\n" +
                            "紫外線: %d\n" +
                            "建議: %s",
                            cityName, date,
                            forecast.getMaxTemperature(),
                            forecast.getMinTemperature(),
                            forecast.getDayCondition(),
                            forecast.getNightCondition(),
                            forecast.getPrecipitationProbability(),
                            forecast.getMaxWindSpeed(),
                            forecast.getMinHumidity(),
                            forecast.getMaxHumidity(),
                            forecast.getUvIndex(),
                            forecast.getRecommendation()
                    );
                }
            }

            return "無法找到 " + date + " 的天氣預測";
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return "無法獲取該日期的天氣預測";
        }
    }
}
