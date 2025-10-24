package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 使用者搜尋條件 DTO
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchCriteria {

    /** 使用者名稱（模糊搜尋） */
    private String name;

    /** 電子郵件（模糊搜尋） */
    private String email;
}
