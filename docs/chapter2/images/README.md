# 第2章所需截圖清單

本目錄包含第2章文件中引用的所有截圖。

## 2.1 Spring MVC 核心概念

### 需要的截圖

1. **2.1-mvc-architecture.png**
   - 說明：MVC 架構圖
   - 內容：顯示 Model、View、Controller 三層架構及其交互關係
   - 建議：使用流程圖或架構圖工具繪製（如 draw.io、Lucidchart）
   - 尺寸建議：800x600

2. **2.1-dispatcher-servlet.png**
   - 說明：DispatcherServlet 工作流程圖
   - 內容：展示請求從進入到回應的完整處理流程
   - 建議：包含 HandlerMapping、Controller、ViewResolver 等元件
   - 尺寸建議：1000x700

## 2.2 RESTful API 設計

### 需要的截圖

3. **2.2-rest-principles.png**
   - 說明：REST 設計原則示意圖
   - 內容：展示 REST 的核心原則（無狀態、統一介面、資源導向等）
   - 建議：使用圖表或心智圖呈現
   - 尺寸建議：800x600

4. **2.2-http-methods.png**
   - 說明：HTTP 方法與 CRUD 對應表
   - 內容：列出 GET、POST、PUT、DELETE 與 CRUD 操作的對應關係
   - 建議：使用表格形式，包含範例 URL
   - 尺寸建議：800x400

## 2.3 控制器開發實戰

### 需要的截圖

5. **2.3-postman-test.png**
   - 說明：Postman API 測試畫面
   - 內容：顯示完整的 API 請求和回應
   - 建議：選擇 GET /api/users 或 POST /api/users 端點
   - 尺寸建議：1280x720

6. **2.3-api-response.png**
   - 說明：API 回應格式範例
   - 內容：展示成功回應的 JSON 格式
   - 建議：使用 Postman 或 curl 的回應結果
   - 尺寸建議：800x400

7. **2.3-error-handling.png**
   - 說明：錯誤處理回應範例
   - 內容：顯示驗證失敗或資源不存在的錯誤回應
   - 建議：包含錯誤碼、訊息和詳細資訊
   - 尺寸建議：800x400

## 製作指南

### API 測試截圖步驟

1. **啟動應用程式**：
   ```bash
   cd code-examples/chapter2-spring-mvc-api
   mvn spring-boot:run
   ```

2. **使用 Postman 測試**：
   - 匯入 API 集合或手動建立請求
   - 執行各種 API 操作（GET、POST、PUT、DELETE）
   - 擷取清晰的請求/回應畫面

3. **截圖要點**：
   - 確保 URL 清晰可見
   - 包含完整的 JSON 回應
   - 顯示 HTTP 狀態碼
   - 避免包含敏感資訊

### 架構圖製作工具（使用 Mermaid）

本專案使用 **Mermaid** 製作架構圖：

**推薦工具**：
- **Mermaid Live Editor** (https://mermaid.live/) - 線上編輯器（推薦）
- **VS Code** + Markdown Preview Mermaid Support 擴充套件
- **Mermaid CLI** - 命令列工具

**步驟**：
1. 開啟 `ARCHITECTURE_DIAGRAMS.md` 文件
2. 複製對應圖表的 Mermaid 程式碼
3. 使用上述任一工具生成 PNG 圖片
4. 儲存到本目錄

**詳細說明**：請參考 `ARCHITECTURE_DIAGRAMS.md`

## 截圖規範

- 格式：PNG
- 解析度：至少 1080p
- 內容：清晰可讀，重點明確
- 標註：必要時使用紅框或箭頭標示
- 命名：使用文件中引用的檔案名稱
