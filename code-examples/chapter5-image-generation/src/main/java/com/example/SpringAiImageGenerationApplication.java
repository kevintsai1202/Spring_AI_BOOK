package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Spring AI 圖片生成應用 - 第 5.3 章
 *
 * 功能：
 * - 文字轉圖片 (Text-to-Image)
 * - 支援 OpenAI DALL-E 3、ZhiPu AI CogView-3 等模型
 * - 企業級圖片管理系統
 * - 圖片快取和批次生成
 */
@SpringBootApplication
@EnableCaching
public class SpringAiImageGenerationApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiImageGenerationApplication.class, args);
    }

}
