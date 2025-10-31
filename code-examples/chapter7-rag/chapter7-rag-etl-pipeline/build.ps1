# 設置 Java 21 環境
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

# 編譯專案並將錯誤輸出到文件
mvn clean compile -DskipTests 2>&1 | Tee-Object -FilePath "build.log"
