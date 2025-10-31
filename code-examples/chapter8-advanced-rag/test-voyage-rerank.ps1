# Voyage Rerank-2.5 完整測試腳本
$BaseUrl = "http://localhost:8080"

Write-Host "=================================" -ForegroundColor Cyan
Write-Host "Voyage Rerank-2.5 完整測試" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

# 測試 1: 健康檢查
Write-Host "測試 1: 健康檢查" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method Get
    Write-Host "✓ 應用狀態: $($health.status)" -ForegroundColor Green
    Write-Host "  - PostgreSQL: $($health.components.db.status)" -ForegroundColor Green
    Write-Host "  - Redis: $($health.components.redis.status)" -ForegroundColor Green
} catch {
    Write-Host "✗ 健康檢查失敗: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 測試 2: 載入測試文檔
Write-Host "測試 2: 載入測試文檔到向量存儲" -ForegroundColor Yellow
$documents = @(
    @{
        content = "Spring AI 是一個基於 Spring Boot 的 AI 框架，提供了完整的 RAG 實現，包括向量存儲、文檔檢索和生成等功能。它支援多種 AI 模型如 OpenAI、Azure OpenAI 等。"
        metadata = @{
            title = "Spring AI 框架介紹"
            source = "docs"
            category = "spring-ai"
        }
    },
    @{
        content = "Voyage AI 提供先進的 rerank-2.5 模型，專門用於文檔重排序，提高檢索準確性。它支援多語言，包括中文，並且效果優於傳統的 BM25 算法。"
        metadata = @{
            title = "Voyage AI Rerank 模型"
            source = "docs"
            category = "reranking"
        }
    },
    @{
        content = "向量數據庫 PgVector 是 PostgreSQL 的擴展，提供高效的向量相似度搜索功能。它支援 HNSW 索引，適合大規模向量檢索場景。"
        metadata = @{
            title = "PgVector 向量數據庫"
            source = "docs"
            category = "database"
        }
    },
    @{
        content = "今天天氣很好，適合出去散步。天空湛藍，陽光明媚，是個放鬆心情的好日子。"
        metadata = @{
            title = "天氣日記"
            source = "blog"
            category = "life"
        }
    },
    @{
        content = "Python 是一種廣泛使用的編程語言，特別適合數據科學和機器學習。它有豐富的函式庫生態系統。"
        metadata = @{
            title = "Python 編程語言"
            source = "docs"
            category = "programming"
        }
    }
)

try {
    $body = @{ documents = $documents } | ConvertTo-Json -Depth 10
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/documents/batch" -Method Post -Body $body -ContentType "application/json"
    Write-Host "✓ 成功載入 $($documents.Count) 個文檔" -ForegroundColor Green
    Write-Host "  - 文檔 IDs: $($response.documentIds -join ', ')" -ForegroundColor Gray
} catch {
    Write-Host "✗ 載入文檔失敗: $_" -ForegroundColor Red
}
Write-Host ""

# 等待索引完成
Write-Host "等待向量索引完成..." -ForegroundColor Yellow
Start-Sleep -Seconds 3
Write-Host ""

# 測試 3: 執行 RAG 查詢（使用 Voyage Rerank-2.5）
Write-Host "測試 3: 執行 RAG 查詢（自動使用 Voyage rerank-2.5）" -ForegroundColor Yellow
$query = "Spring AI 的 reranking 功能如何使用？"

Write-Host "查詢: $query" -ForegroundColor Cyan
Write-Host ""

try {
    $body = @{
        query = $query
        options = @{
            enableReranking = $true
            finalTopK = 3
            maxContextLength = 2000
        }
    } | ConvertTo-Json -Depth 10

    $startTime = Get-Date
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/rag/query" -Method Post -Body $body -ContentType "application/json"
    $duration = ((Get-Date) - $startTime).TotalMilliseconds

    Write-Host "✓ 查詢成功 (耗時: ${duration}ms)" -ForegroundColor Green
    Write-Host ""
    Write-Host "=== 檢索到的文檔（經過 Voyage rerank-2.5 重排序）===" -ForegroundColor Cyan

    for ($i = 0; $i -lt $response.documents.Count; $i++) {
        $doc = $response.documents[$i]
        Write-Host "  排名 $($i + 1):" -ForegroundColor Yellow
        Write-Host "    - 分數: $($doc.score)" -ForegroundColor White
        Write-Host "    - 標題: $($doc.metadata.title)" -ForegroundColor White
        Write-Host "    - 內容: $($doc.content.Substring(0, [Math]::Min(80, $doc.content.Length)))..." -ForegroundColor Gray
        Write-Host ""
    }

    Write-Host "=== AI 生成回答 ===" -ForegroundColor Cyan
    Write-Host $response.answer -ForegroundColor White
    Write-Host ""

    Write-Host "=== 統計信息 ===" -ForegroundColor Cyan
    Write-Host "  - 處理時間: ${duration}ms" -ForegroundColor White
    Write-Host "  - 檢索文檔數: $($response.documents.Count)" -ForegroundColor White
    Write-Host "  - Reranking 模型: Voyage rerank-2.5" -ForegroundColor White

} catch {
    Write-Host "✗ 查詢失敗: $_" -ForegroundColor Red
    Write-Host "錯誤詳情: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 測試 4: 比較不同查詢的 reranking 效果
Write-Host "測試 4: 測試中文查詢的 Reranking 效果" -ForegroundColor Yellow
$query2 = "向量數據庫是什麼？"

Write-Host "查詢: $query2" -ForegroundColor Cyan

try {
    $body = @{
        query = $query2
        options = @{
            enableReranking = $true
            finalTopK = 3
        }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$BaseUrl/api/rag/query" -Method Post -Body $body -ContentType "application/json"

    Write-Host "✓ 查詢成功" -ForegroundColor Green
    Write-Host ""
    Write-Host "前3個最相關文檔:" -ForegroundColor Cyan

    for ($i = 0; $i -lt [Math]::Min(3, $response.documents.Count); $i++) {
        $doc = $response.documents[$i]
        Write-Host "  $($i + 1). $($doc.metadata.title) (分數: $($doc.score))" -ForegroundColor White
    }

} catch {
    Write-Host "✗ 查詢失敗: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "=================================" -ForegroundColor Cyan
Write-Host "測試完成！" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Cyan
