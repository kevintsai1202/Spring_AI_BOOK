package com.example.advancedrag.service;

import com.example.advancedrag.dto.AdvancedRAGRequest;
import com.example.advancedrag.dto.AdvancedRAGResponse;
import com.example.advancedrag.dto.ModerationResult;
import com.example.advancedrag.model.RAGQueryOptions;
import com.example.advancedrag.model.ScoredDocument;
import com.example.advancedrag.properties.RAGProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Advanced RAG 主服務
 *
 * 協調整個 RAG 流程：
 * 1. 查詢重寫/擴展
 * 2. 多階段檢索（粗檢索 + Re-ranking）
 * 3. 上下文優化
 * 4. LLM 生成答案
 * 5. 性能指標收集
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdvancedRAGService {

    private final ChatClient chatClient;
    private final QueryRewriteService queryRewriteService;
    private final MultiStageRetrievalService retrievalService;
    private final RerankingService rerankingService;
    private final ContextOptimizationService contextOptimizationService;
    private final ContentModerationService contentModerationService;
    private final CustomRuleModerationService customRuleModerationService;
    private final RAGMetricsService metricsService;
    private final RAGProperties ragProperties;

    /**
     * RAG 生成 Prompt 模板
     */
    private static final String RAG_PROMPT_TEMPLATE = """
            你是一個專業的問答助手。請基於提供的上下文信息回答用戶的問題。

            回答要求：
            1. 僅使用上下文中提供的信息回答問題
            2. 如果上下文中沒有相關信息，請明確說明"根據提供的信息無法回答該問題"
            3. 回答要準確、簡潔、有條理
            4. 適當引用上下文中的關鍵信息
            5. 使用繁體中文回答

            上下文信息：
            {context}

            用戶問題：{question}

            請提供你的答案：
            """;

    /**
     * 執行 Advanced RAG 查詢
     *
     * @param request RAG 查詢請求
     * @return RAG 查詢響應
     */
    public AdvancedRAGResponse query(AdvancedRAGRequest request) {
        long totalStartTime = System.currentTimeMillis();
        String queryId = UUID.randomUUID().toString();

        // 記錄查詢開始
        metricsService.recordQuery();

        try {
            log.info("開始 Advanced RAG 查詢，Query ID: {}, 查詢: [{}]",
                    queryId, request.getQuery());

            // 構建響應對象
            AdvancedRAGResponse.AdvancedRAGResponseBuilder responseBuilder = AdvancedRAGResponse.builder()
                    .originalQuery(request.getQuery())
                    .queryId(queryId)
                    .sessionId(request.getSessionId())
                    .timestamp(LocalDateTime.now());

            // 獲取查詢選項
            RAGQueryOptions options = request.getOptionsOrDefault();

            // === 階段 0：查詢內容審核（Pre-Moderation）===
            if (request.getEnableModeration() != null && request.getEnableModeration()) {
                long moderationStartTime = System.currentTimeMillis();
                ModerationResult queryModerationResult = performQueryModeration(request.getQuery());

                // 記錄審核指標
                metricsService.recordModeration();
                if (!queryModerationResult.getPassed()) {
                    metricsService.recordModerationFailed();
                }

                if (!queryModerationResult.getPassed()) {
                    // 查詢未通過審核，直接返回錯誤
                    log.warn("查詢未通過內容審核：{}", queryModerationResult.getReason());

                    // 記錄查詢失敗
                    long totalTime = System.currentTimeMillis() - totalStartTime;
                    metricsService.recordQueryFailure();
                    metricsService.recordQueryDuration(totalTime);

                    return AdvancedRAGResponse.builder()
                            .originalQuery(request.getQuery())
                            .queryId(queryId)
                            .sessionId(request.getSessionId())
                            .timestamp(LocalDateTime.now())
                            .answer("抱歉，您的查詢包含不當內容，無法處理。原因：" + queryModerationResult.getReason())
                            .processingTimeMs(totalTime)
                            .metadata(Map.of(
                                    "moderation_failed", true,
                                    "moderation_result", queryModerationResult
                            ))
                            .build();
                }

                // 暫時註釋 metadata，稍後統一處理
                // long moderationTime = System.currentTimeMillis() - moderationStartTime;
            }

            // === 階段 1：查詢預處理 ===
            long rewriteStartTime = System.currentTimeMillis();
            String processedQuery = preprocessQuery(request, responseBuilder);
            long rewriteTime = System.currentTimeMillis() - rewriteStartTime;

            // === 階段 2：多階段檢索 ===
            long retrievalStartTime = System.currentTimeMillis();
            List<ScoredDocument> retrievedDocs = performRetrieval(processedQuery, options, request);
            long retrievalTime = System.currentTimeMillis() - retrievalStartTime;
            responseBuilder.retrievalTimeMs(retrievalTime);

            // 記錄檢索指標
            metricsService.recordRetrievalDuration(retrievalTime);
            metricsService.recordDocumentsRetrieved(retrievedDocs.size());

            log.info("檢索完成，檢索到 {} 個文檔", retrievedDocs.size());

            // === 階段 2.5：Re-ranking 精確排序 ===
            long rerankingStartTime = System.currentTimeMillis();
            List<ScoredDocument> rerankedDocs = retrievedDocs;

            if (options.getEnableReranking() && retrievedDocs.size() > options.getFinalTopK()) {
                rerankedDocs = rerankingService.rerank(processedQuery, retrievedDocs, options);
                log.info("Re-ranking 完成，最終文檔數: {}", rerankedDocs.size());
            } else {
                log.info("跳過 Re-ranking（已關閉或文檔數量不足）");
                // 如果不進行 Re-ranking，直接限制數量
                rerankedDocs = retrievedDocs.stream()
                        .limit(options.getFinalTopK())
                        .toList();
            }

            long rerankingTime = System.currentTimeMillis() - rerankingStartTime;
            responseBuilder.rerankingTimeMs(rerankingTime);

            // 記錄 Re-ranking 指標
            metricsService.recordRerankingDuration(rerankingTime);

            // === 階段 3：上下文優化 ===
            String optimizedContext = contextOptimizationService.optimizeContext(
                    rerankedDocs,
                    options
            );

            // === 階段 4：LLM 生成答案 ===
            long generationStartTime = System.currentTimeMillis();
            String answer = generateAnswer(request.getQuery(), optimizedContext);
            long generationTime = System.currentTimeMillis() - generationStartTime;
            responseBuilder.generationTimeMs(generationTime);

            // 記錄生成指標
            metricsService.recordGenerationDuration(generationTime);

            // === 階段 4.5：答案內容審核（Post-Moderation）===
            if (request.getEnableModeration() != null && request.getEnableModeration()) {
                long answerModerationStartTime = System.currentTimeMillis();
                ModerationResult answerModerationResult = performAnswerModeration(answer);

                // 記錄答案審核指標
                metricsService.recordModeration();
                if (!answerModerationResult.getPassed()) {
                    metricsService.recordModerationFailed();
                }

                if (!answerModerationResult.getPassed()) {
                    // 答案未通過審核，替換為安全回覆
                    log.warn("生成的答案未通過內容審核：{}", answerModerationResult.getReason());
                    answer = "抱歉，生成的答案包含不當內容，已被過濾。請嘗試重新表述您的問題。";
                }
                // 暫時註釋 metadata，稍後統一處理
            }

            // === 組裝響應 ===
            responseBuilder.answer(answer);

            // 添加檢索到的文檔（如果請求）
            if (request.getReturnDocuments()) {
                List<AdvancedRAGResponse.RetrievedDocument> retrievedDocuments =
                        convertToResponseDocuments(rerankedDocs, request.getReturnScoringDetails());
                responseBuilder.documents(retrievedDocuments);
            }

            // 計算總耗時
            long totalTime = System.currentTimeMillis() - totalStartTime;
            responseBuilder.processingTimeMs(totalTime);

            // 構建響應
            AdvancedRAGResponse response = responseBuilder.build();

            // 添加元數據
            response.addMetadata("context_quality",
                    contextOptimizationService.calculateContextQuality(rerankedDocs));
            response.addMetadata("rewrite_time_ms", rewriteTime);
            response.addMetadata("original_doc_count", retrievedDocs.size());
            response.addMetadata("reranked_doc_count", rerankedDocs.size());

            log.info("Advanced RAG 查詢完成，Query ID: {}, 總耗時: {}ms", queryId, totalTime);

            // 記錄查詢成功和總耗時
            metricsService.recordQuerySuccess();
            metricsService.recordQueryDuration(totalTime);

            return response;

        } catch (Exception e) {
            log.error("Advanced RAG 查詢失敗，Query ID: {}", queryId, e);

            // 記錄查詢失敗
            long totalTime = System.currentTimeMillis() - totalStartTime;
            metricsService.recordQueryFailure();
            metricsService.recordQueryDuration(totalTime);

            throw new RuntimeException("RAG 查詢失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 查詢預處理
     *
     * @param request 請求
     * @param responseBuilder 響應構建器
     * @return 處理後的查詢
     */
    private String preprocessQuery(AdvancedRAGRequest request,
                                    AdvancedRAGResponse.AdvancedRAGResponseBuilder responseBuilder) {
        String originalQuery = request.getQuery();

        // 查詢重寫
        if (request.getEnableQueryRewrite()) {
            String rewrittenQuery = queryRewriteService.rewriteQuery(originalQuery);
            responseBuilder.rewrittenQuery(rewrittenQuery);
            log.info("查詢重寫：[{}] -> [{}]", originalQuery, rewrittenQuery);
            return rewrittenQuery;
        }

        // 查詢擴展
        if (request.getEnableQueryExpansion()) {
            List<String> expandedQueries = queryRewriteService.expandQuery(
                    originalQuery,
                    request.getOptionsOrDefault().getQueryExpansionCount()
            );
            responseBuilder.expandedQueries(expandedQueries);
            log.info("查詢擴展：生成 {} 個擴展查詢", expandedQueries.size());
        }

        return originalQuery;
    }

    /**
     * 執行檢索
     *
     * @param query 查詢
     * @param options 選項
     * @param request 原始請求
     * @return 檢索到的文檔
     */
    private List<ScoredDocument> performRetrieval(String query, RAGQueryOptions options,
                                                    AdvancedRAGRequest request) {
        // 如果啟用查詢擴展，使用混合檢索
        if (request.getEnableQueryExpansion()) {
            List<String> keywords = queryRewriteService.extractKeywords(query);
            return retrievalService.hybridRetrieval(query, keywords, options);
        }

        // 標準多階段檢索
        return retrievalService.retrieve(query, options);
    }

    /**
     * 生成答案
     *
     * @param question 用戶問題
     * @param context 上下文
     * @return 生成的答案
     */
    private String generateAnswer(String question, String context) {
        try {
            log.debug("開始生成答案，上下文長度: {}", context.length());

            PromptTemplate promptTemplate = new PromptTemplate(RAG_PROMPT_TEMPLATE);
            Prompt prompt = promptTemplate.create(Map.of(
                    "question", question,
                    "context", context
            ));

            String answer = chatClient.prompt(prompt)
                    .call()
                    .content();

            log.debug("答案生成完成，答案長度: {}", answer.length());

            return answer;

        } catch (Exception e) {
            log.error("答案生成失敗", e);
            return "抱歉，生成答案時發生錯誤：" + e.getMessage();
        }
    }

    /**
     * 轉換為響應文檔格式
     *
     * @param scoredDocuments 評分文檔列表
     * @param includeScoringDetails 是否包含評分細節
     * @return 響應文檔列表
     */
    private List<AdvancedRAGResponse.RetrievedDocument> convertToResponseDocuments(
            List<ScoredDocument> scoredDocuments, boolean includeScoringDetails) {

        List<AdvancedRAGResponse.RetrievedDocument> documents = new ArrayList<>();

        for (int i = 0; i < scoredDocuments.size(); i++) {
            ScoredDocument scoredDoc = scoredDocuments.get(i);
            Document doc = scoredDoc.getDocument();

            AdvancedRAGResponse.RetrievedDocument.RetrievedDocumentBuilder builder =
                    AdvancedRAGResponse.RetrievedDocument.builder()
                            .documentId(doc.getId())
                            .content(doc.getText())
                            .source(extractSource(doc))
                            .score(scoredDoc.getScore())
                            .rank(i + 1)
                            .metadata(doc.getMetadata());

            // 添加詳細評分信息
            if (includeScoringDetails) {
                builder.semanticScore(scoredDoc.getSemanticScore())
                        .bm25Score(scoredDoc.getBm25Score())
                        .qualityScore(scoredDoc.getQualityScore())
                        .freshnessScore(scoredDoc.getFreshnessScore());
            }

            documents.add(builder.build());
        }

        return documents;
    }

    /**
     * 從文檔元數據中提取來源
     *
     * @param document 文檔
     * @return 來源
     */
    private String extractSource(Document document) {
        Object source = document.getMetadata().get("source");
        if (source != null) {
            return source.toString();
        }

        Object fileName = document.getMetadata().get("file_name");
        if (fileName != null) {
            return fileName.toString();
        }

        return "Unknown";
    }

    /**
     * 執行查詢內容審核
     *
     * @param query 查詢文本
     * @return 審核結果
     */
    private ModerationResult performQueryModeration(String query) {
        try {
            log.debug("開始查詢內容審核");

            // 並行執行兩種審核
            ModerationResult openAIResult = contentModerationService.moderateContent(query);
            ModerationResult customRuleResult = customRuleModerationService.moderateContent(query);

            // 計算綜合結果（各占 50% 權重）
            double combinedScore = (openAIResult.getModerationScore() * 0.5) +
                    (customRuleResult.getModerationScore() * 0.5);

            // 只要有一個審核不通過，則綜合結果不通過
            boolean flagged = openAIResult.getFlagged() || customRuleResult.getFlagged();

            // 構建原因
            String reason = flagged ?
                    String.format("OpenAI: %s; 自定義規則: %s",
                            openAIResult.getReason(), customRuleResult.getReason()) :
                    "內容正常";

            return ModerationResult.builder()
                    .flagged(flagged)
                    .passed(!flagged)
                    .moderationScore(combinedScore)
                    .reason(reason)
                    .build();

        } catch (Exception e) {
            log.error("查詢內容審核失敗", e);
            // 審核失敗時返回通過（避免誤攔截）
            return ModerationResult.builder()
                    .flagged(false)
                    .passed(true)
                    .moderationScore(0.0)
                    .reason("審核服務不可用，默認通過")
                    .build();
        }
    }

    /**
     * 執行答案內容審核
     *
     * @param answer 生成的答案
     * @return 審核結果
     */
    private ModerationResult performAnswerModeration(String answer) {
        try {
            log.debug("開始答案內容審核");

            // 並行執行兩種審核
            ModerationResult openAIResult = contentModerationService.moderateContent(answer);
            ModerationResult customRuleResult = customRuleModerationService.moderateContent(answer);

            // 計算綜合結果（各占 50% 權重）
            double combinedScore = (openAIResult.getModerationScore() * 0.5) +
                    (customRuleResult.getModerationScore() * 0.5);

            // 只要有一個審核不通過，則綜合結果不通過
            boolean flagged = openAIResult.getFlagged() || customRuleResult.getFlagged();

            // 構建原因
            String reason = flagged ?
                    String.format("OpenAI: %s; 自定義規則: %s",
                            openAIResult.getReason(), customRuleResult.getReason()) :
                    "內容正常";

            return ModerationResult.builder()
                    .flagged(flagged)
                    .passed(!flagged)
                    .moderationScore(combinedScore)
                    .reason(reason)
                    .build();

        } catch (Exception e) {
            log.error("答案內容審核失敗", e);
            // 審核失敗時返回通過（避免誤攔截）
            return ModerationResult.builder()
                    .flagged(false)
                    .passed(true)
                    .moderationScore(0.0)
                    .reason("審核服務不可用，默認通過")
                    .build();
        }
    }
}
