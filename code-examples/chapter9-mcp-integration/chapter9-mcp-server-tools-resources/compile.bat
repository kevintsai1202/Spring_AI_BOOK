@echo off
chcp 65001 > nul
REM ========================================
REM  編譯腳本
REM ========================================

echo ========================================
echo   編譯 MCP Server 專案
echo ========================================
echo.

REM 設定 Java 環境
set JAVA_HOME=D:\java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

REM 設定 Maven 路徑
set MAVEN_HOME=D:\apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%

REM 顯示環境資訊
echo Java 版本:
java -version
echo.

echo Maven 版本:
call mvn.cmd -version
echo.

echo ========================================
echo   開始編譯...
echo ========================================
echo.

REM 編譯專案
call mvn.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   編譯成功!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   編譯失敗! 錯誤代碼: %ERRORLEVEL%
    echo ========================================
)

echo.
pause
