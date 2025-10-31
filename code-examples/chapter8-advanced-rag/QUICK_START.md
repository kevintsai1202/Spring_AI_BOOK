# Advanced RAG 系統快速啟動指南

## 📌 當前狀態

**階段**: 階段一 - 基礎架構搭建（40% 完成）
**可編譯性**: 待驗證
**可運行性**: 未測試

---

## ✅ 已完成的文件

### 配置文件
1. ✅ `pom.xml` - Maven 專案配置（已修復依賴）
2. ✅ `docker-compose.yml` - Docker 服務
3. ✅ `application.yml` - 主配置
4. ✅ `application-dev.yml` - 開發環境配置
5. ✅ `application-prod.yml` - 生產環境配置

### Java 源代碼
1. ✅ `AdvancedRagApplication.java` - 主應用類
2. ✅ `properties/RAGProperties.java`
3. ✅ `properties/EmbeddingProperties.java`
4. ✅ `properties/ModerationProperties.java`
5. ✅ `properties/EvaluationProperties.java`
6. ✅ `controller/HealthController.java` - 健康檢查

### 測試數據
1. ✅ `test-cases/basic-qa.json`
2. ✅ `test-cases/domain-specific.json`
3. ✅ `test-cases/edge-cases.json`

---

## 🚀 快速測試步驟

### 方法 1: 使用編譯腳本（推薦）

```powershell
cd E:\Spring_AI_BOOK\code-examples\chapter8-advanced-rag
.\scripts\compile.ps1
```

### 方法 2: 手動編譯

```powershell
# 1. 設置 Java 21
$env:JAVA_HOME="D:\java\jdk-21"
$env:Path="D:\java\jdk-21\bin;$env:Path"

# 2. 進入專案目錄
cd E:\Spring_AI_BOOK\code-examples\chapter8-advanced-rag

# 3. 編譯
mvn clean compile
```

### 方法 3: 完整啟動（需要先通過編譯）

```powershell
# 執行完整設置和啟動腳本
.\scripts\setup-and-run.ps1
```

---

## ⚠️ 已知問題

### 1. 編譯問題
- **問題**: Maven 可能使用錯誤的 Java 版本
- **解決**: 使用提供的編譯腳本，它會自動設置 Java 21

### 2. Docker 服務
- **問題**: PgVector 和 Redis 需要手動啟動
- **解決**:
  ```powershell
  docker-compose up -d pgvector redis
  ```

---

## 📝 待完成的核心類

為了讓專案可以完整運行，還需要創建以下核心類：

### 優先級 P0（必須）
- `config/VectorStoreConfiguration.java` - 向量數據庫配置
- `exception/GlobalExceptionHandler.java` - 全局異常處理

### 優先級 P1（重要）
- DTO 類（10個）
- Model 類（10個）
- Config 類（剩餘 5個）
- Exception 類（剩餘 4個）
- Util 工具類（3個）

### 優先級 P2（後續階段）
- Service 服務層（階段二）
- Advisor 層（階段三）
- 其他功能模塊（階段四-七）

---

## 💡 下一步建議

### 選項 A: 驗證當前架構
1. 運行 `.\scripts\compile.ps1` 測試編譯
2. 如果編譯成功，嘗試啟動應用（即使功能不完整）
3. 訪問 http://localhost:8080/api/v1/health 驗證健康狀態

### 選項 B: 完成基礎類創建
1. 先創建 P0 優先級的類
2. 創建基本的 DTO 和 Model
3. 創建異常處理框架
4. 然後再驗證編譯和運行

### 選項 C: 分階段開發（推薦）
1. 完成階段一所有基礎類
2. 測試編譯和基本運行
3. 進入階段二開發核心 RAG 功能
4. 每個階段結束都進行測試

---

## 📁 專案文件結構

```
chapter8-advanced-rag/
├── pom.xml                                    ✅
├── docker-compose.yml                         ✅
├── init-scripts/
│   └── 01-init-vector-extension.sql          ✅
├── prometheus/
│   └── prometheus.yml                         ✅
├── scripts/
│   ├── compile.ps1                            ✅
│   └── setup-and-run.ps1                      ✅
├── src/main/
│   ├── java/com/example/advancedrag/
│   │   ├── AdvancedRagApplication.java       ✅
│   │   ├── properties/                        ✅ (4個類)
│   │   ├── controller/                        ✅ (1個類)
│   │   ├── dto/                               ⏳ (待創建)
│   │   ├── model/                             ⏳ (待創建)
│   │   ├── exception/                         ⏳ (待創建)
│   │   ├── util/                              ⏳ (待創建)
│   │   ├── config/                            ⏳ (待創建)
│   │   └── service/                           ⏳ (階段二)
│   └── resources/
│       ├── application.yml                    ✅
│       ├── application-dev.yml                ✅
│       ├── application-prod.yml               ✅
│       └── test-cases/                        ✅ (3個文件)
├── docs/
│   ├── spec.md                                ✅
│   ├── api.md                                 ✅
│   ├── PROJECT_STRUCTURE.md                   ✅
│   └── README.md                              ✅
├── PROGRESS.md                                ✅
└── QUICK_START.md                             ✅ (本文件)
```

---

## 🔗 相關文檔

- 完整開發進度: [PROGRESS.md](PROGRESS.md)
- 技術規格: [docs/spec.md](docs/spec.md)
- API 設計: [docs/api.md](docs/api.md)
- 專案結構: [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md)
- 專案說明: [docs/README.md](docs/README.md)

---

**最後更新**: 2025-01-30
**當前階段**: 階段一 - 基礎架構搭建（40%）
**下一步**: 編譯測試或繼續創建基礎類
