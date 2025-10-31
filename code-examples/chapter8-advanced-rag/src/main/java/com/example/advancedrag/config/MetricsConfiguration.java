package com.example.advancedrag.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 監控指標配置
 *
 * 配置 Micrometer 和 Prometheus 指標收集
 */
@Configuration
public class MetricsConfiguration {

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 配置 MeterRegistry 自定義器
     *
     * 為所有指標添加通用標籤
     *
     * @return MeterRegistryCustomizer 實例
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(Tags.of(
                        "application", applicationName,
                        "environment", getEnvironment()
                ));
    }

    /**
     * 獲取當前環境
     *
     * @return 環境名稱
     */
    private String getEnvironment() {
        String profile = System.getProperty("spring.profiles.active");
        return profile != null ? profile : "default";
    }
}
