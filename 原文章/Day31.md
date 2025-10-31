# 使用 Spring AI 打造企業 RAG 知識庫【31】- Function Calling 整合即時 API 資料

## 有了AI寫程式都可以偷懶

![AI Development](https://example.com/ai-development.jpg)

還記得前面提到要取得即時資訊只能透過 Function Calling 嗎？網路上一堆教學都使用模擬資料，今天來教大家怎麼抓中央氣象局的資料，並使用最新的 @Tool 註解實現更優雅的 Function Calling。

## 取得中央氣象局開放平台資料

中央氣象局有個資料開放平台：https://opendata.cwa.gov.tw/index

註冊後就能在 **會員資訊－取得授權碼** 拿到自己的授權碼。

![Weather API](https://example.com/weather-api.png)

可以在資料主題找需要的資料，由於我只要抓到目前的氣象資訊，所以選擇 觀測-現在天氣觀測報告。

> 最好找有 API 的，不然需要下載檔案再用讀檔的方式處理

進入後就是 API 常用的 Swagger 網站了。點選 Try it out 輸入授權碼，按下 Execute 就能取得 JSON 資料了。

## 程式碼實作

使用最新的 Spring AI 1.1-SNAPSHOT 和 @Tool 註解實現 Function Calling。

### 1. Weather Service 實現

首先實作一個完整的天氣服務類：

```java
@Service
@Slf4j
public class WeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${cwa.api.key}")
    private String apiKey;
    
    @Value("${cwa.api.base-url:https://opendata.cwa.gov.tw/api/v1/rest/datastore}")
    private String baseUrl;

    public WeatherService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Tool("取得指定地區的目前天氣資訊")
    public WeatherInfo getCurrentWeather(
        @ToolParameter("地區名稱，例如：台北、桃園、台中") String location) {
        
        try {
            String url = baseUrl + "/O-A0003-001?Authorization=" + apiKey;
            
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            
            if (response == null || !response.has("records")) {
                return WeatherInfo.error("無法取得天氣資料");
            }
            
            // 解析天氣資料
            JsonNode records = response.get("records");
            JsonNode locations = records.get("location");
            
            if (locations == null || !locations.isArray()) {
                return WeatherInfo.error("天氣資料格式錯誤");
            }
            
            // 尋找指定地區的天氣資料
            for (JsonNode locationNode : locations) {
                String locationName = locationNode.get("locationName").asText();
                
                if (locationName.contains(location) || location.contains(locationName)) {
                    return parseWeatherData(locationNode, locationName);
                }
            }
            
            return WeatherInfo.notFound("找不到 " + location + " 的天氣資料，請確認地區名稱是否正確");
            
        } catch (Exception e) {
            log.error("取得天氣資料時發生錯誤: {}", e.getMessage(), e);
            return WeatherInfo.error("取得天氣資料失敗：" + e.getMessage());
        }
    }

    @Tool("取得全台各地的溫度排行榜")
    public TemperatureRanking getTemperatureRanking(
        @ToolParameter("排行數量，預設為10") Integer topCount) {
        
        if (topCount == null || topCount <= 0) {
            topCount = 10;
        }
        
        try {
            String url = baseUrl + "/O-A0003-001?Authorization=" + apiKey;
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            
            if (response == null || !response.has("records")) {
                return TemperatureRanking.error("無法取得天氣資料");
            }
            
            List<LocationTemperature> temperatures = new ArrayList<>();
            JsonNode locations = response.get("records").get("location");
            
            for (JsonNode locationNode : locations) {
                String locationName = locationNode.get("locationName").asText();
                JsonNode weatherElements = locationNode.get("weatherElement");
                
                Double temp = extractTemperature(weatherElements);
                if (temp != null) {
                    temperatures.add(new LocationTemperature(locationName, temp));
                }
            }
            
            // 依溫度排序並取前N名
            List<LocationTemperature> highestTemps = temperatures.stream()
                .sorted((a, b) -> Double.compare(b.temperature(), a.temperature()))
                .limit(topCount)
                .collect(Collectors.toList());
            
            List<LocationTemperature> lowestTemps = temperatures.stream()
                .sorted((a, b) -> Double.compare(a.temperature(), b.temperature()))
                .limit(topCount)
                .collect(Collectors.toList());
            
            return new TemperatureRanking(highestTemps, lowestTemps, LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("取得溫度排行榜時發生錯誤: {}", e.getMessage(), e);
            return TemperatureRanking.error("取得溫度排行榜失敗：" + e.getMessage());
        }
    }

    @Tool("取得天氣預報資訊")
    public WeatherForecast getWeatherForecast(
        @ToolParameter("地區名稱") String location,
        @ToolParameter("預報天數，1-7天") Integer days) {
        
        if (days == null || days < 1 || days > 7) {
            days = 3; // 預設3天
        }
        
        try {
            // 使用預報API
            String url = baseUrl + "/F-C0032-001?Authorization=" + apiKey;
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            
            return parseForecastData(response, location, days);
            
        } catch (Exception e) {
            log.error("取得天氣預報時發生錯誤: {}", e.getMessage(), e);
            return WeatherForecast.error("取得天氣預報失敗：" + e.getMessage());
        }
    }

    private WeatherInfo parseWeatherData(JsonNode locationNode, String locationName) {
        try {
            JsonNode weatherElements = locationNode.get("weatherElement");
            LocalDateTime observationTime = LocalDateTime.now(); // 可以從API回應中解析實際時間
            
            Double temperature = extractTemperature(weatherElements);
            Double humidity = extractHumidity(weatherElements);
            String weather = extractWeatherDescription(weatherElements);
            Double rainfall = extractRainfall(weatherElements);
            String windDirection = extractWindDirection(weatherElements);
            Double windSpeed = extractWindSpeed(weatherElements);
            
            return new WeatherInfo(
                locationName,
                temperature,
                humidity,
                weather,
                rainfall,
                windDirection,
                windSpeed,
                observationTime,
                true,
                null
            );
            
        } catch (Exception e) {
            return WeatherInfo.error("解析天氣資料失敗：" + e.getMessage());
        }
    }

    private Double extractTemperature(JsonNode weatherElements) {
        return extractElementValue(weatherElements, "TEMP");
    }

    private Double extractHumidity(JsonNode weatherElements) {
        return extractElementValue(weatherElements, "HUMD");
    }

    private Double extractRainfall(JsonNode weatherElements) {
        return extractElementValue(weatherElements, "24R");
    }

    private Double extractWindSpeed(JsonNode weatherElements) {
        return extractElementValue(weatherElements, "WDSD");
    }

    private String extractWeatherDescription(JsonNode weatherElements) {
        JsonNode element = findWeatherElement(weatherElements, "Weather");
        if (element != null && element.has("elementValue")) {
            return element.get("elementValue").asText();
        }
        return "晴朗";
    }

    private String extractWindDirection(JsonNode weatherElements) {
        JsonNode element = findWeatherElement(weatherElements, "WDIR");
        if (element != null && element.has("elementValue")) {
            return element.get("elementValue").asText();
        }
        return "無風";
    }

    private Double extractElementValue(JsonNode weatherElements, String elementName) {
        JsonNode element = findWeatherElement(weatherElements, elementName);
        if (element != null && element.has("elementValue")) {
            try {
                return element.get("elementValue").asDouble();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private JsonNode findWeatherElement(JsonNode weatherElements, String elementName) {
        if (weatherElements != null && weatherElements.isArray()) {
            for (JsonNode element : weatherElements) {
                if (elementName.equals(element.get("elementName").asText())) {
                    return element;
                }
            }
        }
        return null;
    }

    // 天氣資訊資料類別
    public record WeatherInfo(
        String location,
        Double temperature,
        Double humidity,
        String weather,
        Double rainfall,
        String windDirection,
        Double windSpeed,
        LocalDateTime observationTime,
        boolean success,
        String errorMessage
    ) {
        public static WeatherInfo error(String message) {
            return new WeatherInfo(null, null, null, null, null, null, null, 
                                 LocalDateTime.now(), false, message);
        }
        
        public static WeatherInfo notFound(String message) {
            return new WeatherInfo(null, null, null, null, null, null, null, 
                                 LocalDateTime.now(), false, message);
        }
        
        @Override
        public String toString() {
            if (!success) {
                return errorMessage;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("📍 地點：").append(location).append("\n");
            sb.append("🌡️ 溫度：").append(temperature != null ? temperature + "°C" : "無資料").append("\n");
            sb.append("💧 濕度：").append(humidity != null ? humidity + "%" : "無資料").append("\n");
            sb.append("☁️ 天氣：").append(weather != null ? weather : "無資料").append("\n");
            sb.append("🌧️ 降雨量：").append(rainfall != null ? rainfall + "mm" : "無資料").append("\n");
            sb.append("💨 風向：").append(windDirection != null ? windDirection : "無資料").append("\n");
            sb.append("🌪️ 風速：").append(windSpeed != null ? windSpeed + "m/s" : "無資料").append("\n");
            sb.append("⏰ 觀測時間：").append(observationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            
            return sb.toString();
        }
    }

    public record LocationTemperature(String location, Double temperature) {}

    public record TemperatureRanking(
        List<LocationTemperature> highest,
        List<LocationTemperature> lowest,
        LocalDateTime timestamp
    ) {
        public static TemperatureRanking error(String message) {
            return new TemperatureRanking(List.of(), List.of(), LocalDateTime.now());
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("🌡️ 全台溫度排行榜 (").append(timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(")\n\n");
            
            sb.append("🔥 最高溫地區：\n");
            for (int i = 0; i < highest.size(); i++) {
                LocationTemperature loc = highest.get(i);
                sb.append(String.format("%d. %s: %.1f°C\n", i + 1, loc.location(), loc.temperature()));
            }
            
            sb.append("\n❄️ 最低溫地區：\n");
            for (int i = 0; i < lowest.size(); i++) {
                LocationTemperature loc = lowest.get(i);
                sb.append(String.format("%d. %s: %.1f°C\n", i + 1, loc.location(), loc.temperature()));
            }
            
            return sb.toString();
        }
    }

    public record WeatherForecast(
        String location,
        List<DailyForecast> forecasts,
        boolean success,
        String errorMessage
    ) {
        public static WeatherForecast error(String message) {
            return new WeatherForecast(null, List.of(), false, message);
        }
    }

    public record DailyForecast(
        LocalDate date,
        String weather,
        Integer minTemp,
        Integer maxTemp,
        String description
    ) {}
}
```

### 2. 配置檔案

`application.yml`

```yaml
cwa:
  api:
    key: ${CWA_API_KEY}
    base-url: https://opendata.cwa.gov.tw/api/v1/rest/datastore
    timeout: 30s
    
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.1  # 較低溫度確保準確性
          
# 外部 API 配置
external-api:
  weather:
    retry:
      max-attempts: 3
      delay: 1000ms
    cache:
      ttl: 300s  # 5分鐘快取
```

### 3. 控制器實現

使用最新的 ChatClient API 整合 Function Calling：

```java
@RestController
@RequestMapping("/ai/weather")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Weather AI", description = "AI 天氣查詢服務")
public class WeatherController {
    
    private final ChatClient chatClient;
    private final WeatherService weatherService;
    
    @Operation(summary = "智能天氣查詢", description = "使用自然語言查詢天氣資訊")
    @GetMapping("/chat")
    public ResponseEntity<WeatherChatResponse> chatWeather(
            @Parameter(description = "天氣查詢問題", example = "台北現在天氣如何？")
            @RequestParam String question) {
        
        Instant startTime = Instant.now();
        
        try {            
            String response = chatClient.prompt()
                .user(question)
                .functions("getCurrentWeather", "getTemperatureRanking", "getWeatherForecast")
                .call()
                .content();
            
            Duration duration = Duration.between(startTime, Instant.now());
            
            WeatherChatResponse chatResponse = new WeatherChatResponse(
                question,
                response,
                true,
                null,
                duration.toMillis()
            );
            
            return ResponseEntity.ok(chatResponse);
            
        } catch (Exception e) {
            log.error("天氣查詢失敗: {}", e.getMessage(), e);
            
            Duration duration = Duration.between(startTime, Instant.now());
            
            WeatherChatResponse errorResponse = new WeatherChatResponse(
                question,
                null,
                false,
                "天氣查詢失敗：" + e.getMessage(),
                duration.toMillis()
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
    
    @Operation(summary = "直接取得天氣資訊")
    @GetMapping("/current")
    public ResponseEntity<WeatherService.WeatherInfo> getCurrentWeather(
            @Parameter(description = "地區名稱") @RequestParam String location) {
        
        WeatherService.WeatherInfo weatherInfo = weatherService.getCurrentWeather(location);
        
        if (weatherInfo.success()) {
            return ResponseEntity.ok(weatherInfo);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(weatherInfo);
        }
    }
    
    @Operation(summary = "取得溫度排行榜")
    @GetMapping("/temperature-ranking")
    public ResponseEntity<WeatherService.TemperatureRanking> getTemperatureRanking(
            @Parameter(description = "排行數量") @RequestParam(defaultValue = "10") Integer topCount) {
        
        WeatherService.TemperatureRanking ranking = weatherService.getTemperatureRanking(topCount);
        return ResponseEntity.ok(ranking);
    }
    
    public record WeatherChatResponse(
        String question,
        String answer,
        boolean success,
        String errorMessage,
        long responseTimeMs
    ) {}
}
```

### 4. 快取和重試機制

```java
@Configuration
@EnableCaching
@EnableRetry
public class WeatherApiConfig {
    
    @Bean
    public RestTemplate weatherRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // 設定連接超時
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        
        restTemplate.setRequestFactory(factory);
        
        // 添加攔截器記錄請求
        restTemplate.getInterceptors().add((request, body, execution) -> {
            log.debug("Calling weather API: {}", request.getURI());
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }
    
    @Bean
    public CacheManager weatherCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("weather-data");
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)  // 5分鐘過期
            .recordStats());
        return cacheManager;
    }
}

// 在 WeatherService 中添加快取註解
@Service
public class WeatherService {
    
    @Cacheable(value = "weather-data", key = "#location", cacheManager = "weatherCacheManager")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Tool("取得指定地區的目前天氣資訊")
    public WeatherInfo getCurrentWeather(String location) {
        // 原有實現
    }
    
    @Recover
    public WeatherInfo recoverGetCurrentWeather(Exception e, String location) {
        log.error("重試後仍無法取得天氣資料: {}", e.getMessage());
        return WeatherInfo.error("天氣服務暫時無法使用，請稍後再試");
    }
}
```

### 5. 監控和指標

```java
@Component
@RequiredArgsConstructor
public class WeatherServiceMetrics {
    
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void handleWeatherRequest(WeatherRequestEvent event) {
        Counter.builder("weather.api.requests")
            .tag("location", event.getLocation())
            .tag("success", String.valueOf(event.isSuccess()))
            .register(meterRegistry)
            .increment();
    }
    
    @EventListener
    public void handleApiResponse(WeatherApiResponseEvent event) {
        Timer.builder("weather.api.response.time")
            .tag("endpoint", event.getEndpoint())
            .register(meterRegistry)
            .record(event.getDuration(), TimeUnit.MILLISECONDS);
    }
}

@Component
public class WeatherServiceAspect {
    
    private final ApplicationEventPublisher eventPublisher;
    
    @Around("@annotation(tool)")
    public Object measureWeatherApiCall(ProceedingJoinPoint joinPoint, Tool tool) throws Throwable {
        Instant start = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        try {
            Object result = joinPoint.proceed();
            
            Duration duration = Duration.between(start, Instant.now());
            
            // 發布成功事件
            eventPublisher.publishEvent(new WeatherApiResponseEvent(
                methodName, duration.toMillis(), true));
            
            return result;
            
        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            
            // 發布失敗事件
            eventPublisher.publishEvent(new WeatherApiResponseEvent(
                methodName, duration.toMillis(), false));
            
            throw e;
        }
    }
}
```

### 6. 健康檢查

```java
@Component
public class WeatherServiceHealthIndicator implements HealthIndicator {
    
    private final WeatherService weatherService;
    
    @Override
    public Health health() {
        try {
            // 執行簡單的API測試
            WeatherService.WeatherInfo testResult = weatherService.getCurrentWeather("台北");
            
            if (testResult.success()) {
                return Health.up()
                    .withDetail("status", "Weather API is accessible")
                    .withDetail("test_location", "台北")
                    .withDetail("response_time", "< 30s")
                    .build();
            } else {
                return Health.down()
                    .withDetail("status", "Weather API test failed")
                    .withDetail("error", testResult.errorMessage())
                    .build();
            }
            
        } catch (Exception e) {
            return Health.down()
                .withDetail("status", "Weather API is not accessible")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 驗收成果

### 測試天氣查詢

```bash
# 基本天氣查詢
curl "http://localhost:8080/ai/weather/chat?question=桃園目前天氣如何？"

# 複雜查詢
curl "http://localhost:8080/ai/weather/chat?question=全台最高溫的前三名是哪些地方？"

# 預報查詢
curl "http://localhost:8080/ai/weather/chat?question=台中未來三天天氣預報"

# 直接API調用
curl "http://localhost:8080/ai/weather/current?location=台北"

# 溫度排行榜
curl "http://localhost:8080/ai/weather/temperature-ranking?topCount=5"
```

### 測試結果範例

```json
{
    "question": "桃園目前天氣如何？",
    "answer": "📍 地點：桃園\n🌡️ 溫度：28.5°C\n💧 濕度：72%\n☁️ 天氣：多雲\n🌧️ 降雨量：0.0mm\n💨 風向：東南風\n🌪️ 風速：2.3m/s\n⏰ 觀測時間：2025-01-29 14:30",
    "success": true,
    "errorMessage": null,
    "responseTimeMs": 1250
}
```

## 企業級最佳實踐

### 1. API 限流和配額管理

```java
@Component
public class WeatherApiQuotaManager {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final int DAILY_QUOTA = 10000;
    private final int HOURLY_QUOTA = 1000;
    
    public boolean checkQuota(String apiKey) {
        String dailyKey = "weather:quota:daily:" + apiKey + ":" + LocalDate.now();
        String hourlyKey = "weather:quota:hourly:" + apiKey + ":" + LocalDateTime.now().getHour();
        
        Long dailyCount = redisTemplate.opsForValue().increment(dailyKey);
        redisTemplate.expire(dailyKey, Duration.ofDays(1));
        
        Long hourlyCount = redisTemplate.opsForValue().increment(hourlyKey);
        redisTemplate.expire(hourlyKey, Duration.ofHours(1));
        
        return dailyCount <= DAILY_QUOTA && hourlyCount <= HOURLY_QUOTA;
    }
}
```

### 2. 多資料源容錯

```java
@Service
public class MultiSourceWeatherService {
    
    private final List<WeatherDataSource> dataSources;
    
    public WeatherInfo getCurrentWeatherWithFallback(String location) {
        for (WeatherDataSource source : dataSources) {
            try {
                WeatherInfo result = source.getCurrentWeather(location);
                if (result.success()) {
                    return result;
                }
            } catch (Exception e) {
                log.warn("Weather source {} failed: {}", source.getName(), e.getMessage());
                continue;
            }
        }
        
        return WeatherInfo.error("所有天氣資料源都無法使用");
    }
}
```

## 回顧

今天學到的內容：

1. 如何使用最新的 @Tool 註解實現 Function Calling
2. 整合真實的第三方 API（中央氣象局）
3. Spring Boot 呼叫外部 API 的最佳實踐
4. 使用 ChatClient 新 API 進行 Function Calling
5. 企業級的錯誤處理、重試、快取機制
6. API 限流和配額管理
7. 多資料源容錯處理
8. 監控和健康檢查實現

透過這個範例，我們可以看到 AI 不只是協助資料分析，還能處理複雜的即時資料查詢任務，讓應用程式更加智能化。

## Source Code

程式碼下載: [https://github.com/kevintsai1202/SpringBoot-AI-Day31-Updated.git](https://github.com/kevintsai1202/SpringBoot-AI-Day31-Updated.git)