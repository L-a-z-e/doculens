package com.doculens.domain.collection.controller;

import com.doculens.domain.collection.dto.CollectionRequest;
import com.doculens.domain.collection.dto.CollectionResponse;
import com.doculens.domain.collection.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping
    public ResponseEntity<CollectionResponse> create(@Valid @RequestBody CollectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<CollectionResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(collectionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(collectionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CollectionRequest request) {
        return ResponseEntity.ok(collectionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean force) {
        collectionService.delete(id, force);
        return ResponseEntity.noContent().build();
    }
}
