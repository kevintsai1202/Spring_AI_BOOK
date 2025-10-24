# 截圖製作指南

本文件提供完整的截圖製作步驟，確保所有必要的截圖都能正確製作。

## 📋 截圖清單總覽

- **第1章**：4張截圖
- **第2章**：7張截圖
- **第3章**：10張截圖
- **總計**：21張截圖

---

## 🚀 第1章：Spring Boot 基礎（4張）

### 準備工作

1. 確保 STS4 已安裝
2. 確保 Java 21 環境已設定

### 截圖1：1.1-sts4-install.png

**內容**：STS4 啟動後的歡迎畫面

**步驟**：
1. 啟動 STS4
2. 如果是第一次啟動，會顯示歡迎畫面
3. 截取整個 IDE 視窗
4. 確保可以看到：
   - STS4 標題列
   - 工作區選擇對話框（如有）
   - 歡迎頁面或主介面

**工具**：Windows Snipping Tool 或 Snip & Sketch (Win+Shift+S)

**儲存位置**：`docs/chapter1/images/1.1-sts4-install.png`

---

### 截圖2：1.1-create-project.png

**內容**：Spring Starter Project 建立對話框

**步驟**：
1. 在 STS4 中選擇 `File` → `New` → `Spring Starter Project`
2. 等待對話框完全載入
3. 截取對話框畫面
4. 確保可以看到：
   - Name、Group、Artifact 等欄位
   - Java Version 選項
   - Spring Boot Version 選項

**儲存位置**：`docs/chapter1/images/1.1-create-project.png`

---

### 截圖3：1.1-project-structure.png

**內容**：專案目錄結構

**步驟**：
1. 開啟 chapter1-spring-boot-basics 專案
2. 在 Package Explorer 或 Project Explorer 中展開專案
3. 展開以下目錄：
   - src/main/java
   - src/main/resources
   - src/test/java
4. 截取專案樹狀結構
5. 確保可以看到：
   - DemoApplication.java
   - application.yml
   - pom.xml

**儲存位置**：`docs/chapter1/images/1.1-project-structure.png`

---

### 截圖4：1.1-startup-console.png

**內容**：Spring Boot 應用成功啟動的控制台輸出

**步驟**：
```bash
# 進入專案目錄
cd E:\Spring_AI_BOOK\code-examples\chapter1-spring-boot-basics

# 確認 Java 21
java -version

# 執行應用程式
mvn spring-boot:run
```

**截圖時機**：看到以下訊息時截圖
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::

Started DemoApplication in X.XXX seconds
```

**確保包含**：
- Spring Boot ASCII 標誌
- "Started DemoApplication" 訊息
- 啟動時間

**儲存位置**：`docs/chapter1/images/1.1-startup-console.png`

---

## 🚀 第2章：Spring MVC API（7張）

### 準備工作

```bash
# 進入第2章專案
cd E:\Spring_AI_BOOK\code-examples\chapter2-spring-mvc-api

# 編譯並啟動
mvn clean compile
mvn spring-boot:run
```

**等待啟動完成**，看到 "Started ApiApplication" 訊息。

### 截圖5：2.3-postman-test.png

**內容**：Postman API 測試畫面

**步驟**：
1. 開啟 Postman
2. 建立新請求：
   - Method: GET
   - URL: `http://localhost:8080/api/users`
3. 點擊 Send
4. 截取整個 Postman 視窗
5. 確保可以看到：
   - 請求 URL 和方法
   - 回應狀態碼 (200 OK)
   - JSON 回應內容

**儲存位置**：`docs/chapter2/images/2.3-postman-test.png`

---

### 截圖6：2.3-api-response.png

**內容**：API 成功回應的 JSON 格式

**步驟**：
1. 在 Postman 中執行：
   - Method: GET
   - URL: `http://localhost:8080/api/users/1`
2. 點擊 Send
3. 放大 Response Body 區域
4. 選擇 Pretty 格式顯示
5. 截取 JSON 回應
6. 確保可以看到完整的 JSON 結構

**或使用 curl**：
```bash
curl http://localhost:8080/api/users/1
```

**儲存位置**：`docs/chapter2/images/2.3-api-response.png`

---

### 截圖7：2.3-error-handling.png

**內容**：錯誤處理回應範例

**步驟**：
1. 在 Postman 中執行：
   - Method: GET
   - URL: `http://localhost:8080/api/users/999` （不存在的 ID）
2. 點擊 Send
3. 應該收到 404 錯誤
4. 截取回應
5. 確保可以看到：
   - 狀態碼 404
   - 錯誤訊息
   - 錯誤詳情

**儲存位置**：`docs/chapter2/images/2.3-error-handling.png`

---

### 截圖8-11：架構圖（使用 Mermaid 製作）

**工具**：Mermaid Live Editor (https://mermaid.live/)

**步驟**：
1. 開啟 `docs/chapter2/images/ARCHITECTURE_DIAGRAMS.md` 文件
2. 複製對應圖表的 Mermaid 程式碼
3. 訪問 https://mermaid.live/
4. 貼上程式碼
5. 點擊「Actions」→「PNG」下載圖片
6. 儲存到 `docs/chapter2/images/` 目錄

**需要製作的圖片**：
- `2.1-mvc-architecture.png` - MVC 三層架構圖
- `2.1-dispatcher-servlet.png` - DispatcherServlet 請求處理流程
- `2.2-rest-principles.png` - REST 設計原則
- `2.2-http-methods.png` - HTTP 方法與 CRUD 對應

**詳細 Mermaid 程式碼**：請參考 `docs/chapter2/images/ARCHITECTURE_DIAGRAMS.md`

---

## 🚀 第3章：企業級功能（10張）

### 準備工作

⚠️ **重要**：第3章需要 Java 21

```bash
# 設定 Java 21
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 確認版本
java -version

# 進入第3章專案
cd E:\Spring_AI_BOOK\code-examples\chapter3-enterprise-features

# 執行（使用批次檔）
.\run.bat

# 或手動執行
mvn clean package -DskipTests
java -jar target\chapter3-enterprise-features-1.0.0.jar
```

**等待啟動完成**，看到：
```
🚀 企業級功能應用程式已啟動！
📖 Swagger UI: http://localhost:8080/swagger-ui.html
📖 API Docs: http://localhost:8080/v3/api-docs
```

---

### 截圖10：3.1-validation-error.png

**內容**：驗證錯誤回應範例

**步驟 - 使用 Postman**：
1. Method: POST
2. URL: `http://localhost:8080/api/users/register`
3. Headers: `Content-Type: application/json`
4. Body (raw JSON):
```json
{
  "username": "test",
  "email": "invalid-email",
  "password": "weak",
  "fullName": "Test User",
  "age": 25
}
```
5. 點擊 Send
6. 應該收到 400 Bad Request 和驗證錯誤訊息
7. 截取回應

**或使用 curl**：
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"test\",\"password\":\"weak\"}"
```

**儲存位置**：`docs/chapter3/images/3.1-validation-error.png`

---

### 截圖11：3.2-file-upload-postman.png

**內容**：Postman 檔案上傳測試

**步驟**：
1. 準備一個測試圖片檔案（如 test.jpg）
2. 在 Postman 中：
   - Method: POST
   - URL: `http://localhost:8080/api/files/upload`
   - Body 類型選擇：form-data
   - 添加 Key: `file`，Type: File
   - 選擇您的測試圖片
3. 點擊 Send
4. 截取整個 Postman 視窗
5. 確保可以看到：
   - form-data 設定
   - 選擇的檔案名稱
   - 回應狀態碼和 JSON

**儲存位置**：`docs/chapter3/images/3.2-file-upload-postman.png`

---

### 截圖12：3.2-file-metadata.png

**內容**：檔案上傳成功的回應

**步驟**：
1. 接續上一步的檔案上傳
2. 放大 Response Body
3. 確保顯示 Pretty JSON 格式
4. 截取回應
5. 確保可以看到：
   - originalFilename
   - storedFilename
   - downloadUrl
   - previewUrl
   - fileSize
   - contentType

**儲存位置**：`docs/chapter3/images/3.2-file-metadata.png`

---

### 截圖13：3.2-file-directory.png

**內容**：檔案儲存目錄結構

**步驟**：
1. 開啟檔案總管
2. 導航到：`E:\Spring_AI_BOOK\code-examples\chapter3-enterprise-features\uploads`
3. 確保可以看到上傳的檔案（UUID 命名）
4. 截取檔案總管視窗
5. 或使用命令：
```bash
ls E:\Spring_AI_BOOK\code-examples\chapter3-enterprise-features\uploads
```

**儲存位置**：`docs/chapter3/images/3.2-file-directory.png`

---

### 截圖14：3.3-swagger-ui.png

**內容**：Swagger UI 主畫面

**步驟**：
1. 開啟瀏覽器
2. 訪問：http://localhost:8080/swagger-ui.html
3. 等待頁面完全載入
4. 截取整個瀏覽器視窗（或全螢幕）
5. 確保可以看到：
   - Swagger UI 標題和說明
   - 所有 API 端點分組（使用者管理、檔案管理）
   - 展開的端點列表

**提示**：可以按 F11 進入全螢幕模式再截圖

**儲存位置**：`docs/chapter3/images/3.3-swagger-ui.png`

---

### 截圖15：3.3-api-endpoint.png

**內容**：單一 API 端點詳細資訊

**步驟**：
1. 在 Swagger UI 中
2. 找到 POST `/api/users/register` 端點
3. 點擊展開
4. 截取展開後的區域
5. 確保可以看到：
   - 端點描述
   - 參數列表
   - 請求 Body 範例
   - 回應格式

**儲存位置**：`docs/chapter3/images/3.3-api-endpoint.png`

---

### 截圖16：3.3-try-it-out.png

**內容**：Swagger UI 測試功能

**步驟**：
1. 在 POST `/api/users/register` 端點
2. 點擊 "Try it out" 按鈕
3. 在 Request body 中填入測試資料：
```json
{
  "username": "swagger_test",
  "email": "test@example.com",
  "password": "Test@1234",
  "fullName": "Swagger Test User",
  "age": 30
}
```
4. **尚未點擊 Execute**
5. 截取填入資料後的畫面

**儲存位置**：`docs/chapter3/images/3.3-try-it-out.png`

---

### 截圖17：3.3-api-response-example.png

**內容**：Swagger UI 執行測試後的回應

**步驟**：
1. 接續上一步
2. 點擊 "Execute" 按鈕
3. 等待回應
4. 向下捲動到 "Responses" 區域
5. 截取回應區域
6. 確保可以看到：
   - HTTP 狀態碼
   - Response body (JSON)
   - Response headers

**儲存位置**：`docs/chapter3/images/3.3-api-response-example.png`

---

## 📝 截圖檢查清單

完成後請確認：

### 第1章（4張）
- [ ] 1.1-sts4-install.png
- [ ] 1.1-create-project.png
- [ ] 1.1-project-structure.png
- [ ] 1.1-startup-console.png

### 第2章（7張）
- [ ] 2.1-mvc-architecture.png （繪製）
- [ ] 2.1-dispatcher-servlet.png （繪製）
- [ ] 2.2-rest-principles.png （繪製）
- [ ] 2.2-http-methods.png （繪製）
- [ ] 2.3-postman-test.png
- [ ] 2.3-api-response.png
- [ ] 2.3-error-handling.png

### 第3章（10張）
- [ ] 3.1-validation-annotations.png （程式碼）
- [ ] 3.1-custom-validator.png （程式碼）
- [ ] 3.1-validation-error.png
- [ ] 3.2-file-upload-postman.png
- [ ] 3.2-file-metadata.png
- [ ] 3.2-file-directory.png
- [ ] 3.3-swagger-ui.png
- [ ] 3.3-api-endpoint.png
- [ ] 3.3-try-it-out.png
- [ ] 3.3-api-response-example.png

## 🎨 截圖品質要求

- **解析度**：至少 1080p (1920x1080)
- **格式**：PNG
- **清晰度**：文字清晰可讀
- **內容**：避免包含敏感資訊
- **大小**：單張不超過 2MB

## 🔧 推薦工具

### 截圖工具
- **Windows 內建**：
  - Snipping Tool
  - Snip & Sketch (Win+Shift+S)
- **第三方**：
  - ShareX（功能強大）
  - Greenshot（輕量級）
  - Lightshot（快速分享）

### API 測試工具
- **Postman**（推薦）
- curl（命令列）
- HTTPie（命令列）

### 繪圖工具
- **draw.io / diagrams.net**（免費）
- Lucidchart
- PlantUML（程式碼生成）
- Mermaid（Markdown 圖表）

---

## 📞 需要協助？

如果在截圖過程中遇到問題：
1. 檢查應用程式是否正常啟動
2. 確認 Java 版本是否正確
3. 查看控制台是否有錯誤訊息
4. 參考各章節 README.md 的詳細說明
