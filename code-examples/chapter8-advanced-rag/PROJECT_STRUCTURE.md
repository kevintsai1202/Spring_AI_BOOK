# Advanced RAG 專案結構說明

## 專案目錄結構

```
chapter8-advanced-rag/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── advancedrag/
│   │   │               ├── AdvancedRagApplication.java
│   │   │               │
│   │   │               ├── config/                          # 配置類
│   │   │               │   ├── EnterpriseRAGConfiguration.java
│   │   │               │   ├── MultiEmbeddingConfiguration.java
│   │   │               │   ├── ModerationConfiguration.java
│   │   │               │   ├── RedisConfiguration.java
│   │   │               │   ├── VectorStoreConfiguration.java
│   │   │               │   └── SecurityConfiguration.java
│   │   │               │
│   │   │               ├── controller/                      # 控制器層
│   │   │               │   ├── RAGController.java
│   │   │               │   ├── DocumentController.java
│   │   │               │   ├── ModerationController.java
│   │   │               │   ├── EvaluationController.java
│   │   │               │   └── MonitoringController.java
│   │   │               │
│   │   │               ├── service/                         # 服務層
│   │   │               │   ├── rag/                         # RAG 相關服務
│   │   │               │   │   ├── AdvancedRAGService.java
│   │   │               │   │   ├── MultiStageRetrievalService.java
│   │   │               │   │   └── RerankingService.java
│   │   │               │   │
│   │   │               │   ├── embedding/                   # Embedding 相關服務
│   │   │               │   │   ├── SmartEmbeddingService.java
│   │   │               │   │   ├── EmbeddingTextPreprocessor.java
│   │   │               │   │   └── EmbeddingPerformanceMonitor.java
│   │   │               │   │
│   │   │               │   ├── moderation/                  # 內容審核服務
│   │   │               │   │   ├── AIContentModerationService.java
│   │   │               │   │   └── CustomRuleModerationService.java
│   │   │               │   │
│   │   │               │   ├── evaluation/                  # 評估測試服務
│   │   │               │   │   ├── AIEvaluationService.java
│   │   │               │   │   ├── ContinuousEvaluationService.java
│   │   │               │   │   └── RerankingEvaluationService.java
│   │   │               │   │
│   │   │               │   └── document/                    # 文檔管理服務
│   │   │               │       ├── DocumentManagementService.java
│   │   │               │       └── OptimizedVectorStoreService.java
│   │   │               │
│   │   │               ├── advisor/                         # Advisor 層
│   │   │               │   ├── RerankingAdvisor.java
│   │   │               │   ├── AnswerQualityAdvisor.java
│   │   │               │   ├── AdvancedQueryRewriter.java
│   │   │               │   └── AdvancedQueryExpander.java
│   │   │               │
│   │   │               ├── evaluator/                       # 評估器
│   │   │               │   ├── CustomRelevancyEvaluator.java
│   │   │               │   └── BiasEvaluator.java
│   │   │               │
│   │   │               ├── dto/                             # 數據傳輸對象
│   │   │               │   ├── request/
│   │   │               │   │   ├── AdvancedRAGRequest.java
│   │   │               │   │   ├── ModerationRequest.java
│   │   │               │   │   ├── DocumentAddRequest.java
│   │   │               │   │   └── EvaluationRequest.java
│   │   │               │   │
│   │   │               │   └── response/
│   │   │               │       ├── AdvancedRAGResponse.java
│   │   │               │       ├── ModerationResult.java
│   │   │               │       ├── EvaluationReport.java
│   │   │               │       ├── EvaluationResult.java
│   │   │               │       └── ApiResponse.java
│   │   │               │
│   │   │               ├── model/                           # 領域模型
│   │   │               │   ├── EmbeddingContext.java
│   │   │               │   ├── ModerationContext.java
│   │   │               │   ├── RAGQueryOptions.java
│   │   │               │   ├── ScoredDocument.java
│   │   │               │   ├── RerankingCandidate.java
│   │   │               │   ├── TestCase.java
│   │   │               │   ├── ModelStats.java
│   │   │               │   ├── ModelPerformanceReport.java
│   │   │               │   └── PreprocessingOptions.java
│   │   │               │
│   │   │               ├── metrics/                         # 指標收集
│   │   │               │   ├── RerankingMetrics.java
│   │   │               │   ├── MetricsCollector.java
│   │   │               │   └── PerformanceMonitor.java
│   │   │               │
│   │   │               ├── util/                            # 工具類
│   │   │               │   ├── VectorUtil.java
│   │   │               │   ├── TextUtil.java
│   │   │               │   └── CacheKeyGenerator.java
│   │   │               │
│   │   │               ├── exception/                       # 異常處理
│   │   │               │   ├── AdvancedRAGException.java
│   │   │               │   ├── ModerationException.java
│   │   │               │   ├── EvaluationException.java
│   │   │               │   └── GlobalExceptionHandler.java
│   │   │               │
│   │   │               ├── properties/                      # 配置屬性
│   │   │               │   ├── RAGProperties.java
│   │   │               │   ├── EmbeddingProperties.java
│   │   │               │   ├── ModerationProperties.java
│   │   │               │   └── EvaluationProperties.java
│   │   │               │
│   │   │               └── event/                           # 事件處理
│   │   │                   ├── ContentModerationEvent.java
│   │   │                   ├── EvaluationCompletedEvent.java
│   │   │                   └── SystemAlertEvent.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml                              # 主配置文件
│   │       ├── application-dev.yml                          # 開發環境配置
│   │       ├── application-test.yml                         # 測試環境配置
│   │       ├── application-prod.yml                         # 生產環境配置
│   │       │
│   │       ├── test-cases/                                  # 測試案例
│   │       │   ├── basic-qa.json
│   │       │   ├── domain-specific.json
│   │       │   └── edge-cases.json
│   │       │
│   │       ├── prompts/                                     # 提示詞模板
│   │       │   ├── query-rewrite.txt
│   │       │   ├── query-expansion.txt
│   │       │   └── answer-quality.txt
│   │       │
│   │       ├── static/                                      # 靜態資源
│   │       │   └── swagger-ui/
│   │       │
│   │       └── logback-spring.xml                           # 日誌配置
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── advancedrag/
│                       ├── service/                         # 服務測試
│                       │   ├── AdvancedRAGServiceTest.java
│                       │   ├── SmartEmbeddingServiceTest.java
│                       │   ├── RerankingServiceTest.java
│                       │   ├── ModerationServiceTest.java
│                       │   └── EvaluationServiceTest.java
│                       │
│                       ├── advisor/                         # Advisor 測試
│                       │   ├── RerankingAdvisorTest.java
│                       │   └── AnswerQualityAdvisorTest.java
│                       │
│                       ├── controller/                      # 控制器測試
│                       │   ├── RAGControllerTest.java
│                       │   └── ModerationControllerTest.java
│                       │
│                       ├── integration/                     # 整合測試
│                       │   ├── RAGIntegrationTest.java
│                       │   └── ModerationIntegrationTest.java
│                       │
│                       └── util/                            # 工具類測試
│                           └── VectorUtilTest.java
│
├── docs/                                                    # 文檔目錄
│   ├── spec.md                                              # 技術規格文檔
│   ├── api.md                                               # API 設計文檔
│   ├── PROJECT_STRUCTURE.md                                 # 專案結構說明（本文件）
│   ├── DEPLOYMENT.md                                        # 部署指南
│   └── DEVELOPMENT.md                                       # 開發指南
│
├── scripts/                                                 # 腳本目錄
│   ├── setup.ps1                                            # 環境設置腳本
│   ├── build.ps1                                            # 構建腳本
│   ├── run.ps1                                              # 運行腳本
│   ├── test.ps1                                             # 測試腳本
│   └── deploy.ps1                                           # 部署腳本
│
├── docker/                                                  # Docker 相關文件
│   ├── Dockerfile                                           # 應用 Dockerfile
│   ├── docker-compose.yml                                   # Docker Compose 配置
│   └── prometheus/
│       └── prometheus.yml                                   # Prometheus 配置
│
├── reports/                                                 # 評估報告目錄
│   └── .gitkeep
│
├── .gitignore                                               # Git 忽略文件
├── pom.xml                                                  # Maven 構建文件
└── README.md                                                # 專案說明文件
```

---

## 模組說明

### 核心模組

#### 1. config 包（配置層）
- **EnterpriseRAGConfiguration**: 企業級 RAG 系統配置，整合所有 Advisor
- **MultiEmbeddingConfiguration**: 多 Embedding 模型配置
- **ModerationConfiguration**: 內容審核配置
- **VectorStoreConfiguration**: 向量數據庫配置
- **RedisConfiguration**: Redis 快取配置
- **SecurityConfiguration**: 安全和認證配置

#### 2. controller 包（控制器層）
- **RAGController**: RAG 查詢 API 端點
- **DocumentController**: 文檔管理 API 端點
- **ModerationController**: 內容審核 API 端點
- **EvaluationController**: 評估測試 API 端點
- **MonitoringController**: 監控和指標 API 端點

#### 3. service 包（服務層）

##### rag 子包
- **AdvancedRAGService**: 主要 RAG 服務，協調多階段檢索流程
- **MultiStageRetrievalService**: 多階段檢索服務（粗檢索 + 精檢索）
- **RerankingService**: Re-ranking 服務，精確排序檢索結果

##### embedding 子包
- **SmartEmbeddingService**: 智能 Embedding 服務，動態選擇模型
- **EmbeddingTextPreprocessor**: 文本預處理服務
- **EmbeddingPerformanceMonitor**: Embedding 效能監控

##### moderation 子包
- **AIContentModerationService**: 內容審核服務（OpenAI + Mistral AI + 自定義規則）
- **CustomRuleModerationService**: 自定義規則審核服務

##### evaluation 子包
- **AIEvaluationService**: AI 評估測試服務
- **ContinuousEvaluationService**: 持續評估監控服務
- **RerankingEvaluationService**: Re-ranking 效果評估服務

##### document 子包
- **DocumentManagementService**: 文檔管理服務
- **OptimizedVectorStoreService**: 優化的向量存儲服務

#### 4. advisor 包（Advisor 層）
- **RerankingAdvisor**: Re-ranking Advisor，實現自定義重排序邏輯
- **AnswerQualityAdvisor**: 答案品質控制 Advisor
- **AdvancedQueryRewriter**: 查詢重寫轉換器
- **AdvancedQueryExpander**: 查詢擴展轉換器

#### 5. evaluator 包（評估器）
- **CustomRelevancyEvaluator**: 自定義相關性評估器
- **BiasEvaluator**: 偏見評估器

#### 6. dto 包（數據傳輸對象）

##### request 子包
- **AdvancedRAGRequest**: RAG 查詢請求
- **ModerationRequest**: 內容審核請求
- **DocumentAddRequest**: 文檔添加請求
- **EvaluationRequest**: 評估測試請求

##### response 子包
- **AdvancedRAGResponse**: RAG 查詢回應
- **ModerationResult**: 內容審核結果
- **EvaluationReport**: 評估報告
- **EvaluationResult**: 單個測試案例評估結果
- **ApiResponse**: 統一 API 回應格式

#### 7. model 包（領域模型）
- **EmbeddingContext**: Embedding 上下文配置
- **ModerationContext**: 審核上下文
- **RAGQueryOptions**: RAG 查詢選項
- **ScoredDocument**: 評分文檔
- **RerankingCandidate**: Re-ranking 候選文檔
- **TestCase**: 測試案例
- **ModelStats**: 模型統計數據
- **ModelPerformanceReport**: 模型效能報告
- **PreprocessingOptions**: 預處理選項

#### 8. metrics 包（指標收集）
- **RerankingMetrics**: Re-ranking 指標收集
- **MetricsCollector**: 通用指標收集器
- **PerformanceMonitor**: 效能監控器

#### 9. util 包（工具類）
- **VectorUtil**: 向量計算工具（餘弦相似度等）
- **TextUtil**: 文本處理工具
- **CacheKeyGenerator**: 快取鍵生成器

#### 10. exception 包（異常處理）
- **AdvancedRAGException**: RAG 系統異常
- **ModerationException**: 內容審核異常
- **EvaluationException**: 評估測試異常
- **GlobalExceptionHandler**: 全局異常處理器

#### 11. properties 包（配置屬性）
- **RAGProperties**: RAG 系統配置屬性
- **EmbeddingProperties**: Embedding 配置屬性
- **ModerationProperties**: 審核配置屬性
- **EvaluationProperties**: 評估配置屬性

#### 12. event 包（事件處理）
- **ContentModerationEvent**: 內容審核事件
- **EvaluationCompletedEvent**: 評估完成事件
- **SystemAlertEvent**: 系統告警事件

---

## 資源目錄說明

### 配置文件
- **application.yml**: 主配置文件，包含通用配置
- **application-dev.yml**: 開發環境特定配置
- **application-test.yml**: 測試環境特定配置
- **application-prod.yml**: 生產環境特定配置

### test-cases 目錄
存放評估測試用的測試案例，JSON 格式：
- **basic-qa.json**: 基礎問答測試案例
- **domain-specific.json**: 領域特定測試案例
- **edge-cases.json**: 邊界情況測試案例

### prompts 目錄
存放各種提示詞模板：
- **query-rewrite.txt**: 查詢重寫提示詞
- **query-expansion.txt**: 查詢擴展提示詞
- **answer-quality.txt**: 答案品質評估提示詞

---

## 測試目錄說明

### service 包
單元測試，測試各個服務類的功能。

### advisor 包
測試自定義 Advisor 的邏輯和效果。

### controller 包
測試 API 端點的請求和回應。

### integration 包
整合測試，測試多個組件協同工作的場景。

### util 包
測試工具類的功能。

---

## 腳本目錄說明

### PowerShell 腳本
- **setup.ps1**: 環境設置腳本
  - 檢查 Java 21
  - 檢查 Maven
  - 檢查 Docker（如果需要）
  - 設置環境變數

- **build.ps1**: 構建腳本
  - 清理舊的構建
  - 執行 Maven 編譯
  - 運行單元測試

- **run.ps1**: 運行腳本
  - 設置環境變數（Java 21, API Keys）
  - 啟動 Spring Boot 應用

- **test.ps1**: 測試腳本
  - 執行單元測試
  - 執行整合測試
  - 生成測試報告

- **deploy.ps1**: 部署腳本
  - 構建 Docker 映像
  - 推送到 Docker Registry
  - 部署到目標環境

---

## Docker 目錄說明

### Dockerfile
定義應用的 Docker 映像構建過程。

### docker-compose.yml
定義完整的服務棧：
- Advanced RAG 應用（3 個實例）
- PgVector 數據庫
- Redis 快取
- Prometheus 監控
- Grafana 可視化

### prometheus 目錄
Prometheus 監控配置文件。

---

## 開發規範

### 包命名規範
- 使用小寫字母
- 使用單數形式（如 `service` 而非 `services`）
- 子包按功能分類（如 `service/rag`, `service/embedding`）

### 類命名規範
- 使用帕斯卡命名法（PascalCase）
- 類名應清晰表達其職責
- Service 類以 `Service` 結尾
- Controller 類以 `Controller` 結尾
- Configuration 類以 `Configuration` 結尾
- DTO 類以 `Request` 或 `Response` 結尾

### 方法命名規範
- 使用駝峰命名法（camelCase）
- 方法名應清晰表達其行為
- 布林值返回的方法以 `is`, `has`, `can` 等開頭
- 查詢方法以 `get`, `find`, `query` 等開頭
- 修改方法以 `update`, `modify`, `set` 等開頭

### 註釋規範
- 所有 public 方法必須有 Javadoc 註釋
- 複雜的業務邏輯必須有詳細的中文註釋
- 關鍵參數和返回值必須註釋說明

### 日誌規範
- 使用 SLF4J + Logback
- 使用合適的日誌級別（DEBUG, INFO, WARN, ERROR）
- 重要操作記錄 INFO 日誌
- 異常必須記錄 ERROR 日誌並包含堆棧信息

---

## 依賴管理

### 核心依賴
- Spring Boot 3.5.7
- Spring AI 1.0.3
- Java 21

### 重要依賴
- spring-ai-spring-boot-starter
- spring-ai-openai-spring-boot-starter
- spring-ai-pgvector-store-spring-boot-starter
- spring-ai-advisors-vector-store
- spring-boot-starter-data-redis
- micrometer-registry-prometheus
- lombok

詳細依賴配置參考 `pom.xml`。

---

## 擴展點

### 1. 自定義 Advisor
實現 `CallAroundAdvisor` 接口，添加自定義的處理邏輯。

### 2. 自定義 Evaluator
實現 `Evaluator` 接口，添加自定義的評估邏輯。

### 3. 自定義 Embedding 策略
擴展 `SmartEmbeddingService`，實現自定義的模型選擇策略。

### 4. 自定義 Re-ranking 算法
擴展 `RerankingService`，實現自定義的評分算法。

### 5. 自定義內容審核規則
擴展 `CustomRuleModerationService`，添加企業特定的審核規則。

---

## 環境要求

### 開發環境
- Java 21
- Maven 3.9+
- Docker Desktop（可選）
- IDE: IntelliJ IDEA / Eclipse / VS Code

### 運行環境
- Java 21 Runtime
- PostgreSQL with PgVector
- Redis
- 至少 4GB 記憶體
- OpenAI API Key
- Mistral AI API Key（可選）

---

## 快速開始

```powershell
# 1. 進入專案目錄
cd E:\Spring_AI_BOOK\code-examples\chapter8-advanced-rag

# 2. 設置環境
.\scripts\setup.ps1

# 3. 構建專案
.\scripts\build.ps1

# 4. 運行應用
.\scripts\run.ps1

# 5. 訪問 API
# http://localhost:8080/api/v1/monitoring/health
```

---

## 參考文檔

- [技術規格文檔](spec.md)
- [API 設計文檔](api.md)
- [部署指南](DEPLOYMENT.md)
- [開發指南](DEVELOPMENT.md)

---

**文檔版本**: 1.0.0
**最後更新**: 2025-01-30
