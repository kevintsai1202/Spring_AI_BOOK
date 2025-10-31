# Neo4j 向量資料庫設定指南

## 📋 概述

本專案使用 **Neo4j 5.15.0** 作為**統一資料存儲**，同時處理：
- ✅ 向量嵌入存儲與檢索
- ✅ 文檔元資料管理
- ✅ 文檔關聯關係
- ✅ 業務資料存儲

**架構優勢**: 簡化系統複雜度，無需額外的關聯式資料庫或快取層。

---

## 🚀 快速開始

### 1. 啟動 Neo4j 服務

```powershell
# 啟動 Neo4j 向量資料庫
.\start-services.ps1
```

### 2. 訪問 Neo4j Browser

啟動後，打開瀏覽器訪問：

**URL**: http://localhost:7474

**登入資訊**:
- 用戶名：`neo4j`
- 密碼：`test1234`

### 3. 啟動應用程式

```powershell
# 運行 Spring Boot 應用程式
.\run-app.ps1
```

### 4. 停止服務

```powershell
# 停止所有服務
.\stop-services.ps1
```

---

## 🔧 配置說明

### Docker Compose 配置

```yaml
neo4j:
  image: neo4j:5.15.0
  ports:
    - "7474:7474"  # HTTP UI
    - "7687:7687"  # Bolt 協議
  environment:
    - NEO4J_AUTH=neo4j/test1234
```

### Application 配置

```yaml
spring:
  ai:
    vectorstore:
      neo4j:
        uri: bolt://localhost:7687
        username: neo4j
        password: test1234
        database: neo4j
        index-name: vector-index
        label: Document
        embedding-property: embedding
```

---

## 📊 Neo4j 向量索引

### 自動創建索引

Spring AI 會自動創建向量索引，但你也可以手動創建：

```cypher
// 創建向量索引
CALL db.index.vector.createNodeIndex(
  'vector-index',                    // 索引名稱
  'Document',                        // 節點標籤
  'embedding',                       // 嵌入屬性
  1536,                             // 向量維度（OpenAI text-embedding-3-small）
  'cosine'                          // 相似度計算方式
)
```

### 查看現有索引

```cypher
// 列出所有向量索引
SHOW INDEXES
```

---

## 🔍 常用 Cypher 查詢

### 1. 查看文檔數量

```cypher
MATCH (d:Document)
RETURN count(d) as documentCount
```

### 2. 查看最近添加的文檔

```cypher
MATCH (d:Document)
RETURN d.id, d.content, d.metadata
ORDER BY d.metadata.processed_at DESC
LIMIT 10
```

### 3. 向量相似度搜索（手動）

```cypher
// 使用向量索引搜索相似文檔
CALL db.index.vector.queryNodes(
  'vector-index',                    // 索引名稱
  10,                                // 返回前 10 個結果
  [0.1, 0.2, ...]                   // 查詢向量（1536 維）
)
YIELD node, score
RETURN node.content, score
ORDER BY score DESC
```

### 4. 刪除所有文檔

```cypher
// ⚠️ 警告：這會刪除所有文檔
MATCH (d:Document)
DETACH DELETE d
```

### 5. 查看文檔元資料統計

```cypher
MATCH (d:Document)
RETURN
  d.metadata.detected_language as language,
  count(d) as count
ORDER BY count DESC
```

---

## 🛠️ 管理操作

### 查看資料庫統計

```cypher
// 查看節點和關係統計
CALL apoc.meta.stats()
```

### 資料庫備份

```powershell
# 停止 Neo4j
docker-compose stop neo4j

# 複製資料卷
docker run --rm -v chapter7-rag-vector-enhancement_neo4j_data:/data -v ${PWD}/backup:/backup alpine tar czf /backup/neo4j-backup.tar.gz /data

# 重啟 Neo4j
docker-compose start neo4j
```

### 資料庫還原

```powershell
# 停止 Neo4j
docker-compose stop neo4j

# 刪除現有資料
docker volume rm chapter7-rag-vector-enhancement_neo4j_data

# 還原資料
docker run --rm -v chapter7-rag-vector-enhancement_neo4j_data:/data -v ${PWD}/backup:/backup alpine sh -c "cd /data && tar xzf /backup/neo4j-backup.tar.gz --strip 1"

# 重啟 Neo4j
docker-compose start neo4j
```

---

## 📈 性能優化

### 連接池配置

**開發環境** (application-dev.yml):
```yaml
spring:
  ai:
    vectorstore:
      neo4j:
        pool:
          max-size: 50
          acquisition-timeout: 60s
```

**生產環境** (application-prod.yml):
```yaml
spring:
  ai:
    vectorstore:
      neo4j:
        pool:
          max-size: 100
          acquisition-timeout: 120s
```

### 索引優化

```cypher
// 查看索引使用情況
CALL db.index.vector.queryNodes('vector-index', 10, [/* 向量 */])
YIELD node, score
RETURN score

// 重建索引（如果性能下降）
DROP INDEX `vector-index` IF EXISTS;
CALL db.index.vector.createNodeIndex(
  'vector-index', 'Document', 'embedding', 1536, 'cosine'
)
```

---

## 🔐 安全設定

### 修改密碼

1. **在 docker-compose.yml 中修改**:
```yaml
environment:
  - NEO4J_AUTH=neo4j/your_new_password
```

2. **在 application.yml 中修改**:
```yaml
spring:
  ai:
    vectorstore:
      neo4j:
        password: your_new_password
```

3. **重啟服務**:
```powershell
.\stop-services.ps1
.\start-services.ps1
```

---

## 🐛 故障排除

### 問題 1: 無法連接 Neo4j

**症狀**: `Unable to connect to bolt://localhost:7687`

**解決方法**:
1. 檢查 Docker 服務是否運行：
   ```powershell
   docker-compose ps
   ```

2. 查看 Neo4j 日誌：
   ```powershell
   docker-compose logs neo4j
   ```

3. 確認端口未被佔用：
   ```powershell
   netstat -ano | findstr "7687"
   ```

### 問題 2: 認證失敗

**症狀**: `Authentication failure`

**解決方法**:
1. 確認密碼正確（預設：`test1234`）
2. 刪除 Neo4j 資料卷並重新啟動：
   ```powershell
   docker-compose down -v
   docker-compose up -d
   ```

### 問題 3: 向量索引未創建

**症狀**: 搜索時報錯 `Index not found`

**解決方法**:
1. 手動創建索引（見上方「Neo4j 向量索引」章節）
2. 確認 Spring AI 配置正確
3. 檢查應用程式日誌

---

## 📚 參考資源

- [Neo4j 官方文檔](https://neo4j.com/docs/)
- [Spring AI Neo4j Vector Store](https://docs.spring.io/spring-ai/reference/api/vectordbs/neo4j.html)
- [Neo4j 向量索引文檔](https://neo4j.com/docs/cypher-manual/current/indexes-for-vector-search/)
- [Cypher 查詢語言](https://neo4j.com/docs/cypher-manual/current/)

---

## 🎯 下一步

1. ✅ 啟動 Neo4j 服務
2. ✅ 配置應用程式連接
3. ✅ 驗證向量索引
4. 📝 開始使用 RAG 功能
5. 🚀 生產環境部署

---

**注意**: 本配置僅供開發和測試使用。生產環境請參考 Neo4j 官方文檔進行安全加固和性能優化。
