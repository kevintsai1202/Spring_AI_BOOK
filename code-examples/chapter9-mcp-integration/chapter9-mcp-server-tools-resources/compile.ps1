# 設定環境變數
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

# 切換到專案目錄
Set-Location "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-server-tools-resources"

Write-Host "========================================" -ForegroundColor Green
Write-Host "  MCP Server 編譯和測試" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 顯示 Java 版本
Write-Host "Java 版本:" -ForegroundColor Cyan
java -version
Write-Host ""

# 執行編譯
Write-Host "執行 Maven 編譯..." -ForegroundColor Cyan
& "D:\apache-maven-3.9.11\bin\mvn.cmd" clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  編譯成功!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green

    # 啟動應用測試
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  啟動 MCP Server 進行測試..." -ForegroundColor Cyan
    Write-Host "  端點: http://localhost:8080/mcp/message" -ForegroundColor Cyan
    Write-Host "  (按 Ctrl+C 停止)" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""

    & "D:\apache-maven-3.9.11\bin\mvn.cmd" spring-boot:run

} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  編譯失敗! 錯誤代碼: $LASTEXITCODE" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    exit $LASTEXITCODE
}
