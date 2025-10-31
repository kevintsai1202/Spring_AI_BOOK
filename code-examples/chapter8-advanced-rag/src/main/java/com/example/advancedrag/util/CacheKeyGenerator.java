package com.example.advancedrag.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 快取鍵生成器
 *
 * 為 Embedding 和其他快取場景生成唯一的快取鍵
 */
public class CacheKeyGenerator {

    private static final String EMBEDDING_PREFIX = "emb:";
    private static final String QUERY_PREFIX = "query:";
    private static final String DOCUMENT_PREFIX = "doc:";
    private static final String SESSION_PREFIX = "session:";

    /**
     * 生成 Embedding 快取鍵
     *
     * @param text 文本內容
     * @param model 模型名稱
     * @param dimensions 向量維度
     * @return 快取鍵
     */
    public static String generateEmbeddingKey(String text, String model, Integer dimensions) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("文本不能為空");
        }

        // 使用 MD5 生成文本的唯一標識
        String textHash = DigestUtils.md5Hex(text);

        return EMBEDDING_PREFIX + model + ":" + dimensions + ":" + textHash;
    }

    /**
     * 生成查詢快取鍵
     *
     * @param query 查詢內容
     * @param sessionId 會話 ID
     * @return 快取鍵
     */
    public static String generateQueryKey(String query, String sessionId) {
        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("查詢不能為空");
        }

        String queryHash = DigestUtils.md5Hex(query);
        String session = StringUtils.isNotBlank(sessionId) ? sessionId : "default";

        return QUERY_PREFIX + session + ":" + queryHash;
    }

    /**
     * 生成文檔快取鍵
     *
     * @param documentId 文檔 ID
     * @return 快取鍵
     */
    public static String generateDocumentKey(String documentId) {
        if (StringUtils.isBlank(documentId)) {
            throw new IllegalArgumentException("文檔 ID 不能為空");
        }

        return DOCUMENT_PREFIX + documentId;
    }

    /**
     * 生成會話快取鍵
     *
     * @param sessionId 會話 ID
     * @return 快取鍵
     */
    public static String generateSessionKey(String sessionId) {
        if (StringUtils.isBlank(sessionId)) {
            throw new IllegalArgumentException("會話 ID 不能為空");
        }

        return SESSION_PREFIX + sessionId;
    }

    /**
     * 生成通用快取鍵
     *
     * @param prefix 前綴
     * @param parts 鍵的各個部分
     * @return 快取鍵
     */
    public static String generateKey(String prefix, String... parts) {
        if (StringUtils.isBlank(prefix)) {
            throw new IllegalArgumentException("前綴不能為空");
        }

        StringBuilder key = new StringBuilder(prefix);
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                key.append(":").append(part);
            }
        }

        return key.toString();
    }

    /**
     * 生成帶時間戳的快取鍵
     *
     * @param prefix 前綴
     * @param identifier 標識符
     * @return 快取鍵
     */
    public static String generateKeyWithTimestamp(String prefix, String identifier) {
        long timestamp = System.currentTimeMillis();
        return generateKey(prefix, identifier, String.valueOf(timestamp));
    }
}
