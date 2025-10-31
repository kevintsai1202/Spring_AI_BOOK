# Chapter 6.6-6.7: 向量記憶系統 (Memory Vector)

## 📖 專案概述

本專案實現了基於 **Neo4j 向量資料庫的長期記憶系統**,結合短期和長期記憶,實現智能語義搜尋和記憶管理。

### 對應教學章節
- 6.6 安裝和配置 Neo4j 向量資料庫
- 6.7 使用向量資料庫作為對話的長久記憶

### 依賴關係
- 依賴 `chapter6-memory-core` 的基礎記憶和 Advisor 系統

---

## 🎯 核心功能

### 1. Neo4j 向量資料庫系統 (Vector Database)

#### 1.1 向量索引管理
- **創建向量索引**: 文檔、訊息、查詢向量
- **相似性函數**: Cosine、Euclidean、Dot Product
- **索引優化**: HNSW 參數調整
- **索引監控**: 狀態和效能追蹤

#### 1.2 向量存儲實現
- **Neo4jVectorStore**: Spring AI 官方實現
- **文檔嵌入**: 自動向量化
- **相似性搜尋**: 基於向量距離
- **批量操作**: 高效批量插入和刪除

#### 1.3 向量資料庫配置
- **Docker 部署**: Docker Compose 配置
- **記憶體優化**: Heap 和 PageCache 調整
- **性能監控**: 數據庫統計和指標收集

---

### 2. 長期記憶系統 (Long-term Memory)

#### 2.1 VectorStoreChatMemoryAdvisor
- **語義搜尋**: 基於 Embedding 的相似性檢索
- **記憶調用**: 自動檢索相關歷史訊息
- **相似性閾值**: 可配置的召回策略

#### 2.2 混合記憶架構
- **短期記憶整合**: 最近的對話上下文
- **長期記憶整合**: 語義相關的歷史對話
- **記憶融合**: 相關性排序和優先級

#### 2.3 記憶同步機制
- **自動同步**: 定期將短期訊息同步到向量資料庫
- **手動同步**: 按需觸發同步操作
- **增量更新**: 只同步新增的訊息

---

### 3. 智能檢索系統 (Smart Retrieval)

#### 3.1 多層次檢索
- **短期檢索**: 最近訊息的順序存取
- **長期檢索**: 向量相似性搜尋
- **融合檢索**: 結合兩種策略的混合檢索

#### 3.2 檢索策略
- **相似性閾值過濾**: 排除低相關性結果
- **排名算法**: 基於相關性和時間的多因素排名
- **動態策略**: 根據查詢類型選擇檢索方式

#### 3.3 檢索效能優化
- **快取機制**: 常見查詢的快取
- **批量檢索**: 多個查詢的並行執行
- **索引優化**: 向量索引參數調整

---

### 4. 企業級記憶管理 (Memory Management)

#### 4.1 記憶分析
- **統計分析**: 訊息數量、字符數等
- **品質評估**: 相關性、完整性、時效性
- **使用趨勢**: 記憶訪問模式分析

#### 4.2 記憶生命週期
- **自動清理**: 過期記憶的定期清理
- **備份恢復**: 記憶的備份和恢復機制
- **版本管理**: 記憶版本跟蹤

#### 4.3 記憶監控告警
- **效能監控**: 檢索延遲、吞吐量監控
- **健康檢查**: 資料庫連接和索引狀態
- **告警機制**: 異常情況的自動告警

---

### 5. REST API 服務層

#### 5.1 向量搜尋端點
- `POST /api/memory/search` - 語義搜尋
- `POST /api/memory/retrieve/{conversationId}` - 檢索記憶

#### 5.2 向量記憶端點
- `POST /api/memory/vector-chat/{conversationId}` - 向量記憶對話
- `POST /api/memory/hybrid-chat/{conversationId}` - 混合記憶對話

#### 5.3 管理端點
- `GET /api/memory/analytics/{conversationId}` - 記憶分析
- `POST /api/memory/sync/{conversationId}` - 同步記憶
- `POST /api/memory/backup` - 備份記憶
- `DELETE /api/memory/{conversationId}` - 刪除記憶

---

## 🏗️ 架構設計

```
┌──────────────────────────────────────────────────────────────┐
│         Spring AI Vector Memory System                        │
├──────────────────────────────────────────────────────────────┤
│  REST API Layer                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ VectorChatController  MemoryAnalyticsController        │  │
│  └────────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────────┤
│  Service Layer                                               │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ HybridMemoryService                                    │  │
│  │ SmartMemoryRetrievalService                            │  │
│  │ MemorySyncService                                      │  │
│  │ MemoryAnalyticsService                                 │  │
│  └────────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────────┤
│  Memory & Advisor Layer                                      │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ VectorStoreChatMemoryAdvisor                           │  │
│  │ MessageChatMemoryAdvisor (from core)                   │  │
│  │ MemorySyncAdvisor                                      │  │
│  └────────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────────┤
│  Vector Store & Search Layer                                 │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ VectorStore (Neo4j Implementation)                     │  │
│  │ EmbeddingModel (OpenAI)                                │  │
│  │ SearchRequest/SearchResult                             │  │
│  └────────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────────┤
│  Memory Storage Layer                                        │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ ChatMemory (Short-term from core)                      │  │
│  │ Neo4j Vector Index (Long-term)                         │  │
│  └────────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────────┤
│  Embedding & ML Layer                                        │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ OpenAI Embedding API                                   │  │
│  │ Vector Normalization                                   │  │
│  │ Similarity Metrics (Cosine, Euclidean, etc.)          │  │
│  └────────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────────┤
│  Database & Infrastructure Layer                             │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Neo4j Database (Vector Index)                          │  │
│  │ PostgreSQL (Metadata Store)                            │  │
│  │ Redis Cache (Optional)                                 │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

---

## 📊 資料模型

### Document 文檔模型
```java
public record Document(
    String id,                           // 文檔 ID
    String content,                      // 內容
    float[] embedding,                   // 向量嵌入
    Map<String, Object> metadata,        // 元數據
    LocalDateTime createdAt,             // 創建時間
    LocalDateTime updatedAt              // 更新時間
) { }

// 元數據結構
{
    "conversationId": "conv-123",
    "messageType": "USER" | "ASSISTANT",
    "messageIndex": 0,
    "timestamp": "2024-01-01T12:00:00",
    "previousMessageType": "ASSISTANT",
    "userId": "user-456",
    "topic": "spring-boot"
}
```

### SearchRequest 搜尋請求
```java
public record SearchRequest(
    String query,                        // 搜尋查詢文本
    int topK,                            // 返回前 K 個結果
    double similarityThreshold,          // 相似性閾值
    String filterExpression              // 過濾表達式
) { }
```

### MemoryRetrievalResult 檢索結果
```java
public record MemoryRetrievalResult(
    String conversationId,
    String query,
    List<Message> shortTermMemories,
    List<Document> longTermMemories,
    List<MemoryItem> fusedMemories,
    LocalDateTime retrievalTime
) { }
```

### MemoryItem 記憶項目
```java
public record MemoryItem(
    String content,
    MemoryType type,                    // SHORT_TERM | LONG_TERM
    double relevanceScore,              // 相關性分數 [0-1]
    LocalDateTime timestamp,
    Map<String, Object> metadata
) { }
```

---

## 🔄 關鍵流程

### 流程1: 混合記憶對話流程

```
用戶輸入
    ↓
ChatClient.prompt()
    ↓
┌─────────────────────────────────────┐
│  短期記憶檢索                        │
│  MessageChatMemoryAdvisor          │
│  └─ 獲取最近 20 條訊息              │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│  向量化用戶查詢                      │
│  OpenAI Embedding API               │
│  └─ 生成 1536 維向量                │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│  向量搜尋 (Cosine Similarity)      │
│  Neo4j Vector Index                 │
│  └─ 返回相似性 > 0.75 的前 10 條  │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│  記憶融合和排序                      │
│  SmartMemoryRetrievalService        │
│  ├─ 短期記憶權重: 60%               │
│  └─ 長期記憶權重: 40%               │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│  構建增強提示詞                      │
│  "Based on your past conversations: │
│   [短期訊息] [長期訊息]             │
│   Please answer: [用戶查詢]"        │
└─────────────────────────────────────┘
    ↓
調用 LLM 模型
    ↓
獲取 AI 回應
    ↓
保存到短期記憶
    ↓
┌─────────────────────────────────────┐
│  異步同步到長期記憶                  │
│  MemorySyncService                  │
│  └─ 每 5 分鐘或訊息數 > 10 時觸發  │
└─────────────────────────────────────┘
    ↓
返回用戶
```

### 流程2: 向量同步流程

```
短期記憶中有新訊息
    ↓
定時或手動觸發同步
    ↓
讀取新訊息 (最近 10 條)
    ↓
轉換為 Document 對象
    ├─ 內容: 訊息文本
    ├─ 元數據: conversationId, messageType 等
    └─ 向量: 通過 EmbeddingModel 生成
    ↓
Neo4j VectorStore.add()
    ├─ 創建 Document 節點
    ├─ 創建 Vector 索引項
    └─ 添加元數據關係
    ↓
更新最後同步時間戳
    ↓
完成
```

### 流程3: 智能檢索流程

```
接收搜尋查詢
    ↓
分析查詢特徵
    ├─ 包含時間詞 (昨天、之前)?
    ├─ 包含具體實體?
    └─ 查詢長度和複雜度?
    ↓
決定檢索策略
    ├─ 短期優先 (時間相關)
    ├─ 長期優先 (語義相關)
    └─ 混合檢索 (默認)
    ↓
執行檢索
    ├─ 短期: ChatMemory.get(conversationId)
    └─ 長期: VectorStore.similaritySearch()
    ↓
融合結果
    ├─ 轉換為統一的 MemoryItem 格式
    ├─ 計算綜合相關性分數
    │  score = position_weight * 0.6 + semantic_weight * 0.4
    └─ 按分數排序
    ↓
返回 Top-K 結果 (通常 10-20 項)
    ↓
完成
```

---

## 💾 虛擬碼 (Pseudocode)

### 混合記憶服務
```pseudocode
Class HybridMemoryService {

    Function chatWithHybridMemory(conversationId, userMessage) {
        // 1. 構建 Advisor
        shortTermAdvisor = MessageChatMemoryAdvisor.builder(shortTermMemory)
            .conversationId(conversationId)
            .build()

        longTermAdvisor = VectorStoreChatMemoryAdvisor.builder(longTermMemory)
            .conversationId(conversationId)
            .topK(5)
            .similarityThreshold(0.75)
            .build()

        // 2. 執行對話 (一次性添加所有 Advisor)
        response = chatClient.prompt()
            .advisors(shortTermAdvisor, longTermAdvisor)
            .user(userMessage)
            .call()
            .content()

        // 3. 返回
        Return response
    }

    Function chatWithSmartMemory(conversationId, userMessage) {
        // 1. 分析查詢類型
        strategy = determineMemoryStrategy(userMessage)

        // 2. 根據策略執行
        Switch strategy {
            Case SHORT_TERM_ONLY:
                Return chatWithShortTermMemory(conversationId, userMessage)

            Case LONG_TERM_ONLY:
                Return chatWithLongTermMemory(conversationId, userMessage)

            Case HYBRID (Default):
                Return chatWithHybridMemory(conversationId, userMessage)
        }
    }

    Function determineMemoryStrategy(userMessage) {
        If userMessage.contains("剛才") OR userMessage.contains("剛剛"):
            Return MemoryStrategy.SHORT_TERM_ONLY

        If userMessage.contains("之前") OR userMessage.contains("記得"):
            Return MemoryStrategy.LONG_TERM_ONLY

        Return MemoryStrategy.HYBRID
    }
}
```

### 向量記憶同步
```pseudocode
Class MemorySyncService {

    @Scheduled(fixedRate = 300000)  // 5 分鐘
    Function syncMemories() {
        activeConversationIds = getActiveConversationIds()

        For each conversationId in activeConversationIds {
            syncConversationMemory(conversationId)
        }
    }

    Function syncConversationMemory(conversationId) {
        // 1. 獲取短期記憶
        messages = shortTermMemory.get(conversationId)

        If messages.isEmpty():
            Return

        // 2. 選擇要同步的訊息 (最近 10 條)
        messagesToSync = messages.size() > 10 ?
            messages.subList(0, 10) : messages

        // 3. 轉換為 Document
        documents = convertMessagesToDocuments(conversationId, messagesToSync)

        // 4. 添加到 VectorStore (自動向量化)
        // VectorStore.add() 會使用 EmbeddingModel 自動向量化文檔
        vectorStore.add(documents)

        // 5. 記錄同步時間
        recordSyncTimestamp(conversationId)
    }

    Function convertMessagesToDocuments(conversationId, messages) {
        documents = []

        For i = 0 to messages.size() {
            message = messages[i]

            // 只處理用戶和助手訊息
            If message instanceof UserMessage OR message instanceof AssistantMessage {
                metadata = {
                    "conversationId": conversationId,
                    "messageType": message.getClass().getSimpleName(),
                    "timestamp": LocalDateTime.now().toString(),
                    "messageIndex": i
                }

                document = new Document(
                    content: message.getContent(),
                    metadata: metadata
                )

                documents.add(document)
            }
        }

        Return documents
    }
}
```

### 智能檢索
```pseudocode
Class SmartMemoryRetrievalService {

    Function retrieveRelevantMemories(conversationId, query, options) {
        // 1. 短期檢索
        shortTermResults = retrieveShortTermMemories(
            conversationId,
            options.shortTermLimit
        )

        // 2. 長期檢索
        longTermResults = retrieveLongTermMemories(
            conversationId,
            query,
            options
        )

        // 3. 融合和排序
        fusedMemories = fuseAndRankMemories(
            shortTermResults,
            longTermResults,
            query
        )

        Return MemoryRetrievalResult {
            conversationId,
            query,
            shortTermMemories: shortTermResults,
            longTermMemories: longTermResults,
            fusedMemories: fusedMemories,
            retrievalTime: LocalDateTime.now()
        }
    }

    Function retrieveLongTermMemories(conversationId, query, options) {
        // 1. 向量化查詢
        queryVector = embeddingModel.embed(query)

        // 2. 執行向量搜尋
        searchRequest = SearchRequest {
            query: query,
            topK: options.longTermLimit,
            similarityThreshold: options.similarityThreshold,
            filterExpression: "conversationId == '" + conversationId + "'"
        }

        // 3. 返回結果
        Return vectorStore.similaritySearch(searchRequest)
    }

    Function fuseAndRankMemories(shortTermMemories, longTermMemories, query) {
        allMemories = []

        // 1. 轉換短期記憶
        For i = 0 to shortTermMemories.size() {
            message = shortTermMemories[i]

            relevanceScore = calculateShortTermRelevance(
                message,
                query,
                i
            )

            item = MemoryItem {
                content: message.getContent(),
                type: MemoryType.SHORT_TERM,
                relevanceScore: relevanceScore,
                timestamp: extractTimestamp(message),
                metadata: message.getMetadata()
            }

            allMemories.add(item)
        }

        // 2. 轉換長期記憶
        For each document in longTermMemories {
            item = MemoryItem {
                content: document.getContent(),
                type: MemoryType.LONG_TERM,
                relevanceScore: extractSimilarityScore(document),
                timestamp: extractTimestamp(document),
                metadata: document.getMetadata()
            }

            allMemories.add(item)
        }

        // 3. 按相關性排序
        allMemories.sort((a, b) → b.relevanceScore - a.relevanceScore)

        Return allMemories
    }
}
```

---

## 🔌 系統脈絡圖

```
┌────────────────────────┐
│   外部服務              │
│  ┌──────────────────┐  │
│  │ OpenAI API       │  │
│  │ (Embedding)      │  │
│  │ (Chat)           │  │
│  └──────────────────┘  │
└────────────────────────┘
        ↕
┌─────────────────────────────────────┐
│  Spring AI Vector Memory System      │
│                                     │
│  ┌──────────────────────────────┐   │
│  │  REST API 層                 │   │
│  └──────────────────────────────┘   │
│            ↓                         │
│  ┌──────────────────────────────┐   │
│  │  Service 層                  │   │
│  │  ├─ HybridMemoryService     │   │
│  │  ├─ SmartRetrievalService   │   │
│  │  ├─ MemorySyncService       │   │
│  │  └─ AnalyticsService        │   │
│  └──────────────────────────────┘   │
│            ↓                         │
│  ┌──────────────────────────────┐   │
│  │  Advisor & Memory 層         │   │
│  │  ├─ VectorStoreAdvisor      │   │
│  │  ├─ MemorySyncAdvisor       │   │
│  │  └─ (core advisors)         │   │
│  └──────────────────────────────┘   │
│            ↓                         │
│  ┌──────────────────────────────┐   │
│  │  Vector Store & Search       │   │
│  │  ├─ VectorStore (Neo4j)      │   │
│  │  ├─ EmbeddingModel           │   │
│  │  └─ SearchEngine             │   │
│  └──────────────────────────────┘   │
│            ↓                         │
│  ┌──────────────────────────────┐   │
│  │  Storage 層                  │   │
│  │  ├─ ChatMemory (short-term)  │   │
│  │  └─ Neo4j Vector DB          │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
        ↕
┌────────────────────────┐
│  基礎設施               │
│  ┌──────────────────┐  │
│  │ Neo4j Database   │  │
│  │ (Vector Index)   │  │
│  └──────────────────┘  │
│  ┌──────────────────┐  │
│  │ PostgreSQL       │  │
│  │ (Metadata)       │  │
│  └──────────────────┘  │
│  ┌──────────────────┐  │
│  │ Redis Cache      │  │
│  │ (Optional)       │  │
│  └──────────────────┘  │
└────────────────────────┘
```

---

## 📦 容器/部署概觀

### Docker Compose 配置

```yaml
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - NEO4J_URI=bolt://neo4j:7687
      - NEO4J_USERNAME=neo4j
      - NEO4J_PASSWORD=${NEO4J_PASSWORD}
      - JDBC_URL=jdbc:postgresql://postgres:5432/vectormemory
    depends_on:
      - neo4j
      - postgres
    networks:
      - ai-network

  neo4j:
    image: neo4j:5.15
    ports:
      - "7474:7474"  # HTTP
      - "7687:7687"  # Bolt
    environment:
      NEO4J_AUTH: neo4j/${NEO4J_PASSWORD}
      NEO4J_PLUGINS: '["apoc","graph-data-science"]'
      NEO4J_dbms_memory_heap_initial__size: 2G
      NEO4J_dbms_memory_heap_max__size: 4G
      NEO4J_dbms_memory_pagecache_size: 2G
      NEO4J_db_index_vector_enabled: 'true'
    volumes:
      - neo4j_data:/data
      - neo4j_logs:/logs
    healthcheck:
      test: ["CMD", "cypher-shell", "-u", "neo4j", "-p", "${NEO4J_PASSWORD}", "RETURN 1"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - ai-network

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: vectormemory
      POSTGRES_USER: chat
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - ai-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - ai-network

volumes:
  neo4j_data:
  neo4j_logs:
  postgres_data:
  redis_data:

networks:
  ai-network:
    driver: bridge
```

---

## 🗂️ 模組關係圖

```
chapter6-memory-vector/
│
├── src/main/java/
│   └── com/example/memory/vector/
│       │
│       ├── controller/                # REST 控制層
│       │   ├── VectorChatController   # 向量記憶對話
│       │   ├── MemoryAnalyticsController
│       │   └── SearchController       # 搜尋端點
│       │
│       ├── service/                   # 服務層
│       │   ├── HybridMemoryService    # 混合記憶服務
│       │   ├── SmartMemoryRetrievalService  # 智能檢索
│       │   ├── MemorySyncService      # 記憶同步
│       │   ├── MemoryAnalyticsService # 分析服務
│       │   └── VectorStoreService     # 向量存儲服務
│       │
│       ├── advisor/                   # Advisor 實現
│       │   ├── VectorStoreChatMemoryAdvisor
│       │   └── MemorySyncAdvisor      # 同步增強器
│       │
│       ├── repository/                # 數據訪問層
│       │   ├── VectorMemoryRepository # 向量存儲
│       │   └── MemoryMetadataRepository
│       │
│       ├── model/                     # 資料模型
│       │   ├── MemoryItem
│       │   ├── MemoryRetrievalResult
│       │   ├── SearchRequest
│       │   ├── MemoryAnalytics
│       │   └── MemoryQualityMetrics
│       │
│       ├── config/                    # 配置類
│       │   ├── Neo4jVectorStoreConfig
│       │   ├── HybridMemoryConfig
│       │   └── SearchEngineConfig
│       │
│       ├── embedding/                 # 嵌入相關
│       │   ├── EmbeddingCache
│       │   └── VectorNormalizer
│       │
│       ├── dto/                       # 數據轉移對象
│       │   ├── VectorChatRequest
│       │   ├── SearchResult
│       │   └── RetrievalOptions
│       │
│       └── Application.java           # 主應用類
│
├── src/main/resources/
│   ├── application.yml                # 主配置
│   ├── application-prod.yml           # 生產配置
│   ├── db/
│   │   └── cypher/
│   │       ├── create-indexes.cypher  # 向量索引創建
│   │       └── init-data.cypher       # 初始化數據
│   └── neo4j/
│       └── schema.cypher              # Schema 定義
│
├── src/test/java/
│   └── com/example/memory/vector/
│       ├── VectorMemoryIntegrationTest
│       ├── HybridMemoryTest
│       ├── SmartRetrievalTest
│       ├── Neo4jVectorStoreTest
│       └── PerformanceTest
│
├── docker/
│   ├── Dockerfile                     # 應用容器
│   └── docker-compose.yml             # 編排配置
│
└── pom.xml                            # Maven 依賴
```

---

## 🔀 序列圖

### 混合記憶對話序列

```
User ->> VectorChatController: POST /api/memory/hybrid-chat/{conversationId}
VectorChatController ->> HybridMemoryService: chatWithHybridMemory()
HybridMemoryService ->> ChatClient: prompt()
ChatClient ->> MessageChatMemoryAdvisor: advise()
MessageChatMemoryAdvisor ->> ChatMemory: get(conversationId)
ChatMemory -->> MessageChatMemoryAdvisor: [短期訊息]
MessageChatMemoryAdvisor -->> ChatClient: prompt with short-term context
ChatClient ->> VectorStoreChatMemoryAdvisor: advise()
VectorStoreChatMemoryAdvisor ->> EmbeddingModel: embed(query)
EmbeddingModel -->> VectorStoreChatMemoryAdvisor: vector
VectorStoreChatMemoryAdvisor ->> VectorStore: similaritySearch()
VectorStore ->> Neo4j: vector search query
Neo4j -->> VectorStore: similar documents
VectorStore -->> VectorStoreChatMemoryAdvisor: documents
VectorStoreChatMemoryAdvisor ->> SmartMemoryRetrievalService: fuseMemories()
SmartMemoryRetrievalService -->> VectorStoreChatMemoryAdvisor: fused & ranked
VectorStoreChatMemoryAdvisor -->> ChatClient: prompt with long-term context
ChatClient ->> ChatModel: call(final prompt)
ChatModel -->> ChatClient: response
ChatClient -->> HybridMemoryService: response
HybridMemoryService ->> ChatMemory: add(conversationId, response)
HybridMemoryService ->> MemorySyncService: asyncSync()
MemorySyncService ->> VectorStore: add(documents)
HybridMemoryService -->> VectorChatController: response
VectorChatController -->> User: ChatResponse
```

### 向量同步流程序列

```
ScheduledTask ->> MemorySyncService: syncMemories()
MemorySyncService ->> ChatMemory: getActiveConversations()
ChatMemory -->> MemorySyncService: [conversationIds]
Loop for each conversationId
  MemorySyncService ->> ChatMemory: get(conversationId)
  ChatMemory -->> MemorySyncService: [messages]
  MemorySyncService ->> MemorySyncService: convertToDocuments()
  MemorySyncService ->> EmbeddingModel: embed(document.content)
  EmbeddingModel -->> MemorySyncService: vector
  MemorySyncService ->> VectorStore: add(document with vector)
  VectorStore ->> Neo4j: INSERT Document + Vector Index
  MemorySyncService ->> MemorySyncService: recordSyncTime()
End Loop
MemorySyncService -->> ScheduledTask: completed
```

---

## 💾 ER 圖

```
chat_memory {
    conversation_id STRING [PK]
    user_id STRING
    created_at TIMESTAMP
}

chat_messages {
    id UUID [PK]
    conversation_id STRING [FK]
    message_type ENUM (USER, ASSISTANT, SYSTEM)
    content TEXT
    metadata JSON
    embedding_synced BOOLEAN
    created_at TIMESTAMP
}

vector_documents {
    id UUID [PK]
    conversation_id STRING [FK]
    content TEXT
    embedding VECTOR(1536)
    metadata JSON
    similarity_score FLOAT
    created_at TIMESTAMP
}

memory_sync_log {
    id UUID [PK]
    conversation_id STRING [FK]
    sync_type ENUM (AUTO, MANUAL)
    synced_count INT
    duration_ms LONG
    status ENUM (SUCCESS, FAILED)
    created_at TIMESTAMP
}

memory_analytics {
    conversation_id STRING [PK, FK]
    short_term_count INT
    long_term_count INT
    avg_relevance_score FLOAT
    last_sync_time TIMESTAMP
}

chat_memory ||--o{ chat_messages: contains
chat_memory ||--o{ vector_documents: has_long_term
chat_messages ||--o{ memory_sync_log: synced_in
vector_documents ||--o{ memory_analytics: belongs_to
```

---

## 🏛️ 類別圖

```
┌──────────────────────────────────┐
│   <<interface>>                  │
│   ChatMemory                     │
│  (from chapter6-memory-core)    │
└──────────────────────────────────┘
        ↑
        │

┌──────────────────────────────────┐
│       <<interface>>              │
│       VectorStore                │
├──────────────────────────────────┤
│ + add(documents)                 │
│ + delete(ids)                    │
│ + similaritySearch(request)      │
└──────────────────────────────────┘
        ↑
        │
        └─────────────────────────┐
                                  │
            ┌─────────────────────────────────┐
            │ Neo4jVectorStore                │
            ├─────────────────────────────────┤
            │ - driver: Driver                │
            │ - embeddingModel: EmbeddingModel│
            ├─────────────────────────────────┤
            │ + add(documents)                │
            │ + delete(ids)                   │
            │ + similaritySearch()            │
            │ - normalizeVector()             │
            └─────────────────────────────────┘


┌──────────────────────────────────┐
│   <<interface>>                  │
│   Advisor                        │
│  (from chapter6-memory-core)    │
└──────────────────────────────────┘
        ↑
        │

┌──────────────────────────────────┐
│ VectorStoreChatMemoryAdvisor     │
├──────────────────────────────────┤
│ - vectorStore: VectorStore       │
│ - embeddingModel: EmbeddingModel │
│ - conversationId: String         │
│ - topK: int                      │
│ - similarityThreshold: double    │
├──────────────────────────────────┤
│ + adviseRequest()                │
│ + adviseResponse()               │
│ - retrieveRelevantMemories()     │
│ + getOrder(): 100                │
└──────────────────────────────────┘


┌──────────────────────────────────┐
│ HybridMemoryService              │
├──────────────────────────────────┤
│ - chatClient: ChatClient         │
│ - shortTermMemory: ChatMemory    │
│ - longTermMemory: VectorStore    │
├──────────────────────────────────┤
│ + chatWithHybridMemory()         │
│ + chatWithSmartMemory()          │
│ - determineMemoryStrategy()      │
│ - buildPromptWithMemories()      │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│ SmartMemoryRetrievalService      │
├──────────────────────────────────┤
│ - vectorStore: VectorStore       │
│ - shortTermMemory: ChatMemory    │
│ - embeddingModel: EmbeddingModel │
├──────────────────────────────────┤
│ + retrieveRelevantMemories()     │
│ - retrieveShortTermMemories()    │
│ - retrieveLongTermMemories()     │
│ - fuseAndRankMemories()          │
│ - calculateRelevanceScore()      │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│ MemorySyncService                │
├──────────────────────────────────┤
│ - shortTermMemory: ChatMemory    │
│ - longTermMemory: VectorStore    │
│ - embeddingModel: EmbeddingModel │
├──────────────────────────────────┤
│ + syncMemories()                 │
│ + forceSyncMemory()              │
│ - syncConversationMemory()       │
│ - convertMessagesToDocuments()   │
│ - cleanupExpiredMemories()       │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│ MemoryAnalyticsService           │
├──────────────────────────────────┤
│ - shortTermMemory: ChatMemory    │
│ - longTermMemory: VectorStore    │
│ - meterRegistry: MeterRegistry   │
├──────────────────────────────────┤
│ + getConversationAnalytics()     │
│ - calculateMemoryUsage()         │
│ - analyzeMemoryQuality()         │
│ - recordMemoryOperation()        │
│ - recordRetrievalTime()          │
└──────────────────────────────────┘
```

---

## 🔄 流程圖

### 向量記憶完整流程

```
Start
  │
  ├─→ 用戶輸入對話消息
  │     │
  │     └─→ 驗證 conversationId
  │
  ├─→ [Short-term Memory]
  │     ├─→ ChatMemory.get() 最近 20 條
  │     └─→ 加入 Prompt 前缀
  │
  ├─→ [Embedding] 向量化查詢
  │     ├─→ OpenAI Embedding API
  │     └─→ 生成 1536 維向量
  │
  ├─→ [Vector Search] 向量搜尋
  │     ├─→ Neo4j Vector Index Query
  │     ├─→ 計算 Cosine 相似度
  │     └─→ 過濾 similarityThreshold > 0.75
  │
  ├─→ [Long-term Memory] 檢索相關記憶
  │     ├─→ 返回前 10 相關文檔
  │     └─→ 加入 Prompt 中部
  │
  ├─→ [Memory Fusion] 記憶融合
  │     ├─→ 短期記憶權重 60%
  │     ├─→ 長期記憶權重 40%
  │     └─→ 綜合排序
  │
  ├─→ [Final Prompt] 構建最終提示
  │     └─→ "Based on your past conversations:
  │         [融合記憶]
  │         Please answer: [用戶查詢]"
  │
  ├─→ [LLM Call] 調用 AI 模型
  │     └─→ OpenAI GPT-4
  │
  ├─→ [Get Response] 獲取回應
  │
  ├─→ [Save Short-term] 保存到短期記憶
  │     └─→ ChatMemory.add()
  │
  ├─→ [Async Sync] 非同步同步到長期
  │     ├─→ MemorySyncService (後台線程)
  │     ├─→ 轉換為 Document
  │     ├─→ 向量化
  │     └─→ VectorStore.add()
  │
  ├─→ [Return] 返回用戶
  │
End
```

---

## 📈 狀態圖

### 向量記憶索引狀態

```
           [INITIAL]
              │
              ├─ create()
              ↓
         [BUILDING]
              ├─ (indexing vectors)
              ├─ success() → [ONLINE]
              └─ error() → [FAILED]

          [ONLINE]
              ├─ search() (active)
              ├─ optimize() → [OPTIMIZING]
              ├─ rebuild() → [REBUILDING]
              └─ disable() → [OFFLINE]

       [OPTIMIZING]
              ├─ (parameter tuning)
              └─ done() → [ONLINE]

       [REBUILDING]
              ├─ (reindexing all data)
              └─ done() → [ONLINE]

          [OFFLINE]
              ├─ enable() → [ONLINE]
              └─ delete() → [DELETED]

         [FAILED]
              └─ retry() → [BUILDING]

        [DELETED]
              └─ (terminal state)
```

---

## 📋 範例代碼清單

### 核心實現 (12 個)
1. `HybridMemoryService.java` - 混合記憶服務
2. `SmartMemoryRetrievalService.java` - 智能檢索
3. `MemorySyncService.java` - 記憶同步
4. `MemoryAnalyticsService.java` - 分析服務
5. `VectorStoreChatMemoryAdvisor.java` - 向量 Advisor
6. `MemorySyncAdvisor.java` - 同步 Advisor
7. `Neo4jVectorStoreConfig.java` - Neo4j 配置
8. `HybridMemoryConfig.java` - 混合記憶配置
9. `VectorStoreService.java` - 向量存儲服務
10. `EmbeddingCacheService.java` - 嵌入快取
11. `VectorNormalizer.java` - 向量正規化
12. `SearchEngineService.java` - 搜尋引擎

### REST API (4 個)
13. `VectorChatController.java` - 向量對話端點
14. `MemoryAnalyticsController.java` - 分析端點
15. `SearchController.java` - 搜尋端點
16. `MemoryManagementController.java` - 管理端點

### 資料模型 (6 個)
17. `MemoryItem.java` - 記憶項目
18. `MemoryRetrievalResult.java` - 檢索結果
19. `MemoryAnalytics.java` - 分析結果
20. `VectorChatRequest.java` - 向量請求
21. `SearchRequest.java` - 搜尋請求
22. `RetrievalOptions.java` - 檢索選項

### 配置和 Schema (5 個)
23. `application.yml` - 主配置
24. `application-prod.yml` - 生產配置
25. `create-indexes.cypher` - Neo4j 索引創建
26. `schema.cypher` - Neo4j Schema
27. `init-data.cypher` - 初始化數據

### 測試 (5 個)
28. `VectorMemoryIntegrationTest.java`
29. `HybridMemoryTest.java`
30. `SmartRetrievalTest.java`
31. `Neo4jVectorStoreTest.java`
32. `PerformanceTest.java`

### Docker 和部署 (3 個)
33. `Dockerfile` - 應用容器
34. `docker-compose.yml` - 完整編排
35. `pom.xml` - Maven 依賴

---

## 🧪 測試計劃

### 單元測試
- ✅ Vector Store 操作 (add, delete, search)
- ✅ 嵌入模型集成
- ✅ 記憶同步邏輯
- ✅ 檢索結果融合和排序

### 集成測試
- ✅ 混合記憶對話流程
- ✅ Neo4j 向量索引查詢
- ✅ 短期和長期記憶交互
- ✅ 並發訪問和同步

### 性能測試
- ✅ 大量文檔批量插入 (10K+ 文檔)
- ✅ 向量搜尋延遲 (< 500ms)
- ✅ 並發用戶對話 (100+ 並發)
- ✅ 記憶同步吞吐量

---

## 🚀 快速開始

### 前置要求
- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- OpenAI API Key (用於 Embedding 和 Chat)
- (可選) PostgreSQL for metadata

### 必需的 Bean 配置

```java
@Configuration
public class VectorMemoryConfig {

    // 1. EmbeddingModel Bean (自動向量化)
    @Bean
    public EmbeddingModel embeddingModel() {
        return new OpenAiEmbeddingModel(
            new OpenAiApi(System.getenv("OPENAI_API_KEY"))
        );
    }

    // 2. Neo4j Driver Bean
    @Bean
    public Driver neo4jDriver() {
        return GraphDatabase.driver(
            "neo4j://localhost:7687",
            AuthTokens.basic("neo4j", "password")
        );
    }

    // 3. VectorStore Bean (Neo4j 實現)
    @Bean
    public VectorStore vectorStore(Driver driver, EmbeddingModel embeddingModel) {
        return Neo4jVectorStore.builder(driver, embeddingModel)
            .databaseName("neo4j")
            .distanceType(Neo4jDistanceType.COSINE)
            .embeddingDimension(1536)
            .initializeSchema(true)
            .build();
    }
}
```

### 啟動步驟

```bash
# 1. 進入專案
cd chapter6-memory-vector

# 2. 設定環境變數
export OPENAI_API_KEY=your-key
export NEO4J_PASSWORD=your-neo4j-password

# 3. 啟動 Docker 服務 (Neo4j, PostgreSQL)
docker-compose up -d neo4j postgres

# 4. 等待服務就緒 (30 秒)
sleep 30

# 5. 初始化 Neo4j 索引
docker exec neo4j-vector cypher-shell -u neo4j -p ${NEO4J_PASSWORD} < src/main/resources/db/cypher/create-indexes.cypher

# 6. 編譯並運行應用
mvn spring-boot:run -Dspring-boot.run.arguments=\"--spring.profiles.active=prod\"

# 7. 驗證服務
curl http://localhost:8080/health
```

---

## 📚 參考資源

- [Spring AI Vector Store](https://docs.spring.io/spring-ai/reference/api/vectordbs.html)
- [Neo4j Vector Search](https://neo4j.com/docs/cypher-manual/current/indexes-for-vector-search/)
- [OpenAI Embeddings API](https://platform.openai.com/docs/api-reference/embeddings)
- [Vector Database Concepts](https://www.pinecone.io/learn/vector-database/)

---

## 📝 版本信息

- **Spring Boot**: 3.2.0+
- **Spring AI**: 1.0.0 GA
- **Neo4j**: 5.15+
- **Java**: 21
- **Build Date**: 2024

