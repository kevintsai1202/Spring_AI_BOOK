package com.example.advancedrag.reranking;

import com.example.advancedrag.properties.RAGProperties;
import com.example.advancedrag.service.BM25Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Re-ranking 提供者工廠
 *
 * 根據配置創建相應的 RerankingProvider 實例
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RerankingProviderFactory {

    private final RAGProperties ragProperties;
    private final RestClient restClient;
    private final BM25Service bm25Service;

    /**
     * 創建 RerankingProvider
     *
     * @return RerankingProvider 實例
     */
    public RerankingProvider createProvider() {
        String provider = ragProperties.getReranking().getProvider();
        log.info("創建 Re-ranking 提供者: {}", provider);

        return switch (provider.toLowerCase()) {
            case "voyage" -> createVoyageProvider();
            case "local" -> createLocalProvider();
            // 未來可擴展其他提供者
            // case "cohere" -> createCohereProvider();
            // case "jina" -> createJinaProvider();
            default -> {
                log.warn("未知的 Re-ranking 提供者: {}，使用本地算法", provider);
                yield createLocalProvider();
            }
        };
    }

    /**
     * 創建 Voyage AI 提供者
     *
     * @return VoyageRerankingProvider
     */
    private RerankingProvider createVoyageProvider() {
        String apiKey = ragProperties.getReranking().getApiKey();

        if (!StringUtils.hasText(apiKey)) {
            log.warn("Voyage AI API Key 未配置，降級使用本地算法");
            return createLocalProvider();
        }

        String model = ragProperties.getReranking().getModel();
        VoyageRerankingProvider provider = new VoyageRerankingProvider(restClient, apiKey, model);

        // 驗證 API 連接
        if (!provider.isAvailable()) {
            log.warn("Voyage AI 不可用，降級使用本地算法");
            return createLocalProvider();
        }

        log.info("Voyage AI Re-ranking 提供者創建成功，模型: {}", model);
        return provider;
    }

    /**
     * 創建本地算法提供者
     *
     * @return LocalRerankingProvider
     */
    private RerankingProvider createLocalProvider() {
        log.info("本地 Re-ranking 提供者創建成功");
        return new LocalRerankingProvider(bm25Service);
    }

    /**
     * 檢查提供者是否可用
     *
     * @param provider 提供者
     * @return 是否可用
     */
    public boolean isProviderAvailable(RerankingProvider provider) {
        try {
            return provider.isAvailable();
        } catch (Exception e) {
            log.error("檢查提供者可用性失敗: {}", provider.getProviderName(), e);
            return false;
        }
    }
}
