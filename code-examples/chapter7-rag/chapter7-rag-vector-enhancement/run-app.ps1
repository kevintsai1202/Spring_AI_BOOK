# 運行應用程式的 PowerShell 腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "啟動 RAG Vector Enhancement 應用程式" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 設定 Java 21 環境
Write-Host "設定 Java 21 環境..." -ForegroundColor Yellow
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

# 驗證 Java 版本
java -version
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Java 21 未正確安裝" -ForegroundColor Red
    exit 1
}

# 進入專案目錄
$ProjectDir = "E:\Spring_AI_BOOK\code-examples\chapter7-rag\chapter7-rag-vector-enhancement"
Set-Location $ProjectDir

# 檢查環境變數
Write-Host ""
Write-Host "檢查必要的環境變數..." -ForegroundColor Yellow

if (-not $env:OPENAI_API_KEY) {
    Write-Host "⚠️  警告：OPENAI_API_KEY 未設定" -ForegroundColor Yellow
    Write-Host "  某些 AI 功能可能無法使用" -ForegroundColor Gray
    Write-Host ""
    $response = Read-Host "是否繼續？(Y/N)"
    if ($response -ne "Y" -and $response -ne "y") {
        Write-Host "已取消" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "✅ OPENAI_API_KEY 已設定" -ForegroundColor Green
}

# 設定 Neo4j 密碼（與 docker-compose.yml 一致）
$env:NEO4J_PASSWORD = "test1234"

Write-Host ""
Write-Host "環境配置：" -ForegroundColor Cyan
Write-Host "  Neo4j URI:      bolt://localhost:7687" -ForegroundColor Gray
Write-Host "  Neo4j User:     neo4j" -ForegroundColor Gray
Write-Host "  資料存儲：      Neo4j (向量 + 元資料)" -ForegroundColor Gray

Write-Host ""
Write-Host "啟動應用程式..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 運行應用程式
mvn spring-boot:run -Dspring-boot.run.profiles=dev

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ 應用程式啟動失敗" -ForegroundColor Red
    exit 1
}
