# 測試指南

## 📋 測試環境準備

### 1. 安裝必要工具

- Java 21
- Maven 3.9+
- Docker Desktop
- PowerShell 7+ (Windows)

### 2. 設定環境變數

創建 `.env` 文件:

```env
OPENAI_API_KEY=your-openai-api-key-here
NEO4J_PASSWORD=testpassword
```

## 🚀 啟動測試環境

### 方式 1: 使用 Docker Compose (推薦)

```powershell
# 啟動所有服務
docker-compose up -d

# 查看服務狀態
docker-compose ps

# 查看應用日誌
docker-compose logs -f app
```

### 方式 2: 本地運行

```powershell
# 1. 只啟動 Neo4j
docker-compose up -d neo4j

# 2. 設定環境變數
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"
$env:OPENAI_API_KEY="your-api-key"

# 3. 運行應用
.\run.bat
# 或
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

## 🧪 執行測試

### 1. 運行自動化測試腳本

```powershell
# 執行完整的 API 測試
.\test-api.ps1
```

測試腳本會執行以下測試:
1. ✅ 健康檢查
2. ✅ 混合記憶對話 (兩輪)
3. ✅ 智能策略對話
4. ✅ 手動同步記憶
5. ✅ 獲取記憶分析
6. ✅ 檢索記憶
7. ✅ 獲取活躍對話列表
8. ✅ 清理測試數據

### 2. 手動測試 API

#### 測試 1: 健康檢查

```powershell
curl http://localhost:8080/api/vector-chat/health
```

預期響應:
```
Vector Chat Service is running
```

#### 測試 2: 混合記憶對話

```powershell
# 第一輪對話
$body = @{
    message = "介紹一下 Spring AI"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/vector-chat/test-001/hybrid" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

預期響應:
```json
{
  "conversationId": "test-001",
  "response": "Spring AI 是...",
  "strategy": "HYBRID",
  "shortTermCount": 2,
  "longTermCount": 0,
  "timestamp": "2025-01-27T..."
}
```

#### 測試 3: 短期記憶對話

```powershell
$body = @{
    message = "剛才我問了什麼?"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/vector-chat/test-001/short-term" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

#### 測試 4: 長期記憶對話

```powershell
$body = @{
    message = "之前討論過的內容有哪些?"
    similarityThreshold = 0.75
    topK = 10
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/vector-chat/test-001/long-term" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

#### 測試 5: 智能策略對話

```powershell
$body = @{
    message = "記得我們之前聊過 Spring AI 嗎?"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/vector-chat/test-001/smart" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

#### 測試 6: 手動同步記憶

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/memory/sync/test-001" -Method Post
```

預期響應:
```json
{
  "success": true,
  "conversationId": "test-001",
  "syncTime": "2025-01-27T...",
  "message": "記憶同步成功"
}
```

#### 測試 7: 獲取記憶分析

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/memory/analytics/test-001"
```

預期響應:
```json
{
  "conversationId": "test-001",
  "shortTermCount": 4,
  "longTermCount": 4,
  "totalCharacterCount": 450,
  "averageRelevanceScore": 0.85,
  "lastSyncTime": "2025-01-27T...",
  "syncStatus": "SYNCED",
  "healthScore": 95
}
```

#### 測試 8: 檢索記憶

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/memory/retrieve/test-001?query=Spring AI" -Method Post
```

#### 測試 9: 刪除對話記憶

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/memory/test-001" -Method Delete
```

## 🔍 驗證測試結果

### 1. 檢查 Neo4j 數據庫

訪問 Neo4j Browser: http://localhost:7474

登入信息:
- Username: neo4j
- Password: (你在 .env 中設定的密碼)

執行 Cypher 查詢:

```cypher
// 查看所有記憶文檔
MATCH (n:MemoryDocument) RETURN n LIMIT 25

// 查看特定對話的記憶
MATCH (n:MemoryDocument)
WHERE n.`metadata.conversationId` = 'test-001'
RETURN n

// 查看向量索引
SHOW INDEXES
```

### 2. 檢查應用日誌

```powershell
# Docker 模式
docker-compose logs -f app

# 本地模式
# 日誌輸出到控制台和 logs/chapter6-memory-vector-dev.log
```

查找關鍵日誌:
- ✅ "初始化 Neo4j Driver"
- ✅ "初始化 OpenAI Embedding Model"
- ✅ "初始化 Neo4jVectorStore"
- ✅ "成功同步 X 條訊息到向量資料庫"

### 3. 監控指標

訪問 Actuator 端點:

```powershell
# 健康檢查
curl http://localhost:8080/actuator/health

# Prometheus 指標
curl http://localhost:8080/actuator/prometheus
```

## 🐛 常見問題排查

### 問題 1: 連接 Neo4j 失敗

**症狀**:
```
Failed to connect to Neo4j
```

**解決方案**:
```powershell
# 檢查 Neo4j 容器狀態
docker-compose ps neo4j

# 查看 Neo4j 日誌
docker-compose logs neo4j

# 重啟 Neo4j
docker-compose restart neo4j

# 等待 30 秒後重試
```

### 問題 2: OpenAI API 錯誤

**症狀**:
```
OpenAI API error: 401 Unauthorized
```

**解決方案**:
1. 檢查 API Key 是否正確
2. 確認 `.env` 文件中的 `OPENAI_API_KEY` 設定
3. 檢查 API 額度

### 問題 3: 記憶同步失敗

**症狀**:
```
添加文檔到向量資料庫失敗
```

**解決方案**:
1. 確認 Neo4j 正常運行
2. 檢查向量索引是否已創建:
```cypher
SHOW INDEXES
```
3. 查看詳細錯誤日誌

### 問題 4: 測試腳本執行失敗

**症狀**:
```
Invoke-RestMethod: Connection refused
```

**解決方案**:
```powershell
# 1. 確認應用正在運行
curl http://localhost:8080/actuator/health

# 2. 檢查端口是否被占用
netstat -ano | findstr :8080

# 3. 查看應用日誌
docker-compose logs app
```

## 📊 性能測試

### 並發測試 (需要安裝 Apache Bench)

```powershell
# 安裝 Apache Bench (如果未安裝)
# 或使用 JMeter、k6 等工具

# 測試混合記憶對話端點
ab -n 100 -c 10 -T 'application/json' `
   -p test-payload.json `
   http://localhost:8080/api/vector-chat/perf-test/hybrid
```

### 負載測試建議

- 並發用戶數: 10-50
- 每個用戶對話輪次: 5-10 輪
- 監控指標:
  - 響應時間 (應 < 3秒)
  - 記憶檢索時間 (應 < 500ms)
  - 向量搜尋時間 (應 < 200ms)

## ✅ 測試檢查清單

- [ ] 應用成功啟動
- [ ] Neo4j 連接正常
- [ ] OpenAI API 調用成功
- [ ] 短期記憶功能正常
- [ ] 長期記憶功能正常
- [ ] 混合記憶功能正常
- [ ] 智能策略選擇正常
- [ ] 記憶自動同步功能正常
- [ ] 記憶檢索功能正常
- [ ] 記憶分析功能正常
- [ ] API 響應時間合理
- [ ] 錯誤處理正常
- [ ] 日誌輸出正常
- [ ] 監控指標可訪問

## 📝 測試報告模板

```markdown
# 測試報告

**測試日期**: 2025-01-27
**測試人員**: [姓名]
**測試環境**:
- OS: Windows 11
- Java: 21
- Neo4j: 5.15

## 測試結果

| 測試項目 | 狀態 | 備註 |
|---------|------|------|
| 應用啟動 | ✅ | 正常 |
| API 健康檢查 | ✅ | 正常 |
| 短期記憶對話 | ✅ | 正常 |
| 長期記憶對話 | ✅ | 正常 |
| 混合記憶對話 | ✅ | 正常 |
| 智能策略對話 | ✅ | 正常 |
| 記憶同步 | ✅ | 正常 |
| 記憶檢索 | ✅ | 正常 |
| 記憶分析 | ✅ | 正常 |

## 性能指標

- 平均響應時間: 2.5秒
- 記憶檢索時間: 180ms
- 向量搜尋時間: 120ms

## 問題和建議

1. [問題描述]
2. [改進建議]
```

---

**最後更新**: 2025-01-27
