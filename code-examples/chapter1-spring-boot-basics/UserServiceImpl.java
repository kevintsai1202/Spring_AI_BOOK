/**
 * 使用者服務實作
 * 使用記憶體儲存模擬資料庫操作
 */
package com.example.demo.service;

import com.example.demo.request.CreateUserRequest;
import com.example.demo.response.UserResponse;
import com.example.demo.model.User;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    
    // 使用記憶體中的資料模擬資料庫
    private final List<User> users = new ArrayList<>();
    private Long nextId = 1L;
    
    // 初始化一些測試資料
    public UserServiceImpl() {
        users.add(new User("張小明", "ming@example.com", "password123"));
        users.add(new User("李小華", "hua@example.com", "secret456"));
        users.get(0).setId(nextId++);
        users.get(1).setId(nextId++);
    }
    
    @Override
    public List<UserResponse> findAllUsers() {
        return users.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public UserResponse findUserById(Long id) {
        User user = users.stream()
            .filter(u -> u.getId().equals(id))
            .findFirst()
            .orElse(null);
        
        return user != null ? convertToResponse(user) : null;
    }
    
    @Override
    public UserResponse createUser(CreateUserRequest request) {
        User user = new User(request.getName(), request.getEmail(), request.getPassword());
        user.setId(nextId++);
        users.add(user);
        return convertToResponse(user);
    }
    
    @Override
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        User existingUser = users.stream()
            .filter(u -> u.getId().equals(id))
            .findFirst()
            .orElse(null);
        
        if (existingUser != null) {
            existingUser.setName(request.getName());
            existingUser.setEmail(request.getEmail());
            existingUser.setPassword(request.getPassword());
            return convertToResponse(existingUser);
        }
        return null;
    }
    
    @Override
    public void deleteUser(Long id) {
        users.removeIf(user -> user.getId().equals(id));
    }
    
    /**
     * 私有方法：將 User 轉換為 UserResponse
     * 重要：這裡不包含 password 欄位，確保機密資料不外洩
     */
    private UserResponse convertToResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}