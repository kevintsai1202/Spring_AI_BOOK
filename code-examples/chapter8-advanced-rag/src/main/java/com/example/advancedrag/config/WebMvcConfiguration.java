package com.example.advancedrag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * 配置 CORS、攔截器等 Web 層設置
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    /**
     * 配置 CORS 跨域請求
     *
     * @param registry CORS 註冊器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * 配置 RestTemplate Bean
     *
     * 用於 HTTP 客戶端請求（如 OpenAI Moderation API）
     *
     * @return RestTemplate 實例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
