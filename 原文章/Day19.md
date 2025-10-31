# 使用 Spring AI 打造企業 RAG 知識庫【19】- Spring AI的鏈式增強器（2025年最新版）

> **重要更新說明**：本文已更新至Spring AI 1.0.0以上版本，使用最新的Advisor API。相較於原版本使用已被廢棄的`RequestResponseAdvisor`，現在採用新的`CallAdvisor`和`StreamAdvisor`介面。

## AOP無所不在

![AOP概念圖](https://ithelp.ithome.com.tw/upload/images/20240819/20161290PpCeC08JCZ.jpg)

Spring 框架有兩個很重要的觀念，一個是 IoC，另一個則是 AOP，只是 AOP 大多整合進應用中，開發人員直接撰寫 AOP 的機會越來越少，很多開發人員甚至都忘了還有這種功能

AOP 的基本概念就是可以攔截指定的方法並且增強方法的功能，且無需修改到主要的業務代碼，使業務與非業務處理邏輯分離

例如 `@Transactional` 標註，只要元件的方法上增加 `@Transactional` 標註，Spring 會自動在方法中進行 Open、Commit，並且在業務處理失敗時，執行 Rollback。

另一個常見的應用是 log，使用 AOP 可以在不更動原程式的情況下在執行前後加上 log 紀錄

## Spring AI提供的Advisor(增強器)

在 Spring AOP 中 Advisor(增強器) 的作用就是找到切入點對應的方法，並在定義的時機執行 Advice(增強方法)，還記得 [Day15](https://ithelp.ithome.com.tw/articles/10344753) 最後提到的 `.advisors()` 嗎？這正是要放入 **Advisor** 的地方。

### API 更新重點

在最新版本的Spring AI中，Advisor系統經歷了重大更新：

#### 舊版API（已廢棄）
- `RequestResponseAdvisor` - 已在1.0.0-M3標記為deprecated並在1.0.0-RC1中完全移除
- `CallAroundAdvisor` / `StreamAroundAdvisor` - 在1.0.0-RC1中被重新命名

#### 新版API（當前使用）
- `CallAdvisor` - 用於非串流場景
- `StreamAdvisor` - 用於串流場景
- `ChatClientRequest` / `ChatClientResponse` - 取代原本的AdvisedRequest/AdvisedResponse

### Spring AI內建的Advisor類別

目前 Spring AI 提供的實作類別包括：

![Advisor類別圖](https://ithelp.ithome.com.tw/upload/images/20240819/201612900g7q0iHGAj.png)

- **MessageChatMemoryAdvisor**: 將訊息送出前加入 ChatMemory，之後將所有 ChatMemory 資料以 List 方式送給 AI 處理
- **PromptChatMemoryAdvisor**: 將訊息送出前加入 ChatMemory，之後將所有 ChatMemory 訊息加入 SystemMessage，最後將 UserMessage 與 SystemMessage 一同送給 AI 處理
- **VectorStoreChatMemoryAdvisor**: 前面兩個都是處理短期聊天資訊，一旦系統關閉就會失去所有聊天內容，而此類別則可在訊息送出前加入向量資料庫，並從向量資料庫中取出相似聊天內容送給 AI 處理，讓聊天訊息可以長久保存
- **QuestionAnswerAdvisor**: 此類別即是 RAG 的調用實作，凱文大叔會在後面章節詳細說明

## 程式碼實作

今天先來實作短期記憶，凱文大叔將昨天的 Service 進行改寫，原本手動加入 InMemoryChatMemory 以及取得歷史訊息的部分不用自己動手了

### 使用最新API的ChatService

```java
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatClient chatClient;
    private final ChatMemory chatMemory = new InMemoryChatMemory();

    public String chat(String chatId, String userMessage) {
        return this.chatClient.prompt()
            .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(chatId)
                .lastN(30)
                .build())
            .user(userMessage)
            .call().content();
    }
}
```

### 新舊API對比

#### 舊版寫法（已廢棄）
```java
// 使用已廢棄的RequestResponseAdvisor
.advisors(new MessageChatMemoryAdvisor(chatMemory, chatId, 30))
```

#### 新版寫法（推薦）
```java
// 使用最新的Builder模式
.advisors(MessageChatMemoryAdvisor.builder(chatMemory)
    .conversationId(chatId)
    .lastN(30)
    .build())
```

### 建議的最佳實踐寫法

推薦在ChatClient建構時就預設Advisor，這樣可以獲得更好的效能：

```java
@Configuration
public class ChatConfig {
    
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        ChatMemory chatMemory = new InMemoryChatMemory();
        
        return ChatClient.builder(chatModel)
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build()
            )
            .build();
    }
}

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {
    private final ChatClient chatClient;

    public String chat(String chatId, String userMessage) {
        log.info("Chat ID:{} User Message:{}", chatId, userMessage);
        
        String assistantMessage = this.chatClient.prompt()
            // 在執行時設定advisor參數
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
            .user(userMessage)
            .call().content();
            
        log.info("Chat ID:{} Assistant Message:{}", chatId, assistantMessage);
        return assistantMessage;
    }
}
```

## 驗收成果

![測試結果](https://ithelp.ithome.com.tw/upload/images/20240819/20161290WBgQmjFsi1.png)

> 可以看出效果跟昨天一樣，MessageChatMemoryAdvisor 與 PromptChatMemoryAdvisor 只差在歷史訊息送出的方式，大家可以挑一種喜歡的來使用即可

## 新版API的優勢

1. **更清晰的介面分離**：將非串流和串流場景分開處理
2. **Builder模式**：提供更靈活的配置方式
3. **更好的效能**：支援在建構時預設Advisor
4. **參數傳遞優化**：使用`adviseContext`改善參數共享機制
5. **更好的可觀測性**：完整整合到Spring的監控堆疊中

## 回顧

今天學到了甚麼?

- AOP 基本概念
- Spring AI 新版Advisor API的重大更新
- 新舊API的對比與遷移指南
- 如何在 ChatClient 設定最新版的 Advisor，完成短期記憶
- 最佳實踐的建議寫法

> 新版的Advisor API提供了更好的效能和可維護性。`.advisors()` 是可放入多個 Advisor 的，除了 Spring AI 提供的以外，開發人員也可以自行實作 `CallAdvisor` 和 `StreamAdvisor`，明天我們就將 log 改寫成 Advisor 讓大家熟悉最新Advisor 鏈的寫法

## 遷移提醒

如果您正在使用舊版的Spring AI，請注意：
- `RequestResponseAdvisor` 已在1.0.0-RC1中完全移除
- `CallAroundAdvisor` / `StreamAroundAdvisor` 已重新命名為 `CallAdvisor` / `StreamAdvisor`
- 建議盡快遷移到新版API以獲得更好的效能和維護性

## Source Code:

程式碼下載: [https://github.com/kevintsai1202/SpringBoot-AI-Day19.git](https://github.com/kevintsai1202/SpringBoot-AI-Day19.git)
（注意：GitHub程式碼已更新至最新版本API）

