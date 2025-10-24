# Chapter 1: Spring Boot 基礎

> **章節主題**：掌握 Spring Boot 核心概念、開發環境建置、專案結構與依賴注入基礎

---

## 📚 學習內容

### 1.1 Spring Boot 快速入門
- Spring Boot 核心價值與設計理念
- 開發環境建置（JDK 21 + Maven）
- 第一個 Spring Boot 專案
- 專案結構解析

### 1.2 專案架構與配置
- Maven 依賴管理
- application.yml 配置
- 多環境配置

### 1.3 核心註解與依賴注入
- @SpringBootApplication
- @Component, @Service, @Repository, @Controller
- 依賴注入（DI）模式

### 1.4 第一個 Spring Boot 應用
- 使用者管理系統範例
- RESTful API 基礎
- DTO 模式實踐

---

## 🎯 專案說明

本專案是一個簡單的**使用者管理系統**，示範 Spring Boot 的核心功能：

### 功能特性
- ✅ 使用者 CRUD 操作
- ✅ RESTful API 設計
- ✅ DTO (Data Transfer Object) 模式
- ✅ 服務層與控制器分離
- ✅ 記憶體內資料儲存（模擬資料庫）

### 專案結構
```
chapter1-spring-boot-basics/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── DemoApplication.java      # 主程式入口
│   │   │   ├── User.java                 # 使用者實體
│   │   │   ├── CreateUserRequest.java    # 請求 DTO
│   │   │   ├── UserResponse.java         # 響應 DTO
│   │   │   ├── UserService.java          # 服務介面
│   │   │   ├── UserServiceImpl.java      # 服務實作
│   │   │   └── UserController.java       # REST 控制器
│   │   └── resources/
│   │       └── application.yml            # 應用程式配置
│   └── test/java/                         # 測試程式碼
├── pom.xml                                # Maven 配置
└── README.md                              # 本文件
```

---

## 🚀 快速開始

### 前置需求
- **JDK**: 21
- **Maven**: 3.9+
- **IDE**: IntelliJ IDEA / Eclipse / VS Code (推薦)

### 1. 編譯專案
```powershell
mvn --java-home D:\java\jdk-21 clean compile
```

### 2. 執行應用程式
```powershell
mvn --java-home D:\java\jdk-21 spring-boot:run
```

或直接執行 JAR：
```powershell
mvn --java-home D:\java\jdk-21 clean package
java -jar target\chapter1-spring-boot-basics-1.0.0.jar
```

### 3. 驗證啟動
應用程式啟動後會顯示：
```
🚀 使用者管理系統已啟動！
📖 API 文件: http://localhost:8080/api/users
```

訪問：http://localhost:8080/api/users

---

## 📖 API 說明

### 取得所有使用者
```http
GET http://localhost:8080/api/users
```

**響應範例**：
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

### 根據 ID 取得使用者
```http
GET http://localhost:8080/api/users/{id}
```

### 建立新使用者
```http
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe"
}
```

### 更新使用者
```http
PUT http://localhost:8080/api/users/{id}
Content-Type: application/json

{
  "username": "john_updated",
  "email": "john.new@example.com",
  "password": "newpassword",
  "fullName": "John Updated"
}
```

### 刪除使用者
```http
DELETE http://localhost:8080/api/users/{id}
```

---

## 🔑 核心概念

### 1. @SpringBootApplication
組合註解，包含：
- `@Configuration`: 標記為配置類別
- `@EnableAutoConfiguration`: 啟用自動配置
- `@ComponentScan`: 啟用組件掃描

### 2. 依賴注入 (DI)
```java
@RestController
public class UserController {
    // 建構子注入（推薦）
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
}
```

### 3. DTO 模式
- **CreateUserRequest**: 建立使用者的請求資料
- **UserResponse**: API 響應資料（隱藏敏感資訊如密碼）
- **User**: 內部實體類別

---

## 📝 程式碼位置

### 完整程式碼
- 本目錄包含完整可執行的 Maven 專案

### 相關文件
- **詳細教學**: `../../docs/chapter1/`
- **原始 MD**: `../../1.1.md`, `1.2.md`, `1.3.md`, `1.4.md`

---

## 🧪 測試

### 執行測試
```powershell
mvn --java-home D:\java\jdk-21 test
```

### 手動測試
使用 Postman 或 curl 測試 API：
```powershell
# 取得所有使用者
curl http://localhost:8080/api/users

# 建立新使用者
curl -X POST http://localhost:8080/api/users `\r`n  -H "Content-Type: application/json" `\r`n  -d '{"username":"test","email":"test@example.com","password":"pass123","fullName":"Test User"}'
```

---

## 📚 延伸學習

完成本章後，建議繼續學習：
- **第2章**: Spring MVC 與 RESTful API 深入
- **第3章**: 企業級功能（驗證、異常處理、檔案上傳）
- **第4章**: Spring AI 入門

---

## 🔗 相關連結

- [Spring Boot 官方文件](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Framework 官方文件](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [Maven 官方文件](https://maven.apache.org/guides/)

---

**建立日期**: 2025-10-23
**Spring Boot 版本**: 3.2.0
**Java 版本**: 21








