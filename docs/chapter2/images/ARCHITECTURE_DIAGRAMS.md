# 第2章架構圖 - Mermaid 版本

本文件包含第2章所需的4張架構圖的 Mermaid 原始碼。

## 使用方式

### 方法1：使用 Mermaid Live Editor（推薦）

1. 訪問：https://mermaid.live/
2. 複製下方的 Mermaid 程式碼
3. 貼到編輯器中
4. 點擊「Download PNG」或「Download SVG」
5. 將圖片重新命名並儲存到 `docs/chapter2/images/` 目錄

### 方法2：使用 VS Code

1. 安裝「Markdown Preview Mermaid Support」擴充套件
2. 在 VS Code 中開啟本檔案
3. 使用 Markdown 預覽功能查看圖表
4. 截圖儲存

### 方法3：使用 Mermaid CLI

```bash
# 安裝 mermaid-cli
npm install -g @mermaid-js/mermaid-cli

# 生成圖片
mmdc -i input.md -o output.png
```

---

## 圖1：MVC 架構圖 (2.1-mvc-architecture.png)

**說明**：展示 Model、View、Controller 三層架構及其交互關係

```mermaid
graph TB
    subgraph "MVC Architecture Pattern"
        User([使用者/瀏覽器])

        subgraph Controller["Controller 層"]
            C1[接收 HTTP 請求]
            C2[處理業務邏輯]
            C3[選擇 View]
        end

        subgraph Model["Model 層"]
            M1[業務邏輯]
            M2[資料存取]
            M3[資料驗證]
        end

        subgraph View["View 層"]
            V1[渲染頁面]
            V2[JSON 回應]
            V3[模板引擎]
        end

        subgraph Database[("資料庫")]
            DB[(Database)]
        end
    end

    User -->|1. 發送請求| C1
    C1 --> C2
    C2 -->|2. 呼叫 Model| M1
    M1 --> M2
    M2 <-->|3. 資料操作| DB
    M2 -->|4. 返回資料| C2
    C2 -->|5. 選擇 View| C3
    C3 -->|6. 傳遞資料| V1
    V1 -->|7. 返回回應| User

    style Controller fill:#e3f2fd
    style Model fill:#fff9c4
    style View fill:#f3e5f5
    style Database fill:#e8f5e9
```

**儲存為**：`2.1-mvc-architecture.png`

---

## 圖2：DispatcherServlet 工作流程 (2.1-dispatcher-servlet.png)

**說明**：展示 Spring MVC 請求處理的完整流程

```mermaid
sequenceDiagram
    participant Client as 客戶端
    participant DS as DispatcherServlet
    participant HM as HandlerMapping
    participant HA as HandlerAdapter
    participant Controller as Controller
    participant ViewResolver as ViewResolver
    participant View as View

    Client->>DS: 1. 發送 HTTP 請求
    activate DS

    DS->>HM: 2. 查詢 Handler
    activate HM
    HM-->>DS: 3. 返回 HandlerExecutionChain
    deactivate HM

    DS->>HA: 4. 獲取適配器
    activate HA
    HA->>Controller: 5. 執行 Handler
    activate Controller
    Controller->>Controller: 6. 處理業務邏輯
    Controller-->>HA: 7. 返回 ModelAndView
    deactivate Controller
    HA-->>DS: 8. 返回 ModelAndView
    deactivate HA

    DS->>ViewResolver: 9. 解析 View
    activate ViewResolver
    ViewResolver-->>DS: 10. 返回 View 物件
    deactivate ViewResolver

    DS->>View: 11. 渲染 View
    activate View
    View-->>DS: 12. 返回渲染結果
    deactivate View

    DS-->>Client: 13. 返回 HTTP 回應
    deactivate DS

    Note over DS,Controller: Spring MVC 核心流程
    Note over Client,DS: 所有請求都經過 DispatcherServlet
```

**儲存為**：`2.1-dispatcher-servlet.png`

---

## 圖3：REST 設計原則 (2.2-rest-principles.png)

**說明**：REST 的核心設計原則和特徵

```mermaid
mindmap
  root((REST<br/>設計原則))
    資源導向
      URI 識別資源
      資源的表現形式
      JSON/XML 格式
    統一介面
      HTTP 標準方法
      GET POST PUT DELETE
      PATCH OPTIONS
    無狀態性
      每個請求獨立
      不依賴伺服器狀態
      可擴展性強
    可快取性
      HTTP Cache 控制
      減少伺服器負載
      提升效能
    分層系統
      客戶端-伺服器分離
      中介層支援
      負載平衡
    超媒體驅動
      HATEOAS
      鏈接資源關係
      API 可探索性
```

**儲存為**：`2.2-rest-principles.png`

**替代方案（使用 graph）**：

```mermaid
graph TD
    REST[REST 設計原則]

    REST --> P1[資源導向<br/>Resource-Oriented]
    REST --> P2[統一介面<br/>Uniform Interface]
    REST --> P3[無狀態<br/>Stateless]
    REST --> P4[可快取<br/>Cacheable]
    REST --> P5[分層系統<br/>Layered System]
    REST --> P6[超媒體驅動<br/>HATEOAS]

    P1 --> P1A[URI 識別資源]
    P1 --> P1B[資源表現形式]
    P1 --> P1C[JSON/XML]

    P2 --> P2A[HTTP 標準方法]
    P2 --> P2B[GET POST PUT DELETE]
    P2 --> P2C[統一的錯誤處理]

    P3 --> P3A[每個請求獨立]
    P3 --> P3B[不依賴 Session]
    P3 --> P3C[可擴展性強]

    P4 --> P4A[HTTP Cache-Control]
    P4 --> P4B[ETag 機制]
    P4 --> P4C[提升效能]

    P5 --> P5A[客戶端-伺服器分離]
    P5 --> P5B[支援中介層]
    P5 --> P5C[負載平衡]

    P6 --> P6A[超連結導航]
    P6 --> P6B[API 可探索]
    P6 --> P6C[鬆散耦合]

    style REST fill:#ff6b6b,color:#fff
    style P1 fill:#4ecdc4,color:#fff
    style P2 fill:#45b7d1,color:#fff
    style P3 fill:#f9ca24,color:#333
    style P4 fill:#6c5ce7,color:#fff
    style P5 fill:#a29bfe,color:#fff
    style P6 fill:#fd79a8,color:#fff
```

---

## 圖4：HTTP 方法與 CRUD 對應表 (2.2-http-methods.png)

**說明**：HTTP 方法與 CRUD 操作的對應關係

```mermaid
graph LR
    subgraph CRUD["CRUD 操作"]
        C[Create<br/>建立]
        R[Read<br/>讀取]
        U[Update<br/>更新]
        D[Delete<br/>刪除]
    end

    subgraph HTTP["HTTP 方法"]
        POST[POST<br/>新增資源]
        GET[GET<br/>取得資源]
        PUT[PUT<br/>完整更新]
        PATCH[PATCH<br/>部分更新]
        DELETE[DELETE<br/>刪除資源]
    end

    subgraph Examples["API 範例"]
        E1["POST /api/users<br/>新增使用者"]
        E2["GET /api/users<br/>取得所有使用者"]
        E3["GET /api/users/1<br/>取得特定使用者"]
        E4["PUT /api/users/1<br/>完整更新使用者"]
        E5["PATCH /api/users/1<br/>部分更新使用者"]
        E6["DELETE /api/users/1<br/>刪除使用者"]
    end

    C -.-> POST
    R -.-> GET
    U -.-> PUT
    U -.-> PATCH
    D -.-> DELETE

    POST --> E1
    GET --> E2
    GET --> E3
    PUT --> E4
    PATCH --> E5
    DELETE --> E6

    style C fill:#4caf50,color:#fff
    style R fill:#2196f3,color:#fff
    style U fill:#ff9800,color:#fff
    style D fill:#f44336,color:#fff

    style POST fill:#4caf50,color:#fff
    style GET fill:#2196f3,color:#fff
    style PUT fill:#ff9800,color:#fff
    style PATCH fill:#ff9800,color:#fff
    style DELETE fill:#f44336,color:#fff
```

**替代方案（使用表格）**：

```mermaid
%%{init: {'theme':'base'}}%%
flowchart TB
    subgraph Table["HTTP 方法與 CRUD 對應表"]
        direction TB

        Header["| HTTP 方法 | CRUD 操作 | 說明 | 範例 |"]
        Row1["| POST | Create | 建立新資源 | POST /api/users |"]
        Row2["| GET | Read | 讀取資源 | GET /api/users/1 |"]
        Row3["| PUT | Update | 完整更新資源 | PUT /api/users/1 |"]
        Row4["| PATCH | Update | 部分更新資源 | PATCH /api/users/1 |"]
        Row5["| DELETE | Delete | 刪除資源 | DELETE /api/users/1 |"]

        Header --> Row1 --> Row2 --> Row3 --> Row4 --> Row5
    end

    style Header fill:#1e88e5,color:#fff
    style Row1 fill:#4caf50,color:#fff
    style Row2 fill:#2196f3,color:#fff
    style Row3 fill:#ff9800,color:#fff
    style Row4 fill:#ffa726,color:#fff
    style Row5 fill:#f44336,color:#fff
```

**或使用更清晰的對應圖**：

```mermaid
flowchart LR
    subgraph "CRUD 操作"
        CREATE["📝 CREATE<br/>(建立)"]
        READ["📖 READ<br/>(讀取)"]
        UPDATE["✏️ UPDATE<br/>(更新)"]
        DELETE["🗑️ DELETE<br/>(刪除)"]
    end

    subgraph "HTTP 方法"
        POST["POST"]
        GET["GET"]
        PUT["PUT"]
        PATCH["PATCH"]
        DEL["DELETE"]
    end

    subgraph "URI 範例"
        EX1["/api/users<br/>新增使用者"]
        EX2["/api/users<br/>查詢所有使用者"]
        EX3["/api/users/{id}<br/>查詢單一使用者"]
        EX4["/api/users/{id}<br/>完整更新"]
        EX5["/api/users/{id}<br/>部分更新"]
        EX6["/api/users/{id}<br/>刪除使用者"]
    end

    CREATE ==>|對應| POST
    READ ==>|對應| GET
    UPDATE ==>|對應| PUT
    UPDATE ==>|對應| PATCH
    DELETE ==>|對應| DEL

    POST --> EX1
    GET --> EX2
    GET --> EX3
    PUT --> EX4
    PATCH --> EX5
    DEL --> EX6

    style CREATE fill:#66bb6a,color:#fff,stroke:#2e7d32,stroke-width:3px
    style READ fill:#42a5f5,color:#fff,stroke:#1565c0,stroke-width:3px
    style UPDATE fill:#ffa726,color:#fff,stroke:#e65100,stroke-width:3px
    style DELETE fill:#ef5350,color:#fff,stroke:#c62828,stroke-width:3px

    style POST fill:#66bb6a,color:#fff
    style GET fill:#42a5f5,color:#fff
    style PUT fill:#ffa726,color:#fff
    style PATCH fill:#ffb74d,color:#fff
    style DEL fill:#ef5350,color:#fff
```

---

## 📝 生成圖片的步驟

### 使用 Mermaid Live Editor（推薦）

1. **訪問網站**：https://mermaid.live/

2. **圖1 - MVC 架構圖**：
   - 複製上方「圖1」的 Mermaid 程式碼
   - 貼到編輯器
   - 調整主題（可選）：點擊 Actions → Theme → Default/Dark/Forest
   - 下載：Actions → PNG → 儲存為 `2.1-mvc-architecture.png`

3. **圖2 - DispatcherServlet 流程**：
   - 複製「圖2」程式碼
   - 貼到編輯器
   - 下載儲存為 `2.1-dispatcher-servlet.png`

4. **圖3 - REST 原則**：
   - 複製「圖3」程式碼（選擇您喜歡的版本）
   - 貼到編輯器
   - 如果 mindmap 不支援，使用替代方案
   - 下載儲存為 `2.2-rest-principles.png`

5. **圖4 - HTTP 方法對應**：
   - 複製「圖4」程式碼（選擇您喜歡的版本）
   - 貼到編輯器
   - 下載儲存為 `2.2-http-methods.png`

### 使用 VS Code

1. 安裝擴充套件：
   - Markdown Preview Mermaid Support
   - 或 Mermaid Editor

2. 在 VS Code 中：
   - 開啟本檔案
   - 按 `Ctrl+Shift+V` (Windows) 或 `Cmd+Shift+V` (Mac) 預覽
   - 對圖表截圖
   - 儲存為對應檔名

### 圖片品質要求

- **格式**：PNG
- **解析度**：建議 1920x1080 或更高
- **背景**：透明或白色
- **清晰度**：確保文字清晰可讀

---

## ✅ 完成檢查

- [ ] `2.1-mvc-architecture.png` - MVC 架構圖
- [ ] `2.1-dispatcher-servlet.png` - DispatcherServlet 流程圖
- [ ] `2.2-rest-principles.png` - REST 原則圖
- [ ] `2.2-http-methods.png` - HTTP 方法對應圖

**儲存位置**：`E:\Spring_AI_BOOK\docs\chapter2\images\`

---

**建立日期**：2025-10-23
**工具**：Mermaid
**版本**：v1.0
