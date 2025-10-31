# Chapter 7.5-7.7: RAG 向量增強與企業級優化

## 專案簡介

本專案實現 RAG 系統的向量品質增強、企業資料整合和生產級部署功能，涵蓋以下章節：

- **7.5 ETL (下) - 給向量資料加上 Buff**: 智能文本清理、語義分塊、AI 元資料增強、向量品質評估
- **7.6 企業 RAG 真正的資料來源**: 企業資料來源整合（資料庫、API、即時同步）
- **7.7 RAG 的最後一哩路**: 資料安全、權限控制、生產級部署與監控

## 技術棧

- **基礎框架**: Spring Boot 3.5.7
- **AI 框架**: Spring AI 1.0.3
- **LLM**: OpenAI GPT-4 (gpt-4o)
- **Embedding**: OpenAI text-embedding-3-small
- **向量資料庫**: Neo4j 5.15.0（統一存儲向量和元資料）
- **監控**: Prometheus + Grafana
- **Java**: JDK 21
- **構建工具**: Maven 3.9+

## 核心功能

### 7.5 向量品質增強

1. **智能文本清理**
   - 多語言支援（中文、英文、日文）
   - 移除敏感資訊（郵件、電話、身分證）
   - 標準化處理（空白、換行、特殊字符）
   - 自定義清理規則

2. **元資料增強**
   - 基礎元資料（時間戳、內容哈希、統計）
   - 語言檢測
   - AI 關鍵詞提取（使用 Spring AI KeywordMetadataEnricher）
   - AI 摘要生成（使用 Spring AI SummaryMetadataEnricher）
   - 自定義分類

3. **向量品質評估**
   - 維度驗證
   - 範數檢查
   - 品質評分
   - 異常向量檢測

### 7.6 企業資料整合

1. **多資料源支援**
   - NoSQL 資料庫（MongoDB）
   - REST API
   - 文件系統
   - 訊息佇列（Kafka）
   - 雲端存儲
   - 所有資料統一存儲於 Neo4j 向量資料庫

2. **資料同步模式**
   - 全量同步（Full Sync）
   - 增量同步（Incremental Sync）
   - 即時同步（Real-time Sync with CDC）

3. **連接器管理**
   - 動態連接器註冊
   - 健康檢查
   - 自動重連
   - 連接池管理

### 7.7 生產級部署

1. **資料安全**
   - 身份驗證與授權
   - 角色權限控制
   - 資料脫敏（部分、完全、雜湊、代幣化）
   - 審計日誌

2. **監控與告警**
   - Prometheus 指標收集
   - Grafana 視覺化
   - 健康檢查
   - 效能監控

3. **Docker 部署**
   - 容器化部署
   - Docker Compose 編排
   - 環境變數配置

## 專案結構

```
chapter7-rag-vector-enhancement/
├── src/
│   ├── main/
│   │   ├── java/com/example/enhancement/
│   │   │   ├── config/              # 配置類
│   │   │   ├── controller/          # REST API Controllers
│   │   │   ├── service/             # 核心服務
│   │   │   ├── model/               # 資料模型
│   │   │   ├── security/            # 安全相關
│   │   │   ├── exception/           # 異常處理
│   │   │   ├── connector/           # 資料源連接器
│   │   │   └── EnhancementApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   └── test/
│       └── java/com/example/enhancement/
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── prometheus/
│   └── prometheus.yml
├── pom.xml
└── README.md
```

## 快速開始

### 前置需求

- ✅ JDK 21
- ✅ Maven 3.9+
- ✅ Docker & Docker Compose
- ✅ OpenAI API Key

### 環境變數設定

```powershell
# 設定 OpenAI API Key（必須）
$env:OPENAI_API_KEY="your_openai_api_key"
```

**注意**: Neo4j 密碼已在 `docker-compose.yml` 中預先配置為 `test1234`，無需額外設定。

### 🚀 三步驟啟動

#### 步驟 1: 啟動 Docker 服務

```powershell
# 啟動 Neo4j 向量資料庫
.\start-services.ps1
```

這將啟動：
- **Neo4j 5.15.0** (向量資料庫 + 元資料存儲) - http://localhost:7474

#### 步驟 2: 啟動應用程式

```powershell
# 運行 Spring Boot 應用
.\run-app.ps1
```

#### 步驟 3: 訪問服務

- **Neo4j Browser**: http://localhost:7474
  - 用戶名: `neo4j`
  - 密碼: `test1234`
- **應用程式**: http://localhost:8080
- **Actuator**: http://localhost:8080/actuator/health

### 停止服務

```powershell
# 停止所有服務
.\stop-services.ps1
```

---

## 📖 詳細文檔

- **Neo4j 設定**: 請參閱 [NEO4J_SETUP.md](./NEO4J_SETUP.md)
- **測試報告**: 請參閱 [FINAL_TEST_REPORT.md](./FINAL_TEST_REPORT.md)
- **專案規格**: 請參閱 [chapter7-rag-vector-enhancement-spec.md](./chapter7-rag-vector-enhancement-spec.md)

## API 文檔

應用程式啟動後，可訪問以下端點：

- **應用程式**: http://localhost:8080
- **Actuator**: http://localhost:8080/actuator
- **Health Check**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **Prometheus UI**: http://localhost:9090
- **Grafana**: http://localhost:3000

## 效能指標

| 指標 | 目標值 | 說明 |
|------|--------|------|
| 文本清理速度 | < 100ms/文檔 | 包含所有清理規則 |
| 元資料增強速度 | < 2秒/文檔 | 包含 AI 關鍵詞和摘要 |
| 向量品質評估 | < 500ms/100向量 | 批次評估 |
| 資料同步延遲 | < 30秒 | 即時同步模式 |
| 權限檢查速度 | < 10ms/請求 | 快取命中 |
| 系統可用性 | ≥ 99.9% | 正常運行時間 |
| 並發處理能力 | ≥ 100 QPS | 同時處理請求 |

## 測試

### 執行測試

```bash
# 執行單元測試
mvn test

# 執行整合測試
mvn verify

# 生成測試覆蓋率報告
mvn clean test jacoco:report
```

### 測試結果（2025-10-29）

✅ **測試狀態**: 全部通過

| 指標 | 結果 |
|------|------|
| 總測試數 | 15 |
| 通過 | 15 ✅ |
| 失敗 | 0 |
| 錯誤 | 0 |
| 成功率 | 100% |

**測試模組**:
- ✅ IntegrationTest（5 個測試）- 0.122s
- ✅ LanguageDetectionServiceTest（10 個測試）- 0.027s

詳細測試報告請參閱 [TEST_REPORT.md](./TEST_REPORT.md)

## 相關文檔

- [專案規格書](./chapter7-rag-vector-enhancement-spec.md)
- [Spring AI 官方文檔](https://docs.spring.io/spring-ai/reference/)
- [Neo4j Vector Store](https://docs.spring.io/spring-ai/reference/api/vectordbs/neo4j.html)

## 授權

MIT License

## 作者

Spring AI Enhancement Team
