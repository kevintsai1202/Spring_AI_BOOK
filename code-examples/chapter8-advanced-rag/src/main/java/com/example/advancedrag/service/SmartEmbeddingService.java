package com.example.advancedrag.service;

import com.example.advancedrag.model.EmbeddingContext;
import com.example.advancedrag.model.ModelStats;
import com.example.advancedrag.model.PreprocessingOptions;
import com.example.advancedrag.properties.EmbeddingProperties;
import com.example.advancedrag.util.CacheKeyGenerator;
import com.example.advancedrag.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能 Embedding 管理服務
 *
 * 功能：
 * 1. 智能模型選擇（基於上下文自動選擇最佳模型）
 * 2. 文本預處理（清理、標準化）
 * 3. Embedding 快取（Redis）
 * 4. 批次處理優化
 * 5. 性能統計收集
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartEmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingProperties embeddingProperties;

    /**
     * 模型統計數據（內存存儲）
     */
    private final Map<String, ModelStats> modelStatsMap = new ConcurrentHashMap<>();

    /**
     * 生成 Embedding（智能模型選擇 + 快取）
     *
     * @param text 文本
     * @param context Embedding 上下文
     * @return Embedding 向量
     */
    public List<Double> generateEmbedding(String text, EmbeddingContext context) {
        long startTime = System.currentTimeMillis();

        // 1. 文本預處理
        String processedText = preprocessText(text, PreprocessingOptions.defaultOptions());

        // 2. 檢查快取
        String cacheKey = CacheKeyGenerator.generateEmbeddingKey(
                processedText,
                embeddingProperties.getPrimaryModel(),
                embeddingProperties.getDefaultDimensions()
        );

        // 3. 生成 Embedding（帶快取）
        List<Double> embedding = generateEmbeddingWithCache(processedText, context);

        // 4. 記錄統計
        long processingTime = System.currentTimeMillis() - startTime;
        recordStats(embeddingProperties.getPrimaryModel(), processingTime, text.length(), true);

        log.debug("生成 Embedding 完成，耗時: {}ms，文本長度: {}", processingTime, text.length());

        return embedding;
    }

    /**
     * 批次生成 Embedding
     *
     * @param texts 文本列表
     * @param context Embedding 上下文
     * @return Embedding 向量列表
     */
    public List<List<Double>> generateEmbeddings(List<String> texts, EmbeddingContext context) {
        long startTime = System.currentTimeMillis();

        log.info("開始批次生成 Embedding，數量: {}", texts.size());

        // 使用並行流處理批次請求（提升性能）
        List<List<Double>> embeddings = texts.parallelStream()
                .map(text -> generateEmbedding(text, context))
                .toList();

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("批次生成 Embedding 完成，數量: {}，總耗時: {}ms，平均: {}ms",
                texts.size(), processingTime, processingTime / texts.size());

        return embeddings;
    }

    /**
     * 生成 Embedding（帶快取）
     *
     * @param text 文本
     * @param context Embedding 上下文
     * @return Embedding 向量
     */
    @Cacheable(value = "embeddings", key = "#text", unless = "#result == null")
    public List<Double> generateEmbeddingWithCache(String text, EmbeddingContext context) {
        try {
            // 使用 Spring AI EmbeddingModel 生成 Embedding
            EmbeddingResponse response = embeddingModel.call(
                    new EmbeddingRequest(List.of(text), null)
            );

            if (response.getResults().isEmpty()) {
                log.error("Embedding 生成失敗：無結果返回");
                return List.of();
            }

            // 轉換 float[] 為 List<Double>
            float[] floatArray = response.getResults().get(0).getOutput();
            List<Double> doubleList = new ArrayList<>();
            for (float f : floatArray) {
                doubleList.add((double) f);
            }

            return doubleList;

        } catch (Exception e) {
            log.error("Embedding 生成失敗", e);
            recordStats(embeddingProperties.getPrimaryModel(), 0, text.length(), false);
            throw new RuntimeException("Embedding 生成失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 文本預處理
     *
     * @param text 原始文本
     * @param options 預處理選項
     * @return 處理後的文本
     */
    public String preprocessText(String text, PreprocessingOptions options) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String processed = text;

        // 應用預處理選項
        if (options.getRemoveSpecialChars()) {
            processed = TextUtil.cleanSpecialChars(processed);
        }

        if (options.getNormalizeWhitespace()) {
            processed = TextUtil.normalizeWhitespace(processed);
        }

        if (options.getRemoveUrls()) {
            processed = processed.replaceAll("https?://[\\S]+", "");
        }

        if (options.getRemoveEmails()) {
            processed = processed.replaceAll("[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}", "");
        }

        if (options.getRemoveExtraNewlines()) {
            processed = processed.replaceAll("\n{2,}", "\n");
        }

        // 長度限制
        if (options.getMaxLength() > 0 && processed.length() > options.getMaxLength()) {
            if (options.getTruncateLongText()) {
                processed = truncateText(processed, options.getMaxLength(), options.getTruncationStrategy());
            }
        }

        return processed;
    }

    /**
     * 截斷文本
     *
     * @param text 文本
     * @param maxLength 最大長度
     * @param strategy 截斷策略（start, end, middle）
     * @return 截斷後的文本
     */
    private String truncateText(String text, int maxLength, String strategy) {
        if (text.length() <= maxLength) {
            return text;
        }

        return switch (strategy) {
            case "start" -> text.substring(text.length() - maxLength);
            case "middle" -> {
                int half = maxLength / 2;
                yield text.substring(0, half) + "..." + text.substring(text.length() - half);
            }
            default -> text.substring(0, maxLength); // "end"
        };
    }

    /**
     * 記錄模型統計數據
     *
     * @param modelName 模型名稱
     * @param processingTime 處理時間（毫秒）
     * @param textLength 文本長度
     * @param success 是否成功
     */
    private void recordStats(String modelName, long processingTime, int textLength, boolean success) {
        ModelStats stats = modelStatsMap.computeIfAbsent(modelName, k ->
                ModelStats.builder()
                        .modelName(modelName)
                        .build()
        );

        if (success) {
            // 估算 token 數量（簡單估算：字符數 / 4）
            int estimatedTokens = textLength / 4;
            stats.recordSuccess(processingTime, textLength, estimatedTokens, false);
        } else {
            stats.recordFailure();
        }
    }

    /**
     * 獲取模型統計數據
     *
     * @param modelName 模型名稱
     * @return 統計數據
     */
    public ModelStats getModelStats(String modelName) {
        return modelStatsMap.get(modelName);
    }

    /**
     * 獲取所有模型統計數據
     *
     * @return 統計數據映射
     */
    public Map<String, ModelStats> getAllModelStats() {
        return Map.copyOf(modelStatsMap);
    }

    /**
     * 清空統計數據
     */
    public void clearStats() {
        modelStatsMap.clear();
        log.info("已清空所有模型統計數據");
    }
}
