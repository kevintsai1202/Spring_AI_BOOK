package com.example.etl.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 壓縮檔案 DocumentReader - 處理 ZIP 檔案
 * 實現 Spring AI DocumentReader 接口
 */
@Slf4j
public class ArchiveDocumentReader implements DocumentReader {

    private final Resource archiveResource;
    private final DocumentReaderFactory readerFactory;

    public ArchiveDocumentReader(Resource archiveResource, DocumentReaderFactory readerFactory) {
        this.archiveResource = archiveResource;
        this.readerFactory = readerFactory;
    }

    @Override
    public List<Document> get() {
        return read();
    }

    @Override
    public List<Document> read() {
        log.info("開始提取壓縮檔案: {}", archiveResource.getFilename());

        String fileName = archiveResource.getFilename();
        if (fileName == null) {
            log.error("壓縮檔案名稱為 null");
            return List.of();
        }

        String lowerFileName = fileName.toLowerCase();

        try {
            if (lowerFileName.endsWith(".zip")) {
                return extractZipFile();
            } else {
                log.warn("不支援的壓縮格式: {}", fileName);
                return List.of();
            }
        } catch (Exception e) {
            log.error("壓縮檔案提取失敗: {}", archiveResource.getFilename(), e);
            return List.of();
        }
    }

    /**
     * 提取 ZIP 檔案
     */
    private List<Document> extractZipFile() throws IOException {
        List<Document> documents = new ArrayList<>();

        try (InputStream inputStream = archiveResource.getInputStream();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry entry;
            int entryCount = 0;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                entryCount++;

                // 跳過目錄
                if (entry.isDirectory()) {
                    log.debug("跳過目錄: {}", entry.getName());
                    zipInputStream.closeEntry();
                    continue;
                }

                // 檢查是否支援此文件類型
                if (!isSupportedFileType(entry.getName())) {
                    log.debug("跳過不支援的文件: {}", entry.getName());
                    zipInputStream.closeEntry();
                    continue;
                }

                try {
                    List<Document> entryDocuments = extractZipEntry(zipInputStream, entry);
                    documents.addAll(entryDocuments);
                    log.debug("從 {} 提取 {} 個文檔", entry.getName(), entryDocuments.size());
                } catch (Exception e) {
                    log.warn("提取 ZIP 條目失敗: {}, 錯誤: {}", entry.getName(), e.getMessage());
                }

                zipInputStream.closeEntry();
            }

            log.info("ZIP 檔案處理完成: 共 {} 個條目,提取 {} 個文檔",
                    entryCount, documents.size());
        }

        return documents;
    }

    /**
     * 提取單個 ZIP 條目
     */
    private List<Document> extractZipEntry(ZipInputStream zipInputStream, ZipEntry entry)
            throws IOException {

        String entryName = entry.getName();
        log.debug("正在提取: {}", entryName);

        // 讀取條目內容到記憶體
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }

        byte[] entryData = baos.toByteArray();

        if (entryData.length == 0) {
            log.warn("ZIP 條目為空: {}", entryName);
            return List.of();
        }

        // 建立記憶體資源
        Resource entryResource = new ByteArrayResource(entryData) {
            @Override
            public String getFilename() {
                // 提取文件名(不含路徑)
                int lastSlash = entryName.lastIndexOf('/');
                return lastSlash >= 0 ? entryName.substring(lastSlash + 1) : entryName;
            }
        };

        try {
            // 根據文件類型選擇適當的 DocumentReader
            DocumentReader reader = readerFactory.createReader(entryResource);
            List<Document> documents = reader.read();

            // 添加壓縮檔案相關的元資料
            documents.forEach(doc -> {
                doc.getMetadata().put("archive_source", archiveResource.getFilename());
                doc.getMetadata().put("archive_entry", entryName);
                doc.getMetadata().put("extraction_method", "ZIP_ARCHIVE");
            });

            return documents;

        } catch (UnsupportedOperationException e) {
            log.debug("ZIP 條目無適合的 Reader: {}", entryName);
            return List.of();
        }
    }

    /**
     * 檢查檔案類型是否支援
     */
    private boolean isSupportedFileType(String fileName) {
        String lowerFileName = fileName.toLowerCase();

        List<String> supportedTypes = Arrays.asList(
                ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
                ".txt", ".md", ".markdown",
                ".html", ".htm",
                ".json"
        );

        return supportedTypes.stream().anyMatch(lowerFileName::endsWith);
    }
}
