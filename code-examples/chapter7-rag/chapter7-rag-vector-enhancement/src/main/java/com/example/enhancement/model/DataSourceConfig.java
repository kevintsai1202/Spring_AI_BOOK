package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 資料來源配置
 * 用於定義企業資料來源的連接和同步配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceConfig {
    /**
     * 資料源名稱
     */
    private String name;

    /**
     * 資料源類型
     */
    private DataSourceType type;

    /**
     * 連接字串
     */
    private String connectionString;

    /**
     * 額外屬性
     */
    private Map<String, String> properties;

    /**
     * 安全配置
     */
    private SecurityConfig security;

    /**
     * 同步配置
     */
    private SyncConfig sync;

    /**
     * 重試配置
     */
    private RetryConfig retry;

    /**
     * 安全配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityConfig {
        /**
         * 用戶名
         */
        private String username;

        /**
         * 密碼
         */
        private String password;

        /**
         * 證書路徑
         */
        private String certificatePath;

        /**
         * 是否啟用 SSL
         */
        private boolean sslEnabled;

        /**
         * 允許的角色列表
         */
        private List<String> allowedRoles;
    }

    /**
     * 同步配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncConfig {
        /**
         * 是否啟用同步
         */
        private boolean enabled;

        /**
         * 同步間隔
         */
        private Duration interval;

        /**
         * 同步模式
         */
        private SyncMode mode;

        /**
         * 變更檢測欄位（用於增量同步）
         */
        private String changeDetectionColumn;
    }

    /**
     * 重試配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryConfig {
        /**
         * 最大重試次數
         */
        @Builder.Default
        private int maxAttempts = 3;

        /**
         * 初始延遲
         */
        @Builder.Default
        private Duration initialDelay = Duration.ofSeconds(1);

        /**
         * 最大延遲
         */
        @Builder.Default
        private Duration maxDelay = Duration.ofMinutes(5);

        /**
         * 退避倍數
         */
        @Builder.Default
        private double backoffMultiplier = 2.0;
    }
}
