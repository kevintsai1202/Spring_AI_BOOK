# Chapter 6.8: 進階記憶管理系統 (Advanced Memory Management)

## 專案簡介

本專案展示了基於 Spring AI 1.0.3 的**進階記憶管理技術**,包括智能記憶摘要、混合記憶策略、對話分析等高級功能。

## 核心功能

### 1. 智能記憶摘要系統 (SmartMemoryAdvisor)
- 自動監控對話訊息數量
- 當訊息超過閾值時,自動觸發摘要
- 保留摘要 + 最近訊息,優化記憶使用

### 2. 混合記憶策略系統 (HybridMemoryService)
- **短期記憶優先**: 最近對話的快速存取
- **長期記憶檢索**: 語義相似的歷史對話
- **動態策略切換**: 根據查詢類型選擇記憶策略

### 3. 對話摘要功能 (ConversationSummaryService)
- 智能摘要對話歷史
- 提取對話主題
- 識別關鍵決策
- 提取待辦事項

## 技術棧

- **Spring Boot**: 3.5.7
- **Spring AI**: 1.0.3
- **Java**: 21
- **資料庫**: H2 (開發環境)

## 快速開始

### 1. 環境準備

確保已安裝:
- Java 21
- Maven 3.9+
- OpenAI API Key

### 2. 配置

建立 `.env` 檔案並設定 OpenAI API Key:

```bash
OPENAI_API_KEY=your_api_key_here
```

### 3. 編譯專案

```bash
# 設定 Java 21 環境變數
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 編譯專案
mvn clean compile
```

### 4. 啟動應用

```bash
mvn spring-boot:run
```

應用程式將在 `http://localhost:8086` 啟動。

## API 端點

### 聊天端點

#### POST /api/advanced/chat
使用混合記憶策略進行對話

**請求範例:**
```json
{
  "conversationId": "user123",
  "message": "你好,請介紹一下混合記憶系統",
  "strategy": "HYBRID"
}
```

**回應範例:**
```json
{
  "conversationId": "user123",
  "message": "混合記憶系統結合了短期記憶和長期記憶...",
  "usedStrategy": "HYBRID",
  "messageCount": 10
}
```

### 摘要端點

#### POST /api/advanced/summarize/{conversationId}
生成對話摘要

**回應範例:**
```json
{
  "conversationId": "user123",
  "summary": "本次對話討論了混合記憶系統的實作...",
  "mainTopics": ["混合記憶", "Spring AI"],
  "keyDecisions": ["使用短期+長期記憶策略"],
  "actionItems": [],
  "messageCount": 10,
  "createdAt": "2025-10-27T02:00:00"
}
```

### 健康檢查

#### GET /api/advanced/health
檢查服務狀態

## 記憶策略說明

### SHORT_TERM_ONLY
僅使用短期記憶,適用於查詢最近對話內容:
- 關鍵字: "剛才"、"剛剛"
- 使用場景: 需要回憶最近幾輪對話的內容

### LONG_TERM_ONLY
僅使用長期記憶,適用於查詢歷史對話:
- 關鍵字: "之前"、"記得"
- 使用場景: 需要回憶很久以前的對話內容

### HYBRID (預設)
混合策略,同時使用短期和長期記憶:
- 提供最佳的對話體驗
- 自動平衡短期和長期記憶權重

## 配置參數

在 `application.yml` 中可配置以下參數:

```yaml
app:
  memory:
    # 智能摘要設定
    smart-summary:
      enabled: true
      threshold: 50              # 觸發摘要的訊息數閾值
      keep-recent: 20            # 保留最近的訊息數
      summarize-count: 30        # 每次摘要的訊息數

    # 混合記憶策略設定
    hybrid:
      enabled: true
      short-term-weight: 0.6     # 短期記憶權重
      long-term-weight: 0.4      # 長期記憶權重

    # 記憶優化設定
    optimization:
      enabled: true
      max-messages: 100          # 單一對話最大訊息數
      auto-cleanup: true         # 啟用自動清理
      retention-days: 30         # 記憶保留天數
```

## 專案結構

```
src/main/java/com/example/memory/advanced/
├── advisor/                   # Advisor 層
│   └── SmartMemoryAdvisor    # 智能記憶管理增強器
├── config/                    # 配置層
│   ├── AdvancedMemoryConfig  # Spring AI 配置
│   └── MemoryProperties      # 記憶配置屬性
├── controller/                # 控制器層
│   └── AdvancedChatController # REST API 控制器
├── dto/                       # DTO 層
│   ├── ChatRequest/Response
│   ├── SummaryResponse
│   └── AnalyticsResponse
├── model/                     # 資料模型層
│   ├── ConversationSummary
│   ├── MemoryAnalytics
│   ├── MemoryStrategy
│   └── TodoItem
├── repository/                # 儲存庫層
│   └── ChatMemoryRepository
└── service/                   # 服務層
    ├── HybridMemoryService
    └── ConversationSummaryService
```

## 開發狀態

- ✅ 專案基礎結構
- ✅ 資料模型層
- ✅ DTO 層
- ✅ 配置類
- ✅ 智能記憶 Advisor
- ✅ 混合記憶服務
- ✅ 對話摘要服務
- ✅ REST API 控制器
- ✅ 編譯測試

## 注意事項

1. 本專案為教學示範用途,部分功能為簡化實作
2. SmartMemoryAdvisor 目前為獨立服務,未整合為標準 Advisor
3. 長期記憶功能需整合向量儲存系統
4. 生產環境建議使用 PostgreSQL 替代 H2

## 相關專案

- `chapter6-memory-core`: 基礎記憶系統
- `chapter6-memory-vector`: 向量記憶系統

## 授權

MIT License
