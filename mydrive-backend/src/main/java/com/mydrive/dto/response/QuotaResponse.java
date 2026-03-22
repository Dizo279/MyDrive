package com.mydrive.dto.response;

import com.mydrive.entity.UserQuota;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuotaResponse {

    private Long quotaBytes;
    private Long usedBytes;
    private Long freeBytes;
    private double usedPercent;
    private String plan;

    public static QuotaResponse from(UserQuota quota) {
        return QuotaResponse.builder()
                .quotaBytes(quota.getQuotaBytes())
                .usedBytes(quota.getUsedBytes())
                .freeBytes(quota.getQuotaBytes() - quota.getUsedBytes())
                .usedPercent(Math.round(quota.getUsedPercent() * 10.0) / 10.0)
                .plan(quota.getPlan().name())
                .build();
    }
}