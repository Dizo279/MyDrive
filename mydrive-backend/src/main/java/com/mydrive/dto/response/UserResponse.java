package com.mydrive.dto.response;

import com.mydrive.entity.User;
import com.mydrive.entity.UserQuota;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long   id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    private Long   quotaBytes;
    private Long   usedBytes;
    private Long   freeBytes;
    private double usedPercent;
    private String plan;
    private int    fileCount;

    public static UserResponse from(User user, UserQuota quota, int fileCount) {
        long used  = quota != null ? quota.getUsedBytes()  : 0L;
        long total = quota != null ? quota.getQuotaBytes() : 0L;
        double pct = total > 0 ? Math.round((used * 100.0 / total) * 10.0) / 10.0 : 0;

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .quotaBytes(total)
                .usedBytes(used)
                .freeBytes(total - used)
                .usedPercent(pct)
                .plan(quota != null ? quota.getPlan().name() : "FREE")
                .fileCount(fileCount)
                .build();
    }
}