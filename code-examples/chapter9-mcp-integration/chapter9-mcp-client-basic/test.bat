@echo off
cd /d "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-client-basic"

set JAVA_HOME=D:\java\jdk-21
set PATH=D:\java\jdk-21\bin;D:\apache-maven-3.9.11\bin;%PATH%

REM 檢查 API Key 是否已設置
if "%OPENAI_API_KEY%"=="" (
    echo 警告: OPENAI_API_KEY 環境變數未設置
    echo 請先設置: set OPENAI_API_KEY=your-api-key
    exit /b 1
)

echo ========================================
echo Building project...
echo ========================================
call mvn.cmd clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    exit /b 1
)

echo.
echo ========================================
echo Starting application...
echo ========================================
echo.
java -jar target\chapter9-mcp-client-basic-1.0.0-SNAPSHOT.jar
