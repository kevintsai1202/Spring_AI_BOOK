# MCP Server Advanced 編譯腳本

# 切換到專案目錄
Set-Location -Path "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-server-advanced"

Write-Host "設定 Java 21 環境..." -ForegroundColor Green
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

Write-Host "Java 版本:" -ForegroundColor Cyan
& "D:\java\jdk-21\bin\java.exe" -version

Write-Host "`n開始編譯專案..." -ForegroundColor Green
D:\apache-maven-3.9.11\bin\mvn.cmd clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n編譯成功！" -ForegroundColor Green
} else {
    Write-Host "`n編譯失敗，請檢查錯誤訊息。" -ForegroundColor Red
}
