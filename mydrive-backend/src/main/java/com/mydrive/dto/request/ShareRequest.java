package com.mydrive.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShareRequest {

    @NotNull(message = "fileId không được để trống")
    private Long fileId;

    // Email người được chia sẻ
    private String sharedWithEmail;

    private String permission = "VIEW";

    private Integer expireDays;
}