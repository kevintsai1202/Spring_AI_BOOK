package com.example.controller;

import com.example.dto.WeatherData;
import com.example.service.WeatherService;
import com.example.tools.WeatherTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 天氣控制器 - 提供天氣相關的 REST API
 * 支持自然語言查詢和直接 API 調用
 * 用於 5.9 天氣 API 集成章節
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;
    private final WeatherTools weatherTools;

    /**
     * 查詢城市當前天氣
     * GET /api/v1/weather/current?city=台北
     *
     * 示例: /api/v1/weather/current?city=台北
     */
    @GetMapping("/current")
    public ResponseEntity<WeatherData> getCurrentWeather(@RequestParam String city) {
        log.info("查詢當前天氣: {}", city);

        try {
            WeatherData weatherData = weatherService.getWeatherByCity(city);
            return ResponseEntity.ok(weatherData);
        } catch (IllegalArgumentException e) {
            log.error("城市不支持: {}", city);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 查詢城市天氣簡述
     * GET /api/v1/weather/summary?city=台北
     */
    @GetMapping("/summary")
    public ResponseEntity<String> getWeatherSummary(@RequestParam String city) {
        log.info("查詢天氣簡述: {}", city);

        try {
            String summary = weatherTools.getCurrentWeather(city);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return ResponseEntity.internalServerError()
                    .body("無法獲取天氣信息: " + e.getMessage());
        }
    }

    /**
     * 查詢本週天氣預報
     * GET /api/v1/weather/forecast?city=台北
     */
    @GetMapping("/forecast")
    public ResponseEntity<String> getWeekForecast(@RequestParam String city) {
        log.info("查詢週預報: {}", city);

        try {
            String forecast = weatherTools.getWeekForecast(city);
            return ResponseEntity.ok(forecast);
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return ResponseEntity.internalServerError()
                    .body("無法獲取預報信息: " + e.getMessage());
        }
    }

    /**
     * 查詢週末天氣
     * GET /api/v1/weather/weekend?city=台北
     */
    @GetMapping("/weekend")
    public ResponseEntity<String> getWeekendWeather(@RequestParam String city) {
        log.info("查詢週末天氣: {}", city);

        try {
            String weather = weatherTools.getWeekendWeather(city);
            return ResponseEntity.ok(weather);
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return ResponseEntity.internalServerError()
                    .body("無法獲取週末天氣: " + e.getMessage());
        }
    }

    /**
     * 檢查是否需要帶傘
     * GET /api/v1/weather/umbrella?city=台北
     */
    @GetMapping("/umbrella")
    public ResponseEntity<String> shouldBringUmbrella(@RequestParam String city) {
        log.info("檢查帶傘建議: {}", city);

        try {
            String advice = weatherTools.shouldBringUmbrella(city);
            return ResponseEntity.ok(advice);
        } catch (Exception e) {
            log.error("檢查失敗", e);
            return ResponseEntity.internalServerError()
                    .body("無法檢查帶傘建議: " + e.getMessage());
        }
    }

    /**
     * 獲取穿衣建議
     * GET /api/v1/weather/clothing?city=台北
     */
    @GetMapping("/clothing")
    public ResponseEntity<String> getClothingAdvice(@RequestParam String city) {
        log.info("查詢穿衣建議: {}", city);

        try {
            String advice = weatherTools.getClothingAdvice(city);
            return ResponseEntity.ok(advice);
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return ResponseEntity.internalServerError()
                    .body("無法獲取穿衣建議: " + e.getMessage());
        }
    }

    /**
     * 比較多個城市天氣
     * GET /api/v1/weather/compare?cities=台北,台中,高雄
     */
    @GetMapping("/compare")
    public ResponseEntity<String> compareWeather(@RequestParam String cities) {
        log.info("比較城市天氣: {}", cities);

        try {
            String comparison = weatherTools.compareWeather(cities);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            log.error("比較失敗", e);
            return ResponseEntity.internalServerError()
                    .body("無法比較城市天氣");
        }
    }

    /**
     * 查詢天氣警告
     * GET /api/v1/weather/alerts?city=台北
     */
    @GetMapping("/alerts")
    public ResponseEntity<String> getWeatherAlerts(@RequestParam String city) {
        log.info("查詢天氣警告: {}", city);

        try {
            String alerts = weatherTools.checkWeatherAlerts(city);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return ResponseEntity.internalServerError()
                    .body("無法獲取警告信息: " + e.getMessage());
        }
    }

    /**
     * 根據坐標查詢天氣
     * GET /api/v1/weather/location?lat=25.0443&lon=121.5091
     */
    @GetMapping("/location")
    public ResponseEntity<WeatherData> getWeatherByCoordinates(
            @RequestParam Double lat,
            @RequestParam Double lon) {
        log.info("根據坐標查詢天氣: lat={}, lon={}", lat, lon);

        try {
            WeatherData weatherData = weatherService.getWeatherByCoordinates(lat, lon);
            return ResponseEntity.ok(weatherData);
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 查詢特定日期的天氣預測
     * GET /api/v1/weather/date-forecast?city=台北&date=2024-10-25
     *
     * 示例: /api/v1/weather/date-forecast?city=台北&date=2024-10-25
     */
    @GetMapping("/date-forecast")
    public ResponseEntity<String> getWeatherForDate(
            @RequestParam String city,
            @RequestParam String date) {
        log.info("查詢特定日期天氣: {} on {}", city, date);

        try {
            String weather = weatherTools.getWeatherForDate(city, date);
            return ResponseEntity.ok(weather);
        } catch (Exception e) {
            log.error("查詢失敗", e);
            return ResponseEntity.internalServerError()
                    .body("無法獲取該日期的天氣預測: " + e.getMessage());
        }
    }

    /**
     * 自然語言天氣查詢 (使用 AI)
     * POST /api/v1/weather/ask
     *
     * 示例:
     * {
     *   "question": "台北下週會下雨嗎？"
     * }
     */
    @PostMapping("/ask")
    public ResponseEntity<String> naturalLanguageQuery(@RequestBody Map<String, String> request) {
        log.info("自然語言查詢");

        String question = request.get("question");

        try {
            // 這裡可以集成 AI/LLM 來理解自然語言並調用適當的工具
            // 暫時返回一個提示信息
            return ResponseEntity.ok("收到查詢: " + question + "\n" +
                    "建議直接使用具體的 API 端點進行查詢");
        } catch (Exception e) {
            log.error("查詢執行失敗", e);
            return ResponseEntity.internalServerError()
                    .body("查詢執行失敗");
        }
    }

    /**
     * 清空天氣數據緩存
     * POST /api/v1/weather/cache/clear
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearCache() {
        log.info("清空天氣數據緩存");

        try {
            weatherService.clearCache();
            return ResponseEntity.ok("天氣數據緩存已清空");
        } catch (Exception e) {
            log.error("清空緩存失敗", e);
            return ResponseEntity.internalServerError()
                    .body("清空緩存失敗");
        }
    }

    /**
     * 獲取支持的城市列表
     * GET /api/v1/weather/supported-cities
     */
    @GetMapping("/supported-cities")
    public ResponseEntity<String> getSupportedCities() {
        log.info("獲取支持的城市列表");

        String cities = "支持的城市列表:\n" +
                "- 台北 / 台北市\n" +
                "- 台中 / 台中市\n" +
                "- 高雄 / 高雄市\n" +
                "- 新竹 / 新竹市\n" +
                "- 台南 / 台南市";

        return ResponseEntity.ok(cities);
    }

    /**
     * 健康檢查
     * GET /api/v1/weather/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("{\"status\": \"UP\", \"service\": \"weather-service\"}");
    }
}
