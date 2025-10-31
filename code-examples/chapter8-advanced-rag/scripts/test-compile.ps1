# Advanced RAG 測試編譯腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "編譯測試代碼" -ForegroundColor Cyan
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

# 編譯測試
mvn test-compile

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "測試代碼編譯成功！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "測試代碼編譯失敗！" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    exit 1
}
