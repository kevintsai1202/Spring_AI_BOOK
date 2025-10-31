## 使用 Spring AI 打造企業 RAG 知識庫【12】- 讓AI讀取企業資訊 - Tool Calling (中)

> 📝 **更新說明**：本文章已根據 Spring AI 1.1-SNAPSHOT 最新版本進行更新，採用新的 Tool Calling API 和 ChatClient fluent API。

# 在地化的第一步

![https://ithelp.ithome.com.tw/upload/images/20240811/20161290JBnMyu335V.jpg](https://ithelp.ithome.com.tw/upload/images/20240811/20161290JBnMyu335V.jpg)

只是問時間似乎無法體會 Tool Calling 有甚麼特別之處，今天凱文大叔模擬從後端取得資料並透過 AI 幫我們分析，看看工具該如何撰寫

## 程式碼實作

### 程式目標: 詢問 AI 公司每個產品去年的銷量，並統計占銷量的比例是多少

1. **撰寫工具類別**: 建立產品銷售查詢工具

```java
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonClassDescription;

public class ProductSalesTools {
    
    // 產品資料模型，使用 record 簡化程式碼
    public record Product(String year, String model, Integer quantity) {}
    
    @Tool(description = "Get company product sales information by year and product")
    public ProductSalesResponse getProductSales(
        @ToolParam(description = "銷售年份", required = false) String year,
        @ToolParam(description = "產品型號", required = false) String product
    ) {
        // 模擬從資料庫或服務取得產品銷售資料
        List<Product> products = List.of(
            new Product("2022", "PD-1405", 12500),
            new Product("2023", "PD-1234", 10000),
            new Product("2023", "PD-1235", 1500),
            new Product("2023", "PD-1385", 15000),
            new Product("2024", "PD-1255", 15000),
            new Product("2024", "PD-1300", 12000),
            new Product("2024", "PD-1405", 12500),
            new Product("2024", "PD-1235", 15000),
            new Product("2024", "PD-1385", 15000)
        );
        
        return new ProductSalesResponse(products);
    }
    
    // 回應資料模型
    @JsonClassDescription("公司產品銷售資料回應")
    public record ProductSalesResponse(
        @JsonProperty("products") 
        @JsonPropertyDescription("產品銷售清單") 
        List<Product> products
    ) {}
}
```

2. **建立日期時間工具**: 重複使用昨天的時間工具

```java
import java.time.LocalDateTime;
import org.springframework.ai.tool.annotation.Tool;

public class DateTimeTools {
    
    @Tool(description = "Get the current date and time")
    String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }
}
```

3. **設定 ChatClient**: 使用新的 API 配置多個工具

```java
@Configuration
public class AiConfig {
    
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
            .defaultTools(
                new ProductSalesTools(),
                new DateTimeTools()
            )
            .build();
    }
}
```

4. **建立控制器**: 使用 ChatClient 處理請求

```java
@RestController
public class AiController {
    
    private final ChatClient chatClient;
    
    public AiController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    @GetMapping("/func")
    public String func(@RequestParam String prompt) {
        return chatClient.prompt()
            .user(prompt)
            .call()
            .content();
    }
    
    // 也可以針對特定請求動態添加工具
    @GetMapping("/sales")
    public String salesAnalysis(@RequestParam String prompt) {
        return ChatClient.create(chatClient)
            .prompt(prompt)
            .tools(new ProductSalesTools())  // 只使用銷售工具
            .call()
            .content();
    }
}
```

### 與企業系統整合的進階範例

如果要與實際的企業系統整合，可以這樣實作：

```java
@Component
public class EnterpriseSalesTools {
    
    private final ProductService productService;
    private final SecurityContext securityContext;
    
    public EnterpriseSalesTools(ProductService productService, 
                               SecurityContext securityContext) {
        this.productService = productService;
        this.securityContext = securityContext;
    }
    
    @Tool(description = "Get product sales data with security validation")
    public List<Product> getSecureProductSales(
        @ToolParam(description = "銷售年份") String year,
        @ToolParam(description = "產品類別", required = false) String category
    ) {
        // 檢查使用者權限
        if (!securityContext.hasRole("SALES_VIEWER")) {
            throw new SecurityException("Insufficient permissions");
        }
        
        // 從實際服務取得資料
        return productService.getSalesByYear(year, category);
    }
}
```

### 程式重點說明

#### 新版 API 的優勢

1. **簡化的工具定義**: 
   - 使用 `@Tool` 註解直接標註方法
   - 使用 `@ToolParam` 提供參數描述和約束
   - 不需要複雜的 FunctionCallbackWrapper

2. **更好的類型安全**: 
   - 直接使用 Java 方法，編譯時檢查
   - 支援複雜的資料結構和泛型

3. **靈活的工具管理**:
   - 支援預設工具（defaultTools）
   - 支援執行時工具（runtime tools）
   - 支援工具名稱動態解析

4. **企業級功能**:
   - 與 Spring Security 無縫整合
   - 支援依賴注入和 Spring Bean 管理
   - 支援 AOP 和事務管理

#### 工具參數最佳實踐

```java
public class BestPracticeTools {
    
    @Tool(description = "Search products with comprehensive filtering options")
    public List<Product> searchProducts(
        @ToolParam(description = "產品名稱關鍵字", required = false) 
        String keyword,
        
        @ToolParam(description = "價格範圍：格式為 'min-max'，如 '100-500'", required = false) 
        String priceRange,
        
        @ToolParam(description = "產品類別：ELECTRONICS, CLOTHING, BOOKS", required = false) 
        ProductCategory category,
        
        @ToolParam(description = "結果數量限制，預設為 10", required = false) 
        Integer limit
    ) {
        // 實作搜尋邏輯
        return productService.search(keyword, priceRange, category, limit != null ? limit : 10);
    }
}
```

#### Tool Context 的應用

Spring AI 1.1 新增了 ToolContext 功能，可以傳遞額外的上下文資訊：

```java
public class ContextAwareTools {
    
    @Tool(description = "Get user-specific product recommendations")
    public List<Product> getRecommendations(
        @ToolParam(description = "產品類別") String category,
        ToolContext toolContext  // 接收上下文資訊
    ) {
        // 從 context 取得使用者資訊
        String userId = (String) toolContext.getContext().get("userId");
        String tenantId = (String) toolContext.getContext().get("tenantId");
        
        return recommendationService.getPersonalizedRecommendations(
            userId, tenantId, category);
    }
}
```

使用方式：

```java
@GetMapping("/recommendations")
public String getRecommendations(@RequestParam String prompt, 
                               HttpServletRequest request) {
    return chatClient.prompt()
        .user(prompt)
        .tools(new ContextAwareTools())
        .toolContext(Map.of(
            "userId", getCurrentUserId(request),
            "tenantId", getCurrentTenantId(request)
        ))
        .call()
        .content();
}
```

- 產品的 Class 不一定要放在 Tool 內，這裡可以放 Entity，取得資料的方式可以透過資料庫
- 若要綁定 @Service 物件，記得也把 Tool Class 設為 @Component，這是 Spring 基本的概念
- 傳入的參數可以作為後端資料篩選的參數，減少資料也能減少送給 AI 的 Token 數量
- Spring MVC 的部分可以搭配 Spring Security，有些機密資料不能讓沒權限的人取得
- ChatClient 支援多個 Tool 的配置

> 看到這邊應該能開始體會和 Spring Boot 框架整合有甚麼好處了，從 Spring MVC / Spring Data JPA / Spring Security 都能整合進來，企業引入 AI 的同時還能兼顧安全性以及資料的彈性

## 驗收成果

我詢問的問題是 `公司每個產品去年的銷量，並統計占銷量的比例是多少`

### 未使用Tool Call

![https://ithelp.ithome.com.tw/upload/images/20240811/20161290kfXpzWLlP8.png](https://ithelp.ithome.com.tw/upload/images/20240811/20161290kfXpzWLlP8.png)

### 使用Tool Call

![https://ithelp.ithome.com.tw/upload/images/20240812/20161290MaveECYAa1.png](https://ithelp.ithome.com.tw/upload/images/20240812/20161290MaveECYAa1.png)

## 新版 API 的其他功能特色

### 1. Function 型工具支援

除了方法型工具，也支援函數型工具：

```java
@Configuration
public class FunctionToolsConfig {
    
    @Bean
    @Description("Calculate product discount based on quantity")
    Function<DiscountRequest, DiscountResponse> calculateDiscount() {
        return request -> {
            double discountRate = request.quantity() > 100 ? 0.1 : 0.05;
            double discountAmount = request.price() * discountRate;
            return new DiscountResponse(discountAmount, discountRate);
        };
    }
    
    public record DiscountRequest(int quantity, double price) {}
    public record DiscountResponse(double discountAmount, double discountRate) {}
}
```

### 2. 工具名稱動態解析

```java
@GetMapping("/dynamic")
public String dynamicTools(@RequestParam String prompt, 
                          @RequestParam List<String> toolNames) {
    return chatClient.prompt()
        .user(prompt)
        .toolNames(toolNames)  // 動態指定工具名稱
        .call()
        .content();
}
```

### 3. 異常處理和結果轉換

```java
public class AdvancedTools {
    
    @Tool(description = "Get product with custom result conversion",
          resultConverter = CustomResultConverter.class)
    public Product getProduct(@ToolParam(description = "產品ID") Long id) {
        try {
            return productService.findById(id);
        } catch (Exception e) {
            throw new ToolExecutionException("Failed to retrieve product: " + id, e);
        }
    }
}

@Component
public class CustomResultConverter implements ToolCallResultConverter {
    @Override
    public String convert(Object result, Type returnType) {
        // 自定義結果轉換邏輯
        return JsonUtils.toJson(result);
    }
}
```

## 回顧

今天學到的內容:

1. 新版 Tool Calling API 的企業級應用
2. 多個工具的配置和管理方式
3. 與 Spring 生態系統的深度整合
4. 工具參數的最佳實踐
5. ToolContext 的使用方式
6. 安全性和權限控制的整合

### 新舊 API 對比總結

| 功能 | 舊版 Function Calling | 新版 Tool Calling |
|------|---------------------|-------------------|
| 工具定義 | FunctionCallbackWrapper.builder() | @Tool 註解 |
| 參數描述 | JSON Schema 手寫 | @ToolParam 註解 |
| 工具註冊 | @Bean FunctionCallback | 直接傳入實例或使用 Bean |
| 類型安全 | 運行時檢查 | 編譯時檢查 |
| IDE 支援 | 有限 | 完整支援 |
| Spring 整合 | 基礎整合 | 深度整合 |

新版的 Tool Calling API 不僅簡化了開發流程，更提供了企業級的功能支援，讓 AI 應用能夠真正融入企業的業務流程中。

## Source Code

更新後的完整範例程式碼請參考最新的 Spring AI 官方文檔

