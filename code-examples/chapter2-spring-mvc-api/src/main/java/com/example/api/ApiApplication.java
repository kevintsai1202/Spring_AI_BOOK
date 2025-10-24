package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring MVC API 主程式
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
        System.out.println("🚀 Spring MVC API 應用程式已啟動！");
        System.out.println("📖 API 端點: http://localhost:8080/api");
    }
}
