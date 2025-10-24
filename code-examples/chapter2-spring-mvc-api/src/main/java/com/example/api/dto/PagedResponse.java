package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分頁回應 DTO
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    /** 資料內容 */
    private List<T> content;

    /** 當前頁碼 */
    private int page;

    /** 每頁大小 */
    private int size;

    /** 總元素數量 */
    private long totalElements;

    /** 總頁數 */
    private int totalPages;

    /** 是否為第一頁 */
    private boolean first;

    /** 是否為最後一頁 */
    private boolean last;
}
