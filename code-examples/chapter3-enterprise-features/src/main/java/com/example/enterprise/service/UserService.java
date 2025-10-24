package com.example.enterprise.service;

import com.example.enterprise.dto.UserRegistrationRequest;
import com.example.enterprise.entity.User;
import com.example.enterprise.exception.BusinessException;
import com.example.enterprise.exception.ResourceNotFoundException;
import com.example.enterprise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 使用者服務實作類別
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("使用者", id));
    }

    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        // 檢查電子郵件是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(409, "電子郵件 " + request.getEmail() + " 已被註冊");
        }

        // 檢查使用者名稱是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(409, "使用者名稱 " + request.getUsername() + " 已被使用");
        }

        // 建立使用者
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword()) // 實際應用中應該加密
                .fullName(request.getFullName())
                .age(request.getAge())
                .build();

        User savedUser = userRepository.save(user);
        log.info("使用者註冊成功：{}", savedUser.getUsername());

        return savedUser;
    }

    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("使用者", id);
        }
        userRepository.deleteById(id);
        log.info("使用者刪除成功：ID={}", id);
    }
}
