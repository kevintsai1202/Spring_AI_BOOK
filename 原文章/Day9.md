## 使用 Spring AI 打造企業 RAG 知識庫【9】- 做一個雲端字幕產生器

> **更新說明：** 本文章已根據 Spring AI 1.0.1 最新穩定版本進行全面更新，包含最新的語音轉文字功能。

## 2025 版本更新說明

本文已基於 **Spring AI 1.0.1** 最新版本進行全面更新，主要變更：
- ✅ 更新音頻轉譯配置方式
- ✅ 新增 AudioTranscriptionModel 統一介面使用
- ✅ 優化檔案上傳和響應處理
- ✅ 加入最佳實踐建議和錯誤處理

# 上字幕不再痛苦

![https://ithelp.ithome.com.tw/upload/images/20240810/20161290jCx3iLQjtI.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/20161290jCx3iLQjtI.jpg)

相信很多人知道 OpenAI 開源了 Whisper 模型，網路上也很多人製作本機端的字幕產生程式，凱文大叔試過，只能說慘不忍睹，由於電腦沒有獨立顯卡，不到 30 分鐘的影片竟然一個小時還沒完成

這時就很適合用平台的算力來協助了，音頻轉譯在 Spring AI 的文件中主要支援 OpenAI 和 Azure OpenAI

![https://ithelp.ithome.com.tw/upload/images/20240807/201612904zZn4fPMwh.png](https://ithelp.ithome.com.tw/upload/images/20240807/201612904zZn4fPMwh.png)

原本想說可以用 Groq 省錢，不過凱文大叔實際測試發現 Groq 閹割了很多功能，只能產生純文字，字幕最重要的時間戳記卻無法產生，若單純產生文字稿還是能用，需要時間戳記就只能花點小錢了（22mb 四分多鐘影片約 0.03 美金）

程式碼跟 [Day7](https://ithelp.ithome.com.tw/articles/10343554) 的有點像，主要就是上傳影片或聲音檔，再透過 `AudioTranscriptionModel` 生成文字，不過 `application.yml` 需要調整一下

## 依賴配置

首先確保添加正確的依賴：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

## 設定參數

### 基本配置（application.yml）

```yaml
spring:
  ai:
    model:
      audio:
        transcription: openai  # 啟用音頻轉譯功能
    openai:
      api-key: ${OPENAI_APIKEY}
      audio:
        transcription:
          options:
            model: whisper-1
            response-format: json  # 預設格式
            temperature: 0.0
            language: zh  # 指定中文可提升準確度
  servlet:
    multipart:
      max-file-size: 25MB
      max-request-size: 25MB
```

與 transcription 有關的設定放在 `openai.audio.transcription`，我們設定了幾個重要參數：
- `model`: 使用 whisper-1 模型
- `response-format`: 可選 json, text, srt, verbose_json, vtt
- `temperature`: 控制隨機性，0.0 表示最確定的結果
- `language`: 指定語言可提升準確度和速度

另外有注意到下方多了 multipart 的設定嗎？MultipartFile 預設只能傳 1MB 所以我們必須把每次單檔最大（max-file-size）跟每次請求最大（max-request-size）都設為 25MB

為什麼設成 25MB？因為這是 whisper 的限制，超過大小會回錯誤訊息

## 程式碼實作

### 方法一：使用統一介面（推薦）

```java
@RestController
@RequiredArgsConstructor
public class AiController {
    private final AudioTranscriptionModel transcriptionModel;
    
    @PostMapping("/transcription")
    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) {
        try {
            // 使用統一的 AudioTranscriptionModel 介面
            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(file.getResource());
            AudioTranscriptionResponse response = transcriptionModel.call(prompt);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .body(response.getResult().getOutput());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("轉譯失敗: " + e.getMessage());
        }
    }
}
```

### 方法二：使用 Options 客製化參數

```java
@RestController
@RequiredArgsConstructor
public class AiController {
    private final AudioTranscriptionModel transcriptionModel;
    
    @PostMapping("/vtt")
    public ResponseEntity<String> videoToText(@RequestParam("file") MultipartFile file,
                                            @RequestParam(defaultValue = "srt") String format) {
        try {
            // 建立客製化選項
            OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                    .withTemperature(0f)
                    .withResponseFormat(getResponseFormat(format))
                    .withLanguage("zh")  // 指定中文
                    .withTimestampGranularities(Set.of("segment"))  // 新增時間戳記粒度
                    .build();
            
            AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(
                    file.getResource(), options);
            AudioTranscriptionResponse response = transcriptionModel.call(transcriptionRequest);
            
            String filename = generateFilename(file.getOriginalFilename(), format);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(filename, StandardCharsets.UTF_8)
                                    .build().toString())
                    .header(HttpHeaders.CONTENT_TYPE, getContentType(format))
                    .body(response.getResult().getOutput());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("轉譯失敗: " + e.getMessage());
        }
    }
    
    private TranscriptResponseFormat getResponseFormat(String format) {
        return switch (format.toLowerCase()) {
            case "srt" -> TranscriptResponseFormat.SRT;
            case "vtt" -> TranscriptResponseFormat.VTT;
            case "json" -> TranscriptResponseFormat.JSON;
            case "verbose_json" -> TranscriptResponseFormat.VERBOSE_JSON;
            default -> TranscriptResponseFormat.TEXT;
        };
    }
    
    private String getContentType(String format) {
        return switch (format.toLowerCase()) {
            case "json", "verbose_json" -> MediaType.APPLICATION_JSON_VALUE;
            default -> MediaType.TEXT_PLAIN_VALUE;
        };
    }
    
    private String generateFilename(String originalFilename, String format) {
        String baseName = originalFilename != null ? 
            originalFilename.replaceFirst("\\.[^.]+$", "") : "transcription";
        return baseName + "_字幕." + format;
    }
}
```

### 方法三：批次處理和進階功能

```java
@RestController
@RequiredArgsConstructor
public class AdvancedTranscriptionController {
    private final AudioTranscriptionModel transcriptionModel;
    
    @PostMapping("/batch-transcription")
    public ResponseEntity<List<TranscriptionResult>> batchTranscribe(
            @RequestParam("files") List<MultipartFile> files) {
        
        List<TranscriptionResult> results = files.parallelStream()
                .map(this::processFile)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/transcription-with-summary")
    public ResponseEntity<TranscriptionWithSummary> transcribeWithSummary(
            @RequestParam("file") MultipartFile file,
            @Autowired ChatClient chatClient) {
        
        try {
            // 先進行音頻轉譯
            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(file.getResource());
            AudioTranscriptionResponse response = transcriptionModel.call(prompt);
            String transcription = response.getResult().getOutput();
            
            // 使用 ChatClient 生成摘要
            String summary = chatClient.prompt()
                    .user(u -> u.text("請為以下逐字稿生成重點摘要：\n\n{transcription}")
                            .param("transcription", transcription))
                    .call()
                    .content();
            
            return ResponseEntity.ok(new TranscriptionWithSummary(transcription, summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    private TranscriptionResult processFile(MultipartFile file) {
        try {
            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(file.getResource());
            AudioTranscriptionResponse response = transcriptionModel.call(prompt);
            return new TranscriptionResult(file.getOriginalFilename(), 
                                         response.getResult().getOutput(), 
                                         "success");
        } catch (Exception e) {
            return new TranscriptionResult(file.getOriginalFilename(), null, 
                                         "failed: " + e.getMessage());
        }
    }
    
    // DTO 類別
    public record TranscriptionResult(String filename, String content, String status) {}
    public record TranscriptionWithSummary(String transcription, String summary) {}
}
```

### 程式重點說明

1. **統一介面使用**：使用 `AudioTranscriptionModel` 而非具體實現類，提升程式碼的可移植性

2. **ResponseFormat 選擇**：
   - `SRT`: 最常見的字幕格式，包含時間戳記
   - `VTT`: WebVTT 格式，適合網頁播放器
   - `JSON`: 結構化數據，便於程式處理
   - `VERBOSE_JSON`: 包含更詳細的元數據

3. **錯誤處理**：加入適當的異常處理，提供友善的錯誤訊息

4. **檔案處理**：使用 ContentDisposition 正確設定下載檔案名稱

## 進階配置選項

### 完整的配置範例

```yaml
spring:
  ai:
    model:
      audio:
        transcription: openai
    openai:
      audio:
        transcription:
          options:
            model: whisper-1
            response-format: verbose_json
            prompt: "以下是一段中文對話，請準確轉譯："
            language: zh
            temperature: 0.2
            timestamp-granularities: segment,word  # 可選 segment 或 word
```

### 企業級配置建議

```java
@Configuration
public class AudioTranscriptionConfig {
    
    @Bean
    @ConditionalOnProperty(prefix = "app.transcription", name = "enabled", havingValue = "true")
    public AudioTranscriptionService audioTranscriptionService(
            AudioTranscriptionModel transcriptionModel) {
        return new AudioTranscriptionService(transcriptionModel);
    }
    
    @Bean
    public TaskExecutor transcriptionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("transcription-");
        executor.initialize();
        return executor;
    }
}
```

## 驗收成果

下面是轉出的檔案內容，轉譯的內容除了幾個英文字念不標準翻錯以外XD，中文幾乎都沒問題，凱文大叔覺得最厲害的是一句話中英夾雜也能辨識出來　

**SRT 格式範例**：
```srt
1
00:00:00,000 --> 00:00:05,600
在學習程式或是求職時,是否遇過以下狀況

2
00:00:05,600 --> 00:00:10,400
學完Java仍不知如何開發應用程式

3
00:00:10,400 --> 00:00:17,100
在找Java相關工作,結果超過一半都要求會使用SpringBoot框架
```

**Verbose JSON 格式範例**：
```json
{
  "task": "transcribe",
  "language": "chinese",
  "duration": 23.6,
  "text": "在學習程式或是求職時,是否遇過以下狀況...",
  "segments": [
    {
      "id": 0,
      "seek": 0,
      "start": 0.0,
      "end": 5.6,
      "text": "在學習程式或是求職時,是否遇過以下狀況",
      "tokens": [50364, 1936, 7375, 32771, 12927, 2793, 31875, ...],
      "temperature": 0.0,
      "avg_logprob": -0.2876,
      "compression_ratio": 1.2,
      "no_speech_prob": 0.01
    }
  ]
}
```

另外這模組也很適合做為會議紀錄，不管是線上會議還是錄音筆，他都能輕易的轉成文字，之後還能作為 RAG 的資料來源，當作企業 KM 系統的一環

## 最佳實踐建議

1. **檔案大小控制**：超過 25MB 的檔案需要先進行分割
2. **語言指定**：明確指定語言可提升準確度和處理速度
3. **格式選擇**：根據用途選擇適合的輸出格式
4. **異步處理**：大檔案建議使用異步處理
5. **快取策略**：對相同檔案可考慮實現快取機制
6. **成本控制**：監控 API 使用量，避免意外費用

## 回顧

今天學到甚麼：

- 透過 Whisper 可以輕鬆的將影片或音檔轉成文字
- **更新**：使用統一的 AudioTranscriptionModel 介面
- **更新**：最新版本的配置方式和參數選項
- **更新**：多種輸出格式的支援和選擇
- **新增**：批次處理和進階功能實現
- **新增**：企業級配置和最佳實踐建議

## Source Code

今天的程式碼: [https://github.com/kevintsai1202/SpringBoot-AI-Day9.git](https://github.com/kevintsai1202/SpringBoot-AI-Day9.git)

