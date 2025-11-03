package com.example.mcp.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用戶資料模型
 * 用於存儲和返回用戶的個人資料
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    /**
     * 用戶名（唯一識別符）
     */
    private String username;

    /**
     * 全名
     */
    private String fullName;

    /**
     * 電子郵件
     */
    private String email;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 年齡
     */
    private Integer age;

    /**
     * 職業
     */
    private String occupation;

    /**
     * 興趣愛好列表
     */
    private List<String> interests;
}
