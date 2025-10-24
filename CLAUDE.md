# Spring AI Book 專案配置

## 開發環境要求

### Java 版本
- **使用版本**: Java 21
- **安裝路徑**: `D:\java\jdk-21`
- **環境變數設定**:
  ```powershell
  $env:JAVA_HOME="D:\java\jdk-21"
  $env:Path="D:\java\jdk-21\bin;$env:Path"
  ```

### Maven 版本
- **使用版本**: Maven 3.9+
- **當前安裝**: Maven 3.9.11
- **安裝路徑**: `D:\apache-maven-3.9.11`

## 專案結構

```
E:\Spring_AI_BOOK\
├── code-examples/
│   ├── chapter0-prerequisite/          # 第0章：環境準備
│   ├── chapter1-spring-boot-basics/    # 第1章：Spring Boot 基礎
│   ├── chapter2-spring-mvc-api/        # 第2章：Spring MVC API
│   ├── chapter3-enterprise-features/   # 第3章：企業級功能（資料驗證、檔案處理）
│   ├── chapter4-spring-ai-intro/       # 第4章：Spring AI 入門
│   ├── chapter5-spring-ai-advanced/    # 第5章：Spring AI 進階
│   ├── chapter6-ai-memory/             # 第6章：AI 記憶增強
│   ├── chapter7-rag-basic/             # 第7章：RAG 基礎
│   ├── chapter8-rag-advanced/          # 第8章：RAG 進階
│   └── chapter9-mcp-integration/       # 第9章：MCP 整合
└── docs/
    ├── chapter0/
    ├── chapter1/
    ├── chapter2/
    ├── chapter3/
    ├── chapter4/
    ├── chapter5/
    ├── chapter6/
    ├── chapter7/
    ├── chapter8/
    └── chapter9/
```

## 編譯和執行命令

### 設定環境變數後編譯
```powershell
# 1. 設定 Java 21 環境變數
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 2. 進入專案目錄
cd "E:\Spring_AI_BOOK\code-examples\chapter3-enterprise-features"

# 3. 編譯專案
mvn clean compile

# 4. 執行應用程式
mvn spring-boot:run
```

## 章節技術棧

### 第0-3章（Spring Boot 基礎與企業級功能）
- Spring Boot 3.2.0
- Spring Framework 6.1.1
- Java 21
- Maven 3.9+

### 第4-9章（Spring AI 與 RAG）
- Spring Boot 3.2.0+
- Spring AI（版本待確認）
- Java 21
- Maven 3.9+

## 重要提醒

1. **所有章節專案都使用 Java 21**，執行前必須設定 JAVA_HOME
2. 預設系統使用 Java 8，需要明確切換到 Java 21
3. 每次開啟新的 PowerShell 視窗都需要重新設定環境變數
4. 建議使用 PowerShell 7+ 執行命令

## 專案狀態

- ✅ 第0章：環境準備（已完成）
- ✅ 第1章：Spring Boot 基礎（已完成並測試）
- ✅ 第2章：Spring MVC API（已完成並測試）
- 🔄 第3章：企業級功能（程式碼已建立，待編譯測試）
- ⏳ 第4章：Spring AI 入門（待開發）
- ⏳ 第5章：Spring AI 進階（待開發）
- ⏳ 第6章：AI 記憶增強（待開發）
- ⏳ 第7章：RAG 基礎（待開發）
- ⏳ 第8章：RAG 進階（待開發）
- ⏳ 第9章：MCP 整合（待開發）
