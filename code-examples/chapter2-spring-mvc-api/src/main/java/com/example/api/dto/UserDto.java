package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 使用者資料傳輸物件
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /** 使用者 ID */
    private Long id;

    /** 使用者名稱 */
    private String username;

    /** 電子郵件 */
    private String email;

    /** 全名 */
    private String fullName;

    /** 建立時間 */
    private LocalDateTime createdAt;

    /** 更新時間 */
    private LocalDateTime updatedAt;
}
