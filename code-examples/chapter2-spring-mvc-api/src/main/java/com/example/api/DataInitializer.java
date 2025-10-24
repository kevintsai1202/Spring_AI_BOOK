package com.example.api;

import com.example.api.entity.Product;
import com.example.api.entity.User;
import com.example.api.repository.ProductRepository;
import com.example.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 資料初始化器
 * 應用程式啟動時自動插入測試資料
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // 初始化使用者資料
        initUsers();

        // 初始化產品資料
        initProducts();

        System.out.println("✅ 測試資料初始化完成！");
        System.out.println("📊 使用者數量: " + userRepository.count());
        System.out.println("📦 產品數量: " + productRepository.count());
    }

    /**
     * 初始化使用者資料
     */
    private void initUsers() {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .fullName("系統管理員")
                    .build();
            userRepository.save(admin);

            User john = User.builder()
                    .username("john")
                    .email("john@example.com")
                    .fullName("John Doe")
                    .build();
            userRepository.save(john);

            User jane = User.builder()
                    .username("jane")
                    .email("jane@example.com")
                    .fullName("Jane Smith")
                    .build();
            userRepository.save(jane);
        }
    }

    /**
     * 初始化產品資料
     */
    private void initProducts() {
        if (productRepository.count() == 0) {
            Product iphone = Product.builder()
                    .name("iPhone 15 Pro")
                    .description("Apple 最新款旗艦手機")
                    .price(new BigDecimal("36900"))
                    .stock(100)
                    .category("electronics")
                    .build();
            productRepository.save(iphone);

            Product macbook = Product.builder()
                    .name("MacBook Pro M3")
                    .description("Apple 最新款專業筆記型電腦")
                    .price(new BigDecimal("64900"))
                    .stock(50)
                    .category("electronics")
                    .build();
            productRepository.save(macbook);

            Product airpods = Product.builder()
                    .name("AirPods Pro (第 2 代)")
                    .description("主動降噪無線耳機")
                    .price(new BigDecimal("7490"))
                    .stock(200)
                    .category("electronics")
                    .build();
            productRepository.save(airpods);
        }
    }
}
