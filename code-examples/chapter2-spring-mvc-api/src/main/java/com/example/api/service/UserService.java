package com.example.api.service;

import com.example.api.dto.UserSearchCriteria;
import com.example.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 使用者服務介面
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
public interface UserService {

    /**
     * 根據條件查詢使用者（分頁）
     *
     * @param criteria 查詢條件
     * @param pageable 分頁參數
     * @return 使用者分頁結果
     */
    Page<User> findUsers(UserSearchCriteria criteria, Pageable pageable);

    /**
     * 根據 ID 查詢使用者
     *
     * @param id 使用者 ID
     * @return 使用者
     */
    User findById(Long id);

    /**
     * 儲存使用者
     *
     * @param user 使用者
     * @return 儲存後的使用者
     */
    User save(User user);

    /**
     * 更新使用者
     *
     * @param user 使用者
     * @return 更新後的使用者
     */
    User update(User user);

    /**
     * 刪除使用者
     *
     * @param id 使用者 ID
     */
    void deleteById(Long id);

    /**
     * 檢查使用者是否存在
     *
     * @param id 使用者 ID
     * @return 是否存在
     */
    boolean existsById(Long id);

    /**
     * 驗證使用者是否存在（不存在則拋出異常）
     *
     * @param id 使用者 ID
     */
    void validateUserExists(Long id);
}
