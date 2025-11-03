# Chapter 9 - MCP Client 基礎應用

這是Spring AI書籍第9章的第一個專案範例，展示如何使用 Spring AI MCP Client 連接和調用 MCP Server。

## 📚 學習目標

- 理解 MCP (Model Context Protocol) 的基本概念
- 學會配置 STDIO 和 SSE 兩種傳輸方式
- 將 MCP 工具整合到 Spring AI ChatClient
- 實際調用 Context7 公開的 MCP Server

## 🔧 技術棧

- **Spring Boot**: 3.5.7
- **Spring AI**: 1.0.3
- **Java**: 21+
- **Maven**: 3.9.11
- **MCP SDK**: 0.10.0

## 📋 前置需求

### 必需
1. **Java 21+**: 確保已安裝 Java 21 或更高版本
2. **OpenAI API Key**: 需要設置環境變數 `OPENAI_API_KEY`

### 可選
3. **Node.js 18+**: 如果要使用 STDIO 方式連接 Brave Search Server
4. **Brave API Key**: 如果使用 Brave Search，需要設置 `BRAVE_API_KEY`

## 🚀 快速開始

### 1. 設置環境變數

**Windows (PowerShell)**:
```powershell
$env:OPENAI_API_KEY="your-openai-api-key"
$env:BRAVE_API_KEY="your-brave-api-key"  # 可選
```

**Linux/Mac**:
```bash
export OPENAI_API_KEY=your-openai-api-key
export BRAVE_API_KEY=your-brave-api-key  # 可選
```

### 2. 編譯專案

```bash
# 確保使用Java 21
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 編譯專案
mvn clean compile
```

### 3. 運行應用

```bash
mvn spring-boot:run
```

### 4. 與AI對話

應用啟動後，會顯示已連接的 Server 和可用的工具，然後進入交互式對話模式：

```
You: How to use React hooks?
AI: [AI會使用Context7工具查詢React文檔並回答]
```

輸入 `exit` 或 `quit` 退出程序。

## 🔗 MCP Server 配置

### Context7 (SSE傳輸)

Context7 是一個公開的 MCP Server，提供程式庫文檔檢索功能：

- **URL**: https://mcp.context7.com/mcp
- **傳輸方式**: SSE (Server-Sent Events)
- **無需認證**: 直接可用

**提供的工具**:
- `resolve-library-id`: 解析程式庫名稱到 Context7 ID
- `get-library-docs`: 獲取程式庫文檔

### Brave Search (STDIO傳輸)

Brave Search 是通過 Node.js 運行的本地 MCP Server：

- **命令**: `npx -y @modelcontextprotocol/server-brave-search`
- **傳輸方式**: STDIO (標準輸入輸出)
- **需要 API Key**: 設置 `BRAVE_API_KEY` 環境變數

**提供的工具**:
- `brave_web_search`: 網頁搜索

## 📁 專案結構

```
src/main/java/com/example/mcpclient/
├── cli/                    # CLI命令行界面
│   └── CliRunner.java
├── config/                 # 配置類
│   ├── ChatClientConfig.java
│   └── McpClientManager.java
├── dto/                    # 資料傳輸對象
│   ├── ChatRequest.java
│   └── ChatResponse.java
├── model/                  # 領域模型
│   ├── ResourceInfo.java
│   ├── ServerCapabilities.java
│   ├── ServerInfoResponse.java
│   └── ToolInfo.java
├── service/                # 業務邏輯層
│   ├── McpClientService.java
│   ├── McpResourceService.java
│   └── McpToolService.java
└── McpClientApplication.java  # 主程序入口

src/main/resources/
└── application.yml         # 應用配置
```

## 💡 使用範例

### 查詢程式庫文檔

```
You: How to use Spring Boot autoconfiguration?
```

AI 會：
1. 使用 `resolve-library-id` 解析 "spring-boot"
2. 使用 `get-library-docs` 獲取文檔
3. 基於文檔生成回答

### 網頁搜索 (需要 Brave API Key)

```
You: What's the latest news about AI?
```

AI 會：
1. 使用 `brave_web_search` 搜索最新資訊
2. 整理搜索結果並回答

## 🔍 故障排除

### 問題1：找不到 MCP Server

**症狀**: 啟動時顯示"未檢測到任何MCP Server連接"

**解決方案**:
1. 檢查 `application.yml` 配置是否正確
2. 如果使用 STDIO，確保已安裝 Node.js
3. 檢查網絡連接（Context7需要網絡訪問）

### 問題2：OpenAI API 錯誤

**症狀**: 對話時出現 API 錯誤

**解決方案**:
1. 確認 `OPENAI_API_KEY` 已正確設置
2. 檢查 API Key 是否有效
3. 確認帳戶有足夠的額度

### 問題3：編譯錯誤

**症狀**: Maven 編譯失敗

**解決方案**:
1. 確認使用 Java 21：`java -version`
2. 檢查 Maven 版本：`mvn -version`
3. 清除Maven快取：`mvn clean`

## 📖 相關文檔

- [Spring AI 官方文檔](https://docs.spring.io/spring-ai/reference/)
- [MCP 協議規範](https://spec.modelcontextprotocol.io/)
- [Context7 MCP Server](https://mcp.context7.com/mcp)

## 📝 注意事項

1. **API 成本**: 每次對話都會調用 OpenAI API，注意成本控制
2. **網絡要求**: Context7 需要網絡連接
3. **Node.js**: STDIO 傳輸方式需要 Node.js 環境
4. **Java 版本**: 必須使用 Java 21+

## 🤝 貢獻

歡迎提交 Issue 和 Pull Request！

## 📄 授權

本專案為教學範例，遵循 MIT 授權條款。
