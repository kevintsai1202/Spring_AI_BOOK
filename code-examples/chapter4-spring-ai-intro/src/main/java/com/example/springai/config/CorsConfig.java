/**
 * CORS 跨域配置
 * 允許前端應用（包括 file:// 協議）訪問 API
 */
package com.example.springai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置 CORS 允許的來源和方法
     *
     * 注意：在生產環境中應該限制 allowedOrigins 為具體的域名
     * 開發環境使用 "*" 方便測試
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")  // 允許所有來源（包括 file://）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // 預檢請求快取時間（秒）
    }
}
