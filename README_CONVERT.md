# Markdown to Word 轉換工具使用說明

## 📋 功能說明

此腳本可將目錄下的所有 Markdown 檔案按照章節順序合併，並轉換為單一的 Word 文件（.docx）。

## ✨ 主要特性

- ✅ **智慧排序**: 自動按照檔案編號排序（支援 0, 1.1, 1.2, 5.3.1, 5.10 等多層級編號）
- ✅ **自動目錄**: 生成完整的文件目錄結構（最多 3 層）
- ✅ **記憶體優化**: 使用串流處理，避免大檔案記憶體溢位
- ✅ **章節分頁**: 每個章節自動分頁
- ✅ **程式碼高亮**: 支援程式碼區塊語法高亮
- ✅ **圖片支援**: 自動處理 Markdown 中的圖片連結

## 📦 環境需求

### 1. PowerShell 7+

檢查版本:
```powershell
$PSVersionTable.PSVersion
```

安裝 PowerShell 7:
```powershell
# 使用 winget
winget install --id Microsoft.PowerShell

# 或使用 MSI 安裝程式
# https://github.com/PowerShell/PowerShell/releases
```

### 2. Pandoc

安裝 Pandoc（三選一）:

```powershell
# 方法 1: 使用 winget (推薦)
winget install --id JohnMacFarlane.Pandoc

# 方法 2: 使用 Chocolatey
choco install pandoc

# 方法 3: 從官網下載
# https://pandoc.org/installing.html
```

驗證安裝:
```powershell
pandoc --version
```

## 🚀 使用方法

### 快速開始

1. 確保所有 Markdown 檔案都在同一目錄下
2. 開啟 PowerShell 7
3. 進入專案目錄
4. 執行腳本

```powershell
# 進入專案目錄
cd E:\Spring_AI_BOOK

# 執行轉換腳本
.\convert-md-to-docx.ps1
```

### 執行流程

1. **檢查環境**: 腳本會自動檢查 Pandoc 是否已安裝
2. **掃描檔案**: 找出所有符合格式的 Markdown 檔案（排除 PRD.md）
3. **顯示順序**: 列出將要處理的檔案順序
4. **合併檔案**: 按順序合併所有 Markdown 檔案
5. **轉換格式**: 使用 Pandoc 轉換為 Word 文件
6. **完成輸出**: 生成帶時間戳的 Word 檔案

## 📁 檔案命名規則

腳本會處理以下格式的檔案名稱:

```
0.md          → 序章（最優先）
1.1.md        → 第 1 章第 1 節
1.2.md        → 第 1 章第 2 節
2.1.md        → 第 2 章第 1 節
5.3.1.md      → 第 5 章第 3 節第 1 小節
5.10.md       → 第 5 章第 10 節（正確排在 5.9.md 之後）
```

**不會處理的檔案**:
- `PRD.md` - 需求文件
- `merged_output.md` - 臨時合併檔案
- 其他非數字格式的檔案名稱

## 📤 輸出結果

執行完成後會生成:

```
Spring_AI_Book_YYYYMMDD_HHmmss.docx
```

例如: `Spring_AI_Book_20250107_143022.docx`

**Word 文件包含**:
- 📑 自動生成的目錄（前 3 層標題）
- 📄 所有章節內容（按順序）
- 🔖 每章開始前自動分頁
- 💻 程式碼高亮顯示
- 🖼️ 圖片（如果可存取）

## ⚙️ 進階設定

### 修改 Pandoc 轉換選項

編輯腳本中的 `Convert-MdToDocx` 函式，調整 `$pandocArgs`:

```powershell
$pandocArgs = @(
    $InputPath,
    "-o", $OutputPath,
    "--from=markdown",
    "--to=docx",
    "--toc",                      # 生成目錄
    "--toc-depth=3",              # 目錄深度（可改為 2 或 4）
    "--highlight-style=tango",    # 高亮樣式（可改為 pygments, kate 等）
    "--resource-path=.",          # 資源路徑
    "--standalone"
)
```

### 使用自訂 Word 樣板

如果想使用自訂的 Word 樣板（控制字體、段落間距等）:

1. 創建一個 Word 樣板檔案（例如 `template.docx`）
2. 在 `$pandocArgs` 中加入:

```powershell
"--reference-doc=template.docx"
```

### 修改緩衝區大小

如果遇到記憶體問題，可調整緩衝區大小（預設 64KB）:

在 `Merge-MdFiles` 函式中修改:
```powershell
65536  # 64KB，可改為 32768 (32KB) 或 131072 (128KB)
```

## 🔧 問題排解

### 問題 1: 執行原則錯誤

```
無法載入檔案，因為系統上已停用指令碼執行
```

**解決方法**:
```powershell
# 暫時允許執行（僅限當前工作階段）
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

# 或永久允許（需管理員權限）
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 問題 2: 中文亂碼

確保:
- PowerShell 使用 UTF-8 編碼
- 所有 Markdown 檔案都是 UTF-8 編碼

設定 PowerShell 編碼:
```powershell
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

### 問題 3: 圖片無法顯示

確保:
- 圖片路徑是相對於專案目錄
- 圖片檔案確實存在
- 或使用線上圖片的完整 URL

### 問題 4: 檔案太大轉換失敗

**解決方法**:
1. 將檔案分批處理（分成多個 Word 文件）
2. 壓縮圖片大小
3. 增加系統虛擬記憶體

### 問題 5: Pandoc 轉換錯誤

檢查 Pandoc 版本:
```powershell
pandoc --version
```

建議使用 Pandoc 2.19 或更新版本。

## 📊 效能優化

### 處理大型檔案集

此腳本已針對大型檔案進行優化:

- ✅ 使用串流讀寫，不會一次載入所有內容到記憶體
- ✅ 64KB 緩衝區，平衡速度與記憶體使用
- ✅ 只讀取前 20 行來提取標題，避免完整掃描

### 效能指標參考

預估處理時間（實際依硬體而異）:

| 檔案數量 | 總大小 | 預估時間 |
|---------|--------|---------|
| 10-20 個 | < 5 MB | 10-20 秒 |
| 30-50 個 | 5-15 MB | 30-60 秒 |
| 50+ 個 | > 15 MB | 1-3 分鐘 |

## 🎯 最佳實踐

1. **檔案組織**: 使用一致的編號格式（0, 1.1, 1.2...）
2. **圖片管理**: 將圖片放在 `images/` 子目錄中
3. **定期備份**: 轉換前備份原始 Markdown 檔案
4. **版本控制**: 輸出檔案包含時間戳，方便版本管理
5. **樣板使用**: 建立自訂 Word 樣板以符合格式要求

## 📝 腳本架構

```
convert-md-to-docx.ps1
├── Sort-MdFilesByChapter    # 智慧排序函式
├── Get-ChapterTitle         # 提取章節標題
├── Merge-MdFiles            # 串流合併檔案
├── Test-PandocInstalled     # 檢查 Pandoc
├── Convert-MdToDocx         # Pandoc 轉換
└── 主程式流程               # 整合所有功能
```

## 🤝 支援與回饋

如遇到問題或有改進建議，請檢查:

1. PowerShell 版本是否為 7+
2. Pandoc 是否正確安裝
3. Markdown 檔案格式是否正確
4. 檔案編碼是否為 UTF-8

## 📚 相關資源

- [Pandoc 官方文件](https://pandoc.org/MANUAL.html)
- [PowerShell 7 文件](https://docs.microsoft.com/powershell/)
- [Markdown 語法指南](https://www.markdownguide.org/)

---

**版本**: 1.0.0
**最後更新**: 2025-01-07
**相容性**: Windows 10/11, PowerShell 7+, Pandoc 2.19+
