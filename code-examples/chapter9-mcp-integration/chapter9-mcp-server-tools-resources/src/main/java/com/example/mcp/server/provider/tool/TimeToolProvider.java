package com.example.mcp.server.provider.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 時間工具提供者
 * 提供時間相關工具
 */
@Service
@Slf4j
public class TimeToolProvider {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    /**
     * 獲取當前時間
     */
    @Tool(description = "Get current time for a specific timezone (e.g., 'America/New_York', 'Asia/Tokyo')")
    public String getCurrentTime(String timezone) {
        log.info("獲取當前時間 - 時區: {}", timezone);

        try {
            ZoneId zoneId;
            if (timezone == null || timezone.trim().isEmpty()) {
                zoneId = ZoneId.systemDefault();
            } else {
                zoneId = ZoneId.of(timezone);
            }

            ZonedDateTime now = ZonedDateTime.now(zoneId);
            String result = now.format(FORMATTER);
            log.info("當前時間: {}", result);
            return result;

        } catch (Exception e) {
            log.error("獲取時間失敗: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
