package com.example.mcp.server.provider.tool;

import com.example.mcp.server.model.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 天氣工具提供者
 * 調用 Open-Meteo API 獲取指定位置的溫度資訊
 */
@Service
@Slf4j
public class WeatherToolProvider {

    /**
     * WebClient 用於調用外部 API
     */
    private final WebClient webClient;

    /**
     * 構造函數，初始化 WebClient
     */
    public WeatherToolProvider() {
        this.webClient = WebClient.create();
    }

    /**
     * 獲取指定位置的溫度
     *
     * @param latitude  緯度
     * @param longitude 經度
     * @param city      城市名稱（用於日誌）
     * @return 天氣回應，包含當前溫度資訊
     */
    @Tool(description = "Get weather temperature for a specific location using latitude and longitude coordinates")
    public WeatherResponse getTemperature(double latitude, double longitude, String city) {

        log.info("查詢天氣 - 城市: {}, 緯度: {}, 經度: {}", city, latitude, longitude);

        try {
            // 調用 Open-Meteo API
            WeatherResponse response = webClient
                    .get()
                    .uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
                            latitude, longitude)
                    .retrieve()
                    .bodyToMono(WeatherResponse.class)
                    .block();

            if (response != null && response.getCurrent() != null) {
                log.info("天氣查詢成功 - 城市: {}, 溫度: {}°C",
                        city, response.getCurrent().getTemperature());
            }

            return response;

        } catch (Exception e) {
            log.error("查詢天氣失敗 - 城市: {}, 錯誤: {}", city, e.getMessage());
            throw new RuntimeException("無法獲取天氣資訊: " + e.getMessage(), e);
        }
    }
}
