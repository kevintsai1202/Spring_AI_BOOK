package com.example.enterprise.repository;

import com.example.enterprise.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 檔案元資料存取介面
 *
 * @author Kevin Tsai
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByStoredFilename(String storedFilename);
}
