package com.example.enterprise.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 強密碼驗證器
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private boolean requireSpecialChar;
    private int minLength;

    @Override
    public void initialize(StrongPassword annotation) {
        this.requireSpecialChar = annotation.requireSpecialChar();
        this.minLength = annotation.minLength();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        // 檢查長度
        if (password.length() < minLength) {
            return false;
        }

        // 檢查是否包含大寫字母
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // 檢查是否包含小寫字母
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // 檢查是否包含數字
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // 檢查是否包含特殊字元（如果需要）
        if (requireSpecialChar && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':,.<>?].*")) {
            return false;
        }

        return true;
    }
}
