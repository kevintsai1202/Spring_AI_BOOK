# Spring AI MCP 專案總覽

> **專案目標**：通過 3 個核心專案，完整展示 Spring AI 1.0.3 中 Model Context Protocol (MCP) 的使用和開發，從基礎概念到進階應用，並提供企業整合最佳實踐指南。

## 📚 專案結構

本章節包含 **3 個核心專案** + **企業整合最佳實踐附錄**：

```
chapter9-mcp-integration/
├── PROJECT_OVERVIEW.md                          # 專案總覽（本檔案）
├── MCP_API_正確寫法.md                           # MCP API 參考指南
├── 官方範例對照.md                               # 官方範例對照表
├── chapter9-mcp-client-basic/                   # 專案1：MCP Client 基礎應用
│   ├── README.md
│   └── spec.md                                  # 完整規格文件
├── chapter9-mcp-server-tools-resources/         # 專案2：MCP Server - 工具與資源
│   ├── README.md
│   └── spec.md
├── chapter9-mcp-server-advanced/                # 專案3：MCP Server - 提示與動態功能
│   ├── README.md
│   └── spec.md
└── docs/
    └── enterprise-integration-guide.md          # 附錄：企業整合最佳實踐指南
```

## 🎯 專案規劃

### 專案 1：MCP Client 基礎 (chapter9-mcp-client-basic)

**對應章節**：9.1、9.2
**學習重點**：理解 MCP 協議、學習 MCP Client 的使用

**核心功能**：
- MCP 協議基礎理解
- 連接到多個 MCP Server（STDIO 和 SSE 傳輸）
- 工具發現和調用
- 資源讀取和管理
- 提示系統使用
- 與 Spring AI ChatClient 整合

**技術棧**：
- Spring Boot 3.5.7
- Spring AI 1.0.3
- spring-ai-starter-mcp-client
- spring-ai-starter-openai

**展示重點**：
- STDIO 傳輸連接本地 MCP Server
- SSE (HTTP) 傳輸連接遠端 MCP Server
- 動態工具發現和調用
- 多 Server 管理
- 與 AI 對話的整合

---

### 專案 2：MCP Server - 工具與資源 (chapter9-mcp-server-tools-resources)

**對應章節**：9.4（基礎部分）
**學習重點**：學習開發 MCP Server 的核心功能 - 工具(Tools)和資源(Resources)

**核心功能**：
- 建立基本的 MCP Server (STDIO + SSE 雙傳輸)
- 使用 `@Tool` 和 `@McpTool` 註解開發工具
- 提供靜態資源 (`@McpResource`)
- 提供動態資源（URI 模板變數）
- 工具參數驗證和錯誤處理
- Server 信息和能力配置

**工具示例**：
- **天氣查詢工具**: 調用 Open-Meteo API 獲取溫度
- **數學計算工具**: 加法、乘法、除法運算
- **文本處理工具**: 大小寫轉換、字數統計
- **時間工具**: 獲取當前時間、時區轉換

**資源示例**：
- **用戶資料資源**: `user-profile://{username}` - 完整用戶資訊
- **用戶屬性資源**: `user-attribute://{username}/{attribute}` - 特定屬性
- **配置文件資源**: 靜態應用配置文件
- **日誌文件資源**: 動態讀取應用日誌

**技術棧**：
- Spring Boot 3.5.7
- Spring AI 1.0.3
- spring-ai-starter-mcp-server-webmvc
- Lombok (簡化程式碼)

**展示重點**：
- 混用 Spring AI `@Tool` 和 MCP `@McpTool` 註解
- URI 模板變數自動提取
- 資源內容類型設定 (text/plain, application/json)
- 工具調用進度追蹤 (ProgressToken)
- STDIO 和 SSE 雙傳輸模式切換

**參考官方範例**:
- `weather/starter-webmvc-server`
- `mcp-annotations/mcp-annotations-server` (Tools & Resources 部分)

---

### 專案 3：MCP Server - 提示與動態功能 (chapter9-mcp-server-advanced)

**對應章節**：9.4（進階部分）
**學習重點**：掌握 MCP 的進階特性 - 提示系統(Prompts)、自動完成(Completions)和動態工具更新

**核心功能**：
- **提示系統**: 使用 `@McpPrompt` 建立智能提示模板
- **自動完成**: 使用 `@McpComplete` 提供 URI 和參數補全
- **動態工具更新**: 運行時動態添加/移除工具
- **客戶端處理器**: Progress、Logging、Sampling 通知
- **Server Exchange**: 進階 Server 操作(日誌、進度通知)
- **資料庫整合**: 使用 JPA 存儲動態提示

**提示範例**：
- **greeting**: 簡單問候提示（帶姓名參數）
- **personalized-message**: 個性化訊息（姓名、年齡、興趣）
- **conversation-starter**: 多訊息對話流程
- **dynamic-query**: 資料庫驅動的動態提示

**完成建議範例**：
- **用戶名補全**: 為 `user-profile://{username}` URI 提供補全
- **國家名補全**: 為旅遊提示提供國家名稱建議
- **屬性補全**: 為用戶屬性查詢提供屬性名稱補全

**動態工具範例**：
- 初始工具：天氣預報工具
- 動態添加：數學運算工具集 (sum, multiply, divide)
- 工具變更通知：通知客戶端工具列表已更新

**技術棧**：
- Spring Boot 3.5.7
- Spring AI 1.0.3
- spring-ai-starter-mcp-server-webmvc
- Spring Data JPA (動態提示存儲)
- H2 Database (嵌入式資料庫)
- Lombok

**展示重點**：
- 複雜提示參數處理（必填/選填、類型轉換）
- 提示內邏輯判斷（根據參數生成不同內容）
- Server Exchange 的 Logging 和 Progress 通知
- URI 補全和 Prompt 參數補全的區別
- 動態工具註冊和工具變更通知機制
- 客戶端處理器註解 (`@McpProgress`, `@McpLogging`, `@McpSampling`)

**參考官方範例**:
- `mcp-annotations/mcp-annotations-server` (Prompts & Completions 部分)
- `dynamic-tool-update` (動態工具更新)
- `sampling` (Sampling 功能)
- `mcp-annotations/mcp-annotations-client` (客戶端處理器)

---

## 📘 附錄：企業整合最佳實踐指南

**對應章節**：9.5 (企業整合)
**目標**: 提供企業級 MCP 應用的架構設計和實施指南

本附錄不是獨立專案，而是基於前 3 個專案的知識，提供企業環境中部署和整合 MCP 的最佳實踐。

**涵蓋主題**：

### 1. 安全性
- **OAuth2/JWT 認證**: 整合 Spring Security 保護 MCP Server
- **API Key 管理**: 安全存儲和輪換 API Keys
- **RBAC 授權**: 基於角色的工具訪問控制
- **傳輸加密**: HTTPS/TLS 配置

**參考官方範例**: `weather/starter-webmvc-oauth2-server`

### 2. 性能優化
- **連接池管理**: 優化 STDIO 進程和 HTTP 連接
- **工具調用緩存**: 使用 Spring Cache 緩存結果
- **異步處理**: WebFlux 反應式架構
- **批量操作**: 批量調用多個工具

**參考官方範例**: `client-starter/starter-webflux-client`

### 3. 監控與可觀測性
- **健康檢查**: Spring Boot Actuator 整合
- **指標收集**: Micrometer + Prometheus
- **分散式追蹤**: Spring Cloud Sleuth
- **日誌聚合**: ELK Stack 整合

### 4. 部署策略
- **容器化**: Docker + Docker Compose
- **服務發現**: Eureka/Consul 整合
- **負載均衡**: 多實例部署
- **配置管理**: Spring Cloud Config

**參考官方範例**: `brave-docker-agents-gateway`

### 5. 實戰場景案例
- **智能客服系統**: 整合多個 MCP Server 提供客戶支援
- **開發輔助平台**: 程式碼搜索、文檔查詢、API 測試
- **企業知識管理**: 文檔檢索、問答系統

**文檔位置**: `docs/enterprise-integration-guide.md`

---

## 🔗 專案關聯性與學習路徑

```
┌──────────────────────────────────────────────────────────────┐
│                    學習路徑 (方案 A)                           │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  📖 9.1 章節: MCP 協議基礎                                    │
│  └─> 理解 MCP 核心概念、架構、傳輸方式                        │
│                                                              │
│  💻 專案1: MCP Client 基礎應用                                │
│  └─> 學習使用 MCP Client 連接和調用 Server                   │
│       ├─ STDIO 傳輸 (本地進程)                               │
│       ├─ SSE 傳輸 (HTTP)                                     │
│       └─ 與 ChatClient 整合                                  │
│           ↓                                                  │
│                                                              │
│  📖 9.2 章節: MCP Server 開發基礎                             │
│  └─> 學習 Tools 和 Resources 的開發方法                       │
│                                                              │
│  🛠️  專案2: MCP Server - 工具與資源                          │
│  └─> 開發提供工具和資源的 MCP Server                          │
│       ├─ @Tool 和 @McpTool 註解                              │
│       ├─ @McpResource 靜態/動態資源                          │
│       └─ 雙傳輸模式支援                                       │
│           ↓                                                  │
│                                                              │
│  📖 9.3 章節: MCP 進階特性                                    │
│  └─> 學習 Prompts、Completions、動態更新                      │
│                                                              │
│  🚀 專案3: MCP Server - 提示與動態功能                        │
│  └─> 掌握 MCP 的進階和獨特功能                                │
│       ├─ @McpPrompt 智能提示                                 │
│       ├─ @McpComplete 自動完成                               │
│       ├─ 動態工具更新                                         │
│       └─ 客戶端處理器 (Progress/Logging/Sampling)             │
│           ↓                                                  │
│                                                              │
│  📘 9.5 附錄: 企業整合最佳實踐                                 │
│  └─> 安全性、性能、監控、部署策略                              │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

## 📖 章節對應關係

| 專案/文檔 | 對應章節 | 主要內容 | 學習目標 |
|----------|---------|---------|---------|
| **專案1** | 9.1, 9.2 | MCP Client 基礎應用 | 理解協議、學會使用 |
| **專案2** | 9.4 (基礎) | Server 工具與資源開發 | 掌握 Tools & Resources |
| **專案3** | 9.4 (進階) | Server 提示與動態功能 | 掌握進階特性 |
| **附錄** | 9.5 | 企業整合最佳實踐 | 生產環境部署 |

## 🚀 學習建議

### 推薦學習順序 (方案 A)

#### 階段 1: 基礎理解 (1-2天)
1. **閱讀 9.1 章節** (1小時)
   - 理解 MCP 協議的核心概念
   - 了解 Client-Server 架構
   - 學習 STDIO 和 SSE 兩種傳輸方式的區別

2. **運行專案1** (2-3小時)
   - 配置並連接到外部 MCP Server (如 Brave Search)
   - 體驗工具發現和調用流程
   - 觀察 ChatClient 如何與 MCP 整合
   - **實踐重點**: 理解 MCP Client 的配置和使用模式

#### 階段 2: Server 基礎 (2-3天)
3. **閱讀 9.2 章節** (1小時)
   - 學習如何開發 MCP Server
   - 理解 Tools 和 Resources 的區別
   - 掌握註解驅動開發模式

4. **運行專案2** (3-4小時)
   - 開發自己的工具 (`@Tool`, `@McpTool`)
   - 提供靜態和動態資源 (`@McpResource`)
   - 測試 STDIO 和 SSE 兩種傳輸模式
   - **實踐重點**: 掌握 Server 的核心功能開發

#### 階段 3: 進階特性 (2-3天)
5. **閱讀 9.3 章節** (1.5小時)
   - 學習 Prompts 系統的設計思想
   - 理解 Completions 的應用場景
   - 掌握動態工具更新的機制

6. **運行專案3** (4-5小時)
   - 開發智能提示模板 (`@McpPrompt`)
   - 實現自動完成功能 (`@McpComplete`)
   - 體驗動態工具註冊和工具變更通知
   - 配置客戶端處理器 (Progress, Logging, Sampling)
   - **實踐重點**: 掌握 MCP 的獨特價值和進階功能

#### 階段 4: 企業實踐 (1天)
7. **閱讀 9.5 附錄** (2小時)
   - 了解生產環境的安全性考量
   - 學習性能優化策略
   - 掌握監控和部署最佳實踐
   - **實踐重點**: 為實際專案做好準備

### 各專案學習重點總結

| 專案 | 時間投入 | 核心學習目標 | 關鍵技能 |
|------|---------|------------|---------|
| **專案1** | 2-3小時 | 理解協議、學會使用 | MCP Client 配置、工具調用 |
| **專案2** | 3-4小時 | 開發 Server 基礎功能 | `@Tool`, `@McpTool`, `@McpResource` |
| **專案3** | 4-5小時 | 掌握進階和獨特功能 | `@McpPrompt`, `@McpComplete`, 動態更新 |
| **附錄** | 2小時閱讀 | 企業級最佳實踐 | 安全、性能、監控、部署 |

### 學習成果檢驗

完成 3 個專案後，您應該能夠：

✅ **專案1 後**:
- 配置 MCP Client 連接多個 Server
- 理解 STDIO 和 SSE 傳輸的使用場景
- 將 MCP 工具整合到 Spring AI ChatClient

✅ **專案2 後**:
- 獨立開發 MCP Server
- 使用註解方式開發工具和資源
- 處理工具參數驗證和錯誤處理
- 提供動態資源(URI 模板變數)

✅ **專案3 後**:
- 設計和實現智能提示系統
- 提供自動完成建議
- 實現運行時動態工具更新
- 配置客戶端處理器接收通知

✅ **閱讀附錄後**:
- 理解企業級 MCP 應用的架構設計
- 掌握安全性、性能、監控的最佳實踐
- 知道如何將 MCP 部署到生產環境

## 📋 技術要求

### 開發環境

- **JDK**: 21+
- **Maven**: 3.9+
- **Spring Boot**: 3.5.7
- **Spring AI**: 1.0.3
- **IDE**: IntelliJ IDEA 或 VS Code

### 運行環境

- **作業系統**: Windows 11 / Linux / macOS
- **記憶體**: 最少 4GB（建議 8GB+）
- **Docker**: （專案4 需要）
- **Node.js**: 用於運行某些 MCP Server 示例

### API Keys

- **OpenAI API Key**: 用於 AI 對話功能
- 設定環境變數：`OPENAI_API_KEY=your-api-key`

## 📝 專案狀態

| 專案/文檔 | 狀態 | 進度 | 預計完成時間 |
|----------|-----|------|------------|
| **專案1**: MCP Client 基礎應用 | 📋 規劃中 | 0% | 待定 |
| **專案2**: MCP Server - 工具與資源 | 📋 規劃中 | 0% | 待定 |
| **專案3**: MCP Server - 提示與動態功能 | 📋 規劃中 | 0% | 待定 |
| **附錄**: 企業整合最佳實踐指南 | 📋 規劃中 | 0% | 待定 |

### 開發計劃

**第一階段**: 建立專案骨架和規格文件 (1-2天)
- [ ] 為專案1創建詳細的 spec.md
- [ ] 為專案2創建詳細的 spec.md
- [ ] 為專案3創建詳細的 spec.md
- [ ] 撰寫企業整合最佳實踐指南大綱

**第二階段**: 專案開發 (每個專案 1-2天)
- [ ] 開發並測試專案1
- [ ] 開發並測試專案2
- [ ] 開發並測試專案3

**第三階段**: 文檔完善 (1天)
- [ ] 完成企業整合最佳實踐指南
- [ ] 為每個專案撰寫詳細的 README
- [ ] 添加程式碼註解和範例

## 🔍 學習成果與價值

完成方案 A 的 3 個核心專案後，您將能夠：

### 技術能力
✅ **完全理解 MCP 協議的工作原理**
- Client-Server 通訊機制
- STDIO 和 SSE 兩種傳輸方式的應用場景
- 工具、資源、提示的完整生命週期

✅ **獨立開發 MCP Client 應用**
- 配置多個 MCP Server 連接
- 整合到 Spring AI ChatClient
- 處理工具調用和資源讀取

✅ **開發各種類型的 MCP Server**
- 使用註解驅動開發 (`@McpTool`, `@McpResource`, `@McpPrompt`, `@McpComplete`)
- 實現靜態和動態資源提供
- 支援 STDIO 和 SSE 雙傳輸模式

✅ **掌握 MCP 的進階和獨特功能**
- 智能提示系統設計
- 自動完成功能實現
- 動態工具更新機制
- 客戶端處理器配置

✅ **整合 MCP 到現有的 Spring AI 應用中**
- 與現有工具系統共存
- 混合使用 Spring AI 和 MCP 工具
- 企業級部署和優化

### 實戰能力
✅ **可以直接應用到實際專案**
- 智能客服系統
- 企業知識管理平台
- 開發輔助工具集
- API 測試和監控工具

### 對比優勢
與原 4 專案方案相比，方案 A 的優勢：
- ✅ **更聚焦**: 專注於 MCP 核心功能，不被企業基礎設施分散注意力
- ✅ **更高效**: 3 個專案即可掌握所有核心知識點
- ✅ **更實用**: 附錄形式的企業指南更靈活，可根據需求選讀
- ✅ **更易維護**: 專案數量少，文檔更新和維護成本低

---

## 📚 相關資源

### 官方文檔
- [Spring AI MCP 文檔](https://docs.spring.io/spring-ai/reference/api/mcp/)
- [MCP 協議規範](https://spec.modelcontextprotocol.io/)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)

### 官方範例參考
本專案參考了以下官方範例:
- `client-starter/starter-default-client` - Client 配置模式
- `weather/starter-webmvc-server` - Server 基礎開發
- `mcp-annotations` - 完整註解使用示範
- `dynamic-tool-update` - 動態工具更新
- `sampling` - Sampling 功能
- `weather/starter-webmvc-oauth2-server` - OAuth2 安全性

### 下一步行動

**立即開始**: 請查看各專案的 `spec.md` 文件了解詳細的技術規格和實現計劃。

**建議順序**:
1. 閱讀 `chapter9-mcp-client-basic/spec.md`
2. 閱讀 `chapter9-mcp-server-tools-resources/spec.md`
3. 閱讀 `chapter9-mcp-server-advanced/spec.md`
4. 閱讀 `docs/enterprise-integration-guide.md`

---

**專案維護**: 本文檔持續更新中，最後更新時間: 2025-10-31
