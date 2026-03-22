package com.mydrive.service;

import com.mydrive.dto.response.UserResponse;
import com.mydrive.entity.User;
import com.mydrive.entity.UserQuota;
import com.mydrive.exception.AppException;
import com.mydrive.repository.FileRepository;
import com.mydrive.repository.UserQuotaRepository;
import com.mydrive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository      userRepository;
    private final UserQuotaRepository userQuotaRepository;
    private final FileRepository      fileRepository;

    // ==================== DANH SÁCH USER ====================
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserQuota quota = userQuotaRepository.findByUserId(user.getId()).orElse(null);
            int fileCount   = fileRepository
                    .findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(),
                            com.mydrive.entity.FileEntity.Status.ACTIVE)
                    .size();
            return UserResponse.from(user, quota, fileCount);
        }).toList();
    }

    // ==================== CHI TIẾT USER ====================
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user     = getUser(userId);
        UserQuota quota = userQuotaRepository.findByUserId(userId).orElse(null);
        int fileCount   = fileRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId,
                        com.mydrive.entity.FileEntity.Status.ACTIVE)
                .size();
        return UserResponse.from(user, quota, fileCount);
    }

    // ==================== CẬP NHẬT QUOTA ====================
    @Transactional
    public UserResponse updateQuota(Long userId, String plan, Long customBytes) {
        User user       = getUser(userId);
        UserQuota quota = userQuotaRepository.findByUserId(userId)
                .orElseThrow(() -> AppException.notFound("Quota không tồn tại"));

        if (plan != null) {
            switch (plan.toUpperCase()) {
                case "FREE"  -> { quota.setPlan(UserQuota.Plan.FREE);  quota.setQuotaBytes(5_368_709_120L); }
                case "PRO"   -> { quota.setPlan(UserQuota.Plan.PRO);   quota.setQuotaBytes(10_737_418_240L); }
                case "ADMIN" -> { quota.setPlan(UserQuota.Plan.ADMIN); quota.setQuotaBytes(107_374_182_400L); }
                default      -> throw AppException.badRequest("Plan không hợp lệ");
            }
        }

        if (customBytes != null && customBytes > 0) {
            quota.setQuotaBytes(customBytes);
        }

        userQuotaRepository.save(quota);

        int fileCount = fileRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId,
                        com.mydrive.entity.FileEntity.Status.ACTIVE)
                .size();
        return UserResponse.from(user, quota, fileCount);
    }

    // ==================== XÓA USER ====================
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUser(userId);
        if (user.getRole() == User.Role.ADMIN) {
            throw AppException.badRequest("Không thể xóa tài khoản ADMIN");
        }
        userRepository.delete(user);
    }

    // ==================== ĐỔI ROLE ====================
    @Transactional
    public UserResponse updateRole(Long userId, String role) {
        User user = getUser(userId);
        try {
            user.setRole(User.Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Role không hợp lệ. Dùng USER hoặc ADMIN");
        }
        userRepository.save(user);

        UserQuota quota = userQuotaRepository.findByUserId(userId).orElse(null);
        int fileCount   = fileRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId,
                        com.mydrive.entity.FileEntity.Status.ACTIVE)
                .size();
        return UserResponse.from(user, quota, fileCount);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("User không tồn tại"));
    }
}