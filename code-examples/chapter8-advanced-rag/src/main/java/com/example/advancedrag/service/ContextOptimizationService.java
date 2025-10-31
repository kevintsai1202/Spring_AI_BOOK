package com.example.advancedrag.service;

import com.example.advancedrag.model.RAGQueryOptions;
import com.example.advancedrag.model.ScoredDocument;
import com.example.advancedrag.properties.RAGProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 上下文優化服務
 *
 * 功能：
 * 1. 上下文長度控制（避免超出 LLM token 限制）
 * 2. 文檔去重（移除相似或重複的文檔）
 * 3. 上下文排序（按相關性排序）
 * 4. 上下文格式化（為 LLM 生成優化的上下文）
 * 5. 動態上下文截斷（智能截斷過長內容）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContextOptimizationService {

    private final RAGProperties ragProperties;

    /**
     * 優化上下文
     *
     * @param documents 評分文檔列表
     * @param options 查詢選項
     * @return 優化後的上下文字符串
     */
    public String optimizeContext(List<ScoredDocument> documents, RAGQueryOptions options) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("開始優化上下文，文檔數量: {}", documents.size());

            // 1. 按分數排序
            List<ScoredDocument> sortedDocs = documents.stream()
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .toList();

            // 2. 去重（移除內容相似的文檔）
            List<ScoredDocument> dedupedDocs = deduplicateDocuments(sortedDocs);
            log.debug("去重後文檔數量: {}", dedupedDocs.size());

            // 3. 限制數量
            List<ScoredDocument> limitedDocs = dedupedDocs.stream()
                    .limit(options.getFinalTopK())
                    .toList();

            // 4. 控制總長度
            List<ScoredDocument> truncatedDocs = truncateToMaxLength(
                    limitedDocs,
                    options.getMaxContextLength()
            );

            // 5. 格式化上下文
            String optimizedContext = formatContext(truncatedDocs);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("上下文優化完成，耗時: {}ms，最終文檔數: {}，總長度: {}",
                    processingTime, truncatedDocs.size(), optimizedContext.length());

            return optimizedContext;

        } catch (Exception e) {
            log.error("上下文優化失敗", e);
            // 降級處理：使用簡單格式化
            return formatContextSimple(documents);
        }
    }

    /**
     * 去重文檔（基於內容相似度）
     *
     * @param documents 文檔列表
     * @return 去重後的文檔列表
     */
    private List<ScoredDocument> deduplicateDocuments(List<ScoredDocument> documents) {
        List<ScoredDocument> dedupedDocs = new ArrayList<>();

        for (ScoredDocument doc : documents) {
            boolean isDuplicate = false;

            // 檢查是否與已有文檔重複
            for (ScoredDocument existingDoc : dedupedDocs) {
                if (isSimilarContent(doc.getDocument(), existingDoc.getDocument())) {
                    isDuplicate = true;
                    log.debug("發現重複文檔，已跳過");
                    break;
                }
            }

            if (!isDuplicate) {
                dedupedDocs.add(doc);
            }
        }

        return dedupedDocs;
    }

    /**
     * 判斷兩個文檔內容是否相似
     *
     * @param doc1 文檔1
     * @param doc2 文檔2
     * @return 是否相似
     */
    private boolean isSimilarContent(Document doc1, Document doc2) {
        String content1 = doc1.getText();
        String content2 = doc2.getText();

        // 簡化判斷：內容完全相同或高度重疊
        if (content1.equals(content2)) {
            return true;
        }

        // 計算內容重疊度
        int minLength = Math.min(content1.length(), content2.length());
        int maxLength = Math.max(content1.length(), content2.length());

        // 如果長度差異過大，不視為重複
        if ((double) minLength / maxLength < 0.8) {
            return false;
        }

        // 計算開頭部分的相似度
        int compareLength = Math.min(200, minLength);
        String prefix1 = content1.substring(0, compareLength);
        String prefix2 = content2.substring(0, compareLength);

        return prefix1.equals(prefix2);
    }

    /**
     * 截斷到最大長度
     *
     * @param documents 文檔列表
     * @param maxLength 最大長度
     * @return 截斷後的文檔列表
     */
    private List<ScoredDocument> truncateToMaxLength(List<ScoredDocument> documents, int maxLength) {
        List<ScoredDocument> truncatedDocs = new ArrayList<>();
        int currentLength = 0;

        for (ScoredDocument doc : documents) {
            String content = doc.getDocument().getText();
            int contentLength = content.length();

            if (currentLength + contentLength <= maxLength) {
                // 完整添加
                truncatedDocs.add(doc);
                currentLength += contentLength;
            } else {
                // 部分添加（截斷）
                int remainingLength = maxLength - currentLength;
                if (remainingLength > 100) { // 至少保留 100 字符才有意義
                    String truncatedContent = content.substring(0, remainingLength) + "...";

                    // 創建截斷後的文檔
                    Document truncatedDoc = new Document(
                            doc.getDocument().getId(),
                            truncatedContent,
                            doc.getDocument().getMetadata()
                    );

                    ScoredDocument truncatedScoredDoc = ScoredDocument.builder()
                            .document(truncatedDoc)
                            .score(doc.getScore())
                            .semanticScore(doc.getSemanticScore())
                            .bm25Score(doc.getBm25Score())
                            .qualityScore(doc.getQualityScore())
                            .freshnessScore(doc.getFreshnessScore())
                            .build();

                    truncatedDocs.add(truncatedScoredDoc);
                }
                break;
            }
        }

        return truncatedDocs;
    }

    /**
     * 格式化上下文（生成結構化的上下文字符串）
     *
     * @param documents 文檔列表
     * @return 格式化的上下文
     */
    private String formatContext(List<ScoredDocument> documents) {
        if (documents.isEmpty()) {
            return "沒有找到相關文檔。";
        }

        StringBuilder context = new StringBuilder();
        context.append("以下是檢索到的相關文檔：\n\n");

        for (int i = 0; i < documents.size(); i++) {
            ScoredDocument doc = documents.get(i);
            Document document = doc.getDocument();

            context.append(String.format("【文檔 %d】", i + 1));

            // 添加來源信息（如果有）
            Object source = document.getMetadata().get("source");
            if (source != null) {
                context.append(String.format("（來源：%s）", source));
            }

            context.append("\n");
            context.append(document.getText());
            context.append("\n");

            // 添加分數信息（用於調試）
            if (log.isDebugEnabled()) {
                context.append(String.format("（相關性分數：%.3f）\n", doc.getScore()));
            }

            context.append("\n");
        }

        return context.toString();
    }

    /**
     * 簡單格式化上下文（降級方案）
     *
     * @param documents 文檔列表
     * @return 格式化的上下文
     */
    private String formatContextSimple(List<ScoredDocument> documents) {
        if (documents.isEmpty()) {
            return "沒有找到相關文檔。";
        }

        return documents.stream()
                .limit(5)
                .map(doc -> doc.getDocument().getText())
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * 獲取上下文摘要
     *
     * @param documents 文檔列表
     * @return 上下文摘要信息
     */
    public ContextSummary getContextSummary(List<ScoredDocument> documents) {
        int totalDocs = documents.size();
        int totalLength = documents.stream()
                .mapToInt(doc -> doc.getDocument().getText().length())
                .sum();

        double avgScore = documents.stream()
                .mapToDouble(ScoredDocument::getScore)
                .average()
                .orElse(0.0);

        return new ContextSummary(totalDocs, totalLength, avgScore);
    }

    /**
     * 上下文摘要信息
     */
    public record ContextSummary(
            int documentCount,
            int totalLength,
            double averageScore
    ) {
    }

    /**
     * 優化單個文檔內容（移除冗餘信息）
     *
     * @param content 原始內容
     * @return 優化後的內容
     */
    public String optimizeDocumentContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String optimized = content;

        // 1. 移除多餘的空白行
        optimized = optimized.replaceAll("\n{3,}", "\n\n");

        // 2. 移除首尾空白
        optimized = optimized.trim();

        // 3. 標準化空格
        optimized = optimized.replaceAll("[ \t]+", " ");

        return optimized;
    }

    /**
     * 計算上下文質量分數
     *
     * @param documents 文檔列表
     * @return 質量分數（0-1）
     */
    public double calculateContextQuality(List<ScoredDocument> documents) {
        if (documents.isEmpty()) {
            return 0.0;
        }

        // 因素1：文檔相關性（40%）
        double avgRelevance = documents.stream()
                .mapToDouble(ScoredDocument::getScore)
                .average()
                .orElse(0.0);

        // 因素2：文檔數量適中性（30%）
        int docCount = documents.size();
        double countScore = docCount >= 3 && docCount <= 5 ? 1.0 : 0.7;

        // 因素3：內容長度適中性（30%）
        int totalLength = documents.stream()
                .mapToInt(doc -> doc.getDocument().getText().length())
                .sum();
        double lengthScore = totalLength >= 500 && totalLength <= 3000 ? 1.0 : 0.7;

        // 綜合評分
        return avgRelevance * 0.4 + countScore * 0.3 + lengthScore * 0.3;
    }
}
