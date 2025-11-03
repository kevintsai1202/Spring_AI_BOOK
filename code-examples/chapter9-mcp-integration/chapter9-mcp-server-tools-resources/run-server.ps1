# MCP Server 啟動腳本
# 設定環境變數
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

# 進入專案目錄
Set-Location "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-server-tools-resources"

Write-Host "========================================" -ForegroundColor Green
Write-Host "  MCP Server 啟動" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 顯示 Java 版本
Write-Host "Java 版本:" -ForegroundColor Cyan
java -version
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  啟動 Spring Boot 應用..." -ForegroundColor Cyan
Write-Host "  端點: http://localhost:8080/mcp/message" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 執行 Maven
& "D:\apache-maven-3.9.11\bin\mvn.cmd" spring-boot:run
