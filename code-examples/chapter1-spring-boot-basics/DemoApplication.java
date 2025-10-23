/**
 * Spring Boot 主程式類別
 * 使用者管理系統的入口點
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        System.out.println("🚀 使用者管理系統已啟動！");
        System.out.println("📖 API 文件: http://localhost:8080/api/users");
    }
}