@echo off
chcp 65001 > nul
REM ========================================
REM  MCP Server - STDIO 模式啟動腳本
REM ========================================

echo ========================================
echo   MCP Server - STDIO 模式
echo ========================================
echo.

REM 設定 Java 環境
set JAVA_HOME=D:\java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

REM 顯示 Java 版本
echo 檢查 Java 版本...
java -version
echo.

REM 檢查是否已編譯
if not exist "target\chapter9-mcp-server-tools-resources-1.0.0-SNAPSHOT.jar" (
    echo [警告] 未找到編譯後的 JAR 檔案，正在編譯...
    echo.
    call D:\apache-maven-3.9.11\bin\mvn.cmd clean package -DskipTests
    echo.
)

echo ========================================
echo   啟動 MCP Server (STDIO 模式)
echo ========================================
echo.
echo 提示: STDIO 模式透過標準輸入/輸出進行通訊
echo       適用於本地進程、CLI 工具、開發測試
echo.

REM 啟動應用（STDIO 模式）
java -Dspring.ai.mcp.server.stdio=true ^
     -Dspring.main.web-application-type=none ^
     -Dspring.main.banner-mode=off ^
     -jar target\chapter9-mcp-server-tools-resources-1.0.0-SNAPSHOT.jar

echo.
echo ========================================
echo   MCP Server 已停止
echo ========================================
pause
