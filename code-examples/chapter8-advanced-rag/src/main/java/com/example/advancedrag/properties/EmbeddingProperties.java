package com.example.advancedrag.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Embedding 配置屬性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.embedding")
public class EmbeddingProperties {

    private String primaryModel = "text-embedding-3-small";
    private String rerankingModel = "text-embedding-3-small";
    private String highAccuracyModel = "text-embedding-3-large";
    private Integer defaultDimensions = 1024;
    private Integer highAccuracyDimensions = 3072;
    private Integer lowCostDimensions = 512;
    private Boolean enableCache = true;
    private Integer cacheTtl = 86400;
    private String cacheKeyPrefix = "emb:";

    @Data
    public static class Preprocessing {
        private Boolean cleanSpecialChars = true;
        private Boolean normalizeWhitespace = true;
        private Boolean filterByLength = true;
        private Integer minLength = 10;
        private Integer maxLength = 8000;
    }

    private Preprocessing preprocessing = new Preprocessing();
}
