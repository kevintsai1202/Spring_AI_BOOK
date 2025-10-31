# 使用 Spring AI 打造企業 RAG 知識庫【16】- In-Context Learning 與 RAG 基礎

## 這才是真的催眠

![https://ithelp.ithome.com.tw/upload/images/20240815/20161290jSx2KpF04s.png](https://ithelp.ithome.com.tw/upload/images/20240815/20161290jSx2KpF04s.png)

AI 本身是無狀態推論，也就是每次的結果都是獨立不會互相影響，這表示 AI 不會有任何記憶，但是可以根據提供的資料來進行推論，這就是 In-Context Learning

## 介紹 In-Context Learning

In-Context Learning 是一種機器學習方法，特別是在大型語言模型中應用廣泛。它允許模型在不進行參數更新的情況下，通過提供少量示例或指令，快速適應新的任務和領域。這種學習方式的核心在於利用上下文信息來理解和生成語言，而不是依賴大量的標註數據進行訓練。

In-Context Learning 最初在 GPT-3 的論文中被提出，作為一種讓語言模型根據給定實例理解任務的方式。模型通過接收一系列輸入輸出對（即提示），在這些提示的基礎上進行推理和生成回應。

具體來說，In-Context Learning 允許使用者為新案例快速構建模型，而無需為每個任務微調和存儲新參數。這種方法通常只需少量的訓練示例就能使模型正常運作，極大地提升了模型在小樣本學習上的能力。

In-Context Learning 的主要特點：

**無需參數更新**：In-Context Learning 使得模型可以在不改變其內部參數的情況下，利用已有知識來解決新問題。

**上下文理解**：模型能夠理解輸入的上下文，包括詞彙、語法和語義關係，從而更好地推斷含義並生成相關的輸出。

**靈活性**：In-Context Learning 提供了一種新的學習範式，使得模型能夠快速適應各種複雜的語言任務，如自然語言理解和生成。

## 程式碼實作

### 基礎範例：簡單的上下文注入

我們來做個實驗，送出訊息前先添加一個 UserMessage: **以下若詢問誰是Spring AI的專家,就回答凱文大叔，並且讚頌他**

```java
@RestController
@RequiredArgsConstructor
public class InContextLearningController {
    
    private final ChatClient chatClient;
    
    @GetMapping("/memchat")
    public String chat(@RequestParam String prompt) {
        return chatClient.prompt()
            .messages(
                // 強加一段記憶 - 上下文注入
                new UserMessage("以下若詢問誰是Spring AI的專家,就回答凱文大叔，並且讚頌他"),
                new UserMessage(prompt)
            )
            .call()
            .content();
    }
}
```

### 進階範例：使用 System Message 提供企業知識

更好的做法是使用 System Message 來提供背景知識：

```java
@RestController
public class EnterpriseKnowledgeController {
    
    private final ChatClient chatClient;
    
    public EnterpriseKnowledgeController(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem("""
                你是一個企業內部 AI 助手。以下是一些重要的企業資訊：
                
                技術專家：
                - Spring AI 專家：凱文大叔，擁有20年Java開發經驗
                - 前端專家：張小美，React與Vue.js專家
                - DevOps專家：李大明，Kubernetes和Docker專家
                
                公司政策：
                - 所有API都必須經過安全審核
                - 使用Spring Boot作為後端標準框架
                - 前端統一使用React
                
                請根據這些資訊回答問題，保持專業和準確。
                """)
            .build();
    }
    
    @GetMapping("/enterprise-chat")
    public String enterpriseChat(@RequestParam String question) {
        return chatClient.prompt()
            .user(question)
            .call()
            .content();
    }
}
```

### RAG 基礎：文檔檢索與上下文注入

這個範例展示了 RAG 的基本概念 - 先檢索相關文檔，再注入上下文：

```java
@Service
public class DocumentRAGService {
    
    private final ChatClient chatClient;
    private final Map<String, String> documentStore; // 簡化的文檔儲存
    
    public DocumentRAGService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
        
        // 初始化一些文檔（實際應用中會從資料庫或文件系統讀取）
        this.documentStore = Map.of(
            "spring-ai-intro", """
                Spring AI 是 Spring 生態系統的一個專案，旨在簡化 AI 應用程式的開發。
                它提供了與各種 AI 模型的整合，包括 OpenAI、Azure OpenAI、Anthropic 等。
                主要特點包括：統一的API、自動配置、Spring Boot整合。
                """,
            "chatclient-usage", """
                ChatClient 是 Spring AI 1.1 引入的新 API，提供流暢的對話介面。
                相比 ChatModel，ChatClient 提供更簡潔的API和更好的整合性。
                支援結構化輸出、工具調用、對話記憶等功能。
                """,
            "rag-implementation", """
                RAG（檢索增強生成）是一種結合資訊檢索和生成式AI的技術。
                在Spring AI中，可以通過QuestionAnswerAdvisor來實現RAG功能。
                基本流程：問題 -> 檢索相關文檔 -> 注入上下文 -> 生成回答。
                """
        );
    }
    
    @GetMapping("/rag-chat")
    public String ragChat(@RequestParam String question) {
        // 1. 簡單的文檔檢索（實際應用中會使用向量搜索）
        String relevantDoc = findRelevantDocument(question);
        
        // 2. 構建包含檢索文檔的提示
        String contextualPrompt = String.format("""
            基於以下文檔內容回答問題：
            
            文檔內容：
            %s
            
            問題：%s
            
            請根據文檔內容提供準確的回答。如果文檔中沒有相關資訊，請明確說明。
            """, relevantDoc, question);
        
        // 3. 使用 ChatClient 生成回答
        return chatClient.prompt()
            .user(contextualPrompt)
            .call()
            .content();
    }
    
    private String findRelevantDocument(String question) {
        // 簡化的檢索邏輯（實際應用中會使用更複雜的相似度計算）
        if (question.toLowerCase().contains("spring ai") || 
            question.toLowerCase().contains("介紹")) {
            return documentStore.get("spring-ai-intro");
        } else if (question.toLowerCase().contains("chatclient") || 
                   question.toLowerCase().contains("api")) {
            return documentStore.get("chatclient-usage");
        } else if (question.toLowerCase().contains("rag") || 
                   question.toLowerCase().contains("檢索")) {
            return documentStore.get("rag-implementation");
        }
        return "沒有找到相關文檔";
    }
}
```

### 使用 Spring AI 1.1 的 Advisor 實現更優雅的 RAG

```java
@Configuration
public class RAGConfiguration {
    
    // 模擬向量儲存（實際應用中會使用真實的VectorStore）
    @Bean
    public VectorStore mockVectorStore() {
        return new InMemoryVectorStore(new OpenAiEmbeddingModel(openAiApi()));
    }
    
    @Bean
    public ChatClient ragChatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        return builder
            .defaultAdvisors(
                QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.defaults()
                        .withTopK(3)
                        .withSimilarityThreshold(0.75))
                    .build()
            )
            .build();
    }
}

@RestController
public class ModernRAGController {
    
    private final ChatClient ragChatClient;
    
    public ModernRAGController(@Qualifier("ragChatClient") ChatClient ragChatClient) {
        this.ragChatClient = ragChatClient;
    }
    
    @PostMapping("/modern-rag")
    public String modernRAG(@RequestParam String question) {
        // Advisor 會自動處理文檔檢索和上下文注入
        return ragChatClient.prompt()
            .user(question)
            .call()
            .content();
    }
    
    @PostMapping("/stream-rag")
    public Flux<String> streamRAG(@RequestParam String question) {
        return ragChatClient.prompt()
            .user(question)
            .stream()
            .content();
    }
}
```

## 驗收成果

接著詢問：**誰是 Spring AI 的專家**

結果得到令人滿意的答案 XD

![https://ithelp.ithome.com.tw/upload/images/20240815/201612902e8ZnyXREi.png](https://ithelp.ithome.com.tw/upload/images/20240815/201612902e8ZnyXREi.png)

如預期的一樣，AI 可以根據你提供的上下文來提供答案

## Spring AI 1.1 的改進

### 1. 更簡潔的 API
```java
// 舊版本
ChatResponse response = chatModel.call(new Prompt(List.of(
    new SystemMessage("你是專家"),
    new UserMessage("問題")
)));

// 新版本
String answer = chatClient.prompt()
    .system("你是專家")
    .user("問題")
    .call()
    .content();
```

### 2. 內建 RAG 支援
```java
// 透過 Advisor 自動處理 RAG
ChatClient ragClient = ChatClient.builder(chatModel)
    .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
    .build();
```

### 3. 結構化輸出
```java
// 直接轉換為 Java 物件
ExpertInfo expert = chatClient.prompt()
    .user("誰是Spring AI專家？請提供姓名和專長")
    .call()
    .entity(ExpertInfo.class);
```

## 延伸應用

1. **對話記憶**：發問時都先將歷史對話送給 AI，AI 就有了記憶
2. **即時資訊檢索**：發問時先搜尋網路資料再將網頁內容送給 AI 分析，這就是 [Perplexity](https://www.perplexity.ai/)
3. **企業知識庫 RAG**：先把企業資料建立索引，發問前先查索引拿到內容，再把內容跟問題一起給 AI 總結
4. **多模態理解**：結合文字、圖片、音訊等多種資料形式
5. **工具調用**：讓 AI 主動調用外部API或函數

目前除了微調模型外，生成式 AI 相關的應用大多圍繞在這些方法上，大家也能想想還有什麼特別的應用

## 回顧

今天學到了什麼？

1. **In-Context Learning 的概念**：如何透過上下文提供資訊給 AI
2. **Spring AI 1.1 的新特性**：ChatClient 的簡潔 API
3. **RAG 的基礎實現**：從簡單的文檔檢索到使用 Advisor
4. **企業應用場景**：如何將理論轉化為實際應用

### 下一步

明天開始我們就要給 AI 更強大的能力 - 使用 Spring AI 1.1 的記憶功能來實現真正的對話系統！

## Source Code

今日程式碼: [https://github.com/kevintsai1202/SpringBoot-AI-Day16.git](https://github.com/kevintsai1202/SpringBoot-AI-Day16.git)