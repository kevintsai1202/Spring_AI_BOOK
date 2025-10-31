package com.example.etl.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * DocumentReader 工廠 - 根據文件類型創建對應的 Reader
 */
@Component
@Slf4j
public class DocumentReaderFactory {

    /**
     * 根據資源類型創建對應的 DocumentReader
     *
     * @param resource 要處理的資源
     * @return 對應的 DocumentReader
     */
    public DocumentReader createReader(Resource resource) {
        String filename = resource.getFilename();
        if (filename == null) {
            throw new IllegalArgumentException("無法判斷文件類型:檔名為 null");
        }

        String lowerFilename = filename.toLowerCase();
        log.debug("為文件建立 Reader: {}", filename);

        // 根據文件擴展名選擇合適的 DocumentReader
        if (lowerFilename.endsWith(".pdf")) {
            return new PagePdfDocumentReader(resource);

        } else if (lowerFilename.endsWith(".txt")) {
            TextReader reader = new TextReader(resource);
            reader.getCustomMetadata().put("filename", filename);
            return reader;

        } else if (lowerFilename.endsWith(".md")) {
            try {
                String filePath = "file:" + resource.getFile().getAbsolutePath();
                return new MarkdownDocumentReader(filePath);
            } catch (Exception e) {
                log.error("Failed to create MarkdownDocumentReader for {}", filename, e);
                throw new RuntimeException("Cannot create MarkdownDocumentReader", e);
            }

        } else if (lowerFilename.endsWith(".html") || lowerFilename.endsWith(".htm")) {
            return new JsoupDocumentReader(resource);

        } else if (lowerFilename.endsWith(".json")) {
            return new JsonReader(resource);

        } else if (isTikaSupported(lowerFilename)) {
            return new TikaDocumentReader(resource);

        } else {
            log.warn("不支援的文件類型: {}", filename);
            throw new UnsupportedOperationException("不支援的文件類型: " + filename);
        }
    }

    /**
     * 檢查是否為 Tika 支援的文件類型
     */
    private boolean isTikaSupported(String filename) {
        return filename.endsWith(".doc") ||
               filename.endsWith(".docx") ||
               filename.endsWith(".xls") ||
               filename.endsWith(".xlsx") ||
               filename.endsWith(".ppt") ||
               filename.endsWith(".pptx");
    }
}
