# Chapter 6 Memory Core - 測試指南

本文檔說明如何測試 chapter6-memory-core 應用程序。

## 📋 前置要求

1. **Java 21** 已安裝
2. **Maven 3.9+** 已安裝
3. **OpenAI API Key** 已配置
4. **curl** 或 **Postman** 用於測試API

## 🚀 啟動應用

### 方式一：使用 batch 文件（Windows）

```bash
cd E:\Spring_AI_BOOK\code-examples\chapter6-ai-memory\chapter6-memory-core
run.bat
```

### 方式二：使用 Maven 命令

```bash
# 設定環境變數
set JAVA_HOME=D:\java\jdk-21
set OPENAI_API_KEY=sk-your-api-key

# 運行應用
cd E:\Spring_AI_BOOK\code-examples\chapter6-ai-memory\chapter6-memory-core
mvn spring-boot:run
```

### 方式三：運行 JAR 文件

```bash
java -jar target/chapter6-memory-core-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=dev \
  --OPENAI_API_KEY=sk-your-api-key
```

應用啟動成功後會顯示：
```
Started Application in X.XXX seconds
```

## 🧪 測試 API 端點

### 1. 健康檢查

驗證應用是否正常運行

```bash
curl http://localhost:8080/api/chat/health
```

**預期回應：**
```json
{
  "message": "對話服務正常運行"
}
```

### 2. 創建新對話

發送新的對話請求

```bash
curl -X POST http://localhost:8080/api/chat/new \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "你好，請告訴我今天的日期",
    "enableTools": true
  }'
```

**預期回應：**
```json
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "今天是 2025年10月25日 (星期五)",
  "timestamp": "2025-10-25T09:54:00",
  "success": true,
  "toolsUsed": false
}
```

### 3. 在既存對話中發送消息

```bash
curl -X POST http://localhost:8080/api/chat/conversation/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "台北的天氣如何？",
    "enableTools": true
  }'
```

### 4. 獲取對話歷史

```bash
curl http://localhost:8080/api/chat/conversation/550e8400-e29b-41d4-a716-446655440000/history?userId=user123
```

**預期回應：**
```json
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user123",
  "createdAt": "2025-10-25T09:54:00",
  "lastActivityAt": "2025-10-25T09:55:00",
  "messages": [
    {
      "role": "user",
      "content": "你好，請告訴我今天的日期",
      "timestamp": "2025-10-25T09:54:00",
      "isToolCall": false
    },
    {
      "role": "assistant",
      "timestamp": "2025-10-25T09:54:01",
      "isToolCall": false
    }
  ]
}
```

### 5. 刪除對話

```bash
curl -X DELETE http://localhost:8080/api/chat/conversation/550e8400-e29b-41d4-a716-446655440000
```

**預期回應：**
```json
{
  "message": "對話已成功刪除"
}
```

## 📊 使用 Postman 測試

### 導入集合

1. 打開 Postman
2. 導入以下集合：

```json
{
  "info": {
    "name": "Chapter6 Memory Core API",
    "description": "Spring AI 記憶核心系統 API 測試"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/api/chat/health"
      }
    },
    {
      "name": "New Conversation",
      "request": {
        "method": "POST",
        "url": "http://localhost:8080/api/chat/new",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\"userId\": \"user123\", \"message\": \"你好\", \"enableTools\": true}"
        }
      }
    },
    {
      "name": "Get Conversation History",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/api/chat/conversation/{{conversationId}}/history?userId=user123"
      }
    },
    {
      "name": "Delete Conversation",
      "request": {
        "method": "DELETE",
        "url": "http://localhost:8080/api/chat/conversation/{{conversationId}}"
      }
    }
  ]
}
```

## 🔍 調試模式

啟動調試模式以查看詳細的日誌信息：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
```

或修改 `application-dev.yml`：

```yaml
logging:
  level:
    com.example.memory: DEBUG
    org.springframework.ai: DEBUG
```

## 📝 常見問題

### Q: 應用無法啟動，提示 API Key 無效

**A:** 確保在環境變數中設定了有效的 OPENAI_API_KEY：
```bash
set OPENAI_API_KEY=sk-your-valid-api-key
```

### Q: 端口 8080 已被佔用

**A:** 修改 `application.yml` 中的端口：
```yaml
server:
  port: 8081
```

### Q: 對話回應很慢

**A:** 這可能是網絡問題或 OpenAI API 速率限制。檢查日誌以了解詳細信息。

### Q: 記憶未被保存

**A:** 確認已配置記憶存儲類型：
```yaml
memory:
  storage-type: memory  # 或 window/jdbc/redis
  window-size: 50
```

## 🏗️ 架構驗證

### 驗證 Advisor 鏈

發送包含敏感詞的消息來測試 ContentFilterAdvisor：

```bash
curl -X POST http://localhost:8080/api/chat/new \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "這是一個 sensitive_word_1 的測試",
    "enableTools": true
  }'
```

回應中應該看到敏感詞被替換為 ***

### 驗證工具集成

發送使用工具的請求：

```bash
curl -X POST http://localhost:8080/api/chat/new \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "現在幾點了？",
    "enableTools": true
  }'
```

### 驗證記憶存儲

檢查應用日誌中的記憶操作：

```
Adding message to conversation: conv-123
Retrieving all messages for conversation: conv-123
Clearing messages for conversation: conv-123
```

## 📊 性能測試

### 負載測試

使用 Apache JMeter 或 wrk 進行負載測試：

```bash
# 使用 wrk（需要單獨安裝）
wrk -t4 -c100 -d30s \
  -s script.lua \
  http://localhost:8080/api/chat/health
```

### 記憶性能

測試大量消息的記憶性能：

```bash
# 批量添加 1000 條消息
for i in {1..1000}; do
  curl -X POST http://localhost:8080/api/chat/conversation/perf-test \
    -H "Content-Type: application/json" \
    -d "{\"userId\": \"user123\", \"message\": \"Message $i\"}"
done
```

## 📚 相關資源

- [Spring AI 文檔](https://docs.spring.io/spring-ai/docs/current/reference/)
- [OpenAI API 文檔](https://platform.openai.com/docs)
- [REST API 最佳實踐](https://restfulapi.net/)

## 🎯 下一步

完成測試後，您可以：

1. 查看 `chapter6-memory-vector` 項目以了解向量記憶系統
2. 查看 `chapter6-memory-enterprise` 項目以了解企業級功能
3. 根據需要擴展和自定義此應用程序
