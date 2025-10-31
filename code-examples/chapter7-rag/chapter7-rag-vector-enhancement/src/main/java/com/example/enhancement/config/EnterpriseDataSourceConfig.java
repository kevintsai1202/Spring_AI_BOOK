package com.example.enhancement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 企業資料源配置類
 *
 * 從 application.yml 讀取企業資料源相關配置
 * 支援多種資料源類型：MongoDB、REST API、檔案系統
 * 所有資料統一存儲於 Neo4j 向量資料庫
 */
@Configuration
@ConfigurationProperties(prefix = "app.datasources")
@Data
public class EnterpriseDataSourceConfig {

    /**
     * MongoDB 資料源配置
     */
    private DataSourceSettings mongodb = new DataSourceSettings();

    /**
     * REST API 資料源配置
     */
    private DataSourceSettings restApi = new DataSourceSettings();

    /**
     * 檔案系統資料源配置
     */
    private DataSourceSettings fileSystem = new DataSourceSettings();

    /**
     * 自定義資料源配置
     */
    private Map<String, DataSourceSettings> custom = new HashMap<>();

    /**
     * 資料源設定類
     */
    @Data
    public static class DataSourceSettings {
        /**
         * 是否啟用
         */
        private boolean enabled = false;

        /**
         * 同步模式：FULL_SYNC, INCREMENTAL_SYNC, REAL_TIME_SYNC
         */
        private String syncMode = "INCREMENTAL_SYNC";

        /**
         * 同步間隔（例如：5m, 1h）
         */
        private Duration syncInterval = Duration.ofMinutes(5);

        /**
         * 連接字串
         */
        private String connectionString;

        /**
         * 用戶名
         */
        private String username;

        /**
         * 密碼
         */
        private String password;

        /**
         * 其他屬性
         */
        private Map<String, String> properties = new HashMap<>();

        /**
         * 重試配置
         */
        private RetrySettings retry = new RetrySettings();
    }

    /**
     * 重試設定類
     */
    @Data
    public static class RetrySettings {
        /**
         * 最大重試次數
         */
        private int maxAttempts = 3;

        /**
         * 初始延遲
         */
        private Duration initialDelay = Duration.ofSeconds(1);

        /**
         * 最大延遲
         */
        private Duration maxDelay = Duration.ofMinutes(5);

        /**
         * 退避倍數
         */
        private double backoffMultiplier = 2.0;
    }
}
