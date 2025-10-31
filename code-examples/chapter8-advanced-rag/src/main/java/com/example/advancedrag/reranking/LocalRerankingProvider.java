package com.example.advancedrag.reranking;

import com.example.advancedrag.service.BM25Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 本地 Re-ranking 提供者
 *
 * 使用本地多因子算法進行文檔重排：
 * - 語義相似度（基於原始檢索分數）
 * - BM25 分數（關鍵詞匹配）
 * - 文檔品質
 * - 新鮮度
 *
 * 優點：
 * - 不需要外部 API
 * - 無網路依賴
 * - 無 API 費用
 * - 適合開發/測試環境
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalRerankingProvider implements RerankingProvider {

    private final BM25Service bm25Service;

    // 權重配置
    private static final double SEMANTIC_WEIGHT = 0.4;
    private static final double BM25_WEIGHT = 0.3;
    private static final double QUALITY_WEIGHT = 0.2;
    private static final double FRESHNESS_WEIGHT = 0.1;

    @Override
    public List<RerankResult> rerank(String query, List<Document> documents, int topK) {
        if (documents == null || documents.isEmpty()) {
            log.warn("文檔列表為空，返回空結果");
            return Collections.emptyList();
        }

        try {
            log.info("開始使用本地算法進行 Re-ranking，文檔數: {}, topK: {}", documents.size(), topK);

            // 提取查詢關鍵詞
            List<String> queryKeywords = extractKeywords(query);

            // 計算每個文檔的綜合分數
            List<ScoredDocument> scoredDocs = new ArrayList<>();
            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);

                // 語義分數（從元數據中獲取或使用默認值）
                double semanticScore = getSemanticScore(doc);

                // BM25 分數
                double bm25Score = bm25Service.calculateBM25Score(doc.getText(), query, queryKeywords);

                // 品質分數
                double qualityScore = calculateQualityScore(doc);

                // 新鮮度分數
                double freshnessScore = calculateFreshnessScore(doc);

                // 綜合分數
                double finalScore = (semanticScore * SEMANTIC_WEIGHT) +
                        (bm25Score * BM25_WEIGHT) +
                        (qualityScore * QUALITY_WEIGHT) +
                        (freshnessScore * FRESHNESS_WEIGHT);

                scoredDocs.add(new ScoredDocument(doc, i, finalScore,
                        semanticScore, bm25Score, qualityScore, freshnessScore));

                log.debug("文檔 {} 評分: 語義={:.3f}, BM25={:.3f}, 品質={:.3f}, 新鮮度={:.3f}, 最終={:.3f}",
                        i, semanticScore, bm25Score, qualityScore, freshnessScore, finalScore);
            }

            // 按綜合分數排序
            scoredDocs.sort((a, b) -> Double.compare(b.finalScore, a.finalScore));

            // 轉換為 RerankResult 並取 topK
            List<RerankResult> results = scoredDocs.stream()
                    .limit(topK)
                    .map(scored -> RerankResult.builder()
                            .document(scored.document)
                            .originalIndex(scored.originalIndex)
                            .newIndex(scoredDocs.indexOf(scored))
                            .relevanceScore(scored.finalScore)
                            .providerName(getProviderName())
                            .content(scored.document.getText())
                            .build())
                    .collect(Collectors.toList());

            log.info("本地 Re-ranking 完成，返回 {} 個結果", results.size());

            return results;

        } catch (Exception e) {
            log.error("本地 Re-ranking 失敗", e);
            throw new RuntimeException("本地 Re-ranking 失敗: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "local";
    }

    @Override
    public boolean isAvailable() {
        return true; // 本地算法總是可用
    }

    /**
     * 獲取語義相似度分數
     *
     * @param doc 文檔
     * @return 語義分數（0-1）
     */
    private double getSemanticScore(Document doc) {
        // 嘗試從元數據中獲取原始檢索分數
        Map<String, Object> metadata = doc.getMetadata();
        if (metadata.containsKey("distance")) {
            // 如果有 distance，轉換為相似度分數（distance 越小越相似）
            Object distanceObj = metadata.get("distance");
            if (distanceObj instanceof Number) {
                double distance = ((Number) distanceObj).doubleValue();
                return Math.max(0, 1.0 - distance);
            }
        }
        if (metadata.containsKey("score")) {
            Object scoreObj = metadata.get("score");
            if (scoreObj instanceof Number) {
                return ((Number) scoreObj).doubleValue();
            }
        }
        // 默認返回中等分數
        return 0.7;
    }

    /**
     * 計算文檔品質分數
     *
     * @param doc 文檔
     * @return 品質分數（0-1）
     */
    private double calculateQualityScore(Document doc) {
        double score = 0.5; // 基礎分數

        String content = doc.getText();
        Map<String, Object> metadata = doc.getMetadata();

        // 因素1：內容長度（適中為好）
        int length = content.length();
        if (length >= 200 && length <= 2000) {
            score += 0.2;
        } else if (length > 2000 && length <= 5000) {
            score += 0.1;
        }

        // 因素2：內容結構性（標點符號）
        long punctuationCount = content.chars()
                .filter(c -> ",.!?;:，。！？；：".indexOf(c) >= 0)
                .count();
        if (punctuationCount > 5) {
            score += 0.1;
        }

        // 因素3：元數據豐富度
        if (metadata.containsKey("title") && metadata.get("title") != null) {
            score += 0.1;
        }
        if (metadata.containsKey("author") && metadata.get("author") != null) {
            score += 0.05;
        }
        if (metadata.containsKey("source") && metadata.get("source") != null) {
            score += 0.05;
        }

        return Math.min(1.0, score);
    }

    /**
     * 計算新鮮度分數
     *
     * @param doc 文檔
     * @return 新鮮度分數（0-1）
     */
    private double calculateFreshnessScore(Document doc) {
        Map<String, Object> metadata = doc.getMetadata();

        // 嘗試獲取時間戳
        Object timestampObj = metadata.getOrDefault("updated_at",
                metadata.getOrDefault("created_at",
                        metadata.get("timestamp")));

        if (timestampObj == null) {
            return 0.7; // 無時間信息返回中等分數
        }

        LocalDateTime documentTime = parseTimestamp(timestampObj);
        if (documentTime == null) {
            return 0.7;
        }

        // 計算時間差（天數）
        LocalDateTime now = LocalDateTime.now();
        long daysDiff = ChronoUnit.DAYS.between(documentTime, now);

        // 新鮮度評分規則
        if (daysDiff <= 7) return 1.0;        // 一週內
        if (daysDiff <= 30) return 0.9;       // 一個月內
        if (daysDiff <= 90) return 0.8;       // 三個月內
        if (daysDiff <= 180) return 0.7;      // 半年內
        if (daysDiff <= 365) return 0.6;      // 一年內
        return 0.5;                            // 超過一年
    }

    /**
     * 解析時間戳
     *
     * @param timestampObj 時間戳對象
     * @return LocalDateTime
     */
    private LocalDateTime parseTimestamp(Object timestampObj) {
        try {
            if (timestampObj instanceof LocalDateTime) {
                return (LocalDateTime) timestampObj;
            } else if (timestampObj instanceof String) {
                return LocalDateTime.parse((String) timestampObj);
            } else if (timestampObj instanceof Long) {
                long timestamp = (Long) timestampObj;
                return LocalDateTime.ofEpochSecond(timestamp, 0,
                        java.time.ZoneOffset.systemDefault().getRules()
                                .getOffset(java.time.Instant.ofEpochSecond(timestamp)));
            }
        } catch (Exception e) {
            log.debug("時間戳解析失敗: {}", timestampObj, e);
        }
        return null;
    }

    /**
     * 提取查詢關鍵詞（簡化版）
     *
     * @param query 查詢文本
     * @return 關鍵詞列表
     */
    private List<String> extractKeywords(String query) {
        // 簡單分詞
        String[] tokens = query.split("\\s+");
        return Arrays.stream(tokens)
                .filter(token -> token.length() > 1)
                .filter(token -> !isStopWord(token))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 判斷是否為停用詞
     *
     * @param word 詞語
     * @return 是否為停用詞
     */
    private boolean isStopWord(String word) {
        String lowerWord = word.toLowerCase();
        List<String> stopWords = List.of("的", "了", "是", "在", "我", "有", "和", "就",
                "不", "人", "都", "一", "個", "上", "也", "為", "能", "對", "會");
        return stopWords.contains(lowerWord);
    }

    /**
     * 內部評分文檔類
     */
    private record ScoredDocument(
            Document document,
            int originalIndex,
            double finalScore,
            double semanticScore,
            double bm25Score,
            double qualityScore,
            double freshnessScore
    ) {}
}
