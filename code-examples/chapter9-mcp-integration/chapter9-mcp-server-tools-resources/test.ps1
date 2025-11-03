# 設定環境變數
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

# 切換到專案目錄
Set-Location "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-server-tools-resources"

Write-Host "========================================" -ForegroundColor Green
Write-Host "  測試 MCP Server 專案" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 顯示 Java 版本
Write-Host "Java 版本:" -ForegroundColor Cyan
java -version
Write-Host ""

# 執行 Spring Boot 應用（限制輸出前100行）
Write-Host "啟動 MCP Server..." -ForegroundColor Cyan
Write-Host ""

$process = Start-Process -FilePath "D:\apache-maven-3.9.11\bin\mvn.cmd" `
    -ArgumentList "spring-boot:run" `
    -NoNewWindow `
    -PassThru `
    -RedirectStandardOutput "test-output.log" `
    -RedirectStandardError "test-error.log"

# 等待啟動
Write-Host "等待應用啟動..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 顯示輸出
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  應用輸出（前60行）" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if (Test-Path "test-output.log") {
    Get-Content "test-output.log" | Select-Object -First 60
}

# 停止進程
Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  停止應用..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

Stop-Process -Id $process.Id -Force

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  測試完成" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
