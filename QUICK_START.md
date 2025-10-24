# 🚀 快速開始指南

**目的**：驗證第1-3章專案並製作必要的截圖

---

## 📋 快速檢查

在開始之前，請確認：

- [ ] 已安裝 Java 21（路徑：`D:\java\jdk-21`）
- [ ] 已安裝 Maven 3.9+
- [ ] 已安裝 STS4 或 IntelliJ IDEA
- [ ] 已安裝 Postman（用於 API 測試）
- [ ] 可訪問 Mermaid Live Editor (https://mermaid.live/) 製作架構圖

---

## ⚡ 三步驟快速開始

### 步驟1：測試第1章（5分鐘）

```powershell
# 開啟 PowerShell，執行
cd E:\Spring_AI_BOOK\code-examples\chapter1-spring-boot-basics
.\run.bat
```

看到以下訊息表示成功：
```
🚀 使用者管理系統已啟動！
```

測試 API：
```powershell
# 開啟另一個 PowerShell 視窗
curl http://localhost:8080/api/users
```

按 `Ctrl+C` 停止應用程式。

---

### 步驟2：測試第2章（5分鐘）

```powershell
cd E:\Spring_AI_BOOK\code-examples\chapter2-spring-mvc-api
.\run.bat
```

測試 API：
```powershell
curl http://localhost:8080/api/users
```

按 `Ctrl+C` 停止。

---

### 步驟3：測試第3章（5分鐘）

```powershell
cd E:\Spring_AI_BOOK\code-examples\chapter3-enterprise-features
.\run.bat
```

訪問 Swagger UI：
```powershell
start http://localhost:8080/swagger-ui.html
```

按 `Ctrl+C` 停止。

---

## 📸 製作截圖

如果三個專案都能正常運行，請使用以下文件製作截圖：

**📖 詳細指南**：[SCREENSHOT_EXECUTION_GUIDE.md](./SCREENSHOT_EXECUTION_GUIDE.md)

這份指南包含：
- 每張截圖的詳細步驟
- 預期的畫面內容
- 完成檢查清單

**截圖總數**：21張
- 第1章：4張
- 第2章：7張
- 第3章：10張

---

## 🔧 遇到問題？

### 編譯錯誤
**錯誤訊息**：`invalid flag: --release`

**解決方案**：
1. 確認 Java 21 已安裝在 `D:\java\jdk-21`
2. pom.xml 應該已經配置好（如果沒有，請參考 `VERIFICATION_SUMMARY.md`）

### 啟動失敗
**錯誤訊息**：`UnsupportedClassVersionError`

**解決方案**：
使用 run.bat 而不是直接使用 Maven 命令

### 埠被佔用
**錯誤訊息**：`Port 8080 was already in use`

**解決方案**：
```powershell
# 查找佔用 8080 埠的程序
netstat -ano | findstr :8080

# 終止該程序（用實際的 PID 替換 <PID>）
taskkill /F /PID <PID>
```

---

## 📚 更多資訊

- **環境配置**：[CLAUDE.md](./CLAUDE.md)
- **完整報告**：[VERIFICATION_SUMMARY.md](./VERIFICATION_SUMMARY.md)
- **詳細截圖指南**：[SCREENSHOT_GUIDE.md](./SCREENSHOT_GUIDE.md)
- **執行指南**：[SCREENSHOT_EXECUTION_GUIDE.md](./SCREENSHOT_EXECUTION_GUIDE.md)

---

## ✅ 完成後

請確認：

- [ ] 第1-3章專案都能正常運行
- [ ] 製作了21張截圖
- [ ] 所有截圖儲存在正確位置
- [ ] 截圖品質符合要求（PNG、1080p、清晰）

---

**預估時間**：
- 驗證三個專案：15分鐘
- 製作截圖：2-3小時
- **總計**：約3小時

**建議**：
- 分批完成（例如每天一章）
- 使用截圖工具的快速鍵（Win+Shift+S）提高效率
- 先熟悉 Postman 的使用再開始製作 API 測試截圖

祝您順利完成！🎉
