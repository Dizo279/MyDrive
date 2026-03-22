package com.mydrive.service;

import com.mydrive.dto.request.LoginRequest;
import com.mydrive.dto.request.RegisterRequest;
import com.mydrive.dto.response.AuthResponse;
import com.mydrive.entity.User;
import com.mydrive.entity.UserQuota;
import com.mydrive.exception.AppException;
import com.mydrive.repository.UserRepository;
import com.mydrive.repository.UserQuotaRepository;
import com.mydrive.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository       userRepository;
    private final UserQuotaRepository  userQuotaRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtTokenProvider     jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // ==================== ĐĂNG NHẬP ====================
    public AuthResponse login(LoginRequest request) {
        // Giao cho Spring Security xác thực — tự throw BadCredentialsException nếu sai
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Lấy thông tin user từ DB để đưa vào response
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> AppException.notFound("User không tồn tại"));

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    // ==================== ĐĂNG KÝ ====================
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra trùng username / email
        if (userRepository.existsByUsername(request.getUsername())) {
            throw AppException.conflict("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AppException.conflict("Email đã được sử dụng");
        }

        // Tạo user mới
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build();
        userRepository.save(user);

        // Tạo quota mặc định 5GB cho user mới
        UserQuota quota = UserQuota.builder()
                .user(user)
                .quotaBytes(5_368_709_120L)   // 5GB
                .usedBytes(0L)
                .plan(UserQuota.Plan.FREE)
                .build();
        userQuotaRepository.save(quota);

        // Tự động đăng nhập sau khi đăng ký
        String token = jwtTokenProvider.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}