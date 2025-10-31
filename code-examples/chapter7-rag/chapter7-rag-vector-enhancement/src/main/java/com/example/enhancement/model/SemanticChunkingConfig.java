package com.example.enhancement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 語義分塊配置類
 *
 * 定義文本分塊的各種參數，確保分塊時保持語義完整性
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SemanticChunkingConfig {

    /**
     * 每個分塊的最大 token 數量
     */
    @Builder.Default
    private int maxTokensPerChunk = 1000;

    /**
     * 分塊之間的重疊 token 數量
     */
    @Builder.Default
    private int overlapTokens = 100;

    /**
     * 是否保持段落完整性
     */
    @Builder.Default
    private boolean preserveParagraphs = true;

    /**
     * 是否保持句子完整性
     */
    @Builder.Default
    private boolean preserveSentences = true;

    /**
     * 最小分塊大小（token 數）
     */
    @Builder.Default
    private int minChunkSize = 100;
}
