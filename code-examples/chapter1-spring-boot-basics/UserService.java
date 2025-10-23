/**
 * 使用者服務介面
 * 定義使用者管理的核心業務邏輯
 */
package com.example.demo.service;

import com.example.demo.request.CreateUserRequest;
import com.example.demo.response.UserResponse;
import java.util.List;

public interface UserService {
    List<UserResponse> findAllUsers();
    UserResponse findUserById(Long id);
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(Long id, CreateUserRequest request);
    void deleteUser(Long id);
}