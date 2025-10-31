package com.example.advancedrag.reranking;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * Re-ranking 提供者介面
 *
 * 定義統一的 Re-ranking API，支援多種實現：
 * - Voyage AI
 * - Cohere
 * - Jina AI
 * - 本地算法
 */
public interface RerankingProvider {

    /**
     * 對文檔進行重新排序
     *
     * @param query 查詢文本
     * @param documents 待排序的文檔列表
     * @param topK 返回前 K 個結果
     * @return 重新排序後的結果列表
     */
    List<RerankResult> rerank(String query, List<Document> documents, int topK);

    /**
     * 獲取提供者名稱
     *
     * @return 提供者名稱
     */
    String getProviderName();

    /**
     * 檢查提供者是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}
