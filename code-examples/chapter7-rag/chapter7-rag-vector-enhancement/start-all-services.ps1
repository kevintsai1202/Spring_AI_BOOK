# 啟動所有服務（Docker + Spring Boot）

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 啟動 Chapter 7 RAG 向量增強系統" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 啟動 Docker 服務
Write-Host "[1/3] 啟動 Docker 服務 (Neo4j, PostgreSQL, Redis, Prometheus, Grafana)..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker 服務啟動失敗！" -ForegroundColor Red
    exit 1
}

Write-Host "Docker 服務已啟動！" -ForegroundColor Green
Write-Host ""

# 2. 等待服務就緒
Write-Host "[2/3] 等待服務就緒（約 30 秒）..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "檢查服務狀態..." -ForegroundColor Yellow
docker-compose ps

Write-Host ""

# 3. 啟動 Spring Boot 應用
Write-Host "[3/3] 啟動 Spring Boot 應用..." -ForegroundColor Yellow
Write-Host ""

# 設定環境變數
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

# 檢查 OpenAI API Key
if (-not $env:OPENAI_API_KEY) {
    Write-Host "警告: OPENAI_API_KEY 環境變數未設定！" -ForegroundColor Red
    Write-Host "請設定 OpenAI API Key: `$env:OPENAI_API_KEY='your-api-key'" -ForegroundColor Yellow
    Write-Host ""
    $response = Read-Host "是否繼續？(y/n)"
    if ($response -ne 'y') {
        exit 1
    }
}

Write-Host "使用 Maven 啟動應用程式..." -ForegroundColor Yellow
mvn spring-boot:run

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 服務啟動完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
