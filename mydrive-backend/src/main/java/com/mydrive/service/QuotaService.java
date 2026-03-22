package com.mydrive.service;

import com.mydrive.dto.response.QuotaResponse;
import com.mydrive.entity.User;
import com.mydrive.entity.UserQuota;
import com.mydrive.exception.AppException;
import com.mydrive.repository.UserQuotaRepository;
import com.mydrive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuotaService {

    private final UserQuotaRepository userQuotaRepository;
    private final UserRepository      userRepository;

    // Xem quota của bản thân
    public QuotaResponse getMyQuota(String username) {
        User user = getUser(username);
        UserQuota quota = getQuota(user.getId());
        return QuotaResponse.from(quota);
    }

    // Nâng cấp plan (admin gọi cho user, hoặc user tự nâng cấp)
    @Transactional
    public QuotaResponse upgradePlan(String username, String plan) {
        User user = getUser(username);
        UserQuota quota = getQuota(user.getId());

        UserQuota.Plan newPlan;
        long newQuotaBytes;

        switch (plan.toUpperCase()) {
            case "PRO" -> {
                newPlan = UserQuota.Plan.PRO;
                newQuotaBytes = 10_737_418_240L;   // 10GB
            }
            case "FREE" -> {
                newPlan = UserQuota.Plan.FREE;
                newQuotaBytes = 5_368_709_120L;    // 5GB
            }
            default -> throw AppException.badRequest("Plan không hợp lệ. Chọn FREE hoặc PRO");
        }

        quota.setPlan(newPlan);
        quota.setQuotaBytes(newQuotaBytes);
        userQuotaRepository.save(quota);

        return QuotaResponse.from(quota);
    }

    // Admin xem quota của bất kỳ user nào
    public QuotaResponse getQuotaByUserId(Long userId) {
        UserQuota quota = getQuota(userId);
        return QuotaResponse.from(quota);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("User không tồn tại"));
    }

    private UserQuota getQuota(Long userId) {
        return userQuotaRepository.findByUserId(userId)
                .orElseThrow(() -> AppException.notFound("Quota không tồn tại"));
    }
}