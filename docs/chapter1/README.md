# 第1章：Spring Boot 基礎

本章節介紹 Spring Boot 的核心概念和開發基礎，為後續的 Spring AI 整合學習打下堅實基礎。

## 📚 章節內容

### [1.1 Spring Boot 快速入門](./1.1-spring-boot-quickstart.md)
- Spring Boot 核心價值與特性
- 開發環境建置（STS4）
- 第一個 Spring Boot 專案
- 為 Spring AI 做準備

### 1.2 專案架構與配置
- 分層架構設計原則
- 標準目錄結構
- 配置管理策略（application.yml）
- 多環境配置

### 1.3 依賴注入與核心註解
- IoC 容器與依賴注入
- 常用註解詳解（@Component, @Service, @Repository）
- Bean 生命週期
- 自動配置原理

### 1.4 實戰練習
- 建立完整的 RESTful API
- 整合資料驗證
- 例外處理機制
- 單元測試與整合測試

## 💻 完整程式碼範例

所有章節的完整可執行程式碼都在以下目錄：

```
code-examples/chapter1-spring-boot-basics/
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java          # 應用程式入口
│   ├── controller/                   # RESTful API 控制器
│   ├── service/                      # 業務邏輯層
│   ├── model/                       # 資料模型
│   ├── config/                      # 配置類別
│   └── exception/                   # 例外處理
├── src/main/resources/
│   ├── application.yml              # 主配置
│   ├── application-dev.yml          # 開發環境配置
│   └── application-prod.yml         # 生產環境配置
├── src/test/java/                   # 測試程式碼
└── README.md                        # 專案說明與執行指南
```

## 🚀 快速開始

### 環境要求
- JDK 17 或 21
- Maven 3.9+
- STS4 或 IntelliJ IDEA

### 執行專案

```bash
# 進入專案目錄
cd code-examples/chapter1-spring-boot-basics

# 編譯專案
mvn clean compile

# 執行應用程式
mvn spring-boot:run
```

### 驗證執行結果

應用程式啟動後：
- API 端點：http://localhost:8080/api
- 健康檢查：http://localhost:8080/api/health
- Actuator：http://localhost:8080/actuator

## 📸 需要的截圖

本章節文件中標註了以下截圖位置，請參考 `images/README.md` 了解詳細說明：

### 1.1 Spring Boot 快速入門
- `1.1-sts4-install.png` - STS4 安裝與啟動畫面
- `1.1-create-project.png` - Spring Starter Project 建立對話框
- `1.1-project-structure.png` - 專案目錄結構
- `1.1-startup-console.png` - 應用程式啟動成功畫面

### 1.2 專案架構與配置
- `1.2-layered-architecture.png` - 分層架構示意圖
- `1.2-config-files.png` - 多環境配置檔案結構

### 1.3 依賴注入與核心註解
- `1.3-bean-lifecycle.png` - Bean 生命週期示意圖
- `1.3-auto-configuration.png` - 自動配置執行流程

### 1.4 實戰練習
- `1.4-api-test.png` - REST API 測試結果（Postman 或 curl）
- `1.4-test-coverage.png` - 單元測試覆蓋率報告

## 🎯 學習檢查清單

完成本章後，您應該能夠：

- [ ] 使用 STS4 快速建立 Spring Boot 專案
- [ ] 理解 Spring Boot 的分層架構設計
- [ ] 配置多環境的 application.yml
- [ ] 使用依賴注入和常用註解
- [ ] 建立 RESTful API 端點
- [ ] 實作資料驗證和例外處理
- [ ] 撰寫單元測試和整合測試

## 📖 相關資源

**官方文件**：
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Framework Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/)

**推薦閱讀**：
- [Spring Boot 最佳實踐](https://spring.io/guides)
- [依賴注入設計模式](https://martinfowler.com/articles/injection.html)

---

## 下一章

完成本章學習後，請繼續閱讀 [第2章：Spring MVC 與 RESTful API](../chapter2/) 深入學習 Web 應用開發。
