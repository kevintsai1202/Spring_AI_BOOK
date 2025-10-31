package com.example.etl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本分塊配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkingConfig {
    /** 默認塊大小 (tokens) */
    @Builder.Default
    private int defaultChunkSize = 1000;

    /** 最小塊大小 (字符) */
    @Builder.Default
    private int minChunkSizeChars = 350;

    /** 最小嵌入長度 */
    @Builder.Default
    private int minChunkLengthToEmbed = 10;

    /** 最大塊數量 */
    @Builder.Default
    private int maxNumChunks = 10000;

    /** 保留分隔符 */
    @Builder.Default
    private boolean keepSeparator = true;

    /** 塊重疊大小 */
    @Builder.Default
    private int overlapSize = 200;
}
