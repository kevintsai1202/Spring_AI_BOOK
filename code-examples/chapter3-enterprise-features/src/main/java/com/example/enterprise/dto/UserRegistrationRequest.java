package com.example.enterprise.dto;

import com.example.enterprise.validation.StrongPassword;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 使用者註冊請求 DTO
 * 展示完整的資料驗證功能
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    @NotBlank(message = "使用者名稱不能為空")
    @Size(min = 2, max = 50, message = "使用者名稱長度必須在 2-50 字元之間")
    private String username;

    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "電子郵件格式不正確")
    private String email;

    @StrongPassword(minLength = 8, requireSpecialChar = true,
            message = "密碼必須至少 8 字元，包含大小寫字母、數字和特殊字元")
    private String password;

    @NotBlank(message = "全名不能為空")
    @Size(max = 100, message = "全名長度不能超過 100 字元")
    private String fullName;

    @NotNull(message = "年齡不能為空")
    @Min(value = 18, message = "年齡必須大於等於 18 歲")
    @Max(value = 120, message = "年齡必須小於等於 120 歲")
    private Integer age;
}
