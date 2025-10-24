package com.example.service;

import com.example.dto.WeatherData;
import com.example.dto.WeatherData.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 天氣服務 - 用於 5.9 天氣 API 集成
 * 提供台灣地區的天氣預報查詢功能
 * 實現數據緩存和模擬 API 調用
 */
@Slf4j
@Service
public class WeatherService {

    /** 城市坐標映射 */
    private static final Map<String, WeatherLocation> CITIES = new HashMap<>();

    /** 天氣數據緩存 */
    private final Map<String, WeatherCacheEntry> weatherCache = new HashMap<>();

    /** 緩存過期時間 (毫秒) */
    private static final long CACHE_TTL = 30 * 60 * 1000; // 30 分鐘

    static {
        // 初始化台灣主要城市的坐標
        CITIES.put("台北", new WeatherLocation("台北市", 25.0443, 121.5091));
        CITIES.put("台北市", new WeatherLocation("台北市", 25.0443, 121.5091));
        CITIES.put("台中", new WeatherLocation("台中市", 24.1477, 120.6736));
        CITIES.put("台中市", new WeatherLocation("台中市", 24.1477, 120.6736));
        CITIES.put("高雄", new WeatherLocation("高雄市", 22.6228, 120.3014));
        CITIES.put("高雄市", new WeatherLocation("高雄市", 22.6228, 120.3014));
        CITIES.put("新竹", new WeatherLocation("新竹市", 24.8138, 120.9675));
        CITIES.put("新竹市", new WeatherLocation("新竹市", 24.8138, 120.9675));
        CITIES.put("台南", new WeatherLocation("台南市", 22.9921, 120.2119));
        CITIES.put("台南市", new WeatherLocation("台南市", 22.9921, 120.2119));
    }

    /**
     * 城市位置信息
     */
    private static class WeatherLocation {
        String name;
        Double lat;
        Double lon;

        WeatherLocation(String name, Double lat, Double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }

    /**
     * 天氣數據緩存項
     */
    private static class WeatherCacheEntry {
        WeatherData data;
        long timestamp;

        WeatherCacheEntry(WeatherData data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL;
        }
    }

    /**
     * 根據城市名稱查詢天氣
     */
    public WeatherData getWeatherByCity(String cityName) {
        log.info("查詢城市天氣: {}", cityName);

        // 檢查緩存
        WeatherCacheEntry cacheEntry = weatherCache.get(cityName);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            log.debug("返回緩存的天氣數據: {}", cityName);
            return cacheEntry.data;
        }

        // 查詢城市坐標
        WeatherLocation location = CITIES.get(cityName);
        if (location == null) {
            throw new IllegalArgumentException("未支持的城市: " + cityName);
        }

        // 調用 API 獲取天氣數據（實現中為模擬數據）
        WeatherData weatherData = fetchWeatherData(location);

        // 緩存結果
        weatherCache.put(cityName, new WeatherCacheEntry(weatherData));

        return weatherData;
    }

    /**
     * 根據坐標查詢天氣
     */
    public WeatherData getWeatherByCoordinates(Double latitude, Double longitude) {
        log.info("查詢坐標天氣: lat={}, lon={}", latitude, longitude);

        String key = latitude + "," + longitude;

        // 檢查緩存
        WeatherCacheEntry cacheEntry = weatherCache.get(key);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            return cacheEntry.data;
        }

        WeatherLocation location = new WeatherLocation("查詢地點", latitude, longitude);
        WeatherData weatherData = fetchWeatherData(location);

        weatherCache.put(key, new WeatherCacheEntry(weatherData));

        return weatherData;
    }

    /**
     * 獲取週末天氣預報
     */
    public List<DailyForecast> getWeekendForecast(String cityName) {
        log.info("查詢周末天氣預報: {}", cityName);

        WeatherData weatherData = getWeatherByCity(cityName);

        // 返回週末預報 (假設週六和週日)
        List<DailyForecast> weekendForecasts = new ArrayList<>();
        if (weatherData.getDailyForecasts().size() >= 2) {
            weekendForecasts.add(weatherData.getDailyForecasts().get(5)); // 週六
            weekendForecasts.add(weatherData.getDailyForecasts().get(6)); // 週日
        }

        return weekendForecasts;
    }

    /**
     * 獲取本週天氣概況
     */
    public String getWeeklyWeatherSummary(String cityName) {
        log.info("查詢本週天氣概況: {}", cityName);

        WeatherData weatherData = getWeatherByCity(cityName);

        StringBuilder summary = new StringBuilder();
        summary.append(cityName).append(" 本週天氣概況:\n");
        summary.append("=".repeat(30)).append("\n");

        for (int i = 0; i < Math.min(7, weatherData.getDailyForecasts().size()); i++) {
            DailyForecast forecast = weatherData.getDailyForecasts().get(i);
            summary.append(String.format(
                    "【%s】高溫 %s°C 低溫 %s°C | %s → %s | 降雨機率 %d%%\n",
                    forecast.getForecastDate(),
                    forecast.getMaxTemperature(),
                    forecast.getMinTemperature(),
                    forecast.getDayCondition(),
                    forecast.getNightCondition(),
                    forecast.getPrecipitationProbability()
            ));

            if (forecast.getRecommendation() != null && !forecast.getRecommendation().isEmpty()) {
                summary.append("建議: ").append(forecast.getRecommendation()).append("\n");
            }
        }

        return summary.toString();
    }

    /**
     * 比較多個城市的天氣
     */
    public String compareWeather(List<String> cityNames) {
        log.info("比較多個城市的天氣");

        StringBuilder comparison = new StringBuilder();
        comparison.append("城市天氣對比\n");
        comparison.append("=".repeat(50)).append("\n");
        comparison.append(String.format("%-10s | 溫度 | 體感 | 狀況 | 風速 | 濕度\n", "城市"));
        comparison.append("-".repeat(50)).append("\n");

        for (String cityName : cityNames) {
            try {
                WeatherData weatherData = getWeatherByCity(cityName);
                CurrentWeather current = weatherData.getCurrentWeather();

                comparison.append(String.format(
                        "%-10s | %3d° | %3d° | %-4s | %.1f | %d%%\n",
                        cityName,
                        current.getTemperature().intValue(),
                        current.getFeelsLike().intValue(),
                        current.getCondition(),
                        current.getWindSpeed(),
                        current.getHumidity()
                ));
            } catch (Exception e) {
                log.warn("獲取 {} 的天氣數據失敗", cityName, e);
                comparison.append(String.format("%-10s | 數據不可用\n", cityName));
            }
        }

        return comparison.toString();
    }

    /**
     * 檢查是否會下雨
     */
    public boolean willRainToday(String cityName) {
        WeatherData weatherData = getWeatherByCity(cityName);
        CurrentWeather current = weatherData.getCurrentWeather();
        return current.getPrecipitationProbability() > 50;
    }

    /**
     * 獲取今日穿衣建議
     */
    public String getClothingRecommendation(String cityName) {
        log.info("查詢穿衣建議: {}", cityName);

        WeatherData weatherData = getWeatherByCity(cityName);
        CurrentWeather current = weatherData.getCurrentWeather();
        Double temp = current.getTemperature();

        String recommendation;
        if (temp < 10) {
            recommendation = "建議穿著厚重外套和毛衣";
        } else if (temp < 15) {
            recommendation = "建議穿著夾克或薄外套";
        } else if (temp < 20) {
            recommendation = "建議穿著長袖衣物";
        } else if (temp < 25) {
            recommendation = "建議穿著短袖或輕薄長袖";
        } else {
            recommendation = "建議穿著輕薄衣物，注意防曬";
        }

        if (willRainToday(cityName)) {
            recommendation += "，建議攜帶雨具";
        }

        if (current.getUvIndex() > 7) {
            recommendation += "，紫外線強，建議做好防曬";
        }

        return recommendation;
    }

    /**
     * 清空緩存
     */
    public void clearCache() {
        weatherCache.clear();
        log.info("已清空天氣數據緩存");
    }

    /**
     * 模擬從 API 獲取天氣數據
     */
    private WeatherData fetchWeatherData(WeatherLocation location) {
        log.debug("調用天氣 API: {}", location.name);

        // 模擬當前天氣
        CurrentWeather current = CurrentWeather.builder()
                .temperature(20.0 + Math.random() * 10)
                .feelsLike(19.0 + Math.random() * 10)
                .condition(getRandomCondition())
                .description("天氣狀況描述")
                .humidity((int)(50 + Math.random() * 40))
                .pressure(1013 + (int)(Math.random() * 20 - 10))
                .windSpeed(2.0 + Math.random() * 10)
                .windDirection((int)(Math.random() * 360))
                .windDirectionName(getWindDirectionName((int)(Math.random() * 360)))
                .visibility(10.0)
                .precipitationProbability((int)(Math.random() * 100))
                .uvIndex((int)(Math.random() * 12))
                .updateTime(LocalDateTime.now())
                .build();

        // 模擬逐小時預報
        List<HourlyForecast> hourlyForecasts = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hourlyForecasts.add(HourlyForecast.builder()
                    .forecastTime(LocalDateTime.now().plusHours(i))
                    .temperature(20.0 + Math.random() * 8)
                    .feelsLike(19.0 + Math.random() * 8)
                    .condition(getRandomCondition())
                    .precipitationProbability((int)(Math.random() * 100))
                    .windSpeed(1.0 + Math.random() * 10)
                    .precipitation(Math.random() * 5)
                    .humidity((int)(50 + Math.random() * 40))
                    .build());
        }

        // 模擬逐日預報
        List<DailyForecast> dailyForecasts = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dailyForecasts.add(DailyForecast.builder()
                    .forecastDate(LocalDateTime.now().plusDays(i).format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .maxTemperature(25.0 + Math.random() * 5)
                    .minTemperature(15.0 + Math.random() * 5)
                    .avgTemperature(20.0 + Math.random() * 5)
                    .dayCondition(getRandomCondition())
                    .nightCondition(getRandomCondition())
                    .precipitationProbability((int)(Math.random() * 100))
                    .totalPrecipitation(Math.random() * 20)
                    .maxWindSpeed(5.0 + Math.random() * 15)
                    .maxHumidity((int)(70 + Math.random() * 30))
                    .minHumidity((int)(40 + Math.random() * 30))
                    .uvIndex((int)(Math.random() * 12))
                    .sunrise("06:30")
                    .sunset("18:45")
                    .recommendation(getRecommendation(i))
                    .build());
        }

        // 模擬警告
        List<WeatherAlert> alerts = new ArrayList<>();
        if (Math.random() > 0.7) {
            alerts.add(WeatherAlert.builder()
                    .alertType("豪雨")
                    .alertLevel("警戒")
                    .content("未來24小時可能有局部大雨")
                    .effectiveTime(LocalDateTime.now())
                    .expiryTime(LocalDateTime.now().plusHours(24))
                    .issuedBy("中央氣象署")
                    .build());
        }

        return WeatherData.builder()
                .locationName(location.name)
                .latitude(location.lat)
                .longitude(location.lon)
                .dataTime(LocalDateTime.now())
                .currentWeather(current)
                .hourlyForecasts(hourlyForecasts)
                .dailyForecasts(dailyForecasts)
                .alerts(alerts)
                .build();
    }

    private String getRandomCondition() {
        String[] conditions = {"晴天", "多雲", "陰天", "小雨", "中雨", "大雨", "雷陣雨"};
        return conditions[(int)(Math.random() * conditions.length)];
    }

    private String getWindDirectionName(int degrees) {
        if (degrees < 22.5 || degrees >= 337.5) return "北風";
        else if (degrees < 67.5) return "東北風";
        else if (degrees < 112.5) return "東風";
        else if (degrees < 157.5) return "東南風";
        else if (degrees < 202.5) return "南風";
        else if (degrees < 247.5) return "西南風";
        else if (degrees < 292.5) return "西風";
        else return "西北風";
    }

    private String getRecommendation(int daysAhead) {
        if (daysAhead == 0) {
            return "今日天氣良好，可以外出活動";
        } else if (daysAhead == 1) {
            return "明日可能有雨，建議攜帶雨具";
        } else {
            return "天氣預測中，請持續關注";
        }
    }
}
