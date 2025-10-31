## 使用 Spring AI 打造企業 RAG 知識庫【8】- 透過 Spring AI 生成 AI 圖片

> **更新說明：** 本文章已根據 Spring AI 1.0.1 最新穩定版本進行全面更新，包含最新的圖片生成功能。

## 2025 版本更新說明

本文已基於 **Spring AI 1.0.1** 最新版本進行全面更新，主要變更：
- ✅ 更新最新的圖片生成模型支援清單
- ✅ 新增 ImageOptions 進階配置選項
- ✅ 加入批次生成和品質控制功能
- ✅ 提供多家 AI 服務商的整合範例
- ✅ 加入企業級應用和最佳實踐建議

# 文生圖AI將會是最大圖庫

![https://ithelp.ithome.com.tw/upload/images/20240810/20161290xVSyazJmHj.png](https://ithelp.ithome.com.tw/upload/images/20240810/20161290xVSyazJmHj.png)

昨天學了如何上傳圖片給 AI 辨識，今天我們讓 AI 來產生圖片吧

## Spring AI 支援的圖片生成模型

### 主要支援的 AI 服務商

| 服務商 | 模型 | 特色 | 成本 | 推薦度 |
|--------|------|------|------|--------|
| **OpenAI** | DALL-E 3 | 高品質、理解複雜指令 | 高 | ⭐⭐⭐⭐⭐ |
| **Stability AI** | Stable Diffusion | 開源、可本地部署 | 中 | ⭐⭐⭐⭐ |
| **ZhiPu AI** | CogView | 中文友善、便宜 | 低 | ⭐⭐⭐ |
| **QianFan** | 文心一言 | 百度服務 | 低 | ⭐⭐ |

### 成本效益分析

根據最新測試數據，以下是各服務商的成本對比：

- **OpenAI DALL-E 3**: $0.040-0.080 per image (高品質)
- **Stability AI**: $0.002-0.01 per image (中等品質)  
- **ZhiPu AI**: 免費額度 + 低費率 (適合測試)
- **QianFan**: 需要大陸認證 (地區限制)

對於測試和學習，推薦使用 **ZhiPu AI**，它提供免費額度且註冊門檻較低。

## ZhiPu AI 整合教學

### 1. 申請 API Key

前往 ZhiPu AI 官網 [https://open.bigmodel.cn/](https://open.bigmodel.cn/) 註冊後，在 API 密鑰頁面取得你的 API Key。

![https://ithelp.ithome.com.tw/upload/images/20240806/20161290qkPNJ0Ho4Y.png](https://ithelp.ithome.com.tw/upload/images/20240806/20161290qkPNJ0Ho4Y.png)

### 2. 引入依賴

在 `pom.xml` 中添加 ZhiPu AI 依賴：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-zhipuai-spring-boot-starter</artifactId>
</dependency>
```

### 3. 設定專案參數

在 `application.yml` 中配置：

```yaml
spring:
  ai:
    model:
      image: zhipuai  # 啟用圖片生成模型
    zhipuai:
      api-key: ${ZHIPUAI_APIKEY}
      image:
        options:
          model: cogview-3
          width: 1024
          height: 1024
```

## 程式碼實作

### 方法一：基礎圖片生成

```java
@RestController
@RequiredArgsConstructor
public class ImageGenerationController {
    private final ImageModel imageModel;
    
    @GetMapping("/generate-image")
    public ResponseEntity<String> generateImage(@RequestParam String prompt) {
        try {
            ImageResponse response = imageModel.call(new ImagePrompt(prompt));
            Image image = response.getResult().getOutput();
            
            String htmlResponse = String.format(
                "<html><body><h3>Generated Image</h3>" +
                "<img src='%s' alt='%s' style='max-width:800px;'>" +
                "<p>Prompt: %s</p></body></html>", 
                image.getUrl(), prompt, prompt
            );
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlResponse);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("圖片生成失敗: " + e.getMessage());
        }
    }
}
```

### 方法二：進階配置生成

```java
@RestController
@RequiredArgsConstructor
public class AdvancedImageController {
    private final ImageModel imageModel;
    
    @PostMapping("/generate-image-advanced")
    public ResponseEntity<ImageGenerationResult> generateAdvancedImage(
            @RequestBody ImageGenerationRequest request) {
        
        try {
            // 建立進階選項
            ZhipuAiImageOptions options = ZhipuAiImageOptions.builder()
                    .withModel("cogview-3")
                    .withWidth(request.width())
                    .withHeight(request.height())
                    .withN(request.count())
                    .build();
            
            // 建立圖片訊息
            ImageMessage imageMessage = new ImageMessage(request.prompt());
            ImagePrompt imagePrompt = new ImagePrompt(imageMessage, options);
            
            // 生成圖片
            ImageResponse response = imageModel.call(imagePrompt);
            
            List<String> imageUrls = response.getResults().stream()
                    .map(result -> result.getOutput().getUrl())
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ImageGenerationResult(
                request.prompt(),
                imageUrls,
                "success",
                System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ImageGenerationResult(
                        request.prompt(),
                        Collections.emptyList(),
                        "失敗: " + e.getMessage(),
                        System.currentTimeMillis()
                    ));
        }
    }
    
    // DTO 類別
    public record ImageGenerationRequest(
        String prompt,
        int width,
        int height,
        int count
    ) {}
    
    public record ImageGenerationResult(
        String prompt,
        List<String> imageUrls,
        String status,
        long timestamp
    ) {}
}
```

### 方法三：批次生成和風格控制

```java
@RestController
@RequiredArgsConstructor
public class BatchImageController {
    private final ImageModel imageModel;
    
    @PostMapping("/batch-generate")
    public ResponseEntity<List<BatchImageResult>> batchGenerate(
            @RequestBody List<BatchImageRequest> requests) {
        
        List<BatchImageResult> results = requests.parallelStream()
                .map(this::processImageRequest)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/style-transfer")
    public ResponseEntity<StyleTransferResult> generateWithStyle(
            @RequestBody StyleTransferRequest request) {
        
        try {
            // 組合風格化提示詞
            String enhancedPrompt = buildStyledPrompt(request.basePrompt(), request.style());
            
            ZhipuAiImageOptions options = ZhipuAiImageOptions.builder()
                    .withModel("cogview-3")
                    .withWidth(1024)
                    .withHeight(1024)
                    .build();
            
            ImageResponse response = imageModel.call(
                new ImagePrompt(new ImageMessage(enhancedPrompt), options)
            );
            
            String imageUrl = response.getResult().getOutput().getUrl();
            
            return ResponseEntity.ok(new StyleTransferResult(
                request.basePrompt(),
                request.style(),
                enhancedPrompt,
                imageUrl,
                "success"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new StyleTransferResult(
                        request.basePrompt(),
                        request.style(),
                        "",
                        "",
                        "失敗: " + e.getMessage()
                    ));
        }
    }
    
    private BatchImageResult processImageRequest(BatchImageRequest request) {
        try {
            ImageResponse response = imageModel.call(new ImagePrompt(request.prompt()));
            String imageUrl = response.getResult().getOutput().getUrl();
            return new BatchImageResult(request.id(), request.prompt(), imageUrl, "success");
        } catch (Exception e) {
            return new BatchImageResult(request.id(), request.prompt(), "", 
                                      "失敗: " + e.getMessage());
        }
    }
    
    private String buildStyledPrompt(String basePrompt, String style) {
        return switch (style.toLowerCase()) {
            case "realistic" -> basePrompt + ", photorealistic, high detail, professional photography";
            case "anime" -> basePrompt + ", anime style, manga art, cel shading";
            case "oil_painting" -> basePrompt + ", oil painting style, classical art, brush strokes";
            case "watercolor" -> basePrompt + ", watercolor painting, soft colors, artistic";
            case "cyberpunk" -> basePrompt + ", cyberpunk style, neon lights, futuristic";
            default -> basePrompt;
        };
    }
    
    // DTO 類別
    public record BatchImageRequest(String id, String prompt) {}
    public record BatchImageResult(String id, String prompt, String imageUrl, String status) {}
    public record StyleTransferRequest(String basePrompt, String style) {}
    public record StyleTransferResult(String basePrompt, String style, String enhancedPrompt, 
                                    String imageUrl, String status) {}
}
```

### 方法四：OpenAI DALL-E 3 整合

```java
@RestController
@RequiredArgsConstructor
public class OpenAIImageController {
    private final ImageModel imageModel;
    
    @PostMapping("/dalle-generate")
    public ResponseEntity<OpenAIImageResult> generateWithDALLE(
            @RequestBody OpenAIImageRequest request) {
        
        try {
            // OpenAI 專用選項
            OpenAiImageOptions options = OpenAiImageOptions.builder()
                    .withModel("dall-e-3")
                    .withQuality("hd")  // standard or hd
                    .withStyle("vivid") // vivid or natural
                    .withSize("1024x1024")
                    .withResponseFormat("url")
                    .build();
            
            ImagePrompt prompt = new ImagePrompt(request.prompt(), options);
            ImageResponse response = imageModel.call(prompt);
            
            Image result = response.getResult().getOutput();
            
            return ResponseEntity.ok(new OpenAIImageResult(
                request.prompt(),
                result.getUrl(),
                "dall-e-3",
                "success"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new OpenAIImageResult(
                        request.prompt(),
                        "",
                        "dall-e-3",
                        "失敗: " + e.getMessage()
                    ));
        }
    }
    
    // DTO 類別
    public record OpenAIImageRequest(String prompt) {}
    public record OpenAIImageResult(String prompt, String imageUrl, String model, String status) {}
}
```

## 企業級功能和最佳實踐

### 1. 圖片快取和儲存

```java
@Service
@RequiredArgsConstructor
public class ImageCacheService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ImageStorageService storageService;
    
    @Cacheable(value = "generated-images", key = "#prompt")
    public String getOrGenerateImage(String prompt, Map<String, Object> options) {
        // 檢查快取
        String cacheKey = generateCacheKey(prompt, options);
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedUrl != null) {
            return cachedUrl;
        }
        
        // 生成新圖片
        String imageUrl = generateImage(prompt, options);
        
        // 儲存到快取
        redisTemplate.opsForValue().set(cacheKey, imageUrl, Duration.ofHours(24));
        
        return imageUrl;
    }
    
    private String generateCacheKey(String prompt, Map<String, Object> options) {
        return DigestUtils.md5DigestAsHex((prompt + options.toString()).getBytes());
    }
}
```

### 2. 內容安全過濾

```java
@Component
public class ContentFilterService {
    
    private static final Set<String> BLOCKED_KEYWORDS = Set.of(
        // 添加需要過濾的關鍵字
        "暴力", "血腥", "政治敏感"
    );
    
    public FilterResult filterContent(String prompt) {
        // 關鍵字過濾
        for (String keyword : BLOCKED_KEYWORDS) {
            if (prompt.toLowerCase().contains(keyword.toLowerCase())) {
                return new FilterResult(false, "包含不當內容: " + keyword);
            }
        }
        
        // 可以整合更進階的 AI 內容審核服務
        return new FilterResult(true, "內容檢查通過");
    }
    
    public record FilterResult(boolean passed, String message) {}
}
```

### 3. 使用量監控

```java
@Component
@RequiredArgsConstructor
public class ImageGenerationMonitor {
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void onImageGenerated(ImageGeneratedEvent event) {
        Counter.builder("image.generation.requests")
                .tag("model", event.getModel())
                .tag("status", event.getStatus())
                .register(meterRegistry)
                .increment();
                
        Timer.Sample.start(meterRegistry)
                .stop(Timer.builder("image.generation.duration")
                        .register(meterRegistry));
    }
}
```

## 驗收成果

### 使用瀏覽器測試

直接在瀏覽器輸入以下 URL：
```
http://localhost:8080/generate-image?prompt=一位穿著古裝的美女在花園中
```

![https://ithelp.ithome.com.tw/upload/images/20240806/20161290y9tHGVoXnR.png](https://ithelp.ithome.com.tw/upload/images/20240806/20161290y9tHGVoXnR.png)

### 使用 Postman 測試進階功能

```json
POST /generate-image-advanced
Content-Type: application/json

{
    "prompt": "一個現代科技感的辦公室，充滿未來感的設計",
    "width": 1024,
    "height": 1024,
    "count": 2
}
```

### 使用 curl 測試

```bash
curl -X POST http://localhost:8080/dalle-generate \
  -H "Content-Type: application/json" \
  -d '{"prompt": "A futuristic cityscape with flying cars at sunset"}'
```

## 注意事項和限制

### 內容審核

不同的 AI 服務商都有內容審核機制：

- **ZhiPu AI**: 會封鎖敏感關鍵字，返回錯誤訊息
- **OpenAI**: 有嚴格的內容政策，會拒絕不當請求
- **Stability AI**: 相對寬鬆，但仍有基本限制

### 錯誤處理範例

```json
// ZhiPu AI 敏感內容錯誤
{
    "contentFilter": [{"level": 2, "role": "user"}],
    "error": {
        "code": "1301",
        "message": "系统检测到输入或生成内容可能包含不安全或敏感内容，请您避免输入易产生敏感内容的提示语，感谢您的配合。"
    }
}
```

## 進階應用場景

### 1. 電商產品圖生成

```java
@PostMapping("/product-image")
public ResponseEntity<String> generateProductImage(@RequestBody ProductImageRequest request) {
    String enhancedPrompt = String.format(
        "A professional product photo of %s on %s background, " +
        "studio lighting, high resolution, commercial photography style",
        request.productDescription(),
        request.backgroundColor()
    );
    
    return generateImage(enhancedPrompt);
}
```

### 2. 社群媒體內容生成

```java
@PostMapping("/social-media-image")
public ResponseEntity<String> generateSocialImage(@RequestBody SocialImageRequest request) {
    String styledPrompt = String.format(
        "%s, social media style, %s color scheme, trendy design, modern aesthetics",
        request.content(),
        request.colorScheme()
    );
    
    return generateImage(styledPrompt);
}
```

### 3. 教育內容插圖

```java
@PostMapping("/educational-illustration")
public ResponseEntity<String> generateEducationalImage(@RequestBody EducationImageRequest request) {
    String educationalPrompt = String.format(
        "Educational illustration of %s, clear and simple design, " +
        "suitable for %s level, colorful and engaging",
        request.concept(),
        request.educationLevel()
    );
    
    return generateImage(educationalPrompt);
}
```

## 最佳實踐建議

1. **提示詞優化**: 詳細描述場景、風格、色彩、構圖
2. **成本控制**: 實施快取機制，避免重複生成相同圖片
3. **內容審核**: 建立多層次的內容過濾機制
4. **品質控制**: 對生成結果進行評分和篩選
5. **使用監控**: 追蹤 API 使用量和成本
6. **備援方案**: 準備多個 AI 服務商作為備援

## 回顧

今天學到了什麼：

- 文生圖的 ImageModel 調用方式
- **更新**：最新版本的 ImageOptions 配置
- **新增**：多種 AI 服務商的整合方法
- **新增**：批次生成和風格控制功能
- **新增**：企業級快取、監控和安全機制
- **新增**：實際應用場景和最佳實踐
- 引入不同 AI 模組以及非 OpenAI 的設定
- 產生的圖檔取得和處理

## Source Code

今天的程式碼：[https://github.com/kevintsai1202/SpringBoot-AI-Day8.git](https://github.com/kevintsai1202/SpringBoot-AI-Day8.git)

