# 建立自訂 Word 樣板以置中大章節標題

## 方法一：手動建立樣板（推薦）

### 步驟

1. **生成初始樣板**
   ```powershell
   pwsh -File "E:\Spring_AI_BOOK\convert-md-to-docx.ps1"
   ```

2. **開啟生成的 Word 檔案**
   - 開啟 `Spring_AI_Book_*.docx`

3. **修改標題1樣式**
   - 找到任何一個大章節標題（如"第一章：Spring Boot 基礎"）
   - 選中該標題
   - 在「常用」功能區，找到「標題1」樣式
   - 右鍵點擊「標題1」→ 選擇「修改」
   - 在彈出的對話框中：
     - 點擊左下角「格式」→ 選擇「段落」
     - 對齊方式：改為「置中」
     - 字型大小：建議設為 24-28pt
     - 確定

4. **更新標題1樣式**
   - 在「修改樣式」對話框中，勾選「更新以符合選取範圍」
   - 點擊「確定」

5. **另存為樣板**
   - 點擊「檔案」→「另存新檔」
   - 儲存位置：`E:\Spring_AI_BOOK\reference.docx`
   - 檔案類型：Word 文件（.docx）
   - 刪除文件內容（保留樣式）
   - 儲存

6. **使用樣板轉換**
   腳本會自動使用 `reference.docx` 作為樣板（如果存在）

## 方法二：使用預設設定（當前）

當前腳本已經：
- ✅ 大章節前插入換頁符號
- ✅ 大章節使用 `#` 標題（最大標題）
- ✅ 子章節使用 `##` 標題

若要置中，需要：
1. 在 Word 中開啟檔案後
2. 選取大章節標題
3. 按 `Ctrl+E` 置中對齊
4. 或使用「常用」→「段落」→「置中」

## 快速測試

執行轉換後，檢查：
```powershell
# 開啟最新的 Word 檔案
ii (Get-ChildItem "E:\Spring_AI_BOOK\Spring_AI_Book_*.docx" | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName
```

## 注意事項

- 大章節標題會顯示在目錄中（使用 {.unnumbered} 表示不編號）
- 如果不想顯示在目錄中，可以改為 {.unnumbered .unlisted}
- Word 的標題1預設是最大字體，通常為 16-20pt
