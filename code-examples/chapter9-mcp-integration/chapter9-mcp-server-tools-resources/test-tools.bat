@echo off
REM 測試 MCP Server 工具

echo ========================================
echo   設定 Java 21 環境
echo ========================================

set "JAVA_HOME=D:\java\jdk-21"
set "Path=D:\java\jdk-21\bin;%Path%"

echo JAVA_HOME=%JAVA_HOME%
echo.

cd /d "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-server-tools-resources"

echo ========================================
echo   執行工具測試
echo ========================================
echo.

D:\apache-maven-3.9.11\bin\mvn.cmd test -Dtest=ToolProvidersTest

pause
