package com.ecommerce.backend.config;

import com.ecommerce.backend.enums.Role;

import com.ecommerce.backend.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(request -> {
                    org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOrigins(java.util.List.of("*")); // Cho phép tất cả (Hữu ích khi dev mobile)
                    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(java.util.List.of("*"));
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/seller/shops/**").authenticated() // Allow registration & cancel for any auth user
                        .requestMatchers("/api/seller/**").hasRole(Role.SELLER.name())
                        .requestMatchers("/api/admin/**").hasRole(Role.ADMIN.name())

                        // Các endpoint công khai (không cần đăng nhập)
                        .requestMatchers("/api/ping").permitAll()
                        .requestMatchers("/api/auth/**", "/api/payment/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/shops/**", "/api/reviews/**", "/api/images/**").permitAll()

                        // Các endpoint cần đăng nhập
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/conversations/**", "/api/messages/**").authenticated()
                        .requestMatchers("/api/platform-fees/**").authenticated()

                        // Tất cả các yêu cầu còn lại đều cần xác thực
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

}