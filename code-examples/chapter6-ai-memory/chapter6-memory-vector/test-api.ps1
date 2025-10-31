# PowerShell 測試腳本
# 用於測試 Spring AI 向量記憶系統的 API

$baseUrl = "http://localhost:8080"
$conversationId = "test-conv-$(Get-Date -Format 'yyyyMMddHHmmss')"

Write-Host "=== Spring AI 向量記憶系統 API 測試 ===" -ForegroundColor Cyan
Write-Host "對話 ID: $conversationId" -ForegroundColor Green
Write-Host ""

# 1. 健康檢查
Write-Host "1. 健康檢查..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/vector-chat/health" -Method Get
    Write-Host "✓ 服務運行正常" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ 服務未運行" -ForegroundColor Red
    exit 1
}

# 2. 混合記憶對話 - 第一輪
Write-Host "2. 混合記憶對話 - 第一輪..." -ForegroundColor Yellow
$request1 = @{
    message = "你好!我想了解 Spring AI 的向量記憶功能"
} | ConvertTo-Json

try {
    $response1 = Invoke-RestMethod -Uri "$baseUrl/api/vector-chat/$conversationId/hybrid" `
        -Method Post `
        -ContentType "application/json" `
        -Body $request1

    Write-Host "✓ 對話成功" -ForegroundColor Green
    Write-Host "AI 回應: $($response1.response.Substring(0, [Math]::Min(100, $response1.response.Length)))..." -ForegroundColor White
    Write-Host "短期記憶數: $($response1.shortTermCount)" -ForegroundColor White
    Write-Host "長期記憶數: $($response1.longTermCount)" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "✗ 對話失敗: $($_.Exception.Message)" -ForegroundColor Red
}

# 等待一秒
Start-Sleep -Seconds 1

# 3. 混合記憶對話 - 第二輪
Write-Host "3. 混合記憶對話 - 第二輪..." -ForegroundColor Yellow
$request2 = @{
    message = "那麼 Neo4j 向量資料庫有什麼優勢?"
} | ConvertTo-Json

try {
    $response2 = Invoke-RestMethod -Uri "$baseUrl/api/vector-chat/$conversationId/hybrid" `
        -Method Post `
        -ContentType "application/json" `
        -Body $request2

    Write-Host "✓ 對話成功" -ForegroundColor Green
    Write-Host "AI 回應: $($response2.response.Substring(0, [Math]::Min(100, $response2.response.Length)))..." -ForegroundColor White
    Write-Host "短期記憶數: $($response2.shortTermCount)" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "✗ 對話失敗" -ForegroundColor Red
}

# 等待一秒
Start-Sleep -Seconds 1

# 4. 智能策略對話
Write-Host "4. 智能策略對話 (測試關鍵詞識別)..." -ForegroundColor Yellow
$request3 = @{
    message = "剛才我們聊了什麼?"
} | ConvertTo-Json

try {
    $response3 = Invoke-RestMethod -Uri "$baseUrl/api/vector-chat/$conversationId/smart" `
        -Method Post `
        -ContentType "application/json" `
        -Body $request3

    Write-Host "✓ 對話成功" -ForegroundColor Green
    Write-Host "使用策略: $($response3.strategy)" -ForegroundColor Cyan
    Write-Host "AI 回應: $($response3.response.Substring(0, [Math]::Min(100, $response3.response.Length)))..." -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "✗ 對話失敗" -ForegroundColor Red
}

# 5. 手動同步記憶
Write-Host "5. 手動同步記憶到向量資料庫..." -ForegroundColor Yellow
try {
    $syncResponse = Invoke-RestMethod -Uri "$baseUrl/api/memory/sync/$conversationId" -Method Post

    if ($syncResponse.success) {
        Write-Host "✓ 同步成功" -ForegroundColor Green
        Write-Host "同步時間: $($syncResponse.syncTime)" -ForegroundColor White
    } else {
        Write-Host "✗ 同步失敗" -ForegroundColor Red
    }
    Write-Host ""
} catch {
    Write-Host "✗ 同步失敗: $($_.Exception.Message)" -ForegroundColor Red
}

# 等待2秒讓同步完成
Start-Sleep -Seconds 2

# 6. 獲取記憶分析
Write-Host "6. 獲取記憶分析..." -ForegroundColor Yellow
try {
    $analytics = Invoke-RestMethod -Uri "$baseUrl/api/memory/analytics/$conversationId" -Method Get

    Write-Host "✓ 分析成功" -ForegroundColor Green
    Write-Host "對話 ID: $($analytics.conversationId)" -ForegroundColor White
    Write-Host "短期記憶數: $($analytics.shortTermCount)" -ForegroundColor White
    Write-Host "長期記憶數: $($analytics.longTermCount)" -ForegroundColor White
    Write-Host "總字符數: $($analytics.totalCharacterCount)" -ForegroundColor White
    Write-Host "健康分數: $($analytics.healthScore)/100" -ForegroundColor White
    Write-Host "同步狀態: $($analytics.syncStatus)" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "✗ 分析失敗" -ForegroundColor Red
}

# 7. 檢索記憶
Write-Host "7. 檢索記憶..." -ForegroundColor Yellow
try {
    $retrievalResponse = Invoke-RestMethod -Uri "$baseUrl/api/memory/retrieve/$conversationId`?query=Spring AI" -Method Post

    Write-Host "✓ 檢索成功" -ForegroundColor Green
    Write-Host "檢索查詢: $($retrievalResponse.query)" -ForegroundColor White
    Write-Host "短期記憶: $($retrievalResponse.shortTermMemories.Count) 條" -ForegroundColor White
    Write-Host "長期記憶: $($retrievalResponse.longTermMemories.Count) 條" -ForegroundColor White
    Write-Host "檢索耗時: $($retrievalResponse.retrievalTimeMs) ms" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "✗ 檢索失敗" -ForegroundColor Red
}

# 8. 獲取活躍對話列表
Write-Host "8. 獲取活躍對話列表..." -ForegroundColor Yellow
try {
    $conversations = Invoke-RestMethod -Uri "$baseUrl/api/memory/conversations" -Method Get

    Write-Host "✓ 獲取成功" -ForegroundColor Green
    Write-Host "活躍對話數: $($conversations.Count)" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "✗ 獲取失敗" -ForegroundColor Red
}

# 9. 清理測試數據 (可選)
Write-Host "9. 清理測試數據? (Y/N)" -ForegroundColor Yellow
$cleanup = Read-Host

if ($cleanup -eq "Y" -or $cleanup -eq "y") {
    try {
        $deleteResponse = Invoke-RestMethod -Uri "$baseUrl/api/memory/$conversationId" -Method Delete

        if ($deleteResponse.success) {
            Write-Host "✓ 清理成功" -ForegroundColor Green
        } else {
            Write-Host "✗ 清理失敗" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ 清理失敗" -ForegroundColor Red
    }
} else {
    Write-Host "保留測試數據" -ForegroundColor White
}

Write-Host ""
Write-Host "=== 測試完成 ===" -ForegroundColor Cyan
Write-Host "測試對話 ID: $conversationId" -ForegroundColor Green
