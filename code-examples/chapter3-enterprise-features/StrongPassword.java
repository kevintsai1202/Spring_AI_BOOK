/**
 * 自訂驗證註解：檢查密碼強度
 */
package com.example.demo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
    
    String message() default "密碼強度不足";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 是否要求包含特殊字元
     */
    boolean requireSpecialChar() default true;
    
    /**
     * 最小長度
     */
    int minLength() default 8;
}