# MCP Server Advanced KLs,

# €0HÓ
Set-Location -Path "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-server-advanced"

Write-Host "-ö Java 21 ∞É..." -ForegroundColor Green
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

Write-Host "Java H,:" -ForegroundColor Cyan
& "D:\java\jdk-21\bin\java.exe" -version

Write-Host "`n_’ MCP Server Advanced..." -ForegroundColor Green
Write-Host ":hKL( http://localhost:8081" -ForegroundColor Yellow
Write-Host "H2 Console: http://localhost:8081/h2-console" -ForegroundColor Yellow
Write-Host "API Ôﬁ: http://localhost:8081/api/admin/health" -ForegroundColor Yellow
Write-Host "`n	 Ctrl+C \b:h`n" -ForegroundColor Cyan

D:\apache-maven-3.9.11\bin\mvn.cmd spring-boot:run
