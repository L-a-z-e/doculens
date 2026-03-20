package com.doculens.domain.collection.service;

import com.doculens.domain.collection.dto.CollectionRequest;
import com.doculens.domain.collection.dto.CollectionResponse;
import com.doculens.domain.collection.entity.Collection;
import com.doculens.domain.collection.repository.CollectionRepository;
import com.doculens.domain.document.repository.DocumentRepository;
import com.doculens.global.error.exception.DuplicateResourceException;
import com.doculens.global.error.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final DocumentRepository documentRepository;

    @Transactional
    public CollectionResponse create(CollectionRequest request) {
        if (collectionRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("이미 존재하는 컬렉션 이름입니다: " + request.name());
        }

        Collection collection = Collection.builder()
                .name(request.name())
                .description(request.description())
                .build();

        return CollectionResponse.from(collectionRepository.save(collection));
    }

    public Page<CollectionResponse> findAll(Pageable pageable) {
        return collectionRepository.findAll(pageable)
                .map(CollectionResponse::from);
    }

    public CollectionResponse findById(UUID id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("컬렉션을 찾을 수 없습니다: " + id));
        return CollectionResponse.from(collection);
    }

    @Transactional
    public CollectionResponse update(UUID id, CollectionRequest request) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("컬렉션을 찾을 수 없습니다: " + id));

        if (!collection.getName().equals(request.name())
                && collectionRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("이미 존재하는 컬렉션 이름입니다: " + request.name());
        }

        collection.update(request.name(), request.description());
        return CollectionResponse.from(collection);
    }

    @Transactional
    public void delete(UUID id, boolean force) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("컬렉션을 찾을 수 없습니다: " + id));

        long documentCount = documentRepository.countByCollectionId(id);
        if (!force && documentCount > 0) {
            throw new IllegalStateException(
                    "컬렉션에 문서 " + documentCount + "개가 있습니다. 삭제하려면 force=true를 사용하세요");
        }

        if (force && documentCount > 0) {
            // TODO: vectorStore.delete()도 필요 — Phase 1-3에서 구현
            documentRepository.deleteAllByCollectionId(id);
        }
        collectionRepository.delete(collection);
    }
}
