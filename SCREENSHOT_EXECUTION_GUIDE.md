# 截圖製作執行指南

本文件提供實際執行第1-3章專案並製作截圖的詳細步驟。

## ✅ 準備工作已完成

### pom.xml 配置修正
所有三個章節的 `pom.xml` 已經配置為使用 Java 21 compiler:
- ✅ **chapter1-spring-boot-basics/pom.xml**
- ✅ **chapter2-spring-mvc-api/pom.xml**
- ✅ **chapter3-enterprise-features/pom.xml**

### 執行腳本已建立
- ✅ **chapter1-spring-boot-basics/run.bat**
- ✅ **chapter3-enterprise-features/run.bat** (已存在)

---

## 📸 第1章截圖製作（4張）

### 步驟1：編譯並啟動第1章專案

開啟 PowerShell 並執行:

```powershell
cd E:\Spring_AI_BOOK\code-examples\chapter1-spring-boot-basics

# 方法一：使用 run.bat（推薦）
.\run.bat

# 方法二：手動執行
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"
mvn clean compile
java -jar target\chapter1-spring-boot-basics-1.0.0.jar
```

### 步驟2：製作截圖

#### 截圖 1.1-sts4-install.png
- **時機**：打開 STS4 IDE 時
- **內容**：STS4 啟動後的歡迎畫面或主介面
- **工具**：Windows Snipping Tool (Win+Shift+S)
- **儲存**：`docs/chapter1/images/1.1-sts4-install.png`

#### 截圖 1.1-create-project.png
- **時機**：在 STS4 中選擇 File → New → Spring Starter Project
- **內容**：Spring Starter Project 建立對話框
- **確保顯示**：Name、Group、Artifact、Java Version 等欄位
- **儲存**：`docs/chapter1/images/1.1-create-project.png`

#### 截圖 1.1-project-structure.png
- **時機**：專案建立後
- **內容**：在 STS4 的 Package Explorer 中展開專案結構
- **確保顯示**：
  - src/main/java 下的所有類別
  - src/main/resources/application.yml
  - pom.xml
- **儲存**：`docs/chapter1/images/1.1-project-structure.png`

#### 截圖 1.1-startup-console.png
- **時機**：執行 `.\run.bat` 或 `java -jar` 後，應用程式成功啟動時
- **內容**：控制台顯示 Spring Boot 啟動成功訊息
- **確保包含**：
  - Spring Boot ASCII 標誌
  - "Started DemoApplication in X.XXX seconds" 訊息
  - 🚀 使用者管理系統已啟動！
- **儲存**：`docs/chapter1/images/1.1-startup-console.png`

### 步驟3：測試 API

確認應用程式正常運行:

```powershell
# 測試 GET /api/users
curl http://localhost:8080/api/users

# 或在瀏覽器訪問
start http://localhost:8080/api/users
```

預期回應：
```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "fullName": "Administrator"
  }
]
```

### 步驟4：停止應用程式

在控制台按 `Ctrl+C` 停止應用程式。

---

## 📸 第2章截圖製作（7張）

### 步驟1：編譯並啟動第2章專案

```powershell
cd E:\Spring_AI_BOOK\code-examples\chapter2-spring-mvc-api

# 編譯
mvn clean compile

# 打包
mvn clean package -DskipTests

# 執行
java -jar target\chapter2-spring-mvc-api-1.0.0.jar
```

### 步驟2：製作截圖

#### 架構圖（使用 Mermaid 製作，共4張）

以下截圖使用 **Mermaid** 製作：

**步驟**：
1. 開啟 `docs/chapter2/images/ARCHITECTURE_DIAGRAMS.md` 文件
2. 訪問 **Mermaid Live Editor**: https://mermaid.live/
3. 複製 Mermaid 程式碼到編輯器
4. 下載 PNG 圖片並儲存

**或使用 VS Code**：
1. 安裝「Markdown Preview Mermaid Support」擴充套件
2. 在 VS Code 中開啟 `ARCHITECTURE_DIAGRAMS.md`
3. 使用預覽功能查看圖表並截圖

**需要製作的圖片**：

1. **2.1-mvc-architecture.png** - MVC 架構圖
   - 內容：Model、View、Controller 三層架構和資料流
   - Mermaid 程式碼：見 `ARCHITECTURE_DIAGRAMS.md` 圖1
   - 儲存：`docs/chapter2/images/2.1-mvc-architecture.png`

2. **2.1-dispatcher-servlet.png** - DispatcherServlet 工作流程
   - 內容：序列圖展示請求處理流程
   - Mermaid 程式碼：見 `ARCHITECTURE_DIAGRAMS.md` 圖2
   - 儲存：`docs/chapter2/images/2.1-dispatcher-servlet.png`

3. **2.2-rest-principles.png** - REST 設計原則
   - 內容：REST 六大核心原則
   - Mermaid 程式碼：見 `ARCHITECTURE_DIAGRAMS.md` 圖3
   - 儲存：`docs/chapter2/images/2.2-rest-principles.png`

4. **2.2-http-methods.png** - HTTP 方法與 CRUD 對應表
   - 內容：HTTP 方法與 CRUD 操作對應關係
   - Mermaid 程式碼：見 `ARCHITECTURE_DIAGRAMS.md` 圖4
   - 儲存：`docs/chapter2/images/2.2-http-methods.png`

**詳細步驟請參考**：`docs/chapter2/images/ARCHITECTURE_DIAGRAMS.md`

#### API 測試截圖（共3張）

**安裝 Postman**：https://www.postman.com/downloads/

5. **2.3-postman-test.png** - Postman API 測試
   ```
   Method: GET
   URL: http://localhost:8080/api/users
   ```
   - 確保顯示完整的請求和回應
   - 儲存：`docs/chapter2/images/2.3-postman-test.png`

6. **2.3-api-response.png** - API 回應格式
   ```
   Method: GET
   URL: http://localhost:8080/api/users/1
   ```
   - 放大 Response Body，選擇 Pretty 格式
   - 儲存：`docs/chapter2/images/2.3-api-response.png`

7. **2.3-error-handling.png** - 錯誤處理回應
   ```
   Method: GET
   URL: http://localhost:8080/api/users/999  (不存在的 ID)
   ```
   - 應顯示 404 錯誤和錯誤訊息
   - 儲存：`docs/chapter2/images/2.3-error-handling.png`

### 步驟3：停止應用程式

按 `Ctrl+C` 停止。

---

## 📸 第3章截圖製作（10張）

### 步驟1：啟動第3章專案

⚠️ **重要**：第3章需要 Java 21

```powershell
cd E:\Spring_AI_BOOK\code-examples\chapter3-enterprise-features

# 使用批次檔（推薦）
.\run.bat

# 或使用 build.bat
.\build.bat
```

等待啟動完成，應顯示:
```
🚀 企業級功能應用程式已啟動！
📖 Swagger UI: http://localhost:8080/swagger-ui.html
📖 API Docs: http://localhost:8080/v3/api-docs
```

### 步驟2：製作程式碼截圖（2張）

在 STS4 或 IntelliJ IDEA 中開啟專案：

1. **3.1-validation-annotations.png**
   - 開啟：`src/main/java/com/example/enterprise/dto/UserRegistrationRequest.java`
   - 截取顯示 `@NotBlank`, `@Email`, `@Size`, `@StrongPassword` 等註解的程式碼
   - 儲存：`docs/chapter3/images/3.1-validation-annotations.png`

2. **3.1-custom-validator.png**
   - 開啟：`src/main/java/com/example/enterprise/validation/StrongPassword.java`
   - 以及：`StrongPasswordValidator.java`
   - 截取自訂驗證器的註解定義和實作
   - 儲存：`docs/chapter3/images/3.1-custom-validator.png`

### 步驟3：API 測試截圖（4張）

使用 Postman 進行測試：

3. **3.1-validation-error.png** - 驗證錯誤
   ```
   Method: POST
   URL: http://localhost:8080/api/users/register
   Headers: Content-Type: application/json
   Body (raw JSON):
   {
     "username": "test",
     "email": "invalid-email",
     "password": "weak",
     "fullName": "Test User",
     "age": 25
   }
   ```
   - 應收到 400 Bad Request 和驗證錯誤訊息
   - 儲存：`docs/chapter3/images/3.1-validation-error.png`

4. **3.2-file-upload-postman.png** - 檔案上傳
   ```
   Method: POST
   URL: http://localhost:8080/api/files/upload
   Body: form-data
   Key: file (選擇 File 類型)
   Value: 選擇一個測試圖片（如 test.jpg）
   ```
   - 確保顯示 form-data 設定和檔案名稱
   - 儲存：`docs/chapter3/images/3.2-file-upload-postman.png`

5. **3.2-file-metadata.png** - 檔案元資料回應
   - 接續上一步，放大 Response Body
   - 確保顯示 originalFilename、storedFilename、downloadUrl 等欄位
   - 儲存：`docs/chapter3/images/3.2-file-metadata.png`

6. **3.2-file-directory.png** - 檔案儲存目錄
   - 開啟檔案總管
   - 導航到：`E:\Spring_AI_BOOK\code-examples\chapter3-enterprise-features\uploads`
   - 截取顯示已上傳檔案的畫面
   - 儲存：`docs/chapter3/images/3.2-file-directory.png`

### 步驟4：Swagger UI 截圖（4張）

在瀏覽器中訪問：http://localhost:8080/swagger-ui.html

7. **3.3-swagger-ui.png** - Swagger UI 主畫面
   - 確保顯示所有 API 端點分組
   - 可按 F11 全螢幕後截圖
   - 儲存：`docs/chapter3/images/3.3-swagger-ui.png`

8. **3.3-api-endpoint.png** - API 端點詳情
   - 找到 POST `/api/users/register` 端點
   - 點擊展開
   - 截取顯示參數、請求 Body 範例、回應格式
   - 儲存：`docs/chapter3/images/3.3-api-endpoint.png`

9. **3.3-try-it-out.png** - Try it out 功能
   - 在 POST `/api/users/register` 中點擊 "Try it out"
   - 填入測試資料：
   ```json
   {
     "username": "swagger_test",
     "email": "test@example.com",
     "password": "Test@1234",
     "fullName": "Swagger Test User",
     "age": 30
   }
   ```
   - **尚未點擊 Execute**，截取填入資料後的畫面
   - 儲存：`docs/chapter3/images/3.3-try-it-out.png`

10. **3.3-api-response-example.png** - API 執行回應
    - 接續上一步，點擊 "Execute"
    - 等待回應
    - 截取 Responses 區域，包含狀態碼和 Response body
    - 儲存：`docs/chapter3/images/3.3-api-response-example.png`

### 步驟5：停止應用程式

按 `Ctrl+C` 停止。

---

## ✅ 完成檢查清單

製作完成後，請確認以下檔案都已建立：

### 第1章（4張）
- [ ] `docs/chapter1/images/1.1-sts4-install.png`
- [ ] `docs/chapter1/images/1.1-create-project.png`
- [ ] `docs/chapter1/images/1.1-project-structure.png`
- [ ] `docs/chapter1/images/1.1-startup-console.png`

### 第2章（7張）
- [ ] `docs/chapter2/images/2.1-mvc-architecture.png`
- [ ] `docs/chapter2/images/2.1-dispatcher-servlet.png`
- [ ] `docs/chapter2/images/2.2-rest-principles.png`
- [ ] `docs/chapter2/images/2.2-http-methods.png`
- [ ] `docs/chapter2/images/2.3-postman-test.png`
- [ ] `docs/chapter2/images/2.3-api-response.png`
- [ ] `docs/chapter2/images/2.3-error-handling.png`

### 第3章（10張）
- [ ] `docs/chapter3/images/3.1-validation-annotations.png`
- [ ] `docs/chapter3/images/3.1-custom-validator.png`
- [ ] `docs/chapter3/images/3.1-validation-error.png`
- [ ] `docs/chapter3/images/3.2-file-upload-postman.png`
- [ ] `docs/chapter3/images/3.2-file-metadata.png`
- [ ] `docs/chapter3/images/3.2-file-directory.png`
- [ ] `docs/chapter3/images/3.3-swagger-ui.png`
- [ ] `docs/chapter3/images/3.3-api-endpoint.png`
- [ ] `docs/chapter3/images/3.3-try-it-out.png`
- [ ] `docs/chapter3/images/3.3-api-response-example.png`

---

## 🎨 截圖品質要求

- **格式**：PNG
- **解析度**：至少 1080p (1920x1080)
- **清晰度**：文字清晰可讀
- **內容**：避免包含敏感資訊
- **大小**：單張不超過 2MB

---

## 🔧 常見問題

### Q1: Maven 編譯失敗，顯示 "invalid flag: --release"
**A**: 確認已經修改 pom.xml，添加 maven-compiler-plugin 配置（已完成）

### Q2: Spring Boot Maven Plugin 報錯 UnsupportedClassVersionError
**A**: 使用 `java -jar` 而不是 `mvn spring-boot:run`，或使用提供的 `run.bat`

### Q3: 應用程式啟動失敗
**A**: 確認 Java 版本：
```powershell
java -version  # 應顯示 java version "21"
```

### Q4: Swagger UI 無法訪問
**A**: 確認應用程式已啟動，並且沒有其他程式佔用 8080 埠

---

## 📚 相關文件

- [SCREENSHOT_GUIDE.md](./SCREENSHOT_GUIDE.md) - 完整的截圖製作指南（更詳細）
- [CLAUDE.md](./CLAUDE.md) - 環境配置說明
- [docs/README.md](./docs/README.md) - 文件總覽

---

**製作日期**：2025-10-23
**Java 版本**：21
**Spring Boot 版本**：3.2.0
