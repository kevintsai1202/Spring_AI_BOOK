package com.example.memory.advanced.model;

/**
 * 記憶策略列舉
 *
 * 定義混合記憶系統的不同策略模式
 */
public enum MemoryStrategy {
    /**
     * 僅使用短期記憶
     * 適用於查詢最近對話內容的情境
     */
    SHORT_TERM_ONLY,

    /**
     * 僅使用長期記憶
     * 適用於查詢歷史對話內容的情境
     */
    LONG_TERM_ONLY,

    /**
     * 混合策略
     * 同時使用短期和長期記憶,提供最佳的對話體驗
     */
    HYBRID
}
