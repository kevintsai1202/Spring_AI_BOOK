Set-Location "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-client-basic"

$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

Write-Host "Java Version:"
& java -version

Write-Host "`nCompiling..."
& "D:\apache-maven-3.9.11\bin\mvn.cmd" clean compile

Write-Host "`nDone"
