package com.example.memory.advanced.model;

import java.time.LocalDateTime;

/**
 * 待辦事項資料模型
 *
 * 從對話中提取的待辦事項或後續行動
 */
public record TodoItem(
    /**
     * 待辦事項描述
     */
    String description,

    /**
     * 優先級 (HIGH, MEDIUM, LOW)
     */
    Priority priority,

    /**
     * 截止日期 (可選)
     */
    LocalDateTime dueDate,

    /**
     * 是否已完成
     */
    boolean completed
) {
    /**
     * 優先級列舉
     */
    public enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }

    /**
     * 建立預設待辦事項
     */
    public static TodoItem of(String description) {
        return new TodoItem(description, Priority.MEDIUM, null, false);
    }

    /**
     * 建立帶優先級的待辦事項
     */
    public static TodoItem of(String description, Priority priority) {
        return new TodoItem(description, priority, null, false);
    }
}
