#!/usr/bin/env pwsh
# ============================================================================
# 腳本名稱: refresh-env.ps1
# 功能描述: 刷新當前 PowerShell 工作階段的環境變數（不需重啟）
# ============================================================================

Write-Host "正在刷新環境變數..." -ForegroundColor Cyan

# 從註冊表讀取系統環境變數
$SystemPath = [System.Environment]::GetEnvironmentVariable("Path", "Machine")

# 從註冊表讀取使用者環境變數
$UserPath = [System.Environment]::GetEnvironmentVariable("Path", "User")

# 合併並設定到當前工作階段
$env:Path = "$SystemPath;$UserPath"

Write-Host "✓ 環境變數已刷新" -ForegroundColor Green
Write-Host ""

# 測試 Pandoc 是否可用
try {
    $pandocVersion = pandoc --version 2>&1 | Select-Object -First 1
    Write-Host "✓ Pandoc 可用: $pandocVersion" -ForegroundColor Green
}
catch {
    Write-Host "✗ Pandoc 仍無法使用" -ForegroundColor Red
    Write-Host ""
    Write-Host "請嘗試以下方法:" -ForegroundColor Yellow
    Write-Host "  1. 完全關閉此 PowerShell 視窗，重新開啟新的視窗" -ForegroundColor Cyan
    Write-Host "  2. 或執行: " -ForegroundColor Cyan -NoNewline
    Write-Host "refreshenv" -ForegroundColor White -NoNewline
    Write-Host " (如果有安裝 Chocolatey)" -ForegroundColor Cyan
    Write-Host "  3. 或重新啟動電腦" -ForegroundColor Cyan
}
