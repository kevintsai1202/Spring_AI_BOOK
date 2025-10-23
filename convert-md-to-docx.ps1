#!/usr/bin/env pwsh
# ============================================================================
# 腳本名稱: convert-md-to-docx.ps1
# 功能描述: 將目錄下的 Markdown 檔案轉換為單一 Word 文件
# 環境需求: PowerShell 7+, Pandoc
# ============================================================================

# 設定錯誤處理
$ErrorActionPreference = "Stop"

# ============================================================================
# 函式: Sort-MdFilesByChapter
# 功能: 根據章節編號排序檔案（支援多層級：0, 1.1, 1.2, 5.3.1, 5.10）
# ============================================================================
function Sort-MdFilesByChapter {
    param (
        [Parameter(Mandatory=$true)]
        [System.IO.FileInfo[]]$Files
    )

    return $Files | Sort-Object {
        # 取得檔名並移除副檔名
        $name = $_.BaseName

        # 將檔名分割成數字陣列（如 "5.3.1" -> [5, 3, 1]）
        $parts = $name -split '\.' | ForEach-Object {
            try { [int]$_ } catch { 999 }
        }

        # 建立排序鍵（確保 5.10 在 5.9 之後）
        # 使用格式化字串，每個數字佔 3 位數（如 "005.003.001"）
        $part1 = if ($parts.Count -gt 0) { $parts[0] } else { 999 }
        $part2 = if ($parts.Count -gt 1) { $parts[1] } else { 0 }
        $part3 = if ($parts.Count -gt 2) { $parts[2] } else { 0 }

        # 格式化為可排序的字串
        $sortKey = "{0:D3}.{1:D3}.{2:D3}" -f $part1, $part2, $part3

        return $sortKey
    }
}

# ============================================================================
# 函式: Get-ChapterTitle
# 功能: 從 md 檔案中提取第一個標題作為章節標題
# ============================================================================
function Get-ChapterTitle {
    param (
        [Parameter(Mandatory=$true)]
        [string]$FilePath
    )

    try {
        # 使用串流讀取器，只讀取前幾行來找標題
        $reader = [System.IO.StreamReader]::new($FilePath, [System.Text.Encoding]::UTF8)
        $lineCount = 0
        $maxLines = 20  # 只檢查前 20 行

        while ($null -ne ($line = $reader.ReadLine()) -and $lineCount -lt $maxLines) {
            $lineCount++
            if ($line -match '^#\s+(.+)$') {
                $reader.Close()
                return $matches[1]
            }
        }

        $reader.Close()
        return $null
    }
    catch {
        Write-Warning "無法讀取檔案標題: $FilePath"
        return $null
    }
}

# ============================================================================
# 函式: Load-ChapterNames
# 功能: 載入章節名稱映射
# ============================================================================
function Load-ChapterNames {
    param (
        [Parameter(Mandatory=$true)]
        [string]$ChapterFilePath
    )

    $chapterMap = @{}

    if (Test-Path $ChapterFilePath) {
        Get-Content $ChapterFilePath -Encoding UTF8 | ForEach-Object {
            $line = $_.Trim()
            # 跳過註解和空行
            if ($line -and -not $line.StartsWith('#')) {
                if ($line -match '^(\d+)=(.+)$') {
                    $chapterMap[$matches[1]] = $matches[2]
                }
            }
        }
    }

    return $chapterMap
}

# ============================================================================
# 函式: Get-MainChapterNumber
# 功能: 從檔案名取得大章節編號
# ============================================================================
function Get-MainChapterNumber {
    param (
        [Parameter(Mandatory=$true)]
        [string]$FileName
    )

    # 從檔案名提取主章節編號 (如 "1.1.md" -> "1", "5.3.1.md" -> "5")
    if ($FileName -match '^(\d+)') {
        return $matches[1]
    }

    return $null
}

# ============================================================================
# 函式: Merge-MdFiles
# 功能: 使用串流方式合併 md 檔案以節省記憶體
# ============================================================================
function Merge-MdFiles {
    param (
        [Parameter(Mandatory=$true)]
        [System.IO.FileInfo[]]$Files,

        [Parameter(Mandatory=$true)]
        [string]$OutputPath,

        [Parameter(Mandatory=$true)]
        [hashtable]$ChapterNames
    )

    Write-Host "開始合併 $($Files.Count) 個 Markdown 檔案..." -ForegroundColor Cyan

    # 使用 StreamWriter 進行串流寫入，緩衝區 64KB
    $writer = [System.IO.StreamWriter]::new(
        $OutputPath,
        $false,  # 不追加，覆寫檔案
        [System.Text.Encoding]::UTF8,
        65536  # 64KB 緩衝區
    )

    try {
        $fileNumber = 0
        $currentMainChapter = $null
        $isFirstFile = $true

        foreach ($file in $Files) {
            $fileNumber++
            $fileName = $file.Name
            $baseName = $file.BaseName

            # 取得大章節編號
            $mainChapter = Get-MainChapterNumber -FileName $baseName

            # 取得子章節標題
            $sectionTitle = Get-ChapterTitle -FilePath $file.FullName
            if (-not $sectionTitle) {
                $sectionTitle = $baseName
            }

            Write-Host "  [$fileNumber/$($Files.Count)] 處理: $fileName - $sectionTitle" -ForegroundColor Gray

            # 檢查是否進入新的大章節
            if ($mainChapter -ne $currentMainChapter) {
                # 插入換頁符號（包括第一個大章節前）
                $writer.WriteLine()
                $writer.WriteLine("\newpage")
                $writer.WriteLine()
                $writer.WriteLine()

                # 取得大章節名稱
                $mainChapterName = if ($ChapterNames.ContainsKey($mainChapter)) {
                    $ChapterNames[$mainChapter]
                } else {
                    "第 $mainChapter 章"
                }

                # 寫入大章節標題（使用 # 作為最高層級標題）
                # Pandoc 會將 # 轉換為 Word 的標題1，這是最大的標題
                $writer.WriteLine("# $mainChapterName {.unnumbered .unlisted}")
                $writer.WriteLine()
                $writer.WriteLine()

                # 更新當前大章節
                $currentMainChapter = $mainChapter
            }
            else {
                # 同一大章節內的換頁（子章節之間）
                $writer.WriteLine()
                $writer.WriteLine("\newpage")
                $writer.WriteLine()
            }

            # 寫入子章節標題（使用 ## 表示標題2）
            $writer.WriteLine("## $sectionTitle")
            $writer.WriteLine()

            # 使用串流讀取器讀取檔案內容
            $reader = [System.IO.StreamReader]::new(
                $file.FullName,
                [System.Text.Encoding]::UTF8,
                $true,  # 偵測編碼
                65536   # 64KB 緩衝區
            )

            try {
                $isFirstLine = $true

                # 逐行讀取並寫入
                while ($null -ne ($line = $reader.ReadLine())) {
                    # 跳過第一行的標題（因為已經寫入子章節標題）
                    if ($isFirstLine -and $line -match '^#\s+') {
                        $isFirstLine = $false
                        continue
                    }
                    $isFirstLine = $false

                    $writer.WriteLine($line)
                }
            }
            finally {
                $reader.Close()
                $reader.Dispose()
            }

            # 章節結尾添加空行
            $writer.WriteLine()

            $isFirstFile = $false
        }

        Write-Host "合併完成！" -ForegroundColor Green
    }
    finally {
        $writer.Close()
        $writer.Dispose()
    }
}

# ============================================================================
# 函式: Find-PandocPath
# 功能: 尋找 Pandoc 執行檔的路徑
# ============================================================================
function Find-PandocPath {
    # 1. 先嘗試從 PATH 中找
    try {
        $pandocCmd = Get-Command pandoc -ErrorAction Stop
        return $pandocCmd.Source
    }
    catch {
        # 2. PATH 中找不到，嘗試常見安裝位置
        $possiblePaths = @(
            "C:\Program Files\Pandoc\pandoc.exe",
            "C:\Program Files (x86)\Pandoc\pandoc.exe",
            "$env:LOCALAPPDATA\Programs\Pandoc\pandoc.exe",
            "$env:LOCALAPPDATA\Pandoc\pandoc.exe",
            "$env:ProgramFiles\Pandoc\pandoc.exe"
        )

        foreach ($path in $possiblePaths) {
            if (Test-Path $path) {
                Write-Host "找到 Pandoc 安裝位置: $path" -ForegroundColor Yellow
                return $path
            }
        }

        # 3. 使用 where.exe 嘗試尋找
        try {
            $wherePath = where.exe pandoc 2>$null | Select-Object -First 1
            if ($wherePath -and (Test-Path $wherePath)) {
                return $wherePath
            }
        }
        catch {
            # 忽略錯誤
        }

        return $null
    }
}

# ============================================================================
# 函式: Test-PandocInstalled
# 功能: 檢查 Pandoc 是否已安裝並返回路徑
# ============================================================================
function Test-PandocInstalled {
    $pandocPath = Find-PandocPath

    if ($pandocPath) {
        try {
            $pandocVersion = & $pandocPath --version 2>&1 | Select-Object -First 1
            Write-Host "✓ 檢測到 Pandoc: $pandocVersion" -ForegroundColor Green
            return $pandocPath
        }
        catch {
            Write-Host "✗ 找到 Pandoc 但無法執行: $pandocPath" -ForegroundColor Red
            return $null
        }
    }
    else {
        Write-Host "✗ 未檢測到 Pandoc" -ForegroundColor Red
        Write-Host ""
        Write-Host "請安裝 Pandoc:" -ForegroundColor Yellow
        Write-Host "  1. 使用 winget: " -ForegroundColor Cyan -NoNewline
        Write-Host "winget install --id JohnMacFarlane.Pandoc" -ForegroundColor White
        Write-Host "  2. 使用 Chocolatey: " -ForegroundColor Cyan -NoNewline
        Write-Host "choco install pandoc" -ForegroundColor White
        Write-Host "  3. 或從官網下載: https://pandoc.org/installing.html" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "安裝後請:" -ForegroundColor Yellow
        Write-Host "  - 重新開啟 PowerShell 視窗" -ForegroundColor Cyan
        Write-Host "  - 或執行: " -ForegroundColor Cyan -NoNewline
        Write-Host ".\refresh-env.ps1" -ForegroundColor White
        return $null
    }
}

# ============================================================================
# 函式: Convert-MdToDocx
# 功能: 使用 Pandoc 轉換 Markdown 為 Word 文件
# ============================================================================
function Convert-MdToDocx {
    param (
        [Parameter(Mandatory=$true)]
        [string]$InputPath,

        [Parameter(Mandatory=$true)]
        [string]$OutputPath,

        [Parameter(Mandatory=$true)]
        [string]$PandocPath,

        [Parameter(Mandatory=$false)]
        [string]$ReferenceDoc
    )

    Write-Host ""
    Write-Host "開始轉換為 Word 文件..." -ForegroundColor Cyan

    # Pandoc 轉換參數說明:
    # --toc: 生成目錄 (Table of Contents)
    # --toc-depth=3: 目錄深度為 3 層
    # --number-sections: 自動為章節編號
    # --syntax-highlighting=tango: 程式碼高亮樣式
    # --reference-doc: 可選，使用自訂 Word 樣板
    # --resource-path: 資源檔案路徑（圖片等）

    $pandocArgs = @(
        $InputPath,
        "-o", $OutputPath,
        "--from=markdown+raw_html",
        "--to=docx",
        "--toc",
        "--toc-depth=3",
        "--syntax-highlighting=tango",
        "--resource-path=.",
        "--standalone"
    )

    # 如果有自訂樣板，加入參數
    if ($ReferenceDoc -and (Test-Path $ReferenceDoc)) {
        Write-Host "使用自訂樣板: $ReferenceDoc" -ForegroundColor Cyan
        $pandocArgs += "--reference-doc=$ReferenceDoc"
    }

    try {
        # 執行 Pandoc 轉換
        $process = Start-Process -FilePath $PandocPath `
                                 -ArgumentList $pandocArgs `
                                 -NoNewWindow `
                                 -Wait `
                                 -PassThru

        if ($process.ExitCode -eq 0) {
            Write-Host "✓ 轉換成功！" -ForegroundColor Green
            return $true
        }
        else {
            Write-Host "✗ 轉換失敗，退出代碼: $($process.ExitCode)" -ForegroundColor Red
            return $false
        }
    }
    catch {
        Write-Host "✗ 轉換過程發生錯誤: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# ============================================================================
# 主程式
# ============================================================================

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Markdown to Word 轉換工具" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 取得腳本所在目錄
$scriptPath = $PSScriptRoot
if ([string]::IsNullOrEmpty($scriptPath)) {
    $scriptPath = Get-Location
}

Write-Host "工作目錄: $scriptPath" -ForegroundColor Gray
Write-Host ""

# 檢查 Pandoc 並取得路徑
$pandocPath = Test-PandocInstalled
if (-not $pandocPath) {
    exit 1
}

Write-Host ""

# 載入章節名稱映射
$chapterFilePath = Join-Path $scriptPath "chapters.txt"
if (Test-Path $chapterFilePath) {
    $chapterNames = Load-ChapterNames -ChapterFilePath $chapterFilePath
    Write-Host "✓ 已載入章節名稱映射" -ForegroundColor Green
} else {
    Write-Host "⚠ 未找到 chapters.txt，將使用預設章節名稱" -ForegroundColor Yellow
    $chapterNames = @{}
}

Write-Host ""

# 取得所有 md 檔案（排除 PRD.md 和輸出檔案）
$mdFiles = Get-ChildItem -Path $scriptPath -Filter "*.md" | Where-Object {
    $_.Name -ne "PRD.md" -and
    $_.Name -ne "merged_output.md" -and
    $_.Name -ne "README_CONVERT.md" -and
    $_.BaseName -match '^\d+(\.\d+)*$'  # 只匹配數字格式的檔案
}

if ($mdFiles.Count -eq 0) {
    Write-Host "✗ 未找到符合格式的 Markdown 檔案" -ForegroundColor Red
    exit 1
}

Write-Host "找到 $($mdFiles.Count) 個 Markdown 檔案" -ForegroundColor Green

# 排序檔案
$sortedFiles = Sort-MdFilesByChapter -Files $mdFiles

Write-Host ""
Write-Host "檔案處理順序:" -ForegroundColor Yellow
$sortedFiles | ForEach-Object {
    $title = Get-ChapterTitle -FilePath $_.FullName
    $displayTitle = if ($title) { " - $title" } else { "" }
    Write-Host "  • $($_.Name)$displayTitle" -ForegroundColor Gray
}
Write-Host ""

# 設定輸出檔案路徑
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$mergedMdPath = Join-Path $scriptPath "merged_output.md"
$outputDocxPath = Join-Path $scriptPath "Spring_AI_Book_$timestamp.docx"

try {
    # 合併 Markdown 檔案
    Merge-MdFiles -Files $sortedFiles -OutputPath $mergedMdPath -ChapterNames $chapterNames

    # 檢查是否有自訂樣板
    $referenceDocPath = Join-Path $scriptPath "reference.docx"

    # 轉換為 Word 文件
    $success = Convert-MdToDocx -InputPath $mergedMdPath `
                                -OutputPath $outputDocxPath `
                                -PandocPath $pandocPath `
                                -ReferenceDoc $referenceDocPath

    if ($success) {
        # 取得檔案大小
        $fileSize = (Get-Item $outputDocxPath).Length
        $fileSizeMB = [math]::Round($fileSize / 1MB, 2)

        Write-Host ""
        Write-Host "============================================" -ForegroundColor Green
        Write-Host "  轉換完成！" -ForegroundColor Green
        Write-Host "============================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "輸出檔案: $outputDocxPath" -ForegroundColor Cyan
        Write-Host "檔案大小: $fileSizeMB MB" -ForegroundColor Cyan
        Write-Host ""

        # 詢問是否刪除臨時合併檔案
        Write-Host "是否刪除臨時合併檔案? (merged_output.md) [Y/N]" -ForegroundColor Yellow -NoNewline
        Write-Host " " -NoNewline
        $response = Read-Host

        if ($response -eq 'Y' -or $response -eq 'y') {
            Remove-Item -Path $mergedMdPath -Force
            Write-Host "✓ 已刪除臨時檔案" -ForegroundColor Green
        }
        else {
            Write-Host "保留臨時檔案: $mergedMdPath" -ForegroundColor Gray
        }
    }
    else {
        Write-Host ""
        Write-Host "轉換失敗，保留臨時合併檔案以供檢查: $mergedMdPath" -ForegroundColor Yellow
        exit 1
    }
}
catch {
    Write-Host ""
    Write-Host "✗ 發生錯誤: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "錯誤詳情:" -ForegroundColor Red
    Write-Host $_.Exception.ToString() -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "完成！" -ForegroundColor Green
