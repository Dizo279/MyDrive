package com.mydrive.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_quota")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ 1-1 với User, user_id là foreign key
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "quota_bytes", nullable = false)
    @Builder.Default
    private Long quotaBytes = 5_368_709_120L;   // 5GB mặc định

    @Column(name = "used_bytes", nullable = false)
    @Builder.Default
    private Long usedBytes = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Plan plan = Plan.FREE;

    // Tính % đã dùng — dùng trong QuotaService
    public double getUsedPercent() {
        if (quotaBytes == 0) return 0;
        return (usedBytes * 100.0) / quotaBytes;
    }

    // Kiểm tra còn đủ chỗ để upload không
    public boolean hasEnoughSpace(long fileSizeBytes) {
        return (usedBytes + fileSizeBytes) <= quotaBytes;
    }

    public enum Plan {
        FREE, PRO, ADMIN
    }
}