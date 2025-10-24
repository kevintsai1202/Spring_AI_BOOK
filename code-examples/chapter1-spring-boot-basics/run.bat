@echo off
echo ========================================
echo   Chapter 1 - Spring Boot Basics
echo   使用者管理系統
echo ========================================
echo.

REM 設定 Java 21 環境
set JAVA_HOME=D:\java\jdk-21
set Path=D:\java\jdk-21\bin;%Path%

echo 正在檢查 Java 版本...
java -version
echo.

echo 正在編譯專案...
call mvn clean compile
if errorlevel 1 (
    echo.
    echo ❌ 編譯失敗！
    pause
    exit /b 1
)
echo.

echo 正在打包 JAR 檔案...
call mvn package -DskipTests
if errorlevel 1 (
    echo.
    echo ❌ 打包失敗！
    pause
    exit /b 1
)
echo.

echo ========================================
echo   啟動應用程式
echo ========================================
echo.
echo 🚀 使用者管理系統正在啟動...
echo 📖 API 端點: http://localhost:8080/api/users
echo.

java -jar target\chapter1-spring-boot-basics-1.0.0.jar

pause
