@echo off
set JAVA_HOME=D:\java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
set MAVEN_HOME=D:\apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%

cd /d E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-server-tools-resources

echo ===== Java Version =====
java -version

echo.
echo ===== Starting Maven Compile =====
call mvn.cmd clean compile
