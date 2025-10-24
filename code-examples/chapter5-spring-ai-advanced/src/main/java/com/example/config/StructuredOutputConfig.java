package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 結構化輸出配置
 * 提供 ObjectMapper 和最佳實踐建議
 * 對應 5.10 章節的配置實現
 */
@Configuration
public class StructuredOutputConfig {

    /**
     * 自定義 ObjectMapper 用於結構化輸出
     * 使用 SNAKE_CASE 命名策略以適應 AI 模型的輸出格式
     */
    @Bean("structuredOutputMapper")
    public ObjectMapper structuredOutputMapper() {
        return new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .findAndRegisterModules();
    }

    /**
     * 轉換器最佳實踐建議
     */
    public static class BestPractices {

        public static final String[] RECOMMENDATIONS = {
            "1. 優先使用 entity() 方法：對於大多數結構化輸出需求，新的 entity() 方法更簡潔",
            "2. 複雜場景使用轉換器：需要自定義格式或特殊處理時，使用傳統轉換器",
            "3. 合理設計資料模型：使用 record 類型提高效能和可讀性",
            "4. 添加適當的 JSON 註解：確保序列化和反序列化的正確性",
            "5. 實現錯誤處理：對轉換失敗的情況提供優雅的降級處理",
            "6. 使用快取機制：對於重複的轉換操作考慮添加快取",
            "7. 監控轉換效能：追蹤轉換時間和成功率",
            "8. 驗證輸出格式：確保 AI 輸出符合預期的資料結構"
        };

        /**
         * 根據使用場景選擇最適合的轉換器
         */
        public static String getRecommendedApproach(String useCase) {
            return switch (useCase.toLowerCase()) {
                case "simple_entity" ->
                    "推薦使用 ChatClient.entity() 方法 - 最簡潔且類型安全";

                case "complex_validation" ->
                    "推薦使用 BeanOutputConverter - 可以添加自定義驗證邏輯";

                case "dynamic_structure" ->
                    "推薦使用 MapOutputConverter - 適合未知或動態的資料結構";

                case "simple_list" ->
                    "推薦使用 ListOutputConverter - 處理簡單的列表資料";

                case "enterprise_integration" ->
                    "推薦使用 ChatClient.entity() + 自定義 Jackson 配置";

                default ->
                    "建議先使用 ChatClient.entity() 方法，如需更多控制再考慮傳統轉換器";
            };
        }
    }
}
