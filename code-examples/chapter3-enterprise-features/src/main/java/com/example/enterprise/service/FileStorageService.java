package com.example.enterprise.service;

import com.example.enterprise.dto.FileUploadResponse;
import com.example.enterprise.entity.FileMetadata;
import com.example.enterprise.exception.FileStorageException;
import com.example.enterprise.exception.ResourceNotFoundException;
import com.example.enterprise.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 檔案儲存服務實作類別
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FileStorageService {

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @Value("#{'${app.upload.allowed-types}'.split(',')}")
    private List<String> allowedTypes;

    private final FileMetadataRepository fileMetadataRepository;

    @Transactional
    public FileUploadResponse storeFile(MultipartFile file) {
        // 驗證檔案
        validateFile(file);

        try {
            // 建立上傳目錄
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 產生唯一檔案名稱
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);
            String storedFilename = UUID.randomUUID().toString() + fileExtension;

            // 儲存檔案
            Path targetLocation = uploadDir.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 儲存檔案元資料
            FileMetadata metadata = FileMetadata.builder()
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(targetLocation.toString())
                    .build();

            FileMetadata savedMetadata = fileMetadataRepository.save(metadata);

            log.info("檔案上傳成功：{} -> {}", originalFilename, storedFilename);

            // 建立回應
            return buildFileUploadResponse(savedMetadata);

        } catch (IOException e) {
            throw new FileStorageException("檔案儲存失敗：" + file.getOriginalFilename(), e);
        }
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = Paths.get(uploadPath).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("檔案不存在或無法讀取：" + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("檔案載入失敗：" + filename, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("請選擇要上傳的檔案");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new FileStorageException("不支援的檔案類型：" + contentType);
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }

    private FileUploadResponse buildFileUploadResponse(FileMetadata metadata) {
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/download/")
                .path(metadata.getStoredFilename())
                .toUriString();

        String previewUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/preview/")
                .path(metadata.getStoredFilename())
                .toUriString();

        return FileUploadResponse.builder()
                .id(metadata.getId())
                .originalFilename(metadata.getOriginalFilename())
                .storedFilename(metadata.getStoredFilename())
                .contentType(metadata.getContentType())
                .fileSize(metadata.getFileSize())
                .downloadUrl(downloadUrl)
                .previewUrl(previewUrl)
                .build();
    }
}
