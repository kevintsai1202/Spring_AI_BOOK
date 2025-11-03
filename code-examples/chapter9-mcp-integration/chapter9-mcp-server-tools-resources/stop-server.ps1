# 停止佔用 8080 端口的進程

Write-Host "正在查找佔用 8080 端口的進程..." -ForegroundColor Cyan

$connection = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -First 1

if ($connection) {
    $processId = $connection.OwningProcess
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue

    if ($process) {
        Write-Host "找到進程: $($process.ProcessName) (PID: $processId)" -ForegroundColor Yellow
        Write-Host "正在停止進程..." -ForegroundColor Cyan

        Stop-Process -Id $processId -Force
        Start-Sleep -Seconds 2

        Write-Host "✓ 進程已停止" -ForegroundColor Green
    }
} else {
    Write-Host "沒有進程佔用 8080 端口" -ForegroundColor Green
}
