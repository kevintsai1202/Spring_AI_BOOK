package com.example.enhancement.model;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 同步任務
 * 表示一個資料同步任務的詳細資訊
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncTask {
    /**
     * 任務 ID
     */
    private String id;

    /**
     * 資料源名稱
     */
    private String dataSourceName;

    /**
     * 操作類型（CREATE, UPDATE, DELETE）
     */
    private SyncOperation operation;

    /**
     * 實體 ID
     */
    private String entityId;

    /**
     * 實體類型
     */
    private String entityType;

    /**
     * 變更資料
     */
    private Object changeData;

    /**
     * 時間戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 優先級
     */
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    /**
     * 重試次數
     */
    @Builder.Default
    private int retryCount = 0;

    /**
     * 下次重試時間
     */
    private LocalDateTime nextRetryTime;

    /**
     * 任務狀態
     */
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    /**
     * 錯誤訊息
     */
    private String errorMessage;

    /**
     * 初始化任務 ID
     */
    @PostConstruct
    public void init() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    /**
     * 任務狀態枚舉
     */
    public enum TaskStatus {
        /**
         * 等待中
         */
        PENDING,

        /**
         * 執行中
         */
        RUNNING,

        /**
         * 完成
         */
        COMPLETED,

        /**
         * 失敗
         */
        FAILED,

        /**
         * 重試中
         */
        RETRYING
    }
}
