# Chapter 7.1-7.2: RAG 基礎系統實現 - 專案規格書

## 專案概述

### 專案名稱
**chapter7-rag-basic** - Spring AI RAG 基礎系統

### 專案目標
實現一個完整的 RAG (Retrieval-Augmented Generation) 基礎系統，涵蓋：
- RAG 系統核心架構與流程
- 文本向量化處理
- 向量資料庫整合
- 基礎文檔檢索與生成

### 適用章節
- 7.1 RAG 流程詳解
- 7.2 如何將內容向量化

---

## 1. 架構與選型

### 1.1 系統架構

```mermaid
graph TB
    subgraph "應用層"
        A[REST API Controller] --> B[RAG Service]
        B --> C[Document Service]
        B --> D[Vector Service]
    end

    subgraph "核心服務層"
        C --> E[Text Embedding Service]
        D --> F[Vector Store Service]
        E --> G[Embedding Model]
        F --> H[Vector Database]
    end

    subgraph "資料層"
        H --> I[(Neo4j Vector Store)]
        J[Document Loader] --> K[(File Storage)]
    end

    B --> J
```

### 1.2 技術選型

| 組件 | 技術 | 版本 | 說明 |
|------|------|------|------|
| **基礎框架** | Spring Boot | 3.5.7 | 應用框架 |
| **AI 框架** | Spring AI | 1.0.3 | AI 整合框架 |
| **LLM** | OpenAI GPT-4 | gpt-4o | 語言模型 |
| **Embedding** | OpenAI Embeddings | text-embedding-3-small | 向量化模型 |
| **Vector DB** | Neo4j | 5.x | 向量資料庫 |
| **Build Tool** | Maven | 3.9+ | 構建工具 |
| **Java** | JDK | 21 | 開發語言 |

### 1.3 Spring AI 組件使用

| Spring AI 組件 | 用途 | 實現方式 |
|---------------|------|----------|
| **ChatClient** | RAG 對話管理 | ChatClient.builder(chatModel).defaultAdvisors(questionAnswerAdvisor).build() |
| **QuestionAnswerAdvisor** | RAG 檢索增強 | 自動檢索文檔並組裝上下文 |
| **EmbeddingModel** | 文本向量化 | OpenAiEmbeddingModel |
| **VectorStore** | 向量存儲管理 | Neo4jVectorStore |
| **Document** | 文檔模型 | Spring AI Document 模型 |
| **SearchRequest** | 檢索請求 | 向量相似性搜尋 |

---

## 2. 資料模型

### 2.1 核心實體

#### Document (Spring AI 標準模型)
```java
public class Document {
    private String id;                      // 文檔唯一ID
    private String content;                 // 文檔內容
    private Map<String, Object> metadata;   // 元資料
    private float[] embedding;              // 向量嵌入(自動管理)
}
```

#### RAG 查詢請求
```java
@Data
@Builder
public class RAGQueryRequest {
    private String question;                // 用戶問題
    private int topK = 5;                   // 檢索文檔數量
    private double similarityThreshold = 0.7; // 相似度閾值
    private Map<String, Object> filters;    // 過濾條件
}
```

#### RAG 查詢響應
```java
@Data
@Builder
public class RAGQueryResponse {
    private String question;                // 原始問題
    private String answer;                  // 生成答案
    private List<DocumentSource> sources;   // 引用來源
    private long processingTimeMs;          // 處理時間
    private LocalDateTime timestamp;        // 時間戳
}
```

#### 文檔來源
```java
@Data
@Builder
public class DocumentSource {
    private String documentId;              // 文檔ID
    private String title;                   // 文檔標題
    private String excerpt;                 // 摘錄內容
    private double relevanceScore;          // 相關性分數
    private Map<String, Object> metadata;   // 元資料
}
```

### 2.2 向量化配置

#### Embedding 配置模型
```java
@Data
@Builder
public class EmbeddingConfig {
    private String provider = "openai";     // 提供商
    private String modelName = "text-embedding-3-small";  // 模型名稱
    private int dimensions = 1536;          // 向量維度
    private Duration timeout = Duration.ofSeconds(30);    // 超時時間
}
```

### 2.3 元資料結構

| 欄位名稱 | 類型 | 說明 | 範例 |
|---------|------|------|------|
| `source_file` | String | 來源文件名 | "product_manual.pdf" |
| `document_type` | String | 文檔類型 | "PDF", "TEXT", "MARKDOWN" |
| `created_at` | String | 創建時間 | "2024-01-15T10:30:00" |
| `author` | String | 作者 | "John Doe" |
| `category` | String | 分類 | "技術文檔", "產品說明" |
| `language` | String | 語言 | "zh-TW", "en" |
| `page_number` | Integer | 頁碼 | 1 |
| `chunk_index` | Integer | 分塊索引 | 0 |

---

## 3. 關鍵流程

### 3.1 RAG 完整流程

```mermaid
sequenceDiagram
    participant User as 用戶
    participant API as REST API
    participant RAG as RAG Service
    participant Chat as ChatClient
    participant Advisor as QuestionAnswerAdvisor
    participant Vector as VectorStore
    participant LLM as OpenAI GPT-4

    User->>API: 發送問題
    API->>RAG: query(question)
    RAG->>Chat: prompt(question)
    Chat->>Advisor: 觸發檢索增強
    Advisor->>Vector: similaritySearch(question)
    Vector-->>Advisor: 返回相關文檔
    Advisor->>LLM: 組裝上下文 + 問題
    LLM-->>Advisor: 生成答案
    Advisor-->>Chat: 返回答案
    Chat-->>RAG: 返回結果
    RAG-->>API: RAGQueryResponse
    API-->>User: 返回答案和來源
```

### 3.2 文檔上傳與向量化流程

```mermaid
flowchart TD
    A[上傳文檔] --> B[解析文檔內容]
    B --> C[文本預處理]
    C --> D[文本分塊 TokenTextSplitter]
    D --> E[生成向量嵌入 EmbeddingModel]
    E --> F[存儲到向量資料庫 VectorStore]
    F --> G[返回成功響應]

    C --> C1{清理特殊字符}
    C --> C2{標準化空白}
    C --> C3{處理換行符}

    D --> D1{設定塊大小 800}
    D --> D2{設定重疊 200}
    D --> D3{保持語義完整}

    E --> E1{調用 OpenAI API}
    E --> E2{生成 1536 維向量}
```

### 3.3 向量相似性搜尋流程

```mermaid
flowchart LR
    A[用戶查詢] --> B[查詢向量化]
    B --> C[向量資料庫檢索]
    C --> D[相似性計算]
    D --> E[Top-K 排序]
    E --> F[應用相似度閾值]
    F --> G[返回相關文檔]

    D --> D1[餘弦相似性]
    E --> E1[按分數降序]
    F --> F1[threshold ≥ 0.7]
```

---

## 4. 虛擬碼

### 4.1 RAG 服務核心實現

```java
/**
 * RAG 服務實現 - 使用 Spring AI QuestionAnswerAdvisor
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RAGService {

    private final ChatClient ragChatClient;
    private final VectorStore vectorStore;
    private final DocumentProcessingService documentProcessor;

    /**
     * RAG 查詢 - Spring AI 自動處理檢索增強
     */
    public RAGQueryResponse query(RAGQueryRequest request) {
        log.info("Processing RAG query: {}", request.getQuestion());

        long startTime = System.currentTimeMillis();

        try {
            // Spring AI QuestionAnswerAdvisor 會自動：
            // 1. 向量化問題
            // 2. 檢索相關文檔
            // 3. 組裝上下文
            // 4. 調用 LLM 生成答案
            String answer = ragChatClient.prompt()
                .user(request.getQuestion())
                .call()
                .content();

            long processingTime = System.currentTimeMillis() - startTime;

            return RAGQueryResponse.builder()
                .question(request.getQuestion())
                .answer(answer)
                .processingTimeMs(processingTime)
                .timestamp(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            log.error("RAG query failed", e);
            throw new RAGException("RAG 查詢失敗", e);
        }
    }

    /**
     * 帶過濾條件的 RAG 查詢
     */
    public RAGQueryResponse queryWithFilter(RAGQueryRequest request) {
        String filterExpression = buildFilterExpression(request.getFilters());

        String answer = ragChatClient.prompt()
            .user(request.getQuestion())
            .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, filterExpression))
            .call()
            .content();

        return RAGQueryResponse.builder()
            .question(request.getQuestion())
            .answer(answer)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * 添加文檔到知識庫
     */
    public void addDocuments(List<Resource> resources) {
        log.info("Adding {} documents to knowledge base", resources.size());

        List<Document> documents = new ArrayList<>();

        for (Resource resource : resources) {
            try {
                // 處理文檔
                List<Document> processedDocs = documentProcessor.process(resource);
                documents.addAll(processedDocs);

            } catch (Exception e) {
                log.error("Failed to process document: {}", resource.getFilename(), e);
            }
        }

        // 寫入向量資料庫（自動生成嵌入向量）
        vectorStore.write(documents);

        log.info("Successfully added {} document chunks", documents.size());
    }

    private String buildFilterExpression(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        // 構建過濾表達式
        List<String> conditions = filters.entrySet().stream()
            .map(entry -> String.format("metadata['%s'] == '%s'", entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        return String.join(" && ", conditions);
    }
}
```

### 4.2 文本向量化服務

```java
/**
 * 文本向量化服務 - 使用 Spring AI EmbeddingModel
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TextEmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final MeterRegistry meterRegistry;

    /**
     * 單一文本向量化
     */
    public float[] embedText(String text) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // 預處理文本
            String processedText = preprocessText(text);

            // 使用 Spring AI EmbeddingModel 生成向量
            float[] embedding = embeddingModel.embed(processedText);

            sample.stop(Timer.builder("embedding.generation.time")
                .register(meterRegistry));

            meterRegistry.counter("embedding.generation.count").increment();

            return embedding;

        } catch (Exception e) {
            meterRegistry.counter("embedding.generation.errors").increment();
            log.error("Failed to generate embedding", e);
            throw new EmbeddingException("向量化失敗", e);
        }
    }

    /**
     * 批次文本向量化
     */
    public List<float[]> embedTexts(List<String> texts) {
        log.info("Generating embeddings for {} texts", texts.size());

        try {
            // 預處理所有文本
            List<String> processedTexts = texts.stream()
                .map(this::preprocessText)
                .collect(Collectors.toList());

            // 批次生成向量
            return embeddingModel.embed(processedTexts);

        } catch (Exception e) {
            log.error("Failed to generate batch embeddings", e);
            throw new EmbeddingException("批次向量化失敗", e);
        }
    }

    /**
     * 文本預處理
     */
    private String preprocessText(String text) {
        return text
            .trim()
            .replaceAll("\\s+", " ")
            .replaceAll("[\\r\\n]+", " ")
            .substring(0, Math.min(text.length(), 8000));
    }

    /**
     * 計算向量相似性
     */
    public double calculateSimilarity(float[] vector1, float[] vector2) {
        // 餘弦相似性計算
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
```

### 4.3 文檔處理服務

```java
/**
 * 文檔處理服務 - 使用 Spring AI TokenTextSplitter
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingService {

    private final TokenTextSplitter textSplitter;

    public DocumentProcessingService() {
        // 配置文本分割器
        this.textSplitter = new TokenTextSplitter(
            800,    // defaultChunkSize
            200,    // minChunkSizeChars
            10,     // minChunkLengthToEmbed
            10000,  // maxNumChunks
            true    // keepSeparator
        );
    }

    /**
     * 處理單個文檔
     */
    public List<Document> process(Resource resource) {
        try {
            // 1. 讀取文檔內容
            DocumentReader reader = createDocumentReader(resource);
            List<Document> documents = reader.read();

            // 2. 文本分割
            List<Document> chunks = textSplitter.apply(documents);

            // 3. 添加元資料
            chunks.forEach(doc -> enrichMetadata(doc, resource));

            return chunks;

        } catch (Exception e) {
            log.error("Failed to process document", e);
            throw new DocumentProcessingException("文檔處理失敗", e);
        }
    }

    private DocumentReader createDocumentReader(Resource resource) {
        String filename = resource.getFilename();

        if (filename.endsWith(".pdf")) {
            return new PagePdfDocumentReader(resource);
        } else if (filename.endsWith(".txt")) {
            return new TextReader(resource);
        } else if (filename.endsWith(".md")) {
            return new MarkdownDocumentReader(resource);
        }

        throw new UnsupportedOperationException("不支援的文件格式: " + filename);
    }

    private void enrichMetadata(Document document, Resource resource) {
        Map<String, Object> metadata = document.getMetadata();
        metadata.put("source_file", resource.getFilename());
        metadata.put("processed_at", LocalDateTime.now().toString());
        metadata.put("document_type", getDocumentType(resource.getFilename()));
    }
}
```

### 4.4 RAG 配置類

```java
/**
 * RAG 系統配置 - 使用 Spring AI 組件
 */
@Configuration
@EnableConfigurationProperties(RAGProperties.class)
public class RAGConfig {

    /**
     * 配置 Neo4j 向量資料庫
     */
    @Bean
    public VectorStore vectorStore(
            EmbeddingModel embeddingModel,
            Neo4jClient neo4jClient) {

        return Neo4jVectorStore.builder()
            .neo4jClient(neo4jClient)
            .embeddingModel(embeddingModel)
            .indexName("document-embeddings")
            .embeddingProperty("embedding")
            .build();
    }

    /**
     * 配置 QuestionAnswerAdvisor
     */
    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor(
            VectorStore vectorStore,
            RAGProperties properties) {

        return QuestionAnswerAdvisor.builder(vectorStore)
            .searchRequest(SearchRequest.builder()
                .topK(properties.getTopK())
                .similarityThreshold(properties.getSimilarityThreshold())
                .build())
            .build();
    }

    /**
     * 配置 RAG ChatClient
     */
    @Bean
    public ChatClient ragChatClient(
            ChatModel chatModel,
            QuestionAnswerAdvisor questionAnswerAdvisor) {

        return ChatClient.builder(chatModel)
            .defaultAdvisors(questionAnswerAdvisor)
            .build();
    }

    /**
     * 配置 OpenAI Embedding Model
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .build();

        return new OpenAiEmbeddingModel(
            openAiApi,
            MetadataMode.EMBED,
            OpenAiEmbeddingOptions.builder()
                .withModel("text-embedding-3-small")
                .withDimensions(1536)
                .build(),
            RetryUtils.DEFAULT_RETRY_TEMPLATE
        );
    }
}
```

---

## 5. 系統脈絡圖 (C4 Model)

### 5.1 系統上下文圖 (Level 1)

```mermaid
graph TB
    User[用戶] -->|查詢問題| RAGSystem[RAG 系統<br/>Spring AI Application]
    RAGSystem -->|API 調用| OpenAI[OpenAI API<br/>GPT-4 & Embeddings]
    RAGSystem -->|存儲向量| Neo4j[Neo4j<br/>Vector Database]
    RAGSystem -->|讀取文檔| FileStorage[文件存儲<br/>Local/Cloud]

    style RAGSystem fill:#4A90E2,color:#fff
    style OpenAI fill:#10A37F,color:#fff
    style Neo4j fill:#008CC1,color:#fff
```

### 5.2 容器圖 (Level 2)

```mermaid
graph TB
    subgraph "RAG Application [Spring Boot]"
        WebAPI[Web API<br/>REST Controllers]
        RAGService[RAG Service<br/>Business Logic]
        VectorService[Vector Service<br/>Embedding Management]
        DocService[Document Service<br/>Processing & Loading]
    end

    User[用戶] -->|HTTPS| WebAPI
    WebAPI --> RAGService
    RAGService --> VectorService
    RAGService --> DocService

    VectorService -->|向量化| OpenAI[OpenAI API]
    VectorService -->|相似性搜尋| Neo4j[Neo4j Vector DB]
    DocService -->|存儲文檔| Neo4j
    DocService -->|讀取文件| FileSystem[File System]

    style User fill:#lightblue
    style WebAPI fill:#4A90E2,color:#fff
    style RAGService fill:#4A90E2,color:#fff
    style VectorService fill:#4A90E2,color:#fff
    style DocService fill:#4A90E2,color:#fff
```

### 5.3 組件圖 (Level 3)

```mermaid
graph TB
    subgraph "REST API Layer"
        RAGController[RAGController]
        DocumentController[DocumentController]
    end

    subgraph "Service Layer"
        RAGService[RAGService]
        TextEmbeddingService[TextEmbeddingService]
        DocumentProcessingService[DocumentProcessingService]
    end

    subgraph "Spring AI Components"
        ChatClient[ChatClient]
        QuestionAnswerAdvisor[QuestionAnswerAdvisor]
        EmbeddingModel[EmbeddingModel]
        VectorStore[VectorStore]
        TokenTextSplitter[TokenTextSplitter]
    end

    subgraph "External Services"
        OpenAI[OpenAI API]
        Neo4j[Neo4j Database]
    end

    RAGController --> RAGService
    DocumentController --> DocumentProcessingService

    RAGService --> ChatClient
    RAGService --> VectorStore

    ChatClient --> QuestionAnswerAdvisor
    QuestionAnswerAdvisor --> VectorStore

    TextEmbeddingService --> EmbeddingModel
    DocumentProcessingService --> TokenTextSplitter
    DocumentProcessingService --> VectorStore

    EmbeddingModel --> OpenAI
    VectorStore --> Neo4j
    VectorStore --> EmbeddingModel

    style RAGController fill:#90EE90
    style DocumentController fill:#90EE90
    style RAGService fill:#FFB6C1
    style ChatClient fill:#FFD700
    style QuestionAnswerAdvisor fill:#FFD700
    style VectorStore fill:#FFD700
```

---

## 6. 模組關係圖

```mermaid
classDiagram
    class RAGController {
        -RAGService ragService
        +query(request) RAGQueryResponse
        +addDocuments(files) ApiResponse
    }

    class RAGService {
        -ChatClient ragChatClient
        -VectorStore vectorStore
        -DocumentProcessingService documentProcessor
        +query(request) RAGQueryResponse
        +queryWithFilter(request) RAGQueryResponse
        +addDocuments(resources) void
    }

    class TextEmbeddingService {
        -EmbeddingModel embeddingModel
        +embedText(text) float[]
        +embedTexts(texts) List~float[]~
        +calculateSimilarity(v1, v2) double
    }

    class DocumentProcessingService {
        -TokenTextSplitter textSplitter
        +process(resource) List~Document~
        -createDocumentReader(resource) DocumentReader
        -enrichMetadata(doc, resource) void
    }

    class RAGConfig {
        +vectorStore() VectorStore
        +questionAnswerAdvisor() QuestionAnswerAdvisor
        +ragChatClient() ChatClient
        +embeddingModel() EmbeddingModel
    }

    RAGController --> RAGService
    RAGService --> ChatClient
    RAGService --> VectorStore
    RAGService --> DocumentProcessingService
    TextEmbeddingService --> EmbeddingModel
    DocumentProcessingService --> TokenTextSplitter
    RAGConfig ..> ChatClient : creates
    RAGConfig ..> QuestionAnswerAdvisor : creates
    RAGConfig ..> VectorStore : creates
    RAGConfig ..> EmbeddingModel : creates
```

---

## 7. 序列圖

### 7.1 RAG 查詢序列圖

```mermaid
sequenceDiagram
    actor User
    participant Controller as RAGController
    participant Service as RAGService
    participant Chat as ChatClient
    participant Advisor as QuestionAnswerAdvisor
    participant Vector as VectorStore
    participant Embed as EmbeddingModel
    participant Neo4j as Neo4j DB
    participant OpenAI as OpenAI API

    User->>Controller: POST /api/rag/query
    Controller->>Service: query(request)
    Service->>Chat: prompt(question)

    Note over Chat,Advisor: Spring AI 自動檢索增強

    Chat->>Advisor: 觸發 Advisor
    Advisor->>Embed: embed(question)
    Embed->>OpenAI: API 調用
    OpenAI-->>Embed: 返回向量
    Embed-->>Advisor: 返回向量

    Advisor->>Vector: similaritySearch(vector, topK)
    Vector->>Neo4j: 向量相似性查詢
    Neo4j-->>Vector: 返回相關文檔
    Vector-->>Advisor: 返回文檔列表

    Advisor->>Advisor: 組裝上下文
    Advisor->>OpenAI: 調用 GPT-4(context + question)
    OpenAI-->>Advisor: 返回生成答案

    Advisor-->>Chat: 返回答案
    Chat-->>Service: 返回結果
    Service->>Service: 構建響應
    Service-->>Controller: RAGQueryResponse
    Controller-->>User: 200 OK + 答案
```

### 7.2 文檔上傳與向量化序列圖

```mermaid
sequenceDiagram
    actor User
    participant Controller as DocumentController
    participant Service as RAGService
    participant Processor as DocumentProcessingService
    participant Reader as DocumentReader
    participant Splitter as TokenTextSplitter
    participant Vector as VectorStore
    participant Embed as EmbeddingModel
    participant OpenAI as OpenAI API
    participant Neo4j as Neo4j DB

    User->>Controller: POST /api/rag/documents
    Controller->>Service: addDocuments(files)

    loop 處理每個文檔
        Service->>Processor: process(resource)
        Processor->>Reader: read()
        Reader-->>Processor: List<Document>

        Processor->>Splitter: apply(documents)
        Splitter-->>Processor: List<Document> 分塊

        Processor->>Processor: enrichMetadata()
        Processor-->>Service: 處理後的文檔
    end

    Service->>Vector: write(documents)

    Note over Vector,Neo4j: Spring AI 自動向量化

    loop 批次向量化
        Vector->>Embed: embed(document.content)
        Embed->>OpenAI: API 調用
        OpenAI-->>Embed: 返回向量
        Embed-->>Vector: 返回向量
    end

    Vector->>Neo4j: 批次存儲文檔和向量
    Neo4j-->>Vector: 確認存儲

    Vector-->>Service: 完成
    Service-->>Controller: 成功響應
    Controller-->>User: 200 OK
```

---

## 8. 類別圖

```mermaid
classDiagram
    class RAGQueryRequest {
        -String question
        -int topK
        -double similarityThreshold
        -Map~String, Object~ filters
    }

    class RAGQueryResponse {
        -String question
        -String answer
        -List~DocumentSource~ sources
        -long processingTimeMs
        -LocalDateTime timestamp
    }

    class DocumentSource {
        -String documentId
        -String title
        -String excerpt
        -double relevanceScore
        -Map~String, Object~ metadata
    }

    class RAGService {
        -ChatClient ragChatClient
        -VectorStore vectorStore
        -DocumentProcessingService documentProcessor
        +query(request) RAGQueryResponse
        +queryWithFilter(request) RAGQueryResponse
        +addDocuments(resources) void
    }

    class TextEmbeddingService {
        -EmbeddingModel embeddingModel
        -MeterRegistry meterRegistry
        +embedText(text) float[]
        +embedTexts(texts) List~float[]~
        +calculateSimilarity(v1, v2) double
        -preprocessText(text) String
    }

    class DocumentProcessingService {
        -TokenTextSplitter textSplitter
        +process(resource) List~Document~
        -createDocumentReader(resource) DocumentReader
        -enrichMetadata(doc, resource) void
        -getDocumentType(filename) String
    }

    class RAGException {
        -String message
        -Throwable cause
        +RAGException(message, cause)
    }

    class EmbeddingException {
        -String message
        -Throwable cause
        +EmbeddingException(message, cause)
    }

    RAGQueryResponse --> DocumentSource : contains
    RAGService --> RAGQueryRequest : uses
    RAGService --> RAGQueryResponse : produces
    RAGService --> RAGException : throws
    TextEmbeddingService --> EmbeddingException : throws
```

---

## 9. 流程圖

### 9.1 RAG 查詢主流程

```mermaid
flowchart TD
    Start([開始: 接收用戶問題]) --> Validate{驗證請求}
    Validate -->|無效| Error1[返回錯誤]
    Validate -->|有效| EmbedQ[向量化問題]

    EmbedQ --> Search[向量相似性搜尋]
    Search --> Filter{應用過濾條件}
    Filter -->|有過濾| ApplyFilter[應用元資料過濾]
    Filter -->|無過濾| GetDocs[獲取 Top-K 文檔]
    ApplyFilter --> GetDocs

    GetDocs --> CheckDocs{找到相關文檔?}
    CheckDocs -->|否| NoContext[使用基礎知識回答]
    CheckDocs -->|是| BuildContext[組裝上下文]

    BuildContext --> CallLLM[調用 GPT-4 生成答案]
    NoContext --> CallLLM

    CallLLM --> Success{生成成功?}
    Success -->|否| Error2[記錄錯誤並重試]
    Success -->|是| BuildResponse[構建響應]

    BuildResponse --> AddSources[添加文檔來源]
    AddSources --> RecordMetrics[記錄性能指標]
    RecordMetrics --> End([返回答案])

    Error1 --> End
    Error2 --> End
```

### 9.2 文檔處理流程

```mermaid
flowchart TD
    Start([開始: 接收文檔]) --> CheckType{檢查文件類型}

    CheckType -->|PDF| PDFReader[PagePdfDocumentReader]
    CheckType -->|TXT| TXTReader[TextReader]
    CheckType -->|MD| MDReader[MarkdownDocumentReader]
    CheckType -->|不支援| Error[拋出異常]

    PDFReader --> Extract[提取文本內容]
    TXTReader --> Extract
    MDReader --> Extract

    Extract --> Clean[文本清理與預處理]
    Clean --> Split[TokenTextSplitter 分塊]

    Split --> SetConfig{分塊配置}
    SetConfig --> ChunkSize[塊大小: 800 tokens]
    SetConfig --> Overlap[重疊: 200 tokens]

    ChunkSize --> CreateChunks[創建文檔分塊]
    Overlap --> CreateChunks

    CreateChunks --> AddMeta[添加元資料]
    AddMeta --> MetaFields{元資料欄位}

    MetaFields --> Source[source_file]
    MetaFields --> Type[document_type]
    MetaFields --> Time[processed_at]
    MetaFields --> Index[chunk_index]

    Source --> Embed[向量化處理]
    Type --> Embed
    Time --> Embed
    Index --> Embed

    Embed --> Store[存儲到 Neo4j]
    Store --> Success([處理完成])

    Error --> End([返回錯誤])
```

### 9.3 向量化處理流程

```mermaid
flowchart TD
    Start([開始: 輸入文本]) --> Preprocess[文本預處理]

    Preprocess --> Trim[去除前後空白]
    Trim --> Normalize[標準化空白字符]
    Normalize --> RemoveBreaks[處理換行符]
    RemoveBreaks --> Limit[限制長度 ≤ 8000]

    Limit --> CheckCache{檢查快取}
    CheckCache -->|命中| ReturnCached[返回快取向量]
    CheckCache -->|未命中| CallAPI[調用 OpenAI API]

    CallAPI --> APIRequest{API 請求}
    APIRequest --> Model[Model: text-embedding-3-small]
    APIRequest --> Dims[Dimensions: 1536]

    Model --> GetVector[獲取向量]
    Dims --> GetVector

    GetVector --> Validate{驗證向量}
    Validate -->|無效| Retry[重試機制]
    Validate -->|有效| Cache[更新快取]

    Retry -->|達到上限| Error[拋出異常]
    Retry -->|未達上限| CallAPI

    Cache --> Metrics[記錄指標]
    Metrics --> Success([返回向量])

    ReturnCached --> Success
    Error --> End([處理失敗])
```

---

## 10. 狀態圖

### 10.1 文檔處理狀態

```mermaid
stateDiagram-v2
    [*] --> Uploaded : 文檔上傳

    Uploaded --> Processing : 開始處理
    Processing --> Parsing : 解析文檔

    Parsing --> ParseSuccess : 解析成功
    Parsing --> ParseError : 解析失敗

    ParseError --> Failed : 標記失敗
    Failed --> [*]

    ParseSuccess --> Splitting : 文本分塊
    Splitting --> Embedding : 向量化

    Embedding --> EmbedSuccess : 向量化成功
    Embedding --> EmbedError : 向量化失敗

    EmbedError --> Retry : 重試
    Retry --> Embedding : 重新向量化
    Retry --> Failed : 超過重試次數

    EmbedSuccess --> Storing : 存儲到資料庫
    Storing --> StoreSuccess : 存儲成功
    Storing --> StoreError : 存儲失敗

    StoreError --> Retry

    StoreSuccess --> Indexed : 已索引
    Indexed --> [*]
```

### 10.2 RAG 查詢狀態

```mermaid
stateDiagram-v2
    [*] --> Received : 接收查詢

    Received --> Validating : 驗證請求
    Validating --> Valid : 驗證通過
    Validating --> Invalid : 驗證失敗

    Invalid --> Rejected : 拒絕請求
    Rejected --> [*]

    Valid --> Embedding : 向量化問題
    Embedding --> Searching : 搜尋文檔

    Searching --> Found : 找到相關文檔
    Searching --> NotFound : 未找到文檔

    NotFound --> GeneratingBasic : 基礎回答
    Found --> Assembling : 組裝上下文

    Assembling --> Generating : 生成答案
    GeneratingBasic --> Generating

    Generating --> Success : 生成成功
    Generating --> Failed : 生成失敗

    Failed --> Retry : 重試
    Retry --> Generating
    Retry --> Error : 超過重試次數

    Success --> Response : 返回答案
    Error --> Response : 返回錯誤

    Response --> [*]
```

---

## 11. 部署架構

### 11.1 開發環境部署

```mermaid
graph TB
    subgraph "開發環境"
        App[Spring Boot App<br/>port: 8080]
        Neo4j[Neo4j<br/>port: 7687]
        OpenAI[OpenAI API<br/>雲端服務]
    end

    App -->|Bolt Protocol| Neo4j
    App -->|HTTPS| OpenAI

    Developer[開發者] -->|HTTP| App

    style App fill:#4A90E2,color:#fff
    style Neo4j fill:#008CC1,color:#fff
    style OpenAI fill:#10A37F,color:#fff
```

### 11.2 配置檔案結構

```yaml
# application.yml
spring:
  application:
    name: chapter7-rag-basic
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-4o
      embedding:
        model: text-embedding-3-small
        dimensions: 1536

    neo4j:
      uri: ${NEO4J_URI:bolt://localhost:7687}
      username: ${NEO4J_USERNAME:neo4j}
      password: ${NEO4J_PASSWORD}
      database: ${NEO4J_DATABASE:neo4j}

# RAG 配置
app:
  rag:
    top-k: 5
    similarity-threshold: 0.7
    chunk-size: 800
    chunk-overlap: 200

# 監控配置
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 12. API 文檔

### 12.1 RAG 查詢 API

**端點**: `POST /api/rag/query`

**請求體**:
```json
{
  "question": "Spring Boot 如何配置資料庫連接?",
  "topK": 5,
  "similarityThreshold": 0.7,
  "filters": {
    "category": "技術文檔",
    "language": "zh-TW"
  }
}
```

**響應**:
```json
{
  "question": "Spring Boot 如何配置資料庫連接?",
  "answer": "在 Spring Boot 中配置資料庫連接主要有以下幾種方式...",
  "sources": [
    {
      "documentId": "doc-001",
      "title": "Spring Boot 資料庫配置指南",
      "excerpt": "Spring Boot 提供了自動配置功能...",
      "relevanceScore": 0.92,
      "metadata": {
        "source_file": "spring-boot-guide.pdf",
        "page_number": 15
      }
    }
  ],
  "processingTimeMs": 1250,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 12.2 文檔上傳 API

**端點**: `POST /api/rag/documents`

**請求**: `multipart/form-data`

**響應**:
```json
{
  "success": true,
  "message": "成功添加 3 個文檔到知識庫",
  "documentsProcessed": 3,
  "chunksCreated": 45,
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 13. 測試策略

### 13.1 單元測試

```java
@SpringBootTest
class RAGServiceTest {

    @MockBean
    private ChatClient ragChatClient;

    @MockBean
    private VectorStore vectorStore;

    @Autowired
    private RAGService ragService;

    @Test
    void testQuery_Success() {
        // Arrange
        RAGQueryRequest request = RAGQueryRequest.builder()
            .question("測試問題")
            .topK(5)
            .build();

        when(ragChatClient.prompt()).thenReturn(mockPromptSpec);

        // Act
        RAGQueryResponse response = ragService.query(request);

        // Assert
        assertNotNull(response);
        assertEquals("測試問題", response.getQuestion());
        assertNotNull(response.getAnswer());
    }
}
```

### 13.2 整合測試

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RAGIntegrationTest {

    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.15")
        .withAdminPassword("test1234");

    @Test
    void testEndToEndRAGFlow() {
        // 1. 上傳文檔
        // 2. 執行查詢
        // 3. 驗證結果
    }
}
```

---

## 14. 效能指標

| 指標 | 目標值 | 說明 |
|------|--------|------|
| RAG 查詢響應時間 | < 2秒 | 從接收問題到返回答案 |
| 文檔處理時間 | < 5秒/文檔 | 包含解析、分塊、向量化 |
| 向量搜尋時間 | < 500ms | Top-5 相似性搜尋 |
| 並發查詢支援 | ≥ 50 QPS | 同時處理的查詢數量 |
| 系統可用性 | ≥ 99% | 正常運行時間 |

---

## 15. 依賴清單

```xml
<dependencies>
    <!-- Spring AI Core -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>

    <!-- Neo4j Vector Store -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-neo4j-store-spring-boot-starter</artifactId>
    </dependency>

    <!-- PDF Document Reader -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-pdf-document-reader</artifactId>
    </dependency>

    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Micrometer Prometheus -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## 16. 專案結構

```
chapter7-rag-basic/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/rag/
│   │   │       ├── config/
│   │   │       │   ├── RAGConfig.java
│   │   │       │   └── RAGProperties.java
│   │   │       ├── controller/
│   │   │       │   ├── RAGController.java
│   │   │       │   └── DocumentController.java
│   │   │       ├── service/
│   │   │       │   ├── RAGService.java
│   │   │       │   ├── TextEmbeddingService.java
│   │   │       │   └── DocumentProcessingService.java
│   │   │       ├── model/
│   │   │       │   ├── RAGQueryRequest.java
│   │   │       │   ├── RAGQueryResponse.java
│   │   │       │   └── DocumentSource.java
│   │   │       ├── exception/
│   │   │       │   ├── RAGException.java
│   │   │       │   └── EmbeddingException.java
│   │   │       └── RagBasicApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── sample-documents/
│   └── test/
│       └── java/
│           └── com/example/rag/
│               ├── service/
│               │   └── RAGServiceTest.java
│               └── integration/
│                   └── RAGIntegrationTest.java
├── pom.xml
└── README.md
```

---

## 17. 開發檢查清單

- [ ] Spring AI 依賴配置完成
- [ ] Neo4j 資料庫配置與連接
- [ ] OpenAI API Key 配置
- [ ] RAGService 核心實現
- [ ] TextEmbeddingService 實現
- [ ] DocumentProcessingService 實現
- [ ] REST API Controller 實現
- [ ] 異常處理機制
- [ ] 單元測試編寫
- [ ] 整合測試編寫
- [ ] API 文檔完善
- [ ] 效能指標監控
- [ ] 日誌記錄完善
- [ ] README 文檔

---

**文檔版本**: 1.0
**最後更新**: 2024-01-15
**負責人**: Spring AI Team
