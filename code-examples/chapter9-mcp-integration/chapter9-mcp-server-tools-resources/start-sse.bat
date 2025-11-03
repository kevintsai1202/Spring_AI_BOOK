@echo off
chcp 65001 > nul
REM ========================================
REM  MCP Server - SSE 模式啟動腳本
REM ========================================

echo ========================================
echo   MCP Server - SSE 模式
echo ========================================
echo.

REM 設定 Java 環境
set JAVA_HOME=D:\java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

REM 設定 Maven 路徑
set MAVEN_HOME=D:\apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%

REM 顯示 Java 版本
echo 檢查 Java 版本...
java -version
echo.

echo ========================================
echo   啟動 MCP Server (SSE 模式)
echo ========================================
echo.
echo 提示: SSE 模式透過 HTTP Server-Sent Events 進行通訊
echo       適用於遠端服務、HTTP 整合、生產環境
echo.
echo Server 端點: http://localhost:8080/mcp/message
echo.

REM 使用 Maven 啟動（SSE 模式使用默認配置）
call mvn.cmd spring-boot:run

echo.
echo ========================================
echo   MCP Server 已停止
echo ========================================
pause
