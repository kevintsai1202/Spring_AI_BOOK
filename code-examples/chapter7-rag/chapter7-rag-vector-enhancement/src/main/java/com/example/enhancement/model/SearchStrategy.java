package com.example.enhancement.model;

/**
 * 搜尋策略枚舉
 *
 * 定義不同的資料檢索策略
 */
public enum SearchStrategy {
    /**
     * 語義搜尋 - 基於向量相似度
     */
    SEMANTIC_SEARCH,

    /**
     * 關鍵字搜尋 - 基於文本匹配
     */
    KEYWORD_SEARCH,

    /**
     * 混合搜尋 - 結合語義和關鍵字
     */
    HYBRID_SEARCH
}
