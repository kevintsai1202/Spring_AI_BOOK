-- 對話記憶表 (Chat Memory)
CREATE TABLE IF NOT EXISTS chat_memory (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata TEXT
);

CREATE INDEX IF NOT EXISTS idx_chat_memory_conversation ON chat_memory(conversation_id);
CREATE INDEX IF NOT EXISTS idx_chat_memory_created_at ON chat_memory(created_at);

-- 對話摘要表 (Conversation Summary)
CREATE TABLE IF NOT EXISTS conversation_summary (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL UNIQUE,
    summary TEXT NOT NULL,
    main_topics TEXT,
    key_decisions TEXT,
    action_items TEXT,
    message_count INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_summary_conversation ON conversation_summary(conversation_id);

-- 記憶分析表 (Memory Analytics)
CREATE TABLE IF NOT EXISTS memory_analytics (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    total_messages INT NOT NULL,
    user_messages INT NOT NULL,
    assistant_messages INT NOT NULL,
    avg_message_length DOUBLE NOT NULL,
    first_message_time TIMESTAMP,
    last_message_time TIMESTAMP,
    total_duration_seconds BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_analytics_conversation ON memory_analytics(conversation_id);
