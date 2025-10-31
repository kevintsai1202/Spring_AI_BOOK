package com.example.etl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 資料載入配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadConfig {
    /** 批次大小 */
    @Builder.Default
    private int batchSize = 50;

    /** 批次延遲 (毫秒) */
    @Builder.Default
    private long batchDelayMs = 100;

    /** 發生錯誤時是否繼續 */
    @Builder.Default
    private boolean continueOnError = true;
}
