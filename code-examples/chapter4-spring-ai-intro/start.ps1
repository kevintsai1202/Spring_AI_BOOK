# Spring AI Chapter 4 - Run Application

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Spring AI Chapter 4 - Run Application  " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Set Java 21 environment
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

Write-Host "Checking Java version..." -ForegroundColor Yellow
java -version
Write-Host ""

# Load .env file
$envFile = "E:\Spring_AI_BOOK\.env"
if (Test-Path $envFile) {
    Write-Host "Loading API Keys from .env..." -ForegroundColor Yellow

    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([A-Z_]+)=(.+)$') {
            $key = $matches[1]
            $value = $matches[2]
            Set-Item -Path "env:$key" -Value $value
            Write-Host "  [OK] Loaded: $key" -ForegroundColor Green
        }
    }

    Write-Host "[OK] API Keys loaded successfully" -ForegroundColor Green
} else {
    Write-Host "[ERROR] .env file not found!" -ForegroundColor Red
    Write-Host "Please create .env file with OPENAI_API_KEY" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "Starting Spring Boot application..." -ForegroundColor Yellow
Write-Host "Server will run on: http://localhost:8080" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow
Write-Host ""

# Run Maven Spring Boot
mvn spring-boot:run
