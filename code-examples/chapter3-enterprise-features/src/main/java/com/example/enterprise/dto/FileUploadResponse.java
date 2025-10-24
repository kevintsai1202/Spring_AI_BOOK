package com.example.enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 檔案上傳回應 DTO
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private Long id;
    private String originalFilename;
    private String storedFilename;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
    private String previewUrl;
}
