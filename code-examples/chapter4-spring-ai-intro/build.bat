@echo off
echo ========================================
echo Spring AI Chapter 4 - Build Script
echo ========================================
echo.

REM 設定 Java 21 環境
set JAVA_HOME=D:\java\jdk-21
set Path=D:\java\jdk-21\bin;%Path%

echo 檢查 Java 版本...
java -version
echo.

echo 檢查 Maven 版本...
call mvn --version
echo.

echo 開始編譯專案...
call mvn clean compile

echo.
echo ========================================
echo 編譯完成！
echo ========================================
