package com.example.memory.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * 天氣查詢工具集
 *
 * 提供天氣相關的工具方法
 * 可供LLM調用以獲取實時天氣信息
 */
@Slf4j
@Component
public class WeatherTools {

    /**
     * 模擬的天氣數據
     */
    private static final Map<String, WeatherInfo> WEATHER_DATA = new HashMap<>();

    static {
        // 初始化模擬天氣數據
        WEATHER_DATA.put("台北", new WeatherInfo("台北", 28, "晴", "東南風", 65));
        WEATHER_DATA.put("台中", new WeatherInfo("台中", 26, "多雲", "南風", 70));
        WEATHER_DATA.put("台南", new WeatherInfo("台南", 30, "晴", "西南風", 60));
        WEATHER_DATA.put("高雄", new WeatherInfo("高雄", 31, "晴", "南風", 55));
        WEATHER_DATA.put("新竹", new WeatherInfo("新竹", 25, "陰", "東風", 75));
    }

    /**
     * 獲取指定城市的當前天氣
     *
     * @param city 城市名稱
     * @return 天氣信息
     */
    public String getCurrentWeather(String city) {
        log.info("Getting current weather for city: {}", city);

        WeatherInfo weather = WEATHER_DATA.getOrDefault(city,
            new WeatherInfo(city, 0, "未知", "未知", 0));

        String result = String.format(
            "城市:%s | 溫度:%d°C | 天氣:%s | 風向:%s | 濕度:%d%%",
            weather.getCity(),
            weather.getTemperature(),
            weather.getCondition(),
            weather.getWind(),
            weather.getHumidity()
        );

        log.debug("Weather result: {}", result);
        return result;
    }

    /**
     * 獲取多個城市的天氣信息
     *
     * @param cities 城市列表
     * @return 天氣信息列表
     */
    public String getWeatherForMultipleCities(String... cities) {
        log.info("Getting weather for multiple cities: {}", Arrays.toString(cities));

        StringBuilder result = new StringBuilder();
        for (String city : cities) {
            result.append(getCurrentWeather(city)).append("\n");
        }

        return result.toString();
    }

    /**
     * 獲取所有支援的城市列表
     *
     * @return 城市列表
     */
    public String getSupportedCities() {
        log.info("Getting supported cities list");
        return String.join(", ", WEATHER_DATA.keySet());
    }

    /**
     * 根據天氣狀況獲取建議
     *
     * @param city 城市名稱
     * @return 出行建議
     */
    public String getWeatherAdvice(String city) {
        log.info("Getting weather advice for city: {}", city);

        WeatherInfo weather = WEATHER_DATA.get(city);
        if (weather == null) {
            return "該城市不在我們的數據庫中";
        }

        StringBuilder advice = new StringBuilder();
        advice.append(String.format("%s 的天氣建議: ", city));

        if (weather.getTemperature() >= 30) {
            advice.append("氣溫較高，出門請做好防曬並帶足飲用水。");
        } else if (weather.getTemperature() < 10) {
            advice.append("氣溫較低，出門請穿上厚實的外套。");
        }

        if ("雨".equals(weather.getCondition()) || "陰".equals(weather.getCondition())) {
            advice.append("可能有雨，請帶好傘具。");
        }

        if (weather.getHumidity() > 70) {
            advice.append("濕度較高，悶熱潮濕，請注意透風。");
        }

        return advice.toString();
    }

    /**
     * 天氣信息內部類
     */
    public static class WeatherInfo {
        private String city;
        private int temperature;
        private String condition;
        private String wind;
        private int humidity;

        public WeatherInfo(String city, int temperature, String condition, String wind, int humidity) {
            this.city = city;
            this.temperature = temperature;
            this.condition = condition;
            this.wind = wind;
            this.humidity = humidity;
        }

        public String getCity() { return city; }
        public int getTemperature() { return temperature; }
        public String getCondition() { return condition; }
        public String getWind() { return wind; }
        public int getHumidity() { return humidity; }
    }
}
