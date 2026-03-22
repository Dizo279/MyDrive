package com.mydrive.service;

import com.mydrive.dto.request.ShareRequest;
import com.mydrive.dto.response.FileResponse;
import com.mydrive.dto.response.ShareResponse;
import com.mydrive.entity.FileEntity;
import com.mydrive.entity.SharedFile;
import com.mydrive.entity.User;
import com.mydrive.exception.AppException;
import com.mydrive.repository.FileRepository;
import com.mydrive.repository.SharedFileRepository;
import com.mydrive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final SharedFileRepository sharedFileRepository;
    private final FileRepository       fileRepository;
    private final UserRepository       userRepository;

    @Value("${app.storage.path}")
    private String storagePath;

    @Value("${app.base-url:http://localhost:8080/api}")
    private String baseUrl;

    // ==================== CHIA SẺ QUA EMAIL ====================
    @Transactional
    public ShareResponse shareWithUser(String ownerUsername, ShareRequest request) {
        User owner = getUser(ownerUsername);
        FileEntity file = getFileOfOwner(request.getFileId(), owner.getId());

        // Tìm user theo email
        String email = request.getSharedWithEmail();
        if (email == null || email.isBlank()) {
            throw AppException.badRequest("Vui lòng nhập email người nhận");
        }

        User sharedWith = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound(
                        "Không tìm thấy tài khoản với email: " + email));

        if (owner.getId().equals(sharedWith.getId())) {
            throw AppException.badRequest("Không thể chia sẻ file với chính mình");
        }

        if (sharedFileRepository.existsByFileIdAndSharedWithId(file.getId(), sharedWith.getId())) {
            throw AppException.conflict("File đã được chia sẻ với người dùng này");
        }

        SharedFile share = SharedFile.builder()
                .file(file)
                .owner(owner)
                .sharedWith(sharedWith)
                .permission(SharedFile.Permission.valueOf(
                        request.getPermission().toUpperCase()))
                .expiresAt(request.getExpireDays() != null
                        ? LocalDateTime.now().plusDays(request.getExpireDays())
                        : null)
                .build();

        sharedFileRepository.save(share);
        return ShareResponse.from(share, baseUrl);
    }

    // ==================== TẠO PUBLIC LINK ====================
    @Transactional
    public ShareResponse createPublicLink(String ownerUsername, ShareRequest request) {
        User owner = getUser(ownerUsername);
        FileEntity file = getFileOfOwner(request.getFileId(), owner.getId());

        SharedFile share = SharedFile.builder()
                .file(file)
                .owner(owner)
                .sharedWith(null)
                .permission(SharedFile.Permission.valueOf(
                        request.getPermission().toUpperCase()))
                .publicToken(UUID.randomUUID().toString().replace("-", ""))
                .expiresAt(request.getExpireDays() != null
                        ? LocalDateTime.now().plusDays(request.getExpireDays())
                        : null)
                .build();

        sharedFileRepository.save(share);
        return ShareResponse.from(share, baseUrl);
    }

    // ==================== FILE ĐƯỢC SHARE VỚI MÌNH ====================
    @Transactional(readOnly = true)
    public List<ShareResponse> getSharedWithMe(String username) {
        User user = getUser(username);
        return sharedFileRepository.findBySharedWithId(user.getId())
                .stream()
                .filter(s -> !s.isExpired())
                .map(s -> ShareResponse.from(s, baseUrl))
                .toList();
    }

    // ==================== FILE MÌNH ĐÃ SHARE ====================
    @Transactional(readOnly = true)
    public List<ShareResponse> getMyShares(String username) {
        User user = getUser(username);
        return sharedFileRepository.findByOwnerId(user.getId())
                .stream()
                .map(s -> ShareResponse.from(s, baseUrl))
                .toList();
    }

    // ==================== THU HỒI SHARE ====================
    @Transactional
    public void revokeShare(String username, Long shareId) {
        User user = getUser(username);
        SharedFile share = sharedFileRepository.findById(shareId)
                .orElseThrow(() -> AppException.notFound("Share không tồn tại"));

        if (!share.getOwner().getId().equals(user.getId())) {
            throw AppException.forbidden("Không có quyền thu hồi share này");
        }

        sharedFileRepository.delete(share);
    }

    // ==================== XEM FILE QUA PUBLIC LINK ====================
    @Transactional(readOnly = true)
    public FileResponse getFileByPublicToken(String token) {
        SharedFile share = sharedFileRepository.findByPublicToken(token)
                .orElseThrow(() -> AppException.notFound("Link không tồn tại"));

        if (share.isExpired()) {
            throw AppException.badRequest("Link đã hết hạn");
        }

        return FileResponse.from(share.getFile());
    }

    // ==================== DOWNLOAD QUA PUBLIC LINK ====================
    @Transactional(readOnly = true)
    public Resource downloadByPublicToken(String token) throws MalformedURLException {
        SharedFile share = sharedFileRepository.findByPublicToken(token)
                .orElseThrow(() -> AppException.notFound("Link không tồn tại"));

        if (share.isExpired()) {
            throw AppException.badRequest("Link đã hết hạn");
        }

        if (share.getPermission() != SharedFile.Permission.DOWNLOAD) {
            throw AppException.forbidden("Link này không có quyền download");
        }

        Path filePath = Paths.get(storagePath).resolve(share.getFile().getStoragePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw AppException.notFound("Không thể đọc file");
        }

        return resource;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("User không tồn tại"));
    }

    private FileEntity getFileOfOwner(Long fileId, Long ownerId) {
        return fileRepository.findByIdAndUserId(fileId, ownerId)
                .orElseThrow(() -> AppException.notFound("File không tồn tại hoặc không có quyền"));
    }
}