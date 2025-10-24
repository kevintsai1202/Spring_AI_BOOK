/**
 * 日期時間工具類別
 * 提供當前時間查詢功能，展示 Tool Calling 的基本使用
 */
package com.example.tools;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeTools {

    /**
     * 獲取當前日期和時間
     * @return 格式化的當前日期時間字串
     */
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

        return String.format("當前時間：%s（台灣時間）", now.format(formatter));
    }

    /**
     * 獲取指定格式的當前時間
     * @param format 時間格式（如：yyyy-MM-dd HH:mm:ss）
     * @return 格式化的時間字串
     */
    public String getCurrentTimeWithFormat(String format) {
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return now.format(formatter);
        } catch (Exception e) {
            return "時間格式錯誤，請使用正確的格式，例如：yyyy-MM-dd HH:mm:ss";
        }
    }

    /**
     * 獲取當前時間戳
     * @return Unix 時間戳
     */
    public String getCurrentTimestamp() {
        long timestamp = System.currentTimeMillis() / 1000;
        return String.format("當前時間戳：%d", timestamp);
    }

    /**
     * 獲取當前是星期幾
     * @return 星期資訊
     */
    public String getCurrentDayOfWeek() {
        LocalDateTime now = LocalDateTime.now();
        String[] weekDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        int dayOfWeek = now.getDayOfWeek().getValue() - 1;

        return String.format("今天是%s", weekDays[dayOfWeek]);
    }
}
