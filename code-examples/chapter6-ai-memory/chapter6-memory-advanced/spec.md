# Chapter 6.8: 進階記憶管理 (Memory Advanced)

## 📖 專案概述

本專案展示了基於 Spring AI 1.0.3 的**進階記憶管理技術**,包括智能記憶摘要、混合記憶策略、對話分析等高級功能。

### 對應教學章節
- 6.8 進階記憶管理與優化（對應原文 Day18）

### 依賴關係
- 依賴 `chapter6-memory-core` 的基礎記憶系統
- 依賴 `chapter6-memory-vector` 的向量記憶系統

---

## 🎯 核心功能

### 1. 智能記憶摘要系統

#### 1.1 自動摘要機制
- **長對話自動摘要**: 當對話超過設定長度時自動觸發摘要
- **摘要策略配置**: 可配置摘要觸發條件和保留策略
- **上下文保留**: 保留最近對話 + 歷史摘要
- **摘要品質評估**: 評估摘要的完整性和準確性

#### 1.2 SmartMemoryAdvisor
```java
/**
 * 智能記憶管理增強器
 * 自動摘要長對話,優化記憶使用
 */
public class SmartMemoryAdvisor implements CallAdvisor {
    // 當對話超過50條時,自動摘要前30條
    // 保留摘要 + 最近20條訊息
}
```

---

### 2. 混合記憶策略系統

#### 2.1 短期與長期記憶結合
- **短期記憶優先**: 最近對話的快速存取
- **長期記憶檢索**: 語義相似的歷史對話
- **動態策略切換**: 根據查詢類型選擇記憶策略
- **記憶融合**: 短期和長期記憶的智能融合

#### 2.2 HybridMemoryService
```java
/**
 * 混合記憶服務
 * 結合短期記憶和向量記憶的優勢
 */
public class HybridMemoryService {
    private final ChatClient shortTermClient;  // 短期記憶客戶端
    private final ChatClient longTermClient;   // 長期記憶客戶端

    // 根據查詢類型動態選擇記憶策略
    public String chat(String conversationId, String userMessage);
}
```

---

### 3. 對話分析與管理系統

#### 3.1 對話摘要功能
- **對話主題提取**: 識別對話的主要主題
- **關鍵決策記錄**: 記錄重要決策和結論
- **待辦事項提取**: 從對話中提取待辦事項
- **對話品質評估**: 評估對話的效果和質量

#### 3.2 ConversationSummaryService
```java
/**
 * 對話摘要服務
 * 提供對話歷史的智能摘要功能
 */
public class ConversationSummaryService {
    // 摘要對話歷史
    public ConversationSummary summarize(String conversationId);

    // 提取對話主題
    public List<String> extractTopics(List<Message> messages);

    // 提取待辦事項
    public List<TodoItem> extractTodos(List<Message> messages);
}
```

#### 3.3 對話歷史管理
- **歷史查詢**: 查詢指定對話的完整歷史
- **歷史導出**: 導出對話記錄為不同格式
- **歷史刪除**: 清除指定對話的所有記憶
- **歷史統計**: 統計對話的數量和使用情況

---

### 4. 記憶優化系統

#### 4.1 記憶清理策略
- **時間窗口清理**: 保留指定時間範圍內的記憶
- **訊息數量限制**: 限制每個對話的最大訊息數
- **相似性去重**: 去除相似或重複的記憶內容
- **重要性排序**: 保留更重要的記憶內容

#### 4.2 MessageWindowChatMemory
```java
/**
 * 滑動視窗記憶管理
 * 自動管理記憶容量
 */
public class MessageWindowChatMemory implements ChatMemory {
    private final int maxMessages = 100;  // 最大訊息數
    private final ChatMemoryRepository repository;

    // 自動清理超出視窗的訊息
    public void add(String conversationId, List<Message> messages);
}
```

---

### 5. REST API 服務層

#### 5.1 對話管理端點
- `POST /api/advanced/chat` - 使用混合記憶策略的對話
- `POST /api/advanced/stream` - 串流式混合記憶對話
- `GET /api/advanced/history/{conversationId}` - 獲取對話歷史
- `DELETE /api/advanced/history/{conversationId}` - 清除對話歷史

#### 5.2 對話分析端點
- `POST /api/advanced/summarize/{conversationId}` - 摘要對話
- `GET /api/advanced/topics/{conversationId}` - 提取對話主題
- `GET /api/advanced/analytics/{conversationId}` - 對話統計分析

---

## 🏗️ 架構設計

```
┌──────────────────────────────────────────────────────────┐
│         Spring AI Advanced Memory System                 │
├──────────────────────────────────────────────────────────┤
│  REST API Layer                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │ AdvancedChatController                             │  │
│  │ ConversationManagementController                   │  │
│  └────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────┤
│  Service Layer                                           │
│  ┌────────────────────────────────────────────────────┐  │
│  │ HybridMemoryService                                │  │
│  │ ConversationSummaryService                         │  │
│  │ MemoryOptimizationService                          │  │
│  └────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────┤
│  Advisor & Enhancement Layer                             │
│  ┌────────────────────────────────────────────────────┐  │
│  │ SmartMemoryAdvisor (智能摘要)                      │  │
│  │ MemoryAnalyticsAdvisor (分析統計)                 │  │
│  └────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────┤
│  Core & Vector Memory (from dependencies)               │
│  ┌────────────────────────────────────────────────────┐  │
│  │ ChatMemory (short-term)                            │  │
│  │ VectorStore (long-term)                            │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

---

## 📊 資料模型

### ConversationSummary 對話摘要
```java
public record ConversationSummary(
    String conversationId,
    String summary,                      // 摘要內容
    List<String> mainTopics,            // 主要主題
    List<String> keyDecisions,          // 關鍵決策
    List<TodoItem> actionItems,         // 待辦事項
    int messageCount,                    // 訊息數量
    LocalDateTime createdAt
) { }
```

### MemoryAnalytics 記憶分析
```java
public record MemoryAnalytics(
    String conversationId,
    int totalMessages,                   // 總訊息數
    int userMessages,                    // 用戶訊息數
    int assistantMessages,               // 助手訊息數
    double avgMessageLength,             // 平均訊息長度
    LocalDateTime firstMessageTime,      // 首次對話時間
    LocalDateTime lastMessageTime,       // 最後對話時間
    Duration totalDuration               // 對話總時長
) { }
```

### MemoryOptimizationConfig 優化配置
```java
public record MemoryOptimizationConfig(
    int maxMessages,                     // 最大訊息數
    int summaryTriggerThreshold,        // 摘要觸發閾值
    int keepRecentMessages,             // 保留最近訊息數
    boolean enableAutoCleanup,          // 啟用自動清理
    Duration retentionPeriod            // 保留期限
) { }
```

---

## 🔄 關鍵流程

### 流程1: 智能記憶管理流程

```
用戶請求
    ↓
[SmartMemoryAdvisor]
    ├─ 獲取對話歷史
    ├─ 檢查訊息數量 > 50?
    │   ├─ 是 → 觸發自動摘要
    │   │   ├─ 摘要前30條訊息
    │   │   ├─ 保留摘要 + 最近20條
    │   │   └─ 更新記憶存儲
    │   └─ 否 → 直接使用歷史訊息
    ↓
[繼續對話處理]
    ↓
返回用戶
```

### 流程2: 混合記憶策略流程

```
接收用戶查詢
    ↓
[HybridMemoryService]
    ├─ 分析查詢類型
    │   ├─ 包含"剛才"、"剛剛" → 短期記憶優先
    │   ├─ 包含"之前"、"記得" → 長期記憶優先
    │   └─ 其他 → 混合策略
    ↓
[短期記憶處理]
    ├─ 使用 MessageChatMemoryAdvisor
    └─ 獲取最近20條訊息
    ↓
[長期記憶處理]
    ├─ 使用 VectorStoreChatMemoryAdvisor
    └─ 語義搜索相似對話
    ↓
[記憶融合]
    ├─ 短期記憶權重: 60%
    ├─ 長期記憶權重: 40%
    └─ 綜合排序
    ↓
調用 LLM 生成回應
    ↓
返回用戶
```

### 流程3: 對話摘要流程

```
請求對話摘要
    ↓
[ConversationSummaryService]
    ├─ 獲取完整對話歷史
    ├─ 檢查歷史是否存在?
    │   ├─ 否 → 返回錯誤
    │   └─ 是 → 繼續
    ↓
[構建摘要提示]
    ├─ 格式化對話內容
    ├─ 添加摘要要求
    │   ├─ 主要討論話題
    │   ├─ 重要決定或結論
    │   └─ 待辦事項或後續行動
    ↓
[調用 LLM 生成摘要]
    ↓
[解析和結構化]
    ├─ 提取主題列表
    ├─ 提取關鍵決策
    └─ 提取待辦事項
    ↓
返回結構化摘要
```

---

## 💾 虛擬碼 (Pseudocode)

### 智能記憶管理 Advisor

```pseudocode
Class SmartMemoryAdvisor implements CallAdvisor {

    private final ChatMemory chatMemory
    private final ChatClient summarizerClient
    private final int SUMMARY_THRESHOLD = 50
    private final int KEEP_RECENT = 20

    Function adviseCall(request, chain) {
        // 1. 獲取對話 ID
        conversationId = request.adviseContext().get("conversationId")

        // 2. 獲取當前對話歷史
        history = chatMemory.get(conversationId)

        // 3. 檢查是否需要摘要
        If history.size() > SUMMARY_THRESHOLD {
            // 4. 進行智能摘要
            oldMessages = history.subList(0, 30)
            summary = summarizeMessages(oldMessages)

            // 5. 構建優化的記憶
            optimizedMemory = []
            optimizedMemory.add(new SystemMessage("對話摘要: " + summary))
            optimizedMemory.addAll(history.subList(history.size() - KEEP_RECENT, history.size()))

            // 6. 更新記憶
            chatMemory.clear(conversationId)
            chatMemory.add(conversationId, optimizedMemory)
        }

        // 7. 繼續執行鏈中的下一個 Advisor
        Return chain.nextCall(request)
    }

    Function summarizeMessages(messages) {
        conversationText = messages.stream()
            .map(msg -> msg.getContent())
            .collect(joining("\n"))

        Return summarizerClient.prompt()
            .user("請簡潔摘要以下對話的重點:\n" + conversationText)
            .call()
            .content()
    }
}
```

### 混合記憶服務

```pseudocode
Class HybridMemoryService {

    private final ChatClient shortTermClient
    private final ChatClient longTermClient

    Function chat(conversationId, userMessage) {
        // 1. 分析查詢類型
        strategy = determineStrategy(userMessage)

        // 2. 根據策略選擇處理方式
        Switch strategy {
            Case SHORT_TERM_ONLY:
                Return chatWithShortTerm(conversationId, userMessage)

            Case LONG_TERM_ONLY:
                Return chatWithLongTerm(conversationId, userMessage)

            Case HYBRID:
                Return chatWithHybrid(conversationId, userMessage)
        }
    }

    Function determineStrategy(userMessage) {
        // 根據關鍵字判斷策略
        If userMessage.contains("剛才") OR userMessage.contains("剛剛"):
            Return MemoryStrategy.SHORT_TERM_ONLY

        If userMessage.contains("之前") OR userMessage.contains("記得"):
            Return MemoryStrategy.LONG_TERM_ONLY

        Return MemoryStrategy.HYBRID
    }

    Function chatWithHybrid(conversationId, userMessage) {
        // 使用兩個 Advisor 的混合策略
        Return chatClient.prompt()
            .advisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                VectorStoreChatMemoryAdvisor.builder(vectorStore).build()
            )
            .advisors(advisor -> advisor.param("conversationId", conversationId))
            .user(userMessage)
            .call()
            .content()
    }
}
```

### 對話摘要服務

```pseudocode
Class ConversationSummaryService {

    private final ChatMemory chatMemory
    private final ChatClient chatClient

    Function summarize(conversationId) {
        // 1. 獲取對話歷史
        history = chatMemory.get(conversationId)

        If history.isEmpty():
            Throw NotFoundException("Conversation not found")

        // 2. 構建摘要提示
        prompt = buildSummaryPrompt(history)

        // 3. 調用 LLM 生成摘要
        summaryText = chatClient.prompt()
            .user(prompt)
            .call()
            .content()

        // 4. 解析和結構化摘要
        Return ConversationSummary {
            conversationId: conversationId,
            summary: summaryText,
            mainTopics: extractTopics(summaryText),
            keyDecisions: extractDecisions(summaryText),
            actionItems: extractTodos(summaryText),
            messageCount: history.size(),
            createdAt: LocalDateTime.now()
        }
    }

    Function buildSummaryPrompt(history) {
        conversationText = history.stream()
            .map(msg -> formatMessage(msg))
            .collect(joining("\n"))

        Return """
            請為以下對話提供簡潔的摘要:

            ${conversationText}

            摘要應包含:
            1. 主要討論話題
            2. 重要決定或結論
            3. 待辦事項或後續行動
            """
    }
}
```

---

## 📦 模組關係圖

```
chapter6-memory-advanced/
│
├── src/main/java/
│   └── com/example/memory/advanced/
│       │
│       ├── controller/                # REST 控制層
│       │   ├── AdvancedChatController
│       │   └── ConversationManagementController
│       │
│       ├── service/                   # 服務層
│       │   ├── HybridMemoryService    # 混合記憶服務
│       │   ├── ConversationSummaryService  # 對話摘要
│       │   └── MemoryOptimizationService   # 記憶優化
│       │
│       ├── advisor/                   # Advisor 實現
│       │   ├── SmartMemoryAdvisor     # 智能記憶管理
│       │   └── MemoryAnalyticsAdvisor # 記憶分析
│       │
│       ├── model/                     # 資料模型
│       │   ├── ConversationSummary
│       │   ├── MemoryAnalytics
│       │   └── MemoryOptimizationConfig
│       │
│       ├── config/                    # 配置類
│       │   ├── AdvancedMemoryConfig
│       │   └── HybridMemoryConfig
│       │
│       ├── dto/                       # 數據轉移對象
│       │   ├── SummaryRequest/Response
│       │   └── AnalyticsResponse
│       │
│       └── Application.java           # 主應用類
│
├── src/main/resources/
│   ├── application.yml
│   └── application-advanced.yml
│
├── src/test/java/
│   └── com/example/memory/advanced/
│       ├── HybridMemoryTest
│       ├── ConversationSummaryTest
│       └── IntegrationTest
│
└── pom.xml
```

---

## 🧪 測試計劃

### 單元測試
- ✅ SmartMemoryAdvisor 自動摘要邏輯
- ✅ HybridMemoryService 策略選擇
- ✅ ConversationSummaryService 摘要生成
- ✅ 記憶清理和優化

### 集成測試
- ✅ 混合記憶端到端流程
- ✅ 對話摘要功能
- ✅ 長對話自動優化
- ✅ 多租戶數據隔離

### 性能測試
- ✅ 長對話處理效能（1000+ 訊息）
- ✅ 記憶檢索速度
- ✅ 摘要生成延遲

---

## 🚀 快速開始

### 前置要求
- Java 21+
- Maven 3.9+
- 已部署 chapter6-memory-core 和 chapter6-memory-vector
- OpenAI API Key

### 配置

```yaml
# application.yml
spring:
  ai:
    memory:
      advanced:
        enabled: true
        auto-summarize: true
        summary-threshold: 50
        keep-recent-messages: 20

app:
  memory:
    optimization:
      max-messages: 100
      auto-cleanup: true
      retention-days: 30
```

### 啟動

```bash
cd chapter6-memory-advanced
mvn spring-boot:run
```

---

## 📝 版本信息

- **Spring Boot**: 3.2.0+
- **Spring AI**: 1.0.0 GA
- **Java**: 21
- **對應原文**: Day18
