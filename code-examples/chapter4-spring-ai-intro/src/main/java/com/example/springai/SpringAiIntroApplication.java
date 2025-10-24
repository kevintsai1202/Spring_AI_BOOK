/**
 * Spring AI 入門應用程式主類別
 * 第四章：Spring AI 入門範例
 */
package com.example.springai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI 入門應用程式
 *
 * 本應用程式展示以下功能：
 * 1. ChatClient Fluent API 的基本使用
 * 2. 流式輸出（Server-Sent Events）
 * 3. 多模型配置（OpenAI GPT-4o、GPT-4o-mini、Groq）
 * 4. 自定義聊天選項（temperature、max-tokens 等）
 * 5. 系統提示詞和使用者提示詞的組合使用
 *
 * @author Spring AI Book
 * @version 1.0.0
 */
@SpringBootApplication
public class SpringAiIntroApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiIntroApplication.class, args);
    }
}
