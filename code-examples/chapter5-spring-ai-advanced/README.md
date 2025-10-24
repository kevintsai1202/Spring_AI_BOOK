# Spring AI 進階功能 - 第五章

本項目是《Spring AI 深入指南》第五章的完整代碼實現，展示了 Spring AI 框架的進階功能，包括提示詞範本、Tool Calling、多模態處理、企業數據工具和天氣 API 集成。

## 📋 項目概述

### 技術棧

- **Java**：21
- **Spring Boot**：3.3.0
- **Spring AI**：1.0.0-M4
- **Maven**：3.9+
- **OpenAI API**：最新版本
- **Jackson**：JSON 處理
- **Lombok**：代碼生成

### 功能模塊

| 章節 | 功能 | 實現狀態 |
|------|------|--------|
| **5.1** | 提示詞範本 | ✅ 完整 |
| **5.2** | 多模態處理 | ✅ 完整 |
| **5.6** | 函數呼叫(基礎) | ✅ 完整 |
| **5.7** | 企業數據工具 | ✅ 完整 |
| **5.8** | 工具鏈管理 | ⏳ 待實現 |
| **5.9** | 天氣 API 集成 | ✅ 完整 |
| **5.10** | 結構化輸出 | ⏳ 待實現 |

## 🚀 快速開始

### 1. 編譯項目

```bash
# 設置 Java 21 環境變數
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

# 編譯
cd chapter5-spring-ai-advanced
mvn clean compile
```

### 2. 設置 API Key

```bash
# Windows PowerShell
$env:OPENAI_API_KEY = "your-openai-api-key-here"
$env:GROQ_API_KEY = "your-groq-api-key-here"  # 可選
```

### 3. 運行應用

```bash
mvn spring-boot:run
```

應用將在 `http://localhost:8080` 上運行。

## 📚 API 端點文檔

### 企業數據工具 (5.7)

#### 查詢銷售排名

```
GET /api/v1/enterprise/sales-ranking/{month}
示例: http://localhost:8080/api/v1/enterprise/sales-ranking/2024-10
```

#### 查詢年度增長

```
GET /api/v1/enterprise/yearly-growth/{year}
示例: http://localhost:8080/api/v1/enterprise/yearly-growth/2024
```

#### 執行銷售分析

```
POST /api/v1/enterprise/analyze
Content-Type: application/json

請求體:
{
  "analysisType": "MONTHLY",
  "startDate": "2024-01",
  "endDate": "2024-10",
  "category": "電子產品",
  "includeTrendAnalysis": true,
  "includeForecast": true
}
```

#### AI 銷售分析

```
POST /api/v1/enterprise/ai-analysis
Content-Type: application/json

請求體:
{
  "question": "哪個產品在過去6個月中增長最快？",
  "analysisType": "TREND"
}
```

### 天氣 API 集成 (5.9)

#### 查詢當前天氣

```
GET /api/v1/weather/current/{city}
示例: http://localhost:8080/api/v1/weather/current/台北
```

#### 查詢週天氣預報

```
GET /api/v1/weather/forecast/{city}
示例: http://localhost:8080/api/v1/weather/forecast/台北
```

#### 查詢週末天氣

```
GET /api/v1/weather/weekend/{city}
```

#### 檢查帶傘建議

```
GET /api/v1/weather/umbrella/{city}
```

#### 獲取穿衣建議

```
GET /api/v1/weather/clothing/{city}
```

#### 比較多個城市天氣

```
GET /api/v1/weather/compare?cities=台北,台中,高雄
```

#### 查詢天氣警告

```
GET /api/v1/weather/alerts/{city}
```

### 工具呼叫 (5.6)

#### 計算

```
GET /api/v1/tools/calculate?expression=2+2
```

#### 日期時間

```
GET /api/v1/tools/current-time
```

## 📚 項目結構

```
src/main/java/com/example/
├── SpringAiAdvancedApplication.java      # 應用主類
├── config/
│   └── PromptTemplateConfig.java         # 提示詞配置
├── controller/
│   ├── TemplateController.java           # 範本控制器
│   ├── ToolCallingController.java        # 工具調用控制器
│   └── MultimodalController.java         # 多模態控制器
├── service/
│   ├── PromptTemplateService.java        # 基礎範本服務
│   ├── AdvancedPromptService.java        # 進階範本服務
│   └── PromptTemplateManager.java        # 範本管理器
└── tools/
    ├── DateTimeTools.java                # 日期時間工具
    └── CalculatorTools.java              # 計算器工具
```

## 🔌 API 端點

### 提示詞範本

```bash
# 解釋技術主題
GET /api/template/explain?topic=Spring+Framework&level=intermediate

# 解釋框架版本
GET /api/template/framework?framework=Spring+Boot
```

### Tool Calling

```bash
# 基礎 Tool Calling
GET /api/tool-calling/basic?prompt=What+is+the+current+time

# 當前時間查詢
GET /api/tool-calling/current-time
GET /api/tool-calling/current-time?format=yyyy/MM/dd
```

### 多模態

```bash
# 圖片分析
POST /api/multimodal/image-analysis
  -F "file=@image.jpg"
  -F "message=Describe this image"
```

## 🧪 測試

### 使用 curl 測試

```bash
# 測試提示詞範本
curl "http://localhost:8080/api/template/explain?topic=Docker"

# 測試 Tool Calling
curl "http://localhost:8080/api/tool-calling/current-time"

# 測試多模態（需要用 curl 的 -F 參數）
curl -X POST http://localhost:8080/api/multimodal/image-analysis \
  -F "file=@test.jpg" \
  -F "message=Analyze"
```

### 使用 Postman 測試

1. 導入 API 端點
2. 設置參數
3. 發送請求
4. 查看響應

詳見 `API_TEST_PLAN.md`

## 📖 文檔

- `docs/chapter5/5.0.md` - 章節概述
- `docs/chapter5/5.1.md` - 提示詞範本
- `docs/chapter5/5.2.md` - Tool Calling
- `docs/chapter5/5.3.md` - 多模態處理
- `docs/chapter5/5.4.md` - 進階提示詞設計

## 🔧 配置

### application.yml

```yaml
spring:
  application:
    name: spring-ai-advanced
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7

server:
  port: 8080
  servlet:
    multipart:
      max-file-size: 25MB
      max-request-size: 50MB
```

## 🛠️ 常見問題

### Q1：編譯失敗 - Java 版本

**A：** 確保使用 Java 21，設置正確的 JAVA_HOME

```bash
$env:JAVA_HOME = "D:\java\jdk-21"
java -version  # 驗證
```

### Q2：API 返回 401

**A：** 檢查 OPENAI_API_KEY 環境變數是否設置

```bash
echo $env:OPENAI_API_KEY
```

### Q3：多模態 API 上傳失敗

**A：** 檢查：
- 圖片格式是否支持（JPEG, PNG, GIF, WebP）
- 文件大小是否小於 10MB
- Content-Type 是否正確

## 📝 關鍵特性

### 1. 提示詞範本

- ✅ 動態變數替換
- ✅ 配置文件管理
- ✅ 範本驗證
- ✅ 默認值支持

### 2. Tool Calling

- ✅ DateTimeTools - 日期時間操作
- ✅ CalculatorTools - 數學計算
- ✅ 易於擴展

### 3. 多模態

- ✅ 圖片分析
- ✅ 文件驗證
- ✅ 錯誤處理
- ✅ 安全上傳

## 🔄 工作流程

```
使用者請求
    ↓
選擇合適的控制器
    ↓
使用 ChatClient 調用 AI
    ↓
可能需要工具支持
    ↓
返回 AI 回應
```

## 🎯 最佳實踐

1. **提示詞設計**
   - 清晰的角色定義
   - 充分的上下文
   - 明確的輸出格式

2. **工具設計**
   - 單一責任
   - 完善的錯誤處理
   - 清晰的文檔

3. **API 使用**
   - 驗證輸入
   - 優雅的錯誤處理
   - 日誌記錄

## 📚 相關資源

- [Spring AI 官方文檔](https://docs.spring.io/spring-ai/reference/)
- [OpenAI API 文檔](https://platform.openai.com/docs)
- [Spring Boot 官方文檔](https://spring.io/projects/spring-boot)

## 🤝 貢獻

歡迎提交問題和改進建議！

## 📄 許可

MIT License

---

**項目完成日期：** 2025年10月24日

**版本：** 1.0.0

