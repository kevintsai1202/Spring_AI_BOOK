package com.example.advancedrag.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 內容審核配置屬性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.moderation")
public class ModerationProperties {

    private Boolean enabled = true;
    private Double threshold = 0.8;

    @Data
    public static class Providers {
        @Data
        public static class ProviderConfig {
            private Boolean enabled = true;
            private Double weight = 0.4;
        }

        private ProviderConfig openai = new ProviderConfig();
        private ProviderConfig custom = new ProviderConfig();
    }

    private Providers providers = new Providers();
    private List<String> sensitiveWords = new ArrayList<>();

    @Data
    public static class PiiDetection {
        private Boolean enabled = true;
        private List<String> patterns = new ArrayList<>();
    }

    private PiiDetection piiDetection = new PiiDetection();
}
