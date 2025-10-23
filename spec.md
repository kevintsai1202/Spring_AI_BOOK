# Spring AI 書籍專案重構規格文件

## 1. 架構與選型

### 1.1 專案目標
將 Spring AI 書籍專案整理為標準化、可執行的教學範例專案，包括：
- ✅ 完整可執行的程式碼範例（code-examples）
- ✅ 精簡的 Markdown 文件（只保留重點片段）
- ✅ 與 Spring AI 官網內容一致性驗證

### 1.2 技術選型

**後端框架**：
- Spring Boot 3.2.x+
- Spring AI 1.0.0+
- Java 21

**建置工具**：
- Maven 3.9+

**AI 服務供應商**：
- OpenAI (GPT-5)
- Gemini (2.5 pro)
- Gemini (2.5 flash image (nano banana 圖形生成))
- Groq (Whisper 語音轉文字)

### 1.3 專案結構設計

```
Spring_AI_BOOK/
├── code-examples/              # 完整可執行程式碼
│   ├── chapter0-prerequisite/     # 第0章：前置知識
│   ├── chapter1-spring-boot-basics/   # 第1章：Spring Boot基礎
│   ├── chapter2-spring-mvc-api/       # 第2章：Spring MVC API
│   ├── chapter3-enterprise-features/  # 第3章：企業級功能
│   ├── chapter4-spring-ai-intro/      # 第4章：Spring AI入門
│   ├── chapter5-spring-ai-advanced/   # 第5章：Spring AI進階
│   ├── chapter6-ai-memory-enhancement/ # 第6章：AI記憶增強
│   ├── chapter7-rag-implementation/   # 第7章：RAG實作
│   ├── chapter8-advanced-rag/         # 第8章：進階RAG
│   └── chapter9-mcp-integration/      # 第9章：MCP整合
├── docs/                      # Markdown文件（精簡版）
│   ├── chapter0/
│   ├── chapter1/
│   ├── ...
│   └── chapter9/
└── images/                    # 圖片資源

```

---

## 2. 資料模型

### 2.1 章節對應關係

| 章節編號 | 章節主題 | Spring AI官網對應 | 程式碼範例類型 |
|---------|---------|------------------|--------------|
| 第0章 | 前置知識 | - | 環境配置、IDE設定 |
| 第1章 | Spring Boot基礎 | Getting Started | CRUD、DI、MVC基礎 |
| 第2章 | Spring MVC API | - | RESTful API、異常處理 |
| 第3章 | 企業級功能 | - | 驗證、檔案上傳、安全 |
| 第4章 | Spring AI入門 | Chat Models, API Keys | ChatClient基礎、流式響應 |
| 第5章 | Spring AI進階 | Prompts, Structured Output, Multimodality | PromptTemplate、Function Calling、多模態 |
| 第6章 | AI記憶增強 | Chat Memory, Advisors | 對話歷史、記憶優化 |
| 第7章 | RAG實作 | RAG, Vector Stores, Embeddings | 文檔處理、向量存儲、檢索 |
| 第8章 | 進階RAG | Advanced RAG Patterns | 查詢優化、重排序、監控 |
| 第9章 | MCP整合 | - | Model Context Protocol |

### 2.2 程式碼範例組織結構

**每個章節專案標準結構**：
```
chapterX-topic-name/
├── pom.xml                    # Maven配置
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/springai/chapterX/
│   │   │       ├── config/       # 配置類
│   │   │       ├── controller/   # 控制器
│   │   │       ├── service/      # 服務層
│   │   │       ├── model/        # 資料模型
│   │   │       └── XxxApplication.java  # 主程式
│   │   └── resources/
│   │       ├── application.yml   # 應用配置
│   │       ├── prompts/          # 提示詞範本
│   │       └── static/           # 靜態資源
│   └── test/                     # 測試程式碼
├── README.md                  # 章節說明
└── .env.example              # 環境變數範例
```

---

## 3. 關鍵流程

### 3.1 專案重構流程

```mermaid
graph TD
    A[開始] --> B[分析現有MD文件]
    B --> C[提取完整程式碼]
    C --> D[建立Maven專案結構]
    D --> E[整理程式碼到對應模組]
    E --> F[添加pom.xml和配置]
    F --> G[測試程式可執行性]
    G --> H{測試通過?}
    H -->|否| I[修正問題]
    I --> G
    H -->|是| J[精簡MD文件程式碼]
    J --> K[對照官網驗證]
    K --> L{內容一致?}
    L -->|否| M[調整內容]
    M --> K
    L -->|是| N[完成]
```

### 3.2 MD文件精簡流程

```mermaid
graph TD
    A[讀取完整MD] --> B[識別程式碼區塊]
    B --> C[判斷程式碼性質]
    C --> D{是否為核心概念?}
    D -->|是| E[保留精簡版本]
    D -->|否| F[移除並註記參考位置]
    E --> G[添加code-examples參考連結]
    F --> G
    G --> H[保存精簡MD]
```

---

## 4. 虛擬碼

### 4.1 程式碼提取與整理

```pseudocode
FUNCTION extractCodeFromMarkdown(mdFile):
    content = READ_FILE(mdFile)
    codeBlocks = EXTRACT_CODE_BLOCKS(content)

    FOR EACH block IN codeBlocks:
        language = GET_LANGUAGE(block)
        fileName = INFER_FILE_NAME(block)
        packagePath = INFER_PACKAGE_PATH(block)

        IF language == "java":
            targetPath = BUILD_PATH(chapterDir, packagePath, fileName)
            WRITE_FILE(targetPath, block.content)
        ELSE IF language == "yaml" OR language == "properties":
            targetPath = BUILD_PATH(chapterDir, "resources", fileName)
            WRITE_FILE(targetPath, block.content)
        ELSE IF language == "xml" AND fileName == "pom.xml":
            targetPath = BUILD_PATH(chapterDir, fileName)
            WRITE_FILE(targetPath, block.content)

    RETURN extractedFiles
END FUNCTION

FUNCTION simplifyMarkdown(mdFile, codeExamplesPath):
    content = READ_FILE(mdFile)
    sections = SPLIT_BY_SECTIONS(content)

    FOR EACH section IN sections:
        codeBlocks = EXTRACT_CODE_BLOCKS(section)

        FOR EACH block IN codeBlocks:
            IF IS_CORE_CONCEPT(block):
                simplified = EXTRACT_KEY_LINES(block, maxLines=20)
                referenceLink = BUILD_REFERENCE_LINK(codeExamplesPath, block)
                REPLACE_BLOCK(section, block, simplified + referenceLink)
            ELSE:
                referenceLink = BUILD_REFERENCE_LINK(codeExamplesPath, block)
                REPLACE_BLOCK(section, block, referenceLink)

    WRITE_FILE(mdFile, REBUILD_CONTENT(sections))
END FUNCTION
```

### 4.2 內容驗證流程

```pseudocode
FUNCTION validateAgainstOfficialDocs(chapterContent, officialDocsUrl):
    officialContent = FETCH_DOCS(officialDocsUrl)

    // 提取關鍵API使用方式
    chapterAPIs = EXTRACT_API_USAGE(chapterContent)
    officialAPIs = EXTRACT_API_USAGE(officialContent)

    differences = []

    FOR EACH api IN chapterAPIs:
        IF api NOT IN officialAPIs:
            differences.ADD({
                type: "API_NOT_IN_OFFICIAL",
                api: api
            })
        ELSE IF NOT IS_USAGE_CORRECT(api, officialAPIs[api]):
            differences.ADD({
                type: "INCORRECT_USAGE",
                api: api,
                expected: officialAPIs[api],
                actual: api
            })

    RETURN differences
END FUNCTION
```

---

## 5. 系統脈絡圖

```mermaid
C4Context
    title Spring AI 書籍專案系統脈絡圖

    Person(reader, "讀者", "學習Spring AI的開發者")

    System_Boundary(book, "Spring AI 書籍專案") {
        System(docs, "Markdown文件", "教學文件（精簡版）")
        System(examples, "Code Examples", "可執行程式碼範例")
    }

    System_Ext(springAIDocs, "Spring AI官方文件", "官方參考文件")
    System_Ext(aiServices, "AI服務", "OpenAI, Claude, Groq等")

    Rel(reader, docs, "閱讀教學")
    Rel(reader, examples, "執行範例")
    Rel(docs, examples, "參考連結")
    Rel(docs, springAIDocs, "內容對照")
    Rel(examples, aiServices, "呼叫API")
```

---

## 6. 容器/部署概觀

```mermaid
C4Container
    title Code Examples 容器圖

    Container_Boundary(chapter, "章節專案") {
        Container(controller, "Controller層", "Spring MVC", "處理HTTP請求")
        Container(service, "Service層", "Spring Bean", "業務邏輯處理")
        Container(config, "Configuration", "Spring Config", "AI客戶端配置")
    }

    ContainerDb(vector, "向量資料庫", "Chroma/Pinecone", "儲存嵌入向量")
    System_Ext(openai, "OpenAI API", "AI模型服務")
    System_Ext(claude, "Claude API", "AI模型服務")

    Rel(controller, service, "調用")
    Rel(service, config, "使用")
    Rel(service, vector, "讀寫")
    Rel(service, openai, "API呼叫")
    Rel(service, claude, "API呼叫")
```

---

## 7. 模組關係圖

### 7.1 Backend模組關係

```mermaid
graph TB
    subgraph Chapter1-3["第1-3章：Spring Boot基礎"]
        CH1[Chapter1<br/>Spring Boot Basics]
        CH2[Chapter2<br/>Spring MVC API]
        CH3[Chapter3<br/>Enterprise Features]
    end

    subgraph Chapter4-5["第4-5章：Spring AI核心"]
        CH4[Chapter4<br/>Spring AI Intro]
        CH5[Chapter5<br/>Spring AI Advanced]
    end

    subgraph Chapter6-9["第6-9章：進階應用"]
        CH6[Chapter6<br/>AI Memory]
        CH7[Chapter7<br/>RAG Basic]
        CH8[Chapter8<br/>Advanced RAG]
        CH9[Chapter9<br/>MCP Integration]
    end

    CH1 --> CH2
    CH2 --> CH3
    CH3 --> CH4
    CH4 --> CH5
    CH5 --> CH6
    CH5 --> CH7
    CH6 --> CH8
    CH7 --> CH8
    CH8 --> CH9

    style CH1 fill:#e1f5ff
    style CH2 fill:#e1f5ff
    style CH3 fill:#e1f5ff
    style CH4 fill:#fff9c4
    style CH5 fill:#fff9c4
    style CH6 fill:#f3e5f5
    style CH7 fill:#f3e5f5
    style CH8 fill:#f3e5f5
    style CH9 fill:#f3e5f5
```

---

## 8. 序列圖

### 8.1 讀者學習流程

```mermaid
sequenceDiagram
    participant R as 讀者
    participant MD as Markdown文件
    participant CE as Code Examples
    participant IDE as 開發環境
    participant AI as AI服務

    R->>MD: 1. 閱讀章節內容
    MD->>R: 2. 顯示核心概念程式碼片段
    R->>CE: 3. 點擊參考連結
    CE->>R: 4. 查看完整程式碼
    R->>IDE: 5. 複製到本地開發環境
    R->>IDE: 6. 配置API Key
    IDE->>AI: 7. 執行程式呼叫AI API
    AI->>IDE: 8. 返回AI響應
    IDE->>R: 9. 顯示執行結果
    R->>R: 10. 理解完整流程
```

### 8.2 程式碼重構流程

```mermaid
sequenceDiagram
    participant Dev as 開發者
    participant Analyzer as MD分析器
    participant Extractor as 程式碼提取器
    participant Builder as Maven專案建構器
    participant Validator as 內容驗證器
    participant Docs as Spring AI官網

    Dev->>Analyzer: 1. 分析MD文件
    Analyzer->>Extractor: 2. 提取程式碼區塊
    Extractor->>Builder: 3. 傳遞程式碼和metadata
    Builder->>Builder: 4. 建立Maven專案結構
    Builder->>Builder: 5. 生成pom.xml
    Builder->>Builder: 6. 整理程式碼到對應package
    Builder->>Dev: 7. 回報建置結果
    Dev->>Validator: 8. 驗證內容
    Validator->>Docs: 9. 獲取官方文件
    Docs->>Validator: 10. 返回官方內容
    Validator->>Validator: 11. 對比分析
    Validator->>Dev: 12. 回報差異
    Dev->>Dev: 13. 調整內容
```

---

## 9. ER圖

```mermaid
erDiagram
    CHAPTER ||--o{ MD_FILE : contains
    CHAPTER ||--o{ CODE_EXAMPLE : contains
    MD_FILE ||--o{ CODE_BLOCK : contains
    CODE_BLOCK }o--|| CODE_EXAMPLE : references
    CODE_EXAMPLE ||--o{ JAVA_FILE : contains
    CODE_EXAMPLE ||--|| POM_FILE : contains
    CODE_EXAMPLE ||--|| CONFIG_FILE : contains

    CHAPTER {
        int chapter_number PK
        string chapter_title
        string spring_ai_topic
        string difficulty_level
    }

    MD_FILE {
        int md_id PK
        int chapter_number FK
        string file_name
        string file_path
        text content
    }

    CODE_BLOCK {
        int block_id PK
        int md_id FK
        string language
        text code_content
        boolean is_core_concept
        int line_start
        int line_end
    }

    CODE_EXAMPLE {
        int example_id PK
        int chapter_number FK
        string project_name
        string base_package
        boolean is_executable
        string maven_group_id
    }

    JAVA_FILE {
        int file_id PK
        int example_id FK
        string class_name
        string package_path
        string file_type
        text source_code
    }

    POM_FILE {
        int pom_id PK
        int example_id FK
        string spring_boot_version
        string spring_ai_version
        text dependencies
    }

    CONFIG_FILE {
        int config_id PK
        int example_id FK
        string file_name
        string file_type
        text content
    }
```

---

## 10. 類別圖（後端關鍵類別）

### 10.1 第4章：Spring AI基礎類別

```mermaid
classDiagram
    class ChatClientConfig {
        -ChatClient.Builder builder
        +ChatClient chatClient()
    }

    class AiController {
        -ChatClient chatClient
        +String chat(String message)
        +Flux~String~ stream(String message)
    }

    class ChatModelService {
        -ChatClient chatClient
        +String simpleChat(String userMessage)
        +ChatResponse detailedChat(String userMessage)
    }

    class ChatRequest {
        -String message
        -String model
        -Map~String,Object~ parameters
    }

    ChatClientConfig --> AiController : provides
    AiController --> ChatModelService : uses
    AiController --> ChatRequest : receives
```

### 10.2 第5章：Spring AI進階類別

```mermaid
classDiagram
    class PromptTemplateService {
        -Map~String,PromptTemplate~ templates
        +Prompt createPrompt(String templateName, Map variables)
        +String loadTemplate(String name)
    }

    class FunctionCallingService {
        -ChatClient chatClient
        -List~FunctionCallback~ functions
        +String callWithFunction(String message)
        +void registerFunction(FunctionCallback function)
    }

    class MultimodalController {
        -ChatClient chatClient
        +String analyzeImage(MultipartFile image, String question)
        +String processDocument(MultipartFile pdf)
    }

    class ToolCallingController {
        -FunctionCallingService functionService
        +String askWithTools(String question)
    }

    PromptTemplateService <-- FunctionCallingService : uses
    FunctionCallingService <-- ToolCallingController : uses
    MultimodalController --> ChatClient : uses
```

### 10.3 第7章：RAG實作類別

```mermaid
classDiagram
    class DocumentProcessingService {
        -DocumentReader reader
        -TextSplitter splitter
        +List~Document~ processDocument(Resource file)
        +List~Document~ splitIntoChunks(String text)
    }

    class EmbeddingService {
        -EmbeddingModel embeddingModel
        +List~float[]~ embed(List~String~ texts)
        +float[] embedSingle(String text)
    }

    class VectorStoreService {
        -VectorStore vectorStore
        +void addDocuments(List~Document~ docs)
        +List~Document~ similaritySearch(String query, int k)
    }

    class RAGService {
        -DocumentProcessingService docService
        -EmbeddingService embeddingService
        -VectorStoreService vectorService
        -ChatClient chatClient
        +String query(String question)
        +void ingestDocuments(List~Resource~ files)
    }

    RAGService --> DocumentProcessingService : uses
    RAGService --> EmbeddingService : uses
    RAGService --> VectorStoreService : uses
    DocumentProcessingService --> VectorStoreService : provides docs
    EmbeddingService --> VectorStoreService : provides embeddings
```

---

## 11. 流程圖

### 11.1 專案重構整體流程

```mermaid
flowchart TD
    Start([開始重構]) --> Task1[階段1: 環境準備]
    Task1 --> Task2[階段2: 程式碼提取]
    Task2 --> Task3[階段3: Maven專案建構]
    Task3 --> Task4[階段4: 程式碼整理]
    Task4 --> Task5[階段5: 測試驗證]
    Task5 --> Decision1{測試通過?}
    Decision1 -->|否| Fix1[修正問題]
    Fix1 --> Task5
    Decision1 -->|是| Task6[階段6: MD文件精簡]
    Task6 --> Task7[階段7: 官網內容對照]
    Task7 --> Decision2{內容一致?}
    Decision2 -->|否| Fix2[調整內容]
    Fix2 --> Task7
    Decision2 -->|是| Task8[階段8: 文件整理]
    Task8 --> Task9[階段9: 最終測試]
    Task9 --> Decision3{全部測試通過?}
    Decision3 -->|否| Fix3[修正問題]
    Fix3 --> Task9
    Decision3 -->|是| End([完成重構])

    style Start fill:#4caf50,color:#fff
    style End fill:#4caf50,color:#fff
    style Decision1 fill:#ff9800,color:#fff
    style Decision2 fill:#ff9800,color:#fff
    style Decision3 fill:#ff9800,color:#fff
```

### 11.2 單章節處理流程

```mermaid
flowchart TD
    Start([處理章節N]) --> Read[讀取所有MD檔案]
    Read --> Extract[提取程式碼區塊]
    Extract --> Classify{分類程式碼}

    Classify -->|Java類別| Java[建立Java檔案]
    Classify -->|配置檔| Config[建立配置檔案]
    Classify -->|Maven| Pom[建立pom.xml]

    Java --> Organize[整理到package結構]
    Config --> Organize
    Pom --> Organize

    Organize --> AddDeps[添加必要依賴]
    AddDeps --> Test[執行測試]
    Test --> Check{可執行?}

    Check -->|否| Debug[除錯]
    Debug --> Test

    Check -->|是| Simplify[精簡MD檔案]
    Simplify --> AddRef[添加參考連結]
    AddRef --> Validate[驗證官網內容]
    Validate --> End([完成該章節])

    style Start fill:#2196f3,color:#fff
    style End fill:#2196f3,color:#fff
```

---

## 12. 狀態圖

### 12.1 章節重構狀態機

```mermaid
stateDiagram-v2
    [*] --> 未開始

    未開始 --> 程式碼提取中 : 開始處理
    程式碼提取中 --> 專案建構中 : 提取完成
    專案建構中 --> 程式碼整理中 : 建構完成
    程式碼整理中 --> 測試中 : 整理完成

    測試中 --> 測試失敗 : 測試不通過
    測試失敗 --> 程式碼整理中 : 修正問題
    測試中 --> MD精簡中 : 測試通過

    MD精簡中 --> 內容驗證中 : 精簡完成
    內容驗證中 --> 內容調整中 : 發現差異
    內容調整中 --> 內容驗證中 : 調整完成
    內容驗證中 --> 已完成 : 驗證通過

    已完成 --> [*]

    note right of 測試中
        執行Maven測試
        驗證程式可執行性
    end note

    note right of 內容驗證中
        對照Spring AI官網
        確保內容正確性
    end note
```

### 12.2 程式碼範例執行狀態

```mermaid
stateDiagram-v2
    [*] --> 待執行

    待執行 --> 環境檢查 : 啟動
    環境檢查 --> 配置載入 : 環境OK
    環境檢查 --> 環境錯誤 : 環境問題

    配置載入 --> API驗證 : 配置完成
    配置載入 --> 配置錯誤 : 配置問題

    API驗證 --> 執行中 : 驗證成功
    API驗證 --> API錯誤 : 驗證失敗

    執行中 --> 執行成功 : 正常完成
    執行中 --> 執行失敗 : 發生錯誤

    執行成功 --> [*]

    環境錯誤 --> 待執行 : 修正環境
    配置錯誤 --> 待執行 : 修正配置
    API錯誤 --> 待執行 : 修正API設定
    執行失敗 --> 待執行 : 修正程式碼

    note right of API驗證
        驗證API Key有效性
        檢查網路連線
        確認服務可用性
    end note
```

---

## 13. 實施計畫

### 13.1 階段劃分

#### **階段1：環境準備與分析（1天）**
- [ ] 分析所有MD檔案，建立章節清單
- [ ] 對照Spring AI官網，建立主題對應表
- [ ] 準備Maven專案範本
- [ ] 設定開發環境和工具

#### **階段2：第1-3章基礎章節重構（2天）**
- [ ] Chapter 1: Spring Boot Basics
  - [ ] 提取程式碼
  - [ ] 建立Maven專案
  - [ ] 測試執行
  - [ ] 精簡MD
- [ ] Chapter 2: Spring MVC API
- [ ] Chapter 3: Enterprise Features

#### **階段3：第4-5章 Spring AI核心（3天）**
- [ ] Chapter 4: Spring AI Intro
  - [ ] 對照官網Chat Client API
  - [ ] 提取並整理程式碼
  - [ ] 配置AI服務
  - [ ] 測試API呼叫
  - [ ] 精簡MD文件
- [ ] Chapter 5: Spring AI Advanced
  - [ ] 對照官網Prompts、Function Calling
  - [ ] 整理進階範例

#### **階段4：第6章 AI記憶（2天）**
- [ ] 對照官網Chat Memory文件
- [ ] 實作記憶管理範例
- [ ] 測試Advisor功能

#### **階段5：第7-8章 RAG實作（4天）**
- [ ] Chapter 7: RAG Implementation
  - [ ] 對照官網RAG、Vector Stores文件
  - [ ] 實作文檔處理
  - [ ] 實作向量存儲
  - [ ] 測試檢索功能
- [ ] Chapter 8: Advanced RAG
  - [ ] 實作查詢優化
  - [ ] 實作重排序

#### **階段6：第9章 MCP整合（2天）**
- [ ] 實作MCP客戶端
- [ ] 測試整合功能

#### **階段7：整合測試與文件（2天）**
- [ ] 全部章節執行測試
- [ ] 更新README文件
- [ ] 建立總體說明文件
- [ ] Git提交與推送

### 13.2 驗收標準

**程式碼範例**：
- ✅ 每個章節都有完整的Maven專案
- ✅ 所有專案可獨立執行
- ✅ 包含必要的README和配置說明
- ✅ 遵循統一的程式碼規範
- ✅ 通過基本功能測試

**Markdown文件**：
- ✅ 只保留核心概念的精簡程式碼（≤20行）
- ✅ 包含code-examples參考連結
- ✅ 保持文件可讀性和教學價值
- ✅ 與官網內容一致

**內容驗證**：
- ✅ API使用方式符合官網文件
- ✅ 範例程式碼使用最佳實踐
- ✅ 配置方式與官網建議一致
- ✅ 術語和概念正確

---

## 14. 風險與挑戰

### 14.1 潛在風險

| 風險項目 | 影響等級 | 應對策略 |
|---------|---------|---------|
| API Key配置複雜 | 中 | 提供.env.example範本，詳細說明文件 |
| 依賴版本衝突 | 高 | 統一使用Spring Boot BOM管理版本 |
| 官網文件變更 | 中 | 記錄驗證時間點，定期更新 |
| 範例無法執行 | 高 | 每個範例都進行實際測試 |
| MD精簡過度 | 中 | 保留關鍵概念，添加詳細參考連結 |

### 14.2 技術挑戰

1. **向量資料庫選擇**：不同章節可能使用不同向量資料庫，需要統一或提供多種選擇
2. **AI服務成本**：測試時控制API呼叫次數，避免產生過多費用
3. **環境差異**：確保範例在Windows/Mac/Linux都能執行
4. **Java版本相容**：確保Java 21的新特性使用適當

---

## 15. 附錄

### 15.1 參考資源

- [Spring AI 官方文件](https://docs.spring.io/spring-ai/reference/index.html)
- [Spring Boot 官方文件](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [OpenAI API 文件](https://platform.openai.com/docs)
- [Anthropic Claude API 文件](https://docs.anthropic.com/)

### 15.2 工具與依賴

**開發工具**：
- IntelliJ IDEA / Eclipse / VS Code
- Maven 3.9+
- Git
- Postman / curl（API測試）

**必要依賴**：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
```

---

**文件版本**：v1.0
**建立日期**：2025-10-23
**最後更新**：2025-10-23
