# Chapter 7.1-7.2: RAG 基礎系統

> Spring AI RAG (Retrieval-Augmented Generation) 基礎實現

---

## 📖 專案概述

本專案展示 Spring AI 的 RAG 基礎功能,包括:

- **文檔向量化**: 使用 OpenAI Embeddings 將文檔轉換為向量
- **向量資料庫**: Neo4j 向量存儲與相似性搜尋
- **QuestionAnswerAdvisor**: Spring AI 自動檢索增強
- **RAG 查詢流程**: 自動檢索相關文檔並生成答案

---

## 🎯 學習目標

- ✅ 理解 RAG 的核心流程
- ✅ 掌握文檔向量化處理
- ✅ 使用 Spring AI QuestionAnswerAdvisor
- ✅ 配置 Neo4j 向量資料庫
- ✅ 實現基礎 RAG 查詢 API

---

## 🚀 快速開始

### 前置要求

- Java 21+
- Maven 3.9+
- Docker (用於執行 Neo4j)
- OpenAI API Key

### 1. 啟動 Neo4j 向量資料庫

```bash
docker run -d \
  --name neo4j-rag \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/test1234 \
  neo4j:5.15
```

等待約 30 秒讓 Neo4j 完全啟動。

### 2. 配置環境變數

複製 `.env.example` 為 `.env` 並填入您的 API Key:

```bash
OPENAI_API_KEY=your-openai-api-key-here
NEO4J_PASSWORD=test1234
```

### 3. 編譯專案

```powershell
# 設定 Java 21 環境
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 編譯專案
mvn clean compile
```

### 4. 執行應用

```powershell
mvn spring-boot:run
```

應用程序將在 `http://localhost:8080` 啟動。

---

## 📚 API 文檔

### 1. 健康檢查

```bash
curl http://localhost:8080/api/rag/health
```

**響應**:
```json
{
  "status": "UP",
  "service": "RAG Basic System",
  "version": "1.0.0"
}
```

### 2. 上傳文檔

```bash
curl -X POST http://localhost:8080/api/rag/documents \
  -F "files=@document1.pdf" \
  -F "files=@document2.txt"
```

**響應**:
```json
{
  "success": true,
  "message": "成功添加 2 個文檔到知識庫",
  "documentsProcessed": 2
}
```

### 3. RAG 查詢

```bash
curl -X POST http://localhost:8080/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什麼是 Spring AI?",
    "topK": 5,
    "similarityThreshold": 0.7
  }'
```

**響應**:
```json
{
  "question": "什麼是 Spring AI?",
  "answer": "Spring AI 是一個用於構建 AI 應用程序的框架...",
  "sources": [
    {
      "documentId": "doc-001",
      "title": "Spring AI 指南",
      "excerpt": "Spring AI 提供了與各種 AI 模型整合的功能...",
      "relevanceScore": 0.92,
      "metadata": {
        "source_file": "spring-ai-guide.pdf",
        "page_number": 1
      }
    }
  ],
  "processingTimeMs": 1250,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 4. 帶過濾條件的 RAG 查詢

```bash
curl -X POST http://localhost:8080/api/rag/query-with-filter \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Spring Boot 如何配置?",
    "topK": 3,
    "filters": {
      "category": "技術文檔",
      "language": "zh-TW"
    }
  }'
```

---

## 🏗️ 專案結構

```
chapter7-rag-basic/
├── src/
│   ├── main/
│   │   ├── java/com/example/rag/basic/
│   │   │   ├── config/
│   │   │   │   └── RAGConfig.java           # RAG 配置
│   │   │   ├── controller/
│   │   │   │   └── RAGController.java       # REST API
│   │   │   ├── service/
│   │   │   │   ├── RAGService.java          # RAG 核心服務
│   │   │   │   └── DocumentProcessingService.java  # 文檔處理
│   │   │   ├── model/
│   │   │   │   ├── RAGQueryRequest.java
│   │   │   │   ├── RAGQueryResponse.java
│   │   │   │   └── DocumentSource.java
│   │   │   ├── exception/
│   │   │   │   └── RAGException.java
│   │   │   └── RagBasicApplication.java
│   │   └── resources/
│   │       ├── application.yml              # 主配置
│   │       └── application-dev.yml          # 開發配置
│   └── test/
│       └── java/com/example/rag/basic/
│           └── RagBasicApplicationTests.java
├── pom.xml
├── .env.example
└── README.md
```

---

## 🔑 核心組件說明

### 1. QuestionAnswerAdvisor

Spring AI 的 `QuestionAnswerAdvisor` 自動處理 RAG 流程:

```java
@Bean
public ChatClient ragChatClient(ChatModel chatModel, VectorStore vectorStore) {
    return ChatClient.builder(chatModel)
        .defaultAdvisors(
            new QuestionAnswerAdvisor(
                vectorStore,
                SearchRequest.defaults()
                    .withTopK(5)
                    .withSimilarityThreshold(0.7)
            )
        )
        .build();
}
```

**自動執行步驟**:
1. 向量化用戶問題
2. 從向量資料庫檢索相關文檔 (Top-K)
3. 組裝上下文到 Prompt
4. 調用 LLM 生成答案

### 2. TokenTextSplitter

智能文本分塊工具:

```java
@Bean
public TokenTextSplitter tokenTextSplitter() {
    return new TokenTextSplitter(
        800,    // 每塊大小 (tokens)
        200,    // 重疊大小 (tokens)
        10,     // 最小嵌入長度
        10000,  // 最大塊數量
        true    // 保留分隔符
    );
}
```

### 3. Neo4j Vector Store

向量資料庫配置:

```yaml
spring:
  ai:
    vectorstore:
      neo4j:
        uri: bolt://localhost:7687
        username: neo4j
        password: test1234
        index-name: document-embeddings
        embedding-dimension: 1536
        distance-type: COSINE
```

---

## 🧪 測試指南

### 運行測試

```bash
mvn test
```

### 測試覆蓋範圍

- ✅ 應用程序上下文載入測試
- ✅ RAG 查詢流程測試
- ✅ 文檔處理測試

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

### Q3: 文檔上傳失敗

**解決方案**:
1. 確認文件大小不超過 50MB
2. 確認文件格式為 PDF 或 TXT
3. 檢查日誌中的錯誤訊息

---

## 📊 性能指標

| 指標 | 目標值 |
|------|--------|
| RAG 查詢響應時間 | < 2秒 |
| 文檔處理時間 | < 5秒/文檔 |
| 向量搜尋時間 | < 500ms |
| 系統可用性 | ≥ 99% |

---

## 🔗 相關資源

- [Spring AI 官方文檔](https://docs.spring.io/spring-ai/reference/)
- [Neo4j Vector Search](https://neo4j.com/docs/cypher-manual/current/indexes-for-vector-search/)
- [OpenAI Embeddings](https://platform.openai.com/docs/guides/embeddings)

---

## 📝 版本資訊

- **版本**: 1.0.0
- **Spring Boot**: 3.4.1
- **Spring AI**: 1.0.0-M5
- **Java**: 21
- **最後更新**: 2025-01-28

---

**下一步**: 學習 [chapter7-rag-etl-pipeline](../chapter7-rag-etl-pipeline/) - ETL 管道系統
