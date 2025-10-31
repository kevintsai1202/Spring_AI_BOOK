# Advanced RAG 系統設置和運行腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Advanced RAG 系統環境設置和啟動" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 設置 Java 21
Write-Host "[1/6] 設置 Java 21 環境..." -ForegroundColor Yellow
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

# 驗證 Java 版本
$javaVersion = java -version 2>&1 | Select-Object -First 1
Write-Host "Java 版本: $javaVersion" -ForegroundColor Green
Write-Host ""

# 2. 設置 API Keys
Write-Host "[2/6] 設置 OpenAI API Key..." -ForegroundColor Yellow
$envFile = "E:\Spring_AI_BOOK\.env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match "^OPENAI_API_KEY=(.+)$") {
            $env:OPENAI_API_KEY = $matches[1]
            Write-Host "OpenAI API Key 已設置" -ForegroundColor Green
        }
    }
} else {
    Write-Host "警告: 未找到 .env 文件，請手動設置 OPENAI_API_KEY" -ForegroundColor Red
}
Write-Host ""

# 3. 啟動 Docker 服務
Write-Host "[3/6] 啟動 Docker 服務（PgVector + Redis）..." -ForegroundColor Yellow
docker-compose up -d pgvector redis
Write-Host "等待數據庫啟動..." -ForegroundColor Yellow
Start-Sleep -Seconds 10
docker-compose ps
Write-Host ""

# 4. 清理並編譯專案
Write-Host "[4/6] 清理並編譯專案..." -ForegroundColor Yellow
mvn clean compile
if ($LASTEXITCODE -ne 0) {
    Write-Host "編譯失敗！請檢查錯誤信息。" -ForegroundColor Red
    exit 1
}
Write-Host "編譯成功！" -ForegroundColor Green
Write-Host ""

# 5. 運行應用
Write-Host "[5/6] 啟動 Advanced RAG 應用..." -ForegroundColor Yellow
Write-Host "應用將在 http://localhost:8080 啟動" -ForegroundColor Cyan
Write-Host "健康檢查: http://localhost:8080/actuator/health" -ForegroundColor Cyan
Write-Host "指標監控: http://localhost:8080/actuator/prometheus" -ForegroundColor Cyan
Write-Host ""
Write-Host "按 Ctrl+C 停止應用" -ForegroundColor Yellow
Write-Host ""

mvn spring-boot:run -Dspring-boot.run.profiles=dev
