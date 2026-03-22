package com.mydrive.dto.response;

import com.mydrive.entity.SharedFile;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShareResponse {

    private Long id;
    private Long fileId;
    private String fileName;
    private String ownerUsername;
    private String sharedWithUsername;   // null nếu là public link
    private String permission;
    private String publicToken;          // null nếu share trực tiếp với user
    private String publicUrl;            // link đầy đủ để truy cập
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean expired;

    public static ShareResponse from(SharedFile share, String baseUrl) {
        String publicUrl = null;
        if (share.getPublicToken() != null) {
            publicUrl = baseUrl + "/share/public/" + share.getPublicToken();
        }

        return ShareResponse.builder()
                .id(share.getId())
                .fileId(share.getFile().getId())
                .fileName(share.getFile().getName())
                .ownerUsername(share.getOwner().getUsername())
                .sharedWithUsername(
                        share.getSharedWith() != null
                        ? share.getSharedWith().getUsername()
                        : null
                )
                .permission(share.getPermission().name())
                .publicToken(share.getPublicToken())
                .publicUrl(publicUrl)
                .expiresAt(share.getExpiresAt())
                .createdAt(share.getCreatedAt())
                .expired(share.isExpired())
                .build();
    }
}