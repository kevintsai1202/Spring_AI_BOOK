# Chapter 2: Spring MVC 與 RESTful API 開發

> **章節主題**：掌握 Spring MVC 核心架構、RESTful API 設計原則與最佳實踐

---

## 學習內容

### 2.1 Spring MVC API 開發基礎
- 現代化 API 架構概述
- Spring MVC 在 API 開發中的定位
- @RestController vs @Controller
- JSON 自動處理機制

### 2.2 RESTful API 設計與實踐
- REST 架構風格深度解析
- HTTP 方法語義化使用
- RESTful URL 設計原則
- HTTP 狀態碼最佳實踐
- API 版本控制策略
- 快取策略設計

### 2.3 API 請求與回應處理
- 請求參數處理 (@PathVariable, @RequestParam, @RequestBody)
- 統一回應格式設計
- 全域異常處理
- ResponseEntity 的使用

---

## 專案說明

本專案示範完整的 **RESTful API 開發**，包含：

### 功能特性
- RESTful 使用者管理 API（分頁查詢、快取控制）
- RESTful 產品管理 API（完整 CRUD 操作）
- 統一異常處理機制
- 分頁查詢支援
- DTO 模式實踐
- Spring Data JPA 整合
- H2 記憶體資料庫

### 專案結構
```
chapter2-spring-mvc-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/api/
│   │   │   ├── ApiApplication.java          # 主程式入口
│   │   │   ├── GlobalExceptionHandler.java  # 全域異常處理
│   │   │   ├── UserRestController.java      # 使用者 REST 控制器
│   │   │   ├── ProductRestController.java   # 產品 REST 控制器
│   │   │   ├── dto/                          # 資料傳輸物件
│   │   │   │   ├── UserDto.java
│   │   │   │   ├── ProductDto.java
│   │   │   │   ├── CreateProductRequest.java
│   │   │   │   ├── UpdateProductRequest.java
│   │   │   │   ├── PagedResponse.java
│   │   │   │   └── UserSearchCriteria.java
│   │   │   ├── entity/                       # 實體類別
│   │   │   │   ├── User.java
│   │   │   │   └── Product.java
│   │   │   ├── service/                      # 服務層
│   │   │   │   ├── UserService.java
│   │   │   │   ├── UserServiceImpl.java
│   │   │   │   ├── ProductService.java
│   │   │   │   └── ProductServiceImpl.java
│   │   │   ├── repository/                   # 資料存取層
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── ProductRepository.java
│   │   │   ├── mapper/                       # 映射器
│   │   │   │   ├── UserMapper.java
│   │   │   │   └── ProductMapper.java
│   │   │   ├── model/                        # 模型類別
│   │   │   │   └── ErrorResponse.java
│   │   │   └── exception/                    # 自定義異常
│   │   │       └── ResourceNotFoundException.java
│   │   └── resources/
│   │       └── application.yml               # 應用程式配置
│   └── test/java/                            # 測試程式碼
├── pom.xml                                   # Maven 配置
└── README.md                                 # 本文件
```

---

## 快速開始

### 前置需求
- **JDK**: 21
- **Maven**: 3.9+
- **IDE**: IntelliJ IDEA / Eclipse / VS Code (推薦)

### 1. 編譯專案
```bash
mvn clean compile
```

### 2. 執行應用程式
```bash
mvn spring-boot:run
```

或直接執行 JAR：
```bash
mvn clean package
java -jar target/chapter2-spring-mvc-api-1.0.0.jar
```

### 3. 驗證啟動
應用程式啟動後會顯示：
```
🚀 Spring MVC API 應用程式已啟動！
📖 API 端點: http://localhost:8080/api
```

---

## API 說明

### 使用者 API

#### 取得使用者列表（分頁）
```http
GET http://localhost:8080/api/v1/users?page=0&size=20&name=john&email=john@example.com
```

**查詢參數**：
- `page`: 頁碼（預設: 0）
- `size`: 每頁大小（預設: 20）
- `name`: 使用者名稱（模糊搜尋，選填）
- `email`: 電子郵件（模糊搜尋，選填）

**響應範例**：
```json
{
  "content": [
    {
      "id": 1,
      "username": "john",
      "email": "john@example.com",
      "fullName": "John Doe",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

#### 根據 ID 取得使用者
```http
GET http://localhost:8080/api/v1/users/{id}
```

### 產品 API

#### 建立新產品
```http
POST http://localhost:8080/api/v1/products
Content-Type: application/json

{
  "name": "iPhone 15 Pro",
  "description": "最新款 iPhone",
  "price": 36900,
  "stock": 100,
  "category": "electronics"
}
```

#### 更新產品（完整更新）
```http
PUT http://localhost:8080/api/v1/products/{id}
Content-Type: application/json

{
  "name": "iPhone 15 Pro Max",
  "description": "最新款 iPhone Pro Max",
  "price": 42900,
  "stock": 50,
  "category": "electronics"
}
```

#### 部分更新產品
```http
PATCH http://localhost:8080/api/v1/products/{id}
Content-Type: application/json

{
  "price": 39900,
  "stock": 80
}
```

#### 刪除產品
```http
DELETE http://localhost:8080/api/v1/products/{id}
```

---

## 核心技術

### 1. RESTful API 設計
- **資源導向 URL**: `/api/v1/users`, `/api/v1/products`
- **HTTP 方法語義化**: GET（查詢）、POST（建立）、PUT（更新）、PATCH（部分更新）、DELETE（刪除）
- **統一錯誤處理**: 全域異常處理器統一處理各種異常

### 2. 分頁與快取
```java
@GetMapping
public ResponseEntity<PagedResponse<UserDto>> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    // 分頁查詢實作
}

@GetMapping("/{id}")
public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
            .body(userDto);
}
```

### 3. DTO 模式
- **CreateProductRequest**: 建立產品請求 DTO（含驗證）
- **UpdateProductRequest**: 更新產品請求 DTO（含驗證）
- **ProductDto**: 產品回應 DTO
- **PagedResponse<T>**: 分頁回應 DTO

### 4. 全域異常處理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException e) {
        // 統一錯誤格式
    }
}
```

---

## 資料庫配置

### H2 Console
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: jdbc:h2:mem:testdb
- **Username**: sa
- **Password**: (空白)

### 初始化資料
應用程式使用 H2 記憶體資料庫，採用 `ddl-auto: create-drop` 策略，每次啟動會自動建立資料表結構。

---

## 測試

### 執行測試
```bash
mvn test
```

### 手動測試
使用 Postman 或 curl 測試 API：
```bash
# 取得所有使用者
curl http://localhost:8080/api/v1/users

# 建立新產品
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"MacBook Pro","description":"專業筆記型電腦","price":59900,"stock":30,"category":"electronics"}'

# 更新產品
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"MacBook Pro M3","description":"最新款 MacBook Pro","price":64900,"stock":20,"category":"electronics"}'

# 刪除產品
curl -X DELETE http://localhost:8080/api/v1/products/1
```

---

## 程式碼位置

### 完整程式碼
- 本目錄包含完整可執行的 Maven 專案

### 相關文件
- **詳細教學**: `../../docs/chapter2/`
- **原始 MD**: `../../2.1.md`, `2.2.md`, `2.3.md`

---

## 延伸學習

完成本章後，建議繼續學習：
- **第3章**: 企業級功能（資料驗證、異常處理、檔案上傳）
- **第4章**: Spring AI 入門
- **第5章**: Spring AI 進階

---

## 參考資料

- [Spring MVC Reference Guide](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [RESTful API Design Best Practices](https://restfulapi.net/)

---

**建立日期**: 2025-10-23
**Spring Boot 版本**: 3.2.0
**Java 版本**: 21
