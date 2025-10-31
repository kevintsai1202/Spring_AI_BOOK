# Chapter 7: RAG 實作與優化

> Spring AI RAG (Retrieval-Augmented Generation) 完整實現

---

## 📖 章節概述

本章節深入探討 RAG 系統的完整實現,從基礎概念到企業級應用,涵蓋三個逐步進階的模組。

### 對應原始文章章節
- **7.1-7.2**: RAG 流程詳解、文本向量化
- **7.3-7.4**: ETL 管道與多格式文檔處理
- **7.5-7.7**: 向量品質增強與企業級部署

---

## 📦 模組結構

```
chapter7-rag/
│
├── chapter7-rag-basic/              ✅ RAG 基礎系統 (7.1-7.2)
│   ├── QuestionAnswerAdvisor 自動檢索增強
│   ├── Neo4j 向量資料庫整合
│   ├── 文檔向量化處理
│   └── 基礎 RAG 查詢 API
│
├── chapter7-rag-etl-pipeline/       ⏳ ETL 管道系統 (7.3-7.4)
│   ├── 多格式文檔讀取 (PDF, Word, Excel, PPT, HTML, JSON)
│   ├── ETL Pipeline 實現
│   ├── OCR 圖像文字識別
│   └── 壓縮檔案批次處理
│
├── chapter7-rag-vector-enhancement/ ⏳ 向量增強系統 (7.5-7.7)
│   ├── 智能文本清理與預處理
│   ├── AI 元資料增強 (關鍵詞、摘要)
│   ├── 企業資料源整合 (PostgreSQL, MongoDB, API)
│   ├── 資料安全與權限控制
│   └── 生產級部署與監控
│
├── PROJECT_OVERVIEW.md              專案總覽
└── README.md                        本文件
```

---

## 🎯 學習路徑

### 階段 1: RAG 基礎系統 (chapter7-rag-basic) ✅

**學習目標**:
- ✅ 理解 RAG 的核心流程
- ✅ 掌握文檔向量化處理
- ✅ 使用 Spring AI QuestionAnswerAdvisor
- ✅ 配置 Neo4j 向量資料庫
- ✅ 實現基礎 RAG 查詢 API

**核心內容**:
1. **RAG 流程** (7.1)
   - 文檔檢索 (Retrieval)
   - 上下文組裝
   - 生成增強 (Generation)

2. **文本向量化** (7.2)
   - OpenAI Embeddings API
   - TokenTextSplitter 文本分塊
   - 向量相似性搜尋

**啟動命令**:
```bash
cd chapter7-rag-basic
mvn spring-boot:run
```

**狀態**: ✅ 已完成並測試通過

---

### 階段 2: ETL 管道系統 (chapter7-rag-etl-pipeline) ⏳

**學習目標**:
- 掌握多種文檔格式處理
- 實現完整的 ETL Pipeline
- 使用 Tesseract OCR 識別圖像
- 處理壓縮檔案批次導入

**核心內容**:
1. **多格式文檔讀取** (7.3)
   - PagePdfDocumentReader (PDF)
   - TikaDocumentReader (Office 文檔)
   - JsonReader, TextReader, MarkdownDocumentReader

2. **進階文件類型** (7.4)
   - ImageOCRDocumentReader (圖像識別)
   - ArchiveDocumentReader (壓縮檔案)
   - 遞迴處理與批次導入

**預計實現內容**:
- ✅ 多格式 DocumentReader 實現
- ✅ ETL Pipeline 服務
- ✅ OCR 服務整合
- ✅ 元資料增強

**狀態**: ⏳ 待實現

---

### 階段 3: 向量增強系統 (chapter7-rag-vector-enhancement) ⏳

**學習目標**:
- 提升向量品質與檢索效果
- 整合企業級資料來源
- 實現資料安全機制
- 生產級部署與監控

**核心內容**:
1. **向量品質增強** (7.5)
   - 智能文本清理
   - AI 元資料增強 (關鍵詞、摘要)
   - 向量品質評估

2. **企業資料整合** (7.6)
   - PostgreSQL、MongoDB 整合
   - REST API 資料源
   - 即時同步與 CDC

3. **生產級部署** (7.7)
   - Docker Compose 部署
   - Prometheus + Grafana 監控
   - 資料安全與權限控制

**預計實現內容**:
- ✅ TextCleaningService
- ✅ MetadataEnrichmentService (Spring AI)
- ✅ EnterpriseDataSourceManager
- ✅ DataSecurityService
- ✅ Docker 部署配置

**狀態**: ⏳ 待實現

---

## 🚀 快速開始

### 前置要求

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- OpenAI API Key

### 環境變數設定

```bash
export OPENAI_API_KEY=your-openai-api-key
export NEO4J_PASSWORD=your-neo4j-password
```

### 完整啟動流程

#### 1. 啟動基礎 RAG 系統

```bash
# 啟動 Neo4j
docker run -d \
  --name neo4j-rag \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/test1234 \
  neo4j:5.15

# 等待 Neo4j 啟動
sleep 30

# 啟動應用
cd chapter7-rag-basic
mvn spring-boot:run
```

測試端點:
```bash
# 健康檢查
curl http://localhost:8080/api/rag/health

# RAG 查詢
curl -X POST http://localhost:8080/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什麼是 Spring AI?",
    "topK": 5,
    "similarityThreshold": 0.7
  }'
```

---

## 📊 模組依賴關係

```
chapter7-rag-basic (基礎 RAG)
    ↓ (依賴)
chapter7-rag-etl-pipeline (ETL 管道)
    ↓ (依賴)
chapter7-rag-vector-enhancement (向量增強)
```

**注意**: 必須按順序學習和實現各模組

---

## 📚 核心概念對照表

| 章節 | 核心概念 | 對應模組 | Spring AI 組件 |
|------|---------|---------|---------------|
| 7.1 | RAG 流程詳解 | basic | QuestionAnswerAdvisor |
| 7.2 | 文本向量化 | basic | EmbeddingModel, TokenTextSplitter |
| 7.3 | 多格式文檔讀取 | etl-pipeline | DocumentReader (PDF, Tika, Json) |
| 7.4 | 進階文件處理 | etl-pipeline | OCR, Archive 處理 |
| 7.5 | 向量品質增強 | vector-enhancement | KeywordMetadataEnricher, SummaryMetadataEnricher |
| 7.6 | 企業資料整合 | vector-enhancement | VectorStore, DataSource 整合 |
| 7.7 | 生產級部署 | vector-enhancement | Docker, Monitoring |

---

## 🔧 配置範例

### application.yml 完整配置

```yaml
spring:
  application:
    name: chapter7-rag
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

    vectorstore:
      neo4j:
        uri: ${NEO4J_URI:bolt://localhost:7687}
        username: ${NEO4J_USERNAME:neo4j}
        password: ${NEO4J_PASSWORD}
        index-name: document-embeddings
        embedding-dimension: 1536

app:
  rag:
    top-k: 5
    similarity-threshold: 0.7
    chunk-size: 800
    chunk-overlap: 200
```

---

## 🧪 測試指南

### 運行所有測試

```bash
# 測試基礎模組
cd chapter7-rag-basic && mvn test

# 測試 ETL 模組
cd ../chapter7-rag-etl-pipeline && mvn test

# 測試向量增強模組
cd ../chapter7-rag-vector-enhancement && mvn test
```

### 測試覆蓋範圍

- ✅ chapter7-rag-basic: 1/1 測試通過
- ⏳ chapter7-rag-etl-pipeline: 待實現
- ⏳ chapter7-rag-vector-enhancement: 待實現

---

## 🐛 常見問題

### Q1: Neo4j 連接失敗

**解決方案**:
1. 確認 Docker 容器已啟動: `docker ps`
2. 檢查密碼是否正確
3. 等待 30 秒讓 Neo4j 完全啟動

### Q2: OpenAI API 錯誤

**解決方案**:
1. 確認 API Key 是否正確
2. 檢查 API 額度是否足夠
3. 確認網路連接正常

### Q3: 文檔處理失敗

**解決方案**:
1. 確認文件格式是否支援
2. 檢查文件大小限制
3. 查看日誌中的詳細錯誤訊息

---

## 📊 性能指標

| 指標 | 目標值 | 說明 |
|------|--------|------|
| RAG 查詢響應時間 | < 2秒 | 包含檢索和生成 |
| 文檔處理時間 | < 5秒/文檔 | 包含解析和分塊 |
| 向量搜尋時間 | < 500ms | Top-5 相似性搜尋 |
| 並發查詢支援 | ≥ 50 QPS | 同時處理的查詢數量 |
| 系統可用性 | ≥ 99.9% | 正常運行時間 |

---

## 🔗 相關資源

### 官方文檔
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Spring AI RAG](https://docs.spring.io/spring-ai/reference/api/rag.html)
- [Spring AI Advisors](https://docs.spring.io/spring-ai/reference/api/advisors.html)
- [Spring AI Vector Stores](https://docs.spring.io/spring-ai/reference/api/vectordbs.html)

### 向量資料庫
- [Neo4j Vector Search](https://neo4j.com/docs/cypher-manual/current/indexes-for-vector-search/)
- [OpenAI Embeddings](https://platform.openai.com/docs/guides/embeddings)

---

## 🤝 貢獻

歡迎提交 Issue 和 Pull Request!

---

## 📄 授權

本專案採用 MIT 授權

---

## 📝 版本資訊

- **版本**: 1.0.0
- **Spring Boot**: 3.4.1
- **Spring AI**: 1.0.0-M5
- **Java**: 21
- **最後更新**: 2025-01-28

---

## 🎓 下一步學習

1. **完成基礎模組**: 熟悉 chapter7-rag-basic 的所有功能
2. **學習 ETL 管道**: 進入 chapter7-rag-etl-pipeline 學習多格式文檔處理
3. **掌握向量增強**: 學習 chapter7-rag-vector-enhancement 的企業級功能
4. **進階優化**: 學習 Chapter 8 的進階 RAG 技術

---

**維護者**: Kevin Tsai
**專案狀態**: Phase 1 完成 ✅ | Phase 2-3 待實現 ⏳
