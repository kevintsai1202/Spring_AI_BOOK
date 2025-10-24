# Spring AI 圖片生成 (5.3 - Text-to-Image)

企業級文字轉圖片應用，使用 Spring AI 框架與 OpenAI DALL-E 模型實現高質量圖片生成功能。

## 專案概述

本專案提供完整的圖片生成 API，支援以下功能：
- 單個文本轉圖片生成
- 電商產品圖片專用生成
- 批次圖片生成
- 生成進度查詢
- 快取管理
- 提示詞驗證

## 技術棧

- **框架**: Spring Boot 3.3.0
- **AI 框架**: Spring AI 1.0.3
- **AI 模型**: OpenAI DALL-E 3
- **快取**: Redis (可選)
- **構建工具**: Maven 3.9+
- **Java**: JDK 21+

## 專案結構

```
chapter5-5.3-image-generation/
├── src/main/java/com/example/
│   ├── config/                     # 配置類
│   │   └── ImageModelConfig.java   # 圖片模型配置
│   ├── controller/                 # REST 控制器
│   │   └── ImageGenerationController.java
│   ├── dto/                        # 數據傳輸對象
│   │   ├── ImageGenerationRequest.java
│   │   ├── ImageGenerationResponse.java
│   │   ├── ProductImageRequest.java
│   │   └── ProductImageResponse.java
│   └── service/                    # 服務層
│       ├── ImageGenerationService.java
│       └── impl/
│           └── ImageGenerationServiceImpl.java
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
      model: dall-e-3
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
cd "E:\Spring_AI_BOOK\code-examples\chapter5-5.3-image-generation"

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
java -jar target/spring-ai-image-generation-1.0.0.jar
```

應用程式將在 `http://localhost:8080` 啟動。

## API 文檔

### 1. 生成單個圖片

**端點**: `POST /api/images/generate`

**請求示例**:
```json
{
  "prompt": "一隻在雪地裡的可愛狐狸",
  "model": "dall-e-3",
  "size": "1024x1024",
  "quality": "hd",
  "style": "vivid",
  "quantity": 1,
  "userId": "user123"
}
```

**回應示例**:
```json
{
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUCCESS",
  "imageUrls": [
    "https://oaidalleapiprodscus.blob.core.windows.net/private/..."
  ],
  "revisedPrompt": "A cute fox in snowy landscape...",
  "model": "dall-e-3",
  "processingTime": 3245,
  "createdAt": "2025-10-24T22:30:00Z",
  "expiresAt": "2025-10-25T22:30:00Z"
}
```

### 2. 生成電商產品圖片

**端點**: `POST /api/images/products`

**請求示例**:
```json
{
  "productName": "無線降噪耳機",
  "productDescription": "高端藍牙耳機，主動降噪功能",
  "productCategory": "電子產品",
  "backgroundStyle": "white",
  "perspective": "3/4 view",
  "lighting": "studio",
  "quantity": 1,
  "productId": "PROD-001",
  "sku": "SKU-12345"
}
```

**回應示例**:
```json
{
  "requestId": "550e8400-e29b-41d4-a716-446655440001",
  "status": "SUCCESS",
  "imageUrls": [
    "https://oaidalleapiprodscus.blob.core.windows.net/private/..."
  ],
  "qualityMetrics": {
    "qualityScore": 9.2,
    "clarityScore": 9.5,
    "backgroundFitScore": 8.8,
    "lightingScore": 9.0,
    "colorAccuracyScore": 9.3
  },
  "productId": "PROD-001",
  "sku": "SKU-12345"
}
```

### 3. 批次生成圖片

**端點**: `POST /api/images/batch`

**請求示例**:
```json
[
  {
    "prompt": "美麗的日落風景",
    "model": "dall-e-3",
    "size": "1024x1024"
  },
  {
    "prompt": "現代化城市街道",
    "model": "dall-e-3",
    "size": "1024x1024"
  }
]
```

### 4. 查詢生成進度

**端點**: `GET /api/images/{requestId}/status`

**示例**: `GET /api/images/550e8400-e29b-41d4-a716-446655440000/status`

### 5. 清除快取

**端點**: `DELETE /api/images/{requestId}/cache`

**示例**: `DELETE /api/images/550e8400-e29b-41d4-a716-446655440000/cache`

### 6. 驗證提示詞

**端點**: `POST /api/images/validate-prompt`

**請求示例**:
```json
{
  "prompt": "一隻在雪地裡的可愛狐狸"
}
```

**回應示例**:
```json
{
  "valid": true,
  "originalPrompt": "一隻在雪地裡的可愛狐狸",
  "optimizedPrompt": "A cute fox in snowy landscape, realistic style",
  "message": "提示詞有效"
}
```

### 7. 健康檢查

**端點**: `GET /api/images/health`

**回應示例**:
```json
{
  "status": "UP",
  "service": "Image Generation Service",
  "message": "服務正常運行"
}
```

## 使用示例

### cURL 請求

```bash
# 生成圖片
curl -X POST http://localhost:8080/api/images/generate \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "一隻在雪地裡的可愛狐狸",
    "model": "dall-e-3",
    "size": "1024x1024"
  }'

# 查詢健康狀態
curl http://localhost:8080/api/images/health
```

### Java 客戶端

```java
RestTemplate restTemplate = new RestTemplate();

ImageGenerationRequest request = ImageGenerationRequest.builder()
    .prompt("一隻在雪地裡的可愛狐狸")
    .model("dall-e-3")
    .size("1024x1024")
    .quality("hd")
    .style("vivid")
    .build();

ImageGenerationResponse response = restTemplate.postForObject(
    "http://localhost:8080/api/images/generate",
    request,
    ImageGenerationResponse.class
);

System.out.println("圖片 URL: " + response.getImageUrls()[0]);
```

## 配置說明

### application.yml 主要配置

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      model: dall-e-3

  application:
    name: spring-ai-image-generation

app:
  image:
    # 圖片生成配置
    model: dall-e-3
    quality: hd                    # hd 或 standard
    style: vivid                   # vivid 或 natural
    size: 1024x1024               # 1024x1024, 1792x1024, 1024x1792

    # 批次和超時配置
    max-batch-size: 5
    default-timeout: 30s           # 生成超時時間

    # 快取配置
    cache-enabled: true
    cache-ttl: 3600               # 快取過期時間（秒）
    output-directory: ./generated-images
```

## 主要類說明

### ImageGenerationRequest
輸入請求 DTO，包含：
- `prompt`: 圖片描述文本
- `model`: 使用的模型
- `size`: 圖片尺寸
- `quality`: 圖片質量
- `style`: 藝術風格
- `quantity`: 生成數量
- `userId`: 用戶 ID

### ImageGenerationResponse
輸出回應 DTO，包含：
- `requestId`: 請求 ID
- `status`: 生成狀態
- `imageUrls`: 生成的圖片 URL 數組
- `revisedPrompt`: 模型修改後的提示詞
- `processingTime`: 處理耗時

### ImageGenerationService
核心服務接口，提供：
- `generateImage()`: 生成單個圖片
- `generateProductImage()`: 生成產品圖片
- `generateImageBatch()`: 批次生成
- `validatePrompt()`: 驗證提示詞
- `clearCache()`: 清除快取

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

1. **快取策略**: 使用 Redis 快取重複提示詞的結果
2. **批次處理**: 支持批量生成，提高效率
3. **非同步處理**: 可使用 @Async 進行異步調用
4. **超時設置**: 配置合理的超時時間以避免長期等待

## 常見問題

**Q: 如何處理 API 速率限制？**
A: 實現重試邏輯和指數退避策略。

**Q: 生成的圖片會過期嗎？**
A: 是的，OpenAI 生成的 URL 通常在 1 小時後過期。建議及時下載並保存。

**Q: 如何提高生成質量？**
A: 使用詳細、具體的提示詞；選擇 `quality: hd` 和 `style: vivid`。

## 許可證

MIT License

## 聯繫方式

如有問題或建議，請提交 Issue 或 Pull Request。
