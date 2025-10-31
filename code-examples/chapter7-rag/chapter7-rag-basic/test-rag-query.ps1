# 測試 RAG 查詢功能

$url = "http://localhost:8080/api/rag/query"

$body = @{
    question = "請介紹 Spring Framework 的核心功能"
    topK = 3
    similarityThreshold = 0.5
} | ConvertTo-Json

Write-Host "=== RAG 查詢測試 ===" -ForegroundColor Cyan
Write-Host "問題: 請介紹 Spring Framework 的核心功能" -ForegroundColor Yellow
Write-Host ""
Write-Host "發送查詢請求..." -ForegroundColor Gray

try {
    $response = Invoke-RestMethod -Uri $url -Method Post -ContentType "application/json" -Body $body -TimeoutSec 120

    Write-Host "✓ 查詢成功!" -ForegroundColor Green
    Write-Host ""
    Write-Host "問題: $($response.question)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "答案:" -ForegroundColor Yellow
    Write-Host $response.answer -ForegroundColor White
    Write-Host ""
    Write-Host "處理時間: $($response.processingTimeMs) ms" -ForegroundColor Gray
    Write-Host "來源文檔數: $($response.sources.Count)" -ForegroundColor Gray

    if ($response.sources -and $response.sources.Count -gt 0) {
        Write-Host ""
        Write-Host "來源文檔:" -ForegroundColor Cyan
        $response.sources | ForEach-Object {
            Write-Host "  - $($_.title)" -ForegroundColor White
            Write-Host "    相關性: $([math]::Round($_.relevanceScore, 3))" -ForegroundColor Gray
        }
    }

} catch {
    Write-Host "× 查詢失敗!" -ForegroundColor Red
    Write-Host "錯誤: $($_.Exception.Message)" -ForegroundColor Red
}
