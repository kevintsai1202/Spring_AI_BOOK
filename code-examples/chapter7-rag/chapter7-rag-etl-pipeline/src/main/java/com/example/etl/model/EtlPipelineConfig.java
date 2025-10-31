package com.example.etl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * ETL Pipeline 完整配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtlPipelineConfig {
    /** 資料來源列表 */
    @Builder.Default
    private List<DataSource> dataSources = new ArrayList<>();

    /** 分塊配置 */
    private ChunkingConfig chunkingConfig;

    /** 元資料增強配置 */
    private MetadataEnrichmentConfig enrichmentConfig;

    /** 載入配置 */
    private LoadConfig loadConfig;
}
