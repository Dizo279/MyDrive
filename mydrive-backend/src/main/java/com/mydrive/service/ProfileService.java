package com.mydrive.service;

import com.mydrive.dto.request.UpdateProfileRequest;
import com.mydrive.dto.response.AuthResponse;
import com.mydrive.entity.User;
import com.mydrive.exception.AppException;
import com.mydrive.repository.UserRepository;
import com.mydrive.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository   userRepository;
    private final PasswordEncoder  passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Xem thông tin bản thân
    public AuthResponse getProfile(String username) {
        User user = getUser(username);
        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    // Cập nhật thông tin — luôn yêu cầu currentPassword
    @Transactional
    public AuthResponse updateProfile(String currentUsername, UpdateProfileRequest request) {
        User user = getUser(currentUsername);

        // Xác minh mật khẩu hiện tại
        if (request.getCurrentPassword() == null ||
                !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw AppException.badRequest("Mật khẩu hiện tại không đúng");
        }

        // Đổi username
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw AppException.conflict("Username đã tồn tại");
            }
            user.setUsername(request.getUsername());
        }

        // Đổi email
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw AppException.conflict("Email đã được sử dụng");
            }
            user.setEmail(request.getEmail());
        }

        // Đổi password
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);

        // Tạo token mới vì username có thể đã thay đổi
        String newToken = jwtTokenProvider.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(newToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("User không tồn tại"));
    }
}