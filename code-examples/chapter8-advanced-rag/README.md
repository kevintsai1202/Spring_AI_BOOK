# Advanced RAG 系統

> 企業級智能檢索增強生成（Retrieval-Augmented Generation）系統

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.3-blue.svg)](https://docs.spring.io/spring-ai/reference/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 目錄

- [專案簡介](#專案簡介)
- [核心特性](#核心特性)
- [技術架構](#技術架構)
- [快速開始](#快速開始)
- [配置說明](#配置說明)
- [API 文檔](#api-文檔)
- [專案結構](#專案結構)
- [開發指南](#開發指南)
- [測試](#測試)
- [部署](#部署)
- [監控與運維](#監控與運維)
- [常見問題](#常見問題)
- [貢獻指南](#貢獻指南)
- [許可證](#許可證)

---

## 專案簡介

Advanced RAG 系統是一個基於 Spring AI 框架開發的企業級智能檢索增強生成系統，提供高精度的知識檢索和智能問答能力。

### 設計理念

- **多階段檢索優化**: 實現粗檢索 + Re-ranking 精檢索的兩階段策略
- **智能 Embedding 管理**: 動態選擇最佳 Embedding 模型，平衡精度和成本
- **內容安全防護**: 多層內容審核機制，確保輸出內容安全
- **持續品質監控**: 自動化評估測試，持續優化系統性能
- **企業級特性**: 完整的監控、日誌、告警和指標收集

### 應用場景

- ✅ **企業知識庫問答**: 為企業內部知識管理提供智能檢索和問答
- ✅ **客戶服務系統**: 智能客服機器人，提供準確的產品和服務信息
- ✅ **技術文檔助手**: 幫助開發者快速查找技術文檔和 API 說明
- ✅ **法律合規諮詢**: 智能檢索法律法規，提供合規建議
- ✅ **醫療知識查詢**: 醫療專業知識的精確檢索和解答

---

## 核心特性

### 🚀 多階段智能檢索

```
用戶查詢 → 查詢處理 → 粗檢索(Top-30) → Re-ranking(Top-5) → 上下文優化 → LLM 生成
```

- **第一階段 - 粗檢索**: 使用向量相似度快速篩選 Top-30 候選文檔
- **第二階段 - Re-ranking**: 使用多因子評分精確排序，選擇 Top-5 最相關文檔
- **第三階段 - 上下文優化**: 智能壓縮和組織檢索結果
- **第四階段 - LLM 生成**: 基於優化上下文生成高品質答案

### 🧠 智能 Embedding 管理

- **動態模型選擇**: 根據查詢需求自動選擇最佳 Embedding 模型
  - 高精度需求: `text-embedding-3-large` (3072 維)
  - 成本效益: `text-embedding-3-small` (512 維)
  - 標準配置: `text-embedding-3-small` (1024 維)

- **智能快取策略**: Redis 快取 Embedding 結果，快取命中率 > 70%
- **批量處理優化**: 支持批量 Embedding 生成，提升處理效率
- **效能監控**: 實時監控各模型的使用情況和性能指標

### 🛡️ 多層內容審核

- **OpenAI Moderation**: 檢測性、暴力、仇恨等不當內容
- **Mistral AI Moderation**: 補充審核，提供多維度風險評估
- **自定義規則審核**:
  - 企業敏感詞檢測
  - 個人信息識別 (PII)
  - 企業政策合規檢查

- **綜合風險評分**:
  - OpenAI 權重: 40%
  - Mistral AI 權重: 40%
  - 自定義規則權重: 20%

### 📊 自動化評估測試

- **相關性評估**: 檢測答案與問題的相關性
- **事實準確性評估**: 驗證答案的事實性
- **完整性評估**: 評估答案的完整程度
- **連貫性評估**: 檢查答案的邏輯連貫性
- **持續監控**: 每小時自動執行評估測試

### 🎯 Re-ranking 精確排序

基於多因子評分的 Re-ranking 算法：

- **語義相似度** (權重 40%): 基於 Embedding 的語義匹配
- **BM25 分數** (權重 30%): 關鍵詞匹配度
- **文檔品質** (權重 20%): 文檔長度、結構、元數據
- **新鮮度** (權重 10%): 文檔更新時間

**效果提升**: Re-ranking 使檢索精度提升 15-25%

### 💬 對話記憶管理

- **會話持久化**: 基於 Redis 的會話存儲
- **上下文理解**: 支持多輪對話的上下文追蹤
- **記憶優化**: 智能壓縮對話歷史，節省 token 消耗

### 📈 完整監控體系

- **Prometheus + Grafana**: 實時指標監控和可視化
- **指標收集**:
  - 查詢成功率、回應時間
  - Embedding 快取命中率
  - 內容審核統計
  - Re-ranking 效果指標
- **告警機制**: 異常情況自動告警

---

## 技術架構

### 技術棧

| 技術組件 | 版本 | 用途 |
|---------|------|------|
| Spring Boot | 3.5.7 | 應用框架 |
| Spring AI | 1.0.3 | AI 整合框架 |
| Java | 21 | 開發語言 |
| OpenAI API | Latest | LLM + Embedding + Moderation |
| Mistral AI API | Latest | Moderation |
| PgVector | Latest | 向量數據庫 |
| Redis | 7+ | 快取和會話管理 |
| Prometheus | Latest | 指標收集 |
| Grafana | Latest | 可視化監控 |
| Maven | 3.9+ | 構建工具 |

### 系統架構圖

```
┌─────────────────────────────────────────────────────────┐
│                      用戶層                               │
│  Web 應用  │  移動應用  │  API 客戶端  │  管理後台       │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────┐
│                   API 網關層（可選）                       │
│              Nginx / AWS ALB / Kong                     │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────┐
│              Advanced RAG 應用層                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  REST API  │  WebSocket  │  Health Check        │   │
│  └──────────────────────┬──────────────────────────┘   │
│  ┌──────────────────────┴──────────────────────────┐   │
│  │  RAG Service  │  Embedding  │  Moderation        │   │
│  │  Evaluation  │  Document Mgmt  │  Monitoring     │   │
│  └──────────────────────┬──────────────────────────┘   │
│  ┌──────────────────────┴──────────────────────────┐   │
│  │  Advisors: Reranking | Query Rewrite | Memory   │   │
│  └─────────────────────────────────────────────────┘   │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────┐
│                    數據層                                │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐   │
│  │  PgVector   │  │    Redis    │  │ File Storage │   │
│  │  向量數據庫  │  │  快取/會話   │  │   S3/MinIO   │   │
│  └─────────────┘  └─────────────┘  └──────────────┘   │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────┐
│                  外部服務層                               │
│  ┌──────────────────────────────────────────────────┐   │
│  │  OpenAI API  │  Mistral AI API  │  Prometheus   │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## 快速開始

### 前置需求

1. **Java 21**
   ```powershell
   # 檢查 Java 版本
   java -version
   # 應顯示: openjdk version "21.x.x"
   ```

2. **Maven 3.9+**
   ```powershell
   # 檢查 Maven 版本
   mvn -version
   ```

3. **Docker Desktop** (可選，用於本地運行數據庫)
   ```powershell
   # 檢查 Docker 版本
   docker --version
   ```

4. **API Keys**
   - OpenAI API Key: [https://platform.openai.com/api-keys](https://platform.openai.com/api-keys)
   - Mistral AI API Key (可選): [https://console.mistral.ai/](https://console.mistral.ai/)

### 環境設置

#### 1. 設置環境變數

```powershell
# 設置 Java 21
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 設置 API Keys
$env:OPENAI_API_KEY="your-openai-api-key-here"
$env:MISTRAL_API_KEY="your-mistral-api-key-here"  # 可選
```

#### 2. 啟動依賴服務（使用 Docker）

```powershell
# 進入專案目錄
cd E:\Spring_AI_BOOK\code-examples\chapter8-advanced-rag

# 啟動 PostgreSQL + PgVector + Redis
docker-compose up -d pgvector redis

# 檢查服務狀態
docker-compose ps
```

#### 3. 構建專案

```powershell
# 清理並編譯
mvn clean compile

# 或者執行測試
mvn clean test
```

#### 4. 運行應用

```powershell
# 方式 1: 使用 Maven
mvn spring-boot:run

# 方式 2: 使用腳本（推薦）
.\scripts\run.ps1
```

#### 5. 驗證運行

```powershell
# 健康檢查
curl http://localhost:8080/api/v1/monitoring/health

# 或在瀏覽器訪問
# http://localhost:8080/api/v1/monitoring/health
```

### 測試 API

#### 1. RAG 查詢測試

```bash
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "什麼是 Spring AI？",
    "options": {
      "finalTopK": 5,
      "enableReranking": true
    }
  }'
```

#### 2. 內容審核測試

```bash
curl -X POST http://localhost:8080/api/v1/moderation/check \
  -H "Content-Type: application/json" \
  -d '{
    "content": "這是一個測試內容"
  }'
```

#### 3. 添加文檔測試

```bash
curl -X POST http://localhost:8080/api/v1/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Spring AI 是一個用於構建 AI 應用的框架...",
    "metadata": {
      "title": "Spring AI 簡介",
      "source": "official",
      "category": "技術文檔"
    }
  }'
```

---

## 配置說明

### application.yml 配置

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-3-small
          dimensions: 1024
      moderation:
        enabled: true
        model: text-moderation-latest

    mistralai:
      api-key: ${MISTRAL_API_KEY}
      moderation:
        enabled: true
        model: mistral-moderation-latest

  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 3000ms

# RAG 系統配置
app:
  rag:
    final-top-k: 5
    similarity-threshold: 0.7
    max-context-length: 4000
    enable-reranking: true

  embedding:
    primary-model: text-embedding-3-small
    reranking-model: text-embedding-3-small
    default-dimensions: 1024
    enable-cache: true
    cache-ttl: 86400  # 24 hours

  moderation:
    enabled: true
    threshold: 0.8
    providers:
      openai:
        weight: 0.4
        enabled: true
      mistral:
        weight: 0.4
        enabled: true
      custom:
        weight: 0.2
        enabled: true

  evaluation:
    continuous: true
    interval: 3600000  # 1 hour
    thresholds:
      relevancy: 0.8
      factuality: 0.85
      completeness: 0.7
      coherence: 0.75
      response_time: 2000  # ms
      overall: 0.8
```

### 環境特定配置

#### 開發環境 (application-dev.yml)
```yaml
logging:
  level:
    com.example.advancedrag: DEBUG
    org.springframework.ai: DEBUG

app:
  moderation:
    enabled: false  # 開發環境可選擇關閉審核
```

#### 生產環境 (application-prod.yml)
```yaml
logging:
  level:
    com.example.advancedrag: INFO
    org.springframework.ai: WARN

app:
  moderation:
    enabled: true
    threshold: 0.9  # 生產環境提高審核閾值
```

---

## API 文檔

完整的 API 文檔請參考：[API 設計文檔](docs/api.md)

### 主要端點

| 端點 | 方法 | 說明 |
|------|------|------|
| `/api/v1/rag/query` | POST | RAG 查詢 |
| `/api/v1/rag/query/batch` | POST | 批量查詢 |
| `/api/v1/documents` | POST | 添加文檔 |
| `/api/v1/documents/batch` | POST | 批量添加文檔 |
| `/api/v1/documents/{id}` | DELETE | 刪除文檔 |
| `/api/v1/moderation/check` | POST | 內容審核 |
| `/api/v1/evaluation/run` | POST | 執行評估測試 |
| `/api/v1/evaluation/reports` | GET | 獲取評估報告 |
| `/api/v1/monitoring/metrics` | GET | 獲取系統指標 |
| `/api/v1/monitoring/health` | GET | 健康檢查 |

---

## 專案結構

詳細的專案結構說明請參考：[專案結構文檔](docs/PROJECT_STRUCTURE.md)

### 核心目錄

```
chapter8-advanced-rag/
├── src/main/java/com/example/advancedrag/
│   ├── config/                    # 配置類
│   ├── controller/                # 控制器
│   ├── service/                   # 服務層
│   │   ├── rag/                   # RAG 服務
│   │   ├── embedding/             # Embedding 服務
│   │   ├── moderation/            # 審核服務
│   │   ├── evaluation/            # 評估服務
│   │   └── document/              # 文檔管理
│   ├── advisor/                   # Advisor 層
│   ├── dto/                       # 數據傳輸對象
│   ├── model/                     # 領域模型
│   ├── metrics/                   # 指標收集
│   └── exception/                 # 異常處理
├── src/main/resources/
│   ├── application.yml            # 主配置
│   ├── test-cases/                # 測試案例
│   └── prompts/                   # 提示詞模板
├── docs/                          # 文檔目錄
│   ├── spec.md                    # 技術規格
│   ├── api.md                     # API 文檔
│   └── PROJECT_STRUCTURE.md       # 結構說明
└── scripts/                       # 腳本目錄
```

---

## 開發指南

### 代碼規範

1. **命名規範**
   - 類名: PascalCase
   - 方法名: camelCase
   - 常量: UPPER_SNAKE_CASE
   - 包名: lowercase

2. **註釋規範**
   - 所有 public 方法必須有 Javadoc 註釋（中文）
   - 複雜邏輯必須有行內註釋說明
   - 重要參數和返回值必須註釋

3. **日誌規範**
   - 使用 SLF4J + Logback
   - 使用合適的日誌級別
   - 異常必須記錄完整堆棧

### 添加自定義 Advisor

```java
@Component
public class CustomAdvisor implements CallAroundAdvisor {

    @Override
    public String getName() {
        return "CustomAdvisor";
    }

    @Override
    public int getOrder() {
        return 5;  // 設置執行順序
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        // 前置處理
        log.info("Before processing: {}", request.userText());

        // 調用鏈
        AdvisedResponse response = chain.nextAroundCall(request);

        // 後置處理
        log.info("After processing: {}", response);

        return response;
    }
}
```

### 添加自定義評估器

```java
@Component
public class CustomEvaluator implements Evaluator {

    @Override
    public EvaluationResponse evaluate(EvaluationRequest request) {
        // 實現自定義評估邏輯
        boolean passed = performCustomEvaluation(request);

        return new EvaluationResponse(passed, "評估完成");
    }

    private boolean performCustomEvaluation(EvaluationRequest request) {
        // 自定義評估邏輯
        return true;
    }
}
```

---

## 測試

### 運行測試

```powershell
# 運行所有測試
mvn test

# 運行特定測試類
mvn test -Dtest=AdvancedRAGServiceTest

# 運行整合測試
mvn verify
```

### 測試覆蓋率

```powershell
# 生成測試覆蓋率報告
mvn jacoco:report

# 查看報告
# target/site/jacoco/index.html
```

### 評估測試

```powershell
# 手動觸發評估測試
curl -X POST http://localhost:8080/api/v1/evaluation/run/immediate

# 查看評估報告
curl http://localhost:8080/api/v1/evaluation/reports
```

---

## 部署

### Docker 部署

#### 1. 構建 Docker 映像

```powershell
# 構建應用映像
docker build -t advanced-rag:latest -f docker/Dockerfile .
```

#### 2. 使用 Docker Compose 部署

```powershell
# 啟動完整服務棧
docker-compose up -d

# 查看服務狀態
docker-compose ps

# 查看日誌
docker-compose logs -f advanced-rag-app
```

#### 3. 停止服務

```powershell
docker-compose down
```

### 生產環境部署

詳細的部署指南請參考：[部署文檔](docs/DEPLOYMENT.md)

---

## 監控與運維

### Prometheus 指標

訪問：`http://localhost:8080/actuator/prometheus`

### Grafana 儀表板

訪問：`http://localhost:3000`

默認賬號：`admin` / `admin`

### 關鍵指標

- **查詢成功率**: `rag_query_success_rate`
- **平均回應時間**: `rag_response_time_avg`
- **Embedding 快取命中率**: `embedding_cache_hit_rate`
- **內容審核攔截率**: `moderation_block_rate`
- **Re-ranking 處理時間**: `reranking_processing_time`

### 日誌查看

```powershell
# 查看應用日誌
tail -f logs/spring.log

# 查看錯誤日誌
tail -f logs/error.log
```

---

## 常見問題

### Q1: OpenAI API 調用失敗怎麼辦？

**A**:
1. 檢查 API Key 是否正確設置
2. 檢查網絡連接
3. 查看 OpenAI API 狀態：[https://status.openai.com/](https://status.openai.com/)
4. 檢查是否達到速率限制

### Q2: 向量檢索沒有結果？

**A**:
1. 確認已添加文檔到向量數據庫
2. 檢查 `similarity-threshold` 設置是否過高
3. 嘗試降低閾值或增加 `topK` 數量

### Q3: Re-ranking 速度慢？

**A**:
1. 減少候選文檔數量（調整粗檢索的 topK）
2. 使用更快的 Embedding 模型
3. 啟用 Embedding 快取

### Q4: 記憶體使用過高？

**A**:
1. 調整 JVM 參數：`-Xmx4g -Xms2g`
2. 限制批量處理的文檔數量
3. 清理過期的快取數據

### Q5: 如何提升檢索準確率？

**A**:
1. 使用更高維度的 Embedding 模型
2. 啟用 Re-ranking 功能
3. 優化文本預處理策略
4. 調整評分權重配置
5. 增加高品質的訓練數據

---

## 性能優化建議

### 1. Embedding 優化
- 啟用快取（快取命中率目標 > 70%）
- 使用批量處理
- 根據場景選擇合適的模型維度

### 2. 檢索優化
- 調整粗檢索和精檢索的數量比例
- 優化向量索引配置
- 使用讀寫分離

### 3. 快取優化
- Redis Cluster 部署
- 設置合理的 TTL
- 定期清理過期數據

### 4. 併發優化
- 調整線程池配置
- 使用異步處理
- 實現請求限流

---

## 貢獻指南

歡迎貢獻代碼、報告問題或提出建議！

### 貢獻流程

1. Fork 專案
2. 創建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

### 代碼審查標準

- 遵循代碼規範
- 包含單元測試
- 通過所有測試
- 更新相關文檔

---

## 版本歷史

### v1.0.0 (2025-01-30)
- ✅ 初始版本發布
- ✅ 實現多階段智能檢索
- ✅ 整合智能 Embedding 管理
- ✅ 實現內容審核功能
- ✅ 建立評估測試框架
- ✅ 完成監控和指標收集

---

## 相關資源

### 文檔
- [技術規格文檔](docs/spec.md)
- [API 設計文檔](docs/api.md)
- [專案結構文檔](docs/PROJECT_STRUCTURE.md)
- [部署指南](docs/DEPLOYMENT.md)

### 參考資料
- [Spring AI 官方文檔](https://docs.spring.io/spring-ai/reference/)
- [OpenAI API 文檔](https://platform.openai.com/docs/)
- [PgVector 文檔](https://github.com/pgvector/pgvector)
- [RAG 技術論文](https://arxiv.org/abs/2005.11401)

---

## 許可證

本專案採用 MIT 許可證。詳見 [LICENSE](LICENSE) 文件。

---

## 聯繫方式

- **專案主頁**: [GitHub](https://github.com/your-org/advanced-rag)
- **問題反饋**: [Issues](https://github.com/your-org/advanced-rag/issues)
- **電子郵件**: support@your-domain.com
- **技術文檔**: [Documentation](https://docs.your-domain.com)

---

## 致謝

感謝以下開源專案和技術社區：

- [Spring Framework](https://spring.io/)
- [Spring AI](https://spring.io/projects/spring-ai)
- [OpenAI](https://openai.com/)
- [PgVector](https://github.com/pgvector/pgvector)
- [Prometheus](https://prometheus.io/)

---

**© 2025 Advanced RAG Project. All rights reserved.**
