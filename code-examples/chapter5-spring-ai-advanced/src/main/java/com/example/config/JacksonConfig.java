package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson 配置
 * 處理 OpenAI API 新增字段兼容性問題
 * OpenAI 最近在 API 響應中添加了新的 "annotations" 字段
 * Spring AI 1.0.0-M4 的 ChatCompletionMessage 類未配置忽略未知字段
 * 此配置使 Jackson 能夠忽略未知字段，解決兼容性問題
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置 ObjectMapper bean
     * 設置 Jackson 忽略反序列化時的未知字段
     * 這允許 API 響應包含新字段而不會導致異常
     *
     * @return 配置好的 ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 忽略未知字段 - 這是關鍵配置
        // 允許 OpenAI API 響應包含新的 "annotations" 字段而不會失敗
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 其他有用的配置
        mapper.findAndRegisterModules();

        return mapper;
    }
}
