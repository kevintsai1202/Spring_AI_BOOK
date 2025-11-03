Set-Location "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-client-basic"

$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;D:\apache-maven-3.9.11\bin;$env:Path"

# 檢查 API Key
if (-not $env:OPENAI_API_KEY) {
    Write-Host "警告: OPENAI_API_KEY 環境變數未設置" -ForegroundColor Yellow
    Write-Host "請先設置: `$env:OPENAI_API_KEY = 'your-api-key'" -ForegroundColor Yellow
    exit 1
}

Write-Host "Building project..." -ForegroundColor Green
& mvn.cmd clean package -DskipTests

Write-Host "Starting application..." -ForegroundColor Green
& java -jar target/chapter9-mcp-client-basic-1.0.0-SNAPSHOT.jar
