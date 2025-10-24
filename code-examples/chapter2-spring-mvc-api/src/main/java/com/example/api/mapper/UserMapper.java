package com.example.api.mapper;

import com.example.api.dto.UserDto;
import com.example.api.entity.User;
import org.springframework.stereotype.Component;

/**
 * 使用者映射器
 * 負責 User 實體與 UserDto 之間的轉換
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Component
public class UserMapper {

    /**
     * 將 User 實體轉換為 UserDto
     *
     * @param user 使用者實體
     * @return 使用者 DTO
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 將 UserDto 轉換為 User 實體
     *
     * @param dto 使用者 DTO
     * @return 使用者實體
     */
    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .build();
    }
}
