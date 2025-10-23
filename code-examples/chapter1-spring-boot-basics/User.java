/**
 * 使用者實體類別
 * 使用 Lombok @Data 註解自動生成 getter/setter 方法
 * 包含完整的使用者資訊，包括機密的密碼欄位
 */
package com.example.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;  // 機密資料，不應在 API 回應中暴露
    
    // 自訂建構函式（不包含 id）
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}