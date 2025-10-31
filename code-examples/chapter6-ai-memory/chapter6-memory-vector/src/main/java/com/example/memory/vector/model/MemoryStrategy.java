package com.example.memory.vector.model;

/**
 * 記憶檢索策略枚舉
 */
public enum MemoryStrategy {
    /**
     * 只使用短期記憶
     */
    SHORT_TERM_ONLY,

    /**
     * 只使用長期記憶
     */
    LONG_TERM_ONLY,

    /**
     * 混合模式 (短期 + 長期)
     */
    HYBRID
}
