# Chapter 6.1-6.5: AI 記憶核心系統 (Memory Core)

## 📖 專案概述

本專案實現了 Spring AI 的**記憶管理核心功能和增強器(Advisors)系統**,涵蓋基礎記憶到高級Advisor鏈式處理。

### 對應教學章節
- 6.1 記憶系統概述
- 6.2 基本對話記憶實現
- 6.3 官方記憶系統(部分)
- 6.4 鏈式增強器系統
- 6.5 Spring AI 擴展機制(Tools 部分)

---

## 🎯 核心功能

### 1. 記憶管理系統 (Memory Management)

#### 1.1 短期記憶 (Short-term Memory)
- **InMemoryChatMemory**: 基於記憶體的對話存儲
- **MessageWindowChatMemory**: 滑動視窗記憶實現
- 自動清理和大小管理
- 會話隔離

#### 1.2 持久化記憶 (Persistent Memory)
- **JDBC 存儲**: 關聯式資料庫(PostgreSQL/MySQL)
- **配置切換機制**: 開發/測試/生產環境配置
- 事務管理和一致性保證

#### 1.3 記憶 API
- 標準化的 ChatMemory 介面
- 單條和批量訊息操作
- 對話歷史檢索
- 清理和管理接口

---

### 2. Advisor 增強器系統 (Advisor Chain)

#### 2.1 核心 Advisor 實現
- **MessageChatMemoryAdvisor**: 訊息級記憶管理
- **內容過濾 Advisor**: 敏感詞過濾和安全檢查
- **日誌記錄 Advisor**: 審計日誌和效能追蹤
- **安全檢查 Advisor**: 身份驗證和授權

#### 2.2 Advisor 鏈式機制
- 責任鏈模式實現
- Order 優先級控制
- 前置和後置處理
- 異常處理和降級

#### 2.3 進階 Advisor 功能
- 非同步 Advisor 處理
- Advisor 快取機制
- 效能監控和指標
- 動態 Advisor 配置

---

### 3. Tools 工具系統 (Tool Integration)

#### 3.1 工具定義
- **@Tool 註解**: 方法級工具定義
- **天氣查詢工具**: 實時數據獲取
- **日期時間工具**: 本地化時間處理
- **自定義工具**: 業務邏輯整合

#### 3.2 工具管理
- ChatClient 與 Tools 整合
- 多工具組合
- 工具參數驗證
- 結果序列化

---

### 4. REST API 服務層

#### 4.1 對話端點
- `POST /api/chat/conversation/{conversationId}` - 新建對話
- `GET /api/chat/conversation/{conversationId}/history` - 獲取歷史
- `DELETE /api/chat/conversation/{conversationId}` - 清除對話

#### 4.2 記憶管理端點
- `POST /api/memory/sync/{conversationId}` - 手動同步
- `GET /api/memory/analytics/{conversationId}` - 統計分析

---

## 🏗️ 架構設計

```
┌─────────────────────────────────────────────────────────┐
│              Spring AI Memory Core System               │
├─────────────────────────────────────────────────────────┤
│  REST API Layer                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │ ChatController    MemoryController               │  │
│  └──────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  Service Layer                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │ ChatMemoryService    AdvisorChainService         │  │
│  │ ToolManagementService                            │  │
│  └──────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  Advisor & Memory Layer                                 │
│  ┌──────────────────────────────────────────────────┐  │
│  │ MessageChatMemoryAdvisor                         │  │
│  │ ContentFilterAdvisor                             │  │
│  │ LoggingAdvisor                                   │  │
│  │ SecurityAdvisor                                  │  │
│  └──────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  Storage Layer                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │ ChatMemory (Interface)                           │  │
│  │ ├─ InMemoryChatMemoryRepository                  │  │
│  │ ├─ JdbcChatMemoryRepository                      │  │
│  │ └─ MessageWindowChatMemory                       │  │
│  └──────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  Tool Layer                                             │
│  ┌──────────────────────────────────────────────────┐  │
│  │ WeatherTools         DateTimeTools               │  │
│  │ CustomBusinessTools                              │  │
│  └──────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  LLM Integration                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │ ChatClient    ChatModel (OpenAI/Ollama)          │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 資料模型

### Message 訊息模型
```java
public interface Message {
    String getContent();           // 訊息內容
    Map<String, Object> getMetadata();  // 元數據
    String getMessageType();       // 訊息類型
}

// 實現類
- UserMessage       // 用戶訊息
- AssistantMessage  // AI 助手訊息
- SystemMessage     // 系統訊息
```

### ChatMemory 記憶模型
```java
public interface ChatMemory {
    void add(String conversationId, Message message);
    void add(String conversationId, List<Message> messages);
    List<Message> get(String conversationId);
    void clear(String conversationId);
}
```

### Advisor Context 上下文
```java
public record AdvisedRequest(
    Prompt prompt,
    String userText,
    Map<String, Object> userParams,
    Map<String, Object> systemParams,
    Map<String, Object> advisorContext
) { }
```

---

## 🔄 關鍵流程

### 流程1: 基本記憶對話流程

```
User Input
    ↓
REST API 接收
    ↓
提取 conversationId
    ↓
ChatMemory.get() 獲取歷史
    ↓
構建 ChatClient.prompt()
    ↓
通過 Advisor Chain
    ├─ MessageChatMemoryAdvisor (加入記憶)
    ├─ ContentFilterAdvisor (過濾內容)
    ├─ LoggingAdvisor (記錄日誌)
    └─ SecurityAdvisor (安全檢查)
    ↓
調用 LLM Model
    ↓
獲取 AI Response
    ↓
ChatMemory.add() 保存到記憶
    ↓
返回結果到用戶
```

### 流程2: Advisor 鏈式執行流程

```
Request 進入 Advisor Chain
    ↓
┌────────────────────────────────┐
│  Advisor 1 (Order: 0)          │
│  ├─ 前置處理 (before)          │
│  │  └─ 修改 Request             │
│  ├─ 執行下一個 Advisor          │
│  │  (chain.nextCall)            │
│  └─ 後置處理 (after)            │
│     └─ 修改 Response            │
└────────────────────────────────┘
    ↓
┌────────────────────────────────┐
│  Advisor 2 (Order: 100)        │
│  ...                            │
└────────────────────────────────┘
    ↓
LLM Call
    ↓
Response 返回經過所有 Advisor 後置處理
    ↓
最終 Response 返回用戶
```

### 流程3: Tools 工具調用流程

```
User Query
    ↓
ChatClient.prompt().tools(WeatherTools, DateTimeTools)
    ↓
LLM 判斷是否需要調用工具
    ↓
是 → Tool Calling
    ├─ WeatherTools.getCurrentWeather()
    ├─ DateTimeTools.getCurrentDateTime()
    └─ ...
    ↓
Tool 執行並返回結果
    ↓
LLM 基於工具結果生成最終回應
    ↓
返回用戶
```

---

## 💾 虛擬碼 (Pseudocode)

### 記憶系統實現
```pseudocode
Class ChatMemoryService {

    Function chat(conversationId, userMessage) {
        // 1. 從存儲獲取歷史訊息
        history = chatMemory.get(conversationId)

        // 2. 構建包含歷史的提示詞
        prompt = buildPrompt(history, userMessage)

        // 3. 通過 Advisor 鏈
        advisors = [MessageChatMemoryAdvisor, LoggingAdvisor, ...]
        advisedRequest = wrapRequest(prompt, conversationId, advisors)

        // 4. 執行鏈中每個 Advisor
        For each advisor in advisors {
            advisedRequest = advisor.advise(advisedRequest, chain)
        }

        // 5. 調用 LLM
        response = chatModel.call(advisedRequest.prompt)

        // 6. 保存到記憶
        chatMemory.add(conversationId, userMessage)
        chatMemory.add(conversationId, response)

        // 7. 返回結果
        Return response
    }
}

Class AdvisorChain {

    Function executeChain(request, advisors, index) {
        If index >= advisors.length {
            // 執行 LLM Call
            Return callLLM(request)
        }

        currentAdvisor = advisors[index]

        // 執行當前 Advisor 的前置處理
        modifiedRequest = currentAdvisor.before(request)

        // 遞迴執行下一個 Advisor
        response = executeChain(modifiedRequest, advisors, index + 1)

        // 執行當前 Advisor 的後置處理
        modifiedResponse = currentAdvisor.after(response)

        Return modifiedResponse
    }
}

Class ToolIntegration {

    Function executeToolCall(query, tools) {
        // 1. 調用 LLM 判斷是否需要工具
        toolDecision = llm.decideTools(query, tools.getSchemas())

        If toolDecision.needsTools {
            // 2. 執行所選工具
            For each toolCall in toolDecision.toolCalls {
                toolName = toolCall.name
                params = toolCall.params

                // 動態調用工具方法
                result = tools[toolName].invoke(params)
                toolResults.add({toolName, result})
            }

            // 3. 基於工具結果生成回應
            finalResponse = llm.generateResponse(query, toolResults)
        } Else {
            // 直接生成回應
            finalResponse = llm.generateResponse(query)
        }

        Return finalResponse
    }
}
```

---

## 🔌 系統脈絡圖 (Context Diagram)

```
┌────────────────┐
│   外部系統      │
│  ┌──────────┐  │
│  │  OpenAI  │  │
│  │  模型    │  │
│  └──────────┘  │
└────────────────┘
        ↕
┌─────────────────────────────────┐
│  Spring AI Memory Core System    │
│                                 │
│  ┌──────────────────────────┐   │
│  │  REST API 層             │   │
│  └──────────────────────────┘   │
│           ↓                      │
│  ┌──────────────────────────┐   │
│  │  Service 層              │   │
│  │  ├─ ChatMemoryService   │   │
│  │  ├─ AdvisorService      │   │
│  │  └─ ToolService         │   │
│  └──────────────────────────┘   │
│           ↓                      │
│  ┌──────────────────────────┐   │
│  │  Advisor & Memory 層     │   │
│  └──────────────────────────┘   │
│           ↓                      │
│  ┌──────────────────────────┐   │
│  │  Storage 層              │   │
│  │  ├─ Memory DB (in-memory)   │
│  │  └─ JDBC Repository         │
│  └──────────────────────────┘   │
└─────────────────────────────────┘
        ↕
┌────────────────┐
│  外部數據源     │
│  ┌──────────┐  │
│  │ PostgreSQL│  │
│  │或 MySQL   │  │
│  └──────────┘  │
│  ┌──────────┐  │
│  │天氣API    │  │
│  │時間服務   │  │
│  └──────────┘  │
└────────────────┘
```

---

## 📦 容器/部署概觀

### 開發環境 (Docker Compose)

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
      - SPRING_PROFILES_ACTIVE=dev
      - OPENAI_API_KEY=${OPENAI_API_KEY}
    depends_on:
      - postgres

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: chatmemory
      POSTGRES_USER: chat
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### 部署步驟

```
1. 環境準備
   - 設定 OPENAI_API_KEY
   - 配置 Java 21

2. 構建應用
   mvn clean package -DskipTests

3. 啟動容器
   docker-compose up -d

4. 驗證服務
   curl http://localhost:8080/health
```

---

## 🗂️ 模組關係圖 (Backend Modules)

```
chapter6-memory-core/
│
├── src/main/java/
│   └── com/example/memory/
│       │
│       ├── controller/                # REST 控制層
│       │   ├── ChatController         # 對話端點
│       │   └── MemoryController       # 記憶管理端點
│       │
│       ├── service/                   # 服務層
│       │   ├── ChatMemoryService      # 記憶服務
│       │   ├── AdvisorChainService    # Advisor 鏈服務
│       │   ├── ToolService            # Tools 管理服務
│       │   └── LoggingService         # 日誌服務
│       │
│       ├── advisor/                   # Advisor 實現
│       │   ├── MessageChatMemoryAdvisor
│       │   ├── ContentFilterAdvisor
│       │   ├── LoggingAdvisor
│       │   ├── SecurityAdvisor
│       │   └── AsyncLoggingAdvisor
│       │
│       ├── tool/                      # Tools 定義
│       │   ├── WeatherTools           # 天氣工具
│       │   ├── DateTimeTools          # 時間工具
│       │   └── CustomBusinessTools    # 業務工具
│       │
│       ├── memory/                    # 記憶實現
│       │   ├── InMemoryChatMemoryRepository
│       │   ├── JdbcChatMemoryRepository
│       │   ├── MessageWindowChatMemory
│       │   └── MemorySyncService
│       │
│       ├── config/                    # 配置類
│       │   ├── ChatClientConfig       # ChatClient 配置
│       │   ├── ChatMemoryConfig       # 記憶配置
│       │   ├── AdvisorChainConfig     # Advisor 配置
│       │   └── SecurityConfig         # 安全配置
│       │
│       ├── dto/                       # 資料轉移對象
│       │   ├── ChatRequest
│       │   ├── ChatResponse
│       │   ├── ConversationHistory
│       │   └── AdvisorContext
│       │
│       ├── exception/                 # 異常處理
│       │   ├── ChatException
│       │   ├── MemoryException
│       │   └── GlobalExceptionHandler
│       │
│       └── Application.java           # 主應用類
│
├── src/main/resources/
│   ├── application.yml                # 主配置
│   ├── application-dev.yml            # 開發配置
│   ├── application-prod.yml           # 生產配置
│   └── db/
│       └── schema.sql                 # 資料庫 Schema
│
├── src/test/java/
│   └── com/example/memory/
│       ├── ChatMemoryServiceTest
│       ├── AdvisorChainTest
│       ├── ToolIntegrationTest
│       └── E2ETest
│
└── pom.xml                           # Maven 依賴配置
```

---

## 🔀 序列圖 (Sequence Diagram)

### 對話流程序列圖

```
User ->> Controller: POST /api/chat/{conversationId}
Controller ->> ChatMemoryService: chat(conversationId, message)
ChatMemoryService ->> ChatMemory: get(conversationId)
ChatMemory -->> ChatMemoryService: [历史訊息列表]
ChatMemoryService ->> AdvisorChainService: buildAdvisorChain()
AdvisorChainService -->> ChatMemoryService: [Advisor 列表]
ChatMemoryService ->> MemoryAdvisor: advise(request)
MemoryAdvisor ->> ChatMemory: 取得歷史訊息
ChatMemory -->> MemoryAdvisor: 訊息列表
MemoryAdvisor ->> FilterAdvisor: chain.next()
FilterAdvisor ->> FilterAdvisor: filterContent()
FilterAdvisor ->> LoggingAdvisor: chain.next()
LoggingAdvisor ->> LoggingAdvisor: logRequest()
LoggingAdvisor ->> ChatModel: call(prompt)
ChatModel -->> LoggingAdvisor: response
LoggingAdvisor ->> LoggingAdvisor: logResponse()
LoggingAdvisor -->> FilterAdvisor: response
FilterAdvisor -->> MemoryAdvisor: response
MemoryAdvisor ->> ChatMemory: add(response)
MemoryAdvisor -->> ChatMemoryService: response
ChatMemoryService -->> Controller: response
Controller -->> User: ChatResponse
```

### Advisor 鏈式執行序列圖

```
Request ->> AdvisorChain: executeChain()
AdvisorChain ->> Advisor1: beforeAdvice()
Advisor1 -->> AdvisorChain: modifiedRequest
AdvisorChain ->> Advisor2: beforeAdvice()
Advisor2 -->> AdvisorChain: modifiedRequest
AdvisorChain ->> LLMModel: call()
LLMModel -->> AdvisorChain: response
AdvisorChain ->> Advisor2: afterAdvice()
Advisor2 -->> AdvisorChain: modifiedResponse
AdvisorChain ->> Advisor1: afterAdvice()
Advisor1 -->> AdvisorChain: modifiedResponse
AdvisorChain -->> Request: finalResponse
```

---

## 💾 ER 圖 (Entity-Relationship Diagram)

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
    created_at TIMESTAMP
}

conversation_metadata {
    conversation_id STRING [PK, FK]
    title STRING
    topic STRING
    status ENUM (ACTIVE, ARCHIVED)
    last_activity TIMESTAMP
}

audit_logs {
    id UUID [PK]
    conversation_id STRING [FK]
    event_type ENUM (ADVISOR_CALL, MEMORY_ADD, FILTER_ACTION)
    details JSON
    created_at TIMESTAMP
}

chat_memory ||--o{ chat_messages: contains
chat_memory ||--o{ conversation_metadata: has
chat_memory ||--o{ audit_logs: generates
```

---

## 🏛️ 類別圖 (Class Diagram - 關鍵類別)

```
┌─────────────────────────────────┐
│      <<interface>>              │
│      ChatMemory                 │
├─────────────────────────────────┤
│ + add(conversationId, message)  │
│ + get(conversationId)           │
│ + clear(conversationId)         │
└─────────────────────────────────┘
        ↑           ↑
        │           │
        │      ┌────────────────────────────┐
        │      │ MessageWindowChatMemory    │
        │      ├────────────────────────────┤
        │      │ - maxMessages: int         │
        │      │ - repository: Repository   │
        │      ├────────────────────────────┤
        │      │ + add()                    │
        │      │ + get()                    │
        │      │ - slideWindow()            │
        │      └────────────────────────────┘
        │
        └─────────────────────────────────────────┐
                                                  │
        ┌─────────────────────────────────────────┘
        │
        ├────────────────────────────────┐
        │ InMemoryChatMemoryRepository   │
        ├────────────────────────────────┤
        │ - conversations: Map            │
        ├────────────────────────────────┤
        │ + save()                        │
        │ + findByConversationId()        │
        │ + deleteByConversationId()      │
        └────────────────────────────────┘

        ├────────────────────────────────┐
        │ JdbcChatMemoryRepository       │
        ├────────────────────────────────┤
        │ - jdbcTemplate: JdbcTemplate    │
        ├────────────────────────────────┤
        │ + save()                        │
        │ + findByConversationId()        │
        │ + deleteByConversationId()      │
        └────────────────────────────────┘


┌──────────────────────────────────┐
│       <<interface>>              │
│       Advisor                    │
├──────────────────────────────────┤
│ + getName(): String              │
│ + getOrder(): int                │
└──────────────────────────────────┘
        ↑
        │
        ├─────────────────────────────────────┐
        │ MessageChatMemoryAdvisor            │
        ├─────────────────────────────────────┤
        │ - chatMemory: ChatMemory            │
        ├─────────────────────────────────────┤
        │ + adviseRequest()                   │
        │ + adviseResponse()                  │
        │ + getOrder(): 0                     │
        └─────────────────────────────────────┘

        ├─────────────────────────────────────┐
        │ ContentFilterAdvisor                │
        ├─────────────────────────────────────┤
        │ - filterService: FilterService      │
        ├─────────────────────────────────────┤
        │ + adviseRequest()                   │
        │ + adviseResponse()                  │
        │ - filterContent()                   │
        │ + getOrder(): 100                   │
        └─────────────────────────────────────┘

        ├─────────────────────────────────────┐
        │ LoggingAdvisor                      │
        ├─────────────────────────────────────┤
        │ - logger: Logger                    │
        ├─────────────────────────────────────┤
        │ + adviseRequest()                   │
        │ + adviseResponse()                  │
        │ - logRequest()                      │
        │ - logResponse()                     │
        │ + getOrder(): 800                   │
        └─────────────────────────────────────┘

        └─────────────────────────────────────┐
            SecurityAdvisor                    │
            ├─────────────────────────────────┤
            │ - securityService: SecuritySvc  │
            ├─────────────────────────────────┤
            │ + adviseRequest()                │
            │ + adviseResponse()               │
            │ - validateUser()                 │
            │ - checkPermissions()             │
            │ + getOrder(): -1000              │
            └─────────────────────────────────┘
```

---

## 🔄 流程圖 (Flowchart)

### 記憶對話完整流程

```
Start
  │
  ├─→ 接收用戶訊息
  │     │
  │     └─→ 驗證 conversationId
  │           ├─ 有效? → 繼續
  │           └─ 無效? → 返回錯誤
  │
  ├─→ 從記憶檢索歷史
  │     │
  │     └─→ 構建提示詞
  │
  ├─→ 執行 Advisor Chain
  │     │
  │     ├─→ [Order: -1000] SecurityAdvisor
  │     │     ├─ 身份驗證
  │     │     ├─ 權限檢查
  │     │     └─ 速率限制
  │     │
  │     ├─→ [Order: 0] MemoryAdvisor
  │     │     └─ 加入歷史訊息
  │     │
  │     ├─→ [Order: 100] FilterAdvisor
  │     │     └─ 過濾敏感詞
  │     │
  │     ├─→ [Order: 800] LoggingAdvisor
  │     │     └─ 記錄請求日誌
  │     │
  │     └─→ [Order: 900] MetricsAdvisor
  │           └─ 記錄統計指標
  │
  ├─→ 調用 LLM 模型
  │     │
  │     ├─→ 判斷是否需要 Tools
  │     │     ├─ 是 → 執行 Tool Call
  │     │     └─ 否 → 直接生成回應
  │     │
  │     └─→ 獲取 AI 回應
  │
  ├─→ 後置處理 (Advisor Response)
  │     │
  │     └─→ 過濾、格式化、審計
  │
  ├─→ 保存到記憶
  │     │
  │     └─→ ChatMemory.add()
  │
  ├─→ 返回結果
  │     │
  │     └─→ REST API Response
  │
End
```

---

## 📈 狀態圖 (State Diagram)

### 對話狀態機

```
           [INITIAL]
              │
              ├─ create()
              ↓
          [ACTIVE]
              ├─ add_message() → [PROCESSING]
              ├─ retrieve_history()
              └─ archive() → [ARCHIVED]

          [PROCESSING]
              ├─ (advisor chain executing)
              ├─ success() → [ACTIVE]
              └─ error() → [ERROR]

          [ERROR]
              ├─ retry() → [PROCESSING]
              └─ cancel() → [CANCELLED]

          [ARCHIVED]
              └─ restore() → [ACTIVE]

          [CANCELLED]
              └─ (terminal state)
```

---

## 📋 範例代碼清單

### 核心實現
1. `ChatMemoryService.java` - 記憶服務主類
2. `MessageChatMemoryAdvisor.java` - 訊息記憶增強器
3. `AdvisorChainService.java` - Advisor 鏈管理
4. `ChatMemoryConfig.java` - 記憶配置
5. `ChatClientConfig.java` - ChatClient 配置

### Advisors
6. `ContentFilterAdvisor.java` - 內容過濾
7. `LoggingAdvisor.java` - 日誌記錄
8. `SecurityAdvisor.java` - 安全檢查
9. `AsyncLoggingAdvisor.java` - 非同步日誌

### Tools
10. `WeatherTools.java` - 天氣工具
11. `DateTimeTools.java` - 時間工具
12. `ToolManagementService.java` - 工具管理

### Storage
13. `InMemoryChatMemoryRepository.java`
14. `JdbcChatMemoryRepository.java`
15. `MemorySyncService.java`

### REST API
16. `ChatController.java` - 對話控制器
17. `MemoryController.java` - 記憶控制器
18. `DTO 類別集合` (ChatRequest, ChatResponse 等)

### 配置和工具
19. `application.yml` - 主配置
20. `application-dev.yml` - 開發配置
21. `schema.sql` - 資料庫 Schema
22. `pom.xml` - Maven 依賴

### 測試
23. `ChatMemoryServiceTest.java`
24. `AdvisorChainTest.java`
25. `ToolIntegrationTest.java`
26. `E2ETest.java`

---

## 🧪 測試計劃

### 單元測試
- ✅ ChatMemory 介面各實現
- ✅ 各 Advisor 前置/後置處理
- ✅ Tool 參數驗證和執行
- ✅ 配置的條件加載

### 集成測試
- ✅ Advisor 鏈式執行順序
- ✅ 訊息在不同存儲間的流轉
- ✅ Tools 與 LLM 的整合
- ✅ REST API 端點功能

### 端到端測試
- ✅ 完整對話流程
- ✅ 記憶持久化和檢索
- ✅ 多並發用戶對話
- ✅ 異常情況處理

---

## 📖 學習路徑

```
1. 理解 ChatMemory 介面
   └─ 實現簡單的 InMemory 存儲
      └─ 實現 JDBC 持久化

2. 學習 Advisor 機制
   └─ 實現 MessageChatMemoryAdvisor
      └─ 實現內容過濾和日誌 Advisor
         └─ 組合多個 Advisor

3. 整合 Tools
   └─ 實現簡單 Tool
      └─ 整合到 ChatClient
         └─ 實現複雜 Tool

4. 建立完整應用
   └─ 設計 REST API
      └─ 實現 Service 層
         └─ 配置生產環境
```

---

## 🚀 快速開始

### 前置要求
- Java 21+
- Maven 3.9+
- PostgreSQL 或 MySQL (可選,開發階段可用記憶體)
- OpenAI API Key

### 啟動步驟

```bash
# 1. 克隆或進入專案
cd chapter6-memory-core

# 2. 設定環境變數
export OPENAI_API_KEY=your-key

# 3. 選擇配置文件
# application-dev.yml (開發,使用記憶體)
# application-prod.yml (生產,使用 PostgreSQL)

# 4. 啟動應用
mvn spring-boot:run -Dspring-boot.run.arguments=\"--spring.profiles.active=dev\"

# 5. 測試 API
curl -X POST http://localhost:8080/api/chat/test-conversation \
  -H "Content-Type: application/json" \
  -d '{"message": "你好"}'
```

---

## 📚 參考資源

- [Spring AI 官方文檔](https://docs.spring.io/spring-ai/reference/)
- [ChatMemory API](https://docs.spring.io/spring-ai/reference/api/chat-memory.html)
- [Advisors 系統](https://docs.spring.io/spring-ai/reference/api/advisors.html)
- [Tool 調用機制](https://docs.spring.io/spring-ai/reference/api/functions.html)
- [PostgreSQL Docker](https://hub.docker.com/_/postgres)

---

## 📝 版本信息

- **Spring Boot**: 3.2.0+
- **Spring AI**: 1.0.0 GA
- **Java**: 21
- **Build Date**: 2024
- **作者**: AI Teaching Material

---

## ⚖️ 許可證

MIT License - 自由使用和修改

