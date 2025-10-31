# Advanced RAG 系統技術規格文件

## 文檔版本
- **版本**: 1.0.0
- **建立日期**: 2025-01-30
- **最後更新**: 2025-01-30
- **專案代號**: chapter8-advanced-rag

---

## 一、專案概述

### 1.1 專案目標

建立一個企業級的 Advanced RAG（Retrieval-Augmented Generation）系統，提供高精度的知識檢索和智能問答能力。

**核心目標**：
- ✅ 實現多階段智能檢索（粗檢索 + Re-ranking 精檢索）
- ✅ 整合智能 Embedding 模型選擇與優化
- ✅ 提供完整的內容審核和安全防護
- ✅ 建立自動化評估測試框架
- ✅ 實現對話記憶和上下文管理
- ✅ 提供企業級監控和指標收集

### 1.2 技術選型

| 技術組件 | 選型 | 版本 | 說明 |
|---------|------|------|------|
| **Java** | OpenJDK | 21 | 語言運行環境 |
| **Spring Boot** | Spring Boot | 3.5.7 | 應用框架 |
| **Spring AI** | Spring AI | 1.0.3 | AI 整合框架 |
| **Vector Database** | PgVector | Latest | 向量數據庫 |
| **Cache** | Redis | Latest | 快取和會話管理 |
| **LLM Provider** | OpenAI | GPT-4o-mini | 大語言模型 |
| **Embedding Model** | OpenAI | text-embedding-3-small/large | 向量嵌入模型 |
| **Moderation** | OpenAI + Mistral AI | Latest | 內容審核 |
| **Build Tool** | Maven | 3.9+ | 構建工具 |
| **Monitoring** | Micrometer + Prometheus | Latest | 監控系統 |

### 1.3 系統特性

#### 核心功能特性
1. **智能查詢處理**
   - 查詢理解與重寫（Query Rewrite）
   - 查詢擴展（Multi-Query Expansion）
   - 查詢壓縮（Query Compression）
   - 多輪對話上下文理解

2. **多階段檢索優化**
   - 第一階段：粗檢索（Coarse Retrieval）- 使用向量相似度快速篩選
   - 第二階段：Re-ranking 精檢索 - 使用多因子評分精確排序
   - 第三階段：上下文優化 - 智能壓縮和組織檢索結果
   - 第四階段：LLM 生成 - 基於優化上下文生成答案

3. **智能 Embedding 管理**
   - 動態模型選擇（高精度 vs 成本效益）
   - Embedding 快取策略
   - 批量處理優化
   - 效能監控和自動調優

4. **內容安全與品質控制**
   - 多層內容審核（OpenAI + Mistral AI + 自定義規則）
   - 綜合風險評分機制
   - 實時內容安全監控
   - 個人信息保護（PII 檢測）

5. **自動化評估測試**
   - 相關性評估（Relevancy Evaluator）
   - 事實準確性評估（Fact-Checking Evaluator）
   - 完整性和連貫性評估
   - 持續品質監控和告警

6. **企業級特性**
   - 對話記憶管理（ChatMemory）
   - 多租戶支持
   - 詳細的審計日誌
   - 完整的指標收集和監控

---

## 二、系統架構設計

### 2.1 系統脈絡圖（C4 Model - Context Diagram）

```mermaid
graph TB
    subgraph "外部用戶和系統"
        User[企業用戶<br/>查詢知識庫]
        Admin[系統管理員<br/>監控和配置]
        KnowledgeBase[(知識庫<br/>文檔和數據)]
    end

    subgraph "Advanced RAG 系統"
        RAGSystem[Advanced RAG System<br/>智能檢索和問答]
    end

    subgraph "外部服務"
        OpenAI[OpenAI API<br/>LLM + Embedding + Moderation]
        MistralAI[Mistral AI API<br/>Moderation]
        Prometheus[Prometheus<br/>監控系統]
    end

    subgraph "數據存儲"
        PgVector[(PgVector<br/>向量數據庫)]
        Redis[(Redis<br/>快取和會話)]
        FileStorage[(文件存儲<br/>文檔和報告)]
    end

    User -->|提交查詢| RAGSystem
    Admin -->|監控管理| RAGSystem
    KnowledgeBase -->|導入文檔| RAGSystem

    RAGSystem -->|LLM 調用| OpenAI
    RAGSystem -->|內容審核| OpenAI
    RAGSystem -->|內容審核| MistralAI
    RAGSystem -->|向量存儲| PgVector
    RAGSystem -->|快取和會話| Redis
    RAGSystem -->|保存報告| FileStorage
    RAGSystem -->|推送指標| Prometheus

    Admin -->|查看指標| Prometheus
```

### 2.2 容器圖（C4 Model - Container Diagram）

```mermaid
graph TB
    subgraph "Advanced RAG 系統容器"
        WebAPI[Web API<br/>Spring Boot REST API<br/>端口:8080]

        subgraph "核心服務層"
            RAGService[Advanced RAG Service<br/>多階段檢索協調]
            EmbeddingService[Smart Embedding Service<br/>智能模型選擇]
            RerankingService[Reranking Service<br/>精確排序]
            ModerationService[Content Moderation Service<br/>內容審核]
            EvaluationService[AI Evaluation Service<br/>品質評估]
        end

        subgraph "Advisor 層"
            QueryRewriter[Query Rewriter Advisor<br/>查詢重寫]
            QueryExpander[Query Expander Advisor<br/>查詢擴展]
            RerankingAdvisor[Reranking Advisor<br/>結果重排序]
            MemoryAdvisor[Chat Memory Advisor<br/>對話記憶]
            QAAdvisor[Question Answer Advisor<br/>文檔檢索]
        end

        subgraph "監控層"
            MetricsCollector[Metrics Collector<br/>指標收集]
            PerformanceMonitor[Performance Monitor<br/>效能監控]
            AlertService[Alert Service<br/>告警服務]
        end
    end

    subgraph "外部依賴"
        PgVector[(PgVector<br/>向量數據庫)]
        Redis[(Redis<br/>快取/會話)]
        OpenAI[OpenAI API]
        MistralAI[Mistral AI API]
        Prometheus[Prometheus]
    end

    WebAPI --> RAGService
    RAGService --> EmbeddingService
    RAGService --> RerankingService
    RAGService --> ModerationService
    RAGService --> EvaluationService

    RAGService --> QueryRewriter
    RAGService --> QueryExpander
    RAGService --> RerankingAdvisor
    RAGService --> MemoryAdvisor
    RAGService --> QAAdvisor

    RAGService --> MetricsCollector
    MetricsCollector --> PerformanceMonitor
    PerformanceMonitor --> AlertService

    EmbeddingService --> OpenAI
    EmbeddingService --> PgVector
    EmbeddingService --> Redis

    RerankingService --> OpenAI
    ModerationService --> OpenAI
    ModerationService --> MistralAI

    MetricsCollector --> Prometheus
    MemoryAdvisor --> Redis
```

### 2.3 模組關係圖

```mermaid
graph TB
    subgraph "API 層"
        RAGController[RAG Controller]
        EvaluationController[Evaluation Controller]
        ModerationController[Moderation Controller]
        MonitoringController[Monitoring Controller]
    end

    subgraph "服務層 - 核心業務"
        AdvancedRAGService[Advanced RAG Service]
        SmartEmbeddingService[Smart Embedding Service]
        MultiStageRetrievalService[Multi-Stage Retrieval Service]
        RerankingService[Reranking Service]
    end

    subgraph "服務層 - 品質與安全"
        ContentModerationService[Content Moderation Service]
        AIEvaluationService[AI Evaluation Service]
        ContinuousEvaluationService[Continuous Evaluation Service]
    end

    subgraph "Advisor 層"
        QueryRewriterAdvisor[Query Rewriter Advisor]
        QueryExpanderAdvisor[Query Expander Advisor]
        RerankingAdvisor[Reranking Advisor]
        AnswerQualityAdvisor[Answer Quality Advisor]
    end

    subgraph "工具層"
        EmbeddingTextPreprocessor[Embedding Text Preprocessor]
        EmbeddingPerformanceMonitor[Embedding Performance Monitor]
        RerankingMetrics[Reranking Metrics]
    end

    subgraph "配置層"
        EnterpriseRAGConfiguration[Enterprise RAG Configuration]
        MultiEmbeddingConfiguration[Multi-Embedding Configuration]
        ModerationConfiguration[Moderation Configuration]
    end

    subgraph "數據存儲"
        VectorStore[(Vector Store)]
        ChatMemory[(Chat Memory)]
        RedisCache[(Redis Cache)]
    end

    RAGController --> AdvancedRAGService
    EvaluationController --> AIEvaluationService
    ModerationController --> ContentModerationService
    MonitoringController --> ContinuousEvaluationService

    AdvancedRAGService --> SmartEmbeddingService
    AdvancedRAGService --> MultiStageRetrievalService
    AdvancedRAGService --> RerankingService
    AdvancedRAGService --> ContentModerationService

    MultiStageRetrievalService --> QueryRewriterAdvisor
    MultiStageRetrievalService --> QueryExpanderAdvisor
    MultiStageRetrievalService --> RerankingAdvisor

    SmartEmbeddingService --> EmbeddingTextPreprocessor
    SmartEmbeddingService --> EmbeddingPerformanceMonitor
    SmartEmbeddingService --> RedisCache

    RerankingService --> RerankingAdvisor
    RerankingService --> RerankingMetrics

    AIEvaluationService --> AnswerQualityAdvisor
    ContinuousEvaluationService --> AIEvaluationService

    EnterpriseRAGConfiguration --> AdvancedRAGService
    MultiEmbeddingConfiguration --> SmartEmbeddingService
    ModerationConfiguration --> ContentModerationService

    AdvancedRAGService --> VectorStore
    AdvancedRAGService --> ChatMemory
```

---

## 三、關鍵流程設計

### 3.1 Advanced RAG 查詢處理流程圖

```mermaid
flowchart TD
    Start([用戶提交查詢]) --> ValidateInput{驗證輸入}
    ValidateInput -->|無效| ReturnError[返回錯誤]
    ValidateInput -->|有效| ContentModeration[內容審核]

    ContentModeration --> ModerationCheck{審核通過?}
    ModerationCheck -->|否| BlockContent[阻擋內容<br/>返回警告]
    ModerationCheck -->|是| QueryProcessing[查詢處理階段]

    QueryProcessing --> QueryRewrite[查詢重寫<br/>Query Rewriter]
    QueryRewrite --> QueryExpansion[查詢擴展<br/>Multi-Query Expander]
    QueryExpansion --> Stage1[第一階段：粗檢索]

    Stage1 --> CoarseRetrieval[粗檢索<br/>TopK=30, Threshold=0.6]
    CoarseRetrieval --> CheckResults{有結果?}
    CheckResults -->|無| NoResults[無相關文檔<br/>返回無結果提示]
    CheckResults -->|有| Stage2[第二階段：Re-ranking]

    Stage2 --> CalculateScores[計算多因子分數<br/>- 語義相似度<br/>- BM25 分數<br/>- 文檔品質<br/>- 新鮮度]
    CalculateScores --> SortAndFilter[排序和過濾<br/>TopK=5]
    SortAndFilter --> Stage3[第三階段：上下文優化]

    Stage3 --> ContextOptimization[上下文優化<br/>- 去重<br/>- 壓縮<br/>- 格式化]
    ContextOptimization --> Stage4[第四階段：LLM 生成]

    Stage4 --> GeneratePrompt[生成提示詞]
    GeneratePrompt --> CallLLM[調用 LLM]
    CallLLM --> PostProcessing[後處理]

    PostProcessing --> QualityCheck[品質檢查]
    QualityCheck --> RecordMetrics[記錄指標]
    RecordMetrics --> ReturnResponse[返回回應]

    ReturnError --> End([結束])
    BlockContent --> End
    NoResults --> End
    ReturnResponse --> End
```

### 3.2 內容審核流程圖

```mermaid
flowchart TD
    Start([接收內容]) --> CheckEnabled{審核啟用?}
    CheckEnabled -->|否| PassDirectly[直接通過]
    CheckEnabled -->|是| ParallelModeration[並行審核]

    ParallelModeration --> OpenAIModeration[OpenAI 審核]
    ParallelModeration --> MistralModeration[Mistral AI 審核]
    ParallelModeration --> CustomRules[自定義規則審核]

    OpenAIModeration --> ExtractOpenAI[提取類別和分數]
    MistralModeration --> ExtractMistral[提取類別和分數]
    CustomRules --> ExtractCustom[提取類別和分數]

    ExtractOpenAI --> CombineResults[綜合評估]
    ExtractMistral --> CombineResults
    ExtractCustom --> CombineResults

    CombineResults --> CalculateRisk[計算綜合風險分數<br/>- OpenAI 權重: 0.4<br/>- Mistral 權重: 0.4<br/>- Custom 權重: 0.2]

    CalculateRisk --> CheckThreshold{風險分數 >= 閾值?}
    CheckThreshold -->|是| BlockContent[阻擋內容]
    CheckThreshold -->|否| CheckWarning{風險分數 > 0.5?}

    CheckWarning -->|是| Warning[建議人工審查]
    CheckWarning -->|否| Pass[通過審核]

    BlockContent --> PublishEvent[發布審核事件]
    Warning --> PublishEvent
    Pass --> PublishEvent
    PassDirectly --> PublishEvent

    PublishEvent --> RecordMetrics[記錄審核指標]
    RecordMetrics --> ReturnResult[返回審核結果]
    ReturnResult --> End([結束])
```

### 3.3 Re-ranking 處理序列圖

```mermaid
sequenceDiagram
    actor User as 用戶
    participant Controller as RAG Controller
    participant Service as Advanced RAG Service
    participant Retriever as Multi-Stage Retriever
    participant Reranker as Reranking Service
    participant Embedding as Embedding Model
    participant VectorDB as Vector Store
    participant LLM as Chat Client

    User->>Controller: 提交查詢
    Controller->>Service: query(userQuery, options)

    Service->>Service: 驗證和預處理

    rect rgb(240, 248, 255)
        Note over Service,VectorDB: 第一階段：粗檢索
        Service->>Retriever: performCoarseRetrieval()
        Retriever->>VectorDB: similaritySearch(topK=30, threshold=0.6)
        VectorDB-->>Retriever: 返回 30 個候選文檔
        Retriever-->>Service: coarseResults
    end

    rect rgb(255, 248, 240)
        Note over Service,Embedding: 第二階段：Re-ranking
        Service->>Reranker: performReranking(query, candidates)

        Reranker->>Embedding: embedForResponse(query)
        Embedding-->>Reranker: queryEmbedding

        loop 每個候選文檔
            Reranker->>Embedding: embedForResponse(doc.content)
            Embedding-->>Reranker: docEmbedding
            Reranker->>Reranker: calculateScores()<br/>- 語義相似度<br/>- BM25<br/>- 品質<br/>- 新鮮度
        end

        Reranker->>Reranker: sortAndFilter(topK=5)
        Reranker-->>Service: rerankedResults
    end

    rect rgb(240, 255, 240)
        Note over Service,LLM: 第三階段：上下文優化
        Service->>Service: optimizeContext(rerankedResults)
        Service->>Service: 組織上下文文本
    end

    rect rgb(255, 240, 255)
        Note over Service,LLM: 第四階段：LLM 生成
        Service->>LLM: generateResponse(query, context)
        LLM-->>Service: response
    end

    Service->>Service: recordMetrics()
    Service-->>Controller: AdvancedRAGResponse
    Controller-->>User: 返回回應
```

### 3.4 智能 Embedding 選擇流程圖

```mermaid
flowchart TD
    Start([開始 Embedding 請求]) --> CheckContext{檢查上下文}

    CheckContext --> HighAccuracy{需要高精度?}
    HighAccuracy -->|是| UseTextEmbedding3Large[使用 text-embedding-3-large<br/>dimensions: 3072]
    HighAccuracy -->|否| CheckCost{成本敏感?}

    CheckCost -->|是| UseTextEmbedding3SmallLow[使用 text-embedding-3-small<br/>dimensions: 512]
    CheckCost -->|否| CheckBatch{批量處理?}

    CheckBatch -->|是| UseTextEmbedding3SmallLow
    CheckBatch -->|否| UseTextEmbedding3SmallStd[使用 text-embedding-3-small<br/>dimensions: 1024]

    UseTextEmbedding3Large --> CheckCache[檢查快取]
    UseTextEmbedding3SmallLow --> CheckCache
    UseTextEmbedding3SmallStd --> CheckCache

    CheckCache --> CacheHit{快取命中?}
    CacheHit -->|是| ReturnCached[返回快取結果]
    CacheHit -->|否| CallEmbeddingAPI[調用 Embedding API]

    CallEmbeddingAPI --> RecordMetrics[記錄效能指標<br/>- 處理時間<br/>- 文本長度<br/>- 向量維度]

    RecordMetrics --> CacheResult[快取結果<br/>TTL: 24小時]
    CacheResult --> ReturnResult[返回 Embedding 結果]
    ReturnCached --> ReturnResult

    ReturnResult --> UpdateStats[更新模型使用統計]
    UpdateStats --> End([結束])
```

---

## 四、資料模型設計

### 4.1 核心實體 ER 圖

```mermaid
erDiagram
    ADVANCED_RAG_REQUEST ||--o{ DOCUMENT : "retrieves"
    ADVANCED_RAG_REQUEST ||--|| ADVANCED_RAG_RESPONSE : "generates"
    ADVANCED_RAG_RESPONSE ||--o{ DOCUMENT : "includes"

    MODERATION_REQUEST ||--|| MODERATION_RESULT : "produces"
    MODERATION_RESULT ||--o{ MODERATION_CHECK_RESULT : "contains"

    EVALUATION_REQUEST ||--o{ TEST_CASE : "uses"
    EVALUATION_REQUEST ||--|| EVALUATION_REPORT : "generates"
    EVALUATION_REPORT ||--o{ EVALUATION_RESULT : "contains"

    EMBEDDING_CONTEXT ||--|| EMBEDDING_REQUEST : "configures"
    EMBEDDING_REQUEST ||--|| EMBEDDING_RESPONSE : "produces"

    DOCUMENT ||--o{ RERANKING_CANDIDATE : "becomes"
    RERANKING_CANDIDATE ||--|| SCORED_DOCUMENT : "evaluated as"

    ADVANCED_RAG_REQUEST {
        string query PK
        string sessionId FK
        RAGQueryOptions options
        LocalDateTime timestamp
    }

    ADVANCED_RAG_RESPONSE {
        string query PK
        string response
        List documents
        int coarseResultCount
        int finalResultCount
        long processingTime
    }

    DOCUMENT {
        string id PK
        string content
        Map metadata
        double score
        List embedding
    }

    MODERATION_REQUEST {
        string content PK
        string sessionId FK
        string userId
        string contentType
    }

    MODERATION_RESULT {
        boolean passed
        double riskScore
        List flaggedReasons
        Map categoryScores
        string recommendation
    }

    MODERATION_CHECK_RESULT {
        string provider PK
        boolean flagged
        Map categories
        Map scores
    }

    EVALUATION_REQUEST {
        string query PK
        List testCases
        LocalDateTime timestamp
    }

    TEST_CASE {
        string id PK
        string question
        List expectedKeywords
        string expectedContext
        double difficulty
        string category
    }

    EVALUATION_RESULT {
        string testCaseId PK "FK"
        string question
        string response
        double relevancyScore
        double factualAccuracy
        double completeness
        double coherence
        double responseTime
    }

    EVALUATION_REPORT {
        string id PK
        int totalTests
        int passedTests
        double avgRelevancyScore
        double avgFactualityScore
        double overallScore
        Instant timestamp
    }

    EMBEDDING_CONTEXT {
        boolean highAccuracyRequired
        boolean costSensitive
        boolean batchProcessing
        string domain
        string language
        int priority
    }

    RERANKING_CANDIDATE {
        string documentId PK "FK"
        double semanticScore
        double lengthScore
        double keywordScore
        double metadataScore
        double finalScore
    }

    SCORED_DOCUMENT {
        string documentId PK "FK"
        double score
        double semanticScore
        double bm25Score
        double qualityScore
        double freshnessScore
    }
```

### 4.2 配置和狀態資料模型

```mermaid
erDiagram
    RAG_CONFIGURATION ||--|| EMBEDDING_CONFIGURATION : "includes"
    RAG_CONFIGURATION ||--|| MODERATION_CONFIGURATION : "includes"
    RAG_CONFIGURATION ||--|| EVALUATION_CONFIGURATION : "includes"

    MODEL_STATS ||--|| MODEL_PERFORMANCE_REPORT : "generates"
    RERANKING_METRICS ||--|| RERANKING_REPORT : "produces"

    RAG_CONFIGURATION {
        string id PK
        int finalTopK
        double similarityThreshold
        int maxContextLength
        boolean enableReranking
        boolean enableModeration
    }

    EMBEDDING_CONFIGURATION {
        string primaryModel
        string rerankingModel
        int defaultDimensions
        boolean enableCache
        int cacheTTL
    }

    MODERATION_CONFIGURATION {
        boolean enabled
        double threshold
        Map providerWeights
        List sensitiveWords
    }

    EVALUATION_CONFIGURATION {
        boolean continuous
        int interval
        Map thresholds
        List testCaseSources
    }

    MODEL_STATS {
        string modelName PK
        long usageCount
        long totalProcessingTime
        long totalTextLength
        LocalDateTime lastUsed
    }

    MODEL_PERFORMANCE_REPORT {
        string modelName PK
        long totalUsage
        double averageProcessingTime
        double averageTextLength
        double processingSpeed
        LocalDateTime lastUsed
    }

    RERANKING_METRICS {
        long totalRerankings
        long totalProcessingTime
        double averageProcessingTime
    }

    RERANKING_REPORT {
        long totalRerankings
        double averageProcessingTime
        long totalProcessingTime
    }
```

---

## 五、核心類別設計

### 5.1 主要服務類別圖

```mermaid
classDiagram
    class AdvancedRAGService {
        -VectorStore vectorStore
        -EmbeddingModel primaryEmbeddingModel
        -EmbeddingModel rerankingEmbeddingModel
        -ChatClient chatClient
        -RerankingMetrics rerankingMetrics
        +query(userQuery: String, options: RAGQueryOptions) AdvancedRAGResponse
        -performCoarseRetrieval(query: String, options: RAGQueryOptions) List~Document~
        -performReranking(query: String, candidates: List~Document~, options: RAGQueryOptions) List~Document~
        -optimizeContext(documents: List~Document~, options: RAGQueryOptions) String
        -generateResponse(query: String, context: String) String
    }

    class SmartEmbeddingService {
        -EmbeddingModel embeddingModel
        -EmbeddingModelPerformanceMonitor performanceMonitor
        -RedisTemplate redisTemplate
        +embedWithSmartSelection(text: String, context: EmbeddingContext) float[]
        +batchEmbed(texts: List~String~, context: EmbeddingContext) Map~String, float[]~
        -selectModelOptions(context: EmbeddingContext) OpenAiEmbeddingOptions
        -getCachedEmbedding(cacheKey: String) float[]
        -cacheEmbedding(cacheKey: String, embedding: float[]) void
    }

    class AIContentModerationService {
        -OpenAiModerationModel openAiModerationModel
        -MistralAiModerationModel mistralModerationModel
        -ApplicationEventPublisher eventPublisher
        -boolean moderationEnabled
        -double moderationThreshold
        +moderateContent(content: String, context: ModerationContext) ModerationResult
        -performOpenAiModeration(content: String, context: ModerationContext) ModerationCheckResult
        -performMistralModeration(content: String, context: ModerationContext) ModerationCheckResult
        -performCustomRuleModeration(content: String, context: ModerationContext) ModerationCheckResult
        -evaluateResults(results: List~ModerationCheckResult~, context: ModerationContext) ModerationResult
    }

    class AIEvaluationService {
        -ChatClient chatClient
        -RelevancyEvaluator relevancyEvaluator
        -FactCheckingEvaluator factCheckingEvaluator
        -VectorStore vectorStore
        +evaluateRagSystem(testCases: List~TestCase~) EvaluationReport
        -evaluateTestCase(testCase: TestCase) EvaluationResult
        -performCustomEvaluation(testCase: TestCase, response: String, context: List~Content~) CustomEvaluationResult
    }

    class MultiStageRetrievalService {
        -VectorStore vectorStore
        -ChatClient.Builder chatClientBuilder
        +createMultiStageConfig() MultiStageRetrievalConfig
        +performMultiStageRetrieval(queryText: String, filters: Map) List~Document~
        +performExpandedMultiStageRetrieval(queryText: String, numberOfExpansions: int) List~Document~
    }

    AdvancedRAGService --> SmartEmbeddingService
    AdvancedRAGService --> MultiStageRetrievalService
    AdvancedRAGService --> AIContentModerationService
    AdvancedRAGService --> AIEvaluationService
```

### 5.2 Advisor 類別圖

```mermaid
classDiagram
    class CallAroundAdvisor {
        <<interface>>
        +getName() String
        +getOrder() int
        +aroundCall(request: AdvisedRequest, chain: CallAroundAdvisorChain) AdvisedResponse
    }

    class RerankingAdvisor {
        -EmbeddingModel rerankingModel
        -int finalTopK
        -RerankingMetrics metrics
        +getName() String
        +getOrder() int
        +aroundCall(request: AdvisedRequest, chain: CallAroundAdvisorChain) AdvisedResponse
        -performReranking(query: String, documents: List~Document~) List~Document~
        -calculateCosineSimilarity(vec1: List~Double~, vec2: List~Double~) double
        -calculateLengthScore(content: String) double
        -calculateKeywordScore(query: String, content: String) double
        -calculateMetadataScore(metadata: Map) double
        -calculateFinalScore(semanticScore: double, lengthScore: double, keywordScore: double, metadataScore: double) double
    }

    class AnswerQualityAdvisor {
        -ChatClient chatClient
        -double qualityThreshold
        +getName() String
        +getOrder() int
        +aroundCall(request: AdvisedRequest, chain: CallAroundAdvisorChain) AdvisedResponse
        -assessAnswerQuality(answer: String) double
        -checkFactualConsistency(answer: String, context: List~Document~) boolean
    }

    class AdvancedQueryRewriter {
        -ChatClient.Builder chatClientBuilder
        +createRewriteTransformer() QueryTransformer
        +createCompressionTransformer() QueryTransformer
        +createCustomRewritePrompt() PromptTemplate
        +createCustomCompressionPrompt() PromptTemplate
    }

    class AdvancedQueryExpander {
        -ChatClient.Builder chatClientBuilder
        +createMultiQueryExpander(numberOfQueries: int) QueryExpander
        +createTranslationTransformer(targetLanguage: String) QueryTransformer
        +createCustomExpansionPrompt() PromptTemplate
        +createCustomTranslationPrompt() PromptTemplate
    }

    CallAroundAdvisor <|.. RerankingAdvisor
    CallAroundAdvisor <|.. AnswerQualityAdvisor
```

### 5.3 資料傳輸物件（DTO）類別圖

```mermaid
classDiagram
    class AdvancedRAGResponse {
        +String query
        +String response
        +List~Document~ retrievedDocuments
        +int coarseResultCount
        +int finalResultCount
        +long processingTime
    }

    class ModerationResult {
        +boolean passed
        +double riskScore
        +List~String~ flaggedReasons
        +Map~String, Double~ categoryScores
        +String recommendation
        +String errorMessage
        +passed()$ ModerationResult
        +error(message: String)$ ModerationResult
    }

    class ModerationContext {
        +String sessionId
        +String userId
        +String contentType
        +Map~String, Object~ metadata
    }

    class EvaluationReport {
        +int totalTests
        +int passedTests
        +double avgRelevancyScore
        +double avgFactualityScore
        +double avgCompletenessScore
        +double avgCoherenceScore
        +double avgResponseTime
        +double overallScore
        +List~EvaluationResult~ results
        +Instant timestamp
        +String errorMessage
        +empty()$ EvaluationReport
        +error(message: String)$ EvaluationReport
    }

    class EvaluationResult {
        +String testCaseId
        +String question
        +String response
        +double relevancyScore
        +double factualAccuracy
        +double completeness
        +double coherence
        +double responseTime
        +int contextRetrieved
        +String errorMessage
        +error(testCaseId: String, errorMessage: String)$ EvaluationResult
    }

    class TestCase {
        +String id
        +String question
        +List~String~ expectedKeywords
        +String expectedContext
        +String expectedAnswer
        +double difficulty
        +String category
    }

    class EmbeddingContext {
        +boolean highAccuracyRequired
        +boolean costSensitive
        +boolean batchProcessing
        +String domain
        +String language
        +int priority
    }

    class RAGQueryOptions {
        +int finalTopK
        +double similarityThreshold
        +int maxContextLength
        +boolean enableReranking
    }

    class ScoredDocument {
        +Document document
        +double score
        +double semanticScore
        +double bm25Score
        +double qualityScore
        +double freshnessScore
    }

    class RerankingCandidate {
        +Document document
        +double semanticScore
        +double lengthScore
        +double keywordScore
        +double metadataScore
        +double finalScore
    }

    EvaluationReport "1" *-- "many" EvaluationResult
    EvaluationResult --> TestCase
```

---

## 六、關鍵演算法設計

### 6.1 多因子 Re-ranking 評分演算法

**演算法描述**：
結合多個評分因子對檢索結果進行精確排序。

**虛擬碼**：
```
function calculateRerankingScore(query, document):
    // 1. 語義相似度分數（權重 0.4）
    queryEmbedding = embeddingModel.embed(query)
    docEmbedding = embeddingModel.embed(document.content)
    semanticScore = cosineSimilarity(queryEmbedding, docEmbedding)

    // 2. BM25 分數（權重 0.3）
    bm25Score = calculateBM25(query, document.content)

    // 3. 文檔品質分數（權重 0.2）
    qualityScore = 0.5  // 基礎分數
    if 100 <= document.length <= 2000:
        qualityScore += 0.2
    if document.hasStructure():
        qualityScore += 0.1
    if document.metadata.contains("title"):
        qualityScore += 0.1
    if document.metadata.source == "official":
        qualityScore += 0.1

    // 4. 新鮮度分數（權重 0.1）
    daysSinceUpdate = calculateDaysSince(document.lastUpdated)
    if daysSinceUpdate <= 30:
        freshnessScore = 1.0
    else if daysSinceUpdate <= 365:
        freshnessScore = 1.0 - (daysSinceUpdate - 30) / 335.0 * 0.5
    else:
        freshnessScore = 0.5

    // 5. 綜合分數計算
    finalScore = semanticScore * 0.4 +
                 bm25Score * 0.3 +
                 qualityScore * 0.2 +
                 freshnessScore * 0.1

    return finalScore

function performReranking(query, candidates, topK):
    scoredDocuments = []
    for each document in candidates:
        score = calculateRerankingScore(query, document)
        scoredDocuments.add(ScoredDocument(document, score))

    // 按分數降序排序
    scoredDocuments.sortDescending(by: score)

    // 取前 topK 個
    return scoredDocuments.take(topK)
```

**複雜度分析**：
- 時間複雜度：O(n * m)，其中 n 是候選文檔數量，m 是 Embedding 維度
- 空間複雜度：O(n)

### 6.2 BM25 評分演算法

**虛擬碼**：
```
function calculateBM25(query, content):
    k1 = 1.2  // 詞頻飽和參數
    b = 0.75  // 長度正規化參數

    queryTerms = tokenize(query.toLowerCase())
    lowerContent = content.toLowerCase()

    score = 0.0
    for each term in queryTerms:
        termFreq = countOccurrences(lowerContent, term)
        if termFreq > 0:
            // 簡化的 BM25 公式（不考慮 IDF）
            tf = termFreq / (termFreq + k1)
            score += tf

    return score / queryTerms.length
```

### 6.3 內容風險評分演算法

**虛擬碼**：
```
function evaluateModerationResults(results, context):
    categoryScores = Map()
    flaggedProviders = []

    // 1. 收集各提供商的評分
    for each result in results:
        if result.isFlagged():
            flaggedProviders.add(result.provider)

            providerWeight = getProviderWeight(result.provider)

            for each (category, score) in result.scores:
                if categoryScores.contains(category):
                    categoryScores[category] += score * providerWeight
                else:
                    categoryScores[category] = score * providerWeight

    // 2. 計算最終風險分數（取最大值）
    totalRiskScore = max(categoryScores.values())

    // 3. 決策判斷
    shouldBlock = (totalRiskScore >= moderationThreshold)

    // 4. 生成建議
    if shouldBlock:
        recommendation = "建議阻擋此內容，風險分數: " + totalRiskScore
    else if totalRiskScore > 0.5:
        recommendation = "內容可能有風險，建議人工審查，風險分數: " + totalRiskScore
    else:
        recommendation = "內容安全，可以通過"

    return ModerationResult(
        passed = !shouldBlock,
        riskScore = totalRiskScore,
        flaggedReasons = flaggedProviders,
        categoryScores = categoryScores,
        recommendation = recommendation
    )
```

---

## 七、API 設計概覽

### 7.1 RESTful API 端點總覽

| 端點 | 方法 | 說明 | 優先級 |
|------|------|------|--------|
| `/api/v1/rag/query` | POST | Advanced RAG 查詢 | P0 |
| `/api/v1/rag/documents` | POST | 批量添加文檔 | P0 |
| `/api/v1/moderation/check` | POST | 內容審核檢查 | P0 |
| `/api/v1/evaluation/run` | POST | 執行評估測試 | P1 |
| `/api/v1/evaluation/report` | GET | 獲取評估報告 | P1 |
| `/api/v1/monitoring/metrics` | GET | 獲取系統指標 | P1 |
| `/api/v1/monitoring/health` | GET | 健康檢查 | P0 |
| `/api/v1/embedding/models` | GET | 獲取可用 Embedding 模型 | P2 |
| `/api/v1/embedding/performance` | GET | Embedding 效能報告 | P2 |

詳細 API 規範請參考 `api.md` 文檔。

---

## 八、非功能性需求

### 8.1 效能需求

| 指標 | 目標 | 備註 |
|------|------|------|
| **查詢回應時間** | < 5 秒 | P95 |
| **粗檢索時間** | < 1 秒 | P95 |
| **Re-ranking 時間** | < 2 秒 | P95 |
| **LLM 生成時間** | < 3 秒 | P95 |
| **內容審核時間** | < 500 毫秒 | P95 |
| **Embedding 快取命中率** | > 70% | 平均值 |
| **並發請求處理** | 100 QPS | 最低要求 |
| **向量檢索準確率** | > 85% | 基於評估測試 |
| **Re-ranking 提升幅度** | +15~25% | 相對於傳統檢索 |

### 8.2 可靠性需求

- **系統可用性**: 99.5%（允許每月停機 3.6 小時）
- **資料一致性**: 強一致性（向量數據）、最終一致性（快取）
- **錯誤處理**:
  - API 調用失敗自動重試（最多 3 次）
  - 服務降級策略（審核服務失敗不影響主流程）
  - 詳細的錯誤日誌和告警
- **資料備份**: 向量數據每日備份，保留 7 天

### 8.3 安全性需求

- **API 安全**:
  - API Key 認證
  - Rate Limiting（每用戶 100 請求/分鐘）
  - 請求參數驗證
- **數據安全**:
  - 敏感數據加密存儲
  - PII 檢測和脫敏
  - 內容審核多層防護
- **審計日誌**: 記錄所有 API 請求、審核結果、評估報告

### 8.4 可擴展性需求

- **水平擴展**: 支持多實例部署
- **向量數據庫擴展**: 支持 10M+ 文檔向量
- **快取擴展**: Redis Cluster 支持
- **監控擴展**: Prometheus + Grafana 完整監控

### 8.5 可維護性需求

- **日誌**: 結構化日誌（JSON 格式）
- **監控**: 完整的指標收集和告警
- **文檔**: API 文檔、部署文檔、運維手冊
- **測試覆蓋率**: > 70%

---

## 九、部署架構

### 9.1 部署架構圖

```mermaid
graph TB
    subgraph "客戶端"
        WebApp[Web 應用]
        MobileApp[移動應用]
        API_Client[API 客戶端]
    end

    subgraph "負載均衡層"
        LB[Nginx / AWS ALB<br/>負載均衡器]
    end

    subgraph "應用服務層"
        App1[Advanced RAG<br/>Instance 1<br/>8080]
        App2[Advanced RAG<br/>Instance 2<br/>8080]
        App3[Advanced RAG<br/>Instance 3<br/>8080]
    end

    subgraph "數據存儲層"
        subgraph "向量數據庫"
            PgVector_Master[(PgVector Master)]
            PgVector_Replica[(PgVector Replica)]
        end

        subgraph "快取和會話"
            Redis_Master[(Redis Master)]
            Redis_Replica[(Redis Replica)]
        end

        FileStore[(文件存儲<br/>S3 / MinIO)]
    end

    subgraph "外部服務"
        OpenAI[OpenAI API]
        MistralAI[Mistral AI API]
    end

    subgraph "監控和日誌"
        Prometheus[Prometheus<br/>指標收集]
        Grafana[Grafana<br/>可視化]
        ELK[ELK Stack<br/>日誌分析]
    end

    WebApp --> LB
    MobileApp --> LB
    API_Client --> LB

    LB --> App1
    LB --> App2
    LB --> App3

    App1 --> PgVector_Master
    App2 --> PgVector_Master
    App3 --> PgVector_Master

    PgVector_Master -.->|複製| PgVector_Replica

    App1 --> Redis_Master
    App2 --> Redis_Master
    App3 --> Redis_Master

    Redis_Master -.->|複製| Redis_Replica

    App1 --> FileStore
    App2 --> FileStore
    App3 --> FileStore

    App1 --> OpenAI
    App2 --> OpenAI
    App3 --> OpenAI

    App1 --> MistralAI
    App2 --> MistralAI
    App3 --> MistralAI

    App1 --> Prometheus
    App2 --> Prometheus
    App3 --> Prometheus

    Prometheus --> Grafana

    App1 --> ELK
    App2 --> ELK
    App3 --> ELK
```

### 9.2 環境配置

| 環境 | 配置 | 說明 |
|------|------|------|
| **開發環境** | 1 應用實例 + PgVector + Redis | 本地開發 |
| **測試環境** | 2 應用實例 + PgVector + Redis | 功能測試 |
| **UAT 環境** | 2 應用實例 + PgVector Cluster + Redis Cluster | 用戶驗收 |
| **生產環境** | 3+ 應用實例 + PgVector HA + Redis Cluster | 正式服務 |

### 9.3 容器化部署（Docker Compose）

```yaml
version: '3.8'

services:
  advanced-rag-app:
    image: advanced-rag:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - MISTRAL_API_KEY=${MISTRAL_API_KEY}
    depends_on:
      - pgvector
      - redis
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '2'
          memory: 4G

  pgvector:
    image: ankane/pgvector:latest
    environment:
      - POSTGRES_DB=advanced_rag
      - POSTGRES_USER=raguser
      - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
    volumes:
      - pgvector_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}
    volumes:
      - grafana_data:/var/lib/grafana

volumes:
  pgvector_data:
  redis_data:
  prometheus_data:
  grafana_data:
```

---

## 十、監控和指標

### 10.1 關鍵指標（KPI）

#### 業務指標
- **查詢成功率**: 目標 > 95%
- **用戶滿意度**: 基於評估分數，目標 > 4.0/5.0
- **平均回應時間**: 目標 < 5 秒
- **Re-ranking 效果提升**: 目標 +15~25%

#### 技術指標
- **Embedding 快取命中率**: 目標 > 70%
- **向量檢索準確率**: 目標 > 85%
- **內容審核攔截率**: 監控指標，無目標
- **API 錯誤率**: 目標 < 1%

#### 資源指標
- **CPU 使用率**: 目標 < 70%
- **記憶體使用率**: 目標 < 80%
- **向量數據庫連接數**: 監控指標
- **Redis 快取大小**: 監控指標

### 10.2 告警規則

| 告警名稱 | 條件 | 級別 | 處理 |
|---------|------|------|------|
| **高錯誤率** | 錯誤率 > 5% 持續 5 分鐘 | Critical | 立即處理 |
| **高回應時間** | P95 回應時間 > 10 秒 | Warning | 優化性能 |
| **服務不可用** | 健康檢查失敗 | Critical | 立即重啟 |
| **數據庫連接失敗** | 連接失敗 > 3 次 | Critical | 檢查數據庫 |
| **快取失效** | 快取命中率 < 50% | Warning | 檢查 Redis |
| **評估分數過低** | 綜合分數 < 0.7 | Warning | 優化模型 |

---

## 十一、測試策略

### 11.1 測試類型

#### 單元測試
- **目標覆蓋率**: > 70%
- **測試框架**: JUnit 5 + Mockito
- **測試範圍**: 所有服務類、Advisor、工具類

#### 整合測試
- **測試框架**: Spring Boot Test
- **測試範圍**: API 端點、服務整合、資料庫操作

#### 性能測試
- **測試工具**: JMeter / Gatling
- **測試場景**:
  - 正常負載（50 QPS）
  - 峰值負載（100 QPS）
  - 壓力測試（200 QPS）

#### 評估測試
- **自動化評估**: 每小時執行一次
- **測試案例**: > 50 個涵蓋不同場景
- **評估指標**: 相關性、準確性、完整性、連貫性

### 11.2 測試狀態圖

```mermaid
stateDiagram-v2
    [*] --> Idle: 系統初始化

    Idle --> Testing: 開始測試

    Testing --> UnitTest: 執行單元測試
    UnitTest --> UnitTestPassed: 測試通過
    UnitTest --> UnitTestFailed: 測試失敗

    UnitTestFailed --> FixCode: 修復代碼
    FixCode --> Testing

    UnitTestPassed --> IntegrationTest: 執行整合測試
    IntegrationTest --> IntegrationTestPassed: 測試通過
    IntegrationTest --> IntegrationTestFailed: 測試失敗

    IntegrationTestFailed --> FixIntegration: 修復整合問題
    FixIntegration --> Testing

    IntegrationTestPassed --> PerformanceTest: 執行性能測試
    PerformanceTest --> PerformanceAcceptable: 性能可接受
    PerformanceTest --> PerformanceUnacceptable: 性能不可接受

    PerformanceUnacceptable --> OptimizePerformance: 優化性能
    OptimizePerformance --> Testing

    PerformanceAcceptable --> EvaluationTest: 執行評估測試
    EvaluationTest --> EvaluationPassed: 評估通過
    EvaluationTest --> EvaluationFailed: 評估失敗

    EvaluationFailed --> ImproveModel: 改進模型
    ImproveModel --> Testing

    EvaluationPassed --> AllTestsPassed: 所有測試通過
    AllTestsPassed --> [*]: 測試完成
```

---

## 十二、風險評估與應對

### 12.1 技術風險

| 風險項 | 可能性 | 影響 | 應對措施 |
|-------|--------|------|---------|
| **OpenAI API 限流** | 高 | 高 | 實現快取、批量處理、降級策略 |
| **向量數據庫性能瓶頸** | 中 | 高 | 索引優化、讀寫分離、集群部署 |
| **Re-ranking 計算耗時過長** | 中 | 中 | 並行計算、快取、權重調優 |
| **記憶體溢出** | 低 | 高 | 限制批量大小、優化資料結構 |
| **依賴服務不可用** | 中 | 高 | 服務降級、熔斷機制、監控告警 |

### 12.2 業務風險

| 風險項 | 可能性 | 影響 | 應對措施 |
|-------|--------|------|---------|
| **檢索結果不相關** | 中 | 高 | 持續評估、模型優化、調整權重 |
| **內容審核漏報** | 低 | 高 | 多層審核、人工複核、持續更新規則 |
| **用戶體驗不佳** | 中 | 中 | A/B 測試、用戶反饋、持續優化 |
| **成本過高** | 中 | 中 | 成本監控、智能模型選擇、快取優化 |

---

## 十三、開發計劃與里程碑

### 13.1 開發階段

| 階段 | 任務 | 預計時間 | 交付物 |
|------|------|---------|--------|
| **階段一：基礎架構** | 專案初始化、依賴配置、基本架構搭建 | 1 天 | 可運行的專案骨架 |
| **階段二：核心功能** | Advanced RAG 服務、Embedding 服務、多階段檢索 | 3 天 | 基本 RAG 功能 |
| **階段三：Re-ranking** | Re-ranking Advisor、多因子評分、效果評估 | 2 天 | Re-ranking 功能 |
| **階段四：內容審核** | 內容審核服務、多層防護、風險評分 | 2 天 | 內容審核功能 |
| **階段五：評估測試** | 評估框架、持續監控、自動化測試 | 2 天 | 評估測試功能 |
| **階段六：監控優化** | 指標收集、告警配置、性能優化 | 1 天 | 完整監控系統 |
| **階段七：測試部署** | 整合測試、性能測試、部署配置 | 2 天 | 可部署的系統 |

### 13.2 里程碑

- ✅ **M1**: 完成規格文檔（第 1 天）
- 🔲 **M2**: 完成基礎架構和核心功能（第 4 天）
- 🔲 **M3**: 完成 Re-ranking 和內容審核（第 8 天）
- 🔲 **M4**: 完成評估測試和監控（第 11 天）
- 🔲 **M5**: 完成測試和部署準備（第 13 天）

---

## 十四、附錄

### 14.1 術語表

| 術語 | 全稱 | 說明 |
|------|------|------|
| **RAG** | Retrieval-Augmented Generation | 檢索增強生成 |
| **LLM** | Large Language Model | 大語言模型 |
| **Embedding** | Vector Embedding | 向量嵌入/向量化 |
| **Re-ranking** | Re-ranking | 重排序 |
| **BM25** | Best Matching 25 | 一種排序算法 |
| **PII** | Personally Identifiable Information | 個人可識別信息 |
| **QPS** | Queries Per Second | 每秒查詢數 |
| **P95** | 95th Percentile | 第 95 百分位數 |

### 14.2 參考文獻

1. [Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks](https://arxiv.org/abs/2005.11401)
2. [Advanced RAG Techniques](https://arxiv.org/abs/2312.10997)
3. [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
4. [OpenAI API Documentation](https://platform.openai.com/docs/)
5. [PgVector Documentation](https://github.com/pgvector/pgvector)

### 14.3 變更記錄

| 版本 | 日期 | 變更內容 | 作者 |
|------|------|---------|------|
| 1.0.0 | 2025-01-30 | 初始版本 | AI Assistant |

---

**文檔結束**
