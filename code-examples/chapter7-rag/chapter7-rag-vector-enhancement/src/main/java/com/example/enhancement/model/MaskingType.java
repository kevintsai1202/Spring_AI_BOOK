package com.example.enhancement.model;

/**
 * 資料脫敏類型枚舉
 *
 * 定義不同的資料脫敏策略
 */
public enum MaskingType {
    /**
     * 部分脫敏 - 保留部分字符可見
     */
    PARTIAL_MASK,

    /**
     * 完全脫敏 - 所有字符都被遮蔽
     */
    FULL_MASK,

    /**
     * 雜湊脫敏 - 使用雜湊函數
     */
    HASH_MASK,

    /**
     * 代幣化 - 使用代幣替換原始值
     */
    TOKENIZE
}
