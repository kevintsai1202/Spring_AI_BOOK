## 使用 Spring AI 打造企業 RAG 知識庫【6】- 懶得打字問 AI？用提示詞範本吧

> **更新說明：** 本文章已根據 Spring AI 1.0.1 最新穩定版本進行全面更新，包含最新的提示詞範本功能和使用方式。

## 2025 版本更新說明

本文已基於 **Spring AI 1.0.1** 最新版本進行全面更新，主要變更：
- ✅ 更新為使用 `ChatClient` fluent API
- ✅ 優化提示詞範本使用方式
- ✅ 新增多種範本配置選項
- ✅ 加入最佳實踐建議

# Garbage in garbage out

![https://ithelp.ithome.com.tw/upload/images/20240810/201612907PBWefpaFK.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/201612907PBWefpaFK.jpg)

Garbage in garbage out也適用在AI，有些人就是不知如何問 AI，得到的解答自然也不具參考價值，這時提示詞範本就派上用場了

## 樣板提示詞

我們可以預設一些提示詞，只需將關鍵字換掉即可，下面是一個簡單的範例

### 方法一：使用 ChatClient 配合 PromptTemplate（推薦）

```java
@RestController
@RequiredArgsConstructor
public class AiController {
    private final ChatClient chatClient;

    @GetMapping(value = "/template1")
    public String template1(@RequestParam String llm) {
        String template = "請問{llm}目前有哪些模型，各有甚麼特殊能力";
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("llm", llm));
        
        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}
```

### 方法二：使用 ChatClient Builder 配置範本（新版本推薦）

```java
@RestController
@RequiredArgsConstructor
public class AiController {
    private final ChatModel chatModel;

    @GetMapping(value = "/template1-fluent")
    public String template1Fluent(@RequestParam String llm) {
        return ChatClient.create(chatModel)
                .prompt()
                .user(u -> u.text("請問{llm}目前有哪些模型，各有甚麼特殊能力")
                        .param("llm", llm))
                .call()
                .content();
    }
}
```

### 程式重點說明

- **template** 中包含一個 `{llm}`，可以在建立 Prompt 時替換
- **方法一**：傳統的 PromptTemplate 方式，適合複雜的範本管理
- **方法二**：新版本的 fluent API，更簡潔直觀
- PromptTemplate 可以使用 create 得到 Prompt 物件，也可以使用 createMessage 得到 Message 物件，這部分取決於後面要如何應用

## 驗收成果

看看 Postman 測試得到的結果

![https://ithelp.ithome.com.tw/upload/images/20240804/201612908Nov6aIOo8.png](https://ithelp.ithome.com.tw/upload/images/20240804/201612908Nov6aIOo8.png)

可以看到我們只需輸入 openai，AI 就能依據我們定義的模板來詢問，甚至能依特定格式產出結果

## 外部檔案匯入提示詞

有時模板太長打在程式裡不美觀也不容易維護，PromptTemplate 也支援使用 Resource 讀取文字檔

首先在 resources 目錄下建立一個文字檔: `prompt.st`　內容就是上個範例的模板字串

![https://ithelp.ithome.com.tw/upload/images/20240804/20161290QOFt2ocS0C.png](https://ithelp.ithome.com.tw/upload/images/20240804/20161290QOFt2ocS0C.png)

### 方法一：傳統方式

```java
@Value("classpath:prompt.st")
private Resource templateResource;

@GetMapping(value = "/template2")
public String template2(@RequestParam String llm) {
    PromptTemplate promptTemplate = new PromptTemplate(templateResource);
    Prompt prompt = promptTemplate.create(Map.of("llm", llm));
    return chatClient.prompt(prompt)
            .call()
            .content();
}
```

### 方法二：使用 ChatClient Builder 配置（推薦）

```java
@Value("classpath:prompt.st")
private Resource templateResource;

@GetMapping(value = "/template2-fluent")
public String template2Fluent(@RequestParam String llm) {
    // 讀取範本內容
    String templateContent;
    try {
        templateContent = templateResource.getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new RuntimeException("Failed to read template", e);
    }
    
    return ChatClient.create(chatModel)
            .prompt()
            .user(u -> u.text(templateContent)
                    .param("llm", llm))
            .call()
            .content();
}
```

這樣看起來是不是簡潔許多？

## 多參數提示詞

最後我們來寫一個演算法產生器，查詢時只需輸入程式語言以及需要的演算法，AI 就能給我們詳細的程式以及說明了

先在 resources 增加一個 `code.st`，內容放入

> language {language}
>
> method {methodName}
>
> 請提供 {language} 語言的 {methodName} 範例，並提供詳細的中文說明

### 透過RequestParam傳入參數

因為上面有兩個需替換的字串，程式碼需要有兩個 _@RequestParam_

```java
@GetMapping(value = "/template3")
public String template3(@RequestParam String language, @RequestParam String methodName) {
```

### 多參數傳入提示詞範本

#### 方法一：使用 PromptTemplate

```java
@Value("classpath:code.st")
private Resource templateResource2;

@GetMapping(value = "/template3")
public String template3(@RequestParam String language, @RequestParam String methodName) {
    PromptTemplate promptTemplate = new PromptTemplate(templateResource2);
    Prompt prompt = promptTemplate.create(Map.of("language", language, "methodName", methodName));
    return chatClient.prompt(prompt)
            .call()
            .content();
}
```

#### 方法二：使用 ChatClient fluent API（推薦）

```java
@Value("classpath:code.st")
private Resource templateResource2;

@GetMapping(value = "/template3-fluent")
public String template3Fluent(@RequestParam String language, @RequestParam String methodName) {
    try {
        String templateContent = templateResource2.getContentAsString(StandardCharsets.UTF_8);
        
        return ChatClient.create(chatModel)
                .prompt()
                .user(u -> u.text(templateContent)
                        .param("language", language)
                        .param("methodName", methodName))
                .call()
                .content();
    } catch (IOException e) {
        throw new RuntimeException("Failed to read template", e);
    }
}
```

## 進階用法：範本管理服務

對於企業級應用，建議建立專門的範本管理服務：

```java
@Service
@RequiredArgsConstructor
public class PromptTemplateService {
    private final ChatClient chatClient;
    private final Map<String, Resource> templates = new HashMap<>();
    
    @PostConstruct
    public void initTemplates() {
        // 初始化範本資源
        templates.put("llm-query", new ClassPathResource("templates/llm-query.st"));
        templates.put("code-gen", new ClassPathResource("templates/code-generation.st"));
    }
    
    public String processTemplate(String templateName, Map<String, Object> params) {
        Resource template = templates.get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        
        try {
            String templateContent = template.getContentAsString(StandardCharsets.UTF_8);
            
            ChatClient.PromptUserSpec userSpec = chatClient.prompt().user(u -> u.text(templateContent));
            
            // 動態設定參數
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                userSpec = userSpec.param(entry.getKey(), entry.getValue());
            }
            
            return userSpec.call().content();
        } catch (IOException e) {
            throw new RuntimeException("Failed to process template", e);
        }
    }
}
```

## 驗收成果

來看看執行結果(中間程式碼太長，將其省略，完整內容可自行測試)

![https://ithelp.ithome.com.tw/upload/images/20240804/20161290r0eIQoDsDx.png](https://ithelp.ithome.com.tw/upload/images/20240804/20161290r0eIQoDsDx.png)

![https://ithelp.ithome.com.tw/upload/images/20240804/201612902JzcltxIPy.png](https://ithelp.ithome.com.tw/upload/images/20240804/201612902JzcltxIPy.png)

還記得昨天提到 Message 有四種嗎？PromptTemplate 產出的 Prompt 預設就是使用 UserMessage 建立，Spring AI 也同時準備了其他三個 Message 的 PromptTemplate

![https://ithelp.ithome.com.tw/upload/images/20240804/20161290sJLPWTlUKT.png](https://ithelp.ithome.com.tw/upload/images/20240804/20161290sJLPWTlUKT.png)

## 最佳實踐建議

1. **優先使用 ChatClient fluent API**：更現代化、類型安全
2. **範本檔案管理**：將複雜範本存放在獨立檔案中
3. **參數驗證**：對用戶輸入進行適當驗證
4. **錯誤處理**：妥善處理範本讀取和處理過程中的異常
5. **效能優化**：考慮範本內容的快取機制

## 回顧

今天學到甚麼

- 使用 PromptTemplate 將模板字串的關鍵字換為輸入的內容再去 AI 查詢結果
- PromptTemplate 使用 Resource 讀取存於檔案中的模板字串
- **新增**：學會使用 ChatClient fluent API 處理提示詞範本
- **新增**：了解企業級範本管理的最佳實踐

## Source Code

今日的原始碼:

[https://github.com/kevintsai1202/SpringBoot-AI-Day6.git](https://github.com/kevintsai1202/SpringBoot-AI-Day6.git)

