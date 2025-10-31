# 使用 Spring AI 打造企業 RAG 知識庫【13】- Function Calling 最終組合技 (下)

## 讓AI自己調用程式

![https://ithelp.ithome.com.tw/upload/images/20240812/20161290El6Y5FmnHN.png](https://ithelp.ithome.com.tw/upload/images/20240812/20161290El6Y5FmnHN.png)

原本 Function Call 只打算寫兩篇，不過最近參考其他框架發現 Function Calling 是會被反覆調用的，測試後發現 Spring AI 也有類似的效果

Spring AI 會先去調用符合的 Function 並取得資料，之後會再拿取得的資料確認是否還有可匹配的 Function，並將目前取得的結果再次呼叫 Function

來看看怎麼活用這樣的技巧吧

## 程式碼實作

### 程式目標: 詢問 AI 公司去年最熱銷的產品，並列出該產品所有型號

在不知道 Function Calling 會重複調用時你可能會把產品改成一個 Class，並將所有產品型號放在 List 變數中，這樣做雖然也沒問題，不過長久下來會浪費許多額外花費，因為程式會將所有產品資料包含型號一起送給 AI 處理，當資料多的時候就形成額外的花費

運用重複調用的特性，我們可以增加一隻查詢產品型號的 Function，Request 的參數是產品名稱，Response 則放產品型號的 List，若 Prompt 有提到需要列出產品型號時，Spring AI 就會主動再去調用這隻查詢型號的 Function，下面直接看 Function 如何增加

### 1. 撰寫工具類別 ProductDetailsTools

使用 Spring AI 1.1 的新 `@Tool` 註解來定義工具方法：

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import java.util.List;

public class ProductDetailsTools {
    
    public record ProductDetail(String product, List<String> models) {}
    
    @Tool(description = "Get the product's model(產品型號) list by product name")
    public ProductDetail getProductModels(
        @ToolParam(description = "產品名稱") String product) {
        
        //模擬資料，企業通常會透過DB或是其他API查詢內容
        List<ProductDetail> productModels = List.of(
            new ProductDetail("PD-1405", List.of("1405-001","1405-002","1405-003")),
            new ProductDetail("PD-1234", List.of("1234-1","1234-2","1234-3","1234-4")),
            new ProductDetail("PD-1235", List.of("1235-4","1235-5")),
            new ProductDetail("PD-1385", List.of("1385-1","1385-2","1385-3")),
            new ProductDetail("PD-1255", List.of("1255-1")),
            new ProductDetail("PD-1300", List.of("1300-1","1300-2","1300-3"))
        );
        
        //模擬查詢後回傳的結果
        return productModels.stream()
            .filter(pd -> pd.product.equals(product))
            .findFirst()
            .orElse(new ProductDetail(product, List.of("無此產品型號")));
    }
}
```

### 2. 撰寫產品銷量工具類別 ProductSalesTools

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import java.util.List;

public class ProductSalesTools {
    
    public record ProductSales(String product, int salesVolume) {}
    
    @Tool(description = "Get the products sales volume at specific year")
    public List<ProductSales> getProductSales(
        @ToolParam(description = "年份，格式如：2023") int year) {
        
        //模擬資料，企業通常會透過DB或是其他API查詢內容
        if (year == 2023) {
            return List.of(
                new ProductSales("PD-1385", 15000),
                new ProductSales("PD-1234", 10000),
                new ProductSales("PD-1405", 8500),
                new ProductSales("PD-1235", 1500),
                new ProductSales("PD-1255", 800),
                new ProductSales("PD-1300", 500)
            );
        }
        return List.of();
    }
}
```

### 3. 使用 ChatClient 調用工具

使用 Spring AI 1.1 的新 ChatClient API：

```java
@RestController
public class ProductController {
    
    private final ChatClient chatClient;
    
    public ProductController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    @GetMapping("/func")
    public String func(String prompt) {
        return chatClient.prompt(prompt)
            .tools(new ProductSalesTools(), new ProductDetailsTools())
            .call()
            .content();
    }
}
```

### 程式重點說明

- **新的 @Tool 註解**：Spring AI 1.1 引入了 `@Tool` 註解，讓工具定義更加簡潔
- **@ToolParam 註解**：用於描述工具參數，幫助 AI 更好地理解參數用途
- **ChatClient fluent API**：使用新的流暢 API，比直接調用 ChatModel 更直觀
- **自動工具調用**：ChatClient 會自動處理工具調用和結果返回
- **多工具支援**：可以同時提供多個工具給 AI 使用

## 驗收成果

直接看測試結果，這次詢問的問題是 `請給我2023年銷售量前三名的產品，並列出該產品所有型號，使用表格方式呈現`

![https://ithelp.ithome.com.tw/upload/images/20240812/20161290C8EIsGeMqI.png](https://ithelp.ithome.com.tw/upload/images/20240812/20161290C8EIsGeMqI.png)

表格的部分是採用 Markdown 格式，若有前端程式搭配就能呈現表格的內容

| 產品型號 | 銷售量 | 所有型號 |
| --- | --- | --- |
| PD-1385 | 15000 | 1385-1, 1385-2, 1385-3 |
| PD-1234 | 10000 | 1234-1, 1234-2, 1234-3, 1234-4 |
| PD-1405 | 8500 | 1405-1, 1405-2, 1405-3 |

## Spring AI 1.1 新特性

### ChatClient API 優勢

1. **流暢介面**：提供更直觀的 API 調用方式
2. **工具整合**：內建工具調用支援，無需手動處理
3. **類型安全**：更好的編譯時檢查
4. **配置簡化**：減少樣板代碼

### @Tool 註解優勢

1. **簡化配置**：不再需要繁瑣的 FunctionCallbackWrapper
2. **自動註冊**：框架自動處理工具註冊
3. **參數描述**：透過 @ToolParam 提供更詳細的描述
4. **類型推斷**：自動處理參數和返回值類型

## 回顧

今天學到的內容:

1. Spring AI 1.1 的新 ChatClient API 使用方式
2. @Tool 註解的簡化工具定義方法
3. 多層 Function Calling 的實現
4. 新版本 API 的優勢和改進

> **重要升級說明**：本文基於 Spring AI 1.1-SNAPSHOT 版本，使用了最新的 ChatClient API 和 @Tool 註解。如果你還在使用舊版本，建議升級以享受更好的開發體驗和性能提升。

## 與舊版本的差異

### 舊版本 (Spring AI 1.0)
```java
// 舊的 FunctionCallbackWrapper 方式
@Bean
public FunctionCallback productDetailsInfo() {
    return FunctionCallbackWrapper.builder(new ProductDetailsFunction())
        .withName("ProductDetailsInfo")
        .withDescription("Get the product's model list")
        .build();
}

// 舊的 ChatModel 直接調用
return chatModel.call(
    new Prompt(prompt, OpenAiChatOptions.builder()
        .withFunction("ProductSalesInfo")
        .withFunction("ProductDetailsInfo")
        .build())
).getResult().getOutput().getContent();
```

### 新版本 (Spring AI 1.1)
```java
// 新的 @Tool 註解方式
@Tool(description = "Get the product's model list")
public ProductDetail getProductModels(String product) {
    // 實現邏輯
}

// 新的 ChatClient API
return chatClient.prompt(prompt)
    .tools(new ProductSalesTools(), new ProductDetailsTools())
    .call()
    .content();
```

新版本的優勢顯而易見：代碼更簡潔、配置更簡單、維護更容易。

## Source Code

今日的程式碼: [https://github.com/kevintsai1202/SpringBoot-AI-Day13.git](https://github.com/kevintsai1202/SpringBoot-AI-Day13.git)