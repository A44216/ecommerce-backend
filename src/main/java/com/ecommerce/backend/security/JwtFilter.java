package com.ecommerce.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // Bỏ qua filter cho các endpoint public
        if (
                path.equals("/api/auth/login") ||
                        path.equals("/api/auth/register") ||
                        path.equals("/api/auth/google") ||
                        path.equals("/api/auth/send-register-otp") ||
                        path.equals("/api/auth/send-forgot-password-otp") ||
                        path.equals("/api/auth/verify-otp") ||
                        path.equals("/api/auth/reset-password")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Nếu không có header hoặc không bắt đầu bằng "Bearer ", coi như khách vãng lai
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        // Chặn ngay nếu Android gửi nhầm chữ "null" hoặc rỗng
        if (jwt.isEmpty() || jwt.equals("null")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Bọc trong try-catch để chặn lỗi vặt do token sai định dạng (token sai thì không crash request)
            username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Nếu có lỗi parse JWT (như MalformedJwtException, ExpiredJwtException),
            // cứ im lặng cho qua, SecurityConfig sẽ tự động chặn nếu API đó bắt buộc đăng nhập
            System.out.println("Bỏ qua Token không hợp lệ: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}