# PowerShell 測試腳本
# 測試 Spring AI 向量記憶系統 API

$baseUrl = "http://localhost:8080"
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$outputFile = "test-results-$timestamp.txt"

Write-Host "=== Chapter 6 Memory Vector 測試報告 ===" -ForegroundColor Cyan | Tee-Object -FilePath $outputFile
Write-Host "測試時間: $(Get-Date)" | Tee-Object -FilePath $outputFile -Append
Write-Host "" | Tee-Object -FilePath $outputFile -Append

# 測試1: 健康檢查
Write-Host "### 測試 1: 健康檢查" -ForegroundColor Yellow | Tee-Object -FilePath $outputFile -Append
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/chat/health" -Method Get
    Write-Host "✓ 健康檢查成功: $response" -ForegroundColor Green | Tee-Object -FilePath $outputFile -Append
} catch {
    Write-Host "✗ 健康檢查失敗: $_" -ForegroundColor Red | Tee-Object -FilePath $outputFile -Append
}
Write-Host "" | Tee-Object -FilePath $outputFile -Append

# 測試2: 混合記憶對話 - 第一輪
Write-Host "### 測試 2: 混合記憶對話 - 第一輪" -ForegroundColor Yellow | Tee-Object -FilePath $outputFile -Append
$body1 = @{message = "你好！我叫張三,我是一名軟體工程師"} | ConvertTo-Json
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/chat/test-001/hybrid" -Method Post -ContentType "application/json" -Body $body1
    Write-Host "✓ 對話成功" -ForegroundColor Green | Tee-Object -FilePath $outputFile -Append
    Write-Host "  會話ID: $($response.conversationId)" | Tee-Object -FilePath $outputFile -Append
    Write-Host "  策略: $($response.strategy)" | Tee-Object -FilePath $outputFile -Append
    Write-Host "  短期記憶數: $($response.shortTermCount)" | Tee-Object -FilePath $outputFile -Append
    $responseText = $response.response
    if ($responseText.Length -gt 100) {
        $responseText = $responseText.Substring(0, 100) + "..."
    }
    Write-Host "  AI回應: $responseText" | Tee-Object -FilePath $outputFile -Append
} catch {
    Write-Host "✗ 對話失敗: $_" -ForegroundColor Red | Tee-Object -FilePath $outputFile -Append
}
Write-Host "" | Tee-Object -FilePath $outputFile -Append

Start-Sleep -Seconds 2

# 測試3: 混合記憶對話 - 第二輪 (測試記憶功能)
Write-Host "### 測試 3: 混合記憶對話 - 第二輪 (測試記憶)" -ForegroundColor Yellow | Tee-Object -FilePath $outputFile -Append
$body2 = @{message = "我剛才說我的名字是什麼？"} | ConvertTo-Json
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/chat/test-001/hybrid" -Method Post -ContentType "application/json" -Body $body2
    Write-Host "✓ 對話成功" -ForegroundColor Green | Tee-Object -FilePath $outputFile -Append
    Write-Host "  短期記憶數: $($response.shortTermCount)" | Tee-Object -FilePath $outputFile -Append
    $responseText = $response.response
    if ($responseText.Length -gt 150) {
        $responseText = $responseText.Substring(0, 150) + "..."
    }
    Write-Host "  AI回應: $responseText" | Tee-Object -FilePath $outputFile -Append

    if ($responseText -like "*張三*") {
        Write-Host "  ✓ 記憶功能正常 - AI成功記住了名字" -ForegroundColor Green | Tee-Object -FilePath $outputFile -Append
    } else {
        Write-Host "  ⚠ 記憶功能可能有問題 - AI未能正確回憶名字" -ForegroundColor Yellow | Tee-Object -FilePath $outputFile -Append
    }
} catch {
    Write-Host "✗ 對話失敗: $_" -ForegroundColor Red | Tee-Object -FilePath $outputFile -Append
}
Write-Host "" | Tee-Object -FilePath $outputFile -Append

# 測試4: 短期記憶對話
Write-Host "### 測試 4: 短期記憶對話" -ForegroundColor Yellow | Tee-Object -FilePath $outputFile -Append
$body3 = @{message = "我的職業是什麼？"} | ConvertTo-Json
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/chat/test-001/short-term" -Method Post -ContentType "application/json" -Body $body3
    Write-Host "✓ 對話成功" -ForegroundColor Green | Tee-Object -FilePath $outputFile -Append
    Write-Host "  策略: $($response.strategy)" | Tee-Object -FilePath $outputFile -Append
    $responseText = $response.response
    if ($responseText.Length -gt 100) {
        $responseText = $responseText.Substring(0, 100) + "..."
    }
    Write-Host "  AI回應: $responseText" | Tee-Object -FilePath $outputFile -Append
} catch {
    Write-Host "✗ 對話失敗: $_" -ForegroundColor Red | Tee-Object -FilePath $outputFile -Append
}
Write-Host "" | Tee-Object -FilePath $outputFile -Append

Write-Host "=== 測試完成 ===" -ForegroundColor Cyan | Tee-Object -FilePath $outputFile -Append
Write-Host "測試報告已儲存至: $outputFile" -ForegroundColor Green
Write-Host ""
Write-Host "查看完整報告: cat $outputFile" -ForegroundColor Cyan
