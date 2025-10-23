/**
 * 使用者控制器
 * 提供完整的 RESTful API 端點
 * 展示 DTO 模式和資料驗證的最佳實踐
 */
package com.example.demo.controller;

import com.example.demo.request.CreateUserRequest;
import com.example.demo.response.UserResponse;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    // 建構函式注入
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 獲取所有使用者
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * 根據 ID 獲取使用者
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.findUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 建立新使用者
     * POST /api/users
     * 使用 @Valid 啟用自動驗證
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * 更新使用者
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 刪除使用者
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}