package com.example.enhancement.model;

/**
 * 文檔分類器接口
 *
 * 用於實現自定義文檔分類邏輯
 */
public interface DocumentClassifier {

    /**
     * 分類文檔內容
     *
     * @param content 文檔內容
     * @return 分類結果
     */
    ClassificationResult classify(String content);

    /**
     * 取得元資料鍵名
     *
     * @return 用於存儲分類結果的元資料鍵
     */
    String getMetadataKey();
}
