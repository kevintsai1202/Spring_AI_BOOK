## 使用 Spring AI 打造企業 RAG 知識庫【7】- 如何跟 ChatGPT 一樣處理多模態資料

> **更新說明：** 本文章已根據 Spring AI 1.0.1 最新穩定版本進行全面更新，包含最新的多模態資料處理功能。

## 2025 版本更新說明

本文已基於 **Spring AI 1.0.1** 最新版本進行全面更新，主要變更：
- ✅ 新增 ChatClient 多模態處理方式
- ✅ 更新支援模型清單和能力對比
- ✅ 加入批次處理和進階功能
- ✅ 優化錯誤處理和最佳實踐建議

# 多模態就是同時提供兩種以上資料源

![https://ithelp.ithome.com.tw/upload/images/20240810/201612905TZ6mYSAy1.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/201612905TZ6mYSAy1.jpg)

還記得 Message 這張 UML 圖片嗎？在 UserMessage 中其實是可以包含多媒體檔案的，這也是最近 AI 強調的多模態（ **Multimodality**）

![https://ithelp.ithome.com.tw/upload/images/20240806/2016129049tSDbXBcM.jpg](https://ithelp.ithome.com.tw/upload/images/20240806/2016129049tSDbXBcM.jpg)

## 支援多模態的 AI 模型

目前有支援多模態的模型如下，其他模型送出多媒體檔案時可是會出現錯誤的：

### 圖像輸入支援的模型
- **OpenAI**: GPT-4 Vision, GPT-4o, GPT-4o mini
- **Anthropic**: Claude 3 (Opus, Sonnet, Haiku)
- **Google**: Gemini Pro Vision, Gemini 1.5 Pro
- **Ollama**: LlaVa, Bakllava 等本地模型
- **AWS Bedrock**: Claude 3 系列
- **Azure OpenAI**: GPT-4 Vision, GPT-4o

### 最新模型能力對比表

| 模型提供商 | 模型名稱 | 圖像 | 音頻 | 視頻 | 文件 | 成本效益 |
|-----------|---------|------|------|------|------|----------|
| OpenAI | GPT-4o | ✅ | ✅ | ✅ | ✅ | 高 |
| OpenAI | GPT-4o mini | ✅ | ❌ | ❌ | ✅ | 極高 |
| Anthropic | Claude 3.5 Sonnet | ✅ | ❌ | ❌ | ✅ | 高 |
| Google | Gemini 1.5 Pro | ✅ | ✅ | ✅ | ✅ | 中 |
| Ollama | LlaVa | ✅ | ❌ | ❌ | ❌ | 極高(本地) |

### 輸入輸出格式支援表

| Input | Output | Examples |
| --- | --- | --- |
| Language/Code/Images (Multi-Modal) | Language/Code | GPT-4o - OpenAI, Gemini 1.5 Pro |
| Language/Code | Language/Code | GPT-3.5 - OpenAI, Claude 3 Haiku |
| Language | Image | DALL-E 3 - OpenAI, Midjourney |
| Language/Image | Image | Stable Diffusion, Midjourney |
| Language | Audio | OpenAI TTS, ElevenLabs |
| Audio | Language | OpenAI Whisper |
| Text | Numbers | Various Embedding Models |

除了文字以外，圖片、影片以及聲音都屬於多媒體檔案

## 程式碼實作

接下來我們寫一隻程式模擬 ChatGPT 上傳檔案並詢問圖片在說明甚麼內容

### 方法一：使用 ChatModel（傳統方式）

```java
@RestController
@RequiredArgsConstructor
public class AiController {
    private final ChatModel chatModel;

    @PostMapping(value = "/imagequery")
    public String imageQuery(@RequestParam MultipartFile file, @RequestParam String message) {
        try {
            UserMessage userMessage = new UserMessage(
                    message,
                    new Media(
                        MimeTypeUtils.parseMimeType(file.getContentType()),
                        file.getResource())
                    );
            return chatModel.call(userMessage);
        } catch (Exception e) {
            return "圖片處理失敗: " + e.getMessage();
        }
    }
}
```

### 方法二：使用 ChatClient（推薦）

```java
@RestController
@RequiredArgsConstructor
public class MultimodalController {
    private final ChatClient chatClient;
    
    @PostMapping(value = "/image-analysis")
    public String analyzeImage(@RequestParam MultipartFile file, 
                              @RequestParam String message) {
        try {
            return chatClient.prompt()
                    .user(u -> u.text(message)
                            .media(MimeTypeUtils.parseMimeType(file.getContentType()), 
                                   file.getResource()))
                    .call()
                    .content();
        } catch (Exception e) {
            return "圖片分析失敗: " + e.getMessage();
        }
    }
    
    @PostMapping(value = "/image-analysis-advanced")
    public ResponseEntity<ImageAnalysisResult> analyzeImageAdvanced(
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "請詳細描述這張圖片的內容") String prompt) {
        
        try {
            // 驗證檔案類型
            if (!isValidImageFile(file)) {
                return ResponseEntity.badRequest()
                        .body(new ImageAnalysisResult("", "不支援的檔案格式"));
            }
            
            // 使用 ChatClient 進行圖片分析
            String analysis = chatClient.prompt()
                    .user(u -> u.text(prompt)
                            .media(MimeTypeUtils.parseMimeType(file.getContentType()), 
                                   file.getResource()))
                    .call()
                    .content();
            
            return ResponseEntity.ok(new ImageAnalysisResult(analysis, "success"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImageAnalysisResult("", "分析失敗: " + e.getMessage()));
        }
    }
    
    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    
    // DTO 類別
    public record ImageAnalysisResult(String analysis, String status) {}
}
```

### 方法三：批次處理多張圖片

```java
@RestController
@RequiredArgsConstructor
public class BatchMultimodalController {
    private final ChatClient chatClient;
    
    @PostMapping(value = "/batch-image-analysis")
    public ResponseEntity<List<BatchAnalysisResult>> batchAnalyzeImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "描述這張圖片") String basePrompt) {
        
        List<BatchAnalysisResult> results = files.parallelStream()
                .map(file -> analyzeImageFile(file, basePrompt))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }
    
    @PostMapping(value = "/compare-images")
    public ResponseEntity<String> compareImages(
            @RequestParam("image1") MultipartFile image1,
            @RequestParam("image2") MultipartFile image2,
            @RequestParam(defaultValue = "比較這兩張圖片的差異") String prompt) {
        
        try {
            // 使用 ChatClient 同時處理兩張圖片
            String comparison = chatClient.prompt()
                    .user(u -> u.text(prompt)
                            .media(MimeTypeUtils.parseMimeType(image1.getContentType()), 
                                   image1.getResource())
                            .media(MimeTypeUtils.parseMimeType(image2.getContentType()), 
                                   image2.getResource()))
                    .call()
                    .content();
            
            return ResponseEntity.ok(comparison);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("圖片比較失敗: " + e.getMessage());
        }
    }
    
    private BatchAnalysisResult analyzeImageFile(MultipartFile file, String prompt) {
        try {
            String analysis = chatClient.prompt()
                    .user(u -> u.text(prompt)
                            .media(MimeTypeUtils.parseMimeType(file.getContentType()), 
                                   file.getResource()))
                    .call()
                    .content();
            
            return new BatchAnalysisResult(file.getOriginalFilename(), analysis, "success");
        } catch (Exception e) {
            return new BatchAnalysisResult(file.getOriginalFilename(), 
                                         null, "failed: " + e.getMessage());
        }
    }
    
    // DTO 類別
    public record BatchAnalysisResult(String filename, String analysis, String status) {}
}
```

### 方法四：支援多種媒體類型

```java
@RestController
@RequiredArgsConstructor
public class UniversalMultimodalController {
    private final ChatClient chatClient;
    
    @PostMapping(value = "/universal-media-analysis")
    public ResponseEntity<MediaAnalysisResult> analyzeMedia(
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "分析這個媒體檔案") String prompt) {
        
        try {
            String contentType = file.getContentType();
            String analysisPrompt = buildAnalysisPrompt(contentType, prompt);
            
            String analysis = chatClient.prompt()
                    .user(u -> u.text(analysisPrompt)
                            .media(MimeTypeUtils.parseMimeType(contentType), 
                                   file.getResource()))
                    .call()
                    .content();
            
            return ResponseEntity.ok(new MediaAnalysisResult(
                file.getOriginalFilename(),
                contentType,
                analysis,
                "success"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MediaAnalysisResult(
                        file.getOriginalFilename(),
                        file.getContentType(),
                        null,
                        "分析失敗: " + e.getMessage()
                    ));
        }
    }
    
    private String buildAnalysisPrompt(String contentType, String basePrompt) {
        return switch (contentType) {
            case String ct when ct.startsWith("image/") -> 
                basePrompt + "，請特別注意圖片中的細節、色彩和構圖";
            case String ct when ct.startsWith("video/") -> 
                basePrompt + "，請描述視頻的主要內容和動作";
            case String ct when ct.startsWith("audio/") -> 
                basePrompt + "，請分析音頻的內容和特點";
            default -> basePrompt;
        };
    }
    
    // DTO 類別
    public record MediaAnalysisResult(String filename, String mediaType, 
                                    String analysis, String status) {}
}
```

### 程式重點說明

1. **檔案上傳**：使用 PostMapping 支援檔案上傳
2. **Media 物件**：需要傳入 MimeType 和 Resource
3. **ChatClient API**：使用 fluent API 的 `.media()` 方法
4. **錯誤處理**：加入適當的異常處理機制
5. **批次處理**：支援同時處理多個檔案
6. **多媒體支援**：可處理圖片、音頻、視頻等格式

## 企業級配置和最佳實踐

### 檔案上傳配置

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
      file-size-threshold: 2KB
```

### 安全配置

```java
@Configuration
public class MultimodalSecurityConfig {
    
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        return factory.createMultipartConfig();
    }
    
    @Component
    public static class FileValidationService {
        
        private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
        );
        
        private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/avi", "video/mov"
        );
        
        public boolean isValidFile(MultipartFile file) {
            String contentType = file.getContentType();
            return contentType != null && 
                   (ALLOWED_IMAGE_TYPES.contains(contentType) || 
                    ALLOWED_VIDEO_TYPES.contains(contentType));
        }
        
        public boolean isImageFile(MultipartFile file) {
            String contentType = file.getContentType();
            return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType);
        }
    }
}
```

## 驗收成果

### PostMan 測試設定

- PostMan 支援多種 Http Method，測試時記得先將方法改為 Post

![https://ithelp.ithome.com.tw/upload/images/20240806/20161290blFyfHLzdL.png](https://ithelp.ithome.com.tw/upload/images/20240806/20161290blFyfHLzdL.png)

- 在 Body 上要將類型改為 form-data

![https://ithelp.ithome.com.tw/upload/images/20240806/20161290CdSrNj8i5p.png](https://ithelp.ithome.com.tw/upload/images/20240806/20161290CdSrNj8i5p.png)

- key 的部分要傳入 file 及 message（程式傳入的參數名），file 需要將類別改為 File 才能傳送檔案

![https://ithelp.ithome.com.tw/upload/images/20240806/20161290PErqEn5SGG.png](https://ithelp.ithome.com.tw/upload/images/20240806/20161290PErqEn5SGG.png)

### 測試結果

完成後就能按 Send 測試結果了，我發送的是下面這張圖，內容就是在介紹 RAG 的資料處理流程

![https://ithelp.ithome.com.tw/upload/images/20240806/20161290lehucZbliQ.jpg](https://ithelp.ithome.com.tw/upload/images/20240806/20161290lehucZbliQ.jpg)

來看看 AI 的回答吧

> 這張圖片描述了一個文檔攝取和檢索增強生成（RAG）的流程。主要分為兩個部分：文檔攝取（ETL）和運行時的檢索增強生成。
>
> 1. **文檔攝取（ETL）**:
>    - **Reader**: 讀取數據源中的文檔。
>    - **Transformer**: 將讀取的文檔進行處理，並將其拆分為小塊（chunks）。
>    - **Writer**: 將處理後的數據寫入一個向量存儲（Vector Store）。
> 2. **運行時（Runtime）**:
>    - 用戶發出查詢（Chat Request），並生成一個提示（Prompt）。
>    - 通過檢索（Retrieve）從向量存儲中獲取相關的查詢塊（query chunks）。
>    - 將用戶查詢和檢索到的信息進行增強（Augment）。
>    - 最後，將增強的提示發送給聊天模型（Chat Model）生成回應（Chat Response）。
>
> 整個過程展示了如何從數據源提取信息並使用該信息來增強用戶查詢的回應。

### 使用 curl 測試

```bash
curl -X POST \
  http://localhost:8080/image-analysis \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@/path/to/your/image.jpg' \
  -F 'message=請詳細分析這張圖片的內容'
```

## 進階應用場景

### 1. 文檔處理助手

```java
@PostMapping("/document-ocr")
public String processDocument(@RequestParam MultipartFile file) {
    return chatClient.prompt()
            .user(u -> u.text("請提取圖片中的所有文字內容，並整理成易讀的格式")
                    .media(MimeTypeUtils.parseMimeType(file.getContentType()), 
                           file.getResource()))
            .call()
            .content();
}
```

### 2. 圖表數據分析

```java
@PostMapping("/chart-analysis")
public String analyzeChart(@RequestParam MultipartFile chartImage) {
    return chatClient.prompt()
            .user(u -> u.text("請分析這個圖表中的數據趨勢，並提供關鍵洞察")
                    .media(MimeTypeUtils.parseMimeType(chartImage.getContentType()), 
                           chartImage.getResource()))
            .call()
            .content();
}
```

### 3. 產品圖片描述生成

```java
@PostMapping("/product-description")
public String generateProductDescription(@RequestParam MultipartFile productImage) {
    return chatClient.prompt()
            .user(u -> u.text("請為這個產品撰寫吸引人的商品描述，包含特點和賣點")
                    .media(MimeTypeUtils.parseMimeType(productImage.getContentType()), 
                           productImage.getResource()))
            .call()
            .content();
}
```

## 最佳實踐建議

1. **檔案大小限制**：根據模型限制設定合適的檔案大小
2. **格式驗證**：確保上傳的檔案格式受支援
3. **錯誤處理**：提供友善的錯誤訊息
4. **成本控制**：監控 API 使用量，特別是多模態請求成本較高
5. **快取策略**：對相同檔案的分析結果可考慮快取
6. **安全考量**：驗證檔案內容，防範惡意檔案上傳

## 回顧

今天學到了甚麼:

1. 如何透過 Spring MVC 上傳檔案
2. 如何將檔案轉為 Media 並加入 UserMessage
3. **新增**：使用 ChatClient 處理多模態數據
4. **新增**：批次處理多個檔案的方法
5. **新增**：企業級安全配置和最佳實踐
6. **新增**：多種實際應用場景範例
7. 在 PostMan 測試檔案上傳並詢問 AI 內容

## Source Code

今天的程式碼：

[https://github.com/kevintsai1202/SpringBoot-AI-Day7.git](https://github.com/kevintsai1202/SpringBoot-AI-Day7.git)

