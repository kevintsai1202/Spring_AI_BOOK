package com.example.enhancement.model;

/**
 * 文本清理規則接口
 *
 * 允許用戶定義自定義的文本清理規則
 */
@FunctionalInterface
public interface TextCleaningRule {

    /**
     * 應用清理規則到文本
     *
     * @param text 原始文本
     * @return 清理後的文本
     */
    String apply(String text);
}
