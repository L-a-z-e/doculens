package com.doculens.domain.document.dto;

import com.doculens.domain.document.entity.Document;
import com.doculens.domain.document.entity.DocumentStatus;
import com.doculens.domain.document.entity.SourceType;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID collectionId,
        String title,
        SourceType sourceType,
        DocumentStatus status,
        int totalChunks,
        Long fileSize,
        String errorMessage,
        LocalDateTime createdAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getCollection().getId(),
                document.getTitle(),
                document.getSourceType(),
                document.getStatus(),
                document.getTotalChunks(),
                document.getFileSize(),
                document.getErrorMessage(),
                document.getCreatedAt()
        );
    }
}
