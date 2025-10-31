# Chapter 6 Advanced Memory - API 使用指南

## 目錄

1. [進階聊天 API](#進階聊天-api)
2. [對話摘要 API](#對話摘要-api)
3. [對話管理 API](#對話管理-api)
4. [記憶優化 API](#記憶優化-api)
5. [測試範例](#測試範例)

---

## 進階聊天 API

### 1. 混合記憶對話

**端點**: `POST /api/advanced/chat`

**描述**: 使用混合記憶策略進行對話

**請求體**:
```json
{
  "conversationId": "user123",
  "message": "你好,請介紹一下混合記憶系統",
  "strategy": "HYBRID"
}
```

**參數說明**:
- `conversationId` (必填): 對話唯一識別碼
- `message` (必填): 用戶訊息內容
- `strategy` (選填): 記憶策略,可選值:
  - `SHORT_TERM_ONLY`: 僅使用短期記憶
  - `LONG_TERM_ONLY`: 僅使用長期記憶
  - `HYBRID`: 混合策略 (預設)

**回應範例**:
```json
{
  "conversationId": "user123",
  "message": "混合記憶系統結合了短期記憶和長期記憶...",
  "usedStrategy": "HYBRID",
  "messageCount": 10
}
```

**PowerShell 測試**:
```powershell
$request = @{
    conversationId = "user123"
    message = "你好"
    strategy = "HYBRID"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8086/api/advanced/chat" `
    -Method Post `
    -Body $request `
    -ContentType "application/json"
```

### 2. 健康檢查

**端點**: `GET /api/advanced/health`

**描述**: 檢查進階聊天服務狀態

**回應**: `"Advanced Memory Service is running!"`

---

## 對話摘要 API

### 1. 生成對話摘要

**端點**: `POST /api/advanced/summarize/{conversationId}`

**描述**: 為指定對話生成智能摘要

**路徑參數**:
- `conversationId`: 對話唯一識別碼

**回應範例**:
```json
{
  "conversationId": "user123",
  "summary": "本次對話討論了混合記憶系統的實作原理...",
  "mainTopics": ["混合記憶", "Spring AI", "記憶管理"],
  "keyDecisions": ["使用短期+長期記憶策略"],
  "actionItems": [
    {
      "description": "測試混合記憶功能",
      "priority": "HIGH",
      "dueDate": null,
      "completed": false
    }
  ],
  "messageCount": 10,
  "createdAt": "2025-10-27T02:00:00"
}
```

**PowerShell 測試**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/advanced/summarize/user123" `
    -Method Post
```

---

## 對話管理 API

### 1. 獲取對話分析統計

**端點**: `GET /api/management/analytics/{conversationId}`

**描述**: 獲取對話的詳細統計分析

**回應範例**:
```json
{
  "conversationId": "user123",
  "totalMessages": 20,
  "userMessages": 10,
  "assistantMessages": 10,
  "avgMessageLength": 85.5,
  "firstMessageTime": "2025-10-27T01:00:00",
  "lastMessageTime": "2025-10-27T02:00:00",
  "durationInSeconds": 3600,
  "durationInMinutes": 60,
  "isActive": true
}
```

**PowerShell 測試**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/management/analytics/user123" `
    -Method Get
```

### 2. 獲取對話統計摘要

**端點**: `GET /api/management/analytics/{conversationId}/summary`

**描述**: 獲取對話統計的文字摘要

**回應範例**:
```
對話統計摘要 [user123]:
- 總訊息數: 20
- 用戶訊息: 10
- 助手訊息: 10
- 平均訊息長度: 85.5 字元
- 對話時長: 60 分鐘
- 活躍狀態: 是
```

**PowerShell 測試**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/management/analytics/user123/summary" `
    -Method Get
```

### 3. 獲取記憶統計資訊

**端點**: `GET /api/management/memory/stats/{conversationId}`

**描述**: 獲取記憶使用統計

**回應範例**:
```json
{
  "conversationId": "user123",
  "currentMessages": 45,
  "maxMessages": 100,
  "usagePercentage": 45.0,
  "timestamp": "2025-10-27T02:00:00"
}
```

**PowerShell 測試**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/management/memory/stats/user123" `
    -Method Get
```

---

## 記憶優化 API

### 1. 優化對話記憶

**端點**: `POST /api/management/memory/optimize/{conversationId}`

**描述**: 手動觸發記憶優化,清理超出限制的舊訊息

**回應**: `"記憶優化完成"`

**PowerShell 測試**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/management/memory/optimize/user123" `
    -Method Post
```

### 2. 清除對話記憶

**端點**: `DELETE /api/management/memory/{conversationId}`

**描述**: 清除指定對話的所有記憶

**回應**: `"記憶已清除"`

**PowerShell 測試**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/management/memory/user123" `
    -Method Delete
```

### 3. 健康檢查

**端點**: `GET /api/management/health`

**描述**: 檢查對話管理服務狀態

**回應**: `"Conversation Management Service is running!"`

---

## 測試範例

### 完整對話流程測試

```powershell
# 1. 健康檢查
Invoke-RestMethod -Uri "http://localhost:8086/api/advanced/health" -Method Get

# 2. 開始對話
$chat1 = @{
    conversationId = "test-user"
    message = "你好,請介紹一下Spring AI的記憶管理功能"
    strategy = "HYBRID"
} | ConvertTo-Json

$response1 = Invoke-RestMethod -Uri "http://localhost:8086/api/advanced/chat" `
    -Method Post -Body $chat1 -ContentType "application/json"

Write-Host "AI: $($response1.message)"

# 3. 繼續對話
$chat2 = @{
    conversationId = "test-user"
    message = "剛才你提到了什麼重點?"
    strategy = "SHORT_TERM_ONLY"
} | ConvertTo-Json

$response2 = Invoke-RestMethod -Uri "http://localhost:8086/api/advanced/chat" `
    -Method Post -Body $chat2 -ContentType "application/json"

Write-Host "AI: $($response2.message)"

# 4. 獲取對話分析
$analytics = Invoke-RestMethod -Uri "http://localhost:8086/api/management/analytics/test-user" `
    -Method Get

Write-Host "訊息總數: $($analytics.totalMessages)"

# 5. 生成對話摘要
$summary = Invoke-RestMethod -Uri "http://localhost:8086/api/advanced/summarize/test-user" `
    -Method Post

Write-Host "對話摘要: $($summary.summary)"

# 6. 獲取記憶統計
$stats = Invoke-RestMethod -Uri "http://localhost:8086/api/management/memory/stats/test-user" `
    -Method Get

Write-Host "記憶使用率: $($stats.usagePercentage)%"

# 7. 清除記憶
Invoke-RestMethod -Uri "http://localhost:8086/api/management/memory/test-user" `
    -Method Delete
```

### 使用測試腳本

專案提供了完整的測試腳本 `test-api.ps1`,可以直接執行:

```powershell
# 確保應用程式正在運行
cd E:\Spring_AI_BOOK\code-examples\chapter6-ai-memory\chapter6-memory-advanced

# 執行測試腳本
.\test-api.ps1
```

---

## 記憶策略說明

### SHORT_TERM_ONLY (短期記憶)
- **適用場景**: 查詢最近幾輪對話的內容
- **觸發關鍵字**: "剛才"、"剛剛"
- **記憶範圍**: 最近 20 條訊息
- **優點**: 回應速度快,上下文連貫性好
- **限制**: 無法回憶較早的對話內容

### LONG_TERM_ONLY (長期記憶)
- **適用場景**: 查詢很久以前的對話內容
- **觸發關鍵字**: "之前"、"記得"
- **記憶範圍**: 所有歷史對話 (基於語義相似度)
- **優點**: 可以回憶任何時間點的對話
- **限制**: 需要向量化支持 (當前版本暫未實作)

### HYBRID (混合策略, 預設)
- **適用場景**: 一般對話,自動平衡短期和長期記憶
- **記憶範圍**: 短期記憶 + 長期記憶
- **權重配置**:
  - 短期記憶權重: 60%
  - 長期記憶權重: 40%
- **優點**: 提供最佳的對話體驗

---

## 錯誤處理

### 常見錯誤

1. **404 Not Found**: 確認 API 端點路徑正確
2. **500 Internal Server Error**: 檢查應用程式日誌
3. **Connection Refused**: 確認應用程式是否正在運行

### 除錯建議

1. 檢查應用程式日誌:
   ```bash
   # 日誌位置: console 輸出
   ```

2. 確認配置正確:
   ```yaml
   # application.yml
   server:
     port: 8086  # 確認端口號
   ```

3. 檢查 OpenAI API Key:
   ```bash
   # .env 檔案
   OPENAI_API_KEY=your_api_key_here
   ```

---

## 配置參數

可在 `application.yml` 中調整以下參數:

```yaml
app:
  memory:
    smart-summary:
      threshold: 50              # 觸發摘要的訊息數
      keep-recent: 20            # 摘要後保留的訊息數
      summarize-count: 30        # 每次摘要的訊息數

    optimization:
      max-messages: 100          # 單一對話最大訊息數
      auto-cleanup: true         # 啟用自動清理
      retention-days: 30         # 記憶保留天數
```

---

## 更多資訊

- 專案 README: `README.md`
- 規格文件: `spec.md`
- 測試腳本: `test-api.ps1`
