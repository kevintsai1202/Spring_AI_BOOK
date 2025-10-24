# Spring AI 技術手冊 - 文件目錄

本目錄包含所有章節的精簡版文件，完整的可執行程式碼位於 `code-examples/` 目錄。

## 📖 文件結構

### 已完成章節

- [✅ 第1章：Spring Boot 基礎](./chapter1/) - 快速入門、專案架構、依賴注入
- [✅ 第2章：Spring MVC 與 RESTful API](./chapter2/) - Web 應用開發核心
- [✅ 第3章：企業級功能開發](./chapter3/) - 資料驗證、檔案處理、API 文件化

### 待完成章節

- [ ] 第4章：Spring AI 入門
- [ ] 第5章：Spring AI 進階
- [ ] 第6章：AI 記憶增強
- [ ] 第7章：RAG 基礎實作
- [ ] 第8章：RAG 進階技術
- [ ] 第9章：MCP 整合

## 💻 程式碼範例

所有章節的完整可執行程式碼都在 `code-examples/` 目錄：

```
E:\Spring_AI_BOOK\
├── code-examples/              # 完整可執行程式碼
│   ├── chapter1-spring-boot-basics/       # ✅ 已完成並測試
│   ├── chapter2-spring-mvc-api/           # ✅ 已完成並測試
│   ├── chapter3-enterprise-features/      # ✅ 已完成並測試
│   ├── chapter4-spring-ai-intro/          # ⏳ 待開發
│   ├── chapter5-spring-ai-advanced/       # ⏳ 待開發
│   ├── chapter6-ai-memory/                # ⏳ 待開發
│   ├── chapter7-rag-basic/                # ⏳ 待開發
│   ├── chapter8-rag-advanced/             # ⏳ 待開發
│   └── chapter9-mcp-integration/          # ⏳ 待開發
└── docs/                       # 精簡版文件
    ├── chapter1/              # ✅ 已精簡
    ├── chapter2/              # ✅ 已精簡
    ├── chapter3/              # ✅ 已精簡
    └── chapter4-9/            # ⏳ 待處理
```

## 📸 截圖需求

每個章節都需要相應的截圖來說明操作步驟和執行結果。詳細的截圖清單請參考各章節 `images/README.md`：

- [第1章截圖需求](./chapter1/images/README.md) - 4張（STS4 安裝、專案建立等）
- [第2章截圖需求](./chapter2/images/README.md) - 7張（MVC 架構、API 測試等）
- [第3章截圖需求](./chapter3/images/README.md) - 10張（驗證、檔案上傳、Swagger UI 等）

## 🎯 文件精簡策略

為了提升閱讀體驗和降低文件維護成本，我們採用以下策略：

### ✅ 保留的內容
- 理論和概念說明
- 學習目標和重點回顧
- 架構設計思想
- 最佳實踐建議
- 關鍵的小型程式碼片段（用於說明概念）

### ❌ 移除的內容
- 完整的程式碼區塊（改為參考連結）
- 重複的配置檔案內容
- 冗長的程式碼範例

### 🔗 添加的內容
- 指向 code-examples 的參考連結
- 快速開始指南
- API 測試範例
- 執行與驗證步驟
- 截圖和視覺化說明

## 🚀 快速導航

### 適合初學者
1. [第1章 - Spring Boot 快速入門](./chapter1/1.1-spring-boot-quickstart.md)
2. [第2章 - RESTful API 開發](./chapter2/)
3. [第3章 - 企業級功能](./chapter3/)

### 完整學習路徑
```
第1章（基礎）
  ↓
第2章（Web API）
  ↓
第3章（企業功能）
  ↓
第4-5章（Spring AI 基礎與進階）
  ↓
第6章（AI 記憶）
  ↓
第7-8章（RAG 技術）
  ↓
第9章（MCP 整合）
```

## 📋 環境需求

### 開發工具
- JDK 17 或 21
- Maven 3.9+
- STS4 或 IntelliJ IDEA
- Postman（API 測試）

### 重要配置

⚠️ **Java 版本設定**（重要）
```powershell
# 確認 Java 版本
java -version

# 設定 JAVA_HOME（如需要）
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"
```

詳細配置請參考 [CLAUDE.md](../CLAUDE.md)

## 🔧 執行範例專案

### 通用步驟

```bash
# 1. 進入專案目錄
cd code-examples/chapter{N}-{name}

# 2. 編譯專案
mvn clean compile

# 3. 執行應用程式
mvn spring-boot:run

# 或使用提供的批次檔（適用於 Windows）
.\run.bat
```

### 驗證執行

應用程式啟動後：
- 查看控制台日誌
- 訪問 API 端點
- 使用 Postman 測試
- 檢查 Swagger UI（如適用）

## 📚 相關資源

### 官方文件
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/)
- [Spring Framework Reference](https://docs.spring.io/spring-framework/)

### 社群資源
- [Spring Guides](https://spring.io/guides)
- [Spring Blog](https://spring.io/blog)
- [Spring GitHub](https://github.com/spring-projects)

## 📝 貢獻指南

### 添加截圖

1. 按照各章節 `images/README.md` 的說明製作截圖
2. 將截圖放置在對應的 `images/` 目錄
3. 確保檔案名稱與文件引用一致

### 更新文件

1. 保持精簡原則
2. 更新參考連結
3. 確保程式碼範例可執行
4. 添加必要的視覺化說明

## 🎓 學習建議

1. **循序漸進**：從第1章開始，按順序學習
2. **動手實作**：運行每個範例專案，親自測試 API
3. **理解原理**：不只是看程式碼，更要理解背後的設計思想
4. **參考文件**：遇到問題時查閱官方文件和本文件的說明
5. **截圖記錄**：將重要的操作步驟和結果截圖保存

---

## 聯絡資訊

如有問題或建議，請參考專案 README 或相關文件。
