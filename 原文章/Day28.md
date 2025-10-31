# 使用 Spring AI 打造企業 RAG 知識庫【28】- 企業RAG真正的資料來源

## 外部資料才用ETL，內部資料直接從資料庫抓

![Database Integration](https://example.com/database-integration.jpg)

前幾天主要在說明如何從電子檔案擷取資料匯入向量資料庫，不過企業最大宗的資料卻都在一般的資料庫上，今天就來說說如何從一般的資料庫匯入數據，並使用最新的 @Tool 註解實現 Function Calling。

## 模擬資料

為了測試方便，資料庫使用嵌入式的 H2 資料庫，可以省去安裝的時間，要連到正式資料庫只需要切換資料庫設定即可。

### 依賴配置

pom.xml 增加以下依賴，使用 H2 資料庫以及 Spring Data JPA：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 實體類別

增加一個 Entity 類別，模擬手機發生狀況的處理紀錄：

`Issue.java`

```java
@Entity
@Data
@Table(name = "issues")
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String issue;
    
    @Column(nullable = false, length = 1000)
    private String solution;
    
    @Column(nullable = false)
    private String model;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp 
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "手機型號: " + model + "\n" +
               "問題描述: " + issue + "\n" +
               "解決方案: " + solution + "\n" +
               "建立時間: " + createdAt;
    }
}
```

- 使用 @Data 會透過 Lombok 幫我們建立 Getter / Setter / RequiredArgsConstructor / ToString / EqualsAndHashCode
- 改寫 toString()，後面可直接寫入向量資料庫
- 加入時間戳記欄位以便追蹤資料建立和更新時間

### Repository 介面

`IssueRepository.java`

```java
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    
    // 根據手機型號查詢
    List<Issue> findByModelContainingIgnoreCase(String model);
    
    // 根據問題關鍵字查詢
    List<Issue> findByIssueContainingIgnoreCase(String keyword);
    
    // 自定義查詢：找出最近的問題
    @Query("SELECT i FROM Issue i ORDER BY i.createdAt DESC")
    List<Issue> findRecentIssues(Pageable pageable);
    
    // 統計各型號的問題數量
    @Query("SELECT i.model, COUNT(i) FROM Issue i GROUP BY i.model")
    List<Object[]> countIssuesByModel();
}
```

這就是 Spring Data 的 DAO 物件，提供了基本的增刪改查功能，還加入了一些自定義查詢方法。

### 資料庫配置

設定資料庫參數 application.yml：

```yaml
spring:
  # 資料庫配置
  h2:
    console:
      enabled: true
      path: /h2-console
  datasource:
    url: jdbc:h2:file:./data/testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  jpa:
    show-sql: true
    open-in-view: false
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true

  # Spring AI 配置  
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
```

## Function Calling 服務

使用最新的 @Tool 註解建立 Function Calling 服務：

`IssueService.java`

```java
@Service
@RequiredArgsConstructor
public class IssueService {
    
    private final IssueRepository issueRepository;
    private final VectorStore vectorStore;
    
    @Tool("根據手機型號查詢相關問題")
    public List<IssueInfo> findIssuesByModel(String model) {
        List<Issue> issues = issueRepository.findByModelContainingIgnoreCase(model);
        return issues.stream()
            .map(this::convertToIssueInfo)
            .collect(Collectors.toList());
    }
    
    @Tool("根據問題關鍵字搜尋解決方案")
    public List<IssueInfo> searchIssuesByKeyword(String keyword) {
        List<Issue> issues = issueRepository.findByIssueContainingIgnoreCase(keyword);
        return issues.stream()
            .map(this::convertToIssueInfo)
            .collect(Collectors.toList());
    }
    
    @Tool("取得最近發生的問題清單")
    public List<IssueInfo> getRecentIssues(int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 20)); // 限制最大20筆
        List<Issue> issues = issueRepository.findRecentIssues(pageable);
        return issues.stream()
            .map(this::convertToIssueInfo)
            .collect(Collectors.toList());
    }
    
    @Tool("取得各手機型號的問題統計")
    public List<ModelStatistics> getIssueStatistics() {
        List<Object[]> stats = issueRepository.countIssuesByModel();
        return stats.stream()
            .map(stat -> new ModelStatistics((String) stat[0], (Long) stat[1]))
            .collect(Collectors.toList());
    }
    
    // 資料傳輸物件
    public record IssueInfo(
        Long id,
        String model,
        String issue,
        String solution,
        LocalDateTime createdAt
    ) {}
    
    public record ModelStatistics(
        String model,
        Long issueCount
    ) {}
    
    private IssueInfo convertToIssueInfo(Issue issue) {
        return new IssueInfo(
            issue.getId(),
            issue.getModel(),
            issue.issue(),
            issue.getSolution(),
            issue.getCreatedAt()
        );
    }
    
    // 批次匯入向量資料庫
    public void importAllIssues() {
        List<Issue> allIssues = issueRepository.findAll();
        List<Document> documents = allIssues.stream()
            .map(this::convertToDocument)
            .collect(Collectors.toList());
            
        // 使用關鍵字增強
        KeywordMetadataEnricher keywordEnricher = new KeywordMetadataEnricher(chatClient, 5);
        List<Document> enhancedDocuments = keywordEnricher.apply(documents);
        
        vectorStore.write(enhancedDocuments);
    }
    
    private Document convertToDocument(Issue issue) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "issue");
        metadata.put("model", issue.getModel());
        metadata.put("issueId", issue.getId());
        metadata.put("createdAt", issue.getCreatedAt().toString());
        
        return Document.builder()
            .withId(issue.getId().toString())
            .withContent(issue.toString())
            .withMetadata(metadata)
            .build();
    }
}
```

## ChatClient 整合

使用最新的 ChatClient API 整合 Function Calling：

`IssueController.java`

```java
@RestController
@RequestMapping("/ai/issues")
@RequiredArgsConstructor
public class IssueController {
    
    private final ChatClient chatClient;
    private final IssueService issueService;
    
    @GetMapping("/chat")
    public ResponseEntity<String> chatWithFunctions(@RequestParam String message) {
        
        String response = chatClient.prompt()
            .user(message)
            .functions("findIssuesByModel", "searchIssuesByKeyword", 
                      "getRecentIssues", "getIssueStatistics")
            .call()
            .content();
            
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/import")
    public ResponseEntity<String> importIssues() {
        try {
            issueService.importAllIssues();
            return ResponseEntity.ok("Successfully imported all issues to vector store");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to import issues: " + e.getMessage());
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<Issue> addIssue(@RequestBody CreateIssueRequest request) {
        Issue issue = new Issue();
        issue.setModel(request.model());
        issue.setIssue(request.issue());
        issue.setSolution(request.solution());
        
        Issue savedIssue = issueRepository.save(issue);
        
        // 同時更新向量資料庫
        Document document = convertToDocument(savedIssue);
        vectorStore.write(List.of(document));
        
        return ResponseEntity.ok(savedIssue);
    }
    
    public record CreateIssueRequest(
        String model,
        String issue, 
        String solution
    ) {}
}
```

## 資料初始化

建立一個資料初始化類別，用於測試：

`DataInitializer.java`

```java
@Component
@RequiredArgsConstructor
public class DataInitializer {
    
    private final IssueRepository issueRepository;
    
    @PostConstruct
    public void initializeData() {
        if (issueRepository.count() == 0) {
            createSampleData();
        }
    }
    
    private void createSampleData() {
        List<Issue> sampleIssues = Arrays.asList(
            createIssue("iPhone 15 Pro", "螢幕出現閃爍", "更新到最新iOS版本或重新啟動裝置"),
            createIssue("iPhone 15 Pro", "電池快速耗盡", "檢查背景App刷新設定，關閉不必要的定位服務"),
            createIssue("Samsung Galaxy S24", "充電緩慢", "使用原廠充電器，清潔充電埠"),
            createIssue("Samsung Galaxy S24", "相機無法對焦", "清潔鏡頭，重新啟動相機App"),
            createIssue("Google Pixel 8", "系統當機", "清除系統快取，檢查儲存空間是否充足"),
            createIssue("Google Pixel 8", "藍牙連接問題", "重置網路設定，重新配對藍牙裝置"),
            createIssue("iPhone 14", "Face ID無法使用", "重新設定Face ID，確保感應器清潔"),
            createIssue("Xiaomi 14", "發熱嚴重", "關閉高效能模式，避免同時執行大型應用"),
            createIssue("OnePlus 12", "Wi-Fi連接不穩定", "重置網路設定，更新路由器韌體"),
            createIssue("Huawei P60 Pro", "通話音質不佳", "清潔麥克風和聽筒，檢查網路訊號強度")
        );
        
        issueRepository.saveAll(sampleIssues);
        log.info("Initialized {} sample issues", sampleIssues.size());
    }
    
    private Issue createIssue(String model, String issue, String solution) {
        Issue issueEntity = new Issue();
        issueEntity.setModel(model);
        issueEntity.setIssue(issue);
        issueEntity.setSolution(solution);
        return issueEntity;
    }
}
```

## 進階查詢整合

結合向量搜索和資料庫查詢：

`HybridSearchService.java`

```java
@Service
@RequiredArgsConstructor
public class HybridSearchService {
    
    private final VectorStore vectorStore;
    private final IssueRepository issueRepository;
    private final ChatClient chatClient;
    
    public List<SearchResult> hybridSearch(String query, int limit) {
        // 1. 向量搜索
        List<Document> vectorResults = vectorStore.similaritySearch(
            SearchRequest.query(query).withTopK(limit)
        );
        
        // 2. 關鍵字搜索
        List<Issue> keywordResults = issueRepository.findByIssueContainingIgnoreCase(query);
        
        // 3. 合併和去重
        Set<String> vectorIds = vectorResults.stream()
            .map(Document::getId)
            .collect(Collectors.toSet());
            
        List<SearchResult> results = new ArrayList<>();
        
        // 加入向量搜索結果（優先級較高）
        vectorResults.forEach(doc -> {
            results.add(new SearchResult(
                doc.getId(),
                doc.getContent(),
                "vector",
                getScoreFromMetadata(doc)
            ));
        });
        
        // 加入關鍵字搜索結果（去重）
        keywordResults.stream()
            .filter(issue -> !vectorIds.contains(issue.getId().toString()))
            .forEach(issue -> {
                results.add(new SearchResult(
                    issue.getId().toString(),
                    issue.toString(),
                    "keyword",
                    0.5 // 預設分數
                ));
            });
            
        return results.stream()
            .sorted((a, b) -> Double.compare(b.score(), a.score()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public record SearchResult(
        String id,
        String content,
        String source,
        double score
    ) {}
    
    private double getScoreFromMetadata(Document doc) {
        // 從 metadata 中提取相似性分數
        return doc.getMetadata().containsKey("score") 
            ? (Double) doc.getMetadata().get("score") 
            : 1.0;
    }
}
```

## 驗收成果

### 測試 Function Calling

訪問 H2 控制台：
```
http://localhost:8080/h2-console
```

測試智能查詢：
```bash
# 查詢特定型號問題
curl "http://localhost:8080/ai/issues/chat?message=iPhone 15 Pro有什麼常見問題？"

# 搜尋解決方案
curl "http://localhost:8080/ai/issues/chat?message=手機螢幕閃爍要怎麼處理？"

# 取得統計資訊
curl "http://localhost:8080/ai/issues/chat?message=統計各個手機型號的問題數量"
```

### 測試資料匯入

```bash
curl http://localhost:8080/ai/issues/import
```

## 企業級最佳實踐

### 1. 連接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
```

### 2. 快取配置

```java
@EnableCaching
@Configuration
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES));
        return cacheManager;
    }
}

// 在 Service 中使用快取
@Cacheable(value = "issues", key = "#model")
public List<IssueInfo> findIssuesByModel(String model) {
    // 查詢實現
}
```

### 3. 監控和指標

```java
@Component
public class IssueMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter searchCounter;
    private final Timer searchTimer;
    
    public IssueMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.searchCounter = Counter.builder("issue.search.count")
            .description("Number of issue searches")
            .register(meterRegistry);
        this.searchTimer = Timer.builder("issue.search.duration")
            .description("Issue search duration")
            .register(meterRegistry);
    }
    
    public void recordSearch() {
        searchCounter.increment();
    }
    
    public Timer.Sample startSearch() {
        return Timer.start(meterRegistry);
    }
}
```

## 回顧

今天學到了什麼？

- 使用最新的 @Tool 註解實現 Function Calling
- 快速使用 Spring Data JPA 建立 H2 資料表格
- 從資料庫讀取內容並轉入向量資料庫的最佳實踐
- 使用 ChatClient 新 API 整合 Function Calling
- 混合搜索：結合向量搜索和關鍵字搜索
- 企業級資料庫配置和監控
- 資料快取和效能優化策略

## Source Code

程式碼下載: [https://github.com/kevintsai1202/SpringBoot-AI-Day28-Updated.git](https://github.com/kevintsai1202/SpringBoot-AI-Day28-Updated.git)