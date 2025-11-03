# Chapter 9 - MCP Server Advanced Features

## 專案概述

這是 Spring AI MCP Server 進階功能展示專案，實作了：
- ✅ **提示系統 (Prompts)**: 使用 `@McpPrompt` 註解建立智能提示模板
- ✅ **自動完成 (Completions)**: 使用 `@McpComplete` 提供 URI 和參數補全
- ⚠️ **動態工具更新 (Dynamic Tools)**: 運行時動態添加工具（開發中）
- ✅ **資料庫整合 (JPA + H2)**: 使用 JPA 存儲動態提示和補全數據
- ✅ **客戶端通知 (Notifications)**: Progress、Logging 通知支援

## 重要提示：版本要求

⚠️ **此專案需要 Spring AI 1.1.0-SNAPSHOT 或更新版本**

MCP 的進階功能（`@McpPrompt`、`@McpComplete` 等註解）在 Spring AI 1.0.3 中不可用，需要使用 1.1.0-SNAPSHOT 版本。

### 使用的 MCP API

```java
// 正確的導入路徑（Spring AI 1.1.0-SNAPSHOT）
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpComplete;
```

## 專案結構

```
src/main/java/com/example/mcp/server/advanced/
├── entity/                    # 資料實體
│   ├── PromptTemplate.java    # 提示模板
│   └── CompletionData.java    # 補全數據
├── repository/                # JPA Repository
│   ├── PromptTemplateRepository.java
│   └── CompletionDataRepository.java
├── provider/                  # MCP 提供者
│   ├── PromptProvider.java            # 提示提供者
│   ├── DynamicPromptProvider.java     # 動態提示（資料庫驅動）
│   └── CompletionProvider.java        # 補全提供者
├── service/                   # 業務服務
│   └── DynamicToolManager.java        # 動態工具管理（開發中）
├── controller/                # REST 控制器
│   └── AdminController.java           # 管理API
└── McpServerAdvancedApplication.java  # 主程式
```

## 已實現的功能

### 1. 提示系統 (Prompts)

#### 基本提示
- `greeting`: 簡單問候
- `personalized-message`: 個性化訊息（帶年齡和興趣邏輯）
- `conversation-starter`: 多訊息對話流程
- `time-aware-greeting`: 時間感知問候

#### 動態提示（資料庫驅動）
- `dynamic-query`: 根據類別從資料庫載入提示
- `template-prompt`: 通過模板名稱載入提示
- `list-templates`: 列出所有可用模板

### 2. 自動完成 (Completions)

#### URI 補全
- `user-profile://{username}`: 用戶名補全
- `user-attribute://{username}/{attribute}`: 用戶屬性補全

#### Prompt 參數補全
- `travel-plan` 的 `country` 參數
- `code-review` 的 `language` 參數
- `personalized-message` 的 `name` 參數

### 3. 資料庫設計

#### 提示模板表 (prompt_template)
- `id`: 主鍵
- `name`: 模板名稱
- `description`: 描述
- `template`: 模板內容
- `parameters`: 參數列表

#### 補全數據表 (completion_data)
- `category`: 類別（主鍵）
- `values`: 補全值列表
- `description`: 描述
- `enabled`: 是否啟用

## 配置說明

### application.yml

```yaml
spring:
  application:
    name: mcp-server-advanced

  ai:
    mcp:
      server:
        name: advanced-server
        version: 1.0.0
        type: SYNC
        prompt-change-notification: true
        tool-change-notification: true

  datasource:
    url: jdbc:h2:mem:mcpdb
    driver-class-name: org.h2.Driver

  h2:
    console:
      enabled: true
      path: /h2-console

server:
  port: 8081
```

## 編譯和運行

### 環境要求
- JDK 21
- Maven 3.9+
- Spring AI 1.1.0-SNAPSHOT

### 編譯
```powershell
# 設定環境
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 編譯專案
mvn clean compile
```

或使用提供的腳本：
```powershell
.\compile.ps1
```

### 運行

#### 方式一：使用腳本（推薦）
```powershell
.\run.ps1
```

#### 方式二：手動運行
```powershell
# 設定環境
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 運行
mvn spring-boot:run
```

應用程式啟動後可訪問：
- **應用程式**: http://localhost:8081
- **H2 Console**: http://localhost:8081/h2-console
  - JDBC URL: `jdbc:h2:mem:mcpdb`
  - Username: `sa`
  - Password: (空白)
- **健康檢查**: http://localhost:8081/api/admin/health

## 管理 API

### 觸發動態工具更新
```bash
curl -X POST http://localhost:8081/api/admin/update-tools
```

### 查詢工具狀態
```bash
curl http://localhost:8081/api/admin/tools/status
```

### 重新載入補全數據
```bash
curl -X POST http://localhost:8081/api/admin/reload-completions
```

## 當前狀態

### ✅ 已完成
- [x] 專案結構建立
- [x] 資料模型和 Repository 層
- [x] 提示系統實作（4個基本提示）
- [x] 資料庫驅動的動態提示（3個提示）
- [x] 自動完成系統（URI 和 Prompt 補全）
- [x] 資料庫初始化和配置
- [x] 管理控制器和 REST API
- [x] Spring AI 1.1.0-SNAPSHOT 版本升級
- [x] 正確的 MCP API 導入路徑
- [x] DynamicToolManager 編譯問題修復
- [x] CompletionProvider 編譯問題修復
- [x] **編譯成功通過** ✨

### ⏳ 待完成
- [ ] 功能測試
  - [ ] 測試所有 Prompts
  - [ ] 測試 Completions
  - [ ] 測試動態工具更新
- [ ] 與 MCP Client 整合測試

## 編譯問題解決紀錄

### ✅ 已解決的問題

#### 1. CompleteCompletion 類別找不到
**問題**: `cannot find symbol: class CompleteCompletion`

**原因**: `CompleteCompletion` 是 `CompleteResult` 的內部類別，需要明確導入

**解決方案**:
```java
import io.modelcontextprotocol.spec.McpSchema.CompleteResult.CompleteCompletion;
```
並將代碼中的 `McpSchema.CompleteCompletion` 改為直接使用 `CompleteCompletion`

#### 2. SyncToolSpecification 方法不存在
**問題**: `cannot find symbol: method name()` 和 `method description()`

**原因**: `SyncToolSpecification` 沒有 `name()` 和 `description()` 方法

**解決方案**: 參考官方範例，直接輸出 tool 物件本身：
```java
log.info("添加新工具: {}", tool);
```

#### 3. Lambda 表達式變數 final 問題
**問題**: `local variables referenced from a lambda expression must be final or effectively final`

**原因**: `prefix` 變數在 lambda 表達式前被多次重新賦值

**解決方案**: 使用 `final` 變數：
```java
String rawPrefix = ...;
final String prefix = rawPrefix.toLowerCase();
```

### ⚠️ 注意事項

#### SNAPSHOT 版本依賴
**問題**: 使用 SNAPSHOT 版本可能導致 API 不穩定

**建議**: 等待 Spring AI 1.1.0 正式發布後再用於生產環境

## 參考資料

### 官方文檔
- [Spring AI MCP 文檔](https://docs.spring.io/spring-ai/reference/api/mcp/)
- [MCP 協議規範](https://spec.modelcontextprotocol.io/)

### 官方範例
- **mcp-annotations**: MCP 註解使用示範
  - 位置: `D:\GitHub\spring-ai-examples\model-context-protocol\mcp-annotations`
  - 包含: Prompts、Completions、Tools、Resources
- **dynamic-tool-update**: 動態工具更新範例
- **sampling**: Sampling 功能範例

### 技術棧
- Spring Boot 3.3.6
- Spring AI 1.1.0-SNAPSHOT
- Spring Data JPA
- H2 Database
- Lombok

## 下一步

1. **解決編譯問題**
   - 查看官方 dynamic-tool-update 範例
   - 修正 DynamicToolManager 的實現

2. **完成測試**
   - 使用 MCP Client 連接測試
   - 驗證所有 Prompts 和 Completions

3. **文檔完善**
   - 添加使用範例
   - 編寫 API 文檔

4. **考慮版本遷移**
   - 評估是否等待 Spring AI 1.1.0 正式版
   - 或暫時使用 Spring AI 1.0.3 的基礎功能

## 授權

Apache License 2.0

## 作者

Spring AI Book 專案團隊

---

**最後更新**: 2025-11-02
**Spring AI 版本**: 1.1.0-SNAPSHOT
**編譯狀態**: ✅ 成功 (BUILD SUCCESS)
**專案狀態**: 開發完成，待功能測試
