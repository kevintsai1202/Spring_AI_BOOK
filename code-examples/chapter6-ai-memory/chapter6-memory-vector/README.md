# Chapter 6 - AI Memory Vector System

Spring AI 長期記憶與向量資料庫系統

## 📋 專案概述

本專案實現了基於 **Neo4j 向量資料庫的長期記憶系統**，結合短期和長期記憶，實現智能語義搜尋和記憶管理。

### 核心功能

- ✅ **Neo4j 向量資料庫整合**: 使用 Neo4j 作為向量存儲
- ✅ **混合記憶系統**: 短期記憶 (InMemoryChatMemory) + 長期記憶 (VectorStore)
- ✅ **智能記憶檢索**: 基於向量相似性的語義搜尋
- ✅ **自動記憶同步**: 定期將短期記憶同步到向量資料庫
- ✅ **記憶分析**: 統計分析和健康檢查
- ✅ **多種對話策略**: 支持短期、長期、混合和智能策略

## 🚀 快速開始

### 前置要求

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- OpenAI API Key

### 環境變數設定

複製 `.env.example` 為 `.env` 並填入你的配置:

```bash
cp .env.example .env
```

編輯 `.env` 文件:

```env
OPENAI_API_KEY=your-openai-api-key-here
NEO4J_PASSWORD=your-neo4j-password
```

### 使用 Docker Compose 啟動

```bash
# 啟動 Neo4j 和應用
docker-compose up -d

# 查看日誌
docker-compose logs -f app

# 停止服務
docker-compose down
```

### 本地開發模式

#### 1. 啟動 Neo4j

```bash
docker-compose up -d neo4j
```

#### 2. 設定環境變數 (PowerShell)

```powershell
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"
$env:OPENAI_API_KEY="your-api-key"
```

#### 3. 編譯並運行

```powershell
# 編譯
mvn clean compile

# 運行 (開發模式)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

## 📡 API 端點

### 向量記憶對話

#### 1. 混合記憶對話 (推薦)
```bash
curl -X POST http://localhost:8080/api/vector-chat/conv-001/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "message": "介紹一下 Spring AI 的向量記憶功能"
  }'
```

#### 2. 短期記憶對話
```bash
curl -X POST http://localhost:8080/api/vector-chat/conv-001/short-term \
  -H "Content-Type: application/json" \
  -d '{
    "message": "剛才我們聊了什麼?"
  }'
```

#### 3. 長期記憶對話
```bash
curl -X POST http://localhost:8080/api/vector-chat/conv-001/long-term \
  -H "Content-Type: application/json" \
  -d '{
    "message": "之前我們討論過 Spring AI 嗎?",
    "similarityThreshold": 0.75,
    "topK": 10
  }'
```

#### 4. 智能策略對話
```bash
curl -X POST http://localhost:8080/api/vector-chat/conv-001/smart \
  -H "Content-Type: application/json" \
  -d '{
    "message": "記得我們之前聊過的內容嗎?"
  }'
```

### 記憶管理

#### 獲取記憶分析
```bash
curl http://localhost:8080/api/memory/analytics/conv-001
```

#### 手動同步記憶
```bash
curl -X POST http://localhost:8080/api/memory/sync/conv-001
```

#### 檢索記憶
```bash
curl -X POST "http://localhost:8080/api/memory/retrieve/conv-001?query=Spring+AI"
```

#### 刪除對話記憶
```bash
curl -X DELETE http://localhost:8080/api/memory/conv-001
```

#### 獲取活躍對話列表
```bash
curl http://localhost:8080/api/memory/conversations
```

## 🏗️ 架構設計

```
┌─────────────────────────────────────────────────────────┐
│         Spring AI Vector Memory System                  │
├─────────────────────────────────────────────────────────┤
│  REST API Layer                                         │
│  ├─ VectorChatController                                │
│  └─ MemoryManagementController                          │
├─────────────────────────────────────────────────────────┤
│  Service Layer                                          │
│  ├─ HybridMemoryService (混合記憶)                      │
│  ├─ MemorySyncService (自動同步)                        │
│  ├─ SmartMemoryRetrievalService (智能檢索)              │
│  └─ MemoryAnalyticsService (分析統計)                   │
├─────────────────────────────────────────────────────────┤
│  Memory & Storage Layer                                 │
│  ├─ InMemoryChatMemory (短期記憶)                       │
│  └─ Neo4jVectorStore (長期記憶)                         │
├─────────────────────────────────────────────────────────┤
│  Infrastructure Layer                                   │
│  ├─ Neo4j Database (向量索引)                           │
│  └─ OpenAI Embedding API                                │
└─────────────────────────────────────────────────────────┘
```

## 🔍 記憶策略說明

### 1. 短期記憶 (SHORT_TERM_ONLY)
- 使用場景: "剛才"、"剛剛"、"剛說"
- 只檢索最近的對話上下文
- 響應速度最快

### 2. 長期記憶 (LONG_TERM_ONLY)
- 使用場景: "之前"、"記得"、"以前"、"曾經"
- 只使用向量資料庫檢索歷史記憶
- 適合跨對話會話的記憶檢索

### 3. 混合記憶 (HYBRID)
- 使用場景: 默認策略
- 同時使用短期和長期記憶
- 提供最完整的上下文

### 4. 智能策略 (SMART)
- 根據用戶查詢自動選擇最合適的策略
- 推薦使用

## ⚙️ 配置說明

### 向量存儲配置 (application.yml)

```yaml
vector:
  store:
    distance-type: COSINE         # 距離計算類型
    embedding-dimension: 1536     # 向量維度
    index-name: spring-ai-memory-index
    initialize-schema: true       # 自動初始化 Schema
```

### 記憶系統配置

```yaml
memory:
  short-term:
    max-messages: 20              # 短期記憶最大訊息數

  long-term:
    enabled: true
    similarity-threshold: 0.75    # 相似性閾值
    top-k: 10                     # 返回的最相關記憶數

  sync:
    enabled: true
    interval: 300000              # 自動同步間隔 (5分鐘)
    message-threshold: 10         # 觸發同步的訊息數閾值
```

## 📊 監控和指標

應用提供了 Actuator 端點用於監控:

```bash
# 健康檢查
curl http://localhost:8080/actuator/health

# Prometheus 指標
curl http://localhost:8080/actuator/prometheus

# 應用信息
curl http://localhost:8080/actuator/info
```

## 🧪 測試

### 運行單元測試
```bash
mvn test
```

### 運行集成測試
```bash
mvn verify
```

## 📁 專案結構

```
src/
├── main/
│   ├── java/com/example/memory/vector/
│   │   ├── config/              # 配置類
│   │   ├── controller/          # REST 控制器
│   │   ├── dto/                 # 數據傳輸對象
│   │   ├── exception/           # 異常處理
│   │   ├── model/               # 數據模型
│   │   └── service/             # 業務服務
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
└── test/
    └── java/com/example/memory/vector/
        └── ...                  # 測試類
```

## 🔧 故障排除

### 1. Neo4j 連接失敗
```bash
# 檢查 Neo4j 是否運行
docker-compose ps neo4j

# 查看 Neo4j 日誌
docker-compose logs neo4j

# 重新啟動 Neo4j
docker-compose restart neo4j
```

### 2. OpenAI API 錯誤
- 確認 API Key 是否正確設置
- 檢查 API 額度是否充足
- 查看應用日誌: `docker-compose logs app`

### 3. 記憶同步問題
```bash
# 手動觸發同步
curl -X POST http://localhost:8080/api/memory/sync/your-conversation-id

# 檢查同步狀態
curl http://localhost:8080/api/memory/analytics/your-conversation-id
```

## 📚 參考資源

- [Spring AI 官方文檔](https://docs.spring.io/spring-ai/reference/)
- [Neo4j Vector Search](https://neo4j.com/docs/cypher-manual/current/indexes-for-vector-search/)
- [OpenAI Embeddings API](https://platform.openai.com/docs/api-reference/embeddings)

## 📝 版本信息

- **Spring Boot**: 3.2.0
- **Spring AI**: 1.0.0-M4
- **Neo4j**: 5.15
- **Java**: 21

## 📄 授權

MIT License

---

**最後更新**: 2025-01-27
**版本**: 0.0.1-SNAPSHOT
