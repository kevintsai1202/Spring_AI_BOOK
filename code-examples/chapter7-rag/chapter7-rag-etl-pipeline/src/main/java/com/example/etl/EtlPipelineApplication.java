package com.example.etl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ETL Pipeline 應用程式主類別
 *
 * Spring AI ETL Pipeline 與多格式文檔處理系統
 * - 支援多種文檔格式: PDF, Word, Excel, PowerPoint, HTML, JSON, Markdown
 * - 壓縮檔案批次處理
 * - 文檔分塊與元資料增強
 * - 向量資料庫整合
 */
@SpringBootApplication
public class EtlPipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtlPipelineApplication.class, args);
    }
}
