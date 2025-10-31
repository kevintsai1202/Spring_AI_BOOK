# Chapter 6 Advanced Memory API 測試腳本
# 使用方式: .\test-api.ps1

$baseUrl = "http://localhost:8086"

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Chapter 6 Advanced Memory API 測試" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# 測試 1: 健康檢查
Write-Host "測試 1: 健康檢查" -ForegroundColor Yellow
Write-Host "GET $baseUrl/api/advanced/health" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/advanced/health" -Method Get
    Write-Host "回應: $response" -ForegroundColor Green
} catch {
    Write-Host "錯誤: $_" -ForegroundColor Red
}
Write-Host ""

# 測試 2: 基本聊天 (HYBRID 策略)
Write-Host "測試 2: 基本聊天 (HYBRID 策略)" -ForegroundColor Yellow
Write-Host "POST $baseUrl/api/advanced/chat" -ForegroundColor Gray
$chatRequest1 = @{
    conversationId = "test-user-001"
    message = "你好,我想了解一下混合記憶系統是如何工作的?"
    strategy = "HYBRID"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/advanced/chat" -Method Post -Body $chatRequest1 -ContentType "application/json"
    Write-Host "回應: " -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 3) -ForegroundColor Green
} catch {
    Write-Host "錯誤: $_" -ForegroundColor Red
}
Write-Host ""

# 測試 3: 繼續對話
Write-Host "測試 3: 繼續對話" -ForegroundColor Yellow
Write-Host "POST $baseUrl/api/advanced/chat" -ForegroundColor Gray
$chatRequest2 = @{
    conversationId = "test-user-001"
    message = "剛才你提到了什麼?"
    strategy = "SHORT_TERM_ONLY"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/advanced/chat" -Method Post -Body $chatRequest2 -ContentType "application/json"
    Write-Host "回應: " -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 3) -ForegroundColor Green
} catch {
    Write-Host "錯誤: $_" -ForegroundColor Red
}
Write-Host ""

# 測試 4: 生成對話摘要
Write-Host "測試 4: 生成對話摘要" -ForegroundColor Yellow
Write-Host "POST $baseUrl/api/advanced/summarize/test-user-001" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/advanced/summarize/test-user-001" -Method Post
    Write-Host "回應: " -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 3) -ForegroundColor Green
} catch {
    Write-Host "錯誤: $_" -ForegroundColor Red
}
Write-Host ""

# 測試 5: 測試不同記憶策略
Write-Host "測試 5: 測試長期記憶策略" -ForegroundColor Yellow
Write-Host "POST $baseUrl/api/advanced/chat" -ForegroundColor Gray
$chatRequest3 = @{
    conversationId = "test-user-001"
    message = "你還記得之前我們討論過什麼嗎?"
    strategy = "LONG_TERM_ONLY"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/advanced/chat" -Method Post -Body $chatRequest3 -ContentType "application/json"
    Write-Host "回應: " -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 3) -ForegroundColor Green
} catch {
    Write-Host "錯誤: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "測試完成!" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
