package com.example.api;

import com.example.api.dto.PagedResponse;
import com.example.api.dto.UserDto;
import com.example.api.dto.UserSearchCriteria;
import com.example.api.entity.User;
import com.example.api.mapper.UserMapper;
import com.example.api.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基礎 RESTful API 控制器
 * 展示標準的資源導向設計模式
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserRestController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserRestController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * 獲取使用者列表
     * GET /api/v1/users
     * 支援分頁和篩選參數
     */
    @GetMapping
    public ResponseEntity<PagedResponse<UserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email
    ) {
        // 建立查詢條件
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .name(name)
                .email(email)
                .build();

        // 執行分頁查詢
        Page<User> userPage = userService.findUsers(criteria, PageRequest.of(page, size));

        // 轉換為 DTO
        List<UserDto> userDtos = userPage.getContent().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        // 建立分頁回應
        PagedResponse<UserDto> response = PagedResponse.<UserDto>builder()
                .content(userDtos)
                .page(page)
                .size(size)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(userPage.getTotalElements()))
                .body(response);
    }

    /**
     * 根據 ID 獲取單一使用者
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        UserDto userDto = userMapper.toDto(user);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
                .body(userDto);
    }
}
