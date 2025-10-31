# 使用 Spring AI 打造企業 RAG 知識庫【27】- 給向量資料加上Buff-ETL(下)

## 嵌套俄羅斯娃娃

回顧一下 ETL Pipeline 中間那一塊：

![ETL Pipeline](https://example.com/etl-pipeline.jpg)

前面只用到 `TokenTextSplitter` 將大檔案切成小塊，今天來詳細的介紹這些工具有甚麼功用以及詳細的參數設定。

## TokenTextSplitter 進階配置

前面用過預設的標準拆分，現在使用最新的 Builder 模式建構子：

```java
TextSplitter splitter = TokenTextSplitter.builder()
    .withDefaultChunkSize(800)        // 每個文字區塊的目標大小（以 Token 為單位）
    .withMinChunkSizeChars(350)       // 每個文字區塊的最小字元數
    .withMinChunkLengthToEmbed(5)     // 要嵌入的區塊的最小長度
    .withMaxNumChunks(10000)          // 從內容產生的最大區塊數
    .withKeepSeparator(true)          // 是否在區塊中保留分隔符號（如換行符）
    .build();
```

> 一般使用預設切割即可，想優化查詢結果再自行調整參數測試

## KeywordMetadataEnricher

它能使用 AI 模型從文件內容中提取關鍵字，並將其添加為 metadata。這個功能使用簡單的提示詞就能做到，下面是原始碼使用的提示詞：

```java
public static final String KEYWORDS_TEMPLATE = """
    {context_str}. Give %s unique keywords for this
    document. Format as comma separated. Keywords: """;
```

### 程式碼實作

修改昨天匯入 pdf 的程式碼，使用最新的 ChatClient API：

`EtlService.java`

```java
@Service
@RequiredArgsConstructor
public class EtlService {
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    // 從Document中取得10個關鍵字
    public List<Document> keywordDocuments(List<Document> documents) {
        KeywordMetadataEnricher keywordEnricher = new KeywordMetadataEnricher(chatClient, 10);
        return keywordEnricher.apply(documents);
    }

    public List<Document> loadPdfAsDocuments() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:pdf/*.pdf");
        
        List<Document> docs = new ArrayList<>();
        for (Resource pdfResource : resources) {
            try {
                ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader(pdfResource);
                docs.addAll(pdfReader.read());
            } catch (IllegalArgumentException e) {
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource);
                docs.addAll(pdfReader.read());
            }
        }
        return docs;
    }

    public void importPdf() throws IOException {
        TextSplitter splitter = TokenTextSplitter.builder()
            .withDefaultChunkSize(800)
            .withMinChunkSizeChars(350)
            .build();
            
        List<Document> documents = loadPdfAsDocuments();
        List<Document> keywordEnhancedDocuments = keywordDocuments(documents);
        List<Document> splitDocuments = splitter.split(keywordEnhancedDocuments);
        
        vectorStore.write(splitDocuments);
    }
}
```

這功能其實就是進階 RAG 的方法之一，透過關鍵字強化 embedding 數值，讓近似搜尋更為精準，另外也可作為推薦字或是進階篩選的功能。

## SummaryMetadataEnricher

它使用 AI 模型為文件建立摘要並新增為 metadata，它可以為當前 Document 以及相鄰 Document（上一個和下一個）產生摘要。

原始碼中的提示詞：

```java
public static final String DEFAULT_SUMMARY_EXTRACT_TEMPLATE = """
    Here is the content of the section:
    {context_str}
    Summarize the key topics and entities of the section.
    Summary:""";
```

### 程式碼實作

`EtlService.java`：使用最新的 ChatClient API

```java
public List<Document> summaryDocuments(List<Document> documents) {
    SummaryMetadataEnricher summaryEnricher = new SummaryMetadataEnricher(
        chatClient,
        List.of(SummaryType.PREVIOUS, SummaryType.CURRENT, SummaryType.NEXT)
    );
    return summaryEnricher.apply(documents);
}

public void importPdf() throws IOException {
    TextSplitter splitter = TokenTextSplitter.builder()
        .withDefaultChunkSize(800)
        .withMinChunkSizeChars(350)
        .build();
        
    List<Document> documents = loadPdfAsDocuments();
    
    // 使用 Pipeline 方式處理文件
    List<Document> processedDocuments = documents.stream()
        .collect(Collectors.toList());
    
    // 先加上關鍵字
    processedDocuments = keywordDocuments(processedDocuments);
    
    // 再加上摘要
    processedDocuments = summaryDocuments(processedDocuments);
    
    // 最後切割並儲存
    List<Document> splitDocuments = splitter.split(processedDocuments);
    vectorStore.write(splitDocuments);
}
```

## 文件處理流水線

為了避免嵌套地獄，我們可以建立一個更清晰的文件處理流水線：

```java
@Component
public class DocumentProcessingPipeline {
    
    private final ChatClient chatClient;
    private final List<DocumentTransformer> transformers;
    
    public DocumentProcessingPipeline(ChatClient chatClient) {
        this.chatClient = chatClient;
        this.transformers = initializeTransformers();
    }
    
    private List<DocumentTransformer> initializeTransformers() {
        List<DocumentTransformer> transformers = new ArrayList<>();
        
        // 1. 關鍵字提取
        transformers.add(new KeywordMetadataEnricher(chatClient, 10));
        
        // 2. 摘要生成
        transformers.add(new SummaryMetadataEnricher(
            chatClient,
            List.of(SummaryType.CURRENT, SummaryType.NEXT)
        ));
        
        // 3. 文件分割
        transformers.add(TokenTextSplitter.builder()
            .withDefaultChunkSize(800)
            .withMinChunkSizeChars(350)
            .build());
            
        return transformers;
    }
    
    public List<Document> process(List<Document> documents) {
        List<Document> currentDocuments = documents;
        
        for (DocumentTransformer transformer : transformers) {
            currentDocuments = transformer.apply(currentDocuments);
        }
        
        return currentDocuments;
    }
}
```

使用流水線：

```java
@Service
@RequiredArgsConstructor  
public class EtlService {
    private final VectorStore vectorStore;
    private final DocumentProcessingPipeline pipeline;

    public void importPdf() throws IOException {
        List<Document> documents = loadPdfAsDocuments();
        List<Document> processedDocuments = pipeline.process(documents);
        vectorStore.write(processedDocuments);
    }
}
```

## 自定義 Metadata Enricher

如果需要自定義的 metadata 處理，可以建立自己的 enricher：

```java
@Component
public class CustomMetadataEnricher implements DocumentTransformer {
    
    private final ChatClient chatClient;
    
    public CustomMetadataEnricher(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    @Override
    public List<Document> apply(List<Document> documents) {
        return documents.stream()
            .map(this::enrichDocument)
            .collect(Collectors.toList());
    }
    
    private Document enrichDocument(Document document) {
        String content = document.getContent();
        
        // 使用 ChatClient 分析文件類型
        String analysisPrompt = """
            Analyze the following document and determine:
            1. Document type (technical, business, legal, etc.)
            2. Complexity level (beginner, intermediate, advanced)
            3. Main topics (list up to 5)
            
            Document content:
            %s
            
            Respond in JSON format:
            {
                "type": "document_type",
                "complexity": "level",
                "topics": ["topic1", "topic2", ...]
            }
            """.formatted(content);
        
        String analysis = chatClient.prompt()
            .user(analysisPrompt)
            .call()
            .content();
        
        // 解析結果並添加到 metadata
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode analysisResult = mapper.readTree(analysis);
            
            metadata.put("document_type", analysisResult.get("type").asText());
            metadata.put("complexity_level", analysisResult.get("complexity").asText());
            metadata.put("main_topics", analysisResult.get("topics").toString());
            
        } catch (Exception e) {
            // 處理解析錯誤
            metadata.put("analysis_error", e.getMessage());
        }
        
        return Document.builder()
            .withContent(content)
            .withMetadata(metadata)
            .build();
    }
}
```

## 批次處理最佳化

對於大量文件的處理，建議使用批次處理：

```java
@Service
public class BatchEtlService {
    
    private final VectorStore vectorStore;
    private final DocumentProcessingPipeline pipeline;
    private final int BATCH_SIZE = 50;
    
    @Async
    public CompletableFuture<Void> importDocumentsBatch(List<Document> documents) {
        
        // 分批處理文件
        List<List<Document>> batches = Lists.partition(documents, BATCH_SIZE);
        
        for (List<Document> batch : batches) {
            try {
                List<Document> processedBatch = pipeline.process(batch);
                vectorStore.write(processedBatch);
                
                // 記錄進度
                log.info("Processed batch of {} documents", batch.size());
                
            } catch (Exception e) {
                log.error("Error processing batch", e);
                // 可以選擇繼續處理其他批次或停止
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }
}
```

## 配置檔案優化

在 `application.yml` 中配置文件處理參數：

```yaml
app:
  document:
    processing:
      batch-size: 50
      keyword-count: 10
      chunk-size: 800
      min-chunk-chars: 350
      summary-types:
        - CURRENT
        - NEXT
      enable-custom-analysis: true
      
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.3  # 較低的溫度以獲得更一致的結果
```

對應的配置類：

```java
@ConfigurationProperties(prefix = "app.document.processing")
@Data
public class DocumentProcessingProperties {
    private int batchSize = 50;
    private int keywordCount = 10;
    private int chunkSize = 800;
    private int minChunkChars = 350;
    private List<SummaryType> summaryTypes = List.of(SummaryType.CURRENT);
    private boolean enableCustomAnalysis = false;
}
```

## 驗收成果

測試文件處理流水線：

```bash
curl -X POST http://localhost:8080/ai/etl/import-with-enhancement
```

查看 Neo4j 中的結果，可以看到除了 text 和原有的 metadata，還會有：
- `metadata.excerpt_keywords`：提取的關鍵字
- `metadata.section_summary`：當前段落摘要
- `metadata.prev_section_summary`：前一段落摘要  
- `metadata.next_section_summary`：下一段落摘要

## 回顧

今天學到了什麼？

- 了解 DocumentTransformer 在 ETL 中扮演的角色
- 使用最新的 ChatClient API 替代舊的 ChatModel
- 透過 KeywordMetadataEnricher 幫資料加上關鍵字
- 透過 SummaryMetadataEnricher 幫資料進行摘要說明
- 建立清晰的文件處理流水線避免嵌套地獄
- 透過上述方法增強近似查詢的準確性
- 企業級的批次處理最佳化策略

## Source Code

程式碼下載: [https://github.com/kevintsai1202/SpringBoot-AI-Day27-Updated.git](https://github.com/kevintsai1202/SpringBoot-AI-Day27-Updated.git)