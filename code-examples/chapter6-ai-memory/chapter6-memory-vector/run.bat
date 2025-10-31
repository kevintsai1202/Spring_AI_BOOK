@echo off
REM Windows 批次腳本 - 啟動 Spring AI 向量記憶系統

echo ========================================
echo Spring AI Vector Memory System
echo ========================================
echo.

REM 設定 Java 21 環境
echo [1/4] 設定 Java 21 環境...
set JAVA_HOME=D:\java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

java -version
if errorlevel 1 (
    echo 錯誤: Java 21 未正確安裝
    pause
    exit /b 1
)
echo.

REM 檢查環境變數
echo [2/4] 檢查環境變數...
if not defined OPENAI_API_KEY (
    echo 警告: OPENAI_API_KEY 未設置
    echo 請設置環境變數或在 application.yml 中配置
    echo.
    set /p OPENAI_API_KEY=請輸入 OpenAI API Key:
    if not defined OPENAI_API_KEY (
        echo 錯誤: 必須提供 OpenAI API Key
        pause
        exit /b 1
    )
)
echo.

REM 啟動 Neo4j (如果使用 Docker)
echo [3/4] 檢查 Neo4j 狀態...
docker ps | findstr neo4j-vector >nul 2>&1
if errorlevel 1 (
    echo Neo4j 未運行, 正在啟動...
    docker-compose up -d neo4j
    echo 等待 Neo4j 啟動 (30秒)...
    timeout /t 30 /nobreak >nul
) else (
    echo Neo4j 已運行
)
echo.

REM 運行應用
echo [4/4] 啟動應用...
echo.
echo ========================================
echo 應用啟動中...
echo ========================================
echo.

call mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

pause
