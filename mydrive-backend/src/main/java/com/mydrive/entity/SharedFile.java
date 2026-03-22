package com.mydrive.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shared_files")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // File được chia sẻ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    // Người chia sẻ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Người được chia sẻ — NULL nếu là public link
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_id")
    private User sharedWith;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Permission permission = Permission.VIEW;

    // Token dùng cho public link, null nếu share trực tiếp với user
    @Column(name = "public_token", unique = true, length = 100)
    private String publicToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Kiểm tra link còn hạn không
    public boolean isExpired() {
        if (expiresAt == null) return false;
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Kiểm tra đây có phải public link không
    public boolean isPublicLink() {
        return publicToken != null && sharedWith == null;
    }

    public enum Permission {
        VIEW, DOWNLOAD
    }
}