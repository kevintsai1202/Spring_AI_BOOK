package com.example.enhancement.model;

/**
 * 存取類型枚舉
 *
 * 定義資料存取的各種操作類型
 */
public enum AccessType {
    /**
     * 讀取權限
     */
    READ,

    /**
     * 寫入權限
     */
    WRITE,

    /**
     * 刪除權限
     */
    DELETE,

    /**
     * 查看敏感資料權限
     */
    VIEW_SENSITIVE,

    /**
     * 導出資料權限
     */
    EXPORT
}
