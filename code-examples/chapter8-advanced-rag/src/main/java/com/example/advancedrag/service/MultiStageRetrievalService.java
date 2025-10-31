package com.example.advancedrag.service;

import com.example.advancedrag.model.EmbeddingContext;
import com.example.advancedrag.model.RAGQueryOptions;
import com.example.advancedrag.model.ScoredDocument;
import com.example.advancedrag.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 多階段檢索服務
 *
 * 實現兩階段檢索策略：
 * 1. 粗檢索（Coarse Retrieval）：快速檢索 Top-N 候選文檔（N=30）
 * 2. 精檢索（Fine Retrieval）：對候選文檔進行精確排序，返回 Top-K（K=5）
 *
 * 檢索策略：
 * - 階段一：向量相似度檢索（快速、召回率高）
 * - 階段二：多因子評分（語義相似度 + BM25 + 品質 + 新鮮度）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultiStageRetrievalService {

    private final VectorStore vectorStore;
    private final SmartEmbeddingService embeddingService;

    /**
     * 多階段檢索
     *
     * @param query 查詢文本
     * @param options 查詢選項
     * @return 評分文檔列表
     */
    public List<ScoredDocument> retrieve(String query, RAGQueryOptions options) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("開始多階段檢索，查詢: [{}]", query);

            // 階段一：粗檢索（Coarse Retrieval）
            List<ScoredDocument> coarseResults = coarseRetrieval(query, options);
            log.info("粗檢索完成，檢索到 {} 個候選文檔", coarseResults.size());

            // 階段二：精檢索（Fine Retrieval）- 在 RerankingService 中實現
            // 這裡只返回粗檢索結果，Re-ranking 由專門的服務處理

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("多階段檢索完成，耗時: {}ms", processingTime);

            return coarseResults;

        } catch (Exception e) {
            log.error("多階段檢索失敗", e);
            throw new RuntimeException("檢索失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 粗檢索：基於向量相似度快速檢索候選文檔
     *
     * @param query 查詢文本
     * @param options 查詢選項
     * @return 候選文檔列表
     */
    private List<ScoredDocument> coarseRetrieval(String query, RAGQueryOptions options) {
        try {
            // 1. 生成查詢 Embedding
            List<Double> queryEmbedding = embeddingService.generateEmbedding(
                    query,
                    EmbeddingContext.highAccuracy()
            );

            // 2. 構建檢索請求
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(options.getCoarseTopK())
                    .similarityThreshold(options.getSimilarityThreshold())
                    .build();

            // 3. 執行向量檢索
            List<Document> documents = vectorStore.similaritySearch(searchRequest);

            log.debug("向量檢索完成，檢索到 {} 個文檔", documents.size());

            // 4. 轉換為 ScoredDocument
            List<ScoredDocument> scoredDocuments = new ArrayList<>();
            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);

                // 從元數據中獲取相似度分數（如果有）
                Double similarityScore = extractSimilarityScore(doc);

                ScoredDocument scoredDoc = ScoredDocument.builder()
                        .document(doc)
                        .score(similarityScore)
                        .semanticScore(similarityScore)
                        .build();

                scoredDocuments.add(scoredDoc);
            }

            return scoredDocuments;

        } catch (Exception e) {
            log.error("粗檢索失敗", e);
            return List.of();
        }
    }

    /**
     * 混合檢索：組合向量檢索和關鍵詞檢索
     *
     * @param query 查詢文本
     * @param keywords 關鍵詞列表
     * @param options 查詢選項
     * @return 候選文檔列表
     */
    public List<ScoredDocument> hybridRetrieval(String query, List<String> keywords, RAGQueryOptions options) {
        try {
            log.info("開始混合檢索，查詢: [{}]，關鍵詞: {}", query, keywords);

            // 1. 向量檢索
            List<ScoredDocument> vectorResults = coarseRetrieval(query, options);

            // 2. 關鍵詞檢索（簡化版：基於內容匹配）
            List<ScoredDocument> keywordResults = keywordSearch(keywords, options);

            // 3. 合併結果（去重）
            Map<String, ScoredDocument> mergedResults = vectorResults.stream()
                    .collect(Collectors.toMap(
                            doc -> doc.getDocument().getId(),
                            doc -> doc,
                            (existing, replacement) -> existing
                    ));

            // 添加關鍵詞檢索結果
            for (ScoredDocument keywordDoc : keywordResults) {
                String docId = keywordDoc.getDocument().getId();
                if (mergedResults.containsKey(docId)) {
                    // 已存在，提升分數
                    ScoredDocument existing = mergedResults.get(docId);
                    Double boostedScore = (existing.getScore() + keywordDoc.getBm25Score()) / 2;
                    existing.setScore(boostedScore);
                } else {
                    // 新文檔，添加
                    mergedResults.put(docId, keywordDoc);
                }
            }

            // 4. 按分數排序並限制數量
            List<ScoredDocument> results = mergedResults.values().stream()
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(options.getCoarseTopK())
                    .toList();

            log.info("混合檢索完成，檢索到 {} 個文檔", results.size());

            return results;

        } catch (Exception e) {
            log.error("混合檢索失敗", e);
            return coarseRetrieval(query, options);
        }
    }

    /**
     * 關鍵詞搜索（簡化版 BM25）
     *
     * @param keywords 關鍵詞列表
     * @param options 查詢選項
     * @return 匹配的文檔列表
     */
    private List<ScoredDocument> keywordSearch(List<String> keywords, RAGQueryOptions options) {
        try {
            // 使用 VectorStore 的全文檢索功能（如果支持）
            // 這裡簡化實現：將關鍵詞組合成查詢字符串
            String combinedQuery = String.join(" ", keywords);

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(combinedQuery)
                    .topK(options.getCoarseTopK() / 2) // 關鍵詞檢索返回一半數量
                    .similarityThreshold(0.5)
                    .build();

            List<Document> documents = vectorStore.similaritySearch(searchRequest);

            // 計算 BM25 分數（簡化版：基於關鍵詞匹配數量）
            return documents.stream()
                    .map(doc -> {
                        double bm25Score = calculateSimpleBM25(doc.getText(), keywords);

                        return ScoredDocument.builder()
                                .document(doc)
                                .score(bm25Score)
                                .bm25Score(bm25Score)
                                .build();
                    })
                    .filter(doc -> doc.getBm25Score() > 0.0)
                    .toList();

        } catch (Exception e) {
            log.error("關鍵詞搜索失敗", e);
            return List.of();
        }
    }

    /**
     * 計算簡化版 BM25 分數
     *
     * @param content 文檔內容
     * @param keywords 關鍵詞列表
     * @return BM25 分數（0-1）
     */
    private double calculateSimpleBM25(String documentContent, List<String> keywords) {
        if (keywords.isEmpty()) {
            return 0.0;
        }

        String lowerContent = documentContent.toLowerCase();
        int matchCount = 0;

        for (String keyword : keywords) {
            int termFreq = TextUtil.termFrequency(lowerContent, keyword.toLowerCase());
            if (termFreq > 0) {
                matchCount++;
            }
        }

        // 簡化評分：匹配關鍵詞的比例
        return (double) matchCount / keywords.size();
    }

    /**
     * 從文檔元數據中提取相似度分數
     *
     * @param document 文檔
     * @return 相似度分數
     */
    private Double extractSimilarityScore(Document document) {
        try {
            // Spring AI VectorStore 會在元數據中存儲 similarity score
            Map<String, Object> metadata = document.getMetadata();

            if (metadata.containsKey("distance")) {
                // distance 越小表示越相似，需要轉換為相似度分數
                Double distance = (Double) metadata.get("distance");
                return 1.0 / (1.0 + distance);
            }

            if (metadata.containsKey("score")) {
                return (Double) metadata.get("score");
            }

            // 默認分數
            return 0.8;

        } catch (Exception e) {
            log.debug("無法提取相似度分數，使用默認值", e);
            return 0.8;
        }
    }

    /**
     * 批次檢索（用於評估測試）
     *
     * @param queries 查詢列表
     * @param options 查詢選項
     * @return 批次檢索結果
     */
    public List<List<ScoredDocument>> batchRetrieve(List<String> queries, RAGQueryOptions options) {
        long startTime = System.currentTimeMillis();

        log.info("開始批次檢索，查詢數量: {}", queries.size());

        List<List<ScoredDocument>> results = queries.stream()
                .map(query -> retrieve(query, options))
                .toList();

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("批次檢索完成，耗時: {}ms，平均: {}ms",
                processingTime, processingTime / queries.size());

        return results;
    }
}
