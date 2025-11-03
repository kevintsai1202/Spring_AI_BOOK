@echo off
REM ========================================
REM 設定 Java 21 環境並啟動 MCP Server
REM ========================================

echo ========================================
echo   設定 Java 21 環境變數
echo ========================================

set "JAVA_HOME=D:\java\jdk-21"
set "Path=D:\java\jdk-21\bin;%Path%"
set "MAVEN_OPTS=-Xmx512m"

echo.
echo JAVA_HOME=%JAVA_HOME%
echo.

echo ========================================
echo   檢查 Java 版本
echo ========================================

java -version
echo.

echo ========================================
echo   啟動 MCP Server (SSE 模式)
echo ========================================
echo 端點: http://localhost:8080/mcp/message
echo 按 Ctrl+C 停止服務
echo.

D:\apache-maven-3.9.11\bin\mvn.cmd spring-boot:run
