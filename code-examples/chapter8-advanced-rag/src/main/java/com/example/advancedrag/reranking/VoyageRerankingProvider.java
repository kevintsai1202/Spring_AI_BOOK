package com.example.advancedrag.reranking;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Voyage AI Re-ranking 提供者
 *
 * 使用 Voyage AI 的 rerank-1 模型進行文檔重排
 * 優點：
 * - 支援多語言（包括中文）
 * - 精確度高
 * - API 穩定
 *
 * API 文檔: https://docs.voyageai.com/docs/reranker
 */
@Slf4j
public class VoyageRerankingProvider implements RerankingProvider {

    private static final String VOYAGE_API_URL = "https://api.voyageai.com/v1/rerank";
    private static final String DEFAULT_MODEL = "rerank-1";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    /**
     * Voyage AI Re-ranking API 響應
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VoyageRerankResponse(
            @JsonProperty("object") String object,
            @JsonProperty("data") List<VoyageRerankItem> data,
            @JsonProperty("model") String model,
            @JsonProperty("usage") VoyageUsage usage
    ) {}

    /**
     * Re-ranking 項目
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VoyageRerankItem(
            @JsonProperty("index") Integer index,
            @JsonProperty("relevance_score") Double relevanceScore,
            @JsonProperty("document") String document
    ) {}

    /**
     * API 使用統計
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VoyageUsage(
            @JsonProperty("total_tokens") Integer totalTokens
    ) {}

    public VoyageRerankingProvider(RestClient restClient, String apiKey) {
        this(restClient, apiKey, DEFAULT_MODEL);
    }

    public VoyageRerankingProvider(RestClient restClient, String apiKey, String model) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.model = model != null ? model : DEFAULT_MODEL;
    }

    @Override
    public List<RerankResult> rerank(String query, List<Document> documents, int topK) {
        if (documents == null || documents.isEmpty()) {
            log.warn("文檔列表為空，返回空結果");
            return Collections.emptyList();
        }

        try {
            log.info("開始使用 Voyage AI 進行 Re-ranking，文檔數: {}, topK: {}", documents.size(), topK);

            // 準備 API 請求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("model", model);
            requestBody.put("top_k", Math.min(topK, documents.size()));
            requestBody.put("return_documents", true);
            requestBody.put("documents", documents.stream()
                    .map(Document::getText)
                    .collect(Collectors.toList()));

            // 調用 API
            ResponseEntity<VoyageRerankResponse> response = restClient.post()
                    .uri(VOYAGE_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<VoyageRerankResponse>() {});

            // 解析結果
            if (response.getBody() == null || response.getBody().data() == null) {
                log.error("Voyage AI API 返回空結果");
                return Collections.emptyList();
            }

            VoyageRerankResponse body = response.getBody();
            List<RerankResult> results = new ArrayList<>();

            for (int i = 0; i < body.data().size(); i++) {
                VoyageRerankItem item = body.data().get(i);
                Document originalDoc = documents.get(item.index());

                RerankResult result = RerankResult.builder()
                        .document(originalDoc)
                        .originalIndex(item.index())
                        .newIndex(i)
                        .relevanceScore(item.relevanceScore())
                        .providerName(getProviderName())
                        .content(originalDoc.getText())
                        .build();

                results.add(result);
            }

            log.info("Voyage AI Re-ranking 完成，返回 {} 個結果", results.size());
            if (body.usage() != null) {
                log.debug("Token 使用: {}", body.usage().totalTokens());
            }

            return results;

        } catch (Exception e) {
            log.error("Voyage AI Re-ranking 失敗", e);
            throw new RuntimeException("Voyage AI Re-ranking 失敗: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "voyage-ai";
    }

    @Override
    public boolean isAvailable() {
        return StringUtils.hasText(apiKey);
    }

    /**
     * 測試 API 連接
     *
     * @return 是否連接成功
     */
    public boolean testConnection() {
        try {
            // 簡單測試請求
            List<Document> testDocs = List.of(
                    Document.builder().text("測試文檔 1").build()
            );
            rerank("測試查詢", testDocs, 1);
            return true;
        } catch (Exception e) {
            log.error("Voyage AI 連接測試失敗", e);
            return false;
        }
    }
}
