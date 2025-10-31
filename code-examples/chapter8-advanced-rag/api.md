# Advanced RAG 系統 API 文檔

## 文檔版本
- **版本**: 1.0.0
- **API 版本**: v1
- **基礎路徑**: `/api/v1`
- **文檔日期**: 2025-01-30

---

## 目錄

1. [API 概述](#api-概述)
2. [認證與授權](#認證與授權)
3. [通用規範](#通用規範)
4. [RAG 查詢 API](#rag-查詢-api)
5. [文檔管理 API](#文檔管理-api)
6. [內容審核 API](#內容審核-api)
7. [評估測試 API](#評估測試-api)
8. [監控和指標 API](#監控和指標-api)
9. [錯誤碼說明](#錯誤碼說明)

---

## API 概述

### 設計原則
- **RESTful 風格**: 遵循 REST 架構設計原則
- **統一回應格式**: 所有 API 使用統一的回應結構
- **版本控制**: 通過 URL 路徑進行版本管理（如 `/api/v1`）
- **冪等性**: GET、PUT、DELETE 操作保證冪等性
- **無狀態**: API 設計為無狀態，會話信息通過 Token 傳遞

### Base URL

**開發環境**: `http://localhost:8080/api/v1`

**生產環境**: `https://api.your-domain.com/api/v1`

---

## 認證與授權

### API Key 認證

所有 API 請求需要在 Header 中包含 API Key：

```
X-API-Key: your-api-key-here
```

### 請求示例

```bash
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key-here" \
  -d '{"query": "什麼是 Spring AI？"}'
```

---

## 通用規範

### 統一回應格式

#### 成功回應

```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    // 實際數據
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

#### 錯誤回應

```json
{
  "success": false,
  "code": 400,
  "message": "請求參數錯誤",
  "error": {
    "field": "query",
    "detail": "查詢內容不能為空"
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

### HTTP 狀態碼

| 狀態碼 | 說明 |
|-------|------|
| `200 OK` | 請求成功 |
| `201 Created` | 資源創建成功 |
| `400 Bad Request` | 請求參數錯誤 |
| `401 Unauthorized` | 未授權，需要認證 |
| `403 Forbidden` | 禁止訪問 |
| `404 Not Found` | 資源不存在 |
| `429 Too Many Requests` | 請求過於頻繁 |
| `500 Internal Server Error` | 服務器內部錯誤 |

### 分頁參數

```json
{
  "page": 0,      // 頁碼（從 0 開始）
  "size": 20,     // 每頁大小
  "sort": "createdAt,desc"  // 排序（可選）
}
```

### 分頁回應

```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [ /* 數據列表 */ ],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20,
    "first": true,
    "last": false
  }
}
```

---

## RAG 查詢 API

### 1. Advanced RAG 查詢

執行智能檢索和問答查詢。

**端點**: `POST /rag/query`

#### 請求參數

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `query` | String | 是 | 用戶查詢內容 |
| `sessionId` | String | 否 | 會話 ID（用於對話記憶） |
| `options` | RAGQueryOptions | 否 | 查詢選項配置 |
| `enableModeration` | Boolean | 否 | 是否啟用內容審核（默認 true） |

**RAGQueryOptions 參數**：

| 參數 | 類型 | 必填 | 默認值 | 說明 |
|------|------|------|--------|------|
| `finalTopK` | Integer | 否 | 5 | 最終返回的文檔數量 |
| `similarityThreshold` | Double | 否 | 0.7 | 相似度閾值（0-1） |
| `maxContextLength` | Integer | 否 | 4000 | 最大上下文長度 |
| `enableReranking` | Boolean | 否 | true | 是否啟用 Re-ranking |

#### 請求示例

```json
{
  "query": "什麼是 Spring AI 的 Embedding 模型？",
  "sessionId": "session-12345",
  "options": {
    "finalTopK": 5,
    "similarityThreshold": 0.75,
    "maxContextLength": 4000,
    "enableReranking": true
  },
  "enableModeration": true
}
```

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "message": "查詢成功",
  "data": {
    "query": "什麼是 Spring AI 的 Embedding 模型？",
    "response": "Spring AI 的 Embedding 模型是用於將文本轉換為向量表示的模型...",
    "retrievedDocuments": [
      {
        "id": "doc-001",
        "content": "Embedding 模型的詳細說明...",
        "score": 0.95,
        "metadata": {
          "source": "official-docs",
          "title": "Spring AI Embedding",
          "lastUpdated": "2025-01-15"
        }
      }
    ],
    "coarseResultCount": 30,
    "finalResultCount": 5,
    "processingTime": 3245,
    "stages": {
      "coarseRetrieval": 856,
      "reranking": 1234,
      "contextOptimization": 156,
      "llmGeneration": 999
    }
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

#### 錯誤示例

```json
{
  "success": false,
  "code": 400,
  "message": "查詢參數錯誤",
  "error": {
    "field": "query",
    "detail": "查詢內容不能為空"
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

### 2. 批量查詢

批量執行多個查詢（異步處理）。

**端點**: `POST /rag/query/batch`

#### 請求參數

```json
{
  "queries": [
    {
      "id": "q1",
      "query": "什麼是 Spring AI？",
      "options": {
        "finalTopK": 3
      }
    },
    {
      "id": "q2",
      "query": "如何使用 Embedding 模型？",
      "options": {
        "finalTopK": 5
      }
    }
  ],
  "sessionId": "session-12345"
}
```

#### 回應示例

```json
{
  "success": true,
  "code": 202,
  "message": "批量查詢已提交",
  "data": {
    "batchId": "batch-abc123",
    "totalQueries": 2,
    "status": "processing",
    "estimatedTime": 10000
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

### 3. 獲取批量查詢結果

獲取批量查詢的執行結果。

**端點**: `GET /rag/query/batch/{batchId}`

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "data": {
    "batchId": "batch-abc123",
    "status": "completed",
    "totalQueries": 2,
    "completedQueries": 2,
    "results": [
      {
        "id": "q1",
        "query": "什麼是 Spring AI？",
        "response": "Spring AI 是...",
        "processingTime": 3000
      },
      {
        "id": "q2",
        "query": "如何使用 Embedding 模型？",
        "response": "使用 Embedding 模型的方法是...",
        "processingTime": 3500
      }
    ]
  },
  "timestamp": "2025-01-30T10:30:15Z"
}
```

---

## 文檔管理 API

### 4. 添加單個文檔

向向量數據庫添加單個文檔。

**端點**: `POST /documents`

#### 請求參數

```json
{
  "content": "文檔內容...",
  "metadata": {
    "title": "文檔標題",
    "source": "official",
    "category": "技術文檔",
    "author": "作者名稱",
    "lastUpdated": "2025-01-30"
  }
}
```

#### 回應示例

```json
{
  "success": true,
  "code": 201,
  "message": "文檔添加成功",
  "data": {
    "id": "doc-001",
    "content": "文檔內容...",
    "metadata": {
      "title": "文檔標題",
      "source": "official",
      "category": "技術文檔"
    },
    "embedding": {
      "model": "text-embedding-3-small",
      "dimensions": 1024
    },
    "createdAt": "2025-01-30T10:30:00Z"
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

### 5. 批量添加文檔

批量向向量數據庫添加文檔。

**端點**: `POST /documents/batch`

#### 請求參數

```json
{
  "documents": [
    {
      "content": "文檔1內容...",
      "metadata": {
        "title": "文檔1"
      }
    },
    {
      "content": "文檔2內容...",
      "metadata": {
        "title": "文檔2"
      }
    }
  ],
  "preprocessingOptions": {
    "cleanSpecialChars": true,
    "normalizeWhitespace": true,
    "filterByLength": true,
    "minLength": 20,
    "maxLength": 4000
  }
}
```

#### 回應示例

```json
{
  "success": true,
  "code": 201,
  "message": "批量添加文檔成功",
  "data": {
    "totalDocuments": 2,
    "successCount": 2,
    "failedCount": 0,
    "addedDocumentIds": ["doc-001", "doc-002"],
    "processingTime": 2345
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

### 6. 刪除文檔

根據 ID 刪除文檔。

**端點**: `DELETE /documents/{documentId}`

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "message": "文檔刪除成功",
  "data": {
    "documentId": "doc-001",
    "deletedAt": "2025-01-30T10:30:00Z"
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

### 7. 查詢文檔

根據條件查詢文檔。

**端點**: `GET /documents/search`

#### 查詢參數

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `query` | String | 否 | 搜索關鍵字 |
| `source` | String | 否 | 文檔來源 |
| `category` | String | 否 | 文檔類別 |
| `page` | Integer | 否 | 頁碼（默認 0） |
| `size` | Integer | 否 | 每頁大小（默認 20） |

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "id": "doc-001",
        "title": "Spring AI Embedding",
        "source": "official",
        "category": "技術文檔",
        "createdAt": "2025-01-30T10:00:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 20
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

## 內容審核 API

### 8. 內容審核檢查

對內容進行多層安全審核。

**端點**: `POST /moderation/check`

#### 請求參數

```json
{
  "content": "待審核的內容文本",
  "sessionId": "session-12345",
  "userId": "user-001",
  "contentType": "user_query"
}
```

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "data": {
    "passed": true,
    "riskScore": 0.15,
    "flaggedReasons": [],
    "categoryScores": {
      "sexual": 0.01,
      "hate": 0.02,
      "harassment": 0.01,
      "violence": 0.03,
      "self_harm": 0.00,
      "sensitive_words": 0.00,
      "personal_info": 0.00
    },
    "recommendation": "內容安全，可以通過",
    "moderationDetails": [
      {
        "provider": "OpenAI",
        "flagged": false,
        "categories": {
          "sexual": false,
          "hate": false,
          "harassment": false,
          "violence": false
        }
      },
      {
        "provider": "Mistral",
        "flagged": false,
        "categories": {
          "sexual": false,
          "hate": false,
          "violence": false
        }
      },
      {
        "provider": "CustomRules",
        "flagged": false,
        "categories": {
          "sensitive_words": false,
          "personal_info": false
        }
      }
    ]
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

#### 內容被阻擋示例

```json
{
  "success": true,
  "code": 200,
  "data": {
    "passed": false,
    "riskScore": 0.92,
    "flaggedReasons": ["OpenAI", "Mistral"],
    "categoryScores": {
      "sexual": 0.95,
      "hate": 0.12,
      "harassment": 0.08,
      "violence": 0.15
    },
    "recommendation": "建議阻擋此內容，風險分數: 0.92",
    "moderationDetails": [ /* 詳細審核結果 */ ]
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

### 9. 獲取審核統計

獲取內容審核的統計數據。

**端點**: `GET /moderation/statistics`

#### 查詢參數

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `startDate` | String | 否 | 開始日期（ISO 8601 格式） |
| `endDate` | String | 否 | 結束日期（ISO 8601 格式） |

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "data": {
    "totalChecks": 1000,
    "passedCount": 950,
    "blockedCount": 50,
    "passRate": 0.95,
    "averageRiskScore": 0.12,
    "topViolationCategories": [
      {
        "category": "sexual",
        "count": 20,
        "percentage": 0.02
      },
      {
        "category": "sensitive_words",
        "count": 15,
        "percentage": 0.015
      }
    ],
    "timeRange": {
      "startDate": "2025-01-01T00:00:00Z",
      "endDate": "2025-01-30T23:59:59Z"
    }
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

## 評估測試 API

### 10. 執行評估測試

執行 RAG 系統的評估測試。

**端點**: `POST /evaluation/run`

#### 請求參數

```json
{
  "testCases": [
    {
      "id": "test-001",
      "question": "什麼是 Spring AI？",
      "expectedKeywords": ["Spring", "AI", "框架"],
      "expectedContext": "Spring AI 相關文檔",
      "category": "基礎知識"
    },
    {
      "id": "test-002",
      "question": "如何配置 Embedding 模型？",
      "expectedKeywords": ["配置", "Embedding", "模型"],
      "category": "配置管理"
    }
  ],
  "evaluationOptions": {
    "enableRelevancyEvaluation": true,
    "enableFactCheckingEvaluation": true,
    "enableCustomEvaluation": true
  }
}
```

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "message": "評估測試完成",
  "data": {
    "totalTests": 2,
    "passedTests": 2,
    "avgRelevancyScore": 0.92,
    "avgFactualityScore": 0.88,
    "avgCompletenessScore": 0.85,
    "avgCoherenceScore": 0.90,
    "avgResponseTime": 3200,
    "overallScore": 0.89,
    "results": [
      {
        "testCaseId": "test-001",
        "question": "什麼是 Spring AI？",
        "response": "Spring AI 是一個...",
        "relevancyScore": 0.95,
        "factualAccuracy": 0.90,
        "completeness": 0.88,
        "coherence": 0.92,
        "responseTime": 3000,
        "contextRetrieved": 5
      },
      {
        "testCaseId": "test-002",
        "question": "如何配置 Embedding 模型？",
        "response": "配置 Embedding 模型的方法是...",
        "relevancyScore": 0.90,
        "factualAccuracy": 0.85,
        "completeness": 0.82,
        "coherence": 0.88,
        "responseTime": 3400,
        "contextRetrieved": 5
      }
    ],
    "timestamp": "2025-01-30T10:30:00Z"
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

### 11. 獲取評估報告

獲取歷史評估報告列表。

**端點**: `GET /evaluation/reports`

#### 查詢參數

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `page` | Integer | 否 | 頁碼（默認 0） |
| `size` | Integer | 否 | 每頁大小（默認 20） |
| `startDate` | String | 否 | 開始日期 |
| `endDate` | String | 否 | 結束日期 |

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "fileName": "evaluation-2025-01-30T10-30-00.json",
        "timestamp": "2025-01-30T10:30:00Z",
        "overallScore": 0.89,
        "totalTests": 50,
        "passedTests": 45
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "number": 0,
    "size": 20
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

### 12. 立即執行評估測試

觸發立即執行評估測試（使用系統預設測試案例）。

**端點**: `POST /evaluation/run/immediate`

#### 回應示例

```json
{
  "success": true,
  "code": 202,
  "message": "評估測試已啟動",
  "data": {
    "taskId": "eval-task-001",
    "status": "running",
    "estimatedTime": 30000
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

## 監控和指標 API

### 13. 獲取系統指標

獲取系統的關鍵性能指標。

**端點**: `GET /monitoring/metrics`

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "data": {
    "moderation": {
      "totalChecks": 1000,
      "passedCount": 950,
      "blockedCount": 50,
      "passRate": 0.95,
      "averageRiskScore": 0.12
    },
    "evaluation": {
      "lastEvaluationTime": "2025-01-30T10:00:00Z",
      "totalTests": 50,
      "passedTests": 45,
      "overallScore": 0.89,
      "avgResponseTime": 3200
    },
    "embedding": {
      "totalEmbeddings": 5000,
      "cacheHitRate": 0.72,
      "averageProcessingTime": 120,
      "modelUsage": {
        "text-embedding-3-small": 3500,
        "text-embedding-3-large": 1500
      }
    },
    "reranking": {
      "totalRerankings": 500,
      "averageProcessingTime": 1200,
      "averageCompressionRatio": 0.17
    },
    "system": {
      "memory": {
        "total": 4294967296,
        "free": 1073741824,
        "used": 3221225472
      },
      "processors": 8,
      "uptime": 86400
    },
    "timestamp": "2025-01-30T10:30:00Z"
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

### 14. 健康檢查

檢查系統健康狀態。

**端點**: `GET /monitoring/health`

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "data": {
    "status": "UP",
    "components": {
      "database": {
        "status": "UP",
        "details": {
          "database": "PostgreSQL",
          "validationQuery": "isValid()"
        }
      },
      "vectorStore": {
        "status": "UP",
        "details": {
          "type": "PgVector",
          "connectionCount": 10
        }
      },
      "redis": {
        "status": "UP",
        "details": {
          "version": "7.0.0",
          "connectedClients": 5
        }
      },
      "openai": {
        "status": "UP",
        "details": {
          "lastCheckTime": "2025-01-30T10:30:00Z"
        }
      },
      "mistralai": {
        "status": "UP",
        "details": {
          "lastCheckTime": "2025-01-30T10:30:00Z"
        }
      }
    }
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

### 15. 獲取 Embedding 模型性能報告

獲取各 Embedding 模型的性能統計。

**端點**: `GET /embedding/performance`

#### 查詢參數

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `modelName` | String | 否 | 指定模型名稱 |

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "data": {
    "models": [
      {
        "modelName": "text-embedding-3-small",
        "totalUsage": 3500,
        "averageProcessingTime": 98.5,
        "averageTextLength": 450,
        "processingSpeed": 4568.5,
        "lastUsed": "2025-01-30T10:25:00Z"
      },
      {
        "modelName": "text-embedding-3-large",
        "totalUsage": 1500,
        "averageProcessingTime": 156.2,
        "averageTextLength": 480,
        "processingSpeed": 3071.8,
        "lastUsed": "2025-01-30T10:28:00Z"
      }
    ],
    "recommendedModel": "text-embedding-3-small"
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

### 16. 獲取儀表板數據

獲取監控儀表板的綜合數據。

**端點**: `GET /monitoring/dashboard`

#### 回應示例

```json
{
  "success": true,
  "code": 200,
  "data": {
    "overallScore": 0.89,
    "lastEvaluationTime": "2025-01-30T10:00:00Z",
    "totalTests": 50,
    "passRate": 0.90,
    "avgResponseTime": 3200,
    "moderationStats": {
      "totalChecks": 1000,
      "passRate": 0.95,
      "averageRiskScore": 0.12
    },
    "systemStats": {
      "uptime": 86400,
      "cpuUsage": 0.45,
      "memoryUsage": 0.75,
      "diskUsage": 0.60
    },
    "recentAlerts": [
      {
        "severity": "warning",
        "message": "回應時間較長",
        "timestamp": "2025-01-30T09:00:00Z"
      }
    ]
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

## 錯誤碼說明

### 通用錯誤碼

| 錯誤碼 | HTTP 狀態 | 說明 | 解決方案 |
|-------|----------|------|---------|
| `1000` | 400 | 請求參數錯誤 | 檢查請求參數格式和必填項 |
| `1001` | 400 | 參數驗證失敗 | 確認參數符合驗證規則 |
| `1002` | 400 | 無效的查詢內容 | 提供有效的查詢文本 |
| `1003` | 400 | 查詢內容過長 | 減少查詢文本長度 |
| `1004` | 400 | 無效的 JSON 格式 | 檢查 JSON 格式是否正確 |

### 認證和授權錯誤碼

| 錯誤碼 | HTTP 狀態 | 說明 | 解決方案 |
|-------|----------|------|---------|
| `2000` | 401 | 未提供 API Key | 在請求頭添加 X-API-Key |
| `2001` | 401 | API Key 無效 | 使用有效的 API Key |
| `2002` | 401 | API Key 已過期 | 更新 API Key |
| `2003` | 403 | 權限不足 | 聯繫管理員獲取權限 |
| `2004` | 429 | 請求過於頻繁 | 降低請求頻率 |

### RAG 系統錯誤碼

| 錯誤碼 | HTTP 狀態 | 說明 | 解決方案 |
|-------|----------|------|---------|
| `3000` | 500 | RAG 查詢失敗 | 稍後重試或聯繫支援 |
| `3001` | 404 | 未找到相關文檔 | 擴大檢索範圍或調整查詢 |
| `3002` | 500 | 向量檢索失敗 | 檢查向量數據庫連接 |
| `3003` | 500 | LLM 調用失敗 | 檢查 OpenAI API 狀態 |
| `3004` | 500 | Embedding 生成失敗 | 檢查 Embedding 服務狀態 |
| `3005` | 500 | Re-ranking 失敗 | 檢查 Re-ranking 服務配置 |

### 內容審核錯誤碼

| 錯誤碼 | HTTP 狀態 | 說明 | 解決方案 |
|-------|----------|------|---------|
| `4000` | 403 | 內容被阻擋 | 修改內容後重試 |
| `4001` | 500 | 內容審核服務失敗 | 稍後重試 |
| `4002` | 500 | OpenAI 審核服務不可用 | 檢查 OpenAI API 狀態 |
| `4003` | 500 | Mistral AI 審核服務不可用 | 檢查 Mistral AI API 狀態 |

### 文檔管理錯誤碼

| 錯誤碼 | HTTP 狀態 | 說明 | 解決方案 |
|-------|----------|------|---------|
| `5000` | 400 | 文檔內容為空 | 提供有效的文檔內容 |
| `5001` | 400 | 文檔過大 | 減少文檔大小或分割文檔 |
| `5002` | 404 | 文檔不存在 | 確認文檔 ID 正確 |
| `5003` | 500 | 文檔添加失敗 | 檢查向量數據庫狀態 |
| `5004` | 500 | 文檔刪除失敗 | 檢查向量數據庫連接 |

### 評估測試錯誤碼

| 錯誤碼 | HTTP 狀態 | 說明 | 解決方案 |
|-------|----------|------|---------|
| `6000` | 400 | 測試案例無效 | 檢查測試案例格式 |
| `6001` | 500 | 評估測試失敗 | 查看詳細錯誤日誌 |
| `6002` | 404 | 評估報告不存在 | 確認報告 ID 正確 |

---

## Rate Limiting（速率限制）

### 限制規則

| 端點 | 限制 | 說明 |
|------|------|------|
| `/rag/query` | 100 請求/分鐘 | 單個查詢 |
| `/rag/query/batch` | 10 請求/分鐘 | 批量查詢 |
| `/documents` | 50 請求/分鐘 | 文檔管理 |
| `/moderation/check` | 200 請求/分鐘 | 內容審核 |
| `/evaluation/*` | 20 請求/分鐘 | 評估測試 |
| `/monitoring/*` | 100 請求/分鐘 | 監控查詢 |

### Rate Limit 回應頭

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1706606460
```

### 超出限制回應

```json
{
  "success": false,
  "code": 429,
  "message": "請求過於頻繁，請稍後再試",
  "error": {
    "retryAfter": 60,
    "limit": 100,
    "remaining": 0,
    "resetTime": "2025-01-30T10:31:00Z"
  },
  "timestamp": "2025-01-30T10:30:00Z"
}
```

---

## WebSocket API（即時通知）

### 連接端點

`ws://localhost:8080/ws/notifications`

### 訂閱主題

```json
{
  "action": "subscribe",
  "topics": [
    "evaluation.completed",
    "moderation.blocked",
    "system.alert"
  ]
}
```

### 消息格式

```json
{
  "topic": "evaluation.completed",
  "timestamp": "2025-01-30T10:30:00Z",
  "data": {
    "evaluationId": "eval-001",
    "overallScore": 0.89,
    "totalTests": 50,
    "passedTests": 45
  }
}
```

---

## API 使用示例

### cURL 示例

```bash
# 1. RAG 查詢
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "query": "什麼是 Spring AI？",
    "options": {
      "finalTopK": 5,
      "enableReranking": true
    }
  }'

# 2. 內容審核
curl -X POST http://localhost:8080/api/v1/moderation/check \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "content": "待審核的內容"
  }'

# 3. 添加文檔
curl -X POST http://localhost:8080/api/v1/documents \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "content": "文檔內容...",
    "metadata": {
      "title": "文檔標題",
      "source": "official"
    }
  }'

# 4. 健康檢查
curl -X GET http://localhost:8080/api/v1/monitoring/health \
  -H "X-API-Key: your-api-key"
```

### JavaScript 示例

```javascript
// RAG 查詢
async function queryRAG(query) {
  const response = await fetch('http://localhost:8080/api/v1/rag/query', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-API-Key': 'your-api-key'
    },
    body: JSON.stringify({
      query: query,
      options: {
        finalTopK: 5,
        enableReranking: true
      }
    })
  });

  const data = await response.json();
  console.log(data);
}

// 內容審核
async function checkContent(content) {
  const response = await fetch('http://localhost:8080/api/v1/moderation/check', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-API-Key': 'your-api-key'
    },
    body: JSON.stringify({
      content: content
    })
  });

  const data = await response.json();
  return data.data.passed;
}
```

### Python 示例

```python
import requests

API_BASE_URL = "http://localhost:8080/api/v1"
API_KEY = "your-api-key"

headers = {
    "Content-Type": "application/json",
    "X-API-Key": API_KEY
}

# RAG 查詢
def query_rag(query):
    response = requests.post(
        f"{API_BASE_URL}/rag/query",
        headers=headers,
        json={
            "query": query,
            "options": {
                "finalTopK": 5,
                "enableReranking": True
            }
        }
    )
    return response.json()

# 內容審核
def check_content(content):
    response = requests.post(
        f"{API_BASE_URL}/moderation/check",
        headers=headers,
        json={"content": content}
    )
    data = response.json()
    return data["data"]["passed"]

# 批量添加文檔
def add_documents(documents):
    response = requests.post(
        f"{API_BASE_URL}/documents/batch",
        headers=headers,
        json={"documents": documents}
    )
    return response.json()
```

---

## 版本變更記錄

| 版本 | 日期 | 變更內容 |
|------|------|---------|
| 1.0.0 | 2025-01-30 | 初始版本，包含所有核心 API |

---

## 支援與聯繫

- **技術文檔**: https://docs.your-domain.com
- **API 測試環境**: https://api-sandbox.your-domain.com
- **問題反饋**: support@your-domain.com
- **GitHub**: https://github.com/your-org/advanced-rag

---

**文檔結束**
