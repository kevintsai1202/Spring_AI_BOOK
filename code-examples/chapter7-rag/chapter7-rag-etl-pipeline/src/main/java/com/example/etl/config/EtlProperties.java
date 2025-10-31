package com.example.etl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ETL 配置屬性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.etl")
public class EtlProperties {
    private ChunkingProperties chunking = new ChunkingProperties();
    private EnrichmentProperties enrichment = new EnrichmentProperties();
    private LoadProperties load = new LoadProperties();

    @Data
    public static class ChunkingProperties {
        private int defaultChunkSize = 1000;
        private int minChunkSizeChars = 350;
        private int minChunkLengthToEmbed = 10;
        private int maxNumChunks = 10000;
        private boolean keepSeparator = true;
    }

    @Data
    public static class EnrichmentProperties {
        private boolean enableBasicMetadata = true;
        private boolean enableLanguageDetection = true;
        private boolean enableContentStatistics = true;
        private boolean enableKeywordExtraction = false;
        private int keywordCount = 5;
        private boolean enableSummaryGeneration = false;
    }

    @Data
    public static class LoadProperties {
        private int batchSize = 50;
        private long batchDelayMs = 100;
        private boolean continueOnError = true;
    }
}
