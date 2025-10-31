# 啟動所有服務的 PowerShell 腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "啟動 RAG Vector Enhancement 服務" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 檢查 Docker 是否運行
Write-Host "檢查 Docker 狀態..." -ForegroundColor Yellow
docker --version
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker 未安裝或未運行，請先啟動 Docker Desktop" -ForegroundColor Red
    exit 1
}

# 進入專案目錄
$ProjectDir = "E:\Spring_AI_BOOK\code-examples\chapter7-rag\chapter7-rag-vector-enhancement"
Set-Location $ProjectDir

# 啟動 Docker Compose 服務
Write-Host ""
Write-Host "啟動 Docker 服務..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Docker 服務啟動成功！" -ForegroundColor Green
    Write-Host ""
    Write-Host "服務狀態：" -ForegroundColor Cyan
    docker-compose ps

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "服務訪問地址：" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Neo4j Browser:  http://localhost:7474" -ForegroundColor Green
    Write-Host "  用戶名: neo4j" -ForegroundColor Gray
    Write-Host "  密碼: test1234" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  功能：向量資料庫 + 元資料存儲" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "等待服務就緒（約 20 秒）..." -ForegroundColor Yellow
    Start-Sleep -Seconds 20

    Write-Host ""
    Write-Host "檢查服務健康狀態：" -ForegroundColor Cyan
    docker-compose ps

    Write-Host ""
    Write-Host "✅ 所有服務已啟動！可以運行應用程式了。" -ForegroundColor Green
    Write-Host ""
    Write-Host "啟動應用程式：" -ForegroundColor Yellow
    Write-Host "  .\run-app.ps1" -ForegroundColor White
    Write-Host ""
    Write-Host "停止服務：" -ForegroundColor Yellow
    Write-Host "  .\stop-services.ps1" -ForegroundColor White
} else {
    Write-Host "❌ Docker 服務啟動失敗" -ForegroundColor Red
    Write-Host ""
    Write-Host "查看日誌：" -ForegroundColor Yellow
    Write-Host "  docker-compose logs" -ForegroundColor White
    exit 1
}
