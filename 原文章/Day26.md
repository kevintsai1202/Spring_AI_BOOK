# 使用 Spring AI 打造企業 RAG 知識庫【26】- 擷取進階文件類型 - ETL(中)

## 把所有檔案都向量化

延續昨天的主題，今天要處理的文件內容比昨天複雜，分別是：

**PDF：** `PagePdfDocumentReader`、 `ParagraphPdfDocumentReader`

**DOCX, PPTX, HTML…：** `TikaDocumentReader`

以上都是 Spring AI 封裝 Apache 專案的工具類別，PDF 是 **pdfbox** 專案，而 `TikaDocumentReader` 就像它的名稱是 **Tika** 的專案，Tika 支援的檔案類型可參考 [官方文件](https://tika.apache.org/2.9.0/formats.html)

## PDF 文件處理

`PagePdfDocumentReader` 與 `ParagraphPdfDocumentReader` 只差在一個是以 Page 為單位，一個是以目錄的章節為單位，顯然以章節為單位拆分內容就不會被截斷，不過不是所有的 pdf 文件都有目錄，我們在程式中可先使用 `ParagraphPdfDocumentReader` 失敗時在改用 `PagePdfDocumentReader`，這樣會最大程度優化文件向量化的結果。

### 依賴配置

首先需要先引入依賴才能使用，編輯 pom.xml 加入下面內容：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
    <version>1.1-SNAPSHOT</version>
</dependency>
```

### 程式實作

`EtlService.java`：在 Service 中加入以下函式

```java
@Service
@RequiredArgsConstructor
public class EtlService {
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public List<Document> loadPdfAsDocuments() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:pdf/*.pdf");
        
        List<Document> docs = new ArrayList<>();
        for (Resource pdfResource : resources) {
            try {
                // 先使用目錄分段讀取方式
                ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader(pdfResource);
                docs.addAll(pdfReader.read());
            } catch (IllegalArgumentException e) {
                // 沒有目錄會產生Exception，改用分頁方式拆分
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource);
                docs.addAll(pdfReader.read());
            }
        }
        return docs;
    }

    public void importPdf() throws IOException {
        // 使用最新的文件切割器
        TextSplitter textSplitter = TokenTextSplitter.builder()
            .withDefaultChunkSize(800)
            .withMinChunkSizeChars(350)
            .withMinChunkLengthToEmbed(5)
            .withMaxNumChunks(10000)
            .withKeepSeparator(true)
            .build();
            
        List<Document> documents = loadPdfAsDocuments();
        List<Document> splitDocuments = textSplitter.split(documents);
        vectorStore.write(splitDocuments);
    }
}
```

`EtlController.java`：Controller 加入對應的 API

```java
@RestController
@RequestMapping("/ai/etl")
@RequiredArgsConstructor
public class EtlController {
    private final EtlService etlService;

    @GetMapping("readpdf")
    public List<Document> readPdfFile() throws IOException {
        return etlService.loadPdfAsDocuments();
    }

    @GetMapping("importpdf")
    public ResponseEntity<String> importPdf() throws IOException {
        etlService.importPdf();
        return ResponseEntity.ok("PDF files imported successfully");
    }
}
```

### 程式重點

1. 將整個目錄檔案一次讀取可使用 `PathMatchingResourcePatternResolver`，透過 `getResources("classpath:pdf/*.pdf")`，就能讀取所有的 pdf 檔
2. 程式先使用 `ParagraphPdfDocumentReader` 分段拆分，失敗時再改用 `PagePdfDocumentReader` 分頁拆分
3. 使用最新的 `TokenTextSplitter.builder()` 方式建立分割器，提供更好的可讀性和配置彈性
4. 文件類資料都會有內容過長問題，需要再使用 `TokenTextSplitter` 分割成更小塊

## Tika 文件處理

Tika 支援的檔案類型非常豐富，有興趣可自行到 [官網查看](https://tika.apache.org/2.9.0/formats.html)，另外 Tika 也有支援 pdf 檔案，不過核心也是使用 pdfbox，使用上個範例能控制更多細節。

### 依賴配置

pom.xml 引入 Tika 依賴：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-tika-document-reader</artifactId>
    <version>1.1-SNAPSHOT</version>
</dependency>
```

### 程式實作

`EtlService.java`：在 Service 中加入以下函式

```java
public List<Document> loadPptxAsDocuments() throws IOException {
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources("classpath:pptx/*.pptx");
    
    List<Document> docs = new ArrayList<>();
    for (Resource pptxResource : resources) {
        TikaDocumentReader pptxReader = new TikaDocumentReader(pptxResource);
        docs.addAll(pptxReader.read());
    }
    return docs;
}

public void importPptx() throws IOException {
    TextSplitter textSplitter = TokenTextSplitter.builder()
        .withDefaultChunkSize(800)
        .withMinChunkSizeChars(350)
        .build();
        
    List<Document> documents = loadPptxAsDocuments();
    List<Document> splitDocuments = textSplitter.split(documents);
    vectorStore.write(splitDocuments);
}
```

`EtlController.java`：同樣的 Controller 也加入對應的 API

```java
@GetMapping("readpptx")
public List<Document> readPptxFile() throws IOException {
    return etlService.loadPptxAsDocuments();
}

@GetMapping("importpptx")
public ResponseEntity<String> importPptx() throws IOException {
    etlService.importPptx();
    return ResponseEntity.ok("PPTX files imported successfully");
}
```

## 批次處理優化

對於企業級應用，我們通常需要處理多種類型的文件，可以建立一個統一的批次處理方法：

```java
@Component
public class DocumentProcessor {
    
    public List<Document> processAllDocuments() throws IOException {
        List<Document> allDocuments = new ArrayList<>();
        
        // 處理PDF文件
        allDocuments.addAll(processPdfDocuments());
        
        // 處理Office文件
        allDocuments.addAll(processOfficeDocuments());
        
        // 處理其他格式
        allDocuments.addAll(processOtherFormats());
        
        return allDocuments;
    }
    
    private List<Document> processPdfDocuments() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:documents/**/*.pdf");
        
        List<Document> docs = new ArrayList<>();
        for (Resource resource : resources) {
            try {
                ParagraphPdfDocumentReader reader = new ParagraphPdfDocumentReader(resource);
                docs.addAll(reader.read());
            } catch (Exception e) {
                PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
                docs.addAll(reader.read());
            }
        }
        return docs;
    }
    
    private List<Document> processOfficeDocuments() throws IOException {
        List<Document> docs = new ArrayList<>();
        String[] patterns = {"**/*.docx", "**/*.pptx", "**/*.xlsx"};
        
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (String pattern : patterns) {
            Resource[] resources = resolver.getResources("classpath:documents/" + pattern);
            for (Resource resource : resources) {
                TikaDocumentReader reader = new TikaDocumentReader(resource);
                docs.addAll(reader.read());
            }
        }
        return docs;
    }
}
```

## 配置和最佳實踐

### application.yml 配置

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-3-small
    vectorstore:
      neo4j:
        uri: ${NEO4J_URI:bolt://localhost:7687}
        username: ${NEO4J_USERNAME:neo4j}
        password: ${NEO4J_PASSWORD:password}
        database-name: ${NEO4J_DATABASE:neo4j}
```

### 企業級配置建議

1. **資源路徑配置**：使用外部配置文件指定文件路徑
2. **批次處理大小**：避免一次處理過多文件導致記憶體溢出
3. **錯誤處理**：完善的異常處理和日誌記錄
4. **進度追蹤**：對於大量文件的處理，提供進度回饋

```java
@ConfigurationProperties(prefix = "app.document")
@Data
public class DocumentProperties {
    private String basePath = "classpath:documents";
    private int batchSize = 100;
    private boolean skipErrors = true;
    private Map<String, String> readerConfig = new HashMap<>();
}
```

## 驗收成果

測試 PDF 導入：
```bash
curl http://localhost:8080/ai/etl/importpdf
```

測試 PPTX 導入：
```bash
curl http://localhost:8080/ai/etl/importpptx
```

檢查 Neo4j 中的資料，可以看到 metadata 預設會保存檔名跟第幾頁資訊。

## 回顧

今天學到了什麼？

- 如何使用最新的 Spring AI 1.1-SNAPSHOT API 處理多種文件格式
- 透過 `ParagraphPdfDocumentReader` 與 `PagePdfDocumentReader` 讀取 PDF 內容
- 透過 `TikaDocumentReader` 讀取 Office、HTML 等相關文件內容
- 如何建立企業級的批次文件處理系統
- 最新的 `TokenTextSplitter.builder()` 使用方式

## Source Code

程式碼下載: [https://github.com/kevintsai1202/SpringBoot-AI-Day26-Updated.git](https://github.com/kevintsai1202/SpringBoot-AI-Day26-Updated.git)