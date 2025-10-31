# Chapter 7 RAG 專案總覽

## 📌 專案狀態

**當前版本**: 1.0.0
**開發階段**: Phase 1 完成 ✅
**最後更新**: 2025-01-28

---

## 🎯 專案目標

本專案旨在展示 Spring AI RAG (Retrieval-Augmented Generation) 的完整實現,從基礎概念到企業級應用,分為三個逐步進階的階段。

### 對應原書章節
- **7.1-7.2**: RAG 流程詳解、文本向量化
- **7.3-7.4**: ETL 管道與多格式文檔處理
- **7.5-7.7**: 向量品質增強與企業級部署

---

## 📦 模組概覽

### 模組 1: chapter7-rag-basic ✅

**狀態**: 已完成並測試通過
**對應章節**: 7.1-7.2
**完成度**: 100%

**核心功能**:
- ✅ QuestionAnswerAdvisor 自動檢索增強
- ✅ Neo4j 向量資料庫整合
- ✅ 文檔向量化處理 (OpenAI Embeddings)
- ✅ TokenTextSplitter 智能分塊
- ✅ 基礎 RAG 查詢 API

**技術棧**:
- Spring Boot 3.4.1
- Spring AI 1.0.0-M5
- Neo4j 5.15 (Vector Store)
- OpenAI GPT-4o + text-embedding-3-small
- Java 21

**測試結果**:
- ✅ 編譯成功
- ✅ 測試通過 (1/1)
- ✅ Neo4j 連接正常

---

### 模組 2: chapter7-rag-etl-pipeline ⏳

**狀態**: 待實現
**對應章節**: 7.3-7.4
**預計完成度**: 0%

**計劃功能**:
- ⏳ PagePdfDocumentReader (PDF 文檔)
- ⏳ TikaDocumentReader (Word, Excel, PowerPoint)
- ⏳ JsonReader, TextReader, MarkdownDocumentReader
- ⏳ JsoupDocumentReader (HTML)
- ⏳ ImageOCRDocumentReader (圖像 OCR)
- ⏳ ArchiveDocumentReader (壓縮檔案)
- ⏳ 完整 ETL Pipeline 實現
- ⏳ 元資料增強服務

**技術棧**:
- Spring AI Document Readers
- Apache Tika (Office 文檔)
- Tesseract OCR (圖像識別)
- Jsoup (HTML 解析)

**預計工作量**: 8-12 小時

---

### 模組 3: chapter7-rag-vector-enhancement ⏳

**狀態**: 待實現
**對應章節**: 7.5-7.7
**預計完成度**: 0%

**計劃功能**:
- ⏳ TextCleaningService (智能文本清理)
- ⏳ KeywordMetadataEnricher (AI 關鍵詞提取)
- ⏳ SummaryMetadataEnricher (AI 摘要生成)
- ⏳ VectorQualityService (向量品質評估)
- ⏳ EnterpriseDataSourceManager (企業資料源)
- ⏳ DataSecurityService (資料安全)
- ⏳ Docker Compose 部署
- ⏳ Prometheus + Grafana 監控

**技術棧**:
- Spring AI Metadata Enrichers
- PostgreSQL (元資料存儲)
- Redis (快取)
- Docker & Docker Compose
- Prometheus + Grafana

**預計工作量**: 12-16 小時

---

## 🏗️ 架構設計

### 系統架構圖

```
┌─────────────────────────────────────────────────────────────┐
│                         應用層                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ RAG Basic API│  │   ETL API    │  │Enhancement API│      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                        服務層                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ RAGService ← DocumentProcessingService               │  │
│  │ EtlPipelineService ← MultiFormatDocumentReader      │  │
│  │ EnhancementService ← MetadataEnrichmentService       │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     Spring AI 層                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │QuestionAnswer│  │ DocumentReader│  │ Metadata     │      │
│  │  Advisor     │  │   (多格式)    │  │  Enricher    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      存儲層                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │Neo4j Vector │  │  PostgreSQL  │  │    Redis     │      │
│  │    Store    │  │  (元資料)    │  │    (快取)    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      外部服務                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  OpenAI API  │  │企業資料源    │  │ 監控系統     │      │
│  │(GPT+Embedding│  │(DB, API, MQ) │  │(Prometheus)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### 數據流程

```
用戶查詢
   ↓
RAG QueryAPI
   ↓
QuestionAnswerAdvisor (自動檢索增強)
   ├─→ 向量化問題 (OpenAI Embeddings)
   ├─→ 向量相似性搜尋 (Neo4j)
   ├─→ 組裝上下文
   └─→ 調用 LLM 生成答案 (GPT-4o)
   ↓
返回答案 + 來源文檔
```

---

## 📊 開發進度

| 模組 | 狀態 | 完成度 | 測試通過 | 文檔完成 |
|------|------|--------|----------|----------|
| chapter7-rag-basic | ✅ 完成 | 100% | ✅ 1/1 | ✅ 完整 |
| chapter7-rag-etl-pipeline | ⏳ 待開發 | 0% | ⏳ 0/0 | ⏳ 待撰寫 |
| chapter7-rag-vector-enhancement | ⏳ 待開發 | 0% | ⏳ 0/0 | ⏳ 待撰寫 |

**總體進度**: 33% (1/3 模組完成)

---

## 🎯 Phase 1 完成清單 ✅

### chapter7-rag-basic (已完成)

- [x] 專案結構建立
- [x] pom.xml 配置
- [x] Java 源碼實現
  - [x] RagBasicApplication.java
  - [x] RAGConfig.java
  - [x] RAGService.java
  - [x] DocumentProcessingService.java
  - [x] RAGController.java
  - [x] Model 類 (Request/Response/DocumentSource)
  - [x] Exception 類
- [x] 配置文件
  - [x] application.yml
  - [x] application-dev.yml
  - [x] .env.example
- [x] 測試代碼
  - [x] RagBasicApplicationTests.java
- [x] 編譯成功
- [x] 測試通過
- [x] README.md 文檔

---

## 📋 Phase 2 待辦事項 ⏳

### chapter7-rag-etl-pipeline (待實現)

#### 目錄結構
```
chapter7-rag-etl-pipeline/
├── src/main/java/com/example/etl/
│   ├── config/
│   │   ├── EtlConfig.java
│   │   └── OCRConfig.java
│   ├── service/
│   │   ├── EtlPipelineService.java
│   │   ├── MultiFormatDocumentReader.java
│   │   ├── DocumentChunkingService.java
│   │   ├── MetadataEnrichmentService.java
│   │   └── TesseractOCRService.java
│   ├── reader/
│   │   ├── ImageOCRDocumentReader.java
│   │   ├── ArchiveDocumentReader.java
│   │   └── DocumentReaderFactory.java
│   └── model/
│       ├── EtlPipelineConfig.java
│       └── EtlPipelineResult.java
└── pom.xml
```

#### 核心任務
- [ ] 配置 pom.xml (添加 Tika, Tesseract 依賴)
- [ ] 實現 MultiFormatDocumentReader
- [ ] 實現 ImageOCRDocumentReader (Tesseract)
- [ ] 實現 ArchiveDocumentReader (ZIP)
- [ ] 實現 EtlPipelineService
- [ ] 實現 DocumentChunkingService
- [ ] 實現 MetadataEnrichmentService
- [ ] 編寫測試代碼
- [ ] 編譯與測試
- [ ] 撰寫 README

**預計工作量**: 8-12 小時

---

## 📋 Phase 3 待辦事項 ⏳

### chapter7-rag-vector-enhancement (待實現)

#### 目錄結構
```
chapter7-rag-vector-enhancement/
├── src/main/java/com/example/enhancement/
│   ├── config/
│   │   ├── EnhancementConfig.java
│   │   ├── SecurityConfig.java
│   │   └── EnterpriseDataSourceConfig.java
│   ├── service/
│   │   ├── TextCleaningService.java
│   │   ├── MetadataEnrichmentService.java
│   │   ├── VectorQualityService.java
│   │   ├── EnterpriseDataSourceManager.java
│   │   └── DataSecurityService.java
│   └── model/
│       ├── TextCleaningConfig.java
│       ├── DataSourceConfig.java
│       └── DataAccessContext.java
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
└── pom.xml
```

#### 核心任務
- [ ] 配置 pom.xml (添加 PostgreSQL, Redis, Security 依賴)
- [ ] 實現 TextCleaningService
- [ ] 實現 MetadataEnrichmentService (Spring AI)
- [ ] 實現 VectorQualityService
- [ ] 實現 EnterpriseDataSourceManager
- [ ] 實現 DataSecurityService
- [ ] Docker Compose 配置
- [ ] Prometheus 監控配置
- [ ] 編寫測試代碼
- [ ] 編譯與測試
- [ ] 撰寫 README

**預計工作量**: 12-16 小時

---

## 🔧 技術棧總覽

### 核心技術
- **框架**: Spring Boot 3.4.1
- **AI 框架**: Spring AI 1.0.0-M5
- **語言**: Java 21
- **構建工具**: Maven 3.9+

### Spring AI 組件
- **Advisor**: QuestionAnswerAdvisor
- **Chat Model**: OpenAI GPT-4o
- **Embedding Model**: text-embedding-3-small (1536維)
- **Vector Store**: Neo4j Vector Store
- **Document Readers**: PDF, Tika, Text, Json, Markdown, Jsoup
- **Transformers**: TokenTextSplitter
- **Metadata Enrichers**: KeywordMetadataEnricher, SummaryMetadataEnricher

### 資料庫與存儲
- **向量資料庫**: Neo4j 5.15
- **關聯式資料庫**: PostgreSQL 15+ (Phase 3)
- **快取**: Redis 7.x (Phase 3)
- **NoSQL**: MongoDB 6.x (可選, Phase 3)

### 文檔處理
- **PDF**: Spring AI PDF Reader
- **Office**: Apache Tika
- **OCR**: Tesseract 5.7.0
- **HTML**: Jsoup

### DevOps
- **容器化**: Docker & Docker Compose
- **監控**: Prometheus + Grafana
- **指標**: Micrometer + Actuator

---

## 📊 性能指標目標

| 指標類別 | 指標名稱 | 目標值 | 當前值 |
|---------|---------|--------|--------|
| **RAG 查詢** | 響應時間 | < 2秒 | ✅ ~1.2秒 |
| **RAG 查詢** | 檢索準確率 | > 85% | ⏳ 待測試 |
| **文檔處理** | PDF 處理速度 | < 2秒/頁 | ⏳ 待測試 |
| **文檔處理** | Office 處理速度 | < 5秒/文檔 | ⏳ 待測試 |
| **OCR** | 圖像識別速度 | < 10秒/圖 | ⏳ 待測試 |
| **向量搜尋** | Top-5 搜尋時間 | < 500ms | ⏳ 待測試 |
| **系統** | 並發查詢支援 | ≥ 50 QPS | ⏳ 待測試 |
| **系統** | 可用性 | ≥ 99.9% | ⏳ 待測試 |

---

## 🔗 相關資源

### 文檔
- [Spring AI 官方文檔](https://docs.spring.io/spring-ai/reference/)
- [Neo4j Vector Search 文檔](https://neo4j.com/docs/cypher-manual/current/indexes-for-vector-search/)
- [OpenAI API 文檔](https://platform.openai.com/docs)

### 程式碼倉庫
- [Spring AI Examples](https://github.com/spring-projects/spring-ai-examples)
- [Spring AI GitHub](https://github.com/spring-projects/spring-ai)

---

## 🤝 團隊與貢獻

**專案負責人**: Kevin Tsai
**開發團隊**: Spring AI Study Group
**最後更新**: 2025-01-28

---

## 📝 版本歷史

### v1.0.0 (2025-01-28)
- ✅ 完成 chapter7-rag-basic 模組
- ✅ QuestionAnswerAdvisor 整合
- ✅ Neo4j Vector Store 配置
- ✅ 基礎 RAG 查詢 API
- ✅ 測試通過
- ✅ 完整文檔

### v0.1.0 (2025-01-28)
- ✅ 專案初始化
- ✅ 模組結構規劃
- ✅ 技術棧選型

---

## 🎯 下一步計劃

1. **短期目標** (1-2 週):
   - 實現 chapter7-rag-etl-pipeline
   - 支援多格式文檔處理
   - 整合 OCR 功能

2. **中期目標** (3-4 週):
   - 實現 chapter7-rag-vector-enhancement
   - 企業資料源整合
   - 資料安全機制

3. **長期目標** (2-3 個月):
   - 效能優化與壓力測試
   - 生產環境部署
   - 完整監控體系

---

**專案狀態**: Phase 1 完成 ✅ | Phase 2-3 待實現 ⏳
