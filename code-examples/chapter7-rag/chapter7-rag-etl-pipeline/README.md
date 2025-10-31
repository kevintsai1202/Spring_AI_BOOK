# Chapter 7 RAG - ETL Pipeline

Spring AI ETL Pipeline 與多格式文檔處理系統

## 專案狀態

⚠️ **開發中 (WIP)** - 專案架構已完成,部分 Spring AI 1.0.3 API 需要進一步調整

## 專案概述

本專案實現完整的 ETL (Extract, Transform, Load) Pipeline,支援多種文檔格式處理:

### 支援的文檔格式
- **PDF**: 使用 Spring AI PDF Reader
- **Office 文檔**: Word (.docx), Excel (.xlsx), PowerPoint (.pptx) - 使用 Apache Tika
- **Web 內容**: HTML/HTM
- **資料格式**: JSON, Markdown, 純文本
- **圖像文字識別**: PNG, JPG, JPEG, TIFF - 使用 Tesseract OCR
- **壓縮檔案**: ZIP 批次處理

## 核心功能

### 1. Extract (提取)
- 多格式文檔讀取
- OCR 圖像文字提取
- 壓縮檔案自動解壓與處理
- 靈活的 DocumentReader 工廠模式

### 2. Transform (轉換)
- **文檔分塊**: 使用 TokenTextSplitter 智能分塊
- **元資料增強**:
  - 基礎元資料(時間戳、來源檔案)
  - 內容統計(字符數、單詞數、估算tokens)
  - 語言檢測
  - 關鍵詞提取(可選,需 AI)
  - 摘要生成(可選,需 AI)

### 3. Load (載入)
- Neo4j Vector Store 整合
- 批次載入優化
- 錯誤處理與重試機制

## 技術架構

### 核心技術棧
- **Spring Boot**: 3.5.7
- **Spring AI**: 1.0.3
- **Java**: 21
- **Maven**: 3.9+
- **Neo4j**: Vector Store
- **Tesseract**: OCR 引擎 (5.7.0)

### 關鍵組件

#### 模型層 (model/)
- `DataSource` - 資料來源模型
- `EtlPipelineConfig` - ETL Pipeline 配置
- `ChunkingConfig` - 文檔分塊配置
- `MetadataEnrichmentConfig` - 元資料增強配置
- `EtlPipelineResult` - 執行結果

#### 服務層 (service/)
- `EtlPipelineService` - 核心 ETL 服務
- `MultiFormatDocumentReader` - 多格式文檔讀取
- `DocumentChunkingService` - 文檔分塊
- `MetadataEnrichmentService` - 元資料增強
- `TesseractOCRService` - OCR 服務

#### Reader 層 (reader/)
- `DocumentReaderFactory` - Reader 工廠
- `ImageOCRDocumentReader` - 圖像 OCR Reader
- `ArchiveDocumentReader` - 壓縮檔案 Reader

#### 配置層 (config/)
- `OCRProperties` - OCR 配置
- `EtlProperties` - ETL 配置

#### 控制層 (controller/)
- `EtlController` - REST API 接口

## 專案結構

```
src/main/java/com/example/etl/
├── config/
│   ├── EtlProperties.java
│   └── OCRProperties.java
├── controller/
│   └── EtlController.java
├── exception/
│   ├── EtlPipelineException.java
│   └── OCRException.java
├── model/
│   ├── ChunkingConfig.java
│   ├── DataSource.java
│   ├── DataSourceType.java
│   ├── EtlPipelineConfig.java
│   ├── EtlPipelineResult.java
│   ├── LoadConfig.java
│   └── MetadataEnrichmentConfig.java
├── reader/
│   ├── ArchiveDocumentReader.java
│   ├── DocumentReaderFactory.java
│   └── ImageOCRDocumentReader.java
├── service/
│   ├── DocumentChunkingService.java
│   ├── EtlPipelineService.java
│   ├── MetadataEnrichmentService.java
│   ├── MultiFormatDocumentReader.java
│   └── TesseractOCRService.java
└── EtlPipelineApplication.java
```

## 配置說明

### application.yml

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
      embedding:
        options:
          model: text-embedding-3-small
          dimensions: 1536

    neo4j:
      uri: ${NEO4J_URI:bolt://localhost:7687}
      username: ${NEO4J_USERNAME:neo4j}
      password: ${NEO4J_PASSWORD}

app:
  etl:
    chunking:
      default-chunk-size: 1000
      min-chunk-size-chars: 350

    enrichment:
      enable-basic-metadata: true
      enable-content-statistics: true

    ocr:
      tessdata-path: ${TESSDATA_PATH:C:/Program Files/Tesseract-OCR/tessdata}
      language: chi_tra+eng
```

## 環境準備

### 必需環境
1. **Java 21**
   ```bash
   java -version
   ```

2. **Maven 3.9+**
   ```bash
   mvn -version
   ```

3. **Neo4j 資料庫**
   ```bash
   docker run -d \
     -p 7474:7474 -p 7687:7687 \
     -e NEO4J_AUTH=neo4j/password \
     neo4j:latest
   ```

### 可選環境
4. **Tesseract OCR** (如需圖像文字識別)
   - Windows: 下載安裝 https://github.com/UB-Mannheim/tesseract/wiki
   - Mac: `brew install tesseract tesseract-lang`
   - Linux: `apt-get install tesseract-ocr tesseract-ocr-chi-tra`

## API 接口

### 1. 執行 ETL Pipeline
```http
POST /api/etl/pipeline
Content-Type: application/json

{
  "dataSources": [
    {
      "name": "技術文檔",
      "type": "PDF",
      "path": "/path/to/document.pdf"
    }
  ],
  "chunkingConfig": {
    "defaultChunkSize": 1000
  },
  "enrichmentConfig": {
    "enableBasicMetadata": true
  }
}
```

### 2. 上傳並處理文件
```http
POST /api/etl/upload
Content-Type: multipart/form-data

files: [file1.pdf, file2.docx]
chunkSize: 1000
enableEnrichment: true
```

### 3. 健康檢查
```http
GET /api/etl/health
```

## 待辦事項

- [ ] 修正 Spring AI 1.0.3 Reader API 相容性問題
- [ ] 完成編譯測試
- [ ] 添加單元測試
- [ ] 添加整合測試
- [ ] 完善錯誤處理
- [ ] 添加監控指標
- [ ] 優化批次處理性能

## 相關章節

- 7.1-7.2: RAG 基礎系統 (chapter7-rag-basic)
- 7.3-7.4: ETL Pipeline 與多格式文檔處理 (本專案)
- 7.5-7.6: RAG 進階功能 (待開發)

## 開發者

Spring AI ETL Team

## 版本

1.0.0-WIP

## 授權

MIT License
