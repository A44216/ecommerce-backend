package com.ecommerce.backend;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.enums.UserStatus;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EcommerceBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceBackendApplication.class, args);
	}

    @Bean
    CommandLineRunner init(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("admin12345").isEmpty()) {
                User admin = new User();
                admin.setFullName("Admin");
                admin.setUsername("admin12345");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(encoder.encode("admin12345"));
                admin.setRole(Role.ADMIN);
                admin.setStatus(UserStatus.ACTIVE);

                repo.save(admin);

                System.out.println("✅ Admin created");
            }

            // ===== CUSTOMER =====
            if (repo.findByEmail("user@gmail.com").isEmpty()) {
                User user = new User();
                user.setFullName("Nguyen Van C");
                user.setUsername("user12345");
                user.setEmail("user@gmail.com");
                user.setPassword(encoder.encode("123456"));
                user.setRole(Role.CUSTOMER);
                user.setStatus(UserStatus.ACTIVE);

                repo.save(user);
                System.out.println("✅ Customer created");
            }

        };
    }

}
