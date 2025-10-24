package com.example.enterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 企業級功能主程式
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@SpringBootApplication
public class EnterpriseApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseApplication.class, args);
        System.out.println("🚀 企業級功能應用程式已啟動！");
        System.out.println("📖 Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("📖 API Docs: http://localhost:8080/v3/api-docs");
    }
}
