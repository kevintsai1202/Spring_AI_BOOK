/**
 * 使用者回應 DTO
 * 使用 Lombok @Data 註解自動生成 getter/setter 方法
 * 重要：不包含 password 欄位，保護機密資料不被外洩
 */
package com.example.demo.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    // 注意：刻意不包含 password 欄位，確保機密資料安全
}