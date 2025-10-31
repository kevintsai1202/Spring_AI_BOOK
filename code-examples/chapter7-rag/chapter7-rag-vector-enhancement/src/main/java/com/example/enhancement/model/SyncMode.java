package com.example.enhancement.model;

/**
 * 資料同步模式枚舉
 *
 * 定義不同的資料同步策略
 */
public enum SyncMode {
    /**
     * 全量同步 - 每次同步所有資料
     */
    FULL_SYNC,

    /**
     * 增量同步 - 只同步變更的資料
     */
    INCREMENTAL_SYNC,

    /**
     * 即時同步 - 使用 CDC 即時捕獲變更
     */
    REAL_TIME_SYNC
}
