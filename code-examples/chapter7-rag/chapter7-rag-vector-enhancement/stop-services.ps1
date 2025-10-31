# 停止所有服務的 PowerShell 腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "停止 RAG Vector Enhancement 服務" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 進入專案目錄
$ProjectDir = "E:\Spring_AI_BOOK\code-examples\chapter7-rag\chapter7-rag-vector-enhancement"
Set-Location $ProjectDir

# 停止 Docker Compose 服務
Write-Host "停止 Docker 服務..." -ForegroundColor Yellow
docker-compose down

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Docker 服務已停止" -ForegroundColor Green
} else {
    Write-Host "❌ 停止服務時發生錯誤" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "如果要完全清除數據卷，請執行：" -ForegroundColor Yellow
Write-Host "  docker-compose down -v" -ForegroundColor White
