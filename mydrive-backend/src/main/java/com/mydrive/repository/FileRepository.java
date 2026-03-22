package com.mydrive.repository;

import com.mydrive.entity.FileEntity;
import com.mydrive.entity.FileEntity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    // Lấy toàn bộ file ACTIVE của một user — JOIN FETCH tránh N+1 query
    @Query("SELECT f FROM FileEntity f JOIN FETCH f.user WHERE f.user.id = :userId AND f.status = :status ORDER BY f.createdAt DESC")
    List<FileEntity> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Status status);

    // Tìm file theo id và user — JOIN FETCH tránh LazyInitializationException
    @Query("SELECT f FROM FileEntity f JOIN FETCH f.user WHERE f.id = :id AND f.user.id = :userId")
    Optional<FileEntity> findByIdAndUserId(Long id, Long userId);

    // Tính tổng dung lượng file ACTIVE của một user
    @Query("SELECT COALESCE(SUM(f.sizeBytes), 0) FROM FileEntity f WHERE f.user.id = :userId AND f.status = 'ACTIVE'")
    Long sumSizeByUserId(Long userId);
}