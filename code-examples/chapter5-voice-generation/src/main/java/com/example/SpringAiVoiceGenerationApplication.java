package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Spring AI 配音生成應用 - 第 5.5 章
 *
 * 功能：
 * - 文字轉語音 (Text-to-Speech, TTS)
 * - 支援 OpenAI TTS-1、TTS-1-HD、ElevenLabs 等模型
 * - 6 種不同聲音選擇
 * - 批次語音生成
 * - 文字預處理和品質優化
 */
@SpringBootApplication
@EnableCaching
public class SpringAiVoiceGenerationApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiVoiceGenerationApplication.class, args);
    }

}
