package com.example.enterprise.controller;

import com.example.enterprise.dto.ApiResponse;
import com.example.enterprise.dto.UserRegistrationRequest;
import com.example.enterprise.entity.User;
import com.example.enterprise.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 使用者 API 控制器
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "使用者管理", description = "使用者註冊、查詢與刪除 API")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "取得所有使用者", description = "查詢系統中所有已註冊的使用者列表")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ApiResponse.success("查詢成功，共 " + users.size() + " 位使用者", users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根據 ID 取得使用者", description = "根據使用者 ID 查詢單一使用者資訊")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "使用者不存在"
            )
    })
    public ApiResponse<User> getUserById(
            @Parameter(description = "使用者 ID", required = true)
            @PathVariable Long id) {
        User user = userService.findById(id);
        return ApiResponse.success("查詢成功", user);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "註冊新使用者", description = "註冊新使用者，需提供使用者名稱、電子郵件和密碼等資訊")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "註冊成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "請求資料驗證失敗"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "使用者名稱或電子郵件已存在"
            )
    })
    public ApiResponse<User> registerUser(
            @Parameter(description = "使用者註冊資訊", required = true)
            @Valid @RequestBody UserRegistrationRequest request) {
        User user = userService.registerUser(request);
        return ApiResponse.success("使用者註冊成功", user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "刪除使用者", description = "根據使用者 ID 刪除使用者")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "刪除成功"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "使用者不存在"
            )
    })
    public ApiResponse<Void> deleteUser(
            @Parameter(description = "使用者 ID", required = true)
            @PathVariable Long id) {
        userService.deleteById(id);
        return ApiResponse.success("使用者刪除成功", null);
    }
}
