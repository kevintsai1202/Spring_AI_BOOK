package com.example.config;

import org.springframework.context.annotation.Configuration;

/**
 * 語音生成 AI 模型配置類
 * Spring Boot 會自動配置 OpenAI 語音生成模型通過 spring-ai-starter-model-openai
 */
@Configuration
public class SpeechModelConfig {
    // Spring AI 自動配置會提供 SpeechModel Bean
    // 無需手動配置
}
