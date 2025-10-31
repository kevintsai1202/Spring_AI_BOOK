package com.example.memory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring AI 記憶核心系統應用程序主類
 *
 * 該應用實現了完整的記憶管理和Advisor增強器系統
 * 支持短期記憶、持久化存儲和智能Advisor鏈機制
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.memory.controller",
    "com.example.memory.service",
    "com.example.memory.config",
    "com.example.memory.advisor",
    "com.example.memory.memory",
    "com.example.memory.tool"
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
