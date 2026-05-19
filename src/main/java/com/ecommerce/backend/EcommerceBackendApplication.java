package com.ecommerce.backend;

import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.enums.ShopStatus;
import com.ecommerce.backend.enums.UserStatus;
import com.ecommerce.backend.repository.ShopRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling

public class EcommerceBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceBackendApplication.class, args);
	}

    @Bean
    CommandLineRunner init(UserRepository repo, ShopRepository shopRepo, PasswordEncoder encoder, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE orders MODIFY COLUMN status VARCHAR(50);");
            } catch (Exception e) {
                System.out.println("Could not alter table orders: " + e.getMessage());
            }
            // ADMIN
            if (repo.findByUsername("admin12345").isEmpty()) {
                User admin = new User();
                admin.setFullName("Admin");
                admin.setUsername("admin12345");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(encoder.encode("admin12345"));
                admin.setRole(Role.ADMIN);
                admin.setStatus(UserStatus.ACTIVE);

                repo.save(admin);

                System.out.println("Admin created");
            }

            // CUSTOMER
            if (repo.findByEmail("user@gmail.com").isEmpty()) {
                User user = new User();
                user.setFullName("Nguyen Van C");
                user.setUsername("user12345");
                user.setEmail("user@gmail.com");
                user.setPassword(encoder.encode("123456"));
                user.setRole(Role.CUSTOMER);
                user.setStatus(UserStatus.ACTIVE);

                repo.save(user);
                System.out.println("Customer created");
            }

            // SELLER
            if (repo.findByEmail("seller@gmail.com").isEmpty()) {
                User user = new User();
                user.setFullName("seller12345");
                user.setUsername("seller12345");
                user.setEmail("seller@gmail.com");
                user.setPassword(encoder.encode("123456"));
                user.setRole(Role.SELLER);
                user.setStatus(UserStatus.ACTIVE);

                repo.save(user);
                System.out.println("Seller created");

                Shop shop = new Shop();
                shop.setUser(user);
                shop.setShopName("Cửa hàng điện tử của " + user.getFullName());
                shop.setStatus(ShopStatus.APPROVED);
                shop.setDescription("Chuyên cung cấp linh kiện điện tử.");

                // 3. Lưu shop
                shopRepo.save(shop);
                System.out.println("Default shop created for seller");
            }

        };
    }

}
