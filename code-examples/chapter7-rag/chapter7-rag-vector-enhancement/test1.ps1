Continue = "Continue"
 = "http://localhost:8080"
 = "user:15530009-77a9-406d-b309-134da2d318c5"
 = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes())
 = @{
    "Authorization" = "Basic "
    "Content-Type" = "application/json"
}

Write-Host "================================" -ForegroundColor Cyan
Write-Host "測試 1: 健康檢查" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
try {
     = Invoke-RestMethod -Uri "/api/rag/health" -Method GET -Headers 
    Write-Host "Success\!" -ForegroundColor Green
     | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Error: " -ForegroundColor Red
}
