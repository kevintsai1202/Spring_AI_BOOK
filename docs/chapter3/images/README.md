# 第3章所需截圖清單

本目錄包含第3章文件中引用的所有截圖。

## 3.1 資料驗證與錯誤處理

### 需要的截圖

1. **3.1-validation-annotations.png**
   - 說明：Bean Validation 註解範例
   - 內容：展示程式碼中使用的驗證註解（@NotBlank, @Email, @Size 等）
   - 建議：截取 UserRegistrationRequest.java 的程式碼
   - 尺寸建議：800x400

2. **3.1-custom-validator.png**
   - 說明：自訂驗證器程式碼
   - 內容：顯示 StrongPassword 註解和 StrongPasswordValidator 實作
   - 建議：包含註解定義和驗證邏輯
   - 尺寸建議：800x500

3. **3.1-validation-error.png**
   - 說明：驗證錯誤回應範例
   - 內容：使用 Postman 測試，顯示驗證失敗的錯誤訊息
   - 建議：提交不完整或格式錯誤的資料
   - 尺寸建議：800x500

## 3.2 檔案上傳與下載

### 需要的截圖

4. **3.2-file-upload-postman.png**
   - 說明：Postman 檔案上傳測試
   - 內容：展示 POST /api/files/upload 的請求設定
   - 建議：顯示 form-data 中的 file 欄位
   - 尺寸建議：1280x720

5. **3.2-file-metadata.png**
   - 說明：檔案元資料回應
   - 內容：顯示檔案上傳成功後的 JSON 回應
   - 建議：包含 originalFilename、storedFilename、downloadUrl 等欄位
   - 尺寸建議：800x400

6. **3.2-file-directory.png**
   - 說明：檔案儲存目錄結構
   - 內容：展示 ./uploads 目錄中儲存的檔案
   - 建議：使用檔案總管或 ls 命令
   - 尺寸建議：600x400

## 3.3 API 文件化（Swagger/OpenAPI）

### 需要的截圖

7. **3.3-swagger-ui.png**
   - 說明：Swagger UI 主畫面
   - 內容：顯示完整的 API 文件介面
   - URL：http://localhost:8080/swagger-ui.html
   - 尺寸建議：1280x800

8. **3.3-api-endpoint.png**
   - 說明：單一 API 端點詳細資訊
   - 內容：展開某個 API 端點，顯示參數、回應格式等
   - 建議：選擇 POST /api/users/register 端點
   - 尺寸建議：1000x600

9. **3.3-try-it-out.png**
   - 說明：Swagger UI 測試功能
   - 內容：展示點擊 "Try it out" 後的測試介面
   - 建議：填入測試資料並執行
   - 尺寸建議：1000x700

10. **3.3-api-response-example.png**
    - 說明：API 回應範例
    - 內容：顯示執行測試後的回應結果
    - 建議：包含回應狀態碼和 JSON 內容
    - 尺寸建議：800x500

## 製作指南

### 啟動應用程式

⚠️ **重要**：需要先設定 Java 21 環境

```bash
# Windows PowerShell
cd E:\Spring_AI_BOOK\code-examples\chapter3-enterprise-features
.\run.bat

# 或手動設定並執行
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"
mvn clean package -DskipTests
java -jar target\chapter3-enterprise-features-1.0.0.jar
```

### API 測試步驟

1. **驗證測試**：
   ```bash
   # 使用 curl 測試（密碼驗證失敗）
   curl -X POST http://localhost:8080/api/users/register \
     -H "Content-Type: application/json" \
     -d '{"username":"test","password":"weak"}'
   ```

2. **檔案上傳測試**：
   ```bash
   # 準備一個測試圖片
   curl -X POST http://localhost:8080/api/files/upload \
     -F "file=@test.jpg"
   ```

3. **Swagger UI 測試**：
   - 開啟瀏覽器訪問 http://localhost:8080/swagger-ui.html
   - 展開 API 端點
   - 點擊 "Try it out"
   - 填入測試資料
   - 點擊 "Execute"

### 截圖要點

**Postman 截圖**：
- 清楚顯示請求 URL 和方法
- 包含請求 Headers（如需要）
- 顯示完整的請求 Body
- 顯示回應狀態碼和完整 JSON

**Swagger UI 截圖**：
- 使用完整螢幕模式
- 確保 API 端點清單完整可見
- 展開的端點要顯示完整資訊
- 測試結果要包含請求和回應

**程式碼截圖**：
- 使用 IDE（STS4 或 IntelliJ IDEA）
- 開啟語法高亮顯示
- 包含足夠的上下文
- 確保程式碼可讀性

## 截圖規範

- 格式：PNG
- 解析度：至少 1080p（1920x1080）
- 背景：使用淺色主題（更易閱讀）
- 標註：必要時使用紅框或箭頭
- 命名：使用文件中引用的檔案名稱
- 隱私：移除或遮蔽敏感資訊
