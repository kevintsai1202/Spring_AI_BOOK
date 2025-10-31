package com.example.advancedrag.advisor;

import com.example.advancedrag.model.RAGQueryOptions;
import com.example.advancedrag.model.ScoredDocument;
import com.example.advancedrag.service.RerankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Re-ranking Advisor
 *
 * Re-ranking 工具類，提供文檔 Re-ranking 功能
 * 注意：Spring AI 1.0.3 的 Advisor API 可能與最新版本不同，
 * 因此這裡實現為普通工具類而不是 Spring AI Advisor
 *
 * 使用示例：
 * <pre>
 * RerankingAdvisor advisor = new RerankingAdvisor(rerankingService, options);
 * List<ScoredDocument> rerankedDocs = advisor.rerank(query, documents);
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RerankingAdvisor {

    private final RerankingService rerankingService;

    /**
     * Re-rank 文檔列表
     *
     * @param query 查詢文本
     * @param documents 文檔列表
     * @param options 查詢選項
     * @return Re-ranking 後的文檔列表
     */
    public List<ScoredDocument> rerank(String query, List<Document> documents, RAGQueryOptions options) {
        try {
            log.info("RerankingAdvisor: 開始 Re-ranking，文檔數量: {}", documents.size());

            // 轉換為 ScoredDocument
            List<ScoredDocument> scoredDocs = convertToScoredDocuments(documents);

            // 執行 Re-ranking
            List<ScoredDocument> rerankedDocs = rerankingService.rerank(query, scoredDocs, options);

            log.info("RerankingAdvisor: Re-ranking 完成，最終文檔數: {}", rerankedDocs.size());

            return rerankedDocs;

        } catch (Exception e) {
            log.error("RerankingAdvisor: Re-ranking 失敗", e);
            // 失敗時返回原始文檔（轉換為 ScoredDocument）
            return convertToScoredDocuments(documents);
        }
    }

    /**
     * Re-rank 文檔列表（使用默認選項）
     *
     * @param query 查詢文本
     * @param documents 文檔列表
     * @return Re-ranking 後的文檔列表
     */
    public List<ScoredDocument> rerank(String query, List<Document> documents) {
        return rerank(query, documents, RAGQueryOptions.builder().build());
    }

    /**
     * 轉換為 ScoredDocument
     *
     * @param documents 文檔列表
     * @return ScoredDocument 列表
     */
    private List<ScoredDocument> convertToScoredDocuments(List<Document> documents) {
        List<ScoredDocument> scoredDocs = new ArrayList<>();

        for (Document doc : documents) {
            // 從元數據中獲取分數（如果有）
            Double score = extractScore(doc);

            ScoredDocument scoredDoc = ScoredDocument.builder()
                    .document(doc)
                    .score(score)
                    .semanticScore(score)
                    .build();

            scoredDocs.add(scoredDoc);
        }

        return scoredDocs;
    }

    /**
     * 從文檔元數據中提取分數
     *
     * @param doc 文檔
     * @return 分數
     */
    private Double extractScore(Document doc) {
        try {
            Map<String, Object> metadata = doc.getMetadata();

            // 嘗試從 distance 字段獲取
            if (metadata.containsKey("distance")) {
                Double distance = (Double) metadata.get("distance");
                return 1.0 / (1.0 + distance);
            }

            // 嘗試從 score 字段獲取
            if (metadata.containsKey("score")) {
                return (Double) metadata.get("score");
            }

            return 0.8; // 默認分數

        } catch (Exception e) {
            log.debug("提取分數失敗，使用默認值", e);
            return 0.8;
        }
    }

    /**
     * 轉換為 Document 列表
     *
     * @param scoredDocs ScoredDocument 列表
     * @return Document 列表
     */
    public List<Document> toDocuments(List<ScoredDocument> scoredDocs) {
        return scoredDocs.stream()
                .map(ScoredDocument::getDocument)
                .toList();
    }
}
