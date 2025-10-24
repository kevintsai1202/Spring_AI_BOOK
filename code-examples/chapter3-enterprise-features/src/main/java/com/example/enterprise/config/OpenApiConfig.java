package com.example.enterprise.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * OpenAPI (Swagger) 配置
 * 自動生成 API 文件
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("企業級功能 API 文件")
                        .version("1.0.0")
                        .description("展示資料驗證、檔案處理、API 文件化等企業級功能")
                        .contact(new Contact()
                                .name("開發團隊")
                                .email("dev@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("開發環境"),
                        new Server().url("https://api.example.com").description("生產環境")
                ));
    }
}
