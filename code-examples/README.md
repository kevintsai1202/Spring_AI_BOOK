# Spring Boot 完整程式範例

本目錄包含從 Spring Boot 教學文件中提取的完整程式範例，按章節組織，方便學習和參考。

## 📁 目錄結構

```
code-examples/
├── chapter1-spring-boot-basics/     # 第一章：Spring Boot 基礎
├── chapter2-spring-mvc-api/         # 第二章：Spring MVC API 開發
├── chapter3-enterprise-features/    # 第三章：企業級功能
├── chapter4-spring-ai-intro/        # 第四章：Spring AI 入門
├── chapter5-spring-ai-advanced/     # 第五章：Spring AI 進階
├── chapter6-ai-memory-enhancement/  # 第六章：AI 記憶增強
├── chapter7-rag-implementation/     # 第七章：RAG 實作
├── chapter8-advanced-rag/           # 第八章：進階 RAG
└── chapter9-mcp-integration/        # 第九章：MCP 整合
```

## 📋 各章節內容

### 第一章：Spring Boot 基礎
- **DemoApplication.java** - Spring Boot 主程式入口
- **User.java** - 使用者實體模型
- **CreateUserRequest.java** - 建立使用者請求 DTO
- **UserResponse.java** - 使用者回應 DTO
- **UserService.java** - 使用者服務介面
- **UserServiceImpl.java** - 使用者服務實作
- **UserController.java** - 使用者控制器
- **application.yml** - 應用程式配置檔案

### 第二章：Spring MVC API 開發
- **UserRestController.java** - RESTful 使用者控制器
- **ProductRestController.java** - RESTful 產品控制器
- **GlobalExceptionHandler.java** - 全域異常處理器

### 第三章：企業級功能
- **StrongPassword.java** - 自訂密碼強度驗證註解
- **StrongPasswordValidator.java** - 密碼強度驗證器
- **FileUploadController.java** - 檔案上傳控制器

## 🚀 使用方式

### 1. 建立 Spring Boot 專案
使用 Spring Initializr 或 STS4 建立新的 Spring Boot 專案，選擇以下依賴：
- Spring Web
- Spring Boot DevTools
- Lombok
- Validation

### 2. 複製程式碼
將對應章節的 Java 檔案複製到您的專案中，注意包結構：
```
src/main/java/com/example/demo/
├── controller/     # 控制器類別
├── service/        # 服務類別
├── model/          # 實體模型
├── request/        # 請求 DTO
├── response/       # 回應 DTO
├── validation/     # 自訂驗證
└── exception/      # 異常處理
```

### 3. 配置檔案
將 `application.yml` 複製到 `src/main/resources/` 目錄下。

### 4. 執行應用程式
執行 `DemoApplication.java` 中的 `main` 方法啟動應用程式。

## 📖 API 測試

### 使用者管理 API
```bash
# 獲取所有使用者
curl -X GET http://localhost:8080/api/users

# 建立新使用者
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "張小明",
    "email": "ming@example.com",
    "password": "password123"
  }'

# 根據 ID 獲取使用者
curl -X GET http://localhost:8080/api/users/1
```

### 檔案上傳 API
```bash
# 單檔案上傳
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@example.txt"

# 檔案下載
curl -X GET http://localhost:8080/api/files/download/filename.txt
```

## 🔧 依賴說明

### Maven 依賴 (pom.xml)
```xml
<dependencies>
    <!-- Spring Boot Web Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Spring Boot DevTools -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## 📝 注意事項

1. **包結構**：請確保 Java 檔案放在正確的包結構中
2. **依賴版本**：建議使用 Spring Boot 3.2.x 或更新版本
3. **Java 版本**：需要 JDK 17 或更高版本
4. **Lombok 設定**：確保 IDE 已安裝 Lombok 插件
5. **檔案上傳**：檔案上傳功能需要建立 `uploads` 目錄

## 🎯 學習建議

1. **循序漸進**：按章節順序學習，每章都有完整的範例
2. **實際操作**：複製程式碼到自己的專案中執行測試
3. **理解原理**：不只是複製程式碼，要理解每個類別的作用
4. **擴展練習**：在範例基礎上添加新功能進行練習

## 📚 相關文件

- [Spring Boot 官方文件](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Framework 參考文件](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [Bean Validation 規範](https://beanvalidation.org/)

---

**提示**：這些程式範例來自完整的 Spring Boot 教學系列，涵蓋從基礎到 Spring AI 的完整學習路徑。