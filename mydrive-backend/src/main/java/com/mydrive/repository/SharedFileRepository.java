package com.mydrive.repository;

import com.mydrive.entity.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {

    // Danh sách file người khác share cho mình
    @Query("SELECT s FROM SharedFile s JOIN FETCH s.file JOIN FETCH s.owner WHERE s.sharedWith.id = :userId")
    List<SharedFile> findBySharedWithId(Long userId);

    // Danh sách file mình đã share ra ngoài
    @Query("SELECT s FROM SharedFile s JOIN FETCH s.file JOIN FETCH s.owner WHERE s.owner.id = :ownerId")
    List<SharedFile> findByOwnerId(Long ownerId);

    // Tìm bằng public token (cho public link)
    @Query("SELECT s FROM SharedFile s JOIN FETCH s.file JOIN FETCH s.owner WHERE s.publicToken = :token")
    Optional<SharedFile> findByPublicToken(String token);

    // Kiểm tra file đã được share với user này chưa
    boolean existsByFileIdAndSharedWithId(Long fileId, Long sharedWithId);

    @Query("SELECT s FROM SharedFile s JOIN FETCH s.file JOIN FETCH s.owner WHERE s.file.id = :fileId AND s.sharedWith.id = :userId AND s.permission = :permission")
    java.util.Optional<SharedFile> findByFileIdAndSharedWithIdAndPermission(Long fileId, Long userId, SharedFile.Permission permission);
}