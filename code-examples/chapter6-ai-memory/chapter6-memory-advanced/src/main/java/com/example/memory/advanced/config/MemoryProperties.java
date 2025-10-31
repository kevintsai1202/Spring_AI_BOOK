package com.example.memory.advanced.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 記憶配置屬性
 *
 * 從 application.yml 讀取記憶相關的配置
 */
@Configuration
@ConfigurationProperties(prefix = "app.memory")
public class MemoryProperties {

    /**
     * 智能摘要配置
     */
    private SmartSummary smartSummary = new SmartSummary();

    /**
     * 混合記憶配置
     */
    private Hybrid hybrid = new Hybrid();

    /**
     * 優化配置
     */
    private Optimization optimization = new Optimization();

    public SmartSummary getSmartSummary() {
        return smartSummary;
    }

    public void setSmartSummary(SmartSummary smartSummary) {
        this.smartSummary = smartSummary;
    }

    public Hybrid getHybrid() {
        return hybrid;
    }

    public void setHybrid(Hybrid hybrid) {
        this.hybrid = hybrid;
    }

    public Optimization getOptimization() {
        return optimization;
    }

    public void setOptimization(Optimization optimization) {
        this.optimization = optimization;
    }

    /**
     * 智能摘要配置
     */
    public static class SmartSummary {
        /**
         * 是否啟用智能摘要
         */
        private boolean enabled = true;

        /**
         * 觸發摘要的訊息數閾值
         */
        private int threshold = 50;

        /**
         * 保留最近的訊息數
         */
        private int keepRecent = 20;

        /**
         * 每次摘要的訊息數
         */
        private int summarizeCount = 30;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }

        public int getKeepRecent() {
            return keepRecent;
        }

        public void setKeepRecent(int keepRecent) {
            this.keepRecent = keepRecent;
        }

        public int getSummarizeCount() {
            return summarizeCount;
        }

        public void setSummarizeCount(int summarizeCount) {
            this.summarizeCount = summarizeCount;
        }
    }

    /**
     * 混合記憶配置
     */
    public static class Hybrid {
        /**
         * 是否啟用混合記憶
         */
        private boolean enabled = true;

        /**
         * 短期記憶權重
         */
        private double shortTermWeight = 0.6;

        /**
         * 長期記憶權重
         */
        private double longTermWeight = 0.4;

        /**
         * 最大短期記憶數
         */
        private int maxShortTerm = 20;

        /**
         * 最大長期記憶數
         */
        private int maxLongTerm = 10;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public double getShortTermWeight() {
            return shortTermWeight;
        }

        public void setShortTermWeight(double shortTermWeight) {
            this.shortTermWeight = shortTermWeight;
        }

        public double getLongTermWeight() {
            return longTermWeight;
        }

        public void setLongTermWeight(double longTermWeight) {
            this.longTermWeight = longTermWeight;
        }

        public int getMaxShortTerm() {
            return maxShortTerm;
        }

        public void setMaxShortTerm(int maxShortTerm) {
            this.maxShortTerm = maxShortTerm;
        }

        public int getMaxLongTerm() {
            return maxLongTerm;
        }

        public void setMaxLongTerm(int maxLongTerm) {
            this.maxLongTerm = maxLongTerm;
        }
    }

    /**
     * 記憶優化配置
     */
    public static class Optimization {
        /**
         * 是否啟用優化
         */
        private boolean enabled = true;

        /**
         * 單一對話的最大訊息數
         */
        private int maxMessages = 100;

        /**
         * 是否啟用自動清理
         */
        private boolean autoCleanup = true;

        /**
         * 記憶保留天數
         */
        private int retentionDays = 30;

        /**
         * 清理間隔 (毫秒)
         */
        private long cleanupInterval = 3600000;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxMessages() {
            return maxMessages;
        }

        public void setMaxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
        }

        public boolean isAutoCleanup() {
            return autoCleanup;
        }

        public void setAutoCleanup(boolean autoCleanup) {
            this.autoCleanup = autoCleanup;
        }

        public int getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }

        public long getCleanupInterval() {
            return cleanupInterval;
        }

        public void setCleanupInterval(long cleanupInterval) {
            this.cleanupInterval = cleanupInterval;
        }
    }
}
