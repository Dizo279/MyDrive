package com.mydrive.controller;

import com.mydrive.dto.request.ShareRequest;
import com.mydrive.dto.response.FileResponse;
import com.mydrive.dto.response.ShareResponse;
import com.mydrive.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.List;

@RestController
@RequestMapping("/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    // POST /api/share/user — chia sẻ với user khác
    @PostMapping("/user")
    public ResponseEntity<ShareResponse> shareWithUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ShareRequest request) {

        return ResponseEntity.status(201)
                .body(shareService.shareWithUser(userDetails.getUsername(), request));
    }

    // POST /api/share/public — tạo public link
    @PostMapping("/public")
    public ResponseEntity<ShareResponse> createPublicLink(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ShareRequest request) {

        return ResponseEntity.status(201)
                .body(shareService.createPublicLink(userDetails.getUsername(), request));
    }

    // GET /api/share/with-me — file được share với mình
    @GetMapping("/with-me")
    public ResponseEntity<List<ShareResponse>> getSharedWithMe(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(shareService.getSharedWithMe(userDetails.getUsername()));
    }

    // GET /api/share/my-shares — file mình đã share
    @GetMapping("/my-shares")
    public ResponseEntity<List<ShareResponse>> getMyShares(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(shareService.getMyShares(userDetails.getUsername()));
    }

    // DELETE /api/share/{id} — thu hồi share
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeShare(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        shareService.revokeShare(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/share/public/{token} — xem file qua public link (không cần đăng nhập)
    @GetMapping("/public/{token}")
    public ResponseEntity<FileResponse> getByPublicToken(@PathVariable String token) {
        return ResponseEntity.ok(shareService.getFileByPublicToken(token));
    }

    // GET /api/share/public/{token}/download — download qua public link
    @GetMapping("/public/{token}/download")
    public ResponseEntity<Resource> downloadByPublicToken(
            @PathVariable String token) throws MalformedURLException {

        Resource resource = shareService.downloadByPublicToken(token);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}