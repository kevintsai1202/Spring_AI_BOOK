# 第2章：Spring MVC 與 RESTful API

> **章節主題**：掌握 Spring MVC 核心概念、RESTful API 設計原則與實踐

---

## 📚 學習內容

### [2.1 Spring MVC API 開發基礎](./2.1-spring-mvc-basics.md)
- MVC 架構概述
- DispatcherServlet 工作原理
- @RestController vs @Controller
- JSON 自動處理機制
- 📊 **包含 Mermaid 圖表**：MVC 架構圖、DispatcherServlet 流程圖

### [2.2 RESTful API 設計原則](./2.2-restful-api-design.md)
- REST 六大核心原則
- HTTP 方法語義化使用
- 資源導向設計模式
- URL 設計規範
- API 版本控制策略
- 📊 **包含 Mermaid 圖表**：REST 原則圖、HTTP 方法對應圖

### [2.3 API 請求與回應處理](./2.3-request-response-handling.md)
- 請求參數處理 (@PathVariable, @RequestParam, @RequestBody)
- 統一回應格式設計
- HTTP 狀態碼最佳實踐
- 全域異常處理 (@RestControllerAdvice)

---

## 🎯 專案說明

本章展示完整的 **Spring MVC RESTful API** 開發實踐：

### 功能特性
- ✅ 完整的 CRUD API 實作
- ✅ RESTful 設計規範
- ✅ 統一回應格式
- ✅ 全域異常處理
- ✅ JPA 資料持久化
- ✅ H2 記憶體資料庫

### 專案結構

```
code-examples/chapter2-spring-mvc-api/
├── src/main/java/com/example/springmvc/
│   ├── SpringMvcApiApplication.java      # 應用程式入口
│   ├── controller/                       # RESTful API 控制器
│   │   ├── UserRestController.java      # 使用者 API
│   │   └── ProductRestController.java   # 產品 API
│   ├── dto/                             # 資料傳輸物件
│   │   ├── ApiResponse.java            # 統一回應格式
│   │   ├── CreateUserRequest.java      # 請求 DTO
│   │   └── UserDto.java                # 回應 DTO
│   ├── entity/                          # JPA 實體
│   ├── service/                         # 業務邏輯層
│   ├── repository/                      # 資料存取層
│   └── exception/                       # 異常處理
│       └── GlobalExceptionHandler.java  # 全域異常處理器
├── src/main/resources/
│   └── application.yml                  # 應用配置
└── README.md                           # 專案說明
```

---

## 🚀 快速開始

### 前置需求
- **JDK**: 21
- **Maven**: 3.9+

### 執行專案

```bash
# 方法一：使用 run.bat（Windows，推薦）
cd code-examples\chapter2-spring-mvc-api
.\run.bat

# 方法二：手動執行
cd code-examples/chapter2-spring-mvc-api
mvn clean compile
java -jar target/chapter2-spring-mvc-api-1.0.0.jar
```

### API 端點測試

應用程式啟動後（預設埠：8080），可以測試以下 API：

#### 使用者 API

```bash
# 取得所有使用者
curl http://localhost:8080/api/users

# 取得單一使用者
curl http://localhost:8080/api/users/1

# 建立新使用者
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","fullName":"John Doe"}'

# 更新使用者
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"username":"john_updated","email":"john.new@example.com"}'

# 刪除使用者
curl -X DELETE http://localhost:8080/api/users/1
```

#### 產品 API

```bash
# 取得所有產品（支援分頁）
curl http://localhost:8080/api/products?page=0&size=10

# 取得單一產品
curl http://localhost:8080/api/products/1

# 建立新產品
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone","price":30000,"category":"電子產品"}'
```

---

## 📊 Mermaid 架構圖

本章包含 4 張 Mermaid 架構圖，說明 Spring MVC 和 REST 核心概念：

### 圖表列表

1. **[MVC 架構圖](./images/2.1-mvc-architecture.md)**
   - 展示 Model、View、Controller 三層架構
   - 資料流程說明

2. **[DispatcherServlet 流程圖](./images/2.1-dispatcher-servlet.md)**
   - Spring MVC 請求處理完整流程
   - 13 個步驟的序列圖

3. **[REST 設計原則](./images/2.2-rest-principles.md)**
   - REST 六大核心原則
   - 詳細說明和最佳實踐

4. **[HTTP 方法與 CRUD 對應](./images/2.2-http-methods.md)**
   - HTTP 方法對應表
   - 包含使用範例和狀態碼

> 💡 **查看方式**：
> - 在 VS Code 中安裝「Markdown Preview Mermaid Support」擴充套件
> - 或在 GitHub 中直接查看（自動渲染）
> - 詳細說明請參考 [images/README_MERMAID.md](./images/README_MERMAID.md)

---

## 📸 需要的截圖（手動製作）

### API 測試截圖（3張）

使用 Postman 製作以下截圖：

1. **2.3-postman-test.png**
   - Postman API 測試畫面
   - GET `/api/users` 端點測試

2. **2.3-api-response.png**
   - API 回應格式範例
   - 展示統一回應結構

3. **2.3-error-handling.png**
   - 錯誤處理回應範例
   - 404 或驗證錯誤回應

> 📖 **詳細步驟**：請參考 [SCREENSHOT_EXECUTION_GUIDE.md](../../SCREENSHOT_EXECUTION_GUIDE.md) 中的第2章部分

---

## 🎯 學習檢查清單

完成本章後，您應該能夠：

- [ ] 理解 MVC 設計模式和 Spring MVC 工作原理
- [ ] 掌握 DispatcherServlet 請求處理流程
- [ ] 正確使用 @RestController 建立 RESTful API
- [ ] 理解 REST 六大核心原則
- [ ] 正確使用 HTTP 方法（GET、POST、PUT、PATCH、DELETE）
- [ ] 設計資源導向的 URL 結構
- [ ] 處理請求參數（@PathVariable、@RequestParam、@RequestBody）
- [ ] 實作統一的 API 回應格式
- [ ] 使用 @RestControllerAdvice 實作全域異常處理
- [ ] 正確使用 HTTP 狀態碼

---

## 📁 程式碼參考

- **完整專案**：[../../code-examples/chapter2-spring-mvc-api/](../../code-examples/chapter2-spring-mvc-api/)
- **控制器範例**：[UserRestController.java](../../code-examples/chapter2-spring-mvc-api/src/main/java/com/example/springmvc/controller/)
- **異常處理**：[GlobalExceptionHandler.java](../../code-examples/chapter2-spring-mvc-api/src/main/java/com/example/springmvc/exception/)
- **統一回應**：[ApiResponse.java](../../code-examples/chapter2-spring-mvc-api/src/main/java/com/example/springmvc/dto/)

---

## 📖 相關資源

### 官方文件
- [Spring MVC Reference](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [RESTful Web Services Guide](https://spring.io/guides/gs/rest-service/)

### 推薦閱讀
- [RESTful API 設計最佳實踐](https://restfulapi.net/)
- [Richardson Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html)

---

## 🔗 章節導航

- **上一章**：[第1章：Spring Boot 基礎](../chapter1/)
- **下一章**：[第3章：企業級功能開發](../chapter3/)
- **回到總覽**：[文件總覽](../README.md)

---

**建立日期**：2025-10-23
**Spring Boot 版本**：3.2.0
**Java 版本**：21
