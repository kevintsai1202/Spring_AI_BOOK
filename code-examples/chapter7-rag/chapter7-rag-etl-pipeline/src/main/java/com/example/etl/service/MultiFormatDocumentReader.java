package com.example.etl.service;

import com.example.etl.model.DataSourceType;
import com.example.etl.reader.ArchiveDocumentReader;
import com.example.etl.reader.DocumentReaderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 多格式文檔讀取服務 - 統一的文檔處理接口
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MultiFormatDocumentReader {

    private final DocumentReaderFactory readerFactory;

    /**
     * 智能讀取文檔 (自動檢測格式)
     */
    public List<Document> readDocument(Resource resource, DataSourceType type) {
        log.info("讀取文檔: {}, 類型: {}", resource.getFilename(), type);

        switch (type) {
            case PDF:
                return readPdfDocument(resource);
            case TEXT:
                return readTextDocument(resource);
            case MARKDOWN:
                return readMarkdownDocument(resource);
            case JSON:
                return readJsonDocument(resource);
            case HTML:
                return readHtmlDocument(resource);
            case WORD:
            case EXCEL:
            case POWERPOINT:
                return readOfficeDocument(resource);
            case ARCHIVE:
                return readArchiveDocument(resource);
            default:
                throw new UnsupportedOperationException("不支援的文檔類型: " + type);
        }
    }

    /**
     * 讀取 PDF 文檔
     */
    private List<Document> readPdfDocument(Resource resource) {
        log.debug("讀取 PDF: {}", resource.getFilename());
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
        return pdfReader.read();
    }

    /**
     * 讀取 Office 文檔 (Word, Excel, PowerPoint)
     */
    private List<Document> readOfficeDocument(Resource resource) {
        log.debug("使用 Tika 讀取 Office 文檔: {}", resource.getFilename());
        TikaDocumentReader tikaReader = new TikaDocumentReader(resource);
        List<Document> documents = tikaReader.read();

        // 添加文檔類型元資料
        String filename = resource.getFilename();
        documents.forEach(doc -> {
            doc.getMetadata().put("document_type", determineDocumentType(filename));
            doc.getMetadata().put("source_file", filename);
            doc.getMetadata().put("extraction_method", "TIKA");
        });

        return documents;
    }

    /**
     * 讀取文本文件
     */
    private List<Document> readTextDocument(Resource resource) {
        log.debug("讀取文本文件: {}", resource.getFilename());
        TextReader textReader = new TextReader(resource);
        textReader.setCharset(StandardCharsets.UTF_8);
        textReader.getCustomMetadata().put("filename", resource.getFilename());
        textReader.getCustomMetadata().put("content_type", "TEXT");
        return textReader.read();
    }

    /**
     * 讀取 Markdown 文件
     */
    private List<Document> readMarkdownDocument(Resource resource) {
        log.debug("讀取 Markdown 文件: {}", resource.getFilename());
        try {
            // MarkdownDocumentReader 需要 file: 前綴的路徑
            String filePath = "file:" + resource.getFile().getAbsolutePath();
            log.debug("Markdown 文件完整路徑: {}", filePath);
            MarkdownDocumentReader markdownReader = new MarkdownDocumentReader(filePath);
            return markdownReader.read();
        } catch (Exception e) {
            log.error("讀取 Markdown 文件失敗: {}", resource.getFilename(), e);
            return List.of();
        }
    }

    /**
     * 讀取 JSON 文件
     */
    private List<Document> readJsonDocument(Resource resource) {
        log.debug("讀取 JSON 文件: {}", resource.getFilename());
        JsonReader jsonReader = new JsonReader(resource);
        return jsonReader.read();
    }

    /**
     * 讀取 HTML 文件
     */
    private List<Document> readHtmlDocument(Resource resource) {
        log.debug("讀取 HTML 文件: {}", resource.getFilename());
        JsoupDocumentReader htmlReader = new JsoupDocumentReader(resource);
        return htmlReader.read();
    }

    /**
     * 讀取壓縮檔案
     */
    private List<Document> readArchiveDocument(Resource resource) {
        log.debug("讀取壓縮檔案: {}", resource.getFilename());
        ArchiveDocumentReader archiveReader = new ArchiveDocumentReader(
                resource, readerFactory);
        return archiveReader.read();
    }

    /**
     * 判斷 Office 文檔類型
     */
    private String determineDocumentType(String filename) {
        if (filename == null) {
            return "UNKNOWN";
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".docx") || lower.endsWith(".doc")) {
            return "WORD";
        } else if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            return "EXCEL";
        } else if (lower.endsWith(".pptx") || lower.endsWith(".ppt")) {
            return "POWERPOINT";
        }
        return "UNKNOWN";
    }
}
