package com.example.advancedrag.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康檢查控制器
 *
 * 提供簡單的健康狀態檢查端點
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * 健康檢查端點
     *
     * @return 健康狀態信息
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "Advanced RAG System");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Advanced RAG 系統正在運行");

        return response;
    }

    /**
     * 系統信息端點
     *
     * @return 系統基本信息
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Advanced RAG System");
        response.put("description", "企業級智能檢索增強生成系統");
        response.put("version", "1.0.0");
        response.put("features", new String[]{
                "多階段智能檢索",
                "智能 Embedding 管理",
                "多層內容審核",
                "自動化評估測試",
                "完整監控體系"
        });

        return response;
    }
}
