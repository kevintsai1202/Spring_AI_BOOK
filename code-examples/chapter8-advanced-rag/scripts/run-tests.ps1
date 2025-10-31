# Advanced RAG 專案測試腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Advanced RAG 專案測試" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 設置 Java 21
Write-Host "設置 Java 21 環境..." -ForegroundColor Yellow
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;" + $env:Path

# 切換到專案根目錄
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir
Set-Location $projectRoot

Write-Host "專案目錄: $projectRoot" -ForegroundColor Green

# 驗證 Java 版本
Write-Host "當前 Java 版本:" -ForegroundColor Green
java -version

Write-Host ""
Write-Host "開始運行測試..." -ForegroundColor Yellow

# 運行測試
mvn test

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "測試全部通過！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "測試失敗！請檢查錯誤信息。" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    exit 1
}
