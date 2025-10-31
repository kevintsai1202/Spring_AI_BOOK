package com.example.advancedrag.service;

import com.example.advancedrag.model.RAGQueryOptions;
import com.example.advancedrag.model.RerankingCandidate;
import com.example.advancedrag.model.ScoredDocument;
import com.example.advancedrag.properties.RAGProperties;
import com.example.advancedrag.util.TextUtil;
import com.example.advancedrag.util.VectorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Re-ranking 服務
 *
 * 實現多因子評分算法對檢索結果進行精確排序：
 * - 語義相似度（40%）：基於向量相似度
 * - BM25 分數（30%）：基於關鍵詞匹配
 * - 文檔品質（20%）：基於文檔元數據和內容品質
 * - 新鮮度（10%）：基於文檔時間戳
 *
 * @deprecated 此服務已過時，建議使用 Spring AI 1.0.3 的 Advisor 模式進行 Re-ranking。
 *             新的實現方式：
 *             <ul>
 *               <li>使用 {@link com.example.advancedrag.advisor.RerankRAGAdvisor} 自動整合到 ChatClient 流程</li>
 *               <li>支援多種 Re-ranking 提供者（Voyage AI、本地算法等）透過 {@link com.example.advancedrag.reranking.RerankingProvider}</li>
 *               <li>配置更靈活，透過 application.yml 的 app.rag.reranking 區塊</li>
 *               <li>無需手動調用，Advisor 會自動在查詢時執行</li>
 *             </ul>
 *             <p>
 *             此類保留僅供向後兼容，未來版本將移除。
 *             </p>
 * @see com.example.advancedrag.advisor.RerankRAGAdvisor
 * @see com.example.advancedrag.reranking.RerankingProvider
 * @see com.example.advancedrag.reranking.VoyageRerankingProvider
 * @see com.example.advancedrag.reranking.LocalRerankingProvider
 */
@Deprecated(since = "1.0.0", forRemoval = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class RerankingService {

    private final RAGProperties ragProperties;
    private final SmartEmbeddingService embeddingService;

    /**
     * Re-rank 候選文檔
     *
     * @param query 查詢文本
     * @param candidates 候選文檔列表
     * @param options 查詢選項
     * @return Re-ranking 後的文檔列表
     */
    public List<ScoredDocument> rerank(String query, List<ScoredDocument> candidates, RAGQueryOptions options) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("開始 Re-ranking，候選文檔數量: {}", candidates.size());

            // 1. 轉換為 RerankingCandidate
            List<RerankingCandidate> rerankingCandidates = convertToRerankingCandidates(candidates);

            // 2. 提取查詢關鍵詞
            List<String> queryKeywords = extractKeywords(query);

            // 3. 計算多維度分數
            for (RerankingCandidate candidate : rerankingCandidates) {
                calculateMultiFactorScore(candidate, query, queryKeywords);
            }

            // 4. 按最終分數排序
            rerankingCandidates.sort((a, b) ->
                    Double.compare(b.getFinalScore(), a.getFinalScore())
            );

            // 5. 分配排名
            for (int i = 0; i < rerankingCandidates.size(); i++) {
                rerankingCandidates.get(i).setRank(i + 1);
            }

            // 6. 限制返回數量
            List<RerankingCandidate> topCandidates = rerankingCandidates.stream()
                    .limit(options.getFinalTopK())
                    .toList();

            // 7. 轉換回 ScoredDocument
            List<ScoredDocument> rerankedDocs = convertToScoredDocuments(topCandidates);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Re-ranking 完成，耗時: {}ms，最終返回: {} 個文檔",
                    processingTime, rerankedDocs.size());

            return rerankedDocs;

        } catch (Exception e) {
            log.error("Re-ranking 失敗，返回原始排序", e);
            return candidates.stream()
                    .limit(options.getFinalTopK())
                    .toList();
        }
    }

    /**
     * 計算多因子分數
     *
     * @param candidate 候選文檔
     * @param query 查詢文本
     * @param queryKeywords 查詢關鍵詞
     */
    private void calculateMultiFactorScore(RerankingCandidate candidate, String query,
                                           List<String> queryKeywords) {
        Document doc = candidate.getDocument();

        // 1. 語義相似度分數（40%）
        double semanticScore = candidate.getSemanticScore() != null ?
                candidate.getSemanticScore() : candidate.getOriginalScore();
        candidate.setSemanticScore(semanticScore);

        // 2. BM25 分數（30%）
        double bm25Score = calculateBM25Score(doc.getText(), query, queryKeywords);
        candidate.setBm25Score(bm25Score);

        // 3. 文檔品質分數（20%）
        double qualityScore = calculateQualityScore(doc);
        candidate.setQualityScore(qualityScore);

        // 4. 新鮮度分數（10%）
        double freshnessScore = calculateFreshnessScore(doc);
        candidate.setFreshnessScore(freshnessScore);

        // 5. 計算最終綜合分數
        candidate.calculateFinalScore();

        // 6. 記錄評分細節
        candidate.addScoringDetail("query_length", query.length());
        candidate.addScoringDetail("doc_length", doc.getText().length());
        candidate.addScoringDetail("keyword_count", queryKeywords.size());

        log.debug("文檔 {} 評分: 語義={:.3f}, BM25={:.3f}, 品質={:.3f}, 新鮮度={:.3f}, 最終={:.3f}",
                doc.getId(), semanticScore, bm25Score, qualityScore, freshnessScore,
                candidate.getFinalScore());
    }

    /**
     * 計算 BM25 分數（關鍵詞匹配）
     *
     * @param content 文檔內容
     * @param query 查詢文本
     * @param keywords 關鍵詞列表
     * @return BM25 分數（0-1）
     */
    private double calculateBM25Score(String content, String query, List<String> keywords) {
        if (keywords.isEmpty()) {
            return 0.5; // 無關鍵詞時返回中等分數
        }

        String lowerContent = content.toLowerCase();
        String lowerQuery = query.toLowerCase();

        // 簡化版 BM25：基於關鍵詞匹配和詞頻
        double score = 0.0;
        int totalMatches = 0;

        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            int termFreq = TextUtil.termFrequency(lowerContent, lowerKeyword);

            if (termFreq > 0) {
                totalMatches++;

                // TF 部分（詞頻）
                double tf = (double) termFreq / (termFreq + 1.0); // 飽和函數
                score += tf;
            }
        }

        // 正規化分數
        if (!keywords.isEmpty()) {
            score = score / keywords.size();
        }

        // 額外加分：查詢完整匹配
        if (lowerContent.contains(lowerQuery)) {
            score = Math.min(1.0, score + 0.2);
        }

        return Math.min(1.0, score);
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

        // 因素2：內容結構性（檢查是否有標點、段落等）
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

        // 嘗試從元數據中獲取時間戳
        Object createdAtObj = metadata.get("created_at");
        Object updatedAtObj = metadata.get("updated_at");
        Object timestampObj = metadata.get("timestamp");

        LocalDateTime documentTime = null;

        // 解析時間戳
        if (updatedAtObj != null) {
            documentTime = parseTimestamp(updatedAtObj);
        } else if (createdAtObj != null) {
            documentTime = parseTimestamp(createdAtObj);
        } else if (timestampObj != null) {
            documentTime = parseTimestamp(timestampObj);
        }

        // 沒有時間信息，返回中等分數
        if (documentTime == null) {
            return 0.7;
        }

        // 計算時間差（天數）
        LocalDateTime now = LocalDateTime.now();
        long daysDiff = ChronoUnit.DAYS.between(documentTime, now);

        // 新鮮度評分規則
        if (daysDiff <= 7) {
            return 1.0; // 一週內：最高分
        } else if (daysDiff <= 30) {
            return 0.9; // 一個月內：高分
        } else if (daysDiff <= 90) {
            return 0.8; // 三個月內：較高分
        } else if (daysDiff <= 180) {
            return 0.7; // 半年內：中等分
        } else if (daysDiff <= 365) {
            return 0.6; // 一年內：較低分
        } else {
            return 0.5; // 超過一年：基礎分
        }
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
                // Unix 時間戳（秒）
                long timestamp = (Long) timestampObj;
                return LocalDateTime.ofEpochSecond(timestamp, 0,
                        java.time.ZoneOffset.systemDefault().getRules().getOffset(
                                java.time.Instant.ofEpochSecond(timestamp)));
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
        // 使用 TextUtil 分詞
        List<String> tokens = TextUtil.tokenize(query);

        // 過濾停用詞和短詞
        return tokens.stream()
                .filter(token -> token.length() > 1)
                .filter(token -> !isStopWord(token))
                .distinct()
                .toList();
    }

    /**
     * 判斷是否為停用詞（簡化版）
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
     * 轉換為 RerankingCandidate
     *
     * @param scoredDocs 評分文檔列表
     * @return RerankingCandidate 列表
     */
    private List<RerankingCandidate> convertToRerankingCandidates(List<ScoredDocument> scoredDocs) {
        List<RerankingCandidate> candidates = new ArrayList<>();
        for (ScoredDocument doc : scoredDocs) {
            candidates.add(RerankingCandidate.fromScoredDocument(doc));
        }
        return candidates;
    }

    /**
     * 轉換為 ScoredDocument
     *
     * @param candidates RerankingCandidate 列表
     * @return ScoredDocument 列表
     */
    private List<ScoredDocument> convertToScoredDocuments(List<RerankingCandidate> candidates) {
        return candidates.stream()
                .map(candidate -> ScoredDocument.builder()
                        .document(candidate.getDocument())
                        .score(candidate.getFinalScore())
                        .semanticScore(candidate.getSemanticScore())
                        .bm25Score(candidate.getBm25Score())
                        .qualityScore(candidate.getQualityScore())
                        .freshnessScore(candidate.getFreshnessScore())
                        .build())
                .toList();
    }
}
