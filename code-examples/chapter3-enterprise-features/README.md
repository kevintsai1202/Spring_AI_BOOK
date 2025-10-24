# Chapter 3：企業級功能開發

本章節展示 Spring Boot 的企業級功能實作，包含資料驗證、檔案處理和 API 文件。

## 專案結構

```
chapter3-enterprise-features/
├── src/
│   ├── main/
│   │   ├── java/com/example/enterprise/
│   │   │   ├── EnterpriseApplication.java          # 應用程式啟動類別
│   │   │   ├── config/
│   │   │   │   └── OpenApiConfig.java              # Swagger/OpenAPI 配置
│   │   │   ├── controller/
│   │   │   │   ├── UserController.java             # 使用者 API 控制器
│   │   │   │   └── FileStorageController.java      # 檔案儲存 API 控制器
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java                # 統一 API 回應格式
│   │   │   │   ├── UserRegistrationRequest.java    # 使用者註冊請求 DTO
│   │   │   │   └── FileUploadResponse.java         # 檔案上傳回應 DTO
│   │   │   ├── entity/
│   │   │   │   ├── User.java                       # 使用者實體
│   │   │   │   └── FileMetadata.java               # 檔案元資料實體
│   │   │   ├── exception/
│   │   │   │   ├── BusinessException.java          # 業務例外基礎類別
│   │   │   │   ├── ResourceNotFoundException.java  # 資源不存在例外
│   │   │   │   ├── FileStorageException.java       # 檔案儲存例外
│   │   │   │   └── GlobalExceptionHandler.java     # 全域例外處理器
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java             # 使用者資料存取介面
│   │   │   │   └── FileMetadataRepository.java     # 檔案元資料存取介面
│   │   │   ├── service/
│   │   │   │   ├── UserService.java                # 使用者服務
│   │   │   │   └── FileStorageService.java         # 檔案儲存服務
│   │   │   └── validation/
│   │   │       ├── StrongPassword.java             # 強密碼驗證註解
│   │   │       └── StrongPasswordValidator.java    # 強密碼驗證器
│   │   └── resources/
│   │       └── application.yml                     # 應用程式配置檔
│   └── test/
└── pom.xml                                         # Maven 專案配置
```

## 功能特色

### 1. 資料驗證 (Bean Validation)
- ✅ 使用標準 Bean Validation 註解 (`@NotBlank`, `@Email`, `@Min`, `@Max`, `@Size`)
- ✅ 自訂驗證註解 `@StrongPassword`，強制密碼包含大小寫字母、數字和特殊字元
- ✅ 全域驗證例外處理，回傳友善的錯誤訊息

### 2. 檔案上傳與下載
- ✅ 支援檔案上傳，限制檔案大小與類型
- ✅ UUID 生成唯一檔案名稱，避免檔名衝突
- ✅ 檔案元資料儲存至資料庫
- ✅ 提供下載與預覽功能

### 3. API 文件 (Swagger/OpenAPI)
- ✅ 整合 SpringDoc OpenAPI
- ✅ 自動生成 API 文件
- ✅ Swagger UI 提供互動式 API 測試介面

### 4. 統一錯誤處理
- ✅ `@RestControllerAdvice` 全域例外處理
- ✅ 統一 API 回應格式
- ✅ 包含驗證錯誤、業務邏輯錯誤和系統錯誤處理

## API 端點

### 使用者管理 API

| 方法 | 端點 | 說明 |
|------|------|------|
| GET | `/api/users` | 取得所有使用者 |
| GET | `/api/users/{id}` | 根據 ID 取得使用者 |
| POST | `/api/users/register` | 註冊新使用者 |
| DELETE | `/api/users/{id}` | 刪除使用者 |

### 檔案管理 API

| 方法 | 端點 | 說明 |
|------|------|------|
| POST | `/api/files/upload` | 上傳檔案 |
| GET | `/api/files/download/{filename}` | 下載檔案 |
| GET | `/api/files/preview/{filename}` | 預覽檔案 |

## 快速開始

### 環境需求
- Java 21
- Maven 3.9+

### 編譯專案
```powershell
mvn clean compile
```

### 執行應用程式

#### 方法1：使用提供的批次檔（推薦）
```powershell
.\run.bat
```

#### 方法2：手動打包並執行
```powershell
# 設定 Java 21 環境變數
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 打包應用程式
mvn clean package -DskipTests

# 執行 JAR 檔案
java -jar target\chapter3-enterprise-features-1.0.0.jar
```

應用程式啟動後，會顯示以下資訊：
```
🚀 企業級功能應用程式已啟動！
📖 Swagger UI: http://localhost:8080/swagger-ui.html
📖 API Docs: http://localhost:8080/v3/api-docs
```

**注意**：由於專案需要 Java 21，請確保已設定 JAVA_HOME 環境變數指向 Java 21 安裝目錄。

### 訪問 Swagger UI
開啟瀏覽器，造訪 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 測試範例

### 1. 註冊使用者

**請求：**
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

**回應：**
```json
{
  "code": 200,
  "message": "使用者註冊成功",
  "data": {
    "id": 1,
    "username": "kevin",
    "email": "kevin@example.com",
    "fullName": "Kevin Tsai",
    "age": 30,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 2. 上傳檔案

**請求：**
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@test.jpg"
```

**回應：**
```json
{
  "code": 200,
  "message": "檔案上傳成功",
  "data": {
    "id": 1,
    "originalFilename": "test.jpg",
    "storedFilename": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
    "contentType": "image/jpeg",
    "fileSize": 102400,
    "downloadUrl": "http://localhost:8080/api/files/download/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
    "previewUrl": "http://localhost:8080/api/files/preview/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg"
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 3. 驗證錯誤測試

**請求（密碼不符合強度要求）：**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "email": "test@example.com",
    "password": "weak",
    "fullName": "Test User",
    "age": 25
  }'
```

**回應：**
```json
{
  "code": 400,
  "message": "資料驗證失敗",
  "errors": {
    "password": "密碼必須至少 8 字元，包含大小寫字母、數字和特殊字元"
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

## 技術棧

- **Spring Boot 3.2.0** - 應用程式框架
- **Spring Data JPA** - 資料存取層
- **H2 Database** - 嵌入式資料庫
- **Bean Validation** - 資料驗證
- **SpringDoc OpenAPI 2.2.0** - API 文件生成
- **Lombok** - 減少樣板程式碼
- **Apache Commons IO** - 檔案處理工具

## 配置說明

### application.yml 主要配置

```yaml
# 檔案上傳配置
spring:
  servlet:
    multipart:
      max-file-size: 10MB        # 單一檔案最大 10MB
      max-request-size: 50MB     # 請求總大小最大 50MB

# 自訂配置
app:
  upload:
    path: ./uploads              # 檔案上傳目錄
    allowed-types: image/jpeg,image/png,image/gif,application/pdf  # 允許的檔案類型
```

## 學習重點

1. **Bean Validation**：使用標準驗證註解和自訂驗證器
2. **自訂驗證器**：`ConstraintValidator` 實作密碼強度驗證
3. **檔案處理**：Spring 的 `MultipartFile` 與 `Resource` 處理
4. **全域例外處理**：`@RestControllerAdvice` 統一錯誤回應
5. **API 文件**：SpringDoc 自動生成 OpenAPI 3.0 規範文件
6. **RESTful API 設計**：統一回應格式與 HTTP 狀態碼使用

## 參考連結

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Bean Validation 規範](https://beanvalidation.org/)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
