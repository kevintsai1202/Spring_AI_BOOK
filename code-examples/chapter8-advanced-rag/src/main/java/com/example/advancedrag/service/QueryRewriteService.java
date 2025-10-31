package com.example.advancedrag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查詢重寫服務
 *
 * 功能：
 * 1. 查詢重寫（優化用戶查詢以提高檢索效果）
 * 2. 查詢擴展（生成多個相關查詢）
 * 3. 關鍵詞提取
 * 4. 查詢意圖識別
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryRewriteService {

    private final ChatClient chatClient;

    /**
     * 查詢重寫 Prompt 模板
     */
    private static final String REWRITE_PROMPT_TEMPLATE = """
            你是一個專業的查詢優化助手。請將用戶的查詢重寫為更適合檢索的形式。

            重寫要求：
            1. 保留原始查詢的核心意圖
            2. 使用更精確、更專業的術語
            3. 補充必要的上下文信息
            4. 移除口語化表達和冗餘信息
            5. 確保查詢清晰、具體

            用戶查詢：{query}

            請直接返回重寫後的查詢，不需要任何解釋。
            """;

    /**
     * 查詢擴展 Prompt 模板
     */
    private static final String EXPANSION_PROMPT_TEMPLATE = """
            你是一個專業的查詢擴展助手。請基於用戶的查詢，生成 {count} 個相關但不同角度的查詢。

            擴展要求：
            1. 每個查詢都應該與原始查詢相關
            2. 從不同角度或層面表達相同的信息需求
            3. 包含同義詞、相關概念
            4. 保持查詢的簡潔性

            用戶查詢：{query}

            請返回 {count} 個查詢，每行一個，不需要編號或其他格式。
            """;

    /**
     * 關鍵詞提取 Prompt 模板
     */
    private static final String KEYWORD_EXTRACTION_PROMPT_TEMPLATE = """
            你是一個專業的關鍵詞提取助手。請從用戶查詢中提取最重要的關鍵詞。

            提取要求：
            1. 提取 3-5 個最重要的關鍵詞
            2. 關鍵詞應該是名詞、專業術語或核心概念
            3. 移除停用詞和無意義詞彙

            用戶查詢：{query}

            請返回關鍵詞列表，用逗號分隔，不需要其他格式。
            """;

    /**
     * 重寫查詢
     *
     * @param originalQuery 原始查詢
     * @return 重寫後的查詢
     */
    @Cacheable(value = "rewrittenQueries", key = "#originalQuery")
    public String rewriteQuery(String originalQuery) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("開始重寫查詢: {}", originalQuery);

            PromptTemplate promptTemplate = new PromptTemplate(REWRITE_PROMPT_TEMPLATE);
            Prompt prompt = promptTemplate.create(Map.of("query", originalQuery));

            String rewrittenQuery = chatClient.prompt(prompt)
                    .call()
                    .content();

            // 清理返回結果（移除可能的引號）
            rewrittenQuery = rewrittenQuery.trim().replaceAll("^\"|\"$", "");

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("查詢重寫完成，耗時: {}ms，原始: [{}]，重寫: [{}]",
                    processingTime, originalQuery, rewrittenQuery);

            return rewrittenQuery;

        } catch (Exception e) {
            log.error("查詢重寫失敗，返回原始查詢", e);
            return originalQuery;
        }
    }

    /**
     * 擴展查詢（生成多個相關查詢）
     *
     * @param originalQuery 原始查詢
     * @param count 生成數量
     * @return 擴展查詢列表
     */
    @Cacheable(value = "expandedQueries", key = "#originalQuery + '_' + #count")
    public List<String> expandQuery(String originalQuery, int count) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("開始擴展查詢: {}，數量: {}", originalQuery, count);

            PromptTemplate promptTemplate = new PromptTemplate(EXPANSION_PROMPT_TEMPLATE);
            Prompt prompt = promptTemplate.create(Map.of(
                    "query", originalQuery,
                    "count", String.valueOf(count)
            ));

            String response = chatClient.prompt(prompt)
                    .call()
                    .content();

            // 解析返回的查詢列表
            List<String> expandedQueries = parseQueryList(response);

            // 確保返回指定數量的查詢
            while (expandedQueries.size() < count) {
                expandedQueries.add(originalQuery);
            }

            // 移除重複項並限制數量
            expandedQueries = expandedQueries.stream()
                    .distinct()
                    .limit(count)
                    .toList();

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("查詢擴展完成，耗時: {}ms，生成數量: {}", processingTime, expandedQueries.size());

            return expandedQueries;

        } catch (Exception e) {
            log.error("查詢擴展失敗，返回原始查詢", e);
            return List.of(originalQuery);
        }
    }

    /**
     * 提取關鍵詞
     *
     * @param query 查詢
     * @return 關鍵詞列表
     */
    @Cacheable(value = "queryKeywords", key = "#query")
    public List<String> extractKeywords(String query) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("開始提取關鍵詞: {}", query);

            PromptTemplate promptTemplate = new PromptTemplate(KEYWORD_EXTRACTION_PROMPT_TEMPLATE);
            Prompt prompt = promptTemplate.create(Map.of("query", query));

            String response = chatClient.prompt(prompt)
                    .call()
                    .content();

            // 解析關鍵詞列表
            List<String> keywords = parseKeywordList(response);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("關鍵詞提取完成，耗時: {}ms，關鍵詞: {}", processingTime, keywords);

            return keywords;

        } catch (Exception e) {
            log.error("關鍵詞提取失敗", e);
            return List.of();
        }
    }

    /**
     * 優化查詢（組合重寫和關鍵詞提取）
     *
     * @param originalQuery 原始查詢
     * @return 優化後的查詢
     */
    public String optimizeQuery(String originalQuery) {
        try {
            // 1. 提取關鍵詞
            List<String> keywords = extractKeywords(originalQuery);

            // 2. 重寫查詢
            String rewrittenQuery = rewriteQuery(originalQuery);

            // 3. 如果重寫失敗或與原查詢相同，使用關鍵詞增強
            if (rewrittenQuery.equals(originalQuery) && !keywords.isEmpty()) {
                return originalQuery + " " + String.join(" ", keywords);
            }

            return rewrittenQuery;

        } catch (Exception e) {
            log.error("查詢優化失敗，返回原始查詢", e);
            return originalQuery;
        }
    }

    /**
     * 解析查詢列表
     *
     * @param response LLM 返回的文本
     * @return 查詢列表
     */
    private List<String> parseQueryList(String response) {
        if (response == null || response.isEmpty()) {
            return new ArrayList<>();
        }

        return response.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                // 移除可能的編號（如 "1. ", "- " 等）
                .map(line -> line.replaceAll("^\\d+\\.\\s*|^-\\s*|^\\*\\s*", ""))
                // 移除引號
                .map(line -> line.replaceAll("^\"|\"$", ""))
                .filter(line -> !line.isEmpty())
                .toList();
    }

    /**
     * 解析關鍵詞列表
     *
     * @param response LLM 返回的文本
     * @return 關鍵詞列表
     */
    private List<String> parseKeywordList(String response) {
        if (response == null || response.isEmpty()) {
            return new ArrayList<>();
        }

        // 嘗試多種分隔符
        String[] separators = {",", "、", ";", "\\n"};

        for (String separator : separators) {
            String[] parts = response.split(separator);
            if (parts.length > 1) {
                return List.of(parts).stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }
        }

        // 如果沒有分隔符，返回整個字符串作為單個關鍵詞
        return List.of(response.trim());
    }
}
