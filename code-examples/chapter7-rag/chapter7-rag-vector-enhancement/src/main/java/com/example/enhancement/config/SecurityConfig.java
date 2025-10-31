package com.example.enhancement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置類
 *
 * 負責配置：
 * 1. HTTP 安全策略
 * 2. 權限控制
 * 3. CORS 設定
 * 4. CSRF 保護
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.security.enable-access-control:true}")
    private boolean enableAccessControl;

    /**
     * 配置 HTTP 安全過濾鏈
     *
     * 開發環境：允許所有請求通過（enableAccessControl=false）
     * 生產環境：需要身份驗證和授權（enableAccessControl=true）
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 基本配置
        http
                // 禁用 CSRF（API 模式）
                .csrf(AbstractHttpConfigurer::disable)
                // 配置 Headers
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );

        // 根據配置決定是否啟用權限控制
        if (enableAccessControl) {
            // 生產環境：啟用權限控制
            http
                    .authorizeHttpRequests(auth -> auth
                            // 允許健康檢查和 Prometheus 端點
                            .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                            // 其他請求需要認證
                            .anyRequest().authenticated()
                    )
                    // 啟用 HTTP Basic 認證
                    .httpBasic(basic -> {});
        } else {
            // 開發環境：允許所有請求
            http.authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
            );
        }

        return http.build();
    }
}
