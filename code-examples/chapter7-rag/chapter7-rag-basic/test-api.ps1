# Spring AI RAG API 測試腳本

$baseUrl = "http://localhost:8080/api/rag"

Write-Host "=== Spring AI RAG 基礎系統 API 測試 ===" -ForegroundColor Cyan
Write-Host ""

# 測試 1: 健康檢查
Write-Host "[測試 1] 健康檢查端點" -ForegroundColor Yellow
Write-Host "GET $baseUrl/health" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/health" -Method Get
    Write-Host "✓ 健康檢查成功" -ForegroundColor Green
    Write-Host "  狀態: $($response.status)" -ForegroundColor White
    Write-Host "  服務: $($response.service)" -ForegroundColor White
    Write-Host "  版本: $($response.version)" -ForegroundColor White
} catch {
    Write-Host "✗ 健康檢查失敗: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 測試 2: 測試文本文件上傳
Write-Host "[測試 2] 上傳測試文檔" -ForegroundColor Yellow

# 創建測試文件
$testContent = @"
Spring AI 介紹

Spring AI 是一個用於構建 AI 驅動應用程序的框架。
它提供了與各種 AI 模型和向量資料庫的整合。

核心功能:
1. Chat Model 整合 (OpenAI, Azure OpenAI, 等)
2. Embedding Model 支援
3. Vector Store 整合 (Neo4j, Pinecone, 等)
4. RAG (Retrieval-Augmented Generation) 支援
5. Function Calling 功能

Spring AI 的優勢:
- Spring Boot 自動配置
- 統一的 API 抽象
- 豐富的生態系統整合
"@

$testFile = "test-document.txt"
$testContent | Out-File -FilePath $testFile -Encoding UTF8

Write-Host "POST $baseUrl/documents" -ForegroundColor Gray
try {
    $form = @{
        files = Get-Item -Path $testFile
    }
    $response = Invoke-RestMethod -Uri "$baseUrl/documents" -Method Post -Form $form
    Write-Host "✓ 文檔上傳成功" -ForegroundColor Green
    Write-Host "  訊息: $($response.message)" -ForegroundColor White
    Write-Host "  處理數量: $($response.documentsProcessed)" -ForegroundColor White
} catch {
    Write-Host "✗ 文檔上傳失敗: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  請確認已設定 OPENAI_API_KEY 環境變數" -ForegroundColor Yellow
}

# 清理測試文件
Remove-Item -Path $testFile -ErrorAction SilentlyContinue

Write-Host ""

# 測試 3: RAG 查詢 (需要有效的 OpenAI API Key)
Write-Host "[測試 3] RAG 查詢" -ForegroundColor Yellow
Write-Host "POST $baseUrl/query" -ForegroundColor Gray

$queryRequest = @{
    question = "什麼是 Spring AI?"
    topK = 5
    similarityThreshold = 0.7
} | ConvertTo-Json

Write-Host "查詢問題: 什麼是 Spring AI?" -ForegroundColor White

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/query" -Method Post `
        -ContentType "application/json" -Body $queryRequest

    Write-Host "✓ RAG 查詢成功" -ForegroundColor Green
    Write-Host "  問題: $($response.question)" -ForegroundColor White
    Write-Host "  答案: $($response.answer)" -ForegroundColor White
    Write-Host "  來源數量: $($response.sources.Count)" -ForegroundColor White
    Write-Host "  處理時間: $($response.processingTimeMs) ms" -ForegroundColor White

    if ($response.sources -and $response.sources.Count -gt 0) {
        Write-Host "`n  來源文檔:" -ForegroundColor Cyan
        $response.sources | ForEach-Object {
            Write-Host "    - $($_.title) (相關性: $($_.relevanceScore))" -ForegroundColor White
        }
    }
} catch {
    Write-Host "✗ RAG 查詢失敗: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  這是正常的，因為需要有效的 OpenAI API Key 才能執行 RAG 查詢" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== 測試完成 ===" -ForegroundColor Cyan
