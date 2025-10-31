# 切換到專案目錄
Set-Location -Path "E:\Spring_AI_BOOK\code-examples\chapter7-rag\chapter7-rag-vector-enhancement"

# 設定 Java 21 環境
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;" + $env:Path

# 編譯專案
mvn clean compile
