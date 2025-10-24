# Spring AI Chapter 4 - Run Application (PowerShell)

Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "Spring AI Chapter 4 - Run Application" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 設定 Java 21 環境
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

Write-Host "檢查 Java 版本..." -ForegroundColor Yellow
java -version
Write-Host ""

# 讀取 .env 檔案並設定環境變數
$envFile = "E:\Spring_AI_BOOK\.env"
if (Test-Path $envFile) {
    Write-Host "正在讀取 API Keys..." -ForegroundColor Yellow

    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([A-Z_]+)=(.+)$') {
            $key = $matches[1]
            $value = $matches[2]
            Set-Item -Path "env:$key" -Value $value
            Write-Host "  [OK] 已載入: $key" -ForegroundColor Green
        }
    }

    Write-Host "[OK] API Keys 載入完成" -ForegroundColor Green
} else {
    Write-Host "[WARNING] .env 檔案不存在!" -ForegroundColor Red
    Write-Host "請設定 OPENAI_API_KEY 環境變數" -ForegroundColor Red
    Read-Host "按 Enter 鍵退出"
    exit 1
}

Write-Host ""
Write-Host "正在啟動應用程式..." -ForegroundColor Yellow
Write-Host "端口: 8080" -ForegroundColor Yellow
Write-Host "按 Ctrl+C 停止應用程式" -ForegroundColor Yellow
Write-Host ""

# 執行 Maven Spring Boot
mvn spring-boot:run
