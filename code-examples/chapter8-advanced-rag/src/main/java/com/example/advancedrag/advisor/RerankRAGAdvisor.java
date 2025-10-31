package com.example.advancedrag.advisor;

import com.example.advancedrag.properties.RAGProperties;
import com.example.advancedrag.reranking.RerankResult;
import com.example.advancedrag.reranking.RerankingProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Re-ranking RAG Advisor
 *
 * 基於 Spring AI 1.0.3 的 BaseAdvisor 實現的 RAG 流程：
 * 1. 向量檢索（粗檢索，取 50-100 個）
 * 2. Re-ranking（精排，取 top 5-10 個）
 * 3. 組成上下文傳給 LLM
 *
 * 特色：
 * - 完全整合 Spring AI 架構
 * - 支援多種 Re-ranking 提供者（Voyage AI、本地算法等）
 * - 自動透過 ChatClient 調用
 * - 配置靈活
 */
@Slf4j
public class RerankRAGAdvisor implements BaseAdvisor {

    /**
     * 默認用戶文本建議（加入上下文的 Prompt）
     */
    private static final String DEFAULT_USER_TEXT_ADVISE = """
            Context information is below.
            ---------------------
            {question_answer_context}
            ---------------------
            Given the context and provided history information and not prior knowledge,
            reply to the user comment. If the answer is not in the context, inform
            the user that you can't answer the question.
            """;

    /**
     * 元數據鍵：檢索到的文檔
     */
    public static final String RETRIEVED_DOCUMENTS = "rag_retrieved_documents";

    /**
     * 元數據鍵：Re-ranking 結果
     */
    public static final String RERANKED_RESULTS = "rag_reranked_results";

    /**
     * 元數據鍵：過濾表達式
     */
    public static final String FILTER_EXPRESSION = "rag_filter_expression";

    private final VectorStore vectorStore;
    private final RerankingProvider rerankingProvider;
    private final RAGProperties ragProperties;
    private final String userTextAdvise;
    private final SearchRequest defaultSearchRequest;
    private int order = 0;

    /**
     * 構造函數
     *
     * @param vectorStore 向量存儲
     * @param rerankingProvider Re-ranking 提供者
     * @param ragProperties RAG 配置
     */
    public RerankRAGAdvisor(VectorStore vectorStore,
                            RerankingProvider rerankingProvider,
                            RAGProperties ragProperties) {
        this(vectorStore, rerankingProvider, ragProperties,
                SearchRequest.builder().build(), DEFAULT_USER_TEXT_ADVISE);
    }

    /**
     * 完整構造函數
     *
     * @param vectorStore 向量存儲
     * @param rerankingProvider Re-ranking 提供者
     * @param ragProperties RAG 配置
     * @param searchRequest 搜索請求配置
     * @param userTextAdvise 用戶文本建議
     */
    public RerankRAGAdvisor(VectorStore vectorStore,
                            RerankingProvider rerankingProvider,
                            RAGProperties ragProperties,
                            SearchRequest searchRequest,
                            String userTextAdvise) {
        Assert.notNull(vectorStore, "VectorStore 不能為 null");
        Assert.notNull(rerankingProvider, "RerankingProvider 不能為 null");
        Assert.notNull(ragProperties, "RAGProperties 不能為 null");
        Assert.notNull(searchRequest, "SearchRequest 不能為 null");
        Assert.hasText(userTextAdvise, "UserTextAdvise 不能為空");

        this.vectorStore = vectorStore;
        this.rerankingProvider = rerankingProvider;
        this.ragProperties = ragProperties;
        this.defaultSearchRequest = searchRequest;
        this.userTextAdvise = userTextAdvise;

        log.info("RerankRAGAdvisor 初始化完成，Re-ranking 提供者: {}",
                rerankingProvider.getProviderName());
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        // 獲取 adviseContext
        Map<String, Object> context = new HashMap<>();

        try {
            // 獲取用戶查詢文本
            String userQuery = request.prompt().getUserMessage().getText();
            log.info("開始 RAG 處理，查詢: {}", userQuery);

            // 第一階段：向量粗檢索
            int firstStageTopK = ragProperties.getReranking().getFirstStageTopK();
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(userQuery)
                    .topK(firstStageTopK)
                    .build();

            log.debug("執行粗檢索，topK: {}", firstStageTopK);
            List<Document> retrievedDocuments = vectorStore.similaritySearch(searchRequest);
            context.put(RETRIEVED_DOCUMENTS, retrievedDocuments);
            log.info("粗檢索完成，檢索到 {} 個文檔", retrievedDocuments.size());

            // 第二階段：Re-ranking 精排
            int finalTopK = ragProperties.getReranking().getFinalTopK();
            log.debug("執行 Re-ranking，提供者: {}, finalTopK: {}",
                    rerankingProvider.getProviderName(), finalTopK);

            List<RerankResult> rerankedResults;
            try {
                rerankedResults = rerankingProvider.rerank(userQuery, retrievedDocuments, finalTopK);
                context.put(RERANKED_RESULTS, rerankedResults);
                log.info("Re-ranking 完成，返回 {} 個文檔", rerankedResults.size());
            } catch (Exception e) {
                log.error("Re-ranking 失敗，使用原始檢索結果", e);
                // 降級處理：直接使用粗檢索結果
                rerankedResults = retrievedDocuments.stream()
                        .limit(finalTopK)
                        .map(doc -> RerankResult.builder()
                                .document(doc)
                                .relevanceScore(0.5)
                                .providerName("fallback")
                                .build())
                        .collect(Collectors.toList());
                context.put(RERANKED_RESULTS, rerankedResults);
            }

            // 組成上下文
            String documentContext = rerankedResults.stream()
                    .map(result -> result.getDocument().getText())
                    .collect(Collectors.joining(System.lineSeparator()));

            log.debug("上下文長度: {} 字符", documentContext.length());

            // 組成上下文提示
            String contextPrompt = PromptTemplate.builder()
                    .template(userTextAdvise)
                    .variables(Map.of("question_answer_context", documentContext))
                    .build()
                    .render();

            // 更新請求 - 增強用戶消息並添加上下文
            return request.mutate()
                    .prompt(request.prompt().augmentUserMessage(contextPrompt))
                    .build();

        } catch (Exception e) {
            log.error("RAG 處理失敗，返回原始請求", e);
            return request;
        }
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        // 響應後處理：暫時直接返回原始響應
        // 元數據已經在 before 階段添加到請求上下文中
        return response;
    }

    @Override
    public String getName() {
        return "RerankRAGAdvisor";
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * 設置 Advisor 執行順序
     *
     * @param order 順序值（越小越先執行）
     * @return this
     */
    public RerankRAGAdvisor withOrder(int order) {
        this.order = order;
        return this;
    }

}
