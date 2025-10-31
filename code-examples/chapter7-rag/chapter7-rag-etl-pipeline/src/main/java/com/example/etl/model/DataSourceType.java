package com.example.etl.model;

/**
 * 資料來源類型枚舉
 */
public enum DataSourceType {
    /** PDF 文檔 */
    PDF,
    /** 純文本文件 */
    TEXT,
    /** Markdown 文件 */
    MARKDOWN,
    /** JSON 文件 */
    JSON,
    /** HTML 文件 */
    HTML,
    /** Word 文檔 */
    WORD,
    /** Excel 文檔 */
    EXCEL,
    /** PowerPoint 文檔 */
    POWERPOINT,
    /** 壓縮檔案 */
    ARCHIVE
}
