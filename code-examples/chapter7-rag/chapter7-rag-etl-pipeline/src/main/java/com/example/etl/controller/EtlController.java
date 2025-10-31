package com.example.etl.controller;

import com.example.etl.model.*;
import com.example.etl.service.EtlPipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ETL Pipeline REST API 控制器
 */
@RestController
@RequestMapping("/api/etl")
@Slf4j
@RequiredArgsConstructor
public class EtlController {

    private final EtlPipelineService etlPipelineService;

    /**
     * 執行 ETL Pipeline
     */
    @PostMapping("/pipeline")
    public ResponseEntity<EtlPipelineResult> executeEtlPipeline(
            @RequestBody EtlPipelineConfig config) {
        log.info("收到 ETL Pipeline 請求");

        EtlPipelineResult result = etlPipelineService.executeEtlPipeline(config);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 上傳文件並處理
     */
    @PostMapping("/upload")
    public ResponseEntity<EtlPipelineResult> uploadAndProcess(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "chunkSize", defaultValue = "1000") int chunkSize,
            @RequestParam(value = "enableEnrichment", defaultValue = "true") boolean enableEnrichment) {

        log.info("收到文件上傳請求: {} 個文件", files.size());

        try {
            // 建立臨時文件
            List<DataSource> dataSources = new ArrayList<>();

            for (MultipartFile file : files) {
                // 儲存臨時文件
                File tempFile = File.createTempFile("upload-", "-" + file.getOriginalFilename());
                file.transferTo(tempFile);

                // 建立資料源
                Resource resource = new FileSystemResource(tempFile);
                DataSourceType type = detectFileType(file.getOriginalFilename());

                DataSource dataSource = DataSource.builder()
                        .name(file.getOriginalFilename())
                        .type(type)
                        .resource(resource)
                        .path(tempFile.getAbsolutePath())
                        .build();

                dataSources.add(dataSource);
            }

            // 建立 ETL 配置
            EtlPipelineConfig config = EtlPipelineConfig.builder()
                    .dataSources(dataSources)
                    .chunkingConfig(ChunkingConfig.builder()
                            .defaultChunkSize(chunkSize)
                            .build())
                    .enrichmentConfig(MetadataEnrichmentConfig.builder()
                            .enableBasicMetadata(enableEnrichment)
                            .enableContentStatistics(enableEnrichment)
                            .build())
                    .loadConfig(LoadConfig.builder().build())
                    .build();

            // 執行 ETL
            EtlPipelineResult result = etlPipelineService.executeEtlPipeline(config);

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            log.error("文件上傳處理失敗", e);
            return ResponseEntity.status(500)
                    .body(EtlPipelineResult.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    /**
     * 健康檢查
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ETL Pipeline Service is running");
    }

    /**
     * 檢測文件類型
     */
    private DataSourceType detectFileType(String filename) {
        if (filename == null) {
            return DataSourceType.TEXT;
        }

        String lower = filename.toLowerCase();

        if (lower.endsWith(".pdf")) return DataSourceType.PDF;
        if (lower.endsWith(".docx") || lower.endsWith(".doc")) return DataSourceType.WORD;
        if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) return DataSourceType.EXCEL;
        if (lower.endsWith(".pptx") || lower.endsWith(".ppt")) return DataSourceType.POWERPOINT;
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) return DataSourceType.MARKDOWN;
        if (lower.endsWith(".json")) return DataSourceType.JSON;
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return DataSourceType.HTML;
        if (lower.endsWith(".zip")) return DataSourceType.ARCHIVE;

        return DataSourceType.TEXT;
    }
}
