package com.example.enhancement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 向量品質評估配置類
 *
 * 從 application.yml 讀取向量品質相關配置
 */
@Configuration
@ConfigurationProperties(prefix = "app.vector-quality")
@Data
public class VectorQualityConfig {

    /**
     * 最小維度
     */
    private int minDimension = 384;

    /**
     * 最大維度
     */
    private int maxDimension = 4096;

    /**
     * 最小範數（L2 norm）
     */
    private double minNorm = 0.1;

    /**
     * 最大範數（L2 norm）
     */
    private double maxNorm = 10.0;

    /**
     * 品質閾值（0.0 - 1.0）
     */
    private double qualityThreshold = 0.7;

    /**
     * 最小文本長度
     */
    private int minTextLength = 10;

    /**
     * 最大文本長度
     */
    private int maxTextLength = 10000;
}
