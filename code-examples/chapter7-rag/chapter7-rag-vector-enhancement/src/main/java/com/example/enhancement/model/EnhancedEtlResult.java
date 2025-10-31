package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 增強型 ETL 執行結果
 *
 * 封裝 ETL Pipeline 的執行結果和統計資訊
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnhancedEtlResult {

    /**
     * 執行開始時間
     */
    private LocalDateTime startTime;

    /**
     * 執行結束時間
     */
    private LocalDateTime endTime;

    /**
     * 執行時長
     */
    private Duration duration;

    /**
     * 原始文檔數量
     */
    private int originalDocumentCount;

    /**
     * 清理後文檔數量
     */
    private int cleanedDocumentCount;

    /**
     * 分塊後文檔數量
     */
    private int chunkedDocumentCount;

    /**
     * 增強後文檔數量
     */
    private int enrichedDocumentCount;

    /**
     * 通過品質檢查的文檔數量
     */
    private int qualityPassedDocumentCount;

    /**
     * 載入到向量資料庫的文檔數量
     */
    private int loadedDocumentCount;

    /**
     * 品質評估結果列表
     */
    private List<EmbeddingQuality> qualityResults;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 錯誤訊息（如果失敗）
     */
    private String errorMessage;

    /**
     * 額外資訊
     */
    private String additionalInfo;

    /**
     * 計算處理速度（文檔/秒）
     */
    public double getProcessingSpeed() {
        if (duration == null || duration.isZero()) {
            return 0.0;
        }
        double seconds = duration.getSeconds() + duration.getNano() / 1_000_000_000.0;
        return loadedDocumentCount / seconds;
    }

    /**
     * 計算品質通過率
     */
    public double getQualityPassRate() {
        if (enrichedDocumentCount == 0) {
            return 0.0;
        }
        return (double) qualityPassedDocumentCount / enrichedDocumentCount;
    }
}
