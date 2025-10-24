# Chapter 5 API 测试计划

## 项目编译和运行

### 编译

```bash
# 设置 Java 21 环境变量
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"

# 编译项目
cd E:\Spring_AI_BOOK\code-examples\chapter5-spring-ai-advanced
mvn clean compile
```

**预期结果：** BUILD SUCCESS ✅

### 运行

```bash
# 方式 1：使用 Maven
mvn spring-boot:run

# 方式 2：编译后运行
mvn clean package
java -jar target/spring-ai-advanced-0.0.1-SNAPSHOT.jar
```

**预期结果：** 应用启动在 `http://localhost:8080`

---

## API 端点测试

### 1. 提示詞範本 API

#### 1.1 解釋技術主題

**端點：** `GET /api/template/explain`

**參數：**
- `topic` (String): 技術主題（如：Spring Framework）
- `level` (String): 程度（初級/中級/高級，默認：中級）

**測試命令：**
```bash
curl "http://localhost:8080/api/template/explain?topic=Spring+Framework&level=intermediate"
```

**預期結果：**
- HTTP 200 OK
- 返回 AI 生成的技術解釋
- 涵蓋核心概念、特性、使用場景、代碼示例

---

#### 1.2 解釋框架版本

**端點：** `GET /api/template/framework`

**參數：**
- `framework` (String): 框架名稱（如：Spring Boot）

**測試命令：**
```bash
curl "http://localhost:8080/api/template/framework?framework=Spring+Boot"
```

**預期結果：**
- HTTP 200 OK
- 返回關於框架版本和特性的信息

---

### 2. Tool Calling API

#### 2.1 基礎 Tool Calling

**端點：** `GET /api/tool-calling/basic`

**參數：**
- `prompt` (String): AI 提示詞

**測試命令：**
```bash
curl "http://localhost:8080/api/tool-calling/basic?prompt=What+is+the+current+time"
```

**預期結果：**
- HTTP 200 OK
- 返回 AI 回應

---

#### 2.2 當前時間查詢

**端點：** `GET /api/tool-calling/current-time`

**參數：**
- `format` (String): 時間格式（默認：yyyy-MM-dd HH:mm:ss）

**測試命令：**
```bash
# 默認格式
curl "http://localhost:8080/api/tool-calling/current-time"

# 自定義格式
curl "http://localhost:8080/api/tool-calling/current-time?format=yyyy%2FMM%2Fdd"
```

**預期結果：**
- HTTP 200 OK
- 返回當前時間（格式：yyyy-MM-dd HH:mm:ss）

---

### 3. 多模態 API

#### 3.1 圖片分析

**端點：** `POST /api/multimodal/image-analysis`

**參數：**
- `file` (MultipartFile): 圖片檔案（JPEG, PNG, GIF, WebP）
- `message` (String): 分析要求

**使用 curl 測試：**
```bash
curl -X POST http://localhost:8080/api/multimodal/image-analysis \
  -F "file=@/path/to/image.jpg" \
  -F "message=Describe this image"
```

**使用 Postman 測試：**
1. 方法：POST
2. URL：`http://localhost:8080/api/multimodal/image-analysis`
3. Body → form-data：
   - Key: `file`, Value: 選擇圖片檔案
   - Key: `message`, Value: "Analyze this image"
4. 點擊 Send

**預期結果：**
- HTTP 200 OK
- 返回 AI 對圖片的分析結果

---

## 測試驗證清單

### 編譯驗證
- [ ] 項目編譯成功（BUILD SUCCESS）
- [ ] 沒有編譯錯誤
- [ ] 沒有編譯警告

### 運行驗證
- [ ] 應用成功啟動
- [ ] 應用監聽 8080 端口
- [ ] 日誌輸出正常

### API 功能驗證

#### 提示詞範本
- [ ] /api/template/explain 返回 200
- [ ] /api/template/explain 返回有效的技術說明
- [ ] /api/template/framework 返回 200
- [ ] /api/template/framework 返回有效的框架信息

#### Tool Calling
- [ ] /api/tool-calling/basic 返回 200
- [ ] /api/tool-calling/current-time 返回 200
- [ ] /api/tool-calling/current-time 返回正確的時間格式

#### 多模態
- [ ] /api/multimodal/image-analysis 接受圖片上傳
- [ ] /api/multimodal/image-analysis 返回 200
- [ ] /api/multimodal/image-analysis 返回有效的圖片分析結果
- [ ] 驗證不支持的格式被拒絕
- [ ] 驗證超大文件被拒絕

---

## 測試工具

### curl（命令行）

優點：
- 簡單快速
- 無需額外工具
- 易於自動化

示例：
```bash
curl -v http://localhost:8080/api/template/explain?topic=Docker
```

### Postman（圖形界面）

優點：
- 友好的界面
- 支持複雜請求
- 支持文件上傳
- 可以保存請求歷史

步驟：
1. 下載並安裝 Postman
2. 創建新 Request
3. 輸入 URL 和參數
4. 點擊 Send

### PowerShell

示例：
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/template/explain?topic=Spring" `
  -Method GET | Select-Object -Property StatusCode, Content
```

---

## 環境要求

### API Key 配置

確保設置以下環境變量：

```bash
# Linux/Mac
export OPENAI_API_KEY="your-api-key-here"

# Windows PowerShell
$env:OPENAI_API_KEY = "your-api-key-here"
```

### 依賴檢查

```bash
# 驗證 Java 版本
java -version
# 應返回 openjdk version "21" 或更高

# 驗證 Maven 版本
mvn -version
# 應返回 Apache Maven 3.9+
```

---

## 常見問題排查

### 問題 1：編譯失敗 - Java 版本錯誤

**症狀：** `bad class file: ... wrong version 61.0, should be 52.0`

**解決：**
```bash
$env:JAVA_HOME = "D:\java\jdk-21"
$env:Path = "D:\java\jdk-21\bin;$env:Path"
java -version  # 驗證版本
```

### 問題 2：應用無法啟動 - 端口被佔用

**症狀：** `Address already in use`

**解決：**
```bash
# 方法 1：更改端口
export SERVER_PORT=8081

# 方法 2：殺死現有進程
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### 問題 3：API 返回 400 錯誤

**症狀：** 參數不被識別

**解決：**
- 確保 URL 編碼正確（如空格用 %20 或 +）
- 確保參數名稱完全匹配
- 檢查大小寫

### 問題 4：圖片上傳失敗

**症狀：** 上傳後返回錯誤

**解決：**
- 確保圖片格式為支持的類型（JPEG, PNG, GIF, WebP）
- 確保文件大小小於 10MB
- 檢查 Content-Type

---

## 測試結果記錄

| 端點 | 方法 | 參數 | 預期結果 | 實際結果 | 狀態 |
|------|------|------|--------|--------|------|
| /api/template/explain | GET | topic=Docker | HTTP 200 | | |
| /api/template/framework | GET | framework=Spring Boot | HTTP 200 | | |
| /api/tool-calling/basic | GET | prompt=time | HTTP 200 | | |
| /api/tool-calling/current-time | GET | (無參數) | HTTP 200 | | |
| /api/multimodal/image-analysis | POST | file + message | HTTP 200 | | |

---

## 下一步

完成所有測試後：

1. ✅ 驗證所有功能正常
2. ✅ 記錄任何問題或改進建議
3. ✅ 準備項目部署
4. ✅ 編寫最終文檔

---

**測試時間：** [填寫日期]
**測試人員：** [填寫姓名]
**測試結果：** ☐ 通過 ☐ 失敗 ☐ 部分通過
