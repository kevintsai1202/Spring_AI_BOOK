# Spring AI 語音生成 (5.5 - Text-to-Speech)

企業級文字轉語音應用，使用 Spring AI 框架與 OpenAI TTS 模型實現高質量語音生成功能。

## 專案概述

本專案提供完整的語音生成 API，支援以下功能：
- 單個文本轉語音生成
- 多語言語音支援
- 可調整語速
- 多種音頻格式輸出
- 批次語音生成
- 生成進度查詢
- 快取管理
- 文本驗證與優化

## 技術棧

- **框架**: Spring Boot 3.3.0
- **AI 框架**: Spring AI 1.0.3
- **AI 模型**: OpenAI Text-to-Speech (TTS)
- **快取**: Redis (可選)
- **構建工具**: Maven 3.9+
- **Java**: JDK 21+

## 專案結構

```
chapter5-5.5-voice-generation/
├── src/main/java/com/example/
│   ├── config/                     # 配置類
│   │   └── SpeechModelConfig.java  # 語音模型配置
│   ├── controller/                 # REST 控制器
│   │   └── VoiceController.java
│   ├── dto/                        # 數據傳輸對象
│   │   ├── VoiceRequest.java
│   │   ├── VoiceResponse.java
│   │   └── VoiceOption.java
│   ├── service/                    # 服務層
│   │   ├── VoiceGenerationService.java
│   │   └── impl/
│   │       └── VoiceGenerationServiceImpl.java
│   └── SpringAiVoiceGenerationApplication.java
├── src/main/resources/
│   ├── application.yml             # 應用配置
│   └── logback-spring.xml          # 日誌配置
├── pom.xml                         # Maven 構建配置
└── README.md                       # 本文件
```

## 環境準備

### 1. 設置 Java 環境

```powershell
# 設置 Java 21
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 驗證
java -version
```

### 2. 配置 OpenAI API 密鑰

編輯 `src/main/resources/application.yml`：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:your-api-key-here}
```

或設置環境變數：
```powershell
$env:OPENAI_API_KEY="sk-..."
```

### 3. 可選：配置 Redis 快取

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

## 編譯與運行

### 編譯專案

```powershell
# 進入專案目錄
cd "E:\Spring_AI_BOOK\code-examples\chapter5-5.5-voice-generation"

# 編譯
mvn clean compile

# 運行測試
mvn test

# 打包
mvn package
```

### 運行應用程式

```powershell
# 方式1：使用 Maven 運行
mvn spring-boot:run

# 方式2：直接運行 JAR
java -jar target/spring-ai-voice-generation-1.0.0.jar
```

應用程式將在 `http://localhost:8080` 啟動。

## API 文檔

### 1. 生成單個語音

**端點**: `POST /api/voices/generate`

**請求示例**:
```json
{
  "text": "今天天氣真好，適合出去遊玩。",
  "model": "tts-1-hd",
  "voice": "alloy",
  "speed": 1.0,
  "format": "mp3",
  "userId": "user123",
  "useCase": "announcement"
}
```

**回應示例**:
```json
{
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUCCESS",
  "audioUrl": "https://cdn.openai.com/audio/...",
  "localPath": "/generated-voices/audio_12345.mp3",
  "model": "tts-1-hd",
  "voice": "alloy",
  "speed": 1.0,
  "format": "mp3",
  "fileSizeBytes": 524288,
  "durationSeconds": 12.5,
  "processingTime": 2341,
  "createdAt": "2025-10-24T22:30:00Z",
  "expiresAt": "2025-10-25T22:30:00Z",
  "textLength": 28
}
```

### 2. 批次生成語音

**端點**: `POST /api/voices/batch`

**請求示例**:
```json
[
  {
    "text": "歡迎使用語音生成服務",
    "model": "tts-1-hd",
    "voice": "nova",
    "speed": 1.0
  },
  {
    "text": "感謝您的使用",
    "model": "tts-1-hd",
    "voice": "shimmer",
    "speed": 0.9
  }
]
```

### 3. 獲取可用語音列表

**端點**: `GET /api/voices/available`

**回應示例**:
```json
[
  {
    "voiceId": "alloy",
    "voiceName": "Alloy",
    "description": "A balanced, versatile voice",
    "gender": "neutral",
    "ageGroup": "adult",
    "provider": "openai",
    "languageCode": "en",
    "languageName": "English",
    "supportedFormats": ["mp3", "opus", "aac", "flac"],
    "speedRange": {
      "min": 0.25,
      "max": 4.0,
      "recommended": 1.0
    },
    "available": true
  },
  {
    "voiceId": "nova",
    "voiceName": "Nova",
    "description": "A bright, energetic voice",
    "gender": "female",
    "ageGroup": "young-adult",
    "provider": "openai",
    "languageCode": "en",
    "languageName": "English",
    "supportedFormats": ["mp3", "opus", "aac", "flac"],
    "speedRange": {
      "min": 0.25,
      "max": 4.0,
      "recommended": 1.0
    },
    "available": true
  }
]
```

可用語音：
- **alloy**: 平衡，多用途聲音
- **echo**: 回聲效果
- **fable**: 故事敘述風格
- **onyx**: 深沉男性聲音
- **nova**: 明亮年輕女性聲音
- **shimmer**: 閃亮優雅聲音

### 4. 按語言獲取語音

**端點**: `GET /api/voices/by-language/{languageCode}`

**示例**: `GET /api/voices/by-language/en`

### 5. 查詢生成進度

**端點**: `GET /api/voices/{requestId}/status`

**示例**: `GET /api/voices/550e8400-e29b-41d4-a716-446655440000/status`

### 6. 清除快取

**端點**: `DELETE /api/voices/{requestId}/cache`

**示例**: `DELETE /api/voices/550e8400-e29b-41d4-a716-446655440000/cache`

### 7. 驗證文本

**端點**: `POST /api/voices/validate-text`

**請求示例**:
```json
{
  "text": "今天天氣真好"
}
```

**回應示例**:
```json
{
  "valid": true,
  "originalText": "今天天氣真好",
  "optimizedText": "今天天氣真好。",
  "message": "文本有效"
}
```

### 8. 估算音頻時長

**端點**: `POST /api/voices/estimate-duration`

**請求示例**:
```json
{
  "text": "今天天氣真好，適合出去遊玩。",
  "speed": 1.0
}
```

**回應示例**:
```json
{
  "estimatedDuration": 12.5,
  "unit": "seconds",
  "textLength": 28,
  "speed": 1.0
}
```

### 9. 健康檢查

**端點**: `GET /api/voices/health`

**回應示例**:
```json
{
  "status": "UP",
  "service": "Voice Generation Service",
  "message": "服務正常運行"
}
```

## 使用示例

### cURL 請求

```bash
# 生成語音
curl -X POST http://localhost:8080/api/voices/generate \
  -H "Content-Type: application/json" \
  -d '{
    "text": "今天天氣真好",
    "model": "tts-1-hd",
    "voice": "alloy",
    "speed": 1.0,
    "format": "mp3"
  }'

# 獲取可用語音
curl http://localhost:8080/api/voices/available

# 估算時長
curl -X POST http://localhost:8080/api/voices/estimate-duration \
  -H "Content-Type: application/json" \
  -d '{
    "text": "今天天氣真好",
    "speed": 1.0
  }'

# 查詢健康狀態
curl http://localhost:8080/api/voices/health
```

### Java 客戶端

```java
RestTemplate restTemplate = new RestTemplate();

VoiceRequest request = VoiceRequest.builder()
    .text("今天天氣真好")
    .model("tts-1-hd")
    .voice("alloy")
    .speed(1.0)
    .format("mp3")
    .build();

VoiceResponse response = restTemplate.postForObject(
    "http://localhost:8080/api/voices/generate",
    request,
    VoiceResponse.class
);

System.out.println("音頻 URL: " + response.getAudioUrl());
System.out.println("時長: " + response.getDurationSeconds() + " 秒");
```

## 配置說明

### application.yml 主要配置

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}

  application:
    name: spring-ai-voice-generation

app:
  voice:
    # 語音生成模型配置
    model: tts-1-hd                # tts-1 或 tts-1-hd
    voice: alloy                   # alloy, echo, fable, onyx, nova, shimmer

    # 音頻格式配置
    format: mp3                    # mp3, opus, aac, flac

    # 語速配置（1.0 為正常速度）
    speed: 1.0                     # 0.25 - 4.0

    # 批次和超時配置
    max-batch-size: 10
    default-timeout: 30s           # 生成超時時間

    # 快取配置
    cache-enabled: true
    cache-ttl: 3600               # 快取過期時間（秒）
    output-directory: ./generated-voices

    # 文本限制
    max-text-length: 4096
```

## 語音特性對比

| 語音 | 性別 | 年齡 | 風格 | 最佳用途 |
|------|------|------|------|---------|
| alloy | 中立 | 成人 | 平衡 | 通用場景 |
| echo | 男性 | 成人 | 深沉 | 專業解說 |
| fable | 男性 | 成人 | 故事 | 故事敘述 |
| onyx | 男性 | 成人 | 深沉 | 嚴肅場景 |
| nova | 女性 | 青年 | 明亮 | 親切互動 |
| shimmer | 女性 | 青年 | 優雅 | 高端場景 |

## 模型對比

| 特性 | tts-1 | tts-1-hd |
|------|-------|----------|
| 延遲 | 低 | 中等 |
| 音質 | 標準 | 高質量 |
| 成本 | 低 | 中等 |
| 推薦用途 | 實時應用 | 高質量錄製 |

## 主要類說明

### VoiceRequest
輸入請求 DTO，包含：
- `text`: 要轉語音的文本
- `model`: 使用的模型
- `voice`: 語音選擇
- `speed`: 語速
- `format`: 音頻格式
- `userId`: 用戶 ID
- `useCase`: 使用場景

### VoiceResponse
輸出回應 DTO，包含：
- `requestId`: 請求 ID
- `status`: 生成狀態
- `audioUrl`: 音頻 URL
- `localPath`: 本地路徑
- `durationSeconds`: 音頻時長
- `processingTime`: 處理耗時
- `qualityMetrics`: 質量指標

### VoiceGenerationService
核心服務接口，提供：
- `generateVoice()`: 生成單個語音
- `generateVoiceBatch()`: 批次生成
- `getAvailableVoices()`: 獲取可用語音
- `getVoicesByLanguage()`: 按語言獲取
- `validateText()`: 驗證文本
- `optimizeText()`: 優化文本
- `estimateAudioDuration()`: 估算時長

## 錯誤處理

API 返回標準 HTTP 狀態碼：

| 狀態碼 | 說明 |
|--------|------|
| 201 | 生成成功 |
| 400 | 請求參數無效 |
| 401 | API 密鑰無效 |
| 429 | 請求過於頻繁 |
| 500 | 服務器內部錯誤 |

## 性能考慮

1. **快取策略**: 使用 Redis 快取相同文本的生成結果
2. **批次處理**: 支持批量生成，提高效率
3. **音頻格式**: 選擇合適的格式平衡質量和文件大小
4. **語速調整**: 根據場景調整語速提升用戶體驗

## 常見問題

**Q: 文本最長支援多少？**
A: 最長 4096 字符。建議分段處理更長的文本。

**Q: 哪個模型質量更好？**
A: `tts-1-hd` 提供更高質量，但響應時間更長。`tts-1` 適合實時應用。

**Q: 如何提高語音自然度？**
A: 使用詳細的標點符號；選擇匹配場景的語音；適當調整語速。

**Q: 生成的音頻會過期嗎？**
A: 是的，OpenAI 生成的 URL 通常在 1 小時後過期。建議及時下載並保存。

**Q: 支援多語言嗎？**
A: 當前版本主要支援英文。建議使用英文文本獲得最佳效果。

## 許可證

MIT License

## 聯繫方式

如有問題或建議，請提交 Issue 或 Pull Request。
