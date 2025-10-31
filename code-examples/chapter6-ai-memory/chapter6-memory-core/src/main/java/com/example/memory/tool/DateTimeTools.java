package com.example.memory.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * 日期時間工具集
 *
 * 提供日期和時間相關的工具方法
 * 支持多時區和本地化格式
 */
@Slf4j
@Component
public class DateTimeTools {

    /**
     * 預設時區（台灣）
     */
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Taipei");

    /**
     * 預設語言環境（繁體中文）
     */
    private static final Locale DEFAULT_LOCALE = Locale.TAIWAN;

    /**
     * 獲取當前日期時間
     *
     * @return 格式化的日期時間字符串
     */
    public String getCurrentDateTime() {
        log.info("Getting current date and time");

        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);
        String result = now.format(
            DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")
                .withLocale(DEFAULT_LOCALE)
        );

        log.debug("Current date time: {}", result);
        return result;
    }

    /**
     * 獲取當前日期
     *
     * @return 格式化的日期字符串
     */
    public String getCurrentDate() {
        log.info("Getting current date");

        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        String dayOfWeek = today.getDayOfWeek()
            .getDisplayName(TextStyle.FULL, DEFAULT_LOCALE);

        String result = String.format(
            "%s (%s)",
            today.format(
                DateTimeFormatter.ofPattern("yyyy年MM月dd日")
                    .withLocale(DEFAULT_LOCALE)
            ),
            dayOfWeek
        );

        log.debug("Current date: {}", result);
        return result;
    }

    /**
     * 獲取當前時間
     *
     * @return 格式化的時間字符串
     */
    public String getCurrentTime() {
        log.info("Getting current time");

        LocalTime now = LocalTime.now(DEFAULT_ZONE);
        String result = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        log.debug("Current time: {}", result);
        return result;
    }

    /**
     * 計算兩個日期之間的天數差
     *
     * @param date1 第一個日期（格式：yyyy-MM-dd）
     * @param date2 第二個日期（格式：yyyy-MM-dd）
     * @return 天數差
     */
    public String getDaysBetween(String date1, String date2) {
        log.info("Calculating days between {} and {}", date1, date2);

        try {
            LocalDate d1 = LocalDate.parse(date1);
            LocalDate d2 = LocalDate.parse(date2);

            long days = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(d1, d2));
            String result = String.format("兩個日期之間相差 %d 天", days);

            log.debug("Days between result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error parsing dates", e);
            return "無法計算，請確保日期格式為 yyyy-MM-dd";
        }
    }

    /**
     * 判斷是否為工作日
     *
     * @return 工作日提示
     */
    public String isWorkingDay() {
        log.info("Checking if today is a working day");

        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        java.time.DayOfWeek dayOfWeek = today.getDayOfWeek();

        boolean isWorking = dayOfWeek != java.time.DayOfWeek.SATURDAY &&
                           dayOfWeek != java.time.DayOfWeek.SUNDAY;

        String dayName = dayOfWeek.getDisplayName(TextStyle.FULL, DEFAULT_LOCALE);
        String result = String.format(
            "今天是%s，%s",
            dayName,
            isWorking ? "是工作日" : "是週末"
        );

        log.debug("Working day check: {}", result);
        return result;
    }

    /**
     * 獲取倒數天數（到指定日期）
     *
     * @param targetDate 目標日期（格式：yyyy-MM-dd）
     * @return 倒數天數字符串
     */
    public String getDaysCountdown(String targetDate) {
        log.info("Calculating countdown to {}", targetDate);

        try {
            LocalDate target = LocalDate.parse(targetDate);
            LocalDate today = LocalDate.now(DEFAULT_ZONE);

            long days = java.time.temporal.ChronoUnit.DAYS.between(today, target);

            String result;
            if (days > 0) {
                result = String.format("距離 %s 還有 %d 天", targetDate, days);
            } else if (days < 0) {
                result = String.format("已經過了 %s %d 天", targetDate, Math.abs(days));
            } else {
                result = String.format("今天就是 %s", targetDate);
            }

            log.debug("Countdown result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error parsing target date", e);
            return "無法計算，請確保日期格式為 yyyy-MM-dd";
        }
    }

    /**
     * 獲取季節信息
     *
     * @return 當前季節
     */
    public String getCurrentSeason() {
        log.info("Getting current season");

        int month = LocalDate.now(DEFAULT_ZONE).getMonthValue();

        String season;
        if (month >= 3 && month <= 5) {
            season = "春季";
        } else if (month >= 6 && month <= 8) {
            season = "夏季";
        } else if (month >= 9 && month <= 11) {
            season = "秋季";
        } else {
            season = "冬季";
        }

        return String.format("現在是 %s", season);
    }
}
