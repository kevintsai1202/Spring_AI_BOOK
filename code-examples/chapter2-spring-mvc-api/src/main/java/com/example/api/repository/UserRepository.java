package com.example.api.repository;

import com.example.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 使用者資料存取介面
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根據使用者名稱查詢使用者
     *
     * @param username 使用者名稱
     * @return 使用者
     */
    Optional<User> findByUsername(String username);

    /**
     * 根據電子郵件查詢使用者
     *
     * @param email 電子郵件
     * @return 使用者
     */
    Optional<User> findByEmail(String email);
}
