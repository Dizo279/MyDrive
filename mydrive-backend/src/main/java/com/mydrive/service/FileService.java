package com.mydrive.service;

import com.mydrive.dto.response.FileResponse;
import com.mydrive.entity.FileEntity;
import com.mydrive.entity.User;
import com.mydrive.entity.UserQuota;
import com.mydrive.exception.AppException;
import com.mydrive.repository.FileRepository;
import com.mydrive.repository.SharedFileRepository;
import com.mydrive.repository.UserQuotaRepository;
import com.mydrive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository      fileRepository;
    private final UserRepository      userRepository;
    private final UserQuotaRepository userQuotaRepository;
    private final SharedFileRepository sharedFileRepository;

    @Value("${app.storage.path}")
    private String storagePath;

    @Value("${app.storage.max-file-size}")
    private long maxFileSize;

    // ==================== UPLOAD ====================
    @Transactional
    public FileResponse upload(String username, MultipartFile file) throws IOException {
        User user = getUser(username);
        UserQuota quota = getQuota(user.getId());

        // Kiểm tra kích thước file
        if (file.getSize() > maxFileSize) {
            throw AppException.badRequest("File vượt quá kích thước cho phép (100MB)");
        }

        // Kiểm tra quota
        if (!quota.hasEnoughSpace(file.getSize())) {
            throw AppException.badRequest("Không đủ dung lượng lưu trữ");
        }

        // Tạo thư mục riêng cho từng user: uploads/{userId}/
        Path userDir = Paths.get(storagePath, String.valueOf(user.getId()));
        Files.createDirectories(userDir);

        // Đặt tên file ngẫu nhiên để tránh trùng
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String storedName = UUID.randomUUID() + extension;
        Path destination = userDir.resolve(storedName);

        // Lưu file vào disk
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        // Lưu metadata vào DB
        FileEntity fileEntity = FileEntity.builder()
                .user(user)
                .name(originalName)
                .storagePath(user.getId() + "/" + storedName)
                .sizeBytes(file.getSize())
                .mimeType(file.getContentType())
                .status(FileEntity.Status.ACTIVE)
                .build();
        fileRepository.save(fileEntity);

        // Cập nhật used_bytes trong quota
        quota.setUsedBytes(quota.getUsedBytes() + file.getSize());
        userQuotaRepository.save(quota);

        return FileResponse.from(fileEntity);
    }

    // ==================== DANH SÁCH FILE ====================
    public List<FileResponse> getMyFiles(String username) {
        User user = getUser(username);
        return fileRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), FileEntity.Status.ACTIVE)
                .stream()
                .map(FileResponse::from)
                .toList();
    }

    // ==================== CHI TIẾT FILE ====================
    public FileResponse getDetail(String username, Long fileId) {
        User user = getUser(username);
        FileEntity file = fileRepository.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> AppException.notFound("File không tồn tại"));
        return FileResponse.from(file);
    }

    // ==================== DOWNLOAD ====================
    public Resource download(String username, Long fileId) throws MalformedURLException {
        User user = getUser(username);
        FileEntity file = fileRepository.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> AppException.notFound("File không tồn tại"));

        if (file.getStatus() == FileEntity.Status.DELETED) {
            throw AppException.notFound("File không tồn tại");
        }

        Path filePath = Paths.get(storagePath).resolve(file.getStoragePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw AppException.notFound("Không thể đọc file");
        }

        return resource;
    }

    // ==================== XÓA FILE ====================
    @Transactional
    public void delete(String username, Long fileId) {
        User user = getUser(username);
        FileEntity file = fileRepository.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> AppException.notFound("File không tồn tại"));

        // Soft delete — chỉ đổi status, không xóa khỏi disk ngay
        file.setStatus(FileEntity.Status.DELETED);
        fileRepository.save(file);

        // Giảm used_bytes trong quota
        UserQuota quota = getQuota(user.getId());
        quota.setUsedBytes(Math.max(0, quota.getUsedBytes() - file.getSizeBytes()));
        userQuotaRepository.save(quota);
    }

    // ==================== HELPER ====================
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("User không tồn tại"));
    }

    private UserQuota getQuota(Long userId) {
        return userQuotaRepository.findByUserId(userId)
                .orElseThrow(() -> AppException.notFound("Quota không tồn tại"));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }


    // ==================== DOWNLOAD FILE ĐƯỢC SHARE ====================
    @Transactional(readOnly = true)
    public Resource downloadShared(String username, Long fileId) throws MalformedURLException {
        User user = getUser(username);

        // Kiểm tra file có được share với user này không và có quyền DOWNLOAD
        com.mydrive.entity.SharedFile share = sharedFileRepository
                .findByFileIdAndSharedWithIdAndPermission(fileId, user.getId(),
                        com.mydrive.entity.SharedFile.Permission.DOWNLOAD)
                .orElseThrow(() -> AppException.forbidden("Bạn không có quyền tải file này"));

        if (share.isExpired()) {
            throw AppException.badRequest("Quyền truy cập đã hết hạn");
        }

        com.mydrive.entity.FileEntity file = share.getFile();
        Path filePath = Paths.get(storagePath).resolve(file.getStoragePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw AppException.notFound("Không thể đọc file");
        }

        return resource;
    }
}
