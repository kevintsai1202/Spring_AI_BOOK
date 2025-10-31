# Chapter 6.1-6.5: Spring AI 記憶核心系統

這個項目實現了 Spring AI 的**記憶管理核心功能和增強器(Advisors)系統**，涵蓋基礎記憶到高級Advisor鏈式處理。

## 📖 項目概述

### 對應教學章節
- 6.1 記憶系統概述
- 6.2 基本對話記憶實現
- 6.3 官方記憶系統(部分)
- 6.4 鏈式增強器系統
- 6.5 Spring AI 擴展機制(Tools 部分)

## 🎯 核心功能

### 1. 記憶管理系統
- **InMemoryChatMemory**: 基於內存的對話存儲
- **MessageWindowChatMemory**: 滑動視窗記憶實現
- 自動清理和大小管理
- 會話隔離

### 2. Advisor 增強器系統
- **MessageChatMemoryAdvisor**: 訊息級記憶管理
- **ContentFilterAdvisor**: 敏感詞過濾和安全檢查
- **LoggingAdvisor**: 審計日誌和效能追蹤
- **安全檢查 Advisor**: 身份驗證和授權

### 3. Tools 工具系統
- **@Tool 註解**: 方法級工具定義
- **天氣查詢工具**: 實時數據獲取
- **日期時間工具**: 本地化時間處理
- **自定義工具**: 業務邏輯整合

### 4. REST API 服務層
- `POST /api/chat/conversation/{conversationId}` - 對話
- `POST /api/chat/new` - 新建對話
- `GET /api/chat/conversation/{conversationId}/history` - 獲取歷史
- `DELETE /api/chat/conversation/{conversationId}` - 清除對話

## 🚀 快速開始

### 前置要求
- Java 21+
- Maven 3.9+
- OpenAI API Key

### 安裝步驟

1. **複製環境變數文件**
```bash
cp .env.example .env
```

2. **配置 OpenAI API Key**
編輯 `.env` 文件，添加你的 OpenAI API Key：
```
OPENAI_API_KEY=sk-your-api-key-here
```

3. **編譯項目**
```bash
mvn clean compile
```

4. **運行應用**

開發環境:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

或直接運行:
```bash
mvn spring-boot:run
```

5. **驗證服務**
```bash
curl http://localhost:8080/api/chat/health
```

## 📝 使用範例

### 創建新對話
```bash
curl -X POST http://localhost:8080/api/chat/new \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "今天天氣如何？",
    "enableTools": true
  }'
```

### 獲取對話歷史
```bash
curl http://localhost:8080/api/chat/conversation/{conversationId}/history?userId=user123
```

### 刪除對話
```bash
curl -X DELETE http://localhost:8080/api/chat/conversation/{conversationId}
```

## 🏗️ 架構設計

### 項目結構
```
src/main/java/com/example/memory/
├── controller/                   # REST 控制層
│   ├── ChatController
│   └── MemoryController
├── service/                      # 服務層
│   ├── ChatMemoryService
│   ├── AdvisorChainService
│   └── ToolService
├── advisor/                      # Advisor 實現
│   ├── Advisor (interface)
│   ├── AdvisorContext
│   ├── LoggingAdvisor
│   ├── ContentFilterAdvisor
│   └── ...
├── memory/                       # 記憶實現
│   ├── ChatMemory (interface)
│   ├── InMemoryChatMemory
│   └── MessageWindowChatMemory
├── tool/                         # Tools 定義
│   ├── WeatherTools
│   ├── DateTimeTools
│   └── ...
├── dto/                          # 數據轉移對象
├── config/                       # 配置類
└── Application.java              # 主應用類
```

### Advisor 執行順序
1. **SecurityAdvisor** (Order: -1000) - 安全檢查
2. **MemoryAdvisor** (Order: 0) - 記憶管理
3. **ContentFilterAdvisor** (Order: 100) - 內容過濾
4. **LoggingAdvisor** (Order: 800) - 日誌記錄
5. **MetricsAdvisor** (Order: 900) - 統計指標

## 🔧 配置說明

### 記憶存儲配置
在 `application.yml` 中設置：
```yaml
memory:
  storage-type: memory  # 可選: memory, window, jdbc, redis
  window-size: 50       # 滑動窗口大小
```

### Spring AI 配置
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4o-mini
```

## 🧪 測試

運行單元測試：
```bash
mvn test
```

## 📚 相關資源

- [Spring AI 文檔](https://docs.spring.io/spring-ai/docs/current/reference/)
- [OpenAI API 文檔](https://platform.openai.com/docs)

## 📄 授權

MIT License

## 👤 作者

Spring AI 教材項目
