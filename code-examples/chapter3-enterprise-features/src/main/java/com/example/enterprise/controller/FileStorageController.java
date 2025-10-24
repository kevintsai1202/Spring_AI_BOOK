package com.example.enterprise.controller;

import com.example.enterprise.dto.ApiResponse;
import com.example.enterprise.dto.FileUploadResponse;
import com.example.enterprise.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 檔案儲存 API 控制器
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "檔案管理", description = "檔案上傳、下載與預覽 API")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "上傳檔案", description = "上傳單一檔案，支援圖片、文件等多種格式")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "上傳成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "檔案驗證失敗"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "檔案儲存失敗"
            )
    })
    public ApiResponse<FileUploadResponse> uploadFile(
            @Parameter(description = "要上傳的檔案", required = true)
            @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileStorageService.storeFile(file);
        return ApiResponse.success("檔案上傳成功", response);
    }

    @GetMapping("/download/{filename:.+}")
    @Operation(summary = "下載檔案", description = "根據檔案名稱下載檔案")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "下載成功"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "檔案不存在"
            )
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "檔案名稱", required = true)
            @PathVariable String filename,
            HttpServletRequest request) {
        // 載入檔案資源
        Resource resource = fileStorageService.loadFileAsResource(filename);

        // 偵測檔案的 MIME 類型
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("無法偵測檔案類型");
        }

        // 預設類型
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/preview/{filename:.+}")
    @Operation(summary = "預覽檔案", description = "在瀏覽器中預覽檔案（適用於圖片、PDF 等）")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "預覽成功"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "檔案不存在"
            )
    })
    public ResponseEntity<Resource> previewFile(
            @Parameter(description = "檔案名稱", required = true)
            @PathVariable String filename,
            HttpServletRequest request) {
        // 載入檔案資源
        Resource resource = fileStorageService.loadFileAsResource(filename);

        // 偵測檔案的 MIME 類型
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("無法偵測檔案類型");
        }

        // 預設類型
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
