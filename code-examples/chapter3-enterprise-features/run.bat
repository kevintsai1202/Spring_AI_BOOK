@echo off
echo ======================================
echo Chapter 3 - Enterprise Features
echo ======================================
echo.
echo Setting Java 21...
set JAVA_HOME=D:\java\jdk-21
set Path=D:\java\jdk-21\bin;%Path%

echo Packaging application...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo.
echo Starting application...
echo.
java -jar target\chapter3-enterprise-features-1.0.0.jar
