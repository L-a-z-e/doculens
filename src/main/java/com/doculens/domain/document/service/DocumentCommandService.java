package com.doculens.domain.document.service;

import com.doculens.domain.collection.entity.Collection;
import com.doculens.domain.collection.repository.CollectionRepository;
import com.doculens.domain.document.dto.DocumentResponse;
import com.doculens.domain.document.entity.Document;
import com.doculens.domain.document.entity.SourceType;
import com.doculens.domain.document.repository.DocumentRepository;
import com.doculens.global.error.exception.DuplicateResourceException;
import com.doculens.global.error.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentCommandService {

    private final DocumentRepository documentRepository;
    private final CollectionRepository collectionRepository;

    @Transactional
    public DocumentResponse save(UUID collectionId, String title, SourceType sourceType,
                                 String contentHash, Long fileSize) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("컬렉션을 찾을 수 없습니다: " + collectionId));

        if (documentRepository.existsByCollectionIdAndContentHash(collectionId, contentHash)) {
            throw new DuplicateResourceException("이 컬렉션에 이미 업로드된 문서입니다");
        }

        Document document = Document.builder()
                .collection(collection)
                .title(title)
                .sourceType(sourceType)
                .contentHash(contentHash)
                .fileSize(fileSize)
                .build();

        return DocumentResponse.from(documentRepository.save(document));
    }

    @Transactional
    public void delete(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("문서를 찾을 수 없습니다: " + id));

        // TODO: vectorStore.delete() — Phase 1-3에서 구현
        documentRepository.delete(document);
    }
}
