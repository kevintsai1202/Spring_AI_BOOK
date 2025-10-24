/**
 * 建立使用者請求 DTO
 * 使用 Lombok @Data 註解自動生成 getter/setter 方法
 * 包含完整的資料驗證註解，確保輸入資料的品質
 */
package com.example.demo.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CreateUserRequest {
    
    @NotBlank(message = "姓名不能為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在 2-50 字元之間")
    private String name;
    
    @Email(message = "電子郵件格式不正確")
    @NotBlank(message = "電子郵件不能為空")
    private String email;
    
    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 20, message = "密碼長度必須在 6-20 字元之間")
    private String password;
}