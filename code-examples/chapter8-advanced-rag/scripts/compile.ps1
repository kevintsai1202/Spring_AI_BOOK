# Advanced RAG 專案編譯腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Advanced RAG 專案編譯" -ForegroundColor Cyan
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
Write-Host "開始編譯..." -ForegroundColor Yellow

# 編譯專案
mvn clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "編譯成功！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "編譯失敗！請檢查錯誤信息。" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    exit 1
}
