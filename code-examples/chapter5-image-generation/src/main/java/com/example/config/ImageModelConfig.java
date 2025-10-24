package com.example.config;

import org.springframework.context.annotation.Configuration;

/**
 * 圖片生成 AI 模型配置類
 * Spring Boot 會自動配置 OpenAI 圖片生成模型通過 spring-ai-starter-model-openai
 */
@Configuration
public class ImageModelConfig {
    // Spring AI 自動配置會提供 ImageModel Bean
    // 無需手動配置
}
