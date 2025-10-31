# 設置 Java 21 環境
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

# 設置 OpenAI API Key (從.env文件讀取)
$envFile = "E:\Spring_AI_BOOK\.env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^OPENAI_API_KEY=(.+)$') {
            $env:OPENAI_API_KEY = $matches[1]
            Write-Host "已設定 OPENAI_API_KEY"
        }
    }
}

# 設置 Neo4j 連接資訊
$env:NEO4J_URI = "bolt://localhost:7687"
$env:NEO4J_USERNAME = "neo4j"
$env:NEO4J_PASSWORD = "test1234"
$env:NEO4J_DATABASE = "neo4j"

Write-Host "`n環境變數已設定:"
Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "OPENAI_API_KEY: $($env:OPENAI_API_KEY.Substring(0,20))..."
Write-Host "NEO4J_URI: $env:NEO4J_URI"
Write-Host "`n啟動 Spring Boot 應用程式...`n"

# 啟動應用程式
mvn spring-boot:run
