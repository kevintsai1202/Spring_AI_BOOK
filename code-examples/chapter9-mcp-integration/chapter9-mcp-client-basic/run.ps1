# 設置工作目錄
Set-Location "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-client-basic"

# 設置 Java 21 環境
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;D:\apache-maven-3.9.11\bin;$env:Path"

# 檢查 API Key 是否已設置
if (-not $env:OPENAI_API_KEY) {
    Write-Host "警告: OPENAI_API_KEY 環境變數未設置" -ForegroundColor Yellow
    Write-Host "請先設置: `$env:OPENAI_API_KEY = 'your-api-key'" -ForegroundColor Yellow
    exit 1
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "  Spring AI MCP Client 測試" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Java 版本:" -ForegroundColor Cyan
& java -version
Write-Host ""

Write-Host "打包應用..." -ForegroundColor Cyan
& mvn.cmd clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n打包成功！啟動應用...`n" -ForegroundColor Green

    # 運行應用
    & java -jar target/chapter9-mcp-client-basic-1.0.0-SNAPSHOT.jar
} else {
    Write-Host "`n打包失敗！" -ForegroundColor Red
}
