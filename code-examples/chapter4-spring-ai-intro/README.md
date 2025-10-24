# Spring AI 入門範例專案

> **第四章範例程式碼**：掌握 Spring AI 的基礎使用，從 Hello World 到流式輸出的完整實作

---

## 📚 專案簡介

本專案是《使用 Spring AI 打造企業 RAG 知識庫》第四章的完整範例程式碼，展示了 Spring AI 1.0 GA 版本的核心功能：

- ✅ **ChatClient API**：統一的 AI 對話介面
- ✅ **流式輸出**：Server-Sent Events (SSE) 即時回應
- ✅ **多模型支援**：OpenAI、Groq、Gemini 彈性切換
- ✅ **參數調校**：Temperature、Max Tokens 等參數配置
- ✅ **前端整合**：EventSource、React、Vue 範例

---

## 🎯 學習目標

完成本專案實作後，您將能夠：

1. 建立第一個 Spring AI 應用程式
2. 掌握 ChatClient 和 ChatModel 的使用方法
3. 實現類似 ChatGPT 的流式輸出效果
4. 配置和切換不同的 AI 模型
5. 整合前端實現完整的對話應用

---

## 🛠️ 環境需求

### 必要環境
- **Java**：21 或以上版本
- **Maven**：3.9 或以上版本
- **IDE**：IntelliJ IDEA / Eclipse / VS Code（推薦 IntelliJ IDEA）

### API Key 需求
- **必填**：OpenAI API Key（[申請連結](https://platform.openai.com/api-keys)）
- **可選**：Groq API Key（免費，[申請連結](https://console.groq.com/)）
- **可選**：Gemini API Key（[申請連結](https://makersuite.google.com/app/apikey)）

### 版本資訊
- Spring Boot: 3.3.0
- Spring AI: 1.0.0-M4
- Java: 21

---

## 🚀 快速開始

### 1. 取得專案

```bash
cd code-examples/chapter4-spring-ai-intro
```

### 2. 配置 API Key

複製環境變數範本並填入您的 API Key：

```bash
# Windows PowerShell
copy .env.example .env

# Linux / macOS
cp .env.example .env
```

編輯 `.env` 檔案，填入真實的 API Key：

```bash
# 必填：OpenAI API Key
OPENAI_API_KEY=sk-your-real-api-key-here

# 可選：Groq API Key（免費）
GROQ_API_KEY=gsk-your-groq-api-key-here
```

**⚠️ 重要提醒**：
- `.env` 檔案已加入 `.gitignore`，不會被提交到版本控制
- 絕不要將真實的 API Key 硬編碼在程式碼中
- 定期輪換 API Key 以確保安全

### 3. 設定環境變數（Windows）

```powershell
# 方法 1：使用 PowerShell 設定（僅當前會話有效）
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"
$env:OPENAI_API_KEY="sk-your-api-key-here"

# 方法 2：使用批次檔（推薦）
# 建立 setenv.bat 並執行
.\setenv.bat
```

### 4. 編譯專案

```bash
# 清理並編譯
mvn clean compile

# 執行測試
mvn test
```

### 5. 啟動應用程式

```bash
# 方法 1：使用 Maven
mvn spring-boot:run

# 方法 2：使用 IDE
# 直接執行 SpringAiIntroApplication.java 的 main 方法
```

### 6. 測試 API

應用程式啟動後（預設端口：8080），可以使用以下方式測試：

#### 基本對話測試

```bash
# 使用 curl
curl "http://localhost:8080/api/ai/chat?prompt=Hello"

# 使用瀏覽器
http://localhost:8080/api/ai/chat?prompt=你好，請介紹Spring AI
```

#### 流式輸出測試

```bash
# 使用 curl
curl -H "Accept: text/event-stream" \
     "http://localhost:8080/api/ai/chat/stream?prompt=介紹Spring AI的主要功能"

# 使用前端範例
open frontend-demo/streaming-demo.html
```

---

## 📂 專案結構

```
chapter4-spring-ai-intro/
├── src/
│   ├── main/
│   │   ├── java/com/example/springai/
│   │   │   ├── SpringAiIntroApplication.java  # 主應用程式
│   │   │   ├── config/
│   │   │   │   └── AiConfig.java              # AI 配置（多模型、參數）
│   │   │   ├── controller/
│   │   │   │   ├── AiController.java          # 基本對話控制器
│   │   │   │   └── StreamingAiController.java # 流式輸出控制器
│   │   │   ├── service/
│   │   │   │   └── ChatModelService.java      # ChatModel 服務
│   │   │   └── dto/
│   │   │       └── ChatRequest.java           # 請求 DTO
│   │   └── resources/
│   │       ├── application.yml                 # 應用配置
│   │       └── application-dev.yml            # 開發環境配置
│   └── test/
│       └── java/com/example/springai/
│           └── SpringAiIntroApplicationTests.java
├── frontend-demo/                              # 前端範例
│   ├── index.html                             # EventSource 基礎範例
│   ├── streaming-demo.html                    # 完整流式聊天介面
│   ├── react-example/                         # React 整合範例
│   └── vue-example/                           # Vue 整合範例
├── pom.xml                                    # Maven 配置
├── .env.example                               # 環境變數範本
├── .gitignore                                 # Git 忽略配置
└── README.md                                  # 本說明文件
```

---

## 🎨 API 端點說明

### 基本對話 API

| 端點 | 方法 | 說明 | 章節 |
|------|------|------|------|
| `/api/ai/chat` | GET | 基本 AI 對話 | 4.2 |
| `/api/ai/chat/system` | POST | 帶系統提示詞的對話 | 4.2 |
| `/api/ai/hello-world` | GET | Hello World 範例 | 4.2 |

### 流式輸出 API

| 端點 | 方法 | 說明 | 章節 |
|------|------|------|------|
| `/api/ai/chat/stream` | GET | 基本流式對話（SSE） | 4.3 |
| `/api/ai/chat/stream/system` | GET | 帶系統提示詞流式對話 | 4.3 |
| `/api/ai/chat/stream/custom` | GET | 自定義流式處理 | 4.3 |

**詳細 API 文件**：請參閱 [docs/chapter4/api.md](../../docs/chapter4/api.md)

---

## 🔧 配置說明

### 應用程式配置（application.yml）

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini  # 模型選擇
          temperature: 0.7     # 創意度：0.0-2.0
          max-tokens: 1000     # 最大輸出長度
          stream: true         # 啟用流式輸出
```

### 模型選擇建議

| 模型 | 適用場景 | 成本 | 特點 |
|------|----------|------|------|
| **gpt-5-nano** | 開發測試 | 極低 | 💰 最經濟的入門選擇 |
| **gpt-4o-mini** | 一般對話 | 低 | ⚡ 性價比最佳 |
| **gpt-5-mini** | 商業應用 | 中 | 🎯 平衡性能與成本 |
| **gpt-5** | 複雜任務 | 高 | 🚀 最強性能 |
| **groq** | 開發測試 | 免費 | 🆓 免費高速 |

### 參數調校指南

| 參數 | 範圍 | 建議值 | 說明 |
|------|------|--------|------|
| temperature | 0.0-2.0 | 程式碼：0.2<br>對話：0.7<br>創意：1.2 | 控制輸出的隨機性 |
| max-tokens | 1-∞ | 對話：1000<br>文章：2000 | 限制輸出長度 |
| top-p | 0.0-1.0 | 0.9-1.0 | 核心採樣參數 |

---

## 💡 使用範例

### 範例 1：基本對話

```bash
curl "http://localhost:8080/api/ai/chat?prompt=什麼是Spring AI？"
```

### 範例 2：帶系統提示詞

```bash
curl -X POST "http://localhost:8080/api/ai/chat/system" \
  -H "Content-Type: application/json" \
  -d '{
    "systemMessage": "你是一個專業的 Java 開發專家",
    "userMessage": "如何使用 Spring Boot 建立 REST API？"
  }'
```

### 範例 3：流式輸出

```bash
curl -H "Accept: text/event-stream" \
     "http://localhost:8080/api/ai/chat/stream?prompt=請詳細介紹Spring AI的核心功能"
```

### 範例 4：前端整合

開啟 `frontend-demo/streaming-demo.html` 查看完整的流式聊天介面範例。

---

## 🧪 測試指南

### 單元測試

```bash
# 執行所有測試
mvn test

# 執行特定測試
mvn test -Dtest=SpringAiIntroApplicationTests
```

### 整合測試

```bash
# 1. 啟動應用程式
mvn spring-boot:run

# 2. 執行測試腳本
./test-api.sh  # Linux/Mac
.\test-api.bat # Windows
```

### 前端測試

1. 確保後端服務已啟動
2. 開啟瀏覽器訪問 `frontend-demo/` 目錄下的 HTML 檔案
3. 測試流式輸出功能

---

## 📖 相關文件

- **章節文件**：[docs/chapter4/](../../docs/chapter4/)
- **API 文件**：[docs/chapter4/api.md](../../docs/chapter4/api.md)
- **技術規格**：[docs/chapter4/spec.md](../../docs/chapter4/spec.md)
- **任務清單**：[docs/chapter4/todo.md](../../docs/chapter4/todo.md)
- **Spring AI 官網**：https://docs.spring.io/spring-ai/reference/

---

## ❓ 常見問題

### Q1: 編譯時出現 "package org.springframework.ai does not exist"

**A**: 請檢查：
1. 是否使用 Java 21 或以上版本
2. Maven 依賴是否正確下載（執行 `mvn dependency:resolve`）
3. 是否配置了 Spring Milestones Repository

### Q2: 啟動時出現 "Error creating bean with name 'chatClient'"

**A**: 請檢查：
1. 環境變數 `OPENAI_API_KEY` 是否已設定
2. API Key 是否有效
3. 網路連線是否正常

### Q3: API 回應錯誤 "Invalid API Key"

**A**: 請檢查：
1. API Key 格式是否正確（應以 `sk-` 開頭）
2. API Key 是否過期
3. OpenAI 帳號是否有足夠的額度

### Q4: 流式輸出無法正常顯示

**A**: 請檢查：
1. 瀏覽器是否支援 EventSource（所有現代瀏覽器都支援）
2. 請求 header 是否包含 `Accept: text/event-stream`
3. 後端是否正常啟動

### Q5: 如何降低 API 使用成本？

**A**: 建議：
1. 開發階段使用 Groq（免費）
2. 使用 `gpt-4o-mini` 或 `gpt-5-nano` 模型
3. 設定合理的 `max-tokens` 限制
4. 實作快取機制避免重複呼叫

---

## 🤝 貢獻指南

歡迎提出問題和建議！

1. Fork 本專案
2. 建立您的功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

---

## 📝 授權

本專案為教學範例程式碼，遵循 MIT License。

---

## 👨‍💻 作者

**Kevin Tsai（凱文大叔）**

- 部落格：[IT邦幫忙](https://ithelp.ithome.com.tw/users/20161290)
- Email: your-email@example.com

---

## 🙏 致謝

- [Spring AI Team](https://spring.io/projects/spring-ai)
- [OpenAI](https://openai.com/)
- [Groq](https://groq.com/)

---

**最後更新**：2025-10-24
**版本**：1.0.0
