package com.doculens.domain.collection.dto;

import com.doculens.domain.collection.entity.Collection;

import java.time.LocalDateTime;
import java.util.UUID;

public record CollectionResponse(
        UUID id,
        String name,
        String description,
        long documentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CollectionResponse from(Collection collection, long documentCount) {
        return new CollectionResponse(
                collection.getId(),
                collection.getName(),
                collection.getDescription(),
                documentCount,
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }

    public static CollectionResponse from(Collection collection) {
        return from(collection, 0);
    }
}
