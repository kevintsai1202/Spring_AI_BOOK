# Chapter 6: AI 記憶增強系統 (AI Memory Enhancement)

> 基於 Spring AI 1.0 GA 的完整記憶管理系統實現

---

## 📖 專案概述

本章節包含三個逐步進階的模組，涵蓋從基礎記憶到企業級應用的完整實現。

### 對應原始文章
- **Day16**: In-Context Learning 與 RAG 基礎
- **Day17**: Spring AI 1.1 記憶管理系統
- **Day18**: 企業級記憶與向量檢索
- **Day19**: 鏈式增強器（Advisor API）
- **Day20**: 自行開發 Spring AI 插件
- **Day21**: 向量資料庫全攻略
- **Day22**: 使用向量資料庫作為對話的長久記憶

---

## 📦 模組結構

```
chapter6-ai-memory/
│
├── chapter6-memory-core/          ✅ 基礎記憶系統（Day16,17,19,20）
│   ├── 6.1-6.2 In-Context Learning
│   ├── 6.3 ChatMemory 短期記憶系統
│   └── 6.4 Advisor 自定義開發
│
├── chapter6-memory-vector/        ✅ 向量記憶系統（Day21,22）
│   ├── 6.5 向量資料庫選擇指南（Day21）
│   ├── 6.6 Neo4j 配置與部署
│   ├── 6.7 VectorStoreChatMemoryAdvisor
│   └── 多向量資料庫整合範例
│
└── chapter6-memory-advanced/      ✅ 進階記憶管理（Day18）
    ├── 6.8 智能記憶摘要系統
    ├── 混合記憶策略
    ├── 對話分析與管理
    └── 記憶優化系統
```

---

## 🎯 學習路徑

### 階段 1: 基礎記憶系統（chapter6-memory-core）

**學習目標**:
- ✅ 理解 In-Context Learning 基本概念
- ✅ 掌握 ChatMemory 短期記憶系統
- ✅ 學會自定義 Advisor 開發
- ✅ 實現工具調用系統

**核心內容**:
1. **In-Context Learning** (對應 6.1-6.2, Day16)
   - 基礎上下文注入
   - System Message 使用
   - 簡單 RAG 實現

2. **ChatMemory 系統** (對應 6.3, Day17)
   - MessageChatMemoryAdvisor
   - PromptChatMemoryAdvisor
   - 不同儲存後端（InMemory, JDBC, Cassandra）

3. **Advisor 開發** (對應 6.4, Day19-20)
   - CallAdvisor 與 StreamAdvisor
   - 自定義 Advisor 實現
   - TokenUsageLogAdvisor 範例

**啟動命令**:
```bash
cd chapter6-memory-core
mvn spring-boot:run
```

---

### 階段 2: 向量記憶系統（chapter6-memory-vector）

**學習目標**:
- ✅ 掌握 20+ 種向量資料庫選擇
- ✅ 配置和部署 Neo4j 向量資料庫
- ✅ 實現 VectorStoreChatMemoryAdvisor
- ✅ 建立混合記憶架構（短期+長期）

**核心內容**:
1. **向量資料庫選擇** (對應 6.5, Day21)
   - pgvector, Qdrant, Weaviate, Milvus, Pinecone 等
   - Docker Compose 多資料庫環境
   - 企業級選擇決策樹
   - 性能基準測試

2. **Neo4j 配置** (對應 6.6)
   - Docker 部署配置
   - 向量索引創建
   - 記憶體和性能優化

3. **長期記憶系統** (對應 6.7, Day22)
   - VectorStoreChatMemoryAdvisor
   - 語義搜尋實現
   - 記憶同步機制
   - 混合記憶策略

**啟動命令**:
```bash
# 1. 啟動 Neo4j（或其他向量資料庫）
docker-compose -f chapter6-memory-vector/docker/docker-compose.yml up -d

# 2. 啟動應用
cd chapter6-memory-vector
mvn spring-boot:run
```

**參考文檔**:
- `VECTOR_DATABASE_GUIDE.md` - 完整向量資料庫選擇指南

---

### 階段 3: 進階記憶管理（chapter6-memory-advanced）

**學習目標**:
- ✅ 實現智能記憶摘要系統
- ✅ 建立混合記憶策略
- ✅ 開發對話分析功能
- ✅ 掌握記憶優化技術

**核心內容**:
1. **智能記憶摘要** (對應 6.8, Day18)
   - SmartMemoryAdvisor
   - 自動摘要機制
   - 長對話優化

2. **混合記憶策略** (對應 Day18)
   - HybridMemoryService
   - 動態策略選擇
   - 記憶融合算法

3. **對話分析** (對應 Day18)
   - ConversationSummaryService
   - 主題提取
   - 待辦事項識別

4. **記憶優化** (對應 Day18)
   - MessageWindowChatMemory
   - 自動清理策略
   - 記憶生命週期管理

**啟動命令**:
```bash
cd chapter6-memory-advanced
mvn spring-boot:run
```

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

#### 1. 啟動基礎記憶系統
```bash
cd chapter6-memory-core
mvn clean install
mvn spring-boot:run
```

測試端點:
```bash
# In-Context Learning
curl "http://localhost:8080/api/context/chat?prompt=你好"

# ChatMemory 短期記憶
curl "http://localhost:8080/api/memory/chat?conversationId=test-001&message=我叫Kevin"
```

---

#### 2. 啟動向量記憶系統
```bash
# 啟動 Neo4j
cd chapter6-memory-vector
docker-compose -f docker/docker-compose.yml up -d neo4j

# 等待 Neo4j 啟動（約 30 秒）
sleep 30

# 啟動應用
mvn clean install
mvn spring-boot:run
```

測試端點:
```bash
# 向量記憶對話
curl -X POST "http://localhost:8081/api/vector/chat" \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"test-001","message":"Spring AI是什麼?"}'

# 混合記憶對話
curl -X POST "http://localhost:8081/api/vector/hybrid-chat" \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"test-001","message":"我之前問過什麼?"}'
```

---

#### 3. 啟動進階記憶管理
```bash
cd chapter6-memory-advanced
mvn clean install
mvn spring-boot:run
```

測試端點:
```bash
# 混合記憶策略對話
curl -X POST "http://localhost:8082/api/advanced/chat" \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"test-001","message":"你好"}'

# 對話摘要
curl "http://localhost:8082/api/advanced/summarize/test-001"
```

---

## 📊 模組依賴關係

```
chapter6-memory-core
    ↓ (依賴)
chapter6-memory-vector
    ↓ (依賴)
chapter6-memory-advanced
```

**注意**: 必須按順序安裝各模組

```bash
# 完整安裝流程
cd chapter6-memory-core && mvn clean install
cd ../chapter6-memory-vector && mvn clean install
cd ../chapter6-memory-advanced && mvn clean install
```

---

## 🧪 測試指南

### 單元測試
```bash
# 測試所有模組
mvn test

# 測試特定模組
cd chapter6-memory-core && mvn test
```

### 集成測試
```bash
mvn verify -P integration-test
```

### 性能測試
```bash
cd chapter6-memory-vector
mvn test -Dtest=PerformanceTest
```

---

## 📚 核心概念對照表

| 原文章 | 章節編號 | 對應模組 | 核心概念 |
|-------|---------|---------|---------|
| Day16 | 6.1-6.2 | core | In-Context Learning, RAG基礎 |
| Day17 | 6.3 | core | ChatMemory, MessageChatMemoryAdvisor |
| Day18 | 6.8 | advanced | 智能摘要, 混合策略, 對話分析 |
| Day19 | 6.4 | core | CallAdvisor, StreamAdvisor, Advisor鏈 |
| Day20 | 6.4 | core | 自定義 Advisor, TokenUsageLogAdvisor |
| Day21 | 6.5 | vector | 20+ 向量資料庫, Docker Compose, 選擇指南 |
| Day22 | 6.6-6.7 | vector | VectorStoreChatMemoryAdvisor, Neo4j |

---

## 🔧 配置範例

### application.yml 完整配置
```yaml
# 基礎配置
spring:
  application:
    name: chapter6-ai-memory
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-4
        temperature: 0.7

# 記憶配置
app:
  memory:
    # 短期記憶
    short-term:
      enabled: true
      max-messages: 20
      store-type: memory  # memory, jdbc, cassandra

    # 向量記憶
    vector:
      enabled: true
      type: neo4j  # neo4j, pgvector, qdrant, weaviate
      dimensions: 1536
      similarity-threshold: 0.75

    # 進階功能
    advanced:
      enabled: true
      auto-summarize: true
      summary-threshold: 50
      keep-recent-messages: 20

# 向量資料庫配置
  vectorstore:
    neo4j:
      uri: bolt://localhost:7687
      username: neo4j
      password: ${NEO4J_PASSWORD}
      distance-type: COSINE
```

---

## 📖 API 文檔

### Core 模組 API (端口: 8080)
| 端點 | 方法 | 描述 |
|------|------|------|
| `/api/context/chat` | GET | In-Context Learning 對話 |
| `/api/memory/chat` | GET | 短期記憶對話 |
| `/api/memory/history/{id}` | GET | 獲取對話歷史 |
| `/api/memory/clear/{id}` | DELETE | 清除對話記憶 |

### Vector 模組 API (端口: 8081)
| 端點 | 方法 | 描述 |
|------|------|------|
| `/api/vector/chat` | POST | 向量記憶對話 |
| `/api/vector/hybrid-chat` | POST | 混合記憶對話 |
| `/api/vector/search` | POST | 語義搜尋 |
| `/api/vector/analytics/{id}` | GET | 記憶分析 |

### Advanced 模組 API (端口: 8082)
| 端點 | 方法 | 描述 |
|------|------|------|
| `/api/advanced/chat` | POST | 混合策略對話 |
| `/api/advanced/stream` | POST | 串流式對話 |
| `/api/advanced/summarize/{id}` | POST | 對話摘要 |
| `/api/advanced/topics/{id}` | GET | 提取對話主題 |
| `/api/advanced/analytics/{id}` | GET | 對話統計分析 |

---

## 🎓 學習建議

### 初學者路徑
1. 從 `chapter6-memory-core` 開始
2. 完成 In-Context Learning 基礎練習
3. 理解 ChatMemory 和 Advisor 概念
4. 嘗試自定義 Advisor

### 進階路徑
1. 學習向量資料庫基礎知識
2. 部署和配置 Neo4j
3. 實現 VectorStoreChatMemoryAdvisor
4. 建立混合記憶系統

### 專家路徑
1. 研究不同向量資料庫的性能特性
2. 優化記憶檢索策略
3. 實現智能記憶管理系統
4. 開發對話分析功能

---

## 🐛 常見問題

### Q1: 為什麼啟動時報 OpenAI API 連接錯誤?
A: 請確認已設定環境變數 `OPENAI_API_KEY`
```bash
export OPENAI_API_KEY=your-api-key
```

### Q2: Neo4j 連接失敗怎麼辦?
A: 確認 Docker 容器已啟動並等待 30 秒讓服務完全就緒
```bash
docker-compose ps
docker logs neo4j
```

### Q3: 記憶沒有被保存?
A: 檢查配置中的 `store-type` 是否正確，InMemory 模式重啟後會清空

### Q4: 向量搜尋沒有結果?
A:
1. 確認已有數據同步到向量資料庫
2. 降低 `similarity-threshold` 閾值
3. 檢查 embedding 維度配置

---

## 📝 版本信息

- **Spring Boot**: 3.2.0+
- **Spring AI**: 1.0.0 GA
- **Java**: 21
- **Neo4j**: 5.13+
- **Maven**: 3.9+

---

## 🔗 相關資源

### 官方文檔
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Spring AI Advisors](https://docs.spring.io/spring-ai/reference/api/advisors.html)
- [Spring AI Vector Stores](https://docs.spring.io/spring-ai/reference/api/vectordbs.html)

### 向量資料庫
- [Neo4j Vector Search](https://neo4j.com/docs/cypher-manual/current/indexes-for-vector-search/)
- [pgvector](https://github.com/pgvector/pgvector)
- [Qdrant](https://qdrant.tech/documentation/)
- [Weaviate](https://weaviate.io/developers/weaviate)

### 原始文章
- [Day16-Day22 iThelp 鐵人賽](https://ithelp.ithome.com.tw/users/20161290/ironman/)

---

## 🤝 貢獻

歡迎提交 Issue 和 Pull Request！

---

## 📄 授權

本專案採用 MIT 授權

---

**最後更新**: 2025
**維護者**: Kevin Tsai
