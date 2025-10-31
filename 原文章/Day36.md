# 使用 Spring AI 打造企業 RAG 知識庫【36】- 新世代 AI 模型整合：Gemini Pro 2.5 與 Claude 4 Sonnet

## 新世代AI模型的春天

![https://ithelp.ithome.com.tw/upload/images/20240810/20161290AIModels2025.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/20161290AIModels2025.jpg)

2025年初，AI領域迎來了重大突破。Google發布了Gemini Pro 2.5，而Anthropic推出了Claude 4 Sonnet，這兩個模型在推理能力、多模態理解和企業應用方面都有顯著提升。

作為企業級RAG知識庫的開發者，我們需要了解如何在Spring AI中整合這些最新的模型，以提供更好的用戶體驗和更準確的回應。

## ▋Gemini Pro 2.5 vs Claude 4 Sonnet 特性比較

| 特性 | Gemini Pro 2.5 | Claude 4 Sonnet |
|-----|---------------|-----------------|
| **推理能力** | 數學和邏輯推理大幅提升 | 長文本理解和分析能力優異 |
| **多模態** | 支援文字、圖片、音訊、影片 | 支援文字、圖片、文檔分析 |
| **上下文長度** | 200K tokens | 500K tokens |
| **延遲性** | 中等 | 較低 |
| **成本效益** | 中等價位 | 較高價位但效果優異 |
| **企業功能** | 原生支援企業安全控制 | 強化隱私保護和內容過濾 |

## ▋Spring AI 1.1 整合 Gemini Pro 2.5

### 依賴配置

首先在 `pom.xml` 中添加 Vertex AI 依賴：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-vertex-ai-gemini-spring-boot-starter</artifactId>
</dependency>
```

### 配置設定

在 `application.yml` 中配置 Gemini Pro 2.5：

```yaml
spring:
  ai:
    vertex:
      ai:
        gemini:
          project-id: your-gcp-project-id
          location: us-central1
          # Gemini Pro 2.5 模型配置
          chat:
            options:
              model: gemini-2.5-pro
              temperature: 0.7
              max-output-tokens: 8192
              top-p: 0.95
              top-k: 40
              # 安全設定
              safety-settings:
                HARM_CATEGORY_HARASSMENT: BLOCK_MEDIUM_AND_ABOVE
                HARM_CATEGORY_HATE_SPEECH: BLOCK_MEDIUM_AND_ABOVE
                HARM_CATEGORY_SEXUALLY_EXPLICIT: BLOCK_MEDIUM_AND_ABOVE
                HARM_CATEGORY_DANGEROUS_CONTENT: BLOCK_MEDIUM_AND_ABOVE
```

### ChatClient 實作

```java
@RestController
@RequiredArgsConstructor
@Slf4j
public class GeminiProController {
    
    private final ChatClient chatClient;
    
    @PostMapping("/gemini/chat")
    public ResponseEntity<ChatResponse> geminiChat(@RequestBody ChatRequest request) {
        try {
            String response = chatClient.prompt()
                .user(request.getMessage())
                .options(VertexAiGeminiChatOptions.builder()
                    .model("gemini-2.5-pro")
                    .temperature(0.7f)
                    .maxOutputTokens(4000)
                    .build())
                .call()
                .content();
            
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("Gemini Pro 2.5 調用失敗", e);
            return ResponseEntity.status(500)
                .body(new ChatResponse("系統暫時無法回應，請稍後再試"));
        }
    }
    
    @PostMapping("/gemini/multimodal")
    public ResponseEntity<String> analyzeMultimodal(
            @RequestParam("text") String text,
            @RequestParam("image") MultipartFile image) {
        
        try {
            // 處理多模態輸入
            byte[] imageBytes = image.getBytes();
            Media imageMedia = new Media(MimeTypeUtils.IMAGE_JPEG, imageBytes);
            
            String response = chatClient.prompt()
                .user(userSpec -> userSpec
                    .text(text)
                    .media(imageMedia))
                .options(VertexAiGeminiChatOptions.builder()
                    .model("gemini-2.5-pro")
                    .temperature(0.3f)
                    .build())
                .call()
                .content();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("多模態分析失敗", e);
            return ResponseEntity.status(500)
                .body("圖片分析失敗，請確認圖片格式正確");
        }
    }
    
    public record ChatRequest(String message) {}
    public record ChatResponse(String response) {}
}
```

## ▋Spring AI 1.1 整合 Claude 4 Sonnet

### 依賴配置

在 `pom.xml` 中添加 Anthropic 依賴：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
</dependency>
```

### 配置設定

在 `application.yml` 中配置 Claude 4 Sonnet：

```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      chat:
        options:
          model: claude-4-sonnet
          max-tokens: 8192
          temperature: 0.7
          # Claude 4 Sonnet 特有配置
          system-prompt: |
            You are a helpful AI assistant specializing in enterprise knowledge management.
            Always provide accurate, well-structured responses and cite sources when possible.
```

### ChatClient 實作

```java
@RestController
@RequiredArgsConstructor
@Slf4j
public class ClaudeController {
    
    private final ChatClient chatClient;
    
    @PostMapping("/claude/chat")
    public ResponseEntity<String> claudeChat(@RequestBody ChatRequest request) {
        try {
            String response = chatClient.prompt()
                .system("你是一個專業的企業知識管理助手，請提供準確且結構化的回應。")
                .user(request.getMessage())
                .options(AnthropicChatOptions.builder()
                    .model("claude-4-sonnet")
                    .temperature(0.7f)
                    .maxTokens(4000)
                    .build())
                .call()
                .content();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Claude 4 Sonnet 調用失敗", e);
            return ResponseEntity.status(500)
                .body("系統暫時無法回應，請稍後再試");
        }
    }
    
    @PostMapping("/claude/document-analysis")
    public ResponseEntity<DocumentAnalysisResponse> analyzeDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("query") String query) {
        
        try {
            // 讀取文檔內容
            String documentContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            
            String analysisPrompt = String.format("""
                請分析以下文檔內容，並回答用戶的問題：
                
                文檔內容：
                %s
                
                用戶問題：%s
                
                請提供詳細的分析結果，包括：
                1. 直接回答
                2. 相關證據
                3. 信心度評估
                """, documentContent, query);
            
            String response = chatClient.prompt()
                .user(analysisPrompt)
                .options(AnthropicChatOptions.builder()
                    .model("claude-4-sonnet")
                    .temperature(0.3f)
                    .maxTokens(6000)
                    .build())
                .call()
                .content();
            
            return ResponseEntity.ok(new DocumentAnalysisResponse(response, "high"));
        } catch (Exception e) {
            log.error("文檔分析失敗", e);
            return ResponseEntity.status(500)
                .body(new DocumentAnalysisResponse("文檔分析失敗", "low"));
        }
    }
    
    public record ChatRequest(String message) {}
    public record DocumentAnalysisResponse(String analysis, String confidence) {}
}
```

## ▋企業級多模型配置策略

### 智能模型選擇器

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class IntelligentModelSelector {
    
    private final ChatClient geminiChatClient;
    private final ChatClient claudeChatClient;
    private final ChatClient openAiChatClient;
    
    public enum ModelType {
        GEMINI_PRO_25("gemini-2.5-pro", "數學推理、多模態分析"),
        CLAUDE_4_SONNET("claude-4-sonnet", "長文本理解、文檔分析"),
        GPT_4O("gpt-4o", "通用對話、程式碼生成");
        
        private final String modelName;
        private final String specialty;
        
        ModelType(String modelName, String specialty) {
            this.modelName = modelName;
            this.specialty = specialty;
        }
    }
    
    public String generateResponse(String query, QueryContext context) {
        ModelType selectedModel = selectOptimalModel(query, context);
        
        log.info("為查詢選擇模型: {} - {}", selectedModel.modelName, selectedModel.specialty);
        
        return switch (selectedModel) {
            case GEMINI_PRO_25 -> generateWithGemini(query, context);
            case CLAUDE_4_SONNET -> generateWithClaude(query, context);
            case GPT_4O -> generateWithOpenAI(query, context);
        };
    }
    
    private ModelType selectOptimalModel(String query, QueryContext context) {
        // 多模態內容優先使用 Gemini Pro 2.5
        if (context.hasMultimodalContent()) {
            return ModelType.GEMINI_PRO_25;
        }
        
        // 長文檔分析優先使用 Claude 4 Sonnet
        if (context.getDocumentLength() > 10000) {
            return ModelType.CLAUDE_4_SONNET;
        }
        
        // 數學和邏輯推理使用 Gemini Pro 2.5
        if (containsMathOrLogic(query)) {
            return ModelType.GEMINI_PRO_25;
        }
        
        // 程式碼相關使用 GPT-4o
        if (containsCodeKeywords(query)) {
            return ModelType.GPT_4O;
        }
        
        // 預設使用 Claude 4 Sonnet（平衡性最佳）
        return ModelType.CLAUDE_4_SONNET;
    }
    
    private String generateWithGemini(String query, QueryContext context) {
        return geminiChatClient.prompt()
            .user(query)
            .options(VertexAiGeminiChatOptions.builder()
                .model("gemini-2.5-pro")
                .temperature(0.7f)
                .build())
            .call()
            .content();
    }
    
    private String generateWithClaude(String query, QueryContext context) {
        return claudeChatClient.prompt()
            .user(query)
            .options(AnthropicChatOptions.builder()
                .model("claude-4-sonnet")
                .temperature(0.7f)
                .build())
            .call()
            .content();
    }
    
    private String generateWithOpenAI(String query, QueryContext context) {
        return openAiChatClient.prompt()
            .user(query)
            .options(OpenAiChatOptions.builder()
                .model("gpt-4o")
                .temperature(0.7f)
                .build())
            .call()
            .content();
    }
    
    private boolean containsMathOrLogic(String query) {
        String[] mathKeywords = {"計算", "公式", "數學", "邏輯", "推理", "證明"};
        return Arrays.stream(mathKeywords)
            .anyMatch(keyword -> query.toLowerCase().contains(keyword));
    }
    
    private boolean containsCodeKeywords(String query) {
        String[] codeKeywords = {"程式", "代碼", "編程", "algorithm", "function", "class"};
        return Arrays.stream(codeKeywords)
            .anyMatch(keyword -> query.toLowerCase().contains(keyword));
    }
    
    public static class QueryContext {
        private final boolean hasMultimodalContent;
        private final int documentLength;
        
        public QueryContext(boolean hasMultimodalContent, int documentLength) {
            this.hasMultimodalContent = hasMultimodalContent;
            this.documentLength = documentLength;
        }
        
        public boolean hasMultimodalContent() { return hasMultimodalContent; }
        public int getDocumentLength() { return documentLength; }
    }
}
```

## ▋RAG 整合與效能最佳化

### 模型特化 RAG 配置

```java
@Configuration
@RequiredArgsConstructor
public class MultiModelRagConfiguration {
    
    @Bean("geminiRagAdvisor")
    public QuestionAnswerAdvisor geminiRagAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore)
            .searchRequest(SearchRequest.builder()
                .similarityThreshold(0.75) // Gemini Pro 2.5 對相似度要求較高
                .topK(8)
                .build())
            .promptTemplate(PromptTemplate.builder()
                .template("""
                    基於以下上下文資訊回答問題，請進行邏輯推理並提供準確答案：
                    
                    上下文：
                    {question_answer_context}
                    
                    問題：{query}
                    
                    請提供詳細的推理過程和結論。
                    """)
                .build())
            .build();
    }
    
    @Bean("claudeRagAdvisor")
    public QuestionAnswerAdvisor claudeRagAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore)
            .searchRequest(SearchRequest.builder()
                .similarityThreshold(0.70) // Claude 4 Sonnet 能處理較寬泛的內容
                .topK(12)
                .build())
            .promptTemplate(PromptTemplate.builder()
                .template("""
                    請基於提供的上下文資訊回答問題，注重內容的準確性和完整性：
                    
                    上下文資訊：
                    {question_answer_context}
                    
                    用戶問題：{query}
                    
                    請提供結構化的回答，包括主要觀點和支持證據。
                    """)
                .build())
            .build();
    }
}
```

## ▋監控與成本控制

### 模型使用監控

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelUsageMonitor {
    
    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicLong> tokenUsage = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> requestCount = new ConcurrentHashMap<>();
    
    @EventListener
    public void onChatClientResponse(ChatClientResponseEvent event) {
        String modelName = event.getModelName();
        int tokens = event.getUsage().getTotalTokens();
        
        // 記錄 token 使用量
        tokenUsage.computeIfAbsent(modelName, k -> new AtomicLong(0))
            .addAndGet(tokens);
        
        // 記錄請求次數
        requestCount.computeIfAbsent(modelName, k -> new AtomicLong(0))
            .incrementAndGet();
        
        // 更新 Micrometer 指標
        Counter.builder("ai.model.requests")
            .tag("model", modelName)
            .register(meterRegistry)
            .increment();
        
        Gauge.builder("ai.model.tokens.total")
            .tag("model", modelName)
            .register(meterRegistry, tokenUsage.get(modelName), AtomicLong::get);
        
        log.debug("模型 {} 使用 {} tokens，總計 {} tokens", 
            modelName, tokens, tokenUsage.get(modelName).get());
    }
    
    @Scheduled(fixedRate = 300000) // 每5分鐘檢查一次
    public void checkUsageLimits() {
        tokenUsage.forEach((model, usage) -> {
            long currentUsage = usage.get();
            long dailyLimit = getDailyLimit(model);
            
            if (currentUsage > dailyLimit * 0.8) {
                log.warn("模型 {} 今日使用量已達 {}%，當前: {}/{}",
                    model, (currentUsage * 100 / dailyLimit), currentUsage, dailyLimit);
            }
        });
    }
    
    private long getDailyLimit(String model) {
        return switch (model.toLowerCase()) {
            case "gemini-2.5-pro" -> 1_000_000;
            case "claude-4-sonnet" -> 500_000;
            case "gpt-4o" -> 300_000;
            default -> 100_000;
        };
    }
}
```

## ▋最佳實踐建議

### 1. **模型選擇策略**
- **Gemini Pro 2.5**: 適合數學推理、多模態分析、科學計算
- **Claude 4 Sonnet**: 適合長文檔分析、創意寫作、複雜推理
- **混合使用**: 根據查詢類型自動選擇最適合的模型

### 2. **成本最佳化**
- 實施智能快取策略，避免重複查詢
- 根據查詢複雜度動態調整 token 限制
- 監控各模型的使用量和成本效益

### 3. **效能調優**
- 針對不同模型調整 RAG 檢索參數
- 實施請求批次處理和非同步調用
- 建立模型回應時間監控

### 4. **安全性考量**
- 實施內容過濾和安全檢查
- 建立 API 密鑰輪換機制
- 記錄和審計所有模型調用

## ▋總結

Gemini Pro 2.5 和 Claude 4 Sonnet 為企業 RAG 知識庫帶來了新的可能性。透過 Spring AI 1.1 的統一介面，我們可以輕鬆整合這些先進模型，並根據具體需求選擇最適合的模型。

關鍵成功要素：
- **智能模型選擇**: 根據查詢特性自動選擇最佳模型
- **成本控制**: 實施有效的監控和限制機制
- **效能最佳化**: 針對各模型特性調整配置
- **安全性**: 確保企業資料安全和合規性

下一步，我們將探討如何將這些新模型整合到現有的企業級 RAG 系統中，實現更智能、更高效的知識管理解決方案。