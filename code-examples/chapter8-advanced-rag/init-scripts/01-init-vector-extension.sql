-- 創建 vector 擴展
CREATE EXTENSION IF NOT EXISTS vector;

-- 創建向量存儲表（Spring AI 會自動創建，這裡僅作為參考）
-- Spring AI PgVector 會自動管理表結構

-- 授予權限
GRANT ALL PRIVILEGES ON DATABASE advanced_rag TO raguser;
