# 第3章：企業級功能開發

本章節學習企業級應用所需的進階功能，包含資料驗證、檔案處理和 API 文件化。

## 📚 章節內容

### [3.1 資料驗證與錯誤處理](./3.1-validation-error-handling.md)
- Bean Validation (JSR 380) 標準
- 內建驗證註解使用
- 自訂驗證器實作
- 全域例外處理機制

### [3.2 檔案上傳與下載](./3.2-file-upload-download.md)
- MultipartFile 檔案處理
- 檔案大小與類型限制
- 檔案儲存策略
- 安全性考量

### [3.3 API 文件化（Swagger/OpenAPI）](./3.3-api-documentation.md)
- SpringDoc OpenAPI 整合
- Swagger UI 使用
- API 註解與文件生成
- 線上測試與除錯

## 💻 完整程式碼範例

完整的可執行程式碼位於：

```
code-examples/chapter3-enterprise-features/
├── src/main/java/com/example/enterprise/
│   ├── EnterpriseApplication.java       # 應用程式入口
│   ├── config/
│   │   └── OpenApiConfig.java          # Swagger 配置
│   ├── controller/
│   │   ├── UserController.java         # 使用者 API
│   │   └── FileStorageController.java  # 檔案管理 API
│   ├── dto/                            # 資料傳輸物件
│   │   ├── ApiResponse.java           # 統一 API 回應格式
│   │   ├── UserRegistrationRequest.java
│   │   └── FileUploadResponse.java
│   ├── entity/                         # JPA 實體
│   │   ├── User.java
│   │   └── FileMetadata.java
│   ├── service/                        # 業務邏輯
│   │   ├── UserService.java
│   │   └── FileStorageService.java
│   ├── repository/                     # 資料存取
│   ├── validation/                     # 自訂驗證器
│   │   ├── StrongPassword.java        # 強密碼註解
│   │   └── StrongPasswordValidator.java
│   └── exception/                      # 例外處理
│       ├── BusinessException.java
│       ├── ResourceNotFoundException.java
│       ├── FileStorageException.java
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   └── application.yml                 # 應用配置
├── build.bat                           # Windows 編譯腳本
├── run.bat                             # Windows 執行腳本
└── README.md                          # 詳細說明文件
```

## 🚀 快速開始

### 環境要求

⚠️ **重要**：本專案需要 Java 21 環境

```bash
# 確認 Java 版本
java -version  # 應顯示 Java 21

# 設定 JAVA_HOME（如果需要）
set JAVA_HOME=D:\java\jdk-21
set Path=D:\java\jdk-21\bin;%Path%
```

### 執行專案

**方法一：使用提供的批次檔（推薦）**
```bash
cd code-examples/chapter3-enterprise-features
.\run.bat
```

**方法二：手動編譯並執行**
```bash
cd code-examples/chapter3-enterprise-features

# 打包應用程式
mvn clean package -DskipTests

# 執行 JAR 檔案
java -jar target\chapter3-enterprise-features-1.0.0.jar
```

### 訪問 Swagger UI

應用程式啟動後：
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API 文件**: http://localhost:8080/v3/api-docs
- **健康檢查**: http://localhost:8080/actuator/health

![Swagger UI 畫面](./images/3.3-swagger-ui.png)
> 📸 **截圖說明**：Swagger UI 互動式 API 文件介面

## 📸 需要的截圖

### 3.1 資料驗證與錯誤處理
- `3.1-validation-annotations.png` - Bean Validation 註解範例
- `3.1-custom-validator.png` - 自訂驗證器程式碼
- `3.1-validation-error.png` - 驗證錯誤回應範例

### 3.2 檔案上傳與下載
- `3.2-file-upload-postman.png` - Postman 檔案上傳測試
- `3.2-file-metadata.png` - 檔案元資料回應
- `3.2-file-directory.png` - 檔案儲存目錄結構

### 3.3 API 文件化
- `3.3-swagger-ui.png` - Swagger UI 主畫面
- `3.3-api-endpoint.png` - 單一 API 端點詳細資訊
- `3.3-try-it-out.png` - Swagger UI 測試功能
- `3.3-api-response-example.png` - API 回應範例

## 🎯 主要功能特色

### 1. 資料驗證
- ✅ 使用標準 Bean Validation 註解
- ✅ 自訂 `@StrongPassword` 驗證器
- ✅ 全域驗證例外處理
- ✅ 友善的錯誤訊息

### 2. 檔案上傳/下載
- ✅ MultipartFile 處理
- ✅ 檔案類型與大小限制
- ✅ UUID 唯一檔名生成
- ✅ 檔案元資料儲存
- ✅ 下載與預覽功能

### 3. API 文件化
- ✅ SpringDoc OpenAPI 3.0 整合
- ✅ Swagger UI 互動式介面
- ✅ API 註解與文件生成
- ✅ 線上測試功能

## 🎯 學習檢查清單

完成本章後，您應該能夠：

- [ ] 使用 Bean Validation 進行資料驗證
- [ ] 建立自訂驗證器和驗證註解
- [ ] 實作全域例外處理機制
- [ ] 處理檔案上傳與下載
- [ ] 整合 Swagger/OpenAPI 文件
- [ ] 使用 Swagger UI 測試 API
- [ ] 設計統一的 API 回應格式

## 📖 API 測試範例

### 使用者註冊（含驗證）

**請求**：
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "kevin",
    "email": "kevin@example.com",
    "password": "Test@1234",
    "fullName": "Kevin Tsai",
    "age": 30
  }'
```

**成功回應**：
```json
{
  "code": 200,
  "message": "使用者註冊成功",
  "data": {
    "id": 1,
    "username": "kevin",
    "email": "kevin@example.com",
    "fullName": "Kevin Tsai"
  }
}
```

### 檔案上傳

**請求**：
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@test.jpg"
```

**成功回應**：
```json
{
  "code": 200,
  "message": "檔案上傳成功",
  "data": {
    "id": 1,
    "originalFilename": "test.jpg",
    "storedFilename": "a1b2c3d4-e5f6-7890.jpg",
    "downloadUrl": "http://localhost:8080/api/files/download/a1b2c3d4-e5f6-7890.jpg"
  }
}
```

## 📖 相關資源

**官方文件**：
- [Bean Validation Specification](https://beanvalidation.org/)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [Spring Boot File Upload](https://spring.io/guides/gs/uploading-files/)

**推薦閱讀**：
- [API 文件化最佳實踐](https://swagger.io/resources/articles/best-practices-in-api-documentation/)
- [檔案上傳安全指南](https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload)

---

## 下一章

完成本章學習後，您已經掌握了 Spring Boot 的核心開發技能！接下來請繼續閱讀 [第4章：Spring AI 入門](../chapter4/) 開始學習 AI 整合開發。
