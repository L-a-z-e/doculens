package com.doculens.domain.admin.dto;

import java.util.List;

public record StatsResponse(
        DocumentStats documents,
        CollectionStats collections
) {
    public record DocumentStats(
            long total,
            long totalChunks,
            long completed,
            long failed
    ) {
    }

    public record CollectionStats(
            long total,
            List<CollectionCount> byCollection
    ) {
    }

    public record CollectionCount(
            String name,
            long documentCount
    ) {
    }
}
