package com.mydrive.dto.response;

import com.mydrive.entity.FileEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileResponse {

    private Long id;
    private String name;
    private Long sizeBytes;
    private String mimeType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ownerUsername;

    // Chuyển từ entity sang DTO — không để lộ storagePath ra ngoài
    public static FileResponse from(FileEntity file) {
        return FileResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .sizeBytes(file.getSizeBytes())
                .mimeType(file.getMimeType())
                .status(file.getStatus().name())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .ownerUsername(file.getUser().getUsername())
                .build();
    }
}