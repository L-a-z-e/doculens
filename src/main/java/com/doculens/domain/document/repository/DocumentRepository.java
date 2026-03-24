package com.doculens.domain.document.repository;

import com.doculens.domain.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.doculens.domain.document.entity.DocumentStatus;

import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Page<Document> findByCollectionId(UUID collectionId, Pageable pageable);

    boolean existsByCollectionIdAndContentHash(UUID collectionId, String contentHash);

    long countByCollectionId(UUID collectionId);

    long countByStatus(DocumentStatus status);

    @Query("SELECT COALESCE(SUM(d.totalChunks), 0) FROM Document d")
    long sumTotalChunks();

    @Modifying
    @Query("DELETE FROM Document d WHERE d.collection.id = :collectionId")
    void deleteAllByCollectionId(@Param("collectionId") UUID collectionId);
}
