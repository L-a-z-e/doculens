package com.doculens.domain.admin.service;

import com.doculens.domain.admin.dto.StatsResponse;
import com.doculens.domain.collection.repository.CollectionRepository;
import com.doculens.domain.document.entity.DocumentStatus;
import com.doculens.domain.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final CollectionRepository collectionRepository;
    private final DocumentRepository documentRepository;

    public StatsResponse getStats() {
        long totalDocs = documentRepository.count();
        long completedDocs = documentRepository.countByStatus(DocumentStatus.COMPLETED);
        long failedDocs = documentRepository.countByStatus(DocumentStatus.FAILED);
        long totalChunks = documentRepository.sumTotalChunks();

        var byCollection = collectionRepository.findAll().stream()
                .map(c -> new StatsResponse.CollectionCount(
                        c.getName(),
                        documentRepository.countByCollectionId(c.getId())))
                .toList();

        return new StatsResponse(
                new StatsResponse.DocumentStats(totalDocs, totalChunks, completedDocs, failedDocs),
                new StatsResponse.CollectionStats(collectionRepository.count(), byCollection)
        );
    }
}
