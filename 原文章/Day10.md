## 使用 Spring AI 打造企業 RAG 知識庫【10】- 聲優太花錢？找 AI 幫你配音

> **更新說明：** 本文章已根據 Spring AI 1.0.1 最新穩定版本進行全面更新，包含最新的文字轉語音功能。

## 2025 版本更新說明

本文已基於 **Spring AI 1.0.1** 最新版本進行全面更新，主要變更：
- ✅ 更新語音生成配置方式
- ✅ 新增統一 SpeechModel 介面使用
- ✅ 加入批次處理和語音風格控制
- ✅ 提供企業級應用場景範例
- ✅ 優化音頻檔案處理和下載機制

# AI有ABC腔調

![https://ithelp.ithome.com.tw/upload/images/20240810/20161290YGtkKt0BkG.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/20161290YGtkKt0BkG.jpg)

昨天教大家上字幕，今天則是反過來，給文字讓 AI 幫你配音

比起以前文字轉聲音的軟體，AI 的聲音更為自然，角色跟速度也都能調整，讓我們看看如何操作吧

## Spring AI 支援的語音生成服務

### 主要支援的語音服務

| 服務商 | 模型 | 語音選項 | 品質 | 成本 |
|--------|------|----------|------|------|
| **OpenAI** | TTS-1, TTS-1-HD | 6種聲音 | 極高 | 中 |
| **Azure OpenAI** | TTS-1, TTS-1-HD | 6種聲音 | 極高 | 中 |
| **ElevenLabs** | 多種模型 | 自訂聲音 | 極高 | 高 |

文字轉語音也是劃分在 **Audio Model** 的部分，Spring AI 目前主要支援 OpenAI 和 Azure OpenAI

![https://ithelp.ithome.com.tw/upload/images/20240809/20161290wi54q7gekg.png](https://ithelp.ithome.com.tw/upload/images/20240809/20161290wi54q7gekg.png)

## 依賴配置

確保添加正確的依賴：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

## 配置設定

### 基本配置（application.yml）

```yaml
spring:
  ai:
    model:
      audio:
        speech: openai  # 啟用語音生成功能
    openai:
      api-key: ${OPENAI_APIKEY}
      audio:
        speech:
          options:
            model: tts-1-hd
            voice: alloy
            response-format: mp3
            speed: 1.0
```

為了彈性配置，application.yml 只存放 API KEY，其他參數可以在程式碼內動態調整

## 程式碼實作

### 方法一：基礎語音生成

```java
@RestController
@RequiredArgsConstructor
public class SpeechController {
    private final SpeechModel speechModel;
    
    @GetMapping("/tts")
    public ResponseEntity<byte[]> generateSpeech(@RequestParam String text) {
        try {
            SpeechPrompt speechPrompt = new SpeechPrompt(text);
            SpeechResponse response = speechModel.call(speechPrompt);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename("配音.mp3", StandardCharsets.UTF_8)
                                    .build().toString())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.parseMediaType("audio/mpeg"))
                    .body(response.getResult().getOutput());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(("語音生成失敗: " + e.getMessage()).getBytes());
        }
    }
}
```

### 方法二：進階配置生成

```java
@RestController
@RequiredArgsConstructor
public class AdvancedSpeechController {
    private final SpeechModel speechModel;
    
    @PostMapping("/advanced-tts")
    public ResponseEntity<byte[]> generateAdvancedSpeech(
            @RequestBody SpeechGenerationRequest request) {
        
        try {
            // 建立進階選項
            OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                    .withModel(request.model() != null ? request.model() : "tts-1-hd")
                    .withVoice(parseVoice(request.voice()))
                    .withResponseFormat(parseFormat(request.format()))
                    .withSpeed(request.speed())
                    .build();
            
            SpeechPrompt speechPrompt = new SpeechPrompt(request.text(), speechOptions);
            SpeechResponse response = speechModel.call(speechPrompt);
            
            String filename = generateFilename(request.filename(), request.format());
            String contentType = getContentType(request.format());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(filename, StandardCharsets.UTF_8)
                                    .build().toString())
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(response.getResult().getOutput());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(("語音生成失敗: " + e.getMessage()).getBytes());
        }
    }
    
    private Voice parseVoice(String voice) {
        return switch (voice != null ? voice.toLowerCase() : "alloy") {
            case "echo" -> Voice.ECHO;
            case "fable" -> Voice.FABLE;
            case "onyx" -> Voice.ONYX;
            case "nova" -> Voice.NOVA;
            case "shimmer" -> Voice.SHIMMER;
            default -> Voice.ALLOY;
        };
    }
    
    private AudioResponseFormat parseFormat(String format) {
        return switch (format != null ? format.toLowerCase() : "mp3") {
            case "opus" -> AudioResponseFormat.OPUS;
            case "aac" -> AudioResponseFormat.AAC;
            case "flac" -> AudioResponseFormat.FLAC;
            default -> AudioResponseFormat.MP3;
        };
    }
    
    private String generateFilename(String customName, String format) {
        String baseName = customName != null ? customName : "語音檔案";
        String extension = format != null ? format.toLowerCase() : "mp3";
        return baseName + "." + extension;
    }
    
    private String getContentType(String format) {
        return switch (format != null ? format.toLowerCase() : "mp3") {
            case "opus" -> "audio/opus";
            case "aac" -> "audio/aac";
            case "flac" -> "audio/flac";
            default -> "audio/mpeg";
        };
    }
    
    // DTO 類別
    public record SpeechGenerationRequest(
        String text,
        String voice,
        String model,
        String format,
        Float speed,
        String filename
    ) {}
}
```

### 方法三：批次語音生成

```java
@RestController
@RequiredArgsConstructor
public class BatchSpeechController {
    private final SpeechModel speechModel;
    private final TaskExecutor speechTaskExecutor;
    
    @PostMapping("/batch-tts")
    public ResponseEntity<List<BatchSpeechResult>> batchGenerateSpeech(
            @RequestBody List<BatchSpeechRequest> requests) {
        
        List<CompletableFuture<BatchSpeechResult>> futures = requests.stream()
                .map(request -> CompletableFuture.supplyAsync(
                        () -> processSpeechRequest(request), speechTaskExecutor))
                .collect(Collectors.toList());
        
        List<BatchSpeechResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/generate-audiobook")
    public ResponseEntity<AudiobookResult> generateAudiobook(
            @RequestBody AudiobookRequest request) {
        
        try {
            List<AudioChapter> chapters = request.chapters().stream()
                    .map(this::generateChapterAudio)
                    .collect(Collectors.toList());
            
            // 可以在這裡合併多個音頻檔案
            return ResponseEntity.ok(new AudiobookResult(
                request.title(),
                chapters,
                "success",
                System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AudiobookResult(
                        request.title(),
                        Collections.emptyList(),
                        "失敗: " + e.getMessage(),
                        System.currentTimeMillis()
                    ));
        }
    }
    
    private BatchSpeechResult processSpeechRequest(BatchSpeechRequest request) {
        try {
            OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                    .withVoice(parseVoice(request.voice()))
                    .withSpeed(request.speed())
                    .build();
            
            SpeechPrompt prompt = new SpeechPrompt(request.text(), options);
            SpeechResponse response = speechModel.call(prompt);
            
            // 這裡可以將音頻保存到檔案系統或雲端儲存
            String audioUrl = saveAudioFile(response.getResult().getOutput(), request.id());
            
            return new BatchSpeechResult(request.id(), request.text(), audioUrl, "success");
        } catch (Exception e) {
            return new BatchSpeechResult(request.id(), request.text(), "", 
                                       "失敗: " + e.getMessage());
        }
    }
    
    private AudioChapter generateChapterAudio(ChapterRequest chapter) {
        try {
            OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                    .withVoice(parseVoice(chapter.voice()))
                    .withSpeed(chapter.speed())
                    .build();
            
            SpeechPrompt prompt = new SpeechPrompt(chapter.content(), options);
            SpeechResponse response = speechModel.call(prompt);
            
            String audioUrl = saveAudioFile(response.getResult().getOutput(), 
                                          "chapter_" + chapter.chapterNumber());
            
            return new AudioChapter(chapter.chapterNumber(), chapter.title(), 
                                  audioUrl, chapter.voice());
        } catch (Exception e) {
            return new AudioChapter(chapter.chapterNumber(), chapter.title(), 
                                  "", "error");
        }
    }
    
    private String saveAudioFile(byte[] audioData, String identifier) {
        // 實作音頻檔案儲存邏輯
        // 可以儲存到本地檔案系統、AWS S3、或其他雲端服務
        return "/api/audio/" + identifier + ".mp3";
    }
    
    // DTO 類別
    public record BatchSpeechRequest(String id, String text, String voice, Float speed) {}
    public record BatchSpeechResult(String id, String text, String audioUrl, String status) {}
    public record AudiobookRequest(String title, List<ChapterRequest> chapters) {}
    public record ChapterRequest(int chapterNumber, String title, String content, String voice, Float speed) {}
    public record AudioChapter(int chapterNumber, String title, String audioUrl, String voice) {}
    public record AudiobookResult(String title, List<AudioChapter> chapters, String status, long timestamp) {}
}
```

### 方法四：即時語音串流

```java
@RestController
@RequiredArgsConstructor
public class StreamingSpeechController {
    private final SpeechModel speechModel;
    
    @GetMapping("/stream-tts")
    public ResponseEntity<StreamingResponseBody> streamSpeech(@RequestParam String text) {
        
        StreamingResponseBody stream = outputStream -> {
            try {
                // 將長文本分割成小段進行串流處理
                List<String> textChunks = splitTextIntoChunks(text, 200);
                
                for (String chunk : textChunks) {
                    SpeechPrompt prompt = new SpeechPrompt(chunk);
                    SpeechResponse response = speechModel.call(prompt);
                    
                    byte[] audioData = response.getResult().getOutput();
                    outputStream.write(audioData);
                    outputStream.flush();
                    
                    // 小延遲確保串流順暢
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                throw new RuntimeException("串流語音生成失敗", e);
            }
        };
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(stream);
    }
    
    private List<String> splitTextIntoChunks(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("[.!?]+");
        
        StringBuilder currentChunk = new StringBuilder();
        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() > maxLength) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                }
            }
            currentChunk.append(sentence).append(". ");
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        return chunks;
    }
}
```

## 企業級功能和最佳實踐

### 1. 語音快取服務

```java
@Service
@RequiredArgsConstructor
public class SpeechCacheService {
    private final RedisTemplate<String, byte[]> redisTemplate;
    private final SpeechModel speechModel;
    
    @Cacheable(value = "speech-cache", key = "#cacheKey")
    public byte[] getOrGenerateSpeech(String text, SpeechOptions options) {
        String cacheKey = generateCacheKey(text, options);
        
        byte[] cachedAudio = redisTemplate.opsForValue().get(cacheKey);
        if (cachedAudio != null) {
            return cachedAudio;
        }
        
        // 生成新語音
        SpeechPrompt prompt = new SpeechPrompt(text, options);
        SpeechResponse response = speechModel.call(prompt);
        byte[] audioData = response.getResult().getOutput();
        
        // 儲存到快取（24小時）
        redisTemplate.opsForValue().set(cacheKey, audioData, Duration.ofHours(24));
        
        return audioData;
    }
    
    private String generateCacheKey(String text, SpeechOptions options) {
        return DigestUtils.md5DigestAsHex((text + options.toString()).getBytes());
    }
}
```

### 2. 音頻品質監控

```java
@Component
@RequiredArgsConstructor
public class SpeechQualityMonitor {
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void onSpeechGenerated(SpeechGeneratedEvent event) {
        // 記錄生成次數
        Counter.builder("speech.generation.count")
                .tag("voice", event.getVoice())
                .tag("model", event.getModel())
                .register(meterRegistry)
                .increment();
        
        // 記錄音頻長度
        Gauge.builder("speech.audio.duration")
                .tag("voice", event.getVoice())
                .register(meterRegistry, event.getDurationSeconds());
                
        // 記錄檔案大小
        Gauge.builder("speech.audio.size")
                .tag("format", event.getFormat())
                .register(meterRegistry, event.getFileSizeBytes());
    }
}
```

### 3. 多語言支援

```java
@Service
public class MultiLanguageSpeechService {
    private final Map<String, SpeechModel> speechModels;
    
    public byte[] generateSpeechWithLanguage(String text, String language, String voice) {
        // 根據語言選擇合適的模型和聲音
        SpeechModel model = speechModels.get(language);
        if (model == null) {
            throw new UnsupportedOperationException("不支援的語言: " + language);
        }
        
        // 根據語言調整聲音選項
        Voice selectedVoice = selectVoiceForLanguage(language, voice);
        
        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .withVoice(selectedVoice)
                .withModel("tts-1-hd")
                .build();
        
        SpeechPrompt prompt = new SpeechPrompt(text, options);
        SpeechResponse response = model.call(prompt);
        
        return response.getResult().getOutput();
    }
    
    private Voice selectVoiceForLanguage(String language, String preferredVoice) {
        // 根據語言和偏好選擇最適合的聲音
        return switch (language.toLowerCase()) {
            case "zh", "zh-cn", "zh-tw" -> Voice.NOVA; // 適合中文的聲音
            case "en" -> Voice.valueOf(preferredVoice.toUpperCase());
            case "ja" -> Voice.FABLE; // 適合日文的聲音
            default -> Voice.ALLOY;
        };
    }
}
```

## 程式重點說明

### 語音模型選項

- **TTS-1**: 標準模型，速度快，成本低
- **TTS-1-HD**: 高清模型，品質更好但成本較高

### 聲音選項（Voice）

Spring AI 支援 OpenAI 的六種聲音：

```java
// 可用的聲音選項
Voice.ALLOY    // 中性，平衡的聲音
Voice.ECHO     // 男性，沉穩的聲音  
Voice.FABLE    // 英式口音，優雅
Voice.ONYX     // 男性，深沉的聲音
Voice.NOVA     // 女性，年輕活潑
Voice.SHIMMER  // 女性，溫暖友善
```

### 音頻格式支援

```java
// 支援的音頻格式
AudioResponseFormat.MP3   // 最常用，相容性好
AudioResponseFormat.OPUS  // 高品質，檔案小
AudioResponseFormat.AAC   // Apple 生態系統友善
AudioResponseFormat.FLAC  // 無損壓縮，檔案大
```

### 速度控制

- **範圍**: 0.25 - 4.0
- **預設**: 1.0
- **建議**: 0.8-1.2 之間聽起來最自然

## 驗收成果

### 使用瀏覽器測試

```
http://localhost:8080/tts?text=歡迎使用Spring AI語音生成功能
```

### 使用 Postman 測試進階功能

```json
POST /advanced-tts
Content-Type: application/json

{
    "text": "這是一段測試語音生成的文字，使用了進階配置選項。",
    "voice": "nova",
    "model": "tts-1-hd",
    "format": "mp3",
    "speed": 1.1,
    "filename": "測試語音"
}
```

### 使用 curl 測試

```bash
curl -X POST http://localhost:8080/advanced-tts \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Hello, this is a test of the speech generation API.",
    "voice": "alloy",
    "format": "mp3",
    "speed": 1.0
  }' \
  --output speech.mp3
```

## 實際應用場景

### 1. 電子書朗讀器

```java
@PostMapping("/ebook-reader")
public ResponseEntity<byte[]> generateEbookAudio(@RequestBody EbookRequest request) {
    // 處理長篇內容，分章節生成
    return processLongContent(request.content(), request.voice());
}
```

### 2. 客服語音回應

```java
@PostMapping("/customer-service-voice")
public ResponseEntity<byte[]> generateCustomerServiceResponse(
        @RequestBody CustomerServiceRequest request) {
    
    String responseText = processCustomerQuery(request.query());
    return generateSpeech(responseText, "nova", 0.9f); // 溫暖友善的聲音
}
```

### 3. 多媒體內容配音

```java
@PostMapping("/media-narration")
public ResponseEntity<List<NarrationSegment>> generateMediaNarration(
        @RequestBody MediaNarrationRequest request) {
    
    return request.segments().stream()
            .map(segment -> generateNarrationSegment(segment))
            .collect(Collectors.toList());
}
```

經過測試，雖然偶有錯誤發生，不過聲音聽起來非常自然，可以跟手機版 ChatGPT 對話就知道效果，雖然有點 ABC 腔調，不過整體來說還蠻好聽的

## 最佳實踐建議

1. **文本預處理**: 清理特殊字符，調整標點符號
2. **成本控制**: 實施快取機制，避免重複生成
3. **品質監控**: 追蹤音頻品質和用戶反饋
4. **格式選擇**: 根據用途選擇合適的音頻格式
5. **速度調整**: 根據內容類型調整播放速度
6. **批次處理**: 大量內容使用異步批次處理

## 回顧

今天學到了什麼：

- SpeechModel 的調用方式
- **更新**：最新版本的語音生成配置
- **新增**：批次處理和串流語音生成
- **新增**：企業級快取和監控機制
- **新增**：多語言和多場景應用
- **新增**：音頻品質控制和最佳實踐
- OpenAiAudioSpeechOptions 的參數設定
- 回傳 binary 格式下載檔案的 header 寫法

到目前為止凱文大叔把基本 Model 的操作都介紹過，不過 AI 的變化非常快，搞不好幾個月後又有新的 Model，大家覺得前面介紹這些 Model 可以取代掉那些職業？

## Source Code

程式碼下載：[https://github.com/kevintsai1202/SpringBoot-AI-Day10.git](https://github.com/kevintsai1202/SpringBoot-AI-Day10.git)

