@echo off
echo ========================================
echo Spring AI Chapter 4 - Run Application
echo ========================================
echo.

REM 設定 Java 21 環境
set JAVA_HOME=D:\java\jdk-21
set Path=D:\java\jdk-21\bin;%Path%

REM 讀取 .env 檔案並設定環境變數
if exist "E:\Spring_AI_BOOK\.env" (
    echo 正在讀取 API Keys...
    for /f "usebackq tokens=1,2 delims==" %%a in ("E:\Spring_AI_BOOK\.env") do (
        if "%%a"=="OPENAI_API_KEY" set OPENAI_API_KEY=%%b
        if "%%a"=="GEMINI_API_KEY" set GEMINI_API_KEY=%%b
        if "%%a"=="GROQ_API_KEY" set GROQ_API_KEY=%%b
    )
    echo ✓ API Keys 已載入
) else (
    echo ⚠ 警告: .env 檔案不存在！
    echo 請設定 OPENAI_API_KEY 環境變數
    pause
    exit /b 1
)

echo.
echo 檢查 Java 版本...
java -version
echo.

echo 正在啟動應用程式...
echo 端口: 8080
echo.
call mvn spring-boot:run
