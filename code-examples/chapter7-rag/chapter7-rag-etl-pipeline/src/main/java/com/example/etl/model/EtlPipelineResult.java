package com.example.etl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * ETL Pipeline 執行結果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtlPipelineResult {
    /** 是否成功 */
    private boolean success;

    /** 提取文檔數量 */
    private int extractedCount;

    /** 轉換文檔數量 */
    private int transformedCount;

    /** 載入文檔數量 */
    private int loadedCount;

    /** 處理時間 (毫秒) */
    private long processingTime;

    /** 錯誤訊息 */
    private String errorMessage;

    /** 處理指標 */
    @Builder.Default
    private Map<String, Object> metrics = new HashMap<>();
}
