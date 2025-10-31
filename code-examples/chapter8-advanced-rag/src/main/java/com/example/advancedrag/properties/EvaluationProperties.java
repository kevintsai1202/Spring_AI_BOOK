package com.example.advancedrag.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 評估測試配置屬性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.evaluation")
public class EvaluationProperties {

    private Boolean continuous = true;
    private Integer interval = 3600000; // 1小時

    @Data
    public static class Thresholds {
        private Double relevancy = 0.8;
        private Double factuality = 0.85;
        private Double completeness = 0.7;
        private Double coherence = 0.75;
        private Integer responseTime = 5000;
        private Double overall = 0.8;

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("relevancy", relevancy);
            map.put("factuality", factuality);
            map.put("completeness", completeness);
            map.put("coherence", coherence);
            map.put("responseTime", responseTime);
            map.put("overall", overall);
            return map;
        }
    }

    private Thresholds thresholds = new Thresholds();

    @Data
    public static class TestCases {
        private String basicQaFile = "classpath:test-cases/basic-qa.json";
        private String domainSpecificFile = "classpath:test-cases/domain-specific.json";
        private String edgeCasesFile = "classpath:test-cases/edge-cases.json";
    }

    private TestCases testCases = new TestCases();

    @Data
    public static class Report {
        private String outputDir = "reports";
        private String fileNamePattern = "evaluation-{timestamp}.json";
    }

    private Report report = new Report();
}
